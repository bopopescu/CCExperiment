package org.apache.lucene.swing.models;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ListModel;
import junit.framework.TestCase;
public class TestBasicList extends TestCase {
    private ListModel baseListModel;
    private ListSearcher listSearcher;
    private List<RestaurantInfo> list;
    @Override
    protected void setUp() throws Exception {
        list = new ArrayList<RestaurantInfo>();
        list.add(DataStore.canolis);
        list.add(DataStore.chris);
        baseListModel = new BaseListModel(list.iterator());
        listSearcher = new ListSearcher(baseListModel);
    }
    public void testRows(){
        assertEquals(list.size(), listSearcher.getSize());
    }
    public void testValueAt(){
        assertEquals(baseListModel.getElementAt(0), listSearcher.getElementAt(0));
        assertNotSame(baseListModel.getElementAt(1), listSearcher.getElementAt(0));
    }
}
