package lux.xqts;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import lux.api.QueryContext;
import lux.api.QueryStats;
import lux.api.ResultSet;
import lux.saxon.SaxonExpr;
import lux.xpath.QName;
import lux.xqts.TestCase.ComparisonMode;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;

import org.junit.BeforeClass;
import org.junit.Test;

public class TestRunner extends RunnerBase {

    @BeforeClass
    public static void setup() throws Exception {
        // By default, no indexes are created, and no Lux query optimizations are performed.
        setup(0, "TestSources");
    }

    private boolean runTest (String caseName) throws Exception {
        TestCase test1 = catalog.getTestCaseByName(caseName);
        assertNotNull (caseName, test1);
        return runTest (test1);
    }
    
    private boolean runTest (TestCase test1) throws Exception {
        ++numtests;
        if (printDetailedDiagnostics) {
            ErrorIgnorer ignorer = (ErrorIgnorer) eval.getConfig().getErrorListener();
            ignorer.setShowErrors(true);
        }
        QueryContext context = new QueryContext();
        try {
            bindExternalVariables(test1, context);
            SaxonExpr expr = (SaxonExpr) eval.compile(test1.getQueryText());
            context.setContextItem(test1.getContextItem());
            //System.out.println (expr);
            QueryStats stats = new QueryStats();
            eval.setQueryStats(stats);
            ResultSet<?> results = (ResultSet<?>) eval.evaluate(expr, context);
            if (results.getException() != null) {
                throw results.getException();
            }
            Boolean comparedEqual = test1.compareResult (results);
            if (comparedEqual == null || comparedEqual) {
                //System.out.println (test1.getName() + " OK in " + stats.totalTime + "ms");
                return true;
            } else {
                System.err.println (test1.getName() + " Mismatch: " + TestCase.resultToString(results) + " is not " + test1.getOutputText()[0]);
                ++numfailed;
                // debugging diagnostics:
                if (printDetailedDiagnostics) {
                    XQueryExecutable xq = eval.getProcessor().newXQueryCompiler().compile(test1.getQueryText());
                    XdmItem item = xq.load().evaluateSingle();
                    if (! test1.compareResult(item)) {
                        System.err.println (test1.getName() + " Saxon fails too?");
                    } else {
                        System.err.println (eval.getTranslator().queryFor(xq));
                    }
                }
                return false;
            }
        } catch (Exception e) {
            // Saxon's XQTS report says it doesn't check actual error codes, so neither do we
            if (! (test1.isExpectError() || test1.getComparisonMode() == ComparisonMode.Ignore)) { 
                ++numfailed;
                String error = e.getMessage();
                if (error != null && error.length() > 1024) {
                    error = error.substring(0, 1024);
                }
                System.err.println (test1.getName() + " at " + test1.getPath() + " Unexpected Error: " + error);
                // diagnostics:
                if (printDetailedDiagnostics ) {
                    XQueryExecutable xq = eval.getProcessor().newXQueryCompiler().compile(test1.getQueryText());
                    XQueryEvaluator xqeval = xq.load();
                    for (Map.Entry<QName, Object> binding : context.getVariableBindings().entrySet()) {
                        net.sf.saxon.s9api.QName saxonQName = new net.sf.saxon.s9api.QName(binding.getKey());
                        xqeval.setExternalVariable(saxonQName, (XdmValue) binding.getValue());
                    }                    
                    XdmItem item = xqeval.evaluateSingle();
                    System.err.println (test1.getQueryText() + " returns " + item);
                }
                if (terminateOnException) {
                    throw (e); 
                } else {
                    return false;
                }
            }
            //System.out.println (test1.getName() + " OK (expected error)");
            return true;
        }
    }

    private void runTestGroup (String groupName) throws Exception {
        TestGroup group = catalog.getTestGroupByName(groupName);
        assertNotNull (groupName, group);
        testOneGroup (group);
    }

    private void testOneGroup (TestGroup group) throws Exception {
        //System.out.println(group.getBannerString());
        for (TestCase test : group.getTestCases()) {
            runTest (test);
        }
        for (TestGroup subgroup : group.getSubGroups()) {
            testOneGroup (subgroup);
        }
    }
    
    @Test public void testOneTest() throws Exception {
        printDetailedDiagnostics = true;
        // assertTrue (runTest ("K2-NameTest-68"));  // fails since we don't handle specialized node types
        // Constr-cont-nsmode-11 requires schema-aware processing
        // K2-DirectConElemContent-35 Mismatch: true is not false // requires typed elements
        // K2-ComputeConElem-9 Mismatch: true is not false // requires typed elements
        // These two I don't understand what's wrong with the generated expression - maybe a Saxon bug?
        // K2-sequenceExprTypeswitch-14: The context item for axis step self::node() is undefined 
        // K2-ExternalVariablesWithout-11: The context item is absent, so position() is undefined
        // K2-SeqExprCast-209 Mismatch: 〜 is not &#12316; // count as pass
        
        //<e/>/(typeswitch (self::node())
        //        case $i as node() return .
        // becomes:
        //        declare namespace zz="http://saxon.sf.net/";
        //        (<e >{() }</e>)/(let $zz:zz_typeswitchVar := self::node() return if ($zz:zz_typeswitchVar instance of node()) then (.) else (1))
        //assertTrue (runTest ("K2-sequenceExprTypeswitch-14"));
        assertTrue (runTest ("fn-id-dtd-5"));
    }
    
    @Test public void testGroup () throws Exception {
        terminateOnException = false;
        runTestGroup ("MinimalConformance");
        runTestGroup ("FunctX");
        //runTestGroup ("Basics");
        //runTestGroup ("Expressions");
    }
    
    /*
     * Run test cases, replacing the context item with collection().
     * Compare results and timing using Lux with results and timing using Saxon
     * alone fetching files from the file system.
     * 
     * To do this, we need to:
     * 1. Change the processing of context items when we load the tests to bind external variables to collection()
     * 2. Change the test runner so it compares results from Lux and Saxon (not from XQTS)
     * 2a. the test runner should skip tests that throw errors, and those that use the emptydoc as context
     * 3. Change setup() so it actually indexes the content and optimizes the queries
     * 
     * We also need to update Lux so that it optimizes (specifically) the use of collection() via a variable.
     */
    @Test public void testBenchmark () throws Exception {
        
    }
    
}

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
