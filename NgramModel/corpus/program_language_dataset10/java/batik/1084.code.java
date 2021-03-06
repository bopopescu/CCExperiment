package org.apache.batik.parser;
public interface FragmentIdentifierHandler
    extends PreserveAspectRatioHandler,
            TransformListHandler {
    void startFragmentIdentifier() throws ParseException;
    void idReference(String s) throws ParseException;
    void viewBox(float x, float y, float width, float height)
        throws ParseException;
    void startViewTarget() throws ParseException;
    void viewTarget(String name) throws ParseException;
    void endViewTarget() throws ParseException;
    void zoomAndPan(boolean magnify);
    void endFragmentIdentifier() throws ParseException;
}
