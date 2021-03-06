package org.apache.batik.ext.swing;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import javax.swing.JPanel;
public class JGridBagPanel extends JPanel implements GridBagConstants{
    public static interface InsetsManager{
        Insets getInsets(int gridx, int gridy);
    }
    private static class ZeroInsetsManager implements InsetsManager{
        private Insets insets = new Insets(0, 0, 0, 0);
        public Insets getInsets(int gridx, int gridy){
            return insets;
        }
    }
    private static class DefaultInsetsManager implements InsetsManager{
        int leftInset=5;
        int topInset=5;
        public Insets positiveInsets = new Insets(topInset, leftInset, 0, 0);
        public Insets leftInsets = new Insets(topInset, 0, 0, 0);
        public Insets topInsets = new Insets(0, leftInset, 0, 0);
        public Insets topLeftInsets = new Insets(0, 0, 0, 0);
        public Insets getInsets(int gridx, int gridy){
            if(gridx > 0){
                if(gridy > 0)
                    return positiveInsets;
                else
                    return topInsets;
            }
            else{
                if(gridy > 0)
                    return leftInsets;
                else
                    return topLeftInsets;
            }
        }
    }
    public static final InsetsManager ZERO_INSETS = new ZeroInsetsManager();
    public static final InsetsManager DEFAULT_INSETS = new DefaultInsetsManager();
    public InsetsManager insetsManager;
    public JGridBagPanel(){
        this(new DefaultInsetsManager());
    }
    public JGridBagPanel(InsetsManager insetsManager){
        super(new GridBagLayout());
        if(insetsManager != null)
            this.insetsManager = insetsManager;
        else
            this.insetsManager = new DefaultInsetsManager();
    }
    public void setLayout(LayoutManager layout){
        if(layout instanceof GridBagLayout)
            super.setLayout(layout);
    }
    public void add(Component cmp, int gridx, int gridy,
                    int gridwidth, int gridheight, int anchor, int fill,
                    double weightx, double weighty){
        Insets insets = insetsManager.getInsets(gridx, gridy);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = gridx;
        constraints.gridy = gridy;
        constraints.gridwidth = gridwidth;
        constraints.gridheight = gridheight;
        constraints.anchor = anchor;
        constraints.fill = fill;
        constraints.weightx = weightx;
        constraints.weighty = weighty;
        constraints.insets = insets;
        add(cmp, constraints);
    }
}
