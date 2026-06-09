package com.android.nextai.ui.component.markdown.parser.ext.math.parser;

import com.android.nextai.ui.component.markdown.parser.ext.math.node.MathFormulaInLineNode;
import com.vladsch.flexmark.parser.InlineParser;
import com.vladsch.flexmark.parser.core.delimiter.Delimiter;
import com.vladsch.flexmark.parser.delimiter.DelimiterProcessor;
import com.vladsch.flexmark.parser.delimiter.DelimiterRun;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MathFormulaInLineNodeParser implements DelimiterProcessor {

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
        // 1. Basic interception: $ must not be followed by a space
        if (Character.isWhitespace(nextChar)) return false;

        // 2. Pure digital interception (prevents $100 misjudgments, maintains previous optimization)
        if (Character.isDigit(nextChar)) {
            Matcher matcher = PURE_NUMBER_PATTERN.matcher(after);
            if (matcher.find()) return false;
        }
        return leftFlanking;
    }

    @Override
    public boolean canBeCloser(String before, String after, boolean leftFlanking, boolean rightFlanking,
                               boolean beforeIsPunctuation, boolean afterIsPunctuation, boolean beforeIsWhitespace,
                               boolean afterIsWhiteSpace) {
        if (before == null || before.isEmpty()) return false;

        char prevChar = before.charAt(before.length() - 1);
        // Basic interception: $ must never be a space before it
        if (Character.isWhitespace(prevChar)) return false;

        return rightFlanking;
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
