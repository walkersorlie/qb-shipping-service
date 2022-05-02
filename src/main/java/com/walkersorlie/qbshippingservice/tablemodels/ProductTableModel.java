package com.walkersorlie.qbshippingservice.tablemodels;

import com.walkersorlie.qbshippingservice.entities.Product;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ProductTableModel extends AbstractTableModel {
    private final String[] COLUMN_NAMES = {"Description", "Include"};
    private ArrayList<Object[]> products;

    /**
     *
     * @param products
     *
     */
    public ProductTableModel(ArrayList<Product> products) {
        this.products = products.stream()
                .map(product -> new Object[]{product, Boolean.FALSE})
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public int getRowCount() {
        return products.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int columnIndex) { return COLUMN_NAMES[columnIndex]; }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return products.get(0)[columnIndex].getClass();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 1;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            Product selected = (Product) products.get(rowIndex)[0];
            return selected.getDescription();
        }
        else
            return products.get(rowIndex)[columnIndex];
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (aValue instanceof Boolean && columnIndex == 1) {
            products.get(rowIndex)[columnIndex] = aValue;
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }

    public Product getProductAt(int rowIndex) {
        return (Product) products.get(rowIndex)[0];
    }
}
