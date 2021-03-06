package lux.xquery;

import lux.xpath.AbstractExpression;
import lux.xpath.ExpressionVisitor;

public class Satisfies extends AbstractExpression {
    
    private final Quantifier quantifier;
    private final Variable var;
    
    public Satisfies (Quantifier quantifier, Variable var, AbstractExpression sequence, AbstractExpression condition) {
        super (Type.SATISFIES);
        this.quantifier = quantifier;
        this.var = var;
        subs = new AbstractExpression[] { sequence, condition };
    }
    
    public enum Quantifier {
        SOME, EVERY
    }

    @Override
    public AbstractExpression accept(ExpressionVisitor visitor) {
        super.acceptSubs (visitor);
        return visitor.visit (this);
    }

    @Override
    public void toString(StringBuilder buf) {
        buf.append (quantifier.toString().toLowerCase()).append(' ');
        var.toString(buf);
        buf.append (" in ");
        subs[0].toString (buf);
        buf.append (" satisfies ");
        subs[1].toString (buf);
    }

    @Override
    public int getPrecedence() {
        return 0;
    }

}
