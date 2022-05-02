package com.walkersorlie.qbshippingservice;

import com.walkersorlie.qbshippingservice.entities.Product;
import com.walkersorlie.qbshippingservice.repositories.ProductRepository;
import com.walkersorlie.qbshippingservice.tablemodels.ComputeTableModel;
import com.walkersorlie.qbshippingservice.tablemodels.ProductTableModel;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@EnableMongoRepositories
public class ApplicationUI extends JFrame {

    private ProductRepository productRepository;

    private final JFrame frame = new JFrame("QB Shipping Cost");
    private List<Product> productList;
    private ArrayList<Product> selectedItemsList;
    private JPanel panelMain;
    private JTable computeTable;
    private JTable productTable;
    private JButton addToCompute;
    private JScrollPane scrollPaneProductTable;
    private JScrollPane scrollPaneComputeTable;
    private JTextField shippingCostTextField;
    private JTextField shippingWeightTextField;
    private JLabel shippingWeightLabel;
    private JLabel shippingCostLabel;
    private JButton calculateOrder;
    private ListSelectionModel productTableSelectionModel;

    public ApplicationUI(ProductRepository productRepository) {
        this.productRepository = productRepository;

        $$$setupUI$$$();
//        createUIComponents();
        createListeners();

        frame.setContentPane(panelMain);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        productList = productRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));

        productTable = new JTable(new ProductTableModel(new ArrayList<>(productList)));
        productTable.getModel().addTableModelListener(new TableModelListenerCust());

        computeTable = new JTable(new ComputeTableModel(new ArrayList<>()));

        selectedItemsList = new ArrayList<>();

//        productTableSelectionModel = productTable.getSelectionModel();
//        productTableSelectionModel.addListSelectionListener(new ListSelectionHandlerCust());


//        this.jListProductsList.setListData(this.productList.stream()
//                .map(Product::getDescription)
//                .toArray(String[]::new));

    }

    private void createListeners() {
        System.out.println("createListeners");

        addToCompute.addActionListener(e -> {
            System.out.println("buttonListener");
//            ProductTableModel tm = (ProductTableModel) productTable.getModel();
//            for (int i = 0; i < tm.getRowCount(); i++) {
//                boolean selected = (boolean) tm.getValueAt(i, 1);
////                System.out.println("test: " + selected == "true");
//                System.out.println("selected: " + selected);
//                if (selected)
//                    selectedItemsList.add(tm.getProductAt(i));
//            }
//
            System.out.println("selectedItemsList.size(): " + selectedItemsList.size());
            computeTable.setModel(new ComputeTableModel(selectedItemsList));
//            ComputeTableModel ct = (ComputeTableModel) computeTable.getModel();
//            System.out.println(Arrays.toString(ct.getRow(0)));
        });

        calculateOrder.addActionListener(e -> {
            System.out.println("calculateOrder");

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
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.VERTICAL;
        panelMain.add(scrollPaneProductTable, gbc);
        scrollPaneProductTable.setBorder(BorderFactory.createTitledBorder(null, "Product List", TitledBorder.CENTER, TitledBorder.TOP, null, null));
        productTable.setFillsViewportHeight(true);
        productTable.setPreferredScrollableViewportSize(new Dimension(450, 670));
        scrollPaneProductTable.setViewportView(productTable);
        scrollPaneComputeTable = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.VERTICAL;
        panelMain.add(scrollPaneComputeTable, gbc);
        scrollPaneComputeTable.setBorder(BorderFactory.createTitledBorder(null, "Order", TitledBorder.CENTER, TitledBorder.TOP, null, null));
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
        shippingWeightLabel.setLabelFor(shippingWeightTextField);
        shippingCostLabel.setLabelFor(shippingCostTextField);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panelMain;
    }


//    class ListSelectionHandlerCust implements ListSelectionListener {
//
//        @Override
//        public void valueChanged(ListSelectionEvent e) {
//            selectedItemsList = new ArrayList<>();
//
//            System.out.println("Adjusting: " + e.getValueIsAdjusting());
//            ListSelectionModel lsm = (ListSelectionModel) e.getSource();
//
//            for (Integer i : lsm.getSelectedIndices()) {
//                selectedItemsList.add(productTableModel.getProductAt(i));
//                System.out.println(selectedItemsList.get(i).getDescription());
//                computeTableModel.setValueAt(productTableModel.getProductAt(i), i, 0);
//            }
//
////            computeTable.setModel(new ProductTableModel(selectedItemsList));
//
//            System.out.println("computeTable row count: " + computeTableModel.getRowCount());
//        }
//    }
}
