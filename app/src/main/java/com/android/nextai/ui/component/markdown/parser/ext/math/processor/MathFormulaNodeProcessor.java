package com.android.nextai.ui.component.markdown.parser.ext.math.processor;

import com.android.nextai.ui.component.markdown.parser.ext.math.node.MathFormulaNode;
import com.vladsch.flexmark.parser.InlineParser;
import com.vladsch.flexmark.parser.core.delimiter.Delimiter;
import com.vladsch.flexmark.parser.delimiter.DelimiterProcessor;
import com.vladsch.flexmark.parser.delimiter.DelimiterRun;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MathFormulaNodeProcessor implements DelimiterProcessor {

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

    /**
     * Regex: If the sequence is a number followed by only one $, intercept it (excluding the $100
     * case, but not displayMode like $$100).
     */
    private static final Pattern PURE_NUMBER_PATTERN = Pattern.compile("^\\d+(\\.\\d+)?(?=\\s|$)");

    @Override
    public boolean canBeOpener(String before, String after, boolean leftFlanking, boolean rightFlanking,
                               boolean beforeIsPunctuation, boolean afterIsPunctuation, boolean beforeIsWhitespace,
                               boolean afterIsWhiteSpace) {
        if (after.isEmpty()) return false;

        // Decide whether now are handling $$ or $.
        boolean isDisplayModeAttempt = after.startsWith("$");

        if (!isDisplayModeAttempt) {
            char nextChar = after.charAt(0);
            // Intercept ordinary pure numbers (e.g., $100).
            if (Character.isDigit(nextChar)) {
                Matcher matcher = PURE_NUMBER_PATTERN.matcher(after);
                return !matcher.find();
            }
        } else {
            // If it's $$, make sure there is still content after it, it can't be empty or purely $$
            // followed by a line break that crosses the line.
            return after.length() != 1;
        }
        return true;
    }

    @Override
    public boolean canBeCloser(String before, String after, boolean leftFlanking, boolean rightFlanking,
                               boolean beforeIsPunctuation, boolean afterIsPunctuation, boolean beforeIsWhitespace,
                               boolean afterIsWhiteSpace) {
        return !before.isEmpty();
    }

    @Override
    public boolean skipNonOpenerCloser() {
        return true;
    }

    @Override
    public int getDelimiterUse(DelimiterRun opener, DelimiterRun closer) {
        // Core logic: The length of the open/close interval must match. $$ must be paired with $$,
        // $ must be paired with $.
        if (opener.length() >= 2 && closer.length() >= 2) {
            return 2; //  $$...$$
        } else if (opener.length() == 1 && closer.length() == 1) {
            return 1; //  $...$
        }
        return 0; // If the length does not match, it will not be parsed.
    }

    @Override
    public Node unmatchedDelimiterNode(InlineParser inlineParser, DelimiterRun delimiter) {
        return null;
    }

    @Override
    public void process(Delimiter opener, Delimiter closer, int delimitersUsed) {
        // $$ -> DisplayMode
        boolean isDisplayMode = (delimitersUsed == 2);

        BasedSequence text = opener.getInput().subSequence(opener.getEndIndex(), closer.getStartIndex());

        MathFormulaNode formula = new MathFormulaNode(
                opener.getTailChars(delimitersUsed),
                text,
                closer.getLeadChars(delimitersUsed),
                isDisplayMode
        );

        opener.moveNodesBetweenDelimitersTo(formula, closer);
    }
}