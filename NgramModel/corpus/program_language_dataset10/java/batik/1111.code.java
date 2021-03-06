package org.apache.batik.parser;
import java.io.IOException;
public class TransformListParser extends NumberParser {
    protected TransformListHandler transformListHandler;
    public TransformListParser() {
        transformListHandler = DefaultTransformListHandler.INSTANCE;
    }
    public void setTransformListHandler(TransformListHandler handler) {
        transformListHandler = handler;
    }
    public TransformListHandler getTransformListHandler() {
        return transformListHandler;
    }
    protected void doParse() throws ParseException, IOException {
        transformListHandler.startTransformList();
        loop: for (;;) {
            try {
                current = reader.read();
                switch (current) {
                case 0xD:
                case 0xA:
                case 0x20:
                case 0x9:
                case ',':
                    break;
                case 'm':
                    parseMatrix();
                    break;
                case 'r':
                    parseRotate();
                    break;
                case 't':
                    parseTranslate();
                    break;
                case 's':
                    current = reader.read();
                    switch (current) {
                    case 'c':
                        parseScale();
                        break;
                    case 'k':
                        parseSkew();
                        break;
                    default:
                        reportUnexpectedCharacterError( current );
                        skipTransform();
                    }
                    break;
                case -1:
                    break loop;
                default:
                    reportUnexpectedCharacterError( current );
                    skipTransform();
                }
            } catch (ParseException e) {
                errorHandler.error(e);
                skipTransform();
            }
        }
        skipSpaces();
        if (current != -1) {
            reportError("end.of.stream.expected",
                        new Object[] { new Integer(current) });
        }
        transformListHandler.endTransformList();
    }
    protected void parseMatrix() throws ParseException, IOException {
        current = reader.read();
        if (current != 'a') {
            reportCharacterExpectedError('a', current );
            skipTransform();
            return;
        }
        current = reader.read();
        if (current != 't') {
            reportCharacterExpectedError('t', current );
            skipTransform();
            return;
        }
        current = reader.read();
        if (current != 'r') {
            reportCharacterExpectedError('r', current );
            skipTransform();
            return;
        }
        current = reader.read();
        if (current != 'i') {
            reportCharacterExpectedError('i', current );
            skipTransform();
            return;
        }
        current = reader.read();
        if (current != 'x') {
            reportCharacterExpectedError('x', current );
            skipTransform();
            return;
        }
        current = reader.read();
        skipSpaces();
        if (current != '(') {
            reportCharacterExpectedError('(', current );
            skipTransform();
            return;
        }
        current = reader.read();
        skipSpaces();
        float a = parseFloat();
        skipCommaSpaces();
        float b = parseFloat();
        skipCommaSpaces();
        float c = parseFloat();
        skipCommaSpaces();
        float d = parseFloat();
        skipCommaSpaces();
        float e = parseFloat();
        skipCommaSpaces();
        float f = parseFloat();
        skipSpaces();
        if (current != ')') {
            reportCharacterExpectedError(')', current );
            skipTransform();
            return;
        }
        transformListHandler.matrix(a, b, c, d, e, f);
    }
    protected void parseRotate() throws ParseException, IOException {
        current = reader.read();
        if (current != 'o') {
            reportCharacterExpectedError('o', current );
            skipTransform();
            return;
        }
        current = reader.read();
        if (current != 't') {
            reportCharacterExpectedError('t', current );
            skipTransform();
            return;
        }
        current = reader.read();
        if (current != 'a') {
            reportCharacterExpectedError('a', current );
            skipTransform();
            return;
        }
        current = reader.read();
        if (current != 't') {
            reportCharacterExpectedError('t', current );
            skipTransform();
            return;
        }
        current = reader.read();
        if (current != 'e') {
            reportCharacterExpectedError('e', current );
            skipTransform();
            return;
        }
        current = reader.read();
        skipSpaces();
        if (current != '(') {
            reportCharacterExpectedError('(', current );
            skipTransform();
            return;
        }
        current = reader.read();
        skipSpaces();
        float theta = parseFloat();
        skipSpaces();
        switch (current) {
        case ')':
            transformListHandler.rotate(theta);
            return;
        case ',':
            current = reader.read();
            skipSpaces();
        }
        float cx = parseFloat();
        skipCommaSpaces();
        float cy = parseFloat();
        skipSpaces();
        if (current != ')') {
            reportCharacterExpectedError(')', current );
            skipTransform();
            return;
        }
        transformListHandler.rotate(theta, cx, cy);
    }
    protected void parseTranslate() throws ParseException, IOException {
        current = reader.read();
        if (current != 'r') {
            reportCharacterExpectedError('r', current );
            skipTransform();
            return;
        }
        current = reader.read();
        if (current != 'a') {
            reportCharacterExpectedError('a', current );
            skipTransform();
            return;
        }
        current = reader.read();
        if (current != 'n') {
            reportCharacterExpectedError('n', current );
            skipTransform();
            return;
        }
        current = reader.read();
        if (current != 's') {
            reportCharacterExpectedError('s', current );
            skipTransform();
            return;
        }
        current = reader.read();
        if (current != 'l') {
            reportCharacterExpectedError('l', current );
            skipTransform();
            return;
        }
        current = reader.read();
        if (current != 'a') {
            reportCharacterExpectedError('a', current );
            skipTransform();
            return;
        }
        current = reader.read();
        if (current != 't') {
            reportCharacterExpectedError('t', current );
            skipTransform();
            return;
        }
        current = reader.read();
        if (current != 'e') {
            reportCharacterExpectedError('e', current );
            skipTransform();
            return;
        }
        current = reader.read();
        skipSpaces();
        if (current != '(') {
            reportCharacterExpectedError('(', current );
            skipTransform();
            return;
        }
        current = reader.read();
        skipSpaces();
        float tx = parseFloat();
        skipSpaces();
        switch (current) {
        case ')':
            transformListHandler.translate(tx);
            return;
        case ',':
            current = reader.read();
            skipSpaces();
        }
        float ty = parseFloat();
        skipSpaces();
        if (current != ')') {
            reportCharacterExpectedError(')', current );
            skipTransform();
            return;
        }
        transformListHandler.translate(tx, ty);
    }
    protected void parseScale() throws ParseException, IOException {
        current = reader.read();
        if (current != 'a') {
            reportCharacterExpectedError('a', current );
            skipTransform();
            return;
        }
        current = reader.read();
        if (current != 'l') {
            reportCharacterExpectedError('l', current );
            skipTransform();
            return;
        }
        current = reader.read();
        if (current != 'e') {
            reportCharacterExpectedError('e', current );
            skipTransform();
            return;
        }
        current = reader.read();
        skipSpaces();
        if (current != '(') {
            reportCharacterExpectedError('(', current );
            skipTransform();
            return;
        }
        current = reader.read();
        skipSpaces();
        float sx = parseFloat();
        skipSpaces();
        switch (current) {
        case ')':
            transformListHandler.scale(sx);
            return;
        case ',':
            current = reader.read();
            skipSpaces();
        }
        float sy = parseFloat();
        skipSpaces();
        if (current != ')') {
            reportCharacterExpectedError(')', current );
            skipTransform();
            return;
        }
        transformListHandler.scale(sx, sy);
    }
    protected void parseSkew() throws ParseException, IOException {
        current = reader.read();
        if (current != 'e') {
            reportCharacterExpectedError('e', current );
            skipTransform();
            return;
        }
        current = reader.read();
        if (current != 'w') {
            reportCharacterExpectedError('w', current );
            skipTransform();
            return;
        }
        current = reader.read();
        boolean skewX = false;
        switch (current) {
        case 'X':
            skewX = true;
        case 'Y':
            break;
        default:
            reportCharacterExpectedError('X', current );
            skipTransform();
            return;
        }
        current = reader.read();
        skipSpaces();
        if (current != '(') {
            reportCharacterExpectedError('(', current );
            skipTransform();
            return;
        }
        current = reader.read();
        skipSpaces();
        float sk = parseFloat();
        skipSpaces();
        if (current != ')') {
            reportCharacterExpectedError(')', current );
            skipTransform();
            return;
        }
        if (skewX) {
            transformListHandler.skewX(sk);
        } else {
            transformListHandler.skewY(sk);
        }
    }
    protected void skipTransform() throws IOException {
        loop: for (;;) {
            current = reader.read();
            switch (current) {
            case ')':
                break loop;
            default:
                if (current == -1) {
                    break loop;
                }
            }
        }
    }
}
