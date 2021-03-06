package org.apache.batik.swing.gvt;
public interface GVTTreeRendererListener {
    void gvtRenderingPrepare(GVTTreeRendererEvent e);
    void gvtRenderingStarted(GVTTreeRendererEvent e);
    void gvtRenderingCompleted(GVTTreeRendererEvent e);
    void gvtRenderingCancelled(GVTTreeRendererEvent e);
    void gvtRenderingFailed(GVTTreeRendererEvent e);
}
