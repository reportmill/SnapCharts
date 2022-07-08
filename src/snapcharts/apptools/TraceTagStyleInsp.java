/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.apptools;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.text.NumberFormat;
import snap.util.FormatUtils;
import snap.view.*;
import snapcharts.app.ChartPane;
import snapcharts.model.*;
import java.util.Objects;

/**
 * A class to manage UI to edit a TraceStyle.
 */
public class TraceTagStyleInsp extends ChartPartInsp {

    // The TraceInsp
    private TraceInsp  _traceInsp;

    /**
     * Constructor.
     */
    public TraceTagStyleInsp(ChartPane aChartPane, TraceInsp aTraceInsp)
    {
        super(aChartPane);
        _traceInsp = aTraceInsp;
    }

    /**
     * Returns the name.
     */
    @Override
    public String getName()  { return "Trace Tag Style Settings"; }

    /**
     * Returns the ChartPart.
     */
    @Override
    public ChartPart getChartPart()
    {
        return getTagStyle();
    }

    /**
     * Returns the Trace.
     */
    public Trace getTrace()
    {
        ChartPart selPart = _chartPane.getSelChartPart();
        return selPart instanceof Trace ? (Trace) selPart : null;
    }

    /**
     * Returns the TraceStyle.
     */
    public TagStyle getTagStyle()
    {
        Trace trace = getTrace();
        return trace != null ? trace.getTagStyle() : null;
    }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Get Trace, TagStyle
        Trace trace = getTrace(); if (trace == null) return;
        TagStyle tagStyle = trace.getTagStyle();

        // Reset ShowTagsCheckBox
        boolean showTags = trace.isShowTags();
        setViewValue("ShowTagsCheckBox", showTags);

        // Reset TagFontText, TagFontResetButton
        Font tagFont = tagStyle.getFont();
        String fontName = tagFont.getName() + ' ' + FormatUtils.formatNum(tagFont.getSize());
        setViewValue("TagFontText", fontName);
        View tagFontResetButton = getView("TagFontResetButton");
        tagFontResetButton.setPaintable(!Objects.equals(tagFont, TagStyle.DEFAULT_TAG_FONT));
        tagFontResetButton.setPickable(!Objects.equals(tagFont, TagStyle.DEFAULT_TAG_FONT));

        // Reset TagColorButton, TagColorResetButton
        Color tagColor = tagStyle.getFillColor();
        setViewValue("TagColorButton", tagColor);
        setViewVisible("TagColorResetButton", !tagStyle.isPropDefault(TagStyle.Fill_Prop));

        // Reset TagBorderColorButton, TagBorderColorResetButton
        Color tagLineColor = tagStyle.getLineColor();
        setViewValue("TagBorderColorButton", tagLineColor);
        setViewVisible("TagBorderColorResetButton", !tagStyle.isPropDefault(TagStyle.LineColor_Prop));

        // Reset TagBorderWidthText, TagBorderWidthResetButton
        double tagLineWidth = tagStyle.getLineWidth();
        setViewValue("TagBorderWidthText", tagLineWidth);
        setViewVisible("TagBorderWidthResetButton", tagLineWidth != TagStyle.DEFAULT_TAG_LINE_WIDTH);

        // Reset TickFormatText
        NumberFormat numFormat = NumberFormat.getFormatOrDefault(tagStyle.getTextFormat());
        String numFormatPattern = numFormat.isPatternSet() ? numFormat.getPattern() : null;
        setViewValue("TickFormatText", numFormatPattern);

        // Reset ExpNoneButton, ExpSciButton, ExpFinancialButton
        NumberFormat.ExpStyle expStyle = numFormat.getExpStyle();
        setViewValue("ExpNoneButton", expStyle == NumberFormat.ExpStyle.None);
        setViewValue("ExpSciButton", expStyle == NumberFormat.ExpStyle.Scientific);
        setViewValue("ExpFinancialButton", expStyle == NumberFormat.ExpStyle.Financial);
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get Trace, TagStyle
        Trace trace = getTrace(); if (trace == null) return;
        TagStyle tagStyle = trace.getTagStyle();

