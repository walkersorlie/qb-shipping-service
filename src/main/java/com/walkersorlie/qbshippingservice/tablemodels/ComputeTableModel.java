package com.walkersorlie.qbshippingservice.tablemodels;

import com.walkersorlie.qbshippingservice.entities.Product;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class ComputeTableModel extends AbstractTableModel {
    private final String[] COLUMN_NAMES = {"Description", "Quantity", "Price", "Weight", "Ext Price", "Item Total"};

    /**
     * ArrayList of Arrays
     * Each array is: [Product, quantity, cost, weight, item shipping portion, item total]
     * ex. [[Product, quantity, cost, weight, item shipping portion, item total], [Product, quantity, cost, weight, item shipping portion, item total]]
     */
    private ArrayList<Object[]> products;

    public ComputeTableModel(ArrayList<Product> products) {
        this.products = products.stream()
                .map(product -> new Object[]{product, 0, product.getCost(), product.getWeight(), 0, 0})
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public int getRowCount() {
        return products.size();
    }

    public void addRow (Product product) {
        this.products.add(new Object[]{product, 0, product.getCost(), product.getWeight(), 0, 0});
        fireTableRowsInserted(this.products.size() - 1, this.products.size() - 1);
    }

    public Object[] getRow(int rowIndex) {
        return this.products.get(rowIndex);
    }

    public void setRow(int rowIndex, Object[] rowData) {
        this.products.set(rowIndex, rowData);
    }

    public Object[] removeRow(Product productToRemove) {
        for (int i = 0; i < this.products.size(); i++) {
            Product product = (Product) this.products.get(i)[0];
            if (productToRemove.equals(product)) {
                Object[] oldRow = getRow(i);
                this.products.remove(i);
//                new TableModelEvent(this, i, i, ALL_COLUMNS, DELETE);
                fireTableRowsDeleted(i, i);
                return oldRow;
            }
        }
        return null;
    }

    public void removeAllRows() {
        int totalRows = this.products.size();
        this.products.clear();
        fireTableRowsDeleted(0, totalRows - 1);
    }

    public Product getProductAtRow(int rowIndex) { return (Product) this.products.get(rowIndex)[0]; }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int columnIndex) { return COLUMN_NAMES[columnIndex]; }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return this.products.get(0)[columnIndex].getClass();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 1 || columnIndex == 2 || columnIndex == 3 || columnIndex == 4 || columnIndex == 5;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch(columnIndex) {
            case 0: {
                Product product = (Product) this.products.get(rowIndex)[0];
                return product.getDescription();
            }
            case 1: return this.products.get(rowIndex)[1];
            case 2: return this.products.get(rowIndex)[2];
            case 3: return this.products.get(rowIndex)[3];
            case 4: return this.products.get(rowIndex)[4];
            case 5: return this.products.get(rowIndex)[5];
            default: return null;
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        System.out.println("setValueAt value: " + aValue);
        System.out.println("columnIndex: " + columnIndex);
        System.out.println(aValue.getClass());
        if ((aValue instanceof Integer || aValue instanceof Double) && isCellEditable(rowIndex, columnIndex)) {
            this.products.get(rowIndex)[columnIndex] = aValue;
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }
}
