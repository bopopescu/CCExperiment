package org.apache.batik.apps.svgbrowser;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;
import org.apache.batik.util.gui.DropDownComponent.DefaultScrollablePopupMenuItem;
import org.apache.batik.util.gui.DropDownComponent.ScrollablePopupMenu;
import org.apache.batik.util.gui.DropDownComponent.ScrollablePopupMenuEvent;
import org.apache.batik.util.gui.DropDownComponent.ScrollablePopupMenuItem;
import org.apache.batik.util.gui.DropDownComponent.ScrollablePopupMenuModel;
import org.apache.batik.apps.svgbrowser.HistoryBrowser.CommandNamesInfo;
import org.apache.batik.apps.svgbrowser.HistoryBrowser.HistoryBrowserAdapter;
import org.apache.batik.apps.svgbrowser.HistoryBrowser.HistoryBrowserEvent;
import org.apache.batik.util.resources.ResourceManager;
public class DropDownHistoryModel implements ScrollablePopupMenuModel {
    private static final String RESOURCES =
        "org.apache.batik.apps.svgbrowser.resources.DropDownHistoryModelMessages";
    private static ResourceBundle bundle;
    private static ResourceManager resources;
    static {
        bundle = ResourceBundle.getBundle(RESOURCES, Locale.getDefault());
        resources = new ResourceManager(bundle);
    }
    protected ArrayList items = new ArrayList();
    protected HistoryBrowserInterface historyBrowserInterface;
    protected ScrollablePopupMenu parent;
    public DropDownHistoryModel(ScrollablePopupMenu parent,
            HistoryBrowserInterface historyBrowserInterface) {
        this.parent = parent;
        this.historyBrowserInterface = historyBrowserInterface;
        historyBrowserInterface.getHistoryBrowser().addListener
            (new HistoryBrowserAdapter() {
                public void historyReset(HistoryBrowserEvent event) {
                    clearAllScrollablePopupMenuItems("");
                }
             });
    }
    public String getFooterText() {
        return "";
    }
    public ScrollablePopupMenuItem createItem(String itemName) {
        return new DefaultScrollablePopupMenuItem(parent, itemName);
    }
    protected void addItem(ScrollablePopupMenuItem item, String details) {
        int oldSize = items.size();
        items.add(0, item);
        parent.add(item, 0, oldSize, items.size());
        parent.fireItemsWereAdded
            (new ScrollablePopupMenuEvent(parent,
                                          ScrollablePopupMenuEvent.ITEMS_ADDED,
                                          1,
                                          details));
    }
    protected void removeItem(ScrollablePopupMenuItem item, String details) {
        int oldSize = items.size();
        items.remove(item);
        parent.remove(item, oldSize, items.size());
        parent.fireItemsWereRemoved
            (new ScrollablePopupMenuEvent(parent,
                                          ScrollablePopupMenuEvent.ITEMS_REMOVED,
                                          1,
                                          details));
    }
    protected boolean removeLastScrollablePopupMenuItem(String details) {
        for (int i = items.size() - 1; i >= 0; i--) {
            ScrollablePopupMenuItem item =
                (ScrollablePopupMenuItem) items.get(i);
            removeItem(item, details);
            return true;
        }
        return false;
    }
    protected boolean removeFirstScrollablePopupMenuItem(String details) {
        for (int i = 0; i < items.size(); i++) {
            ScrollablePopupMenuItem item =
                (ScrollablePopupMenuItem) items.get(i);
            removeItem(item, details);
            return true;
        }
        return false;
    }
    protected void clearAllScrollablePopupMenuItems(String details) {
        while (removeLastScrollablePopupMenuItem(details)) {
        }
    }
    public void processItemClicked() {
    }
    public void processBeforeShowed() {
        historyBrowserInterface.performCurrentCompoundCommand();
    }
    public void processAfterShowed() {
    }
    public static class UndoPopUpMenuModel extends DropDownHistoryModel {
        protected static String UNDO_FOOTER_TEXT =
            resources.getString("UndoModel.footerText");
        protected static String UNDO_TOOLTIP_PREFIX =
            resources.getString("UndoModel.tooltipPrefix");
        public UndoPopUpMenuModel
                (ScrollablePopupMenu parent,
                 HistoryBrowserInterface historyBrowserInterface) {
            super(parent, historyBrowserInterface);
            init();
        }
        private void init() {
            historyBrowserInterface.getHistoryBrowser().addListener
                (new HistoryBrowserAdapter() {
                     public void executePerformed(HistoryBrowserEvent event) {
                         CommandNamesInfo info =
                             (CommandNamesInfo) event.getSource();
                         String details = UNDO_TOOLTIP_PREFIX
                                 + info.getLastUndoableCommandName();
                         addItem(createItem(info.getCommandName()), details);
                     }
                     public void undoPerformed(HistoryBrowserEvent event) {
                         CommandNamesInfo info =
                             (CommandNamesInfo) event.getSource();
                         String details = UNDO_TOOLTIP_PREFIX
                                 + info.getLastUndoableCommandName();
                         removeFirstScrollablePopupMenuItem(details);
                     }
                     public void redoPerformed(HistoryBrowserEvent event) {
                         CommandNamesInfo info =
                             (CommandNamesInfo) event.getSource();
                         String details = UNDO_TOOLTIP_PREFIX
                                 + info.getLastUndoableCommandName();
                         addItem(createItem(info.getCommandName()), details);
                     }
                     public void doCompoundEdit(HistoryBrowserEvent event) {
                         if (!parent.isEnabled()) {
                             parent.setEnabled(true);
                         }
                     }
                     public void compoundEditPerformed
                             (HistoryBrowserEvent event) {
                     }
                });
        }
        public String getFooterText() {
            return UNDO_FOOTER_TEXT;
        }
        public void processItemClicked() {
            historyBrowserInterface.getHistoryBrowser().compoundUndo
                (parent.getSelectedItemsCount());
        }
    }
    public static class RedoPopUpMenuModel extends DropDownHistoryModel {
        protected static String REDO_FOOTER_TEXT =
            resources.getString("RedoModel.footerText");
        protected static String REDO_TOOLTIP_PREFIX =
            resources.getString("RedoModel.tooltipPrefix");
        public RedoPopUpMenuModel
                    (ScrollablePopupMenu parent,
                     HistoryBrowserInterface historyBrowserInterface) {
            super(parent, historyBrowserInterface);
            init();
        }
        private void init() {
            historyBrowserInterface.getHistoryBrowser().addListener
                (new HistoryBrowserAdapter() {
                     public void executePerformed(HistoryBrowserEvent event) {
                         CommandNamesInfo info =
                            (CommandNamesInfo) event.getSource();
                         String details = REDO_TOOLTIP_PREFIX
                                 + info.getLastRedoableCommandName();
                         clearAllScrollablePopupMenuItems(details);
                     }
                     public void undoPerformed(HistoryBrowserEvent event) {
                         CommandNamesInfo info =
                            (CommandNamesInfo) event.getSource();
                         String details = REDO_TOOLTIP_PREFIX
                                 + info.getLastRedoableCommandName();
                         addItem(createItem(info.getCommandName()), details);
                     }
                     public void redoPerformed(HistoryBrowserEvent event) {
                         CommandNamesInfo info =
                            (CommandNamesInfo) event.getSource();
                         String details = REDO_TOOLTIP_PREFIX
                                 + info.getLastRedoableCommandName();
                         removeFirstScrollablePopupMenuItem(details);
                     }
                     public void doCompoundEdit(HistoryBrowserEvent event) {
                         if (parent.isEnabled()) {
                             parent.setEnabled(false);
                         }
                     }
                     public void compoundEditPerformed
                            (HistoryBrowserEvent event) {
                     }
                 });
        }
        public String getFooterText() {
            return REDO_FOOTER_TEXT;
        }
        public void processItemClicked() {
            historyBrowserInterface.getHistoryBrowser().compoundRedo
                (parent.getSelectedItemsCount());
        }
    }
}
