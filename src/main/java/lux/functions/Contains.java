package lux.functions;

import lux.Evaluator;
import lux.xpath.FunCall;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.xml.ParserException;
import org.apache.lucene.search.Query;
import org.slf4j.LoggerFactory;

/**
 * <code>function lux:contains($query as item()) as xs:boolean</code>
 * <p>Executes a Lucene search query and returns true if the query matches the context item. 
 * Queries are parsed as for lux:search().</p>
*/
public class Contains extends ExtensionFunctionDefinition {

    @Override
    public StructuredQName getFunctionQName() {
        return new StructuredQName("lux", FunCall.LUX_NAMESPACE, "contains");
    }

    @Override
    public SequenceType[] getArgumentTypes() {
        return new SequenceType[] { 
                SequenceType.SINGLE_ITEM,       // query: as element node or string
        };
    }

    @Override
    public int getMaximumNumberOfArguments() {
        return 1;
    }
    
    @Override
    public SequenceType getResultType(SequenceType[] arg0) {
        return SequenceType.SINGLE_BOOLEAN;
    }

    @Override
    public ExtensionFunctionCall makeCallExpression() {
        return new ContainsCall ();
    }
    
    public class ContainsCall extends NamespaceAwareFunctionCall {
        
        @SuppressWarnings("rawtypes") @Override
        public SequenceIterator<? extends Item> call(SequenceIterator[] arguments, XPathContext context) throws XPathException {
            
            Item queryArg = arguments[0].next();
            
            // TODO: refactor to NAFC?
            Evaluator eval = SearchBase.getEvaluator(context);
            Query query;
            try {
                query = parseQuery(queryArg, eval);
            } catch (ParseException e) {
                throw new XPathException (e.getMessage(), e);
            } catch (ParserException e) {
                throw new XPathException ("Failed to parse xml query : " + e.getMessage(), e);
            }
            LoggerFactory.getLogger(SearchBase.class).debug("executing query: {}", query);
            // TODO: implement by (1) enhancing PathOptimizer to incorporate our query with the 
            // context query, and (2) traversing our context item using XmlHighlighter with an enhanced
            // HighlightFormatter that can terminate early and cause a result to be returned indicating
            // a match was found
            throw new XPathException ("lux:contains is not implemented");
        }
        
    }

}