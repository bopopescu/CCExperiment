package org.apache.batik.parser;
import java.io.IOException;
public class ClockParser extends TimingParser {
    protected ClockHandler clockHandler;
    protected boolean parseOffset;
    public ClockParser(boolean parseOffset) {
        super(false, false);
        this.parseOffset = parseOffset;
    }
    public void setClockHandler(ClockHandler handler) {
        clockHandler = handler;
    }
    public ClockHandler getClockHandler() {
        return clockHandler;
    }
    protected void doParse() throws ParseException, IOException {
        current = reader.read();
        float clockValue = parseOffset ? parseOffset() : parseClockValue();
        if (current != -1) {
            reportError("end.of.stream.expected",
                        new Object[] { new Integer(current) });
        }
        if (clockHandler != null) {
            clockHandler.clockValue(clockValue);
        }
    }
}
