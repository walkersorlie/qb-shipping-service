package com.walkersorlie.qbshippingservice;

import com.walkersorlie.qbshippingservice.dialogs.EditItemDialog;
import com.walkersorlie.qbshippingservice.entities.Product;
import com.walkersorlie.qbshippingservice.entities.ProductCost;
import com.walkersorlie.qbshippingservice.exceptions.OrderWeightException;
import com.walkersorlie.qbshippingservice.exceptions.ProductNotFoundException;
import com.walkersorlie.qbshippingservice.exceptions.TextFieldValueExeption;
import com.walkersorlie.qbshippingservice.repositories.ProductRepository;
import com.walkersorlie.qbshippingservice.tablemodels.ComputeTableModel;
import com.walkersorlie.qbshippingservice.tablemodels.ProductTableModel;
import org.jetbrains.annotations.NotNull;
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
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableMongoRepositories
public class ApplicationUI extends JFrame {

    private final ProductRepository productRepository;
    private List<Product> productList;
    private ArrayList<Product> selectedItemsList;
    private ArrayList<Product> updatedProductsToSave;
    private JPanel panelMain;
    private RXTable computeTable;
    private JTable productTable;
    private TableRowSorter<ProductTableModel> productTableSorter;
    private JScrollPane scrollPaneProductTable;
    private JScrollPane scrollPaneComputeTable;
    private JTextField shippingCostTextField;
    private JTextField shippingWeightTextField;
    private JLabel shippingWeightLabel;
    private JLabel shippingCostLabel;
    private JButton calculateOrderButton;
    private JTextField searchTextField;
    private JLabel searchFieldLabel;
    private JTextField subtotalTextField;
    private JLabel subtotalLabel;
    private JTextField orderTotalCostTextField;
    private JLabel orderTotalCostLabel;
    private JButton saveUpdatedProductsAfterCalculateOrderButton;
    private JButton clearProductsButton;
    private EditItemDialog editItemDialogBox;
    private ListSelectionModel productTableSelectionModel;
    private JPopupMenu computeTablePopupMenu;

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
        computeTable = new RXTable(new ComputeTableModel(new ArrayList<>())) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component component = super.prepareRenderer(renderer, row, column);
                int rendererWidth = component.getPreferredSize().width;
                TableColumn tableColumn = getColumnModel().getColumn(column);
                tableColumn.setPreferredWidth(Math.max(rendererWidth + getIntercellSpacing().width, tableColumn.getPreferredWidth()));
                return component;
            }
        };
        computeTable.setSelectAllForEdit(true);
        computeTable.getModel().addTableModelListener(new ComputeTableModelListener());

        selectedItemsList = new ArrayList<>();
        updatedProductsToSave = new ArrayList<>();
    }


    /**
     * Creates listeners for components
     * @// TODO: 2022-10-08 Add a text field to show the app calculated order total
     */
    private void createListeners() {
        subtotalTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                subtotalTextField.selectAll();
            }

            @Override
            public void focusLost(FocusEvent e) {
                subtotalTextField.select(0, 0);
                autoPopulateOrderTotalTextField();
            }
        });

        shippingCostTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                shippingCostTextField.selectAll();
            }

            @Override
            public void focusLost(FocusEvent e) {
                shippingCostTextField.select(0, 0);
                autoPopulateOrderTotalTextField();
            }
        });

        shippingWeightTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                shippingWeightTextField.selectAll();
            }

            @Override
            public void focusLost(FocusEvent e) {
                shippingWeightTextField.select(0, 0);
            }
        });

        orderTotalCostTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                orderTotalCostTextField.selectAll();
            }

            @Override
            public void focusLost(FocusEvent e) {
                orderTotalCostTextField.select(0, 0);
            }
        });

        searchTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                searchTextField.selectAll();
            }

            @Override
            public void focusLost(FocusEvent e) {
                searchTextField.select(0, 0);
            }
        });

        // table sorter to filter the product table to show search results from search text field
        searchTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                String str = searchTextField.getText();
                if (str.trim().length() == 0)
                    productTableSorter.setRowFilter(null);
                else
                    productTableSorter.setRowFilter(RowFilter.regexFilter("(?i)" + str));    //(?i) case insensitive search
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                String str = searchTextField.getText();
                if (str.trim().length() == 0)
                    productTableSorter.setRowFilter(null);
                else
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
//                        int row = target.getSelectedRow();
                        int row = target.convertRowIndexToModel(target.getSelectedRow());
                        ProductTableModel productTableModel = (ProductTableModel) target.getModel();
                        Product productToEdit = productTableModel.getProductAt(row);
