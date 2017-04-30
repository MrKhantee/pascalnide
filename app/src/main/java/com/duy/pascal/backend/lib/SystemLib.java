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

package com.duy.pascal.backend.lib;

import com.duy.pascal.backend.exceptions.OrdinalExpressionExpectedException;
import com.duy.pascal.backend.lib.annotations.PascalMethod;
import com.duy.pascal.backend.lib.runtime_exceptions.InvalidFloatingPointOperation;
import com.duy.pascal.backend.lib.runtime_exceptions.RangeCheckError;
import com.js.interpreter.runtime.VariableBoxer;
import com.js.interpreter.runtime.exception.RuntimePascalException;
import com.js.interpreter.runtime.exception.ScriptTerminatedException;
import com.js.interpreter.runtime.exception.WrongArgsException;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Random;

/**
 * System lib
 * <p>
 * - key event
 * - key read
 * <p>
 * Created by Duy on 07-Mar-17.
 */
public class SystemLib implements PascalLibrary {


    private Random random = new Random();


    @Override
    public boolean instantiate(Map<String, Object> pluginargs) {
        return false;
    }

    @Override
    public void shutdown() {

    }


    @PascalMethod(description = "system lib")
    public void delay(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }

    @PascalMethod(description = "system lib")
    public int Byte(boolean b) {
        return b ? 1 : 0;
    }

    @PascalMethod(description = "Generate random number")
    public int random(int range) {
        return random.nextInt(range);
    }

    @PascalMethod(description = "Initialize random number generator")
    public void randomize() {
        random = new Random(System.currentTimeMillis());
    }

    @PascalMethod(description = "Increase value of integer variable")
    public void inc(VariableBoxer<Object> boxer) throws RuntimePascalException, WrongArgsException {
        inc(boxer, 1);
    }

    @PascalMethod(description = "Increase value of integer variable")
    public void inc(VariableBoxer<Object> boxer, Object increment) throws RuntimePascalException, WrongArgsException {
        if (boxer.get() instanceof Long) {
            long count;
            if (increment instanceof Integer) {
                count = Long.parseLong(increment.toString());
            } else if (increment instanceof Long) {
                count = (long) increment;
            } else {
                throw new OrdinalExpressionExpectedException();
            }
            boxer.set(((Long) boxer.get()) + count);
        } else if (boxer.get() instanceof Integer) {
            int count;
            if (increment instanceof Long) {
                count = Integer.parseInt(increment.toString());
            } else if (increment instanceof Integer) {
                count = (int) increment;
            } else {
                throw new OrdinalExpressionExpectedException();
            }
            boxer.set(((Integer) boxer.get()) + count);
        } else if (boxer.get() instanceof Character) {
            int count;
            if (increment instanceof Long) {
                count = Integer.parseInt(increment.toString());
            } else if (increment instanceof Integer) {
                count = (int) increment;
            } else {
                throw new OrdinalExpressionExpectedException();
            }

            Character character = (Character) boxer.get();
            Character newChar = (char) (character + count);
            boxer.set(newChar);
        } else if (boxer.get() instanceof Boolean) {
            if ((Boolean) boxer.get()) {
                throw new RangeCheckError(boxer);
            } else {
                boxer.set(true);
            }
        } else {
            //throw exception
            throw new WrongArgsException("inc");
//            boxer.set((long) boxer.get() - 1);
        }
    }

    @PascalMethod(description = "Decrease value of variable")
    public void dec(VariableBoxer<Object> boxer) throws RuntimePascalException, WrongArgsException {
        dec(boxer, 1);
    }

