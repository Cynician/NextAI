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
    
//    @Override
//    public boolean canBeOpener(String before, String after, boolean leftFlanking, boolean rightFlanking,
//            boolean beforeIsPunctuation, boolean afterIsPunctuation, boolean beforeIsWhitespace,
//            boolean afterIsWhiteSpace) {
//        return leftFlanking && beforeIsWhitespace;
//    }
//
//    @Override
//    public boolean canBeCloser(String before, String after, boolean leftFlanking, boolean rightFlanking,
//            boolean beforeIsPunctuation, boolean afterIsPunctuation, boolean beforeIsWhitespace,
//            boolean afterIsWhiteSpace) {
//        return rightFlanking && afterIsWhiteSpace;
//    }


    @Override
    public boolean canBeOpener(String before, String after, boolean leftFlanking, boolean rightFlanking,
                               boolean beforeIsPunctuation, boolean afterIsPunctuation, boolean beforeIsWhitespace,
                               boolean afterIsWhiteSpace) {
        // 开头判定：$ 后面绝对不能是空格或换行，且不能是数字（防止 $100 误判）
        if (after == null || after.isEmpty()) return false;
        char nextChar = after.charAt(0);
        if (Character.isWhitespace(nextChar) || Character.isDigit(nextChar)) {
            return false;
        }
        return true; // 只要后面有实质的数学公式内容，就允许作为 Opener
    }

    @Override
    public boolean canBeCloser(String before, String after, boolean leftFlanking, boolean rightFlanking,
                               boolean beforeIsPunctuation, boolean afterIsPunctuation, boolean beforeIsWhitespace,
                               boolean afterIsWhiteSpace) {
        // 结尾判定：$ 前面绝对不能是空格或换行（公式内部末尾不可能以空格结尾）
        if (before == null || before.isEmpty()) return false;
        char prevChar = before.charAt(before.length() - 1);
        if (Character.isWhitespace(prevChar)) {
            return false;
        }
        return true; // 只要公式身体正常结束，不管后面跟着句号、空格还是换行，都允许作为 Closer
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
