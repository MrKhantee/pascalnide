package com.duy.pascal.interperter.ast.runtime.value.boxing;

import android.support.annotation.NonNull;

import com.duy.pascal.interperter.ast.codeunit.RuntimeExecutableCodeUnit;
import com.duy.pascal.interperter.ast.expressioncontext.CompileTimeContext;
import com.duy.pascal.interperter.ast.expressioncontext.ExpressionContext;
import com.duy.pascal.interperter.ast.variablecontext.VariableContext;
import com.duy.pascal.interperter.ast.runtime.value.AssignableValue;
import com.duy.pascal.interperter.ast.runtime.value.RuntimeValue;
import com.duy.pascal.interperter.debugable.DebuggableReturnValue;
import com.duy.pascal.interperter.linenumber.LineInfo;
import com.duy.pascal.interperter.exceptions.parsing.value.UnAssignableTypeException;
import com.duy.pascal.interperter.exceptions.runtime.RuntimePascalException;
import com.duy.pascal.interperter.declaration.lang.types.PointerType;
import com.duy.pascal.interperter.declaration.lang.types.RuntimeType;

public class GetAddress extends DebuggableReturnValue {
    private final AssignableValue target;
    private LineInfo line;

    public GetAddress(AssignableValue target) throws UnAssignableTypeException {
        this.line = target.getLineNumber();
        this.target = target;
    }


    @Override
    public RuntimeType getRuntimeType(ExpressionContext exprContext) throws Exception {
        return new RuntimeType(new PointerType(target.getRuntimeType(exprContext).declType),
                false);
    }

    @Override
    public String toString() {
        return "@" + target;
    }

    @NonNull
    @Override
    public LineInfo getLineNumber() {
        return target.getLineNumber();
    }

    @Override
    public void setLineNumber(LineInfo lineNumber) {

    }

    @Override
    public Object compileTimeValue(CompileTimeContext context)
            throws Exception {
        return null;
    }

    @Override
    public boolean canDebug() {
        return false;
    }

    @Override
    public Object getValueImpl(VariableContext f, RuntimeExecutableCodeUnit<?> main)
            throws RuntimePascalException {
        return target.getReference(f, main);
    }

    @Override
    public RuntimeValue compileTimeExpressionFold(CompileTimeContext context)
            throws Exception {
        return this;
    }
}
