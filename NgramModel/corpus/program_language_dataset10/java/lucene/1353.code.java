package org.apache.lucene.swing.models;
import junit.framework.TestCase;
public class TestUpdatingList extends TestCase {
    private BaseListModel baseListModel;
    private ListSearcher listSearcher;
    RestaurantInfo infoToAdd1, infoToAdd2;
    @Override
    protected void setUp() throws Exception {
        baseListModel = new BaseListModel(DataStore.getRestaurants());
        listSearcher = new ListSearcher(baseListModel);
        infoToAdd1 = new RestaurantInfo();
        infoToAdd1.setName("Pino's");
        infoToAdd2 = new RestaurantInfo();
        infoToAdd2.setName("Pino's");
        infoToAdd2.setType("Italian");
    }
    public void testAddWithoutSearch(){
        assertEquals(baseListModel.getSize(), listSearcher.getSize());
        int count = listSearcher.getSize();
        baseListModel.addRow(infoToAdd1);
        count++;
        assertEquals(count, listSearcher.getSize());
    }
    public void testRemoveWithoutSearch(){
        assertEquals(baseListModel.getSize(), listSearcher.getSize());
        baseListModel.addRow(infoToAdd1);
        int count = listSearcher.getSize();
        baseListModel.removeRow(infoToAdd1);
        count--;
        assertEquals(count, listSearcher.getSize());
    }
    public void testAddWithSearch(){
        assertEquals(baseListModel.getSize(), listSearcher.getSize());
        listSearcher.search("pino's");
        int count = listSearcher.getSize();
        baseListModel.addRow(infoToAdd2);
        count++;
        assertEquals(count, listSearcher.getSize());
    }
    public void testRemoveWithSearch(){
        assertEquals(baseListModel.getSize(), listSearcher.getSize());
        baseListModel.addRow(infoToAdd1);
        listSearcher.search("pino's");
        int count = listSearcher.getSize();
        baseListModel.removeRow(infoToAdd1);
        count--;
        assertEquals(count, listSearcher.getSize());
    }
}