        // Handle ShowTagsCheckBox
        if (anEvent.equals("ShowTagsCheckBox")) {
            boolean showTags = anEvent.getBoolValue();
            trace.setShowTags(showTags);
            _traceInsp.resetLater();
        }

        // Handle TagFontText, TagFontSizeAdd1Button, TagFontSizeSub1Button, TagFontResetButton
        Font tagFont = tagStyle.getFont();
        if (anEvent.equals("TagFontText")) {
            String fontStr = anEvent.getStringValue();
            Font font = Font.getFont(fontStr, tagFont.getSize());
            tagStyle.setFont(font);
        }
        if (anEvent.equals("TagFontSizeAdd1Button")) {
            Font font2 = tagFont.deriveFont(tagFont.getSize() + 1);
            tagStyle.setFont(font2);
        }
        if (anEvent.equals("TagFontSizeSub1Button")) {
            double size2 = Math.max(tagFont.getSize() - 1, 6);
            Font font2 = tagFont.deriveFont(size2);
            tagStyle.setFont(font2);
        }
        if (anEvent.equals("TagFontResetButton"))
            tagStyle.setFont(TagStyle.DEFAULT_TAG_FONT);

        // Handle TagColorButton, TagColorResetButton
        if (anEvent.equals("TagColorButton")) {
            Color color = (Color) getViewValue("TagColorButton");
            tagStyle.setFill(color);
        }
        if (anEvent.equals("TagColorResetButton"))
            tagStyle.setFill(TagStyle.DEFAULT_TAG_FILL);

        // Handle TagBorderColorButton, TagBorderColorResetButton
        if (anEvent.equals("TagBorderColorButton")) {
            Color color = (Color) getViewValue("TagBorderColorButton");
            tagStyle.setLineColor(color);
        }
        if (anEvent.equals("TagBorderColorResetButton"))
            tagStyle.setLineColor(TagStyle.DEFAULT_TAG_LINE_COLOR);

        // Handle TagBorderWidthText, TagBorderWidthAdd1Button, TagBorderWidthSub1Button, TagBorderWidthResetButton
        if (anEvent.equals("TagBorderWidthText"))
            tagStyle.setLineWidth(Math.max(anEvent.getIntValue(), 0));
        if (anEvent.equals("TagBorderWidthAdd1Button"))
            tagStyle.setLineWidth(tagStyle.getLineWidth() + 1);
        if (anEvent.equals("TagBorderWidthSub1Button"))
            tagStyle.setLineWidth(Math.max(tagStyle.getLineWidth() - 1, 0));
        if (anEvent.equals("TagBorderWidthResetButton"))
            tagStyle.setLineWidth(TagStyle.DEFAULT_TAG_LINE_WIDTH);

        // Handle TickFormatText
        if (anEvent.equals("TickFormatText")) {
            NumberFormat numFormat = NumberFormat.getFormatOrDefault(tagStyle.getTextFormat());
            tagStyle.setTextFormat(numFormat.copyForProps(NumberFormat.Pattern_Prop, anEvent.getStringValue()));
        }

        // Handle ExpNoneButton, ExpSciButton, ExpFinancialButton
        if (anEvent.equals("ExpNoneButton")) {
            NumberFormat numFormat = NumberFormat.getFormatOrDefault(tagStyle.getTextFormat());
            tagStyle.setTextFormat(numFormat.copyForProps(NumberFormat.ExpStyle_Prop, NumberFormat.ExpStyle.None));
        }
        if (anEvent.equals("ExpSciButton")) {
            NumberFormat numFormat = NumberFormat.getFormatOrDefault(tagStyle.getTextFormat());
            tagStyle.setTextFormat(numFormat.copyForProps(NumberFormat.ExpStyle_Prop, NumberFormat.ExpStyle.Scientific));
        }
        if (anEvent.equals("ExpFinancialButton")) {
            NumberFormat numFormat = NumberFormat.getFormatOrDefault(tagStyle.getTextFormat());
            tagStyle.setTextFormat(numFormat.copyForProps(NumberFormat.ExpStyle_Prop, NumberFormat.ExpStyle.Financial));
        }
    }
}