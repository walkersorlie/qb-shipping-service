package com.walkersorlie.qbshippingservice.dialogs;

import com.walkersorlie.qbshippingservice.entities.Product;
import com.walkersorlie.qbshippingservice.entities.ProductCost;
import com.walkersorlie.qbshippingservice.repositories.ProductRepository;
import com.walkersorlie.qbshippingservice.tablemodels.ProductCostTableModel;
import com.walkersorlie.qbshippingservice.tablemodels.ProductTableModel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EditItemDialog extends JDialog {

    private final ProductRepository productRepository;
    private Product product;
    private JPanel panel1;
    private JTextField descriptionTextField;
    private JTextField costTextField;
    private JTextField weightTextField;
    private JButton saveButton;
    private JButton cancelButton;
    private JLabel descriptionLabel;
    private JLabel costLabel;
    private JLabel weightLabel;
    private JTable productCostTable;
    private JButton addProductCostButton;
    private JButton saveAndCloseButton;
    private AddProductCostDialog addProductCostDialog;

    public EditItemDialog(ProductRepository productRepository, Product product) {
        this.productRepository = productRepository;
        this.product = product;

        $$$setupUI$$$();
        createListeners();

        descriptionTextField.setText(product.getDescription());
        costTextField.setText(product.getCost().toString());
        weightTextField.setText(product.getWeight().toString());

        this.setModal(true);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.getContentPane().add(panel1);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);

    }

    /**
     * IntelliJ requires this method if in 'GUI Designer' in 'Settings', the 'Generate GUI into: Java Source Code'
     * is selected
     */
    private void createUIComponents() {
        ArrayList<ProductCost> productCostArrayList = product.getProductCosts() != null ? product.getProductCosts() : new ArrayList<>();
        productCostTable = new JTable(new ProductCostTableModel(productCostArrayList)) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component component = super.prepareRenderer(renderer, row, column);
                int rendererWidth = component.getPreferredSize().width;
                TableColumn tableColumn = getColumnModel().getColumn(column);
                tableColumn.setPreferredWidth(Math.max(rendererWidth + getIntercellSpacing().width, tableColumn.getPreferredWidth()));
                return component;
            }
        };

        // creates a table row sorter to sort the ProductCost entities by ascending date
        TableRowSorter<ProductCostTableModel> sorter = new TableRowSorter<>((ProductCostTableModel) productCostTable.getModel());
        List<RowSorter.SortKey> sortKeys = new ArrayList<>(1);
        sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
        sorter.setSortKeys(sortKeys);
        productCostTable.setRowSorter(sorter);
    }

    /**
     * Creates listeners for components
     */
    private void createListeners() {
        // closes the dialog without saving any potential changes to data
        cancelButton.addActionListener(e -> {
            EditItemDialog.this.setVisible(false);
            EditItemDialog.this.dispatchEvent(new WindowEvent(EditItemDialog.this, WindowEvent.WINDOW_CLOSING));
        });

        // saves the changed data but does not close the window
        saveButton.addActionListener(e -> {
            try {
                this.product.setCost(Double.parseDouble(costTextField.getText().strip()));
                this.product.setWeight(Double.parseDouble(weightTextField.getText().strip()));

                ProductCost newCost = new ProductCost(LocalDate.now().toString(), this.product.getCost(), this.product.getId());
                this.product.addProductCost(newCost);

                // update the product record in the DB with the new cost and weight
                this.product = productRepository.save(this.product);

                // update productCost table with new record
                productCostTable.setModel(new ProductCostTableModel(this.product.getProductCosts()));
            } catch (NumberFormatException ex) {
                String message;
                JTextField textFieldToRequestFocus;

                if (costTextField.getText().isBlank()) {
                    message = "Enter a valid cost";
                    textFieldToRequestFocus = costTextField;
                } else {
                    message = "Enter a valid weight";
                    textFieldToRequestFocus = weightTextField;
                }

                JOptionPane.showMessageDialog(null, message);
                textFieldToRequestFocus.requestFocus();
            }
        });

        // saves the changed data and then closes the dialog
        saveAndCloseButton.addActionListener(e -> {
            try {
                this.product.setCost(Double.parseDouble(costTextField.getText().strip()));
                this.product.setWeight(Double.parseDouble(weightTextField.getText().strip()));

                ProductCost newCost = new ProductCost(LocalDate.now().toString(), this.product.getCost(), this.product.getId());
                this.product.addProductCost(newCost);

                // update the product record in the DB with the new cost and weight
                this.product = productRepository.save(this.product);

                // closes the dialog
                EditItemDialog.this.setVisible(false);
                EditItemDialog.this.dispatchEvent(new WindowEvent(EditItemDialog.this, WindowEvent.WINDOW_CLOSING));
            } catch (NumberFormatException ex) {
                String message;
                JTextField textFieldToRequestFocus;

                if (costTextField.getText().isBlank()) {
                    message = "Enter a valid cost";
                    textFieldToRequestFocus = costTextField;
                } else {
                    message = "Enter a valid weight";
                    textFieldToRequestFocus = weightTextField;
                }

                JOptionPane.showMessageDialog(null, message);
                textFieldToRequestFocus.requestFocus();
            }
        });

        // opens a new dialog to add a product cost record to the Product
        addProductCostButton.addActionListener(e -> {
            double oldProductCost = this.product.getCost();
            addProductCostDialog = new AddProductCostDialog(productRepository, this.product);

            // gets updated product for the case that a product cost entry was added
            this.product = addProductCostDialog.getUpdatedProduct();

            // if a ProductCost entity was added to this Product and the cost is the current cost of the Product
            // set it in the text field
            if (oldProductCost != this.product.getCost())
                costTextField.setText(this.product.getCost().toString());

            // check that the product has product cost entries
            // default product.ProductCostEntries is a blank ArrayList
            if (this.product.getProductCosts() != null)
                productCostTable.setModel(new ProductCostTableModel(this.product.getProductCosts()));
        });
    }

    public Product getUpdatedProduct() {
        return this.product;
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
        panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        descriptionTextField = new JTextField();
        descriptionTextField.setEditable(false);
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(descriptionTextField, gbc);
        weightTextField = new JTextField();
        weightTextField.setEditable(true);
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 5;
        gbc.weightx = 0.5;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(weightTextField, gbc);
        saveButton = new JButton();
        saveButton.setText("Save");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 9;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(saveButton, gbc);
        cancelButton = new JButton();
        cancelButton.setText("Cancel");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 9;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(cancelButton, gbc);
        costTextField = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.gridheight = 2;
        gbc.weightx = 0.5;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(costTextField, gbc);
        costLabel = new JLabel();
        costLabel.setText("Cost");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridheight = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(costLabel, gbc);
        descriptionLabel = new JLabel();
        descriptionLabel.setText("Description");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(descriptionLabel, gbc);
        weightLabel = new JLabel();
        weightLabel.setText("Weight");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.gridheight = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(weightLabel, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel1.add(spacer1, gbc);
        final JPanel spacer2 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel1.add(spacer2, gbc);
        final JPanel spacer3 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel1.add(spacer3, gbc);
        final JPanel spacer4 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 10;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel1.add(spacer4, gbc);
        final JPanel spacer5 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(spacer5, gbc);
        final JPanel spacer6 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(spacer6, gbc);
        final JPanel spacer7 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(spacer7, gbc);
        final JPanel spacer8 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 9;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(spacer8, gbc);
        final JPanel spacer9 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 7;
        gbc.gridy = 9;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(spacer9, gbc);
        final JScrollPane scrollPane1 = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.gridwidth = 6;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(scrollPane1, gbc);
        scrollPane1.setBorder(BorderFactory.createTitledBorder(null, "Product Cost History", TitledBorder.CENTER, TitledBorder.TOP, null, null));
        scrollPane1.setViewportView(productCostTable);
        final JPanel spacer10 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 8;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel1.add(spacer10, gbc);
        addProductCostButton = new JButton();
        addProductCostButton.setText("Add Product Cost");
        gbc = new GridBagConstraints();
        gbc.gridx = 7;
        gbc.gridy = 7;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(addProductCostButton, gbc);
        saveAndCloseButton = new JButton();
        saveAndCloseButton.setText("Save and close");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 9;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(saveAndCloseButton, gbc);
        final JPanel spacer11 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 9;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(spacer11, gbc);
        costLabel.setLabelFor(costTextField);
        descriptionLabel.setLabelFor(descriptionTextField);
        weightLabel.setLabelFor(weightTextField);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }

}
