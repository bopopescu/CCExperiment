package org.apache.lucene.swing.models;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.table.AbstractTableModel;
public class BaseTableModel extends AbstractTableModel {
    private List<String> columnNames = new ArrayList<String>();
    private List<Object> rows = new ArrayList<Object>();
    public BaseTableModel(Iterator<?> data) {
        columnNames.add("Name");
        columnNames.add("Type");
        columnNames.add("Phone");
        columnNames.add("Street");
        columnNames.add("City");
        columnNames.add("State");
        columnNames.add("Zip");
        while (data.hasNext()) {
            Object nextRow = data.next();
            rows.add(nextRow);
        }
    }
    public int getColumnCount() {
        return columnNames.size();
    }
    public int getRowCount() {
        return rows.size();
    }
    public void addRow(RestaurantInfo info){
        rows.add(info);
        fireTableDataChanged();
    }
    public void removeRow(RestaurantInfo info){
        rows.remove(info);
        fireTableDataChanged();
    }
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }
    public Object getValueAt(int rowIndex, int columnIndex) {
        RestaurantInfo restaurantInfo = (RestaurantInfo) rows.get(rowIndex);
        if (columnIndex == 0){ 
            return restaurantInfo.getName();
        } else if (columnIndex == 1){ 
            return restaurantInfo.getType();
        } else if (columnIndex == 2){ 
            return restaurantInfo.getPhone();
        } else if (columnIndex == 3){ 
            return restaurantInfo.getStreet();
        } else if (columnIndex == 4){ 
            return restaurantInfo.getCity();
        } else if (columnIndex == 5){ 
            return restaurantInfo.getState();
        } else if (columnIndex == 6){ 
            return restaurantInfo.getZip();
        } else {
            return "";
        }
    }
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    }
    @Override
    public String getColumnName(int columnIndex) {
        return columnNames.get(columnIndex).toString();
    }
}
