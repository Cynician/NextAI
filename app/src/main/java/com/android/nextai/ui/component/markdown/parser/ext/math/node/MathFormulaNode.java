package com.android.nextai.ui.component.markdown.parser.ext.math.node;

import com.vladsch.flexmark.util.ast.DelimitedNode;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;

import org.jetbrains.annotations.NotNull;


public class MathFormulaNode extends Node implements DelimitedNode {

    protected BasedSequence openingMarker = BasedSequence.NULL;
    protected BasedSequence text = BasedSequence.NULL;
    protected BasedSequence closingMarker = BasedSequence.NULL;
    /**
     * Classification:
     * <p>
     * $$...$$(DisplayMode)-> Not inline formula
     * <p>
     * $...$ -> Inline formula
     */
    private  Boolean isDisplayMode;

    public MathFormulaNode() {
    }

    public MathFormulaNode(BasedSequence chars) {
        super(chars);
    }

    public MathFormulaNode(BasedSequence openingMarker, BasedSequence text, BasedSequence closingMarker, Boolean isDisplayMode) {
        super(openingMarker.baseSubSequence(openingMarker.getStartOffset(), closingMarker.getEndOffset()));
        this.openingMarker = openingMarker;
        this.text = text;
        this.closingMarker = closingMarker;
        this.isDisplayMode = isDisplayMode;
    }

    public boolean isDisplayMode() {
        return isDisplayMode;
    }

    @Override
    public @NotNull BasedSequence[] getSegments() {
        return new BasedSequence[] { openingMarker, text, closingMarker };
    }
    
    @Override
    public void getAstExtra(@NotNull StringBuilder out) {
        delimitedSegmentSpanChars(out, openingMarker, text, closingMarker, "text");
    }

    public BasedSequence getOpeningMarker() {
        return openingMarker;
    }

    public void setOpeningMarker(BasedSequence openingMarker) {
        this.openingMarker = openingMarker;
    }

    public BasedSequence getText() {
        return text;
    }

    public void setText(BasedSequence text) {
        this.text = text;
    }

    public BasedSequence getClosingMarker() {
        return closingMarker;
    }

    public void setClosingMarker(BasedSequence closingMarker) {
        this.closingMarker = closingMarker;
    }

}
