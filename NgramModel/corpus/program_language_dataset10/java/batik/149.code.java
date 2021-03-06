package org.apache.batik.bridge;
import org.apache.batik.gvt.Marker;
import org.w3c.dom.Element;
public interface MarkerBridge extends Bridge {
    Marker createMarker(BridgeContext ctx,
                        Element markerElement,
                        Element paintedElement);
}
