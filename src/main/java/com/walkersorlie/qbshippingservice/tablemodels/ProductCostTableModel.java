package com.walkersorlie.qbshippingservice.tablemodels;

import com.walkersorlie.qbshippingservice.entities.ProductCost;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;

public class ProductCostTableModel extends AbstractTableModel {
    private final String[] COLUMN_NAMES = {"Date", "Cost"};
    private final ArrayList<ProductCost> productCostArrayList;

    public ProductCostTableModel(ArrayList<ProductCost> productCostArrayList) {
        this.productCostArrayList = productCostArrayList;
    }

    @Override
    public int getRowCount() {
        return productCostArrayList.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int columnIndex) { return COLUMN_NAMES[columnIndex]; }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0: return productCostArrayList.get(rowIndex).getDateCreated();
            case 1: return productCostArrayList.get(rowIndex).getCost();
            default: return "";
        }
    }

//    @Override
//    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
//        if (productCostArrayList.size() + 1 == rowIndex)
//            productCostArrayList.add((ProductCost) aValue);
//    }
}
