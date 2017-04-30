/*
 *  Copyright 2017 Tran Le Duy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duy.pascal.backend.lib.templated.sizeof;

import com.duy.pascal.backend.exceptions.ParsingException;
import com.duy.pascal.backend.exceptions.UnAssignableTypeException;
import com.duy.pascal.backend.linenumber.LineInfo;
import com.duy.pascal.backend.pascaltypes.BasicType;
import com.duy.pascal.backend.pascaltypes.RuntimeType;
import com.js.interpreter.ast.expressioncontext.CompileTimeContext;
import com.js.interpreter.ast.expressioncontext.ExpressionContext;
import com.js.interpreter.ast.instructions.Executable;
import com.js.interpreter.ast.instructions.SetValueExecutable;
import com.js.interpreter.ast.returnsvalue.FunctionCall;
import com.js.interpreter.ast.returnsvalue.ReturnsValue;
import com.js.interpreter.runtime.VariableContext;
import com.js.interpreter.runtime.codeunit.RuntimeExecutable;
import com.js.interpreter.runtime.exception.RuntimePascalException;

class SizeOfObjectCall extends FunctionCall {

    private LineInfo line;
    private ReturnsValue array;

    SizeOfObjectCall(ReturnsValue array, LineInfo line) {
        this.array = array;
        this.line = line;
    }

    @Override
    public RuntimeType getType(ExpressionContext f) throws ParsingException {
        return new RuntimeType(BasicType.Integer, false);
    }

    @Override
    public LineInfo getLine() {
        return line;
    }

    @Override
    public SetValueExecutable createSetValueInstruction(ReturnsValue r)
            throws UnAssignableTypeException {
        throw new UnAssignableTypeException(this);
    }

    @Override
    public Object compileTimeValue(CompileTimeContext context) {
        return null;
    }

    @Override
    public ReturnsValue compileTimeExpressionFold(CompileTimeContext context)
            throws ParsingException {
        return new SizeOfObjectCall(array.compileTimeExpressionFold(context), line);
    }

    @Override
    public Executable compileTimeConstantTransform(CompileTimeContext c)
            throws ParsingException {
        return new SizeOfObjectCall(array.compileTimeExpressionFold(c), line);
    }

    @Override
    protected String getFunctionName() {
        return "sizeof";
    }

    @Override
    public Object getValueImpl(VariableContext f, RuntimeExecutable<?> main)
            throws RuntimePascalException {
        Object value = array.getValue(f, main);
        if (value instanceof Integer) {
            return 4;
        } else if (value instanceof Long) {
            return 8;
        } else if (value instanceof Double) {
            return 8;
        } else if (value instanceof Short) {
            return 1;
        } else if (value instanceof Byte) {
            return 1;
        } else if (value instanceof Character) {
            return 2;
        } else if (value instanceof String) {
            return ((String) value).length() + 1;
        } else if (value instanceof StringBuilder) {
            return ((StringBuilder) value).length() + 1;
        }
        return 0;
    }
}