/*
 *  Copyright (c) 2017 Tran Le Duy
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

package com.duy.pascal.ui.autocomplete.completion;

import java.util.regex.Pattern;

/**
 * Created by Duy on 11-Feb-17.
 */

public class Patterns {
    /**
     * match reserved keyword
     */
    public static final Pattern KEYWORDS = Pattern.compile(
            "(\\b)(uses|const|do|for|while|if|else|in|case|and|array|begin|div" +
                    "|downto|to|mod|of" +
                    "|procedure|program|repeat|until|shl|shr" +
                    "|then|type|var|end|function" +
                    "|true|false" +
                    "|and|or|xor|not|break|exit" +
                    "|integer|byte|word|shortint|smallint|cardinal" +
                    "|string|ansistring" +
                    "|single|real|extended|comp|curreny" +
                    "|longint|int64|qword|longword|dword" +
                    "|boolean" +
                    "|char|text" +
                    "|record|continue" +
                    "|unit|interface|initialization|finalization|implementation|with" +
                    "|null|nil|set|new)(\\b)",
            Pattern.CASE_INSENSITIVE);

    public static final Pattern NON_WORD = Pattern.compile("\\b");

    /**
     * match builtin pascal function
     */
    public static final Pattern BUILTIN_FUNCTIONS = Pattern.compile(
            "(\\b)(sin|cos|sqrt|length" +
                    "|exp|tan|keyPressed|readKey|delay|random|randomize|inc|dec" +
                    "|ceil|trunc|frac|floor|abs|round|sqr|pred|succ|ln|arctan" +
                    "|odd|int|halt|odd)(\\b)", Pattern.CASE_INSENSITIVE);
    /**
     * match some spacial symbol
     */
    public static final Pattern SYMBOLS = Pattern.compile("[+\\-'*=<>/:)(\\]\\[;@\\^,.]");

    /**
     * match number
     */
    public static final Pattern NUMBERS = Pattern.compile(
            "\\b((\\d*[.]?\\d+([Ee][+-]?[\\d]+)?)|" + //simple decimal
                    "(\\$[0-9a-fA-F]+)|" + //hex
                    "(%[01]+)|" + //binary
                    "(&[0-7]+)|" +//octal
                    "([Ee][+-]?[\\d]+))\\b");

    public static final Pattern HEX_COLOR = Pattern.compile("(#[0-9a-fA-F]{6})");
    public static final Pattern RGB_FUNCTION = Pattern.compile(
            "([Rr][Gg][Bb])" + //1
                    "(\\()" +//2
                    "(\\s?\\d+\\s?)" +//3
                    "(,)" +//4
                    "(\\s?\\d+\\s?)" +//5
                    "(,)" +//6
                    "(\\s?\\d+\\s?)" +//7
                    "(\\))");
    public static final Pattern ARGB_FUNCTION = Pattern.compile(
            "([Aa][Rr][Gg][Bb])" +
                    "(\\()" +
                    "(\\s?\\d+\\s?)" +
                    "(,)" +
                    "(\\s?\\d+\\s?)" +
                    "(,)" +
                    "(\\s?\\d+\\s?)" +
                    "(,)" +
                    "(\\s?\\d+\\s?)" +
                    "(\\))");
    public static final Pattern TEXT_COLOR_FUNCTION = Pattern.compile("(textColor)" +
            "(\\()" +
            "([0-9]+)" +
            "(\\))");
    public static final Pattern TEXT_BACKGROUND_FUNCTION = Pattern.compile("(textColor)" +
            "(\\()" +
            "([0-9]+)" +
            "(\\))");

    public static final Pattern IDENTIFIER = Pattern.compile("[a-zA-Z_][A-Za-z0-9_]*");
    public static final Pattern FILE_NAME = Pattern.compile(IDENTIFIER + "(\\." + IDENTIFIER + ")?");
    public static final Pattern END_ASSIGN = Pattern.compile(":=( )*$");
    public static final Pattern ID_ASSIGN = Pattern.compile("(" + IDENTIFIER + ")(" + END_ASSIGN + ")");

}
