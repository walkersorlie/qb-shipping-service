package com.walkersorlie.qbshippingservice;

import com.walkersorlie.qbshippingservice.dialogs.EditItemDialog;
import com.walkersorlie.qbshippingservice.entities.Product;
import com.walkersorlie.qbshippingservice.entities.ProductCost;
import com.walkersorlie.qbshippingservice.repositories.ProductRepository;
import com.walkersorlie.qbshippingservice.tablemodels.ComputeTableModel;
import com.walkersorlie.qbshippingservice.tablemodels.ProductTableModel;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableMongoRepositories
public class ApplicationUI extends JFrame {

    private final ProductRepository productRepository;
    private List<Product> productList;
    private ArrayList<Product> selectedItemsList;
    private JPanel panelMain;
    private JTable computeTable;
    private JTable productTable;
    private TableRowSorter<ProductTableModel> productTableSorter;
    private JButton addToCompute;
    private JScrollPane scrollPaneProductTable;
    private JScrollPane scrollPaneComputeTable;
    private JTextField shippingCostTextField;
    private JTextField shippingWeightTextField;
    private JLabel shippingWeightLabel;
    private JLabel shippingCostLabel;
    private JButton calculateOrderButton;
    private JTextField searchTextField;
    private JLabel searchFieldLabel;
    private EditItemDialog editItemDialogBox;
    private ListSelectionModel productTableSelectionModel;

    public ApplicationUI(ProductRepository productRepository) {
        this.productRepository = productRepository;

        $$$setupUI$$$();
        createListeners();

        JFrame frame = new JFrame("QB Shipping Costs");
        frame.setContentPane(panelMain);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * IntelliJ requires this method if in 'GUI Designer' in 'Settings', the 'Generate GUI into: Java Source Code'
     * is selected
     */
    private void createUIComponents() {
        productList = productRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));

