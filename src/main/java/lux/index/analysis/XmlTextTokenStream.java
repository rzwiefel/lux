package lux.index.analysis;

import lux.xml.Offsets;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

/**
 * Extracts tokens from an s9api XML document tree (XdmNode) in order to make them
 * available to Lucene classes that accept TokenStreams, like the indexer and highlighter.
 */
public final class XmlTextTokenStream extends TextOffsetTokenStream {

    protected final TokenStream origWrapped;
    
    /**
     * Creates a TokenStream returning tokens drawn from the text content of the document.
     * @param fieldName nominally: the field to be analyzed; the analyzer receives this when the
     * token stream is reset at node boundaries
     * @param analyzer specifies what text processing to apply to node text
     * @param wrapped a TokenStream generated by the analyzer
     * @param doc tokens will be drawn from all of the text in this document
     * @param offsets if provided, character offsets are captured in this object
     * In theory this can be used for faster highlighting, but until that is proven, 
     * this should always be null.
     */
    
    public XmlTextTokenStream(String fieldName, Analyzer analyzer, TokenStream wrapped, XdmNode doc, Offsets offsets) {
        super(fieldName, analyzer, wrapped, doc, offsets);
        contentIter = new ContentIterator(doc);
        origWrapped = wrapped;
    }

    @Override
    void updateNodeAtts() {
        AncestorIterator nodeAncestors = new AncestorIterator(curNode);
        while (nodeAncestors.hasNext()) {
            XdmNode e = (XdmNode) nodeAncestors.next();
            assert (e.getNodeKind() == XdmNodeKind.ELEMENT);
            QName qname = e.getNodeName();
            if (eltVis.get(qname) == ElementVisibility.HIDDEN) {
                setWrappedTokenStream(empty);
                return;
            }
        }
        setWrappedTokenStream(origWrapped);
    }

}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/.
 */