    @PascalMethod(description = "Decrease value of variable")
    public void dec(VariableBoxer<Object> boxer, Object increment) throws RuntimePascalException, WrongArgsException {
        if (boxer.get() instanceof Long) {
            long count;
            if (increment instanceof Integer) {
                count = Long.parseLong(increment.toString());
            } else if (increment instanceof Long) {
                count = (long) increment;
            } else {
                throw new OrdinalExpressionExpectedException();
            }
            boxer.set(((Long) boxer.get()) - count);
        } else if (boxer.get() instanceof Integer) {
            int count;
            if (increment instanceof Long) {
                count = Integer.parseInt(increment.toString());
            } else if (increment instanceof Integer) {
                count = (int) increment;
            } else {
                throw new OrdinalExpressionExpectedException();
            }
            boxer.set(((Integer) boxer.get()) - count);
        } else if (boxer.get() instanceof Character) {
            int count;
            if (increment instanceof Long) {
                count = Integer.parseInt(increment.toString());
            } else if (increment instanceof Integer) {
                count = (int) increment;
            } else {
                throw new OrdinalExpressionExpectedException();
            }

            Character character = (Character) boxer.get();
            Character newChar = (char) (character - count);
            boxer.set(newChar);
        } else if (boxer.get() instanceof Boolean) {
            if (!(Boolean) boxer.get()) {
                throw new RangeCheckError(boxer);
            } else {
                boxer.set(false);
            }
        } else {
            //throw exception
            throw new WrongArgsException("dec");
//            boxer.set((long) boxer.get() - 1);
        }
    }

    @PascalMethod(description = "Truncate a floating point value.")
    public long trunc(double d) {
        return (long) Math.floor(d);
    }

    @PascalMethod(description = "Return fractional part of floating point value.")
    public double frac(double d) {
        return d - ((long) d);
    }

    @PascalMethod(description = "Calculate absolute value")
    public int abs(int d) {
        return Math.abs(d);
    }

    @PascalMethod(description = "Calculate absolute value")
    public long abs(long d) {
        return Math.abs(d);
    }

    @PascalMethod(description = "Calculate absolute value")
    public double abs(double d) {
        return Math.abs(d);
    }

    @PascalMethod(description = "Round floating point value to nearest integer number.")
    public long round(double d) {
        return Math.round(d);
    }

    @PascalMethod(description = "Calculate sine of angle")
    public double sin(double d) {
        return Math.sin(d);
    }

    @PascalMethod(description = "Calculate cosine of angle")
    public double cos(double d) {
        return Math.cos(d);
    }

    @PascalMethod(description = "Calculate the square of a value.")
    public int sqr(int d) {
        return d * d;
    }

    @PascalMethod(description = "Calculate the square of a value.")
    public long sqr(long d) {
        return d * d;
    }

    @PascalMethod(description = "Calculate the square of a value.")
    public double sqr(double d) {
        return d * d;
    }

    @PascalMethod(description = "Calculate the square root of a value")
    public double sqrt(double d) throws InvalidFloatingPointOperation {
        if (d < 0) {
            throw new InvalidFloatingPointOperation(d);
        }
        return Math.sqrt(d);
    }

    @PascalMethod(description = "Return previous element for an ordinal type")
    public int pred(int d) {
        return d - 1;
    }

    @PascalMethod(description = "system lib")
    public int succ(int d) {
        return d + 1;
    }

    /**
     * logarithm function
     */
    @PascalMethod(description = "system lib")
    public double ln(double d) throws InvalidFloatingPointOperation {
        if (d < 0) {
            throw new InvalidFloatingPointOperation(d);
        }
        return Math.log(d);
    }

    @PascalMethod(description = "Calculate inverse tangent")
    public double arctan(double a) {
        return Math.atan(a);
    }

    @PascalMethod(description = "Exponentiate")
    public double exp(double a) {
        return Math.exp(a);
    }


    @PascalMethod(description = "Calculate integer part of floating point value.")
    public int Int(double x) {
        return (int) x;
    }

    @PascalMethod(description = "Is a value odd or even ?")
    public boolean odd(long x) {
        return x % 2 == 0;
    }

    @PascalMethod(description = "Stop program execution")
    public void halt(long value) throws ScriptTerminatedException {
        throw new ScriptTerminatedException(null);
    }

    @PascalMethod(description = "")
    public void halt() throws ScriptTerminatedException {
        throw new ScriptTerminatedException(null);
    }

    @PascalMethod(description = "Is a value odd or even ?")
    public boolean odd(int i) {
        return i % 2 == 1;
    }

    @PascalMethod(description = "Convert ascii to character")
    public char chr(int i) {
        return (char) i;
    }


    @PascalMethod(description = "Convert character to ascii code")
    public int ord(char c) {
        return (int) c;
    }


