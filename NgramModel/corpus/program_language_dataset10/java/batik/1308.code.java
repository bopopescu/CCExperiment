package org.apache.batik.swing.svg;
public interface GVTTreeBuilderListener {
    void gvtBuildStarted(GVTTreeBuilderEvent e);
    void gvtBuildCompleted(GVTTreeBuilderEvent e);
    void gvtBuildCancelled(GVTTreeBuilderEvent e);
    void gvtBuildFailed(GVTTreeBuilderEvent e);
}