        // creates the product table and sets the column width to the width of the data
        productTable = new JTable(new ProductTableModel(new ArrayList<>(productList))) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component component = super.prepareRenderer(renderer, row, column);
                int rendererWidth = component.getPreferredSize().width;
                TableColumn tableColumn = getColumnModel().getColumn(column);
                tableColumn.setPreferredWidth(Math.max(rendererWidth + getIntercellSpacing().width, tableColumn.getPreferredWidth()));
                return component;
            }
        };

        productTable.getModel().addTableModelListener(new ProductTableModelListener());
        productTableSorter = new TableRowSorter<>((ProductTableModel) productTable.getModel());
        productTable.setRowSorter(productTableSorter);

        // creates the compute table and sets the column width to the width of the data
        computeTable = new JTable(new ComputeTableModel(new ArrayList<>())) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component component = super.prepareRenderer(renderer, row, column);
                int rendererWidth = component.getPreferredSize().width;
                TableColumn tableColumn = getColumnModel().getColumn(column);
                tableColumn.setPreferredWidth(Math.max(rendererWidth + getIntercellSpacing().width, tableColumn.getPreferredWidth()));
                return component;
            }
        };

        selectedItemsList = new ArrayList<>();
    }

    /**
     * Creates listeners for components
     */
    private void createListeners() {
        // table sorter to filter the product table to show search results from search text field
        searchTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                String str = searchTextField.getText();
                if (str.trim().length() == 0) {
                    productTableSorter.setRowFilter(null);
                } else
                    productTableSorter.setRowFilter(RowFilter.regexFilter("(?i)" + str));    //(?i) case insensitive search
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                String str = searchTextField.getText();
                if (str.trim().length() == 0) {
                    productTableSorter.setRowFilter(null);
                } else
                    productTableSorter.setRowFilter(RowFilter.regexFilter("(?i)" + str));
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

        // opens a dialog to edit an item if the product description column is double clicked
        productTable.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    JTable target = (JTable) e.getSource();
                    int column = target.getSelectedColumn();

                    // only want to allow double click on the description column (column zero)
                    if (column == 0) {
                        int row = target.getSelectedRow();
                        ProductTableModel productTableModel = (ProductTableModel) target.getModel();
                        Product productToEdit = productTableModel.getProductAt(row);
                        editItemDialogBox = new EditItemDialog(productRepository, productToEdit);
                        Product updatedProduct = editItemDialogBox.getUpdatedProduct();

                        // now check if this item is in selectedItemsList
                        // if it is, replace the item in selectedItemsList with updated item
                        // then update computeTable table model to reflect changes
                        if (selectedItemsList.stream()
                                .map((Product::getId))
                                .collect(Collectors.toList())
                                .contains(updatedProduct.getId())) {

                            for (int i = 0; i < selectedItemsList.size(); i++) {
                                if (selectedItemsList.get(i).getId().equals(updatedProduct.getId())) {
                                    selectedItemsList.set(i, updatedProduct);
                                    computeTable.setModel(new ComputeTableModel(selectedItemsList));
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        });

        // calculates the cost per item for an order; includes shipping cost in calculation
        calculateOrderButton.addActionListener(e -> {
            try {
                ComputeTableModel computeTableModel = (ComputeTableModel) computeTable.getModel();
                double totalOrderShippingCost = Double.parseDouble(shippingCostTextField.getText().strip());
                double totalOrderWeight = Double.parseDouble(shippingWeightTextField.getText().strip());

                // calculate total weight for each item (quantity * weight)
                // (item total weight / order total weight) * total shipping cost
                for (int rowIndex = 0; rowIndex < computeTableModel.getRowCount(); rowIndex++) {
                    int quantity = (int) computeTableModel.getValueAt(rowIndex, 1);
                    double computeTableProductCost = (double) computeTableModel.getValueAt(rowIndex, 2);
                    double computeTableProductWeight = (double) computeTableModel.getValueAt(rowIndex, 3);

                    double totalItemWeight = quantity * computeTableProductWeight;
                    double costPerItem = (totalItemWeight / totalOrderWeight) * totalOrderShippingCost;
                    computeTableModel.setValueAt(costPerItem, rowIndex, 4);

                    // Product at the current row
                    Product rowProduct = (Product) computeTableModel.getRow(rowIndex)[0];
                    boolean updateRowProduct = false;

                    // check if the cost of the product in the compute table is different than what is in the DB
                    // if so, create new ProductCost entry
                    if (computeTableProductCost != rowProduct.getCost()) {
                        ProductCost productCost = new ProductCost(
                                LocalDate.now().toString(),
                                computeTableProductCost,
                                rowProduct.getId());

                        // replace old cost with new cost
                        rowProduct.setCost(computeTableProductCost);

                        // add productCost to rowProduct.productCost
                        rowProduct.addProductCost(productCost);
                        updateRowProduct = true;
                    }

                    // check if the weight of the product in compute table is different than what is in the DB
                    if (computeTableProductWeight != rowProduct.getWeight()) {
                        rowProduct.setWeight(computeTableProductWeight);
                        updateRowProduct = true;
                    }

                    if (updateRowProduct) {
                        // pass the product to DB save() method because either weight or product_cost was changed
                        Product updatedRowProduct = productRepository.save(rowProduct);

                        // replace the old Product at row rowIndex with the freshly updated Product from DB
                        // this Product includes updated product_cost entities
                        computeTableModel.setRow(rowIndex, new Object[]{
                                updatedRowProduct,
                                quantity,
                                updatedRowProduct.getCost(),
                                updatedRowProduct.getWeight(),
                                costPerItem
                        });
                    }
                }
            } catch (NumberFormatException ex) {
                String message;
                JTextField textFieldToRequestFocus;

                if (shippingCostTextField.getText().isBlank()) {
                    message = "Enter a valid shipping cost";
                    textFieldToRequestFocus = shippingCostTextField;
                } else {
                    message = "Enter a valid order weight";
                    textFieldToRequestFocus = shippingWeightTextField;
                }

                JOptionPane.showMessageDialog(null, message);
                textFieldToRequestFocus.requestFocus();
            }
        });
    }

    /**
     * Listener that listens to changes in productTable
     * Adds products to selectedItemsList if the product checkbox is selected
     * Meaning that the product is to be included in the order
     */
    class ProductTableModelListener implements TableModelListener {
        @Override
        public void tableChanged(TableModelEvent e) {
            int row = e.getFirstRow();
            int col = e.getColumn();
            ProductTableModel productTableModel = (ProductTableModel) e.getSource();
            boolean productCheckboxIsSelected = (boolean) productTableModel.getValueAt(row, col);
            boolean selectedItemsListIsUpdated = false;

            // checks if the product was previously in selectedItemsList but is no longer in the list
            // product checkbox was selected but is no longer selected
            // if so, removes it from selectedItemsList
            if (selectedItemsList.contains(productTableModel.getProductAt(row)) && !productCheckboxIsSelected) {
                selectedItemsList.remove(productTableModel.getProductAt(row));
                selectedItemsListIsUpdated = true;
            }

            // if the checkbox of the product is selected, adds product to selectedItemsList
            if (productCheckboxIsSelected) {
                selectedItemsList.add(productTableModel.getProductAt(row));
                selectedItemsListIsUpdated = true;
            }

            // check if selectedItemsList is actually updated
            if (selectedItemsListIsUpdated) {
                // creates a new computeTableModel with the updated selectedItemsList
                computeTable.setModel(new ComputeTableModel(selectedItemsList));
            }
        }
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        panelMain = new JPanel();
        panelMain.setLayout(new GridBagLayout());
        scrollPaneProductTable = new JScrollPane();
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.5;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.VERTICAL;
        panelMain.add(scrollPaneProductTable, gbc);
        scrollPaneProductTable.setBorder(BorderFactory.createTitledBorder(null, "Product List", TitledBorder.CENTER, TitledBorder.TOP, null, null));
        productTable.setAutoResizeMode(2);
        productTable.setFillsViewportHeight(true);
        productTable.setPreferredScrollableViewportSize(new Dimension(450, 670));
        scrollPaneProductTable.setViewportView(productTable);
        scrollPaneComputeTable = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 0.5;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.VERTICAL;
        panelMain.add(scrollPaneComputeTable, gbc);
        scrollPaneComputeTable.setBorder(BorderFactory.createTitledBorder(null, "Order", TitledBorder.CENTER, TitledBorder.TOP, null, null));
        computeTable.setAutoResizeMode(4);
        computeTable.setFillsViewportHeight(true);
        computeTable.setPreferredScrollableViewportSize(new Dimension(620, 400));
        scrollPaneComputeTable.setViewportView(computeTable);
        addToCompute = new JButton();
        addToCompute.setText("Make Order");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelMain.add(addToCompute, gbc);
        shippingWeightTextField = new JTextField();
        shippingWeightTextField.setHorizontalAlignment(4);
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelMain.add(shippingWeightTextField, gbc);
        shippingCostTextField = new JTextField();
        shippingCostTextField.setHorizontalAlignment(4);
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelMain.add(shippingCostTextField, gbc);
        shippingWeightLabel = new JLabel();
        shippingWeightLabel.setText("Total Shipping Weight");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panelMain.add(shippingWeightLabel, gbc);
        shippingCostLabel = new JLabel();
        shippingCostLabel.setHorizontalAlignment(10);
        shippingCostLabel.setHorizontalTextPosition(11);
        shippingCostLabel.setText("Total Shipping Cost");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panelMain.add(shippingCostLabel, gbc);
        calculateOrderButton = new JButton();
        calculateOrderButton.setText("Calculate Order");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelMain.add(calculateOrderButton, gbc);
        searchTextField = new JTextField();
        searchTextField.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelMain.add(searchTextField, gbc);
        searchFieldLabel = new JLabel();
        searchFieldLabel.setText("Search Field");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panelMain.add(searchFieldLabel, gbc);
        shippingWeightLabel.setLabelFor(shippingWeightTextField);
        shippingCostLabel.setLabelFor(shippingCostTextField);
        searchFieldLabel.setLabelFor(searchTextField);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panelMain;
    }
}