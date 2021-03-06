package org.apache.batik.transcoder.wmf.tosvg;
import java.awt.Dimension;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.transcoder.ToSVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
public class WMFTranscoder extends ToSVGAbstractTranscoder {
    public WMFTranscoder(){
    }
    public void transcode(TranscoderInput input, TranscoderOutput output)
        throws TranscoderException {
        DataInputStream is = getCompatibleInput(input);
        WMFRecordStore currentStore = new WMFRecordStore();
        try {
            currentStore.read(is);
        } catch (IOException e){
            handler.fatalError(new TranscoderException(e));
            return;
        }
        float wmfwidth; 
        float wmfheight; 
        float conv = 1.0f; 
        if (hints.containsKey(KEY_INPUT_WIDTH)) {
            wmfwidth = ((Integer)hints.get(KEY_INPUT_WIDTH)).intValue();
            wmfheight = ((Integer)hints.get(KEY_INPUT_HEIGHT)).intValue();
        } else {
            wmfwidth = currentStore.getWidthPixels();
            wmfheight = currentStore.getHeightPixels();
        }
        float width = wmfwidth;
        float height = wmfheight;
        if (hints.containsKey(KEY_WIDTH)) {
            width = ((Float)hints.get(KEY_WIDTH)).floatValue();
            conv = width / wmfwidth;
            height = height * width / wmfwidth;
        }
        int xOffset = 0;
        int yOffset = 0;
        if (hints.containsKey(KEY_XOFFSET)) {
            xOffset = ((Integer)hints.get(KEY_XOFFSET)).intValue();
        }
        if (hints.containsKey(KEY_YOFFSET)) {
            yOffset = ((Integer)hints.get(KEY_YOFFSET)).intValue();
        }
        float sizeFactor = currentStore.getUnitsToPixels() * conv;
        int vpX = (int)(currentStore.getVpX() * sizeFactor);
        int vpY = (int)(currentStore.getVpY() * sizeFactor);
        int vpW;
        int vpH;
        if (hints.containsKey(KEY_INPUT_WIDTH)) {
            vpW = (int)(((Integer)hints.get(KEY_INPUT_WIDTH)).intValue() * conv);
            vpH = (int)(((Integer)hints.get(KEY_INPUT_HEIGHT)).intValue() * conv);
        } else {
            vpW = (int)(currentStore.getWidthUnits() * sizeFactor);
            vpH = (int)(currentStore.getHeightUnits() * sizeFactor);
        }
        WMFPainter painter = new WMFPainter(currentStore, xOffset, yOffset, conv);
        Document doc = this.createDocument(output);
        svgGenerator = new SVGGraphics2D(doc);
        svgGenerator.getGeneratorContext().setPrecision(4);
        painter.paint(svgGenerator);
        svgGenerator.setSVGCanvasSize(new Dimension(vpW, vpH));
        Element svgRoot = svgGenerator.getRoot();
        svgRoot.setAttributeNS(null, SVG_VIEW_BOX_ATTRIBUTE,
                                String.valueOf( vpX ) + ' ' + vpY + ' ' +
                               vpW + ' ' + vpH );
        writeSVGToOutput(svgGenerator, svgRoot, output);
    }
    private DataInputStream getCompatibleInput(TranscoderInput input)
        throws TranscoderException {
        if (input == null){
            handler.fatalError(new TranscoderException( String.valueOf( ERROR_NULL_INPUT ) ));
        }
        InputStream in = input.getInputStream();
        if (in != null){
            return new DataInputStream(new BufferedInputStream(in));
        }
        String uri = input.getURI();
        if (uri != null){
            try{
                URL url = new URL(uri);
                in = url.openStream();
                return new DataInputStream(new BufferedInputStream(in));
            } catch (MalformedURLException e){
                handler.fatalError(new TranscoderException(e));
            } catch (IOException e){
                handler.fatalError(new TranscoderException(e));
            }
        }
        handler.fatalError(new TranscoderException( String.valueOf( ERROR_INCOMPATIBLE_INPUT_TYPE ) ));
        return null;
    }
    public static final String WMF_EXTENSION = ".wmf";
    public static final String SVG_EXTENSION = ".svg";
    public static void main(String[] args) throws TranscoderException {
        if(args.length < 1){
            System.out.println("Usage : WMFTranscoder.main <file 1> ... <file n>");
            System.exit(1);
        }
        WMFTranscoder transcoder = new WMFTranscoder();
        int nFiles = args.length;
        for(int i=0; i<nFiles; i++){
            String fileName = args[i];
            if(!fileName.toLowerCase().endsWith(WMF_EXTENSION)){
                System.err.println(args[i] + " does not have the " + WMF_EXTENSION + " extension. It is ignored");
            }
            else{
                System.out.print("Processing : " + args[i] + "...");
                String outputFileName = fileName.substring(0, fileName.toLowerCase().indexOf(WMF_EXTENSION)) + SVG_EXTENSION;
                File inputFile = new File(fileName);
                File outputFile = new File(outputFileName);
                try {
                    TranscoderInput input = new TranscoderInput(inputFile.toURL().toString());
                    TranscoderOutput output = new TranscoderOutput(new FileOutputStream(outputFile));
                    transcoder.transcode(input, output);
                }catch(MalformedURLException e){
                    throw new TranscoderException(e);
                }catch(IOException e){
                    throw new TranscoderException(e);
                }
                System.out.println(".... Done");
            }
        }
        System.exit(0);
    }
}
