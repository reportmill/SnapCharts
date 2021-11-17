/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.apptools;
import snap.geom.Line;
import snap.geom.Shape;
import snap.gfx.*;
import snap.text.NumberFormat;
import snap.util.FormatUtils;
import snap.util.SnapUtils;
import snap.util.StringUtils;
import snap.view.*;
import snapcharts.app.ChartPane;
import snapcharts.model.*;

import java.util.Objects;

/**
 * A class to manage UI to edit a DataStyle.
 */
public class DataStyleInsp extends ChartPartInsp {

    // The View that holds the child insp
    private ColView  _inspBox;

    // The Current ChartPartInsp
    private ChartPartInsp  _currentInsp;

    // The PolarStyleInsp
    private PolarStyleInsp  _polarStyleInsp;

    // The ContourStyleInsp
    private ContourStyleInsp  _contourStyleInsp;

    // Constants
    private static final Color SYMBOL_COLOR = Color.DARKGRAY;

    /**
     * Constructor.
     */
    public DataStyleInsp(ChartPane aChartPane)
    {
        super(aChartPane);
    }

    /**
     * Returns the name.
     */
    @Override
    public String getName()
    {
        // If not set, just return generic
        if (!_chartPane.isUISet()) return "Chart Style";

        // Get Trace ChartType
        Trace trace = getTrace();
        ChartType chartType = trace.getTraceChartType();
        if (chartType == null)
            chartType = getChart().getType();
        return chartType.getStringPlain() + " Style";
    }

