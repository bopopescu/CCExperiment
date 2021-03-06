package org.apache.batik.util.gui;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import org.apache.batik.util.gui.resource.ActionMap;
import org.apache.batik.util.gui.resource.ButtonFactory;
import org.apache.batik.util.gui.resource.MissingListenerException;
import org.apache.batik.util.resources.ResourceManager;
public class URIChooser extends JDialog implements ActionMap {
    public static final int OK_OPTION = 0;
    public static final int CANCEL_OPTION = 1;
    protected static final String RESOURCES =
        "org.apache.batik.util.gui.resources.URIChooserMessages";
    protected static ResourceBundle bundle;
    protected static ResourceManager resources;
    static {
        bundle = ResourceBundle.getBundle(RESOURCES, Locale.getDefault());
        resources = new ResourceManager(bundle);
    }
    protected ButtonFactory buttonFactory;
    protected JTextField textField;
    protected JButton okButton;
    protected JButton clearButton;
    protected String currentPath = ".";
    protected FileFilter fileFilter;
    protected int returnCode;
    protected String chosenPath;
    public URIChooser(JDialog d) {
        super(d);
        initialize();
    }
    public URIChooser(JFrame f) {
        super(f);
        initialize();
    }
    public int showDialog() {
        pack();
        setVisible(true);
        return returnCode;
    }
    public String getText() {
        return chosenPath;
    }
    public void setFileFilter(FileFilter ff) {
        fileFilter = ff;
    }
    protected void initialize() {
        setModal(true);
        listeners.put("BrowseButtonAction", new BrowseButtonAction());
        listeners.put("OKButtonAction",     new OKButtonAction());
        listeners.put("CancelButtonAction", new CancelButtonAction());
        listeners.put("ClearButtonAction",  new ClearButtonAction());
        setTitle(resources.getString("Dialog.title"));
        buttonFactory = new ButtonFactory(bundle, this);
        getContentPane().add( createURISelectionPanel(), BorderLayout.NORTH );
        getContentPane().add( createButtonsPanel(),      BorderLayout.SOUTH );
    }
    protected JPanel createURISelectionPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        ExtendedGridBagConstraints constraints;
        constraints = new ExtendedGridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.weightx = 0;
        constraints.weighty = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.setGridBounds(0, 0, 2, 1);
        p.add(new JLabel(resources.getString("Dialog.label")), constraints);
        textField = new JTextField(30);
        textField.getDocument().addDocumentListener(new DocumentAdapter());
        constraints.weightx = 1.0;
        constraints.weighty = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.setGridBounds(0, 1, 1, 1);
        p.add(textField, constraints);
        constraints.weightx = 0;
        constraints.weighty = 0;
        constraints.fill = GridBagConstraints.NONE;
        constraints.setGridBounds(1, 1, 1, 1);
        p.add(buttonFactory.createJButton("BrowseButton"), constraints);
        return p;
    }
    protected JPanel createButtonsPanel() {
        JPanel  p = new JPanel(new FlowLayout());
        p.add(okButton = buttonFactory.createJButton("OKButton"));
        p.add(buttonFactory.createJButton("CancelButton"));
        p.add(clearButton = buttonFactory.createJButton("ClearButton"));
        okButton.setEnabled(false);
        clearButton.setEnabled(false);
        return p;
    }
    protected void updateOKButtonAction() {
        okButton.setEnabled(!textField.getText().equals(""));
    }
    protected void updateClearButtonAction() {
        clearButton.setEnabled(!textField.getText().equals(""));
    }
    protected class DocumentAdapter implements DocumentListener {
        public void changedUpdate(DocumentEvent e) {
            updateOKButtonAction();
            updateClearButtonAction();
        }
        public void insertUpdate(DocumentEvent e) {
            updateOKButtonAction();
            updateClearButtonAction();
        }
        public void removeUpdate(DocumentEvent e) {
            updateOKButtonAction();
            updateClearButtonAction();
        }
    }
    protected class BrowseButtonAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser(currentPath);
            fileChooser.setFileHidingEnabled(false);
            fileChooser.setFileSelectionMode
                (JFileChooser.FILES_AND_DIRECTORIES);
            if (fileFilter != null) {
                fileChooser.setFileFilter(fileFilter);
            }
            int choice = fileChooser.showOpenDialog(URIChooser.this);
            if (choice == JFileChooser.APPROVE_OPTION) {
                File f = fileChooser.getSelectedFile();
                try {
                    textField.setText(currentPath = f.getCanonicalPath());
                } catch (IOException ex) {
                }
            }
        }
    }
    protected class OKButtonAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            returnCode = OK_OPTION;
            chosenPath = textField.getText();
            dispose();
        }
    }
    protected class CancelButtonAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            returnCode = CANCEL_OPTION;
            dispose();
            textField.setText(chosenPath);
        }
    }
    protected class ClearButtonAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            textField.setText("");
        }
    }
    protected Map listeners = new HashMap(10);
    public Action getAction(String key) throws MissingListenerException {
        return (Action)listeners.get(key);
    }
}
