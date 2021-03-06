package org.apache.batik.apps.svgbrowser;
import java.awt.Component;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
public class SVGOptionPanel extends OptionPanel {
    protected JCheckBox xmlbaseCB;
    protected JCheckBox prettyPrintCB;
    public SVGOptionPanel() {
        super(new BorderLayout());
        add(new JLabel(resources.getString("SVGOptionPanel.label")), 
            BorderLayout.NORTH);
        xmlbaseCB = new JCheckBox
            (resources.getString("SVGOptionPanel.UseXMLBase"));
        xmlbaseCB.setSelected
            (resources.getBoolean("SVGOptionPanel.UseXMLBaseDefault"));
        add(xmlbaseCB, BorderLayout.CENTER);
        prettyPrintCB = new JCheckBox
            (resources.getString("SVGOptionPanel.PrettyPrint"));
        prettyPrintCB.setSelected
            (resources.getBoolean("SVGOptionPanel.PrettyPrintDefault"));
        add(prettyPrintCB, BorderLayout.SOUTH);
    }
    public boolean getUseXMLBase() {
        return xmlbaseCB.isSelected();
    }
    public boolean getPrettyPrint() {
        return prettyPrintCB.isSelected();
    }
    public static SVGOptionPanel showDialog(Component parent) {
        String title = resources.getString("SVGOptionPanel.dialog.title");
        SVGOptionPanel panel = new SVGOptionPanel();
        Dialog dialog = new Dialog(parent, title, panel);
        dialog.pack();
        dialog.setVisible(true);
        return panel;
    }
}
