package com.duy.pascal.interperter.ast.runtime.value.boxing;

import android.support.annotation.NonNull;

import com.duy.pascal.interperter.ast.codeunit.RuntimeExecutableCodeUnit;
import com.duy.pascal.interperter.ast.expressioncontext.CompileTimeContext;
import com.duy.pascal.interperter.ast.expressioncontext.ExpressionContext;
import com.duy.pascal.interperter.ast.variablecontext.VariableContext;
import com.duy.pascal.interperter.ast.runtime.value.RuntimeValue;
import com.duy.pascal.interperter.debugable.DebuggableReturnValue;
import com.duy.pascal.interperter.linenumber.LineNumber;
import com.duy.pascal.interperter.exceptions.parsing.ParsingException;
import com.duy.pascal.interperter.exceptions.runtime.RuntimePascalException;
import com.duy.pascal.interperter.declaration.lang.types.ArgumentType;
import com.duy.pascal.interperter.declaration.lang.types.RuntimeType;

import java.lang.reflect.Array;
import java.util.Arrays;

public class ArrayBoxer extends DebuggableReturnValue {
    public RuntimeValue[] values;
    public ArgumentType type;
    public LineNumber line;

    public ArrayBoxer(RuntimeValue[] array, ArgumentType elementType,
                      LineNumber line) {
        this.values = array;
        this.type = elementType;
        this.line = line;
    }


    public RuntimeValue[] getValues() {
        return values;
    }

    @NonNull
    @Override
    public LineNumber getLineNumber() {
        return line;
    }

    @Override
    public void setLineNumber(LineNumber lineNumber) {

    }

    @NonNull
    @Override
    public RuntimeType getRuntimeType(ExpressionContext context) throws Exception {
        throw new ParsingException(
                line,
                "Attempted to get type of varargs boxer. This should not happen as" +
                        " we are only supposed to pass varargs to plugins");
    }

    @Override
    public boolean canDebug() {
        return false;
    }

    @Override
    public Object getValueImpl(VariableContext f, RuntimeExecutableCodeUnit<?> main)
            throws RuntimePascalException {
        Object[] result = (Object[]) Array.newInstance(type.getRuntimeClass(),
                values.length);
        for (int i = 0; i < values.length; i++) {
            result[i] = values[i].getValue(f, main);
        }
        return result;
    }

    @Override
    public Object compileTimeValue(CompileTimeContext context)
            throws Exception {
        Object[] result = (Object[]) Array.newInstance(type.getRuntimeClass(), values.length);
        for (int i = 0; i < values.length; i++) {
            Object val = values[i].compileTimeValue(context);
            if (val == null) {
                return null;
            } else {
                result[i] = val;
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return Arrays.toString(values);
    }

    @Override
    public RuntimeValue compileTimeExpressionFold(CompileTimeContext context) throws Exception {
        RuntimeValue[] val = new RuntimeValue[values.length];
        for (int i = 0; i < values.length; i++) {
            val[i] = values[i].compileTimeExpressionFold(context);
        }
        return new ArrayBoxer(val, type, line);
    }
}
