package org.apache.batik.swing.svg;
import java.awt.Component;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import org.apache.batik.util.gui.JErrorPane;
public class SVGUserAgentGUIAdapter extends SVGUserAgentAdapter{
    public Component parentComponent;
    public SVGUserAgentGUIAdapter(Component parentComponent) {
        this.parentComponent = parentComponent;
    }
    public void displayError(String message) {
        JOptionPane pane = new JOptionPane(message, JOptionPane.ERROR_MESSAGE);
        JDialog dialog = pane.createDialog(parentComponent, "ERROR");
        dialog.setModal(false);
        dialog.setVisible(true);
    }
    public void displayError(Exception ex) {
        JErrorPane pane = new JErrorPane(ex, JOptionPane.ERROR_MESSAGE);
        JDialog dialog = pane.createDialog(parentComponent, "ERROR");
        dialog.setModal(false);
        dialog.setVisible(true);
    }
    public void displayMessage(String message) {
    }
    public void showAlert(String message) {
        String str = "Script alert:\n" + message;
        JOptionPane.showMessageDialog(parentComponent, str);
    }
    public String showPrompt(String message) {
        String str = "Script prompt:\n" + message;
        return JOptionPane.showInputDialog(parentComponent, str);
    }
    public String showPrompt(String message, String defaultValue) {
        String str = "Script prompt:\n" + message;
        return (String)JOptionPane.showInputDialog
            (parentComponent, str, null,
             JOptionPane.PLAIN_MESSAGE,
             null, null, defaultValue);
    }
    public boolean showConfirm(String message) {
        String str = "Script confirm:\n" + message;
        return JOptionPane.showConfirmDialog
            (parentComponent, str, 
             "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}
