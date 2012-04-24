package lux.saxon;

/**
 * Extends saxon Configuration providing lux-specific configuration - for the moment, this is
 * mostly a place-holder for possible future expansion.
 */

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import net.sf.saxon.Configuration;

public class Config extends Configuration implements URIResolver, EntityResolver {
    public Config () {
        super();
        getParseOptions().setEntityResolver(this);
    }
    
    // TODO resolve uris!
    public Source resolve(String href, String base) throws TransformerException {
        return new StreamSource (new ByteArrayInputStream (new byte[0]));
    }

    // disable dtd processing
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        return new InputSource (new ByteArrayInputStream (new byte[0]));
    }
}