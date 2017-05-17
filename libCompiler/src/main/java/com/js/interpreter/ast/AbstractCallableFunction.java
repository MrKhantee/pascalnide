package com.js.interpreter.ast;

import com.duy.pascal.backend.exceptions.ParsingException;
import com.duy.pascal.backend.linenumber.LineInfo;
import com.js.interpreter.ast.expressioncontext.ExpressionContext;
import com.js.interpreter.ast.returnsvalue.FunctionCall;
import com.js.interpreter.ast.returnsvalue.ReturnValue;
import com.js.interpreter.ast.returnsvalue.SimpleFunctionCall;
import com.js.interpreter.runtime.VariableContext;
import com.js.interpreter.runtime.codeunit.RuntimeExecutable;
import com.js.interpreter.runtime.exception.RuntimePascalException;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public abstract class AbstractCallableFunction extends AbstractFunction {

    /**
     * This invokes a function call of any operator.
     *
     * @param parentContext The program context.
     * @return The return value of the called function.
     */
    public abstract Object call(VariableContext parentContext,
                                RuntimeExecutable<?> main, Object[] arguments)
            throws RuntimePascalException, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException;

    @Override
    public FunctionCall generatePerfectFitCall(LineInfo line,
                                               List<ReturnValue> values, ExpressionContext f)
            throws ParsingException {
        ReturnValue[] args = perfectMatch(values, f);
        if (args == null) {
            return null;
        }
        return new SimpleFunctionCall(this, args, line);
    }

    @Override
    public FunctionCall generateCall(LineInfo line, List<ReturnValue> values,
                                     ExpressionContext f) throws ParsingException {
        ReturnValue[] args = formatArgs(values, f);
        if (args == null) {
            return null;
        }
        return new SimpleFunctionCall(this, args, line);
    }


    @Override
    public String description() {
        return null;
    }
}