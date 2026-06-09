package com.android.nextai.ui.component.markdown.parser.ext.math;

import com.android.nextai.ui.component.markdown.parser.ext.math.parser.MathFormulaBlockNodeParser;
import com.android.nextai.ui.component.markdown.parser.ext.math.parser.MathFormulaInLineNodeParser;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataHolder;


public class MathExtension implements Parser.ParserExtension {

    private MathExtension() {
    }

    public static MathExtension create() {
        return new MathExtension();
    }

    @Override
    public void parserOptions(MutableDataHolder options) {

    }


    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.customBlockParserFactory(new MathFormulaBlockNodeParser.Factory());
        parserBuilder.customDelimiterProcessor(new MathFormulaInLineNodeParser());
    }


}
