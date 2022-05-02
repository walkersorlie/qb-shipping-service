package com.walkersorlie.qbshippingservice.tablemodels;

import com.walkersorlie.qbshippingservice.entities.Product;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ComputeTableModel extends AbstractTableModel {
    private final String[] COLUMN_NAMES = {"Description", "Quantity", "Cost", "Weight", "Order Total"};
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
            case 2: {
                Product product = (Product) products.get(rowIndex)[0];
                return product.getCost();
            }
            case 3: {
                Product product = (Product) products.get(rowIndex)[0];
                return product.getWeight();
            }
            case 4: return products.get(rowIndex)[4];
            default: return null;
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (aValue instanceof Integer && columnIndex == 1) {
            products.get(rowIndex)[columnIndex] = aValue;
            fireTableCellUpdated(rowIndex, columnIndex);
        }
        else if (aValue instanceof Double && (columnIndex == 2 || columnIndex == 3 || columnIndex == 4)) {
            products.get(rowIndex)[columnIndex] = aValue;
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }
}
