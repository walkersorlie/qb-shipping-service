package com.walkersorlie.qbshippingservice;

import com.walkersorlie.qbshippingservice.dialogs.EditItemDialog;
import com.walkersorlie.qbshippingservice.entities.Product;
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
    private JButton calculateOrder;
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

    private void createUIComponents() {
        productList = productRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));

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

        productTable.getModel().addTableModelListener(new TableModelListenerCust());
        productTableSorter = new TableRowSorter<>((ProductTableModel) productTable.getModel());
        productTable.setRowSorter(productTableSorter);

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

    private void createListeners() {
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

        productTable.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    JTable target = (JTable) e.getSource();
                    int column = target.getSelectedColumn();

                    if (column == 0) {  // only want to allow double click on the description column (column zero)
                        int row = target.getSelectedRow();
                        ProductTableModel tm = (ProductTableModel) target.getModel();
                        Product product = tm.getProductAt(row);
                        editItemDialogBox = new EditItemDialog(productRepository, product);
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

        calculateOrder.addActionListener(e -> {
            ComputeTableModel cm = (ComputeTableModel) computeTable.getModel();
            double totalOrderShippingCost = Double.parseDouble(shippingCostTextField.getText().strip());
            double totalOrderWeight = Double.parseDouble(shippingWeightTextField.getText().strip());

            // calculate total weight for each item (quantity * weight)
            // (item total weight / order total weight) * total shipping cost

            for (int i = 0; i < cm.getRowCount(); i++) {
                int quantity = (int) cm.getValueAt(i, 1);
                double cost = (double) cm.getValueAt(i, 2);
                double weight = (double) cm.getValueAt(i, 3);

                double totalItemWeight = quantity * weight;
                double costPerItem = (totalItemWeight / totalOrderWeight) * totalOrderShippingCost;
                cm.setValueAt(costPerItem, i, 4);
            }
        });
    }

    class TableModelListenerCust implements TableModelListener {
        @Override
        public void tableChanged(TableModelEvent e) {
            int row = e.getFirstRow();
            int col = e.getColumn();
            ProductTableModel tm = (ProductTableModel) e.getSource();
            boolean selected = (boolean) tm.getValueAt(row, col);

            if (selectedItemsList.contains(tm.getProductAt(row)) && !selected)
                selectedItemsList.remove(tm.getProductAt(row));

            if (selected)
                selectedItemsList.add(tm.getProductAt(row));

            computeTable.setModel(new ComputeTableModel(selectedItemsList));
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
        calculateOrder = new JButton();
        calculateOrder.setText("Calculate Order");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelMain.add(calculateOrder, gbc);
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
