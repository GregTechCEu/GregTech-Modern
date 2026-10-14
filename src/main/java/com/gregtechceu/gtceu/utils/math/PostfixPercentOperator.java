package com.gregtechceu.gtceu.utils.math;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.operators.AbstractOperator;
import com.ezylang.evalex.operators.OperatorIfc;
import com.ezylang.evalex.operators.PostfixOperator;
import com.ezylang.evalex.parser.Token;

import java.math.BigDecimal;

@PostfixOperator(precedence = OperatorIfc.OPERATOR_PRECEDENCE_MULTIPLICATIVE - 1)
public class PostfixPercentOperator extends AbstractOperator {

    public static final BigDecimal HUNDRED = new BigDecimal(100);

    @Override
    public EvaluationValue evaluate(Expression expression, Token operatorToken,
                                    EvaluationValue... operands) throws EvaluationException {
        EvaluationValue operand = operands[0];

        if (operand.isNumberValue()) {
            return expression.convertValue(
                    operand.getNumberValue().divide(HUNDRED, expression.getConfiguration().getMathContext()));
        } else {
            throw EvaluationException.ofUnsupportedDataTypeInOperation(operatorToken);
        }
    }
}
