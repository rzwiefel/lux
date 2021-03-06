package lux.index.analysis;

import java.io.IOException;
import java.util.Map;

import lux.index.attribute.QNameAttribute;
import lux.xml.QName;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.CharsRef;

/**
 * Expand the input term by adding additional terms at the same position, prefixed by the node names (QNames)
 * found in the QNameAttribute.  The node name is serialized in reverse-Clark format: localname{namespace-uri}
 * if processing is namespace-aware.  Otherwise the node name is serialized as a lexical QName: prefix:localname
 * without regard to any namespace uri binding.
 * TODO: remove the unused namespace-unaware processing, or put it in another class?
 */
final public class QNameTokenFilter extends TokenFilter {

    private final QNameAttribute qnameAtt = addAttribute(QNameAttribute.class);
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final PositionIncrementAttribute posAtt = addAttribute(PositionIncrementAttribute.class);
    private final ElementVisibility defVis; 
    private final Map<String,ElementVisibility> elVis; 
    private boolean namespaceAware;
    private CharsRef term;

    protected QNameTokenFilter(TokenStream input) {
        this (input, ElementVisibility.OPAQUE, null);
    }
    
    protected QNameTokenFilter(TokenStream input, ElementVisibility defVis, Map<String,ElementVisibility> elVis) {
        super(input);
        term = new CharsRef();
        setNamespaceAware(true);
        this.defVis = defVis;
        this.elVis = elVis;
    }
    
    public final void reset (TokenStream inputAgain) {
        assert (input.getAttribute(CharTermAttribute.class) == inputAgain.getAttribute(CharTermAttribute.class));
    }
    
    @Override
    public boolean incrementToken() throws IOException {
        if ((! qnameAtt.hasNext()) || qnameAtt.onFirst()) {
            if (!input.incrementToken()) {
                return false;
            }
            // make a copy of the current term so we can prefix it below
            term.copyChars(termAtt.buffer(), 0, termAtt.length());
        }
        else {
            // set posIncr = 0 if this is not the first token emitted for this term
            posAtt.setPositionIncrement(0);
        }
        // emit <qname>:<term>
        QName qname = qnameAtt.next();
        termAtt.setEmpty();
        if (namespaceAware) {
            termAtt.append(qname.getEncodedName());
        } else {
            if (qname.getPrefix().length() > 0) {
                termAtt.append(qname.getPrefix()).append(':');
            }
            termAtt.append(qname.getLocalPart());
        }
        termAtt.append(':');
        termAtt.append(term);      
        return true;
    }

    /**
     * @return if true, indexed QNames include the namespace URI; otherwise they include the prefix.
     */
    public boolean isNamespaceAware() {
        return namespaceAware;
    }

    public void setNamespaceAware(boolean namespaceAware) {
        this.namespaceAware = namespaceAware;
    }

    public ElementVisibility getDefaultVisibility() {
        return defVis;
    }

    public Map<String, ElementVisibility> getElementVisibility() {
        return elVis;
    }
    
    public TokenStream getInput () {
        return input;
    }

}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/.
 */