//                        Product productToEdit = productRepository.findById(productTableModel.getProductAt(row).getId()).get();
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
        calculateOrderButton.addActionListener(this::calculateOrderButtonListenerImpl);

        saveUpdatedProductsAfterCalculateOrderButton.addActionListener(e -> {
            try {
                int option = JOptionPane.showConfirmDialog(null, "Save any updated products?");
                if (option == 0) {
                    // pass the product to DB save() method because either weight or product_cost was changed
                    ArrayList<Product> updatedProducts = (ArrayList<Product>) productRepository.saveAll(updatedProductsToSave);

                    // update productList here with updated products
                    for (int i = 0; i <= productList.size(); i++) {
                        if (updatedProducts.contains(productList.get(i)))
                            productList.set(i, updatedProducts.get(updatedProducts.indexOf(productList.get(i))));
                    }

                    // clear the updatedProductsToSave list because the products have been saved
                    updatedProductsToSave.clear();
                }
            } catch (IndexOutOfBoundsException ex) {
                JOptionPane.showMessageDialog(null, "No products are updated so nothing to save in the database");
            }
        });

        clearProductsButton.addActionListener(e -> {
            int option = JOptionPane.showConfirmDialog(null, "Clear all products from compute table?");
            if (option == 0) {
                ComputeTableModel computeTableModel = (ComputeTableModel) computeTable.getModel();
                computeTableModel.removeAllRows();

                ProductTableModel productTableModel = (ProductTableModel) productTable.getModel();

                for (int i = 0; i < productTableModel.getRowCount(); i++) {
                    productTableModel.setValueAt(false, i, 1);
                }

                resetTextFields();
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
            ComputeTableModel computeTableModel = (ComputeTableModel) computeTable.getModel();
            boolean productCheckboxIsSelected = (boolean) productTableModel.getValueAt(row, col);
            boolean selectedItemsListIsUpdated = false;

            // checks if the product was previously in selectedItemsList but is no longer in the list
            // product checkbox was selected but is no longer selected
            // if so, removes it from selectedItemsList
            if (selectedItemsList.contains(productTableModel.getProductAt(row)) && !productCheckboxIsSelected) {
                selectedItemsList.remove(productTableModel.getProductAt(row));
                selectedItemsListIsUpdated = true;

                // Update the shipping weight text field here because I can't access the removed product in the model listener
                Object[] oldRow = computeTableModel.removeRow(productTableModel.getProductAt(row));
//                Product oldProduct = (Product) oldRow[0];
//
//                int oldQuantity = (Integer) oldRow[1];
//                double oldQtyWeight = oldQuantity == 0
//                        ? 0
//                        : oldQuantity * oldProduct.getWeight();
//                updateShippingWeightTextField(oldQtyWeight, 0);

                updateTextFields(1, computeTableModel);
            }

            // if the checkbox of the product is selected, adds product to selectedItemsList
            if (productCheckboxIsSelected) {
                selectedItemsList.add(productTableModel.getProductAt(row));
                selectedItemsListIsUpdated = true;

                computeTableModel.addRow(productTableModel.getProductAt(row));
            }

//            if (selectedItemsListIsUpdated) {
//                computeTable.setModel(new ComputeTableModel(selectedItemsList));
//                computeTable.getModel().addTableModelListener(new ComputeTableModelListener());
//            }
        }
    }

    class ComputeTableModelListener implements TableModelListener {
        @Override
        public void tableChanged(@NotNull TableModelEvent e) {
            System.out.println("ComputeTableModelListener table changed");
            int row = e.getFirstRow();
            int col = e.getColumn();
            ComputeTableModel computeTableModel = (ComputeTableModel) e.getSource();

            if (e.getType() == TableModelEvent.UPDATE) {
                updateTextFields(col, computeTableModel);
//                Product productChanged = computeTableModel.getProductAtRow(row);
//                int oldQuantity = computeTableModel.getProductOldQuantity(productChanged.getId());
//                int currentQuantity = (Integer) computeTableModel.getValueAt(row, 1);
//
//                double oldCost = computeTableModel.getProductOldCost(productChanged.getId());
//                double currentCost = (Double) computeTableModel.getValueAt(row, 2);
//
//                double oldWeight = computeTableModel.getProductOldWeight(productChanged.getId());
//                double currentWeight = (Double) computeTableModel.getValueAt(row, 3);
//
//                System.out.println("oldQuantity: " + oldQuantity);
//                System.out.println("currentQuantity: " + currentQuantity);
//                System.out.println("oldCost: " + oldCost);
//                System.out.println("currentCost: " + currentCost);
//                System.out.println("oldWeight: " + oldWeight);
//                System.out.println("currentWeight: " + currentWeight);
//
//                if (col == 1) {
//
//                    double oldQtyWeight = oldQuantity == 0
//                            ? 0
//                            : oldQuantity * currentWeight;
//
//                    double newQtyWeight = currentQuantity == 0
//                            ? 0
//                            : currentQuantity * currentWeight;
//
//                    updateSubtotalTextField((oldQuantity * oldCost), (currentQuantity * currentCost));
//                    updateShippingWeightTextField(oldQtyWeight, newQtyWeight);
//                }
//
//                // if cost changes, recalculate subtotal text field
//                else if (col == 2) {
//                    updateSubtotalTextField((currentQuantity * oldCost), (currentQuantity * currentCost));
//                }
//
//                // if weight changes, recalculate shipping weight text field
//                else if (col == 3) {
//                    updateShippingWeightTextField((currentQuantity * oldWeight), (currentQuantity * currentWeight));
//                }
            }
        }
    }


    // calculates the cost per item for an order; includes shipping cost in calculation
    private void calculateOrderButtonListenerImpl(ActionEvent e) {
        ComputeTableModel computeTableModel = (ComputeTableModel) computeTable.getModel();

        try {
            double calculatedOrderWeightFromTable = 0;
            for (int row = 0; row < computeTableModel.getRowCount(); row++)
                calculatedOrderWeightFromTable += (double) computeTableModel.getValueAt(row, 3) *
                        (int) computeTableModel.getValueAt(row, 1);

            if (!shippingWeightTextField.getText().isBlank()) {
                double userEnteredOrderWeight = Double.parseDouble(shippingWeightTextField.getText().strip());

                if (calculatedOrderWeightFromTable != userEnteredOrderWeight)
                    throw new OrderWeightException("The calculated order weight and entered order weight do not match");
            }

            double totalOrderWeight = calculatedOrderWeightFromTable;

            double orderSubtotal = Double.parseDouble(subtotalTextField.getText().strip());
            double totalOrderShippingCost = Double.parseDouble(shippingCostTextField.getText().strip());
            System.out.println("totalOrderShippingCost: " + totalOrderShippingCost);
            double orderTotalCost = Double.parseDouble(orderTotalCostTextField.getText().strip());
            double orderSubtotalCostCheck = 0;  // to check that the price of each item is correct
            double orderTotalCheck = 0; //  to check that the order total matches what is input

            if (orderSubtotal == Double.POSITIVE_INFINITY || orderSubtotal <= 0)
                throw new TextFieldValueExeption("subtotal");

            // this checks to see if the number in the freight cost field is valid
            if (totalOrderShippingCost == Double.POSITIVE_INFINITY || totalOrderShippingCost <= 0)
                throw new TextFieldValueExeption("freight");

            if (computeTableModel.getRowCount() <= 0)
                throw new IndexOutOfBoundsException();

            // calculate total weight for each item (quantity * weight)
            // (item total weight / order total weight) * total shipping cost
            for (int rowIndex = 0; rowIndex < computeTableModel.getRowCount(); rowIndex++) {
                int quantity = (int) computeTableModel.getValueAt(rowIndex, 1);
                double computeTableProductCost = (double) computeTableModel.getValueAt(rowIndex, 2);
                double computeTableProductWeight = (double) computeTableModel.getValueAt(rowIndex, 3);

                if (computeTableProductWeight == 0) {
                    String message = "Product at row " + (rowIndex + 1) + " is missing a weight";
                    throw new OrderWeightException(message);
                }

                double totalItemWeight = quantity * computeTableProductWeight;
                double totalItemShippingCost = roundDoubleNumber(
                        2,
                        (totalItemWeight / totalOrderWeight * totalOrderShippingCost)
                );

                // sets shipping cost for item in table
                computeTableModel.setValueAt(totalItemShippingCost, rowIndex, 4);

                // sets total item cost + shipping in table
                computeTableModel.setValueAt(totalItemShippingCost + (computeTableProductCost * quantity), rowIndex, 5);

                orderSubtotalCostCheck += (computeTableProductCost * quantity);
                orderTotalCheck += totalItemShippingCost + (computeTableProductCost * quantity);

                // Product at the current row
                Product rowProduct = computeTableModel.getProductAtRow(rowIndex);
                Optional<Product> currentRowProductFromDB = productRepository.findById(rowProduct.getId());
                boolean updateRowProduct = false;

                // check if the cost of the product in the compute table is different than what is in the DB
                // if so, create new ProductCost entry
                try {
                    if (currentRowProductFromDB.isPresent()) {
                        if (computeTableProductCost != currentRowProductFromDB.get().getCost()) {
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
                        if (computeTableProductWeight != currentRowProductFromDB.get().getWeight()) {
                            rowProduct.setWeight(computeTableProductWeight);
                            updateRowProduct = true;
                        }

                        if (updateRowProduct)
                            updatedProductsToSave.add(rowProduct);
                    } else {
                        String message = "This product is not present in the database (Optional.isPresent() returned false).";
                        throw new ProductNotFoundException(message);
                    }
                } catch (ProductNotFoundException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                }
            }

            // rounding check here
            double roundedSubtotalCheck = roundDoubleNumber(2, orderSubtotalCostCheck);
            double roundedTotalCheck = roundDoubleNumber(2, orderTotalCheck);

            if (roundedSubtotalCheck != orderSubtotal) {
                String message = "User input order subtotal and application-calculated item costs do not match up: " +
                        orderSubtotal + " != " + roundedSubtotalCheck;
                JOptionPane.showMessageDialog(null, message);
            } else if (roundedTotalCheck != orderTotalCost) {
                String message = "User input order total and application-calculated order total do not match up: " +
                        orderTotalCost + " != " + roundedTotalCheck;
                JOptionPane.showMessageDialog(null, message);
            }
        } catch (OrderWeightException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        } catch (IndexOutOfBoundsException ex) {
            JOptionPane.showMessageDialog(null, "No products added to compute table");
        } catch (TextFieldValueExeption ex) {
            String message;
            JTextField textFieldToRequestFocus;

            if (ex.getMessage().equals("subtotal")) {
                message = "Enter a valid order subtotal";
                textFieldToRequestFocus = subtotalTextField;
            } else {
                message = "Enter a valid freight cost";
                textFieldToRequestFocus = shippingCostTextField;
            }

            JOptionPane.showMessageDialog(null, message);
            textFieldToRequestFocus.requestFocus();
        }
    }


    private void updateTextFields(int column, @NotNull ComputeTableModel computeTableModel) {
        boolean setWeight = column == 1 || column == 3;
        boolean setCost = column == 1 || column == 2;

        double cost = 0;
        double weight = 0;
        for (int i = 0; i < computeTableModel.getRowCount(); i++) {
            int quantity = (Integer) computeTableModel.getValueAt(i, 1);

            if (setCost)
                cost += quantity * (Double) computeTableModel.getValueAt(i, 2);

            if (setWeight && quantity > 0)
                weight += quantity * (Double) computeTableModel.getValueAt(i, 3);
        }

        if (setCost)
            subtotalTextField.setText(String.valueOf(roundDoubleNumber(2, cost)));

        if (setWeight)
            shippingWeightTextField.setText(String.valueOf(roundDoubleNumber(2, weight)));

    }


    // Populates the order total text field once a subtotal and shipping total have been entered
    private void autoPopulateOrderTotalTextField() {
        double subtotal = subtotalTextField.getText().isBlank()
                ? 0
                : Double.parseDouble(subtotalTextField.getText());
        double shippingTotal = shippingCostTextField.getText().isBlank()
                ? 0
                : Double.parseDouble(shippingCostTextField.getText());

        if (subtotal != 0 && shippingTotal != 0)
            orderTotalCostTextField.setText(
                    String.valueOf(roundDoubleNumber(2, subtotal) + roundDoubleNumber(2, shippingTotal))
            );
        else
            orderTotalCostTextField.setText(String.valueOf(0));
    }


    // rounds a Double to specified number of places using mode HALF_UP
    private double roundDoubleNumber(int places, double value) {
        BigDecimal bd = new BigDecimal(Double.toString(value)).setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }


    private void resetTextFields() {
        subtotalTextField.setText(Integer.toString(0));
        shippingCostTextField.setText(Integer.toString(0));
        shippingWeightTextField.setText(Integer.toString(0));
        orderTotalCostTextField.setText(Integer.toString(0));
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
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 11;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panelMain.add(scrollPaneProductTable, gbc);
        scrollPaneProductTable.setBorder(BorderFactory.createTitledBorder(null, "Product List", TitledBorder.CENTER, TitledBorder.TOP, null, null));
        productTable.setAutoResizeMode(2);
        productTable.setFillsViewportHeight(true);
        productTable.setPreferredScrollableViewportSize(new Dimension(450, 670));
        scrollPaneProductTable.setViewportView(productTable);
        scrollPaneComputeTable = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 13;
        gbc.gridy = 2;
        gbc.gridwidth = 7;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panelMain.add(scrollPaneComputeTable, gbc);
        scrollPaneComputeTable.setBorder(BorderFactory.createTitledBorder(null, "Order", TitledBorder.CENTER, TitledBorder.TOP, null, null));
        computeTable.setAutoResizeMode(2);
        computeTable.setFillsViewportHeight(true);
        computeTable.setPreferredScrollableViewportSize(new Dimension(620, 200));
        scrollPaneComputeTable.setViewportView(computeTable);
        searchTextField = new JTextField();
        searchTextField.setColumns(20);
        searchTextField.setHorizontalAlignment(4);
        searchTextField.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0.1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelMain.add(searchTextField, gbc);
        searchFieldLabel = new JLabel();
        searchFieldLabel.setText("Search Field");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 11;
        gbc.weighty = 0.05;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelMain.add(searchFieldLabel, gbc);
        subtotalTextField = new JTextField();
        subtotalTextField.setColumns(10);
        subtotalTextField.setHorizontalAlignment(4);
        subtotalTextField.setText("0");
        gbc = new GridBagConstraints();
        gbc.gridx = 13;
        gbc.gridy = 1;
        gbc.weightx = 0.25;
        gbc.weighty = 0.1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelMain.add(subtotalTextField, gbc);
        shippingCostLabel = new JLabel();
        shippingCostLabel.setHorizontalAlignment(10);
        shippingCostLabel.setHorizontalTextPosition(11);
        shippingCostLabel.setText("Freight Cost");
        gbc = new GridBagConstraints();
        gbc.gridx = 15;
        gbc.gridy = 0;
        gbc.weighty = 0.05;
        gbc.fill = GridBagConstraints.BOTH;
        panelMain.add(shippingCostLabel, gbc);
        shippingCostTextField = new JTextField();
        shippingCostTextField.setColumns(10);
        shippingCostTextField.setHorizontalAlignment(4);
        shippingCostTextField.setText("0");
        gbc = new GridBagConstraints();
        gbc.gridx = 15;
        gbc.gridy = 1;
        gbc.weightx = 0.25;
        gbc.weighty = 0.1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelMain.add(shippingCostTextField, gbc);
        subtotalLabel = new JLabel();
        subtotalLabel.setText("Subtotal");
        gbc = new GridBagConstraints();
        gbc.gridx = 13;
        gbc.gridy = 0;
        gbc.weighty = 0.05;
        gbc.fill = GridBagConstraints.BOTH;
        panelMain.add(subtotalLabel, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 14;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        panelMain.add(spacer1, gbc);
        final JPanel spacer2 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 12;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.BOTH;
        panelMain.add(spacer2, gbc);
        final JPanel spacer3 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.BOTH;
        panelMain.add(spacer3, gbc);
        shippingWeightTextField = new JTextField();
        shippingWeightTextField.setColumns(10);
        shippingWeightTextField.setHorizontalAlignment(4);
        shippingWeightTextField.setText("0");
        gbc = new GridBagConstraints();
        gbc.gridx = 17;
        gbc.gridy = 1;
        gbc.weightx = 0.25;
        gbc.weighty = 0.1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelMain.add(shippingWeightTextField, gbc);
        shippingWeightLabel = new JLabel();
        shippingWeightLabel.setText("Total Shipping Weight");
        gbc = new GridBagConstraints();
        gbc.gridx = 17;
        gbc.gridy = 0;
        gbc.weighty = 0.05;
        gbc.fill = GridBagConstraints.BOTH;
        panelMain.add(shippingWeightLabel, gbc);
        orderTotalCostLabel = new JLabel();
        orderTotalCostLabel.setText("Order Total Cost");
        gbc = new GridBagConstraints();
        gbc.gridx = 19;
        gbc.gridy = 0;
        gbc.weighty = 0.05;
        gbc.fill = GridBagConstraints.BOTH;
        panelMain.add(orderTotalCostLabel, gbc);
        calculateOrderButton = new JButton();
        calculateOrderButton.setText("Calculate Order");
        gbc = new GridBagConstraints();
        gbc.gridx = 15;
        gbc.gridy = 3;
        gbc.weightx = 0.1;
        gbc.weighty = 0.2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelMain.add(calculateOrderButton, gbc);
        saveUpdatedProductsAfterCalculateOrderButton = new JButton();
        saveUpdatedProductsAfterCalculateOrderButton.setText("Save Products");
        gbc = new GridBagConstraints();
        gbc.gridx = 17;
        gbc.gridy = 3;
        gbc.weightx = 0.1;
        gbc.weighty = 0.2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelMain.add(saveUpdatedProductsAfterCalculateOrderButton, gbc);
        final JPanel spacer4 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 20;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        panelMain.add(spacer4, gbc);
        orderTotalCostTextField = new JTextField();
        orderTotalCostTextField.setColumns(10);
        orderTotalCostTextField.setEditable(true);
        orderTotalCostTextField.setHorizontalAlignment(4);
        orderTotalCostTextField.setText("0");
        gbc = new GridBagConstraints();
        gbc.gridx = 19;
        gbc.gridy = 1;
        gbc.weightx = 0.25;
        gbc.weighty = 0.1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelMain.add(orderTotalCostTextField, gbc);
        final JPanel spacer5 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 16;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelMain.add(spacer5, gbc);
        final JPanel spacer6 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 18;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelMain.add(spacer6, gbc);
        final JPanel spacer7 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 11;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelMain.add(spacer7, gbc);
        final JPanel spacer8 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 10;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelMain.add(spacer8, gbc);
        final JPanel spacer9 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 7;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelMain.add(spacer9, gbc);
        final JPanel spacer10 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelMain.add(spacer10, gbc);
        final JPanel spacer11 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 9;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelMain.add(spacer11, gbc);
        final JPanel spacer12 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 8;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelMain.add(spacer12, gbc);
        final JPanel spacer13 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelMain.add(spacer13, gbc);
        final JPanel spacer14 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelMain.add(spacer14, gbc);
        final JPanel spacer15 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelMain.add(spacer15, gbc);
        final JPanel spacer16 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelMain.add(spacer16, gbc);
        clearProductsButton = new JButton();
        clearProductsButton.setText("Clear Products");
        gbc = new GridBagConstraints();
        gbc.gridx = 19;
        gbc.gridy = 3;
        gbc.weightx = 0.1;
        gbc.weighty = 0.2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelMain.add(clearProductsButton, gbc);
        searchFieldLabel.setLabelFor(searchTextField);
        shippingCostLabel.setLabelFor(shippingCostTextField);
        subtotalLabel.setLabelFor(subtotalTextField);
        shippingWeightLabel.setLabelFor(shippingWeightTextField);
        orderTotalCostLabel.setLabelFor(orderTotalCostTextField);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panelMain;
    }

}