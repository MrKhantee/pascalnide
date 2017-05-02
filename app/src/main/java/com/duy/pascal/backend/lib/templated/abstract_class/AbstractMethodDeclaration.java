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

package com.duy.pascal.backend.lib.templated.abstract_class;


import com.duy.pascal.backend.exceptions.ParsingException;
import com.duy.pascal.backend.linenumber.LineInfo;
import com.duy.pascal.backend.pascaltypes.ArgumentType;
import com.duy.pascal.backend.pascaltypes.DeclaredType;
import com.js.interpreter.ast.AbstractFunction;
import com.js.interpreter.ast.expressioncontext.ExpressionContext;
import com.js.interpreter.ast.returnsvalue.FunctionCall;
import com.js.interpreter.ast.returnsvalue.RValue;

import java.util.List;

public class AbstractMethodDeclaration extends AbstractFunction {

    private IMethodDeclaration t;

    public AbstractMethodDeclaration(IMethodDeclaration t) {
        this.t = t;
    }

    @Override
    public LineInfo getLineNumber() {
        return new LineInfo(-1, t.getClass().getCanonicalName());
    }

    @Override
    public String getEntityType() {
        return "Template Plugin";
    }

    @Override
    public String name() {
        return t.name();
    }

    @Override
    public String description() {
        return null;
    }

    @Override
    public ArgumentType[] argumentTypes() {
        return t.argumentTypes();
    }

    @Override
    public DeclaredType return_type() {
        return t.returnType();
    }

    @Override
    public FunctionCall generatePerfectFitCall(LineInfo line,
                                               List<RValue> values, ExpressionContext f)
            throws ParsingException {
        RValue[] args = this.perfectMatch(values, f);
        if (args == null) {
            return null;
        }
        return t.generatePerfectFitCall(line, args, f);
    }

    @Override
    public FunctionCall generateCall(LineInfo line, List<RValue> values,
                                     ExpressionContext f) throws ParsingException {
        RValue[] args = this.formatArgs(values, f);
        if (args == null) {
            return null;
        }
        return t.generateCall(line, args, f);
    }

}