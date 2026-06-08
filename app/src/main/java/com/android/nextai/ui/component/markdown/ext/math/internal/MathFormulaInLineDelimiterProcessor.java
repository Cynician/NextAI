/*
 * This work is made available under the terms of the BSD 2-Clause "Simplified" License.
 * The BSD accompanies this distribution (LICENSE.txt).
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package com.android.nextai.ui.component.markdown.ext.math.internal;

import com.android.nextai.ui.component.markdown.ext.math.MathFormulaInLineNode;
import com.vladsch.flexmark.parser.InlineParser;
import com.vladsch.flexmark.parser.core.delimiter.Delimiter;
import com.vladsch.flexmark.parser.delimiter.DelimiterProcessor;
import com.vladsch.flexmark.parser.delimiter.DelimiterRun;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MathFormulaInLineDelimiterProcessor implements DelimiterProcessor {

    @Override
    public char getOpeningCharacter() {
        return '$';
    }

    @Override
    public char getClosingCharacter() {
        return '$';
    }

    @Override
    public int getMinLength() {
        return 1;
    }

    private static final Pattern PURE_NUMBER_PATTERN = Pattern.compile("^\\d+(\\.\\d+)?(?=\\$)");

    @Override
    public boolean canBeOpener(String before, String after, boolean leftFlanking, boolean rightFlanking,
                               boolean beforeIsPunctuation, boolean afterIsPunctuation, boolean beforeIsWhitespace,
                               boolean afterIsWhiteSpace) {
        if (after == null || after.isEmpty()) return false;

        char nextChar = after.charAt(0);
        // 1. 基础拦截：$ 后面绝对不能是空格或换行
        if (Character.isWhitespace(nextChar)) {
            return false;
        }

        // 2. 核心改进：如果是数字开头，检查直到下一个 $ 之间是不是纯数字
        if (Character.isDigit(nextChar)) {
            Matcher matcher = PURE_NUMBER_PATTERN.matcher(after);
            if (matcher.find()) {
                // 如果能匹配成功，说明形如 "$100$" 或 "$3.14$"，这是纯金额或数字，拒绝作为公式开头
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean canBeCloser(String before, String after, boolean leftFlanking, boolean rightFlanking,
                               boolean beforeIsPunctuation, boolean afterIsPunctuation, boolean beforeIsWhitespace,
                               boolean afterIsWhiteSpace) {
        // 结尾判定保持原样即可：公式内部末尾不可能以空格结尾
        if (before == null || before.isEmpty()) return false;
        char prevChar = before.charAt(before.length() - 1);
        if (Character.isWhitespace(prevChar)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean skipNonOpenerCloser() {
        return true;
    }

    @Override
    public int getDelimiterUse(DelimiterRun opener, DelimiterRun closer) {
        if (opener.length() == 1 && closer.length() == 1) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public Node unmatchedDelimiterNode(InlineParser inlineParser, DelimiterRun delimiter) {
        return null;
    }

    @Override
    public void process(Delimiter opener, Delimiter closer, int delimitersUsed) {
        BasedSequence text = opener.getInput().subSequence(opener.getEndIndex(), closer.getStartIndex());
        MathFormulaInLineNode formula = new MathFormulaInLineNode(
                opener.getTailChars(delimitersUsed), text, closer.getLeadChars(delimitersUsed));
        opener.moveNodesBetweenDelimitersTo(formula, closer);
    }
    
}
