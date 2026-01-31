package com.financial.rules.exception;

public class ExpressionParseException extends RulesException {
    
    private final String expression;
    private final int position;
    private final String expected;
    
    public ExpressionParseException(String message, String expression) {
        super(message, "EXPRESSION_PARSE_ERROR");
        this.expression = expression;
        this.position = -1;
        this.expected = null;
    }
    
    public ExpressionParseException(String message, String expression, int position) {
        super(message, "EXPRESSION_PARSE_ERROR");
        this.expression = expression;
        this.position = position;
        this.expected = null;
    }
    
    public ExpressionParseException(String message, String expression, int position, String expected) {
        super(message, "EXPRESSION_PARSE_ERROR");
        this.expression = expression;
        this.position = position;
        this.expected = expected;
    }
    
    public String getExpression() {
        return expression;
    }
    
    public int getPosition() {
        return position;
    }
    
    public String getExpected() {
        return expected;
    }
}