    /**
     * Returns the ChartPart.
     */
    @Override
    public ChartPart getChartPart()
    {
        return getTraceStyle();
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
    public TraceStyle getTraceStyle()
    {
        Trace trace = getTrace();
        if (trace != null)
            return trace.getTraceStyle();

        return getChart().getTraceStyle();
    }

    /**
     * Returns the current inspector.
     */
    private ChartPartInsp getCurrentInspector()  { return _currentInsp; }

    /**
     * Sets the current inspector.
     */
    private void setCurrentInspector(ChartPartInsp anInsp)
    {
        // If already set, just return
        if (anInsp == getCurrentInspector()) return;

        // If old, remove it
        if (_currentInsp != null)
            _inspBox.removeChild(_currentInsp.getUI());

        // Set new
        _currentInsp = anInsp;

        // If new, add UI
        if(_currentInsp != null)
            _inspBox.addChild(_currentInsp.getUI());
    }

    /**
     * Returns the ChartPartInsp for chart type.
     */
    private ChartPartInsp getTraceStyleInsp()
    {
        // Get Trace ChartType
        Trace trace = getTrace();
        ChartType chartType = trace != null ? trace.getTraceChartType() : null;
        if (chartType == null)
            chartType = getChart().getType();

        // Return TraceStyleInsp for chartType
        switch (chartType) {
            case POLAR: return getPolarStyleInsp();
            case CONTOUR: return getContourStyleInsp();
            case POLAR_CONTOUR: return getContourStyleInsp();
            default: return null;
        }
    }

    /**
     * Returns the PolarStyleInsp.
     */
    private PolarStyleInsp getPolarStyleInsp()
    {
        if (_polarStyleInsp != null) return _polarStyleInsp;
        PolarStyleInsp insp = new PolarStyleInsp(getChartPane());
        return _polarStyleInsp = insp;
    }

    /**
     * Returns the ContourStyleInsp.
     */
    private ContourStyleInsp getContourStyleInsp()
    {
        if (_contourStyleInsp != null) return _contourStyleInsp;
        ContourStyleInsp insp = new ContourStyleInsp(getChartPane());
        return _contourStyleInsp = insp;
    }

    /**
     * Initialize UI.
     */
    @Override
    protected void initUI()
    {
        _inspBox = getView("InspectorBox", ColView.class);

        // Hide ShowLineBox, LineDashBox
        setViewVisible("ShowLineBox", false);
        setViewVisible("LineDashBox", false);

        // Configure LineDashButton(s)
        for (int i=0; i<Stroke.DASHES_ALL.length; i++) {
            double[] dashArray = Stroke.DASHES_ALL[i];
            Button lineDashButton = getView("LineDashButton_" + i, Button.class);
            if (lineDashButton != null)
                configureLineDashButton(lineDashButton, dashArray);
        }

        // Configure PointJointComboBox
        ComboBox<PointJoin> pointJoinComboBox = getView("PointJoinComboBox", ComboBox.class);
        pointJoinComboBox.setItems(PointJoin.values());

        // Hide ShowAreaBox
        setViewVisible("ShowAreaBox", false);

        // Configure FillModeComboBox to show FillModes
        ComboBox<TraceStyle.FillMode> fillModeComboBox = getView("FillModeComboBox", ComboBox.class);
        fillModeComboBox.setItems(TraceStyle.FillMode.values());
        fillModeComboBox.setItemTextFunction(item -> StringUtils.fromCamelCase(item.toString()));

        // Hide ShowSymbolsBox, SymbolsBox
        setViewVisible("ShowSymbolsBox", false);
        setViewVisible("SymbolsBox", false);

        // Configure SymbolShapeButton_X
        for (int i=0; i<Symbol.SYMBOL_COUNT; i++) {
            Symbol symbol = Symbol.getSymbolForId(i);
            Button symbolButton = getView("SymbolShapeButton_" + i, Button.class);
            if (symbolButton != null)
                configureSymbolShapeButton(symbolButton, symbol);
        }

        // Configure MoreBG ToggleGroup to allow empty, so clicks on selected button will collapse
        getToggleGroup("MoreBG").setAllowEmpty(true);
    }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Update child inspector
        ChartPartInsp chartTypeInsp = getTraceStyleInsp();
        setCurrentInspector(chartTypeInsp);
        if (chartTypeInsp != null)
            chartTypeInsp.resetLater();

        // Update title
        String title = chartTypeInsp != null ? chartTypeInsp.getName() : getName();
        Label label = getCollapser().getLabel();
        label.setText(title);

        // Get DataStyle
        ChartPart selPart = _chartPane.getSelChartPart(); if (selPart == null) return;
        TraceStyle traceStyle = selPart.getTraceStyle(); if (traceStyle == null) return;

        // Reset ShowLineCheckBox
        boolean showLine = traceStyle.isShowLine();
        setViewValue("ShowLineCheckBox", showLine);

        // Reset ShowLineBox.Visible
        boolean showLineMore = getViewBoolValue("ShowLineMoreButton");
        View showLineBox = getView("ShowLineBox");
        ViewAnimUtils.setVisible(showLineBox, showLineMore, false, true);

        // Reset ShowLineBox UI
        if (showLineMore) {

            // Reset LineColorButton, LineColorResetButton
            setViewValue("LineColorButton", traceStyle.getLineColor());
            setViewVisible("LineColorResetButton", traceStyle.isLineColorSet());

            // Reset LineWidthText, LineWidthResetButton
            setViewValue("LineWidthText", traceStyle.getLineWidth());
            setViewVisible("LineWidthResetButton", traceStyle.getLineWidth() != TraceStyle.DEFAULT_LINE_WIDTH);

            // Reset LineDashButton
            ToggleButton lineDashButton = getView("LineDashButton", ToggleButton.class);
            configureLineDashButton(lineDashButton, traceStyle.getLineDash());

            // Reset LineDashBox
            View lineDashBox = getView("LineDashBox");
            ViewAnimUtils.setVisible(lineDashBox, lineDashButton.isSelected(), false, true);

            // Reset PointJoinComboBox
            setViewSelItem("PointJoinComboBox", traceStyle.getPointJoin());
        }

        // Reset ShowAreaCheckBox
        boolean showArea = traceStyle.isShowArea();
        setViewValue("ShowAreaCheckBox", showArea);

        // Reset ShowAreaBox.Visible
        boolean showAreaMore = getViewBoolValue("ShowAreaMoreButton");
        View fillBox = getView("ShowAreaBox");
        ViewAnimUtils.setVisible(fillBox, showAreaMore, false, true);

        // Reset ShowAreaBox UI
        if (showAreaMore) {

            // Reset FillColorButton, FillColorResetButton
            setViewValue("FillColorButton", traceStyle.getFillColor());
            setViewVisible("FillColorResetButton", traceStyle.isFillSet());

            // Reset FillModeComboBox
            setViewSelItem("FillModeComboBox", traceStyle.getFillMode());
        }

        // Reset ShowSymbolsCheckBox
        boolean showSymbols = traceStyle.isShowSymbols();
        setViewValue("ShowSymbolsCheckBox", showSymbols);

        // Reset ShowSymbolsBox.Visible
        boolean showSymbolsMore = getViewBoolValue("ShowSymbolsMoreButton");
        View showSymbolsBox = getView("ShowSymbolsBox");
        ViewAnimUtils.setVisible(showSymbolsBox, showSymbolsMore, false, true);

        // Reset ShowSymbolsBox UI
        if (showSymbolsMore) {

            // Reset SymbolColorButton, SymbolColorResetButton
            SymbolStyle symbolStyle = traceStyle.getSymbolStyle();
            Color symbolColor = symbolStyle.getFillColor();
            setViewValue("SymbolColorButton", symbolColor);
            setViewVisible("SymbolColorResetButton", symbolStyle.isFillSet());

            // Reset SymbolSizeText, SymbolSizeResetButton
            setViewValue("SymbolSizeText", symbolStyle.getSymbolSize());
            setViewVisible("SymbolSizeResetButton", symbolStyle.getSymbolSize() != SymbolStyle.DEFAULT_SYMBOL_SIZE);

            // Reset SymbolShapeButton
            ToggleButton symbolShapeButton = getView("SymbolShapeButton", ToggleButton.class);
            configureSymbolShapeButton(symbolShapeButton, symbolStyle.getSymbol());

            // Reset SymbolsBox
            View symbolsBox = getView("SymbolsBox");
            ViewAnimUtils.setVisible(symbolsBox, symbolShapeButton.isSelected(), false, true);

            // Reset SymbolBorderColorButton, SymbolBorderColorResetButton
            Color symbolBorderColor = symbolStyle.getLineColor();
            setViewValue("SymbolBorderColorButton", symbolBorderColor);
            setViewVisible("SymbolBorderColorResetButton", symbolStyle.isLineColorSet());

            // Reset SymbolBorderWidthText, SymbolBorderWidthResetButton
            double symbolBorderWidth = symbolStyle.getLineWidth();
            setViewValue("SymbolBorderWidthText", symbolBorderWidth);
            setViewVisible("SymbolBorderWidthResetButton", symbolBorderWidth != SymbolStyle.DEFAULT_SYMBOL_BORDER_WIDTH);
        }

        // Reset ShowTagsCheckBox
        boolean showTags = traceStyle.isShowTags();
        setViewValue("ShowTagsCheckBox", showTags);

        // Reset ShowTagsBox.Visible
        boolean showTagsMore = getViewBoolValue("ShowTagsMoreButton");
        View showTagsBox = getView("ShowTagsBox");
        ViewAnimUtils.setVisible(showTagsBox, showTagsMore, false, true);

        // Reset ShowTagsBox UI
        if (showTagsMore) {

            // Reset TagFontText, TagFontResetButton
            TagStyle tagStyle = traceStyle.getTagStyle();
            Font tagFont = tagStyle.getFont();
            String fontName = tagFont.getName() + ' ' + FormatUtils.formatNum(tagFont.getSize());
            setViewValue("TagFontText", fontName);
            View tagFontResetButton = getView("TagFontResetButton");
            tagFontResetButton.setPaintable(!Objects.equals(tagFont, TagStyle.DEFAULT_TAG_FONT));
            tagFontResetButton.setPickable(!Objects.equals(tagFont, TagStyle.DEFAULT_TAG_FONT));

            // Reset TagColorButton, TagColorResetButton
            Color tagColor = tagStyle.getFillColor();
            setViewValue("TagColorButton", tagColor);
            setViewVisible("TagColorResetButton", tagStyle.isFillSet());

            // Reset TagBorderColorButton, TagBorderColorResetButton
            Color tagLineColor = tagStyle.getLineColor();
            setViewValue("TagBorderColorButton", tagLineColor);
            setViewVisible("TagBorderColorResetButton", tagStyle.isLineColorSet());

            // Reset TagBorderWidthText, TagBorderWidthResetButton
            double tagLineWidth = tagStyle.getLineWidth();
            setViewValue("TagBorderWidthText", tagLineWidth);
            setViewVisible("TagBorderWidthResetButton", tagLineWidth != TagStyle.DEFAULT_TAG_BORDER_WIDTH);

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

        // Reset PointSpacing UI
        boolean showSymbolsOrTags = showSymbols || showTags;
        setViewVisible("PointSpacingSep", showSymbolsOrTags);
        setViewVisible("PointSpacingLabel", showSymbolsOrTags);
        setViewVisible("PointSpacingBox", showSymbolsOrTags);
        if (showSymbolsOrTags) {

            // Reset PointSpacingText, PointSpacingResetButton
            setViewValue("PointSpacingText", traceStyle.getPointSpacing());
            setViewVisible("PointSpacingResetButton", traceStyle.getPointSpacing() != TraceStyle.DEFAULT_POINT_SPACING);

            // Reset SkipPointCountText, SkipPointCountResetButton
            setViewValue("SkipPointCountText", traceStyle.getSkipPointCount());
            setViewVisible("SkipPointCountResetButton", traceStyle.getSkipPointCount() != TraceStyle.DEFAULT_SKIP_POINT_COUNT);

            // Reset MaxPointCountText, MaxPointCountResetButton
            setViewValue("MaxPointCountText", traceStyle.getMaxPointCount());
            setViewVisible("MaxPointCountResetButton", traceStyle.getMaxPointCount() != TraceStyle.DEFAULT_MAX_POINT_COUNT);
        }
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get DataStyle, SymbolStyle, TagStyle
        ChartPart selPart = _chartPane.getSelChartPart();
        if (selPart == null) return;
        TraceStyle traceStyle = selPart.getTraceStyle();
        if (traceStyle == null) return;
        SymbolStyle symbolStyle = traceStyle.getSymbolStyle();
        TagStyle tagStyle = traceStyle.getTagStyle();

        // Handle ShowLineCheckBox
        if (anEvent.equals("ShowLineCheckBox")) {
            boolean showLine = anEvent.getBoolValue();
            traceStyle.setShowLine(showLine);
            if (!showLine)
                traceStyle.setShowSymbols(true);
            setViewValue("ShowLineMoreButton", showLine);
        }

        // Handle LineWidthText, LineWidthAdd1Button, LineWidthSub1Button, LineWidthResetButton
        if (anEvent.equals("LineWidthText"))
            traceStyle.setLineWidth(Math.max(anEvent.getIntValue(), 1));
        if (anEvent.equals("LineWidthAdd1Button"))
            traceStyle.setLineWidth(traceStyle.getLineWidth() + 1);
        if (anEvent.equals("LineWidthSub1Button"))
            traceStyle.setLineWidth(Math.max(traceStyle.getLineWidth() - 1, 1));
        if (anEvent.equals("LineWidthResetButton"))
            traceStyle.setLineWidth(1);

        // Handle LineColorButton, LineColorResetButton
        if (anEvent.equals("LineColorButton")) {
            Color color = (Color) getViewValue("LineColorButton");
            traceStyle.setLineColor(color);
        }
        if (anEvent.equals("LineColorResetButton"))
            traceStyle.setLineColor(null);

        // Handle LineDashButton_X
        String eventName = anEvent.getName();
        if (eventName.startsWith("LineDashButton_")) {
            int id = SnapUtils.intValue(eventName);
            double[] dashArray = Stroke.DASHES_ALL[id];
            traceStyle.setLineDash(dashArray);
        }

        // Handle PointJoinComboBox
        if (anEvent.equals("PointJoinComboBox"))
            traceStyle.setPointJoin((PointJoin) anEvent.getSelItem());

        // Handle ShowAreaCheckBox, FillModeComboBox
        if (anEvent.equals("ShowAreaCheckBox")) {
            boolean showArea = anEvent.getBoolValue();
            traceStyle.setShowArea(showArea);
            setViewValue("ShowAreaMoreButton", showArea);
        }

        // Handle FillColorButton, FillColorResetButton
        if (anEvent.equals("FillColorButton")) {
            Color color = (Color) getViewValue("FillColorButton");
            color = color.getAlpha() <= .5 ? color : color.copyForAlpha(.5);
            traceStyle.setFill(color);
        }
        if (anEvent.equals("FillColorResetButton"))
            traceStyle.setFill(null);

        // Handle FillModeComboBox
        if (anEvent.equals("FillModeComboBox")) {
            TraceStyle.FillMode fillMode = (TraceStyle.FillMode) getViewSelItem("FillModeComboBox");
            traceStyle.setFillMode(fillMode);
        }

        // Handle ShowSymbolsCheckBox
        if (anEvent.equals("ShowSymbolsCheckBox")) {
            boolean showSymbols = anEvent.getBoolValue();
            traceStyle.setShowSymbols(showSymbols);
            if (!showSymbols)
                traceStyle.setShowLine(true);
            setViewValue("ShowSymbolsMoreButton", showSymbols);
        }

        // Handle SymbolColorButton, SymbolColorResetButton
        if (anEvent.equals("SymbolColorButton")) {
            Color color = (Color) getViewValue("SymbolColorButton");
            symbolStyle.setFill(color);
        }
        if (anEvent.equals("SymbolColorResetButton"))
            symbolStyle.setFill(SymbolStyle.DEFAULT_SYMBOL_FILL);

        // Handle SymbolSizeText, SymbolSizeAdd1Button, SymbolSizeSub1Button, SymbolSizeResetButton
        if (anEvent.equals("SymbolSizeText"))
            symbolStyle.setSymbolSize(Math.max(anEvent.getIntValue(), 6));
        if (anEvent.equals("SymbolSizeAdd1Button"))
            symbolStyle.setSymbolSize(symbolStyle.getSymbolSize() + 1);
        if (anEvent.equals("SymbolSizeSub1Button"))
            symbolStyle.setSymbolSize(Math.max(symbolStyle.getSymbolSize() - 1, 6));
        if (anEvent.equals("SymbolSizeResetButton"))
            symbolStyle.setSymbolSize(SymbolStyle.DEFAULT_SYMBOL_SIZE);

        // Handle SymbolShapeButton_
        if (eventName.startsWith("SymbolShapeButton_")) {
            int id = SnapUtils.intValue(eventName);
            symbolStyle.setSymbolId(id);
        }

        // Handle SymbolBorderColorButton, SymbolBorderColorResetButton
        if (anEvent.equals("SymbolBorderColorButton")) {
            Color color = (Color) getViewValue("SymbolBorderColorButton");
            symbolStyle.setLineColor(color);
        }
        if (anEvent.equals("SymbolBorderColorResetButton"))
            symbolStyle.setLineColor(SymbolStyle.DEFAULT_SYMBOL_BORDER_COLOR);

        // Handle SymbolBorderWidthText, SymbolBorderWidthAdd1Button, SymbolBorderWidthSub1Button, SymbolBorderWidthResetButton
        if (anEvent.equals("SymbolBorderWidthText"))
            symbolStyle.setLineWidth(Math.max(anEvent.getIntValue(), 0));
        if (anEvent.equals("SymbolBorderWidthAdd1Button"))
            symbolStyle.setLineWidth(symbolStyle.getLineWidth() + 1);
        if (anEvent.equals("SymbolBorderWidthSub1Button"))
            symbolStyle.setLineWidth(Math.max(symbolStyle.getLineWidth() - 1, 0));
        if (anEvent.equals("SymbolBorderWidthResetButton"))
            symbolStyle.setLineWidth(SymbolStyle.DEFAULT_SYMBOL_BORDER_WIDTH);

        // Handle ShowTagsCheckBox
        if (anEvent.equals("ShowTagsCheckBox")) {
            boolean showTags = anEvent.getBoolValue();
            traceStyle.setShowTags(showTags);
            setViewValue("ShowTagsMoreButton", showTags);
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
            tagStyle.setFill(TagStyle.DEFAULT_TAG_COLOR);

        // Handle TagBorderColorButton, TagBorderColorResetButton
        if (anEvent.equals("TagBorderColorButton")) {
            Color color = (Color) getViewValue("TagBorderColorButton");
            tagStyle.setLineColor(color);
        }
        if (anEvent.equals("TagBorderColorResetButton"))
            tagStyle.setLineColor(TagStyle.DEFAULT_TAG_BORDER_COLOR);

        // Handle TagBorderWidthText, TagBorderWidthAdd1Button, TagBorderWidthSub1Button, TagBorderWidthResetButton
        if (anEvent.equals("TagBorderWidthText"))
            tagStyle.setLineWidth(Math.max(anEvent.getIntValue(), 0));
        if (anEvent.equals("TagBorderWidthAdd1Button"))
            tagStyle.setLineWidth(tagStyle.getLineWidth() + 1);
        if (anEvent.equals("TagBorderWidthSub1Button"))
            tagStyle.setLineWidth(Math.max(tagStyle.getLineWidth() - 1, 0));
        if (anEvent.equals("TagBorderWidthResetButton"))
            tagStyle.setLineWidth(TagStyle.DEFAULT_TAG_BORDER_WIDTH);

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

        // Handle PointSpacingText, PointSpacingAdd1Button, PointSpacingSub1Button, PointSpacingResetButton
        if (anEvent.equals("PointSpacingText"))
            traceStyle.setPointSpacing(Math.max(anEvent.getIntValue(), 0));
        if (anEvent.equals("PointSpacingAdd1Button"))
            traceStyle.setPointSpacing(traceStyle.getPointSpacing() + 1);
        if (anEvent.equals("PointSpacingSub1Button"))
            traceStyle.setPointSpacing(Math.max(traceStyle.getPointSpacing() - 1, 0));
        if (anEvent.equals("PointSpacingResetButton"))
            traceStyle.setPointSpacing(TraceStyle.DEFAULT_POINT_SPACING);

        // Handle SkipPointCountText, SkipPointCountAdd1Button, SkipPointCountSub1Button, SkipPointCountResetButton
        if (anEvent.equals("SkipPointCountText"))
            traceStyle.setSkipPointCount(Math.max(anEvent.getIntValue(), 0));
        if (anEvent.equals("SkipPointCountAdd1Button"))
            traceStyle.setSkipPointCount(traceStyle.getSkipPointCount() + 1);
        if (anEvent.equals("SkipPointCountSub1Button"))
            traceStyle.setSkipPointCount(Math.max(traceStyle.getSkipPointCount() - 1, 0));
        if (anEvent.equals("SkipPointCountResetButton"))
            traceStyle.setSkipPointCount(TraceStyle.DEFAULT_SKIP_POINT_COUNT);

        // Handle MaxPointCountText, MaxPointCountAdd1Button, MaxPointCountSub1Button, MaxPointCountResetButton
        if (anEvent.equals("MaxPointCountText"))
            traceStyle.setMaxPointCount(Math.max(anEvent.getIntValue(), 0));
        if (anEvent.equals("MaxPointCountAdd1Button"))
            traceStyle.setMaxPointCount(traceStyle.getMaxPointCount() + 1);
        if (anEvent.equals("MaxPointCountSub1Button"))
            traceStyle.setMaxPointCount(Math.max(traceStyle.getMaxPointCount() - 1, 0));
        if (anEvent.equals("MaxPointCountResetButton"))
            traceStyle.setMaxPointCount(TraceStyle.DEFAULT_MAX_POINT_COUNT);
    }

    /**
     * Configures a LineDash button.
     */
    private void configureLineDashButton(ButtonBase aButton, double[] dashArray)
    {
        Stroke stroke = Stroke.Stroke2.copyForCap(Stroke.Cap.Butt).copyForDashes(dashArray);
        Border border = Border.createLineBorder(Color.BLUE.darker(), 2).copyForStroke(stroke);
        Shape shape = new Line(5, 5, 80, 5);
        ShapeView shapeView = new ShapeView(shape);
        shapeView.setBounds(0, 0, 100, 12);
        shapeView.setBorder(border);
        aButton.setGraphic(shapeView);
    }

    /**
     * Configures a SymbolShapeButton.
     */
    private void configureSymbolShapeButton(ButtonBase aButton, Symbol aSymbol)
    {
        Symbol symbol = aSymbol.copyForSize(12);
        Shape shape = symbol.getShape();
        ShapeView shapeView = new ShapeView(shape);
        shapeView.setFill(SYMBOL_COLOR);
        aButton.setGraphic(shapeView);
    }
}