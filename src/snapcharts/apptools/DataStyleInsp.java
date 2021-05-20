package snapcharts.apptools;
import snap.geom.Line;
import snap.geom.Shape;
import snap.gfx.Border;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.gfx.Stroke;
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

    // The ContourPropsInsp
    private ContourStyleInsp _contourStyleInsp;

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
        if (!_chartPane.isUISet()) return "Chart Style";
        Chart chart = getChart();
        ChartType chartType = chart.getType();
        return chartType.getStringPlain() + " Style";
    }

    /**
     * Returns the ChartPart.
     */
    @Override
    public ChartPart getChartPart()  { return getChart().getDataStyle(); }

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
    private ChartPartInsp getChartPropsInsp()
    {
        ChartType chartType = getChart().getType();
        switch (chartType) {
            case CONTOUR: return getContourPropsInsp();
            case POLAR_CONTOUR: return getContourPropsInsp();
            default: return null;
        }
    }

    /**
     * Returns the ContourPropsInsp.
     */
    private ContourStyleInsp getContourPropsInsp()
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

        // Hide ShowAreaBox
        setViewVisible("ShowAreaBox", false);

        // Configure FillModeComboBox to show FillModes
        ComboBox<DataStyle.FillMode> fillModeComboBox = getView("FillModeComboBox", ComboBox.class);
        fillModeComboBox.setItems(DataStyle.FillMode.values());
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
        ChartPartInsp chartTypeInsp = getChartPropsInsp();
        setCurrentInspector(chartTypeInsp);
        if (chartTypeInsp != null)
            chartTypeInsp.resetLater();

        // Update title
        String title = chartTypeInsp != null ? chartTypeInsp.getName() : getName();
        Label label = getCollapser().getLabel();
        label.setText(title);

        // Get DataStyle
        ChartPart selPart = _chartPane.getSelChartPart(); if (selPart == null) return;
        DataStyle dataStyle = selPart.getDataStyle(); if (dataStyle == null) return;

        // Reset ShowLineCheckBox
        boolean showLine = dataStyle.isShowLine();
        setViewValue("ShowLineCheckBox", showLine);

        // Reset ShowLineBox.Visible
        boolean showLineMore = getViewBoolValue("ShowLineMoreButton");
        View showLineBox = getView("ShowLineBox");
        ViewAnimUtils.setVisible(showLineBox, showLineMore, false, true);

        // Reset ShowLineBox UI
        if (showLineMore) {

            // Reset LineColorButton, LineColorResetButton
            setViewValue("LineColorButton", dataStyle.getLineColor());
            setViewVisible("LineColorResetButton", dataStyle.isLineColorSet());

            // Reset LineWidthText, LineWidthResetButton
            setViewValue("LineWidthText", dataStyle.getLineWidth());
            setViewVisible("LineWidthResetButton", dataStyle.getLineWidth() != DataStyle.DEFAULT_LINE_WIDTH);

            // Reset LineDashButton
            ToggleButton lineDashButton = getView("LineDashButton", ToggleButton.class);
            configureLineDashButton(lineDashButton, dataStyle.getLineDash());

            // Reset LineDashBox
            View lineDashBox = getView("LineDashBox");
            ViewAnimUtils.setVisible(lineDashBox, lineDashButton.isSelected(), false, true);
        }

        // Reset ShowAreaCheckBox
        boolean showArea = dataStyle.isShowArea();
        setViewValue("ShowAreaCheckBox", showArea);

        // Reset ShowAreaBox.Visible
        boolean showAreaMore = getViewBoolValue("ShowAreaMoreButton");
        View fillBox = getView("ShowAreaBox");
        ViewAnimUtils.setVisible(fillBox, showAreaMore, false, true);

        // Reset ShowAreaBox UI
        if (showAreaMore) {

            // Reset FillColorButton, FillColorResetButton
            setViewValue("FillColorButton", dataStyle.getFillColor());
            setViewVisible("FillColorResetButton", dataStyle.isFillColorSet());

            // Reset FillModeComboBox
            setViewSelItem("FillModeComboBox", dataStyle.getFillMode());
        }

        // Reset ShowSymbolsCheckBox
        boolean showSymbols = dataStyle.isShowSymbols();
        setViewValue("ShowSymbolsCheckBox", showSymbols);

        // Reset ShowSymbolsBox.Visible
        boolean showSymbolsMore = getViewBoolValue("ShowSymbolsMoreButton");
        View showSymbolsBox = getView("ShowSymbolsBox");
        ViewAnimUtils.setVisible(showSymbolsBox, showSymbolsMore, false, true);

        // Reset ShowSymbolsBox UI
        if (showSymbolsMore) {

            // Reset SymbolColorButton, SymbolColorResetButton
            setViewValue("SymbolColorButton", dataStyle.getSymbolColor());
            setViewVisible("SymbolColorResetButton", dataStyle.isSymbolColorSet());

            // Reset SymbolSizeText, SymbolSizeResetButton
            setViewValue("SymbolSizeText", dataStyle.getSymbolSize());
            setViewVisible("SymbolSizeResetButton", dataStyle.getSymbolSize() != DataStyle.DEFAULT_SYMBOL_SIZE);

            // Reset SymbolShapeButton
            ToggleButton symbolShapeButton = getView("SymbolShapeButton", ToggleButton.class);
            configureSymbolShapeButton(symbolShapeButton, dataStyle.getSymbol());

            // Reset SymbolsBox
            View symbolsBox = getView("SymbolsBox");
            ViewAnimUtils.setVisible(symbolsBox, symbolShapeButton.isSelected(), false, true);

            // Reset SymbolBorderColorButton, SymbolBorderColorResetButton
            setViewValue("SymbolBorderColorButton", dataStyle.getSymbolBorderColor());
            setViewVisible("SymbolBorderColorResetButton",
                !Objects.equals(dataStyle.getSymbolBorderColor(), DataStyle.DEFAULT_SYMBOL_BORDER_COLOR));

            // Reset SymbolBorderWidthText, SymbolBorderWidthResetButton
            setViewValue("SymbolBorderWidthText", dataStyle.getSymbolBorderWidth());
            setViewVisible("SymbolBorderWidthResetButton",
        dataStyle.getSymbolBorderWidth() != DataStyle.DEFAULT_SYMBOL_BORDER_WIDTH);
        }

        // Reset ShowTagsCheckBox
        boolean showTags = dataStyle.isShowTags();
        setViewValue("ShowTagsCheckBox", showTags);

        // Reset ShowTagsBox.Visible
        boolean showTagsMore = getViewBoolValue("ShowTagsMoreButton");
        View showTagsBox = getView("ShowTagsBox");
        ViewAnimUtils.setVisible(showTagsBox, showTagsMore, false, true);

        // Reset ShowTagsBox UI
        if (showTagsMore) {

            // Reset TagFontText, TagFontResetButton
            Font tagFont = dataStyle.getTagFont();
            String fontName = tagFont.getName() + ' ' + FormatUtils.formatNum(tagFont.getSize());
            setViewValue("TagFontText", fontName);
            View tagFontResetButton = getView("TagFontResetButton");
            tagFontResetButton.setPaintable(!Objects.equals(tagFont, DataStyle.DEFAULT_TAG_FONT));
            tagFontResetButton.setPickable(!Objects.equals(tagFont, DataStyle.DEFAULT_TAG_FONT));

            // Reset TagColorButton, TagColorResetButton
            setViewValue("TagColorButton", dataStyle.getTagColor());
            setViewVisible("TagColorResetButton", !Objects.equals(dataStyle.getTagColor(), DataStyle.DEFAULT_TAG_COLOR));

            // Reset TagBorderColorButton, TagBorderColorResetButton
            setViewValue("TagBorderColorButton", dataStyle.getTagBorderColor());
            setViewVisible("TagBorderColorResetButton", dataStyle.isTagBorderColorSet());

            // Reset TagBorderWidthText, TagBorderWidthResetButton
            setViewValue("TagBorderWidthText", dataStyle.getTagBorderWidth());
            setViewVisible("TagBorderWidthResetButton",
        dataStyle.getTagBorderWidth() != DataStyle.DEFAULT_TAG_BORDER_WIDTH);
        }
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get DataStyle
        ChartPart selPart = _chartPane.getSelChartPart();
        if (selPart == null) return;
        DataStyle dataStyle = selPart.getDataStyle();
        if (dataStyle == null) return;

        // Handle ShowLineCheckBox
        if (anEvent.equals("ShowLineCheckBox")) {
            boolean showLine = anEvent.getBoolValue();
            dataStyle.setShowLine(showLine);
            if (!showLine)
                dataStyle.setShowSymbols(true);
            setViewValue("ShowLineMoreButton", showLine);
        }

        // Handle LineWidthText, LineWidthAdd1Button, LineWidthSub1Button, LineWidthResetButton
        if (anEvent.equals("LineWidthText"))
            dataStyle.setLineWidth(Math.max(anEvent.getIntValue(), 1));
        if (anEvent.equals("LineWidthAdd1Button"))
            dataStyle.setLineWidth(dataStyle.getLineWidth() + 1);
        if (anEvent.equals("LineWidthSub1Button"))
            dataStyle.setLineWidth(Math.max(dataStyle.getLineWidth() - 1, 1));
        if (anEvent.equals("LineWidthResetButton"))
            dataStyle.setLineWidth(1);

        // Handle LineColorButton, LineColorResetButton
        if (anEvent.equals("LineColorButton")) {
            Color color = (Color) getViewValue("LineColorButton");
            dataStyle.setLineColor(color);
        }
        if (anEvent.equals("LineColorResetButton"))
            dataStyle.setLineColor(null);

        // Handle LineDashButton_X
        String eventName = anEvent.getName();
        if (eventName.startsWith("LineDashButton_")) {
            int id = SnapUtils.intValue(eventName);
            double[] dashArray = Stroke.DASHES_ALL[id];
            dataStyle.setLineDash(dashArray);
        }

        // Handle ShowAreaCheckBox, FillModeComboBox
        if (anEvent.equals("ShowAreaCheckBox")) {
            boolean showArea = anEvent.getBoolValue();
            dataStyle.setShowArea(showArea);
            setViewValue("ShowAreaMoreButton", showArea);
        }

        // Handle FillColorButton, FillColorResetButton
        if (anEvent.equals("FillColorButton")) {
            Color color = (Color) getViewValue("FillColorButton");
            color = color.getAlpha() <= .5 ? color : color.copyForAlpha(.5);
            dataStyle.setFillColor(color);
        }
        if (anEvent.equals("FillColorResetButton"))
            dataStyle.setFillColor(null);

        // Handle FillModeComboBox
        if (anEvent.equals("FillModeComboBox")) {
            DataStyle.FillMode fillMode = (DataStyle.FillMode) getViewSelItem("FillModeComboBox");
            dataStyle.setFillMode(fillMode);
        }

        // Handle ShowSymbolsCheckBox
        if (anEvent.equals("ShowSymbolsCheckBox")) {
            boolean showSymbols = anEvent.getBoolValue();
            dataStyle.setShowSymbols(showSymbols);
            if (!showSymbols)
                dataStyle.setShowLine(true);
            setViewValue("ShowSymbolsMoreButton", showSymbols);
        }

        // Handle SymbolColorButton, SymbolColorResetButton
        if (anEvent.equals("SymbolColorButton")) {
            Color color = (Color) getViewValue("SymbolColorButton");
            dataStyle.setSymbolColor(color);
        }
        if (anEvent.equals("SymbolColorResetButton"))
            dataStyle.setSymbolColor(null);

        // Handle SymbolSizeText, SymbolSizeAdd1Button, SymbolSizeSub1Button, SymbolSizeResetButton
        if (anEvent.equals("SymbolSizeText"))
            dataStyle.setSymbolSize(Math.max(anEvent.getIntValue(), 6));
        if (anEvent.equals("SymbolSizeAdd1Button"))
            dataStyle.setSymbolSize(dataStyle.getSymbolSize() + 1);
        if (anEvent.equals("SymbolSizeSub1Button"))
            dataStyle.setSymbolSize(Math.max(dataStyle.getSymbolSize() - 1, 6));
        if (anEvent.equals("SymbolSizeResetButton"))
            dataStyle.setSymbolSize(DataStyle.DEFAULT_SYMBOL_SIZE);

        // Handle SymbolShapeButton_
        if (eventName.startsWith("SymbolShapeButton_")) {
            int id = SnapUtils.intValue(eventName);
            dataStyle.setSymbolId(id);
        }

        // Handle SymbolBorderColorButton, SymbolBorderColorResetButton
        if (anEvent.equals("SymbolBorderColorButton")) {
            Color color = (Color) getViewValue("SymbolBorderColorButton");
            dataStyle.setSymbolBorderColor(color);
        }
        if (anEvent.equals("SymbolBorderColorResetButton"))
            dataStyle.setSymbolBorderColor(DataStyle.DEFAULT_SYMBOL_BORDER_COLOR);

        // Handle SymbolBorderWidthText, SymbolBorderWidthAdd1Button, SymbolBorderWidthSub1Button, SymbolBorderWidthResetButton
        if (anEvent.equals("SymbolBorderWidthText"))
            dataStyle.setSymbolBorderWidth(Math.max(anEvent.getIntValue(), 0));
        if (anEvent.equals("SymbolBorderWidthAdd1Button"))
            dataStyle.setSymbolBorderWidth(dataStyle.getSymbolBorderWidth() + 1);
        if (anEvent.equals("SymbolBorderWidthSub1Button"))
            dataStyle.setSymbolBorderWidth(Math.max(dataStyle.getSymbolBorderWidth() - 1, 0));
        if (anEvent.equals("SymbolBorderWidthResetButton"))
            dataStyle.setSymbolBorderWidth(DataStyle.DEFAULT_SYMBOL_BORDER_WIDTH);

        // Handle ShowTagsCheckBox
        if (anEvent.equals("ShowTagsCheckBox")) {
            boolean showTags = anEvent.getBoolValue();
            dataStyle.setShowTags(showTags);
            setViewValue("ShowTagsMoreButton", showTags);
        }

        // Handle TagFontText, TagFontSizeAdd1Button, TagFontSizeSub1Button, TagFontResetButton
        if (anEvent.equals("TagFontText")) {
            String fontStr = anEvent.getStringValue();
            Font font = Font.getFont(fontStr, dataStyle.getTagFont().getSize());
            dataStyle.setFont(font);
        }
        if (anEvent.equals("TagFontSizeAdd1Button")) {
            Font font2 = dataStyle.getTagFont().deriveFont(dataStyle.getTagFont().getSize() + 1);
            dataStyle.setTagFont(font2);
        }
        if (anEvent.equals("TagFontSizeSub1Button")) {
            double size2 = Math.max(dataStyle.getTagFont().getSize() - 1, 6);
            Font font2 = dataStyle.getTagFont().deriveFont(size2);
            dataStyle.setTagFont(font2);
        }
        if (anEvent.equals("TagFontResetButton"))
            dataStyle.setTagFont(DataStyle.DEFAULT_TAG_FONT);

        // Handle TagColorButton, TagColorResetButton
        if (anEvent.equals("TagColorButton")) {
            Color color = (Color) getViewValue("TagColorButton");
            dataStyle.setTagColor(color);
        }
        if (anEvent.equals("TagColorResetButton"))
            dataStyle.setTagColor(DataStyle.DEFAULT_TAG_COLOR);

        // Handle TagBorderColorButton, TagBorderColorResetButton
        if (anEvent.equals("TagBorderColorButton")) {
            Color color = (Color) getViewValue("TagBorderColorButton");
            dataStyle.setTagBorderColor(color);
        }
        if (anEvent.equals("TagBorderColorResetButton"))
            dataStyle.setTagBorderColor(DataStyle.DEFAULT_TAG_BORDER_COLOR);

        // Handle TagBorderWidthText, TagBorderWidthAdd1Button, TagBorderWidthSub1Button, TagBorderWidthResetButton
        if (anEvent.equals("TagBorderWidthText"))
            dataStyle.setTagBorderWidth(Math.max(anEvent.getIntValue(), 0));
        if (anEvent.equals("TagBorderWidthAdd1Button"))
            dataStyle.setTagBorderWidth(dataStyle.getTagBorderWidth() + 1);
        if (anEvent.equals("TagBorderWidthSub1Button"))
            dataStyle.setTagBorderWidth(Math.max(dataStyle.getTagBorderWidth() - 1, 0));
        if (anEvent.equals("TagBorderWidthResetButton"))
            dataStyle.setTagBorderWidth(DataStyle.DEFAULT_TAG_BORDER_WIDTH);
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