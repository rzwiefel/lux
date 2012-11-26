package lux;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Iterator;

import lux.exception.LuxException;
import lux.index.IndexConfiguration;
import lux.index.XmlIndexer;
import lux.saxon.Expandifier;
import lux.xml.ValueType;
import lux.xquery.XQuery;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * executes all the BasicQueryTest test cases using path indexes and compares results against
 * an unindexed baseline.
 *
 */
public class SearchQueryTest extends BasicQueryTest {
    private static IndexTestSupport index;
    private static long elapsed=0;
    private static long elapsedBaseline=0;
    private static int repeatCount=1;
    
    @BeforeClass
    public static void setup () throws Exception {
        index = new IndexTestSupport();
    }
    
    @AfterClass
    public static void tearDown() throws Exception {
        index.close();
    }
    
    @Override
    public String getQueryString(Q q) {
        switch (q) {
        case ACT_SCENE: return "w(\"ACT\",\"SCENE\")";
        case SCENE: return "\"SCENE\"";
        default: throw new UnsupportedOperationException("No query string for " + q + " in " + getClass().getSimpleName());
        }
    }

    @Override
    public String getQueryXml (Q q) {
        switch (q) {
        case ACT_SCENE: return "<SpanNear ordered=\"true\" slop=\"1\">" +
        		"<SpanTerm>ACT</SpanTerm>" +
        		"<SpanTerm>SCENE</SpanTerm>" +
        		"</SpanNear>";
        case SCENE: return "SCENE";
        default: throw new UnsupportedOperationException("No query string for " + q + " in " + getClass().getSimpleName());
        }
    }

    @Override
    public XmlIndexer getIndexer() {
        return new XmlIndexer (IndexConfiguration.INDEX_PATHS);
    }

    /**
     * @param xpath the path to test
     * @param optimized ignored
     * @param facts ignored
     * @param queries ignored
     * @throws IOException 
     * @throws LockObtainFailedException 
     * @throws CorruptIndexException 
     */
    public void assertQuery (String xpath, String optimized, int facts, ValueType type, Q ... queries) throws CorruptIndexException, LockObtainFailedException, IOException {
        if (repeatCount > 1) {
            benchmark (xpath);
        } else {
            Evaluator saxon = index.makeEvaluator();
            XdmResultSet results = evalQuery(xpath, saxon);
            XdmValue baseResult = evalBaseline(xpath, saxon);
            assertEquals ("result count mismatch for: " + optimized, baseResult.size(), results.size());        
            Iterator<?> baseIter = baseResult.iterator();
            Iterator<?> resultIter = results.iterator();
            for (int i = 0 ; i < results.size(); i++) {
                XdmItem base = (XdmItem) baseIter.next();
                XdmItem r = (XdmItem) resultIter.next();
                assertEquals (base.isAtomicValue(), r.isAtomicValue());
                assertEquals (base.getStringValue(), r.getStringValue());
            }
        }
    }

    private void benchmark (String query) throws CorruptIndexException, LockObtainFailedException, IOException {
        Evaluator eval = index.makeEvaluator();
        XdmResultSet results = evalQuery(query, eval);
        XdmValue baselineResult = evalBaseline(query, eval);
        for (int i = 0; i < repeatCount; i++) {
            long t0 = System.nanoTime();
            evalQuery(query, eval);
            long t = System.nanoTime() - t0;
            elapsed += t;
            t0 = System.nanoTime();
            evalBaseline(query, eval);
            t = System.nanoTime() - t0;
            elapsedBaseline += t;
        }
        // TODO: also assert facts about query optimizations
        System.out.println (String.format("%dms using lux; %dms w/o lux", elapsed/1000000, elapsedBaseline/1000000));
        
        results = evalQuery(query, eval);
        System.out.println ("lux retrieved " + results.size() + " results from " + eval.getQueryStats());
        printDocReaderStats(eval);
        baselineResult = evalBaseline(query, eval);
        System.out.println ("baseline (no lux): retrieved " + baselineResult.size() + " results from " + eval.getQueryStats());
        printDocReaderStats(eval);
    }

    private void printDocReaderStats(Evaluator saxon) {
        System.out.println (String.format(" %d/%d cache hits/misses, %dms building docs", 
                saxon.getDocReader().getCacheHits(), saxon.getDocReader().getCacheMisses(),
                saxon.getDocReader().getBuildTime()/1000000));
    }

    private XdmValue evalBaseline(String xpath, Evaluator eval) {
        XdmValue baseResult;
        XQuery xq = null;
        XCompiler compiler = eval.getCompiler();
        try {
            xq = compiler.makeTranslator().queryFor(compiler.compile(xpath));
            xq = new Expandifier().expandify(xq);
            String expanded = xq.toString();
            XQueryExecutable baseline;
            baseline = compiler.compile(expanded);
            XQueryEvaluator baselineEval = baseline.load();
            baseResult = baselineEval.evaluate();
        } catch (SaxonApiException e) {
            throw new LuxException ("error evaluting query " + xq, e);
        }
        return baseResult;
    }

    private XdmResultSet evalQuery(String xpath, Evaluator eval) {
        eval.invalidateCache();
        eval.setQueryStats(new QueryStats());
        XQueryExecutable xquery = eval.getCompiler().compile(xpath);
        XdmResultSet results = eval.evaluate(xquery);
        if (results.getErrors() != null) {
            throw new LuxException(results.getErrors().iterator().next());
        }
        return results;
    }

}

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
