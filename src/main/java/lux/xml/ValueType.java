package lux.xml;

/** These types correspond roughly to the XDM / XML Schema types
 */
public enum ValueType {
    
    VALUE("item", false, false), 
    DOCUMENT("document-node"), 
    NODE("node"), 
    ELEMENT("element"), 
    ATTRIBUTE("attribute"), 
    TEXT("text"), 
    COMMENT("comment"), 
    PROCESSING_INSTRUCTION("processing-instruction"),
        
    ATOMIC("xs:anyAtomicType", true), 
    STRING("xs:string", true), 
    INT("xs:int", true, false, true), 
    INTEGER("xs:integer", true, false, true), 
    BOOLEAN("xs:boolean", true), 
    DATE("xs:date", true), 
    DATE_TIME("xs:dateTime", true),
    DAY("xs:gDay", true),
    DAY_TIME_DURATION("xs:dayTimeDuration", true),
    MONTH_DAY("xs:gMonthDay", true),
    YEAR("xs:gYear", true),
    YEAR_MONTH("xs:gYearMonth", true),    
    YEAR_MONTH_DURATION("xs:YearMonthDuration", true),
    FLOAT("xs:float", true, false, true), 
    DOUBLE("xs:double", true, false, true), 
    DECIMAL("xs:decimal", true, false, true), 
    TIME("xs:time", true), 
    HEX_BINARY("xs:hexBinary", true), 
    BASE64_BINARY("xs:base64Binary", true), 
    MONTH("xs:gMonth", true), 
    UNTYPED_ATOMIC("xs:untypedAtomic", true),
    QNAME("xs:QName", true), 
    EMPTY("empty-sequence", false, false);
    
    final public static int EXACTLY_ZERO = 0;
    final public static int EXACTLY_ONE = 1;
    final public static int ZERO_OR_ONE = 2;    // ?
    final public static int ONE_OR_MORE = 3;    // +
    final public static int ANY_NUMBER = 4;     // *
    
    final public static String[] CARDINALITY_MARKER = {
            "", "", "?", "+", "*"
    };

    public final boolean isNode;
    public final boolean isAtomic;
    public final boolean isNumeric;
    public final String name;

    ValueType(String nodeTest) {
    	this (nodeTest, false, true, false);
    }

    ValueType(String typeName, boolean isAtomic) {
    	this (typeName, isAtomic, false, false);
    }
    
    ValueType(String typeName, boolean isAtomic, boolean isNode) {
    	this (typeName, isAtomic, isNode, false);
    }

    ValueType(String typeName, boolean isAtomic, boolean isNode, boolean isNumeric) {
        this.isAtomic = isAtomic;        
        this.isNode = isNode;
        this.isNumeric = isNumeric;
        name = typeName;
    }

    /**
     * @param other another type
     * @return whether this type is a subtype of the other
     */
    public boolean is (ValueType other) {
        if (this == other)
            return true;
        if (other == VALUE)
            return true;
        if (this.isAtomic)
            return other == ATOMIC;
        if (this.isNode)
            return other == NODE;            
        return false;
    }

    public ValueType restrict(ValueType type) {
        if (this.is (type)) {
            return this;
        }
        if (type.is(this)) {
            return type;
        }
        if (type.isNode && this.isNode) {
            return NODE;
        }
        if (type.isAtomic && this.isAtomic) {
            return ATOMIC;
        }
        return VALUE;
    }
    
    /**
     * @return the most specific type that includes both this and the other type.
     * @param type the other type
     */
    public ValueType promote(ValueType type) {
        if (this == type)
            return this;
        if (isNode && type.isNode)
            return ValueType.NODE;
        if (isAtomic && type.isAtomic)
            return ValueType.ATOMIC;
        return ValueType.VALUE;
    }
    
    @Override
    public String toString () {
        // Any type qualifications is lost (like element(foo) - they become just element())
        if (!isAtomic) {
            return name + "()";
        }
        return name;
    }

    public String toString (QName qName) {
        if (!isAtomic) {
            return name + '(' + (qName != null ? qName.toString()  : "") + ')';
        }
        return name;
    }

}

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
