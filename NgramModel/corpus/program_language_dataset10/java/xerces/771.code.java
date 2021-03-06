package dom.serialize;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.dom.NodeImpl;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import dom.Writer;
public class TestSerializeDOMIn {
    public TestSerializeDOMIn() {
    }
    public DocumentImpl deserializeDOM( String nameSerializedFile ){
        ObjectInputStream in   = null;
        DocumentImpl      doc  = null;
        try {
            FileInputStream fileIn = new FileInputStream( nameSerializedFile );
            in                     = new ObjectInputStream(fileIn);
            doc                    = (DocumentImpl) in.readObject();
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
        return doc;
    }
    public static void main( String argv[]  ){
        if ( argv.length != 2 ) {
            System.out.println("Error - Usage: java TestSerializeDOMIn yourFile.ser elementName" );
            System.exit(1);
        }
        String    xmlFilename = argv[0];
        TestSerializeDOMIn         tst  = new TestSerializeDOMIn();
        DocumentImpl   doc  = tst.deserializeDOM( xmlFilename );
        NodeList nl         = doc.getElementsByTagName( argv[1]);
        int length      = nl.getLength();
        if ( length == 0 )
            System.out.println(argv[1] + ": is not in the document!");
        NodeImpl node = null;
        for ( int i = 0;i<length;i++ ){
            node                = (NodeImpl) nl.item(i);
            Node childOfElement = node.getFirstChild();
            if ( childOfElement != null ){
                System.out.println( node.getNodeName() + ": " +
                                    childOfElement.getNodeValue() );
            }
        }
        try {
           Writer prettyWriter = new Writer( false );
           System.out.println( "Here is the whole Document" );
           prettyWriter.write(  doc.getDocumentElement() );
        } catch( Exception ex ){
        }
    }
}
