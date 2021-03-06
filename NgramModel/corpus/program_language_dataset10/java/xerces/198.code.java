package org.apache.xerces.dom;
import java.lang.ref.SoftReference;
import org.apache.xerces.impl.RevalidationHandler;
import org.apache.xerces.impl.dtd.XMLDTDLoader;
import org.apache.xerces.parsers.DOMParserImpl;
import org.apache.xerces.util.XMLChar;
import org.apache.xerces.xni.grammars.XMLGrammarDescription;
import org.apache.xml.serialize.DOMSerializerImpl;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.ls.LSSerializer;
public class CoreDOMImplementationImpl
	implements DOMImplementation, DOMImplementationLS {
    private static final int SIZE = 2;
    private SoftReference schemaValidators[] = new SoftReference[SIZE];
    private SoftReference xml10DTDValidators[] = new SoftReference[SIZE];
    private SoftReference xml11DTDValidators[] = new SoftReference[SIZE];
    private int freeSchemaValidatorIndex = -1;
    private int freeXML10DTDValidatorIndex = -1;
    private int freeXML11DTDValidatorIndex = -1;
    private int schemaValidatorsCurrentSize = SIZE;
    private int xml10DTDValidatorsCurrentSize = SIZE;
    private int xml11DTDValidatorsCurrentSize = SIZE;
    private SoftReference xml10DTDLoaders[] = new SoftReference[SIZE];
    private SoftReference xml11DTDLoaders[] = new SoftReference[SIZE];
    private int freeXML10DTDLoaderIndex = -1;
    private int freeXML11DTDLoaderIndex = -1;
    private int xml10DTDLoaderCurrentSize = SIZE;
    private int xml11DTDLoaderCurrentSize = SIZE;
    private int docAndDoctypeCounter = 0;
	static final CoreDOMImplementationImpl singleton = new CoreDOMImplementationImpl();
	public static DOMImplementation getDOMImplementation() {
		return singleton;
	}
	public boolean hasFeature(String feature, String version) {
	    boolean anyVersion = version == null || version.length() == 0;
	    if ((feature.equalsIgnoreCase("+XPath"))       
	        && (anyVersion || version.equals("3.0"))) {
	        try {
	            Class xpathClass = ObjectFactory.findProviderClass(
	                "org.apache.xpath.domapi.XPathEvaluatorImpl",
	                ObjectFactory.findClassLoader(), true);
                Class interfaces[] = xpathClass.getInterfaces();
                for (int i = 0; i < interfaces.length; i++) {
                    if (interfaces[i].getName().equals(
                        "org.w3c.dom.xpath.XPathEvaluator")) {
                        return true;
                    }
                }
	        } catch (Exception e) {
	            return false;
	        }
	        return true;
	    }
	    if (feature.startsWith("+")) {
	        feature = feature.substring(1);
	    }
	    return (
	        feature.equalsIgnoreCase("Core")
	            && (anyVersion
	                || version.equals("1.0")
	                || version.equals("2.0")
	                || version.equals("3.0")))
	                || (feature.equalsIgnoreCase("XML")
	            && (anyVersion
	                || version.equals("1.0")
	                || version.equals("2.0")
	                || version.equals("3.0")))
	                || (feature.equalsIgnoreCase("XMLVersion")
	            && (anyVersion
	                || version.equals("1.0")
	                || version.equals("1.1")))
	                || (feature.equalsIgnoreCase("LS")
	            && (anyVersion 
	                || version.equals("3.0")))
	                || (feature.equalsIgnoreCase("ElementTraversal")
	            && (anyVersion
	                || version.equals("1.0")));
	} 
	public DocumentType createDocumentType( String qualifiedName,
                                    String publicID, String systemID) {
		checkQName(qualifiedName);
		return new DocumentTypeImpl(null, qualifiedName, publicID, systemID);
	}
    final void checkQName(String qname){
        int index = qname.indexOf(':');
        int lastIndex = qname.lastIndexOf(':');
        int length = qname.length();
        if (index == 0 || index == length - 1 || lastIndex != index) {
            String msg =
                DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN,
                    "NAMESPACE_ERR",
                    null);
            throw new DOMException(DOMException.NAMESPACE_ERR, msg);
        }
        int start = 0;
        if (index > 0) {
            if (!XMLChar.isNCNameStart(qname.charAt(start))) {
                String msg =
                    DOMMessageFormatter.formatMessage(
                        DOMMessageFormatter.DOM_DOMAIN,
                        "INVALID_CHARACTER_ERR",
                        null);
                throw new DOMException(DOMException.INVALID_CHARACTER_ERR, msg);
            }
            for (int i = 1; i < index; i++) {
                if (!XMLChar.isNCName(qname.charAt(i))) {
                    String msg =
                        DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN,
                            "INVALID_CHARACTER_ERR",
                            null);
                    throw new DOMException(
                        DOMException.INVALID_CHARACTER_ERR,
                        msg);
                }
            }
            start = index + 1;
        }
        if (!XMLChar.isNCNameStart(qname.charAt(start))) {
            String msg =
                DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN,
                    "INVALID_CHARACTER_ERR",
                    null);
            throw new DOMException(DOMException.INVALID_CHARACTER_ERR, msg);
        }
        for (int i = start + 1; i < length; i++) {
            if (!XMLChar.isNCName(qname.charAt(i))) {
                String msg =
                    DOMMessageFormatter.formatMessage(
                        DOMMessageFormatter.DOM_DOMAIN,
                        "INVALID_CHARACTER_ERR",
                        null);
                throw new DOMException(DOMException.INVALID_CHARACTER_ERR, msg);
            }
        }
    }
	public Document createDocument(
		String namespaceURI,
		String qualifiedName,
		DocumentType doctype)
		throws DOMException {
		if (doctype != null && doctype.getOwnerDocument() != null) {
			String msg =
				DOMMessageFormatter.formatMessage(
					DOMMessageFormatter.DOM_DOMAIN,
					"WRONG_DOCUMENT_ERR",
					null);
			throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, msg);
		}
		CoreDocumentImpl doc = createDocument(doctype);
		if (qualifiedName != null || namespaceURI != null) {
		    Element e = doc.createElementNS(namespaceURI, qualifiedName);
		    doc.appendChild(e);
		}
		return doc;
	}
	protected CoreDocumentImpl createDocument(DocumentType doctype) {
	    return new CoreDocumentImpl(doctype);
	}
	public Object getFeature(String feature, String version) {
	    if (singleton.hasFeature(feature, version)) {
	        if ((feature.equalsIgnoreCase("+XPath"))) {
	            try {
	                Class xpathClass = ObjectFactory.findProviderClass(
	                    "org.apache.xpath.domapi.XPathEvaluatorImpl",
	                    ObjectFactory.findClassLoader(), true);
	                Class interfaces[] = xpathClass.getInterfaces();
	                for (int i = 0; i < interfaces.length; i++) {
	                    if (interfaces[i].getName().equals(
	                        "org.w3c.dom.xpath.XPathEvaluator")) {
	                        return xpathClass.newInstance();
	                    }
	                }
	            } catch (Exception e) {
	                return null;
	            }
	        } else {
	            return singleton;
	        }
	    }
	    return null;
	}
    public LSParser createLSParser(short mode, String schemaType)
		throws DOMException {
		if (mode != DOMImplementationLS.MODE_SYNCHRONOUS || (schemaType !=null &&
		   !"http://www.w3.org/2001/XMLSchema".equals(schemaType) &&
			!"http://www.w3.org/TR/REC-xml".equals(schemaType))) {
			String msg =
				DOMMessageFormatter.formatMessage(
					DOMMessageFormatter.DOM_DOMAIN,
					"NOT_SUPPORTED_ERR",
					null);
			throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
		}
		if (schemaType != null
			&& schemaType.equals("http://www.w3.org/TR/REC-xml")) {
			return new DOMParserImpl(
				"org.apache.xerces.parsers.DTDConfiguration",
				schemaType);
		}
		else {
			return new DOMParserImpl(
				"org.apache.xerces.parsers.XIncludeAwareParserConfiguration",
				schemaType);
		}
	}
    public LSSerializer createLSSerializer() {
        try {
            Class serializerClass = ObjectFactory.findProviderClass(
                "org.apache.xml.serializer.dom3.LSSerializerImpl",
                ObjectFactory.findClassLoader(), true);
            return (LSSerializer) serializerClass.newInstance();
        }
        catch (Exception e) {}
        return new DOMSerializerImpl();
    }
	public LSInput createLSInput() {
		return new DOMInputImpl();
	}
	synchronized RevalidationHandler getValidator(String schemaType, String xmlVersion) {
        if (schemaType == XMLGrammarDescription.XML_SCHEMA) {
            while (freeSchemaValidatorIndex >= 0) {
                SoftReference ref = schemaValidators[freeSchemaValidatorIndex];
                RevalidationHandlerHolder holder = (RevalidationHandlerHolder) ref.get();
                if (holder != null && holder.handler != null) {
                    RevalidationHandler val = holder.handler;
                    holder.handler = null;
                    --freeSchemaValidatorIndex;
                    return val;
                }
                schemaValidators[freeSchemaValidatorIndex--] = null;
            }
            return (RevalidationHandler) (ObjectFactory
                    .newInstance(
                        "org.apache.xerces.impl.xs.XMLSchemaValidator",
                        ObjectFactory.findClassLoader(),
                        true));
        }
        else if(schemaType == XMLGrammarDescription.XML_DTD) {
            if ("1.1".equals(xmlVersion)) {
                while (freeXML11DTDValidatorIndex >= 0) {
                    SoftReference ref = xml11DTDValidators[freeXML11DTDValidatorIndex];
                    RevalidationHandlerHolder holder = (RevalidationHandlerHolder) ref.get();
                    if (holder != null && holder.handler != null) {
                        RevalidationHandler val = holder.handler;
                        holder.handler = null;
                        --freeXML11DTDValidatorIndex;
                        return val;
                    }
                    xml11DTDValidators[freeXML11DTDValidatorIndex--] = null;
                }
                return (RevalidationHandler) (ObjectFactory
                        .newInstance(
                                "org.apache.xerces.impl.dtd.XML11DTDValidator",
                                ObjectFactory.findClassLoader(),
                                true));
            }
            else {
                while (freeXML10DTDValidatorIndex >= 0) {
                    SoftReference ref = xml10DTDValidators[freeXML10DTDValidatorIndex];
                    RevalidationHandlerHolder holder = (RevalidationHandlerHolder) ref.get();
                    if (holder != null && holder.handler != null) {
                        RevalidationHandler val = holder.handler;
                        holder.handler = null;
                        --freeXML10DTDValidatorIndex;
                        return val;
                    }
                    xml10DTDValidators[freeXML10DTDValidatorIndex--] = null;
                }
                return (RevalidationHandler) (ObjectFactory
                        .newInstance(
                            "org.apache.xerces.impl.dtd.XMLDTDValidator",
                            ObjectFactory.findClassLoader(),
                            true));
            }
        }
        return null;
	}
	synchronized void releaseValidator(String schemaType, String xmlVersion,
	        RevalidationHandler validator) {
	    if (schemaType == XMLGrammarDescription.XML_SCHEMA) {
	        ++freeSchemaValidatorIndex;
	        if (schemaValidators.length == freeSchemaValidatorIndex) {
	            schemaValidatorsCurrentSize += SIZE;
	            SoftReference newarray[] =  new SoftReference[schemaValidatorsCurrentSize];
	            System.arraycopy(schemaValidators, 0, newarray, 0, schemaValidators.length);
	            schemaValidators = newarray;
	        }
	        SoftReference ref = schemaValidators[freeSchemaValidatorIndex];
	        if (ref != null) {
	            RevalidationHandlerHolder holder = (RevalidationHandlerHolder) ref.get();
	            if (holder != null) {
	                holder.handler = validator;
	                return;
	            }
	        }
	        schemaValidators[freeSchemaValidatorIndex] = new SoftReference(new RevalidationHandlerHolder(validator));
	    }
	    else if (schemaType == XMLGrammarDescription.XML_DTD) {
	        if ("1.1".equals(xmlVersion)) {
	            ++freeXML11DTDValidatorIndex;
	            if (xml11DTDValidators.length == freeXML11DTDValidatorIndex) {
	                xml11DTDValidatorsCurrentSize += SIZE;
	                SoftReference [] newarray = new SoftReference[xml11DTDValidatorsCurrentSize];
	                System.arraycopy(xml11DTDValidators, 0, newarray, 0, xml11DTDValidators.length);
	                xml11DTDValidators = newarray;
	            }
	            SoftReference ref = xml11DTDValidators[freeXML11DTDValidatorIndex];
	            if (ref != null) {
	                RevalidationHandlerHolder holder = (RevalidationHandlerHolder) ref.get();
	                if (holder != null) {
	                    holder.handler = validator;
	                    return;
	                }
	            }
	            xml11DTDValidators[freeXML11DTDValidatorIndex] = new SoftReference(new RevalidationHandlerHolder(validator));
	        }
	        else {
	            ++freeXML10DTDValidatorIndex;
	            if (xml10DTDValidators.length == freeXML10DTDValidatorIndex) {
	                xml10DTDValidatorsCurrentSize += SIZE;
	                SoftReference [] newarray = new SoftReference[xml10DTDValidatorsCurrentSize];
	                System.arraycopy(xml10DTDValidators, 0, newarray, 0, xml10DTDValidators.length);
	                xml10DTDValidators = newarray;
	            }
	            SoftReference ref = xml10DTDValidators[freeXML10DTDValidatorIndex];
	            if (ref != null) {
	                RevalidationHandlerHolder holder = (RevalidationHandlerHolder) ref.get();
	                if (holder != null) {
	                    holder.handler = validator;
	                    return;
	                }
	            }
	            xml10DTDValidators[freeXML10DTDValidatorIndex] = new SoftReference(new RevalidationHandlerHolder(validator));
	        }
	    }
	}
    synchronized final XMLDTDLoader getDTDLoader(String xmlVersion) {
        if ("1.1".equals(xmlVersion)) {
            while (freeXML11DTDLoaderIndex >= 0) {
                SoftReference ref = xml11DTDLoaders[freeXML11DTDLoaderIndex];
                XMLDTDLoaderHolder holder = (XMLDTDLoaderHolder) ref.get();
                if (holder != null && holder.loader != null) {
                    XMLDTDLoader val = holder.loader;
                    holder.loader = null;
                    --freeXML11DTDLoaderIndex;
                    return val;
                }
                xml11DTDLoaders[freeXML11DTDLoaderIndex--] = null;
            }
            return (XMLDTDLoader) (ObjectFactory
                    .newInstance(
                        "org.apache.xerces.impl.dtd.XML11DTDProcessor",
                        ObjectFactory.findClassLoader(),
                        true));
        }
        else {
            while (freeXML10DTDLoaderIndex >= 0) {
                SoftReference ref = xml10DTDLoaders[freeXML10DTDLoaderIndex];
                XMLDTDLoaderHolder holder = (XMLDTDLoaderHolder) ref.get();
                if (holder != null && holder.loader != null) {
                    XMLDTDLoader val = holder.loader;
                    holder.loader = null;
                    --freeXML10DTDLoaderIndex;
                    return val;
                }
                xml10DTDLoaders[freeXML10DTDLoaderIndex--] = null;
            }
            return new XMLDTDLoader();
        }
    }
    synchronized final void releaseDTDLoader(String xmlVersion, XMLDTDLoader loader) {
        if ("1.1".equals(xmlVersion)) {
            ++freeXML11DTDLoaderIndex;
            if (xml11DTDLoaders.length == freeXML11DTDLoaderIndex) {
                xml11DTDLoaderCurrentSize += SIZE;
                SoftReference [] newarray = new SoftReference[xml11DTDLoaderCurrentSize];
                System.arraycopy(xml11DTDLoaders, 0, newarray, 0, xml11DTDLoaders.length);
                xml11DTDLoaders = newarray;
            }
            SoftReference ref = xml11DTDLoaders[freeXML11DTDLoaderIndex];
            if (ref != null) {
                XMLDTDLoaderHolder holder = (XMLDTDLoaderHolder) ref.get();
                if (holder != null) {
                    holder.loader = loader;
                    return;
                }
            }
            xml11DTDLoaders[freeXML11DTDLoaderIndex] = new SoftReference(new XMLDTDLoaderHolder(loader));
        }
        else {
            ++freeXML10DTDLoaderIndex;
            if (xml10DTDLoaders.length == freeXML10DTDLoaderIndex) {
                xml10DTDLoaderCurrentSize += SIZE;
                SoftReference [] newarray = new SoftReference[xml10DTDLoaderCurrentSize];
                System.arraycopy(xml10DTDLoaders, 0, newarray, 0, xml10DTDLoaders.length);
                xml10DTDLoaders = newarray;
            }
            SoftReference ref = xml10DTDLoaders[freeXML10DTDLoaderIndex];
            if (ref != null) {
                XMLDTDLoaderHolder holder = (XMLDTDLoaderHolder) ref.get();
                if (holder != null) {
                    holder.loader = loader;
                    return;
                }
            }
            xml10DTDLoaders[freeXML10DTDLoaderIndex] = new SoftReference(new XMLDTDLoaderHolder(loader));
        }
    }
	protected synchronized int assignDocumentNumber() {
	    return ++docAndDoctypeCounter;
	}
	protected synchronized int assignDocTypeNumber() {
	    return ++docAndDoctypeCounter;
	}
	public LSOutput createLSOutput() {
	    return new DOMOutputImpl();
	}
    static final class RevalidationHandlerHolder {
        RevalidationHandlerHolder(RevalidationHandler handler) {
            this.handler = handler;
        }
        RevalidationHandler handler;
    }
    static final class XMLDTDLoaderHolder {
        XMLDTDLoaderHolder(XMLDTDLoader loader) {
            this.loader = loader;
        }
        XMLDTDLoader loader;
    }
} 
