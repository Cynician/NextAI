package com.android.nextai.ui.component.markdown.parser.ext.math.node;

import com.vladsch.flexmark.ast.CodeBlock;
import com.vladsch.flexmark.util.ast.BlockContent;
import com.vladsch.flexmark.util.sequence.BasedSequence;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Node representing display mode math formula like the following
 *
 * <pre>
 *     $$\sum_{i=1}^{n}=\frac{n(n+1)}{2}$$
 * </pre>
 *
 */
public class MathFormulaBlockNode extends CodeBlock {
	
	private BasedSequence startMarker;
    private BasedSequence endMarker;
    
    public MathFormulaBlockNode() {
    }

    public MathFormulaBlockNode(BasedSequence chars) {
        super(chars);
    }

    public MathFormulaBlockNode(BasedSequence chars, List<BasedSequence> lineSegments) {
        super(chars, lineSegments);
    }

    public MathFormulaBlockNode(List<BasedSequence> lineSegments) {
        this(getSpanningChars(lineSegments), lineSegments);
    }

    private static @NotNull BasedSequence getSpanningChars(@NotNull List<BasedSequence> lineSegments) {
        return lineSegments.isEmpty() ? BasedSequence.NULL : lineSegments.get(0).baseSubSequence(
                lineSegments.get(0).getStartOffset(),
                lineSegments.get(lineSegments.size() - 1).getEndOffset());
    }

    public MathFormulaBlockNode(BlockContent blockContent) {
        super(blockContent);
    }
    
    public BasedSequence getStartMarker() {
        return startMarker;
    }

    public void setStartMarker(BasedSequence startMarker) {
        this.startMarker = startMarker;
    }

    public BasedSequence getEndMarker() {
        return endMarker;
    }

    public void setEndMarker(BasedSequence endMarker) {
        this.endMarker = endMarker;
    }

}