    @PascalMethod(description = "Convert a numerical or enumeration value to a string")
    public void str(Object num, VariableBoxer<StringBuilder> s) {
        s.set(new StringBuilder(String.valueOf(num)));
    }

    @PascalMethod(description = " Calculate numerical/enumerated value of a string.")
    public void val(String input, VariableBoxer<Object> output, VariableBoxer<Integer> resultCode) throws RuntimePascalException, WrongArgsException {
        try {
            input = input.trim(); //remove white space in start and end postion
            if (output.get() instanceof Long) {
                Long l = Long.parseLong(input);
                output.set(l);
                resultCode.set(1);
            } else if (output.get() instanceof Double) {
                Double d = Double.parseDouble(input);
                output.set(d);
                resultCode.set(1);
            } else if (output.get() instanceof Integer) {
                Integer d = Integer.parseInt(input);
                output.set(d);
                resultCode.set(1);
            } else {
                throw new WrongArgsException("Can not call \"val(string, number, result)\"");
            }
        } catch (NumberFormatException e) {
            resultCode.set(-1);
        }
    }

    @PascalMethod(description = "Convert a string to all uppercase.")
    public char upCase(char s) {
        return Character.toUpperCase(s);
    }

    @PascalMethod(description = "Convert a string to all uppercase.")
    public StringBuilder upCase(StringBuilder s) {
        return new StringBuilder(s.toString().toUpperCase());
    }

    @PascalMethod(description = "Append one string to another")
    public StringBuilder concat(StringBuilder... agrs) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Object s1 : agrs) stringBuilder.append(s1);
        return stringBuilder;
    }

    @PascalMethod(description = "Search for substring in a string")
    public int pos(String substring, String s) {
        return s.indexOf(substring) + 1;
    }

    @PascalMethod(description = "Returns length of a string or array.")
    public int length(StringBuilder s) {
        return s.length();
    }

//    @PascalMethod(description = "Returns length of a string or array.")
//    @MethodTypeData(info = {@ArrayBoundsInfo(starts = {0}, lengths = {0})})
//    public int length(Object[] array) {
//        return array.length;
//    }

    @PascalMethod(description = "Set length of a string.")
    public void setlength(VariableBoxer<StringBuilder> s, int length)
            throws RuntimePascalException {
        String filler = "!@#$%";
        StringBuilder old = s.get();
        if (length <= old.length()) {
            s.set(new StringBuilder(old.subSequence(0, length)));
        } else {
            int extra = length - old.length();
            int count = extra / filler.length();
            StringBuilder result = new StringBuilder(length);
            result.append(old);
            for (int i = 0; i < count; i++) {
                result.append(filler);
            }
            int remaining = extra - count * filler.length();
            result.append(filler.subSequence(0, remaining));
            s.set(result);
        }
    }

    @PascalMethod(description = "Insert one string in another.")
    public void insert(String s, VariableBoxer<StringBuilder> s1, int pos)
            throws RuntimePascalException {
        System.out.println("s = [" + s + "], s1 = [" + s1 + "], pos = [" + pos + "]");
        s1.set(new StringBuilder(s1.get().insert(pos - 1, s)));
    }

    @PascalMethod(description = "Copy part of a string")
    public StringBuilder copy(String s, int from, int count) {
        System.out.println("s = [" + s + "], from = [" + from + "], count = [" + count + "]");
        if (from - 1 + count > s.length()) {
            return new StringBuilder(s.substring(from - 1, s.length()));
        }
        return new StringBuilder(s.substring(from - 1, from - 1 + count));
    }

    @PascalMethod(description = "Delete part of a string")
    public void delete(VariableBoxer<StringBuilder> s, int start, int count)
            throws RuntimePascalException {
        System.out.println("s = [" + s + "], start = [" + start + "], count = [" + count + "]");
        s.set(s.get().delete(start - 1, start + count - 1));
    }

    @PascalMethod(description = "Return size of a variable or type.")
    public int sizeof(Object[] array) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(array);
            byte[] bytes = bos.toByteArray();
            return bytes.length;
        } catch (Exception e) {
            return 0;
        }
    }
}
