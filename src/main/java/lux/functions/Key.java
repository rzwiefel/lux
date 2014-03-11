package lux.functions;

import lux.index.field.FieldDefinition;
import lux.search.SearchService;
import lux.xpath.FunCall;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceType;

/**
* <code>function lux:key($field-name as xs:string, $node as node()) as xs:anyAtomicItem*</code>
* <code>function lux:key($field-name as xs:string) as xs:anyAtomicItem*</code>
* 
* <p>Accepts the name of a lucene field and optionally, a node, and returns
* any stored value(s) of the field for the document containing
* the node, or the context item if no node is specified.  Analogous to the XSLT key() function.
* </p>
* 
* <p>
* If the node (or context item) is not a node drawn from the index, lux:key will return the
* empty sequence.
* </p>
* 
* <p>
* Order by expressions containing lux:key calls are subject to special optimization and are often able to be
* implemented by index-optimized sorting in Lucene (for fields whose values are string-, integer-, or long-valued only).  
* An error results if an attempt is made
* to sort by a field that has multiple values for any of the documents in the sequence.
* </p>
*/
public class Key extends ExtensionFunctionDefinition {

    @Override
    public StructuredQName getFunctionQName() {
        return new StructuredQName ("lux", FunCall.LUX_NAMESPACE, "key");
    }

    @Override
    public SequenceType[] getArgumentTypes() {
        return new SequenceType[] {
                SequenceType.SINGLE_STRING,
                SequenceType.OPTIONAL_NODE
        };
    }
    
    @Override
    public int getMinimumNumberOfArguments() {
        return 1;
    }

    @Override
    public int getMaximumNumberOfArguments() {
        return 2;
    }
    
    @Override
    public boolean trustResultType() {
        return true;
    }
    
    @Override
    public boolean dependsOnFocus () {
        return true;
    }
    
    @Override
    public net.sf.saxon.value.SequenceType getResultType(net.sf.saxon.value.SequenceType[] suppliedArgumentTypes) {
        return SequenceType.ATOMIC_SEQUENCE;
    }

    @Override
    public ExtensionFunctionCall makeCallExpression() {
        return new KeyCall();
    }
    
    class KeyCall extends ExtensionFunctionCall {

        @Override
        public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
            String fieldName = arguments[0].head().getStringValue();
            NodeInfo node;
            if (arguments.length == 1) {
                Item contextItem = context.getContextItem();
                if (! (contextItem instanceof NodeInfo)) {
                    throw new XPathException ("Call to lux:key($field-name) depends on context, but there is no context defined");
                }
                node = (NodeInfo) contextItem;
            } else {
                node = (NodeInfo) arguments[1].head();
            }
            if (node == null) {
                return EmptySequence.getInstance();
            }
            SearchService searchService = SearchBase.getSearchService(context);
            FieldDefinition field = searchService.getEvaluator().getCompiler().getIndexConfiguration().getField(fieldName);
            if (field == null) {
                return EmptySequence.getInstance();
                // TODO: raise an exception!
                // throw new XPathException ("lux:key() called with undefined field '" + fieldName + "'");
            }
            return searchService.key(field, node);
        }
    }

}

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
