package lux.query.parser;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.ext.ExtensionQuery;
import org.apache.lucene.queryParser.ext.ParserExtension;

class NodeParser extends ParserExtension {
    
    private final String textFieldName;
    private final String elementTextFieldName;
    private final String attributeTextFieldName;
    QNameQueryBuilder queryBuilder;
    
    NodeParser (String textFieldName, String elementTextFieldName, String attributeTextFieldName, Analyzer a) {
        queryBuilder = new QNameQueryBuilder(a);
        this.textFieldName = textFieldName;
        this.elementTextFieldName = elementTextFieldName;
        this.attributeTextFieldName = attributeTextFieldName;
    }

    @Override
    public org.apache.lucene.search.Query parse(ExtensionQuery query) throws ParseException {
        String field = query.getField();
        String term = query.getRawQueryString();
        // create either a term query or a phrase query (or a span?)
        if (StringUtils.isEmpty(field)) {
            return queryBuilder.parseQueryTerm(textFieldName, field, term, 1.0f);
        } else if (field.charAt(0) == '@') {
            return queryBuilder.parseQueryTerm(attributeTextFieldName, field.substring(1), term, 1.0f);
        } else {
            return queryBuilder.parseQueryTerm(elementTextFieldName, field, term, 1.0f);
        }
    }
}