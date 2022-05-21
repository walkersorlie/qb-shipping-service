package com.walkersorlie.qbshippingservice.tablemodels;

import com.walkersorlie.qbshippingservice.entities.Product;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ComputeTableModel extends AbstractTableModel {
    private final String[] COLUMN_NAMES = {"Description", "Quantity", "Cost", "Weight", "Order Total"};

    /**
     * ArrayList of Arrays
     * Each array is: [Product, quantity, cost, weight, order total]
     * ex. [[Product, quantity, cost, weight, order total], [Product, quantity, cost, weight, order total]]
     */
    private ArrayList<Object[]> products;

    public ComputeTableModel(ArrayList<Product> products) {
        this.products = products.stream()
                .map(product -> new Object[]{product, 0, product.getCost(), product.getWeight(), 0})
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public int getRowCount() {
        return products.size();
    }

    public Object[] getRow(int rowIndex) {
        return products.get(rowIndex);
    }

    public void setRow(int rowIndex, Object[] rowData) {
        products.set(rowIndex, rowData);
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
        return columnIndex == 1 || columnIndex == 2 || columnIndex == 3;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch(columnIndex) {
            case 0: {
                Product product = (Product) products.get(rowIndex)[0];
                return product.getDescription();
            }
            case 1: return products.get(rowIndex)[1];
            case 2: return products.get(rowIndex)[2];
            case 3: return products.get(rowIndex)[3];
            case 4: return products.get(rowIndex)[4];
            default: return null;
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if ((aValue instanceof Integer || aValue instanceof Double) && isCellEditable(rowIndex, columnIndex)) {
            products.get(rowIndex)[columnIndex] = aValue;
            fireTableCellUpdated(rowIndex, columnIndex);
        }
        else if (aValue instanceof Double && columnIndex == 4) {
            products.get(rowIndex)[columnIndex] = aValue;
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }
}
