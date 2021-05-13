package snapcharts.apptools;
import snap.geom.Line;
import snap.geom.Shape;
import snap.gfx.Border;
import snap.gfx.Color;
import snap.gfx.Stroke;
import snap.util.SnapUtils;
import snap.view.*;
import snap.viewx.ColorButton;
import snap.viewx.ColorWell;
import snapcharts.app.ChartPane;
import snapcharts.model.*;

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

    @Override
    protected void initUI()
    {
        _inspBox = getView("InspectorBox", ColView.class);

        // Make sure LineStyleBox is hidden
        View lineStyleBox = getView("LineStyleBox");
        lineStyleBox.setVisible(false);

        // Make sure LineDashBox is hidden
        View lineDashBox = getView("LineDashBox");
        lineDashBox.setVisible(false);

        // Configure LineDashButton(s)
        for (int i=0; i<Stroke.DASHES_ALL.length; i++) {
            double[] dashArray = Stroke.DASHES_ALL[i];
            Button lineDashButton = getView("LineDashButton_" + i, Button.class);
            if (lineDashButton != null)
                configureLineDashButton(lineDashButton, dashArray);
        }

        // Make sure ShowSymbolsBox is hidden
        View showSymbolsBox = getView("ShowSymbolsBox");
        showSymbolsBox.setVisible(false);

        // Make sure SymbolsBox is hidden
        View symbolsBox = getView("SymbolsBox");
        symbolsBox.setVisible(false);

        // Configure SymbolShapeButton_X
        for (int i=0; i<Symbol.SYMBOL_COUNT; i++) {
            Symbol symbol = Symbol.getSymbolForId(i);
            Button symbolButton = getView("SymbolShapeButton_" + i, Button.class);
            if (symbolButton != null)
                configureSymbolShapeButton(symbolButton, symbol);
        }
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

        // Reset ShowLineCheckBox, LineWidthText
        boolean showLine = dataStyle.isShowLine();
        setViewValue("ShowLineCheckBox", showLine);
        View lineStyleBox = getView("LineStyleBox");
        ViewAnimUtils.setVisible(lineStyleBox, showLine, false, true);

        // Reset LineWidthText, LineWidthResetButton, LineColorButton, LineColorResetButton
        if (showLine) {
            setViewValue("LineWidthText", dataStyle.getLineWidth());
            setViewVisible("LineWidthResetButton", dataStyle.getLineWidth() != DataStyle.DEFAULT_LINE_WIDTH);
            setViewValue("LineColorButton", dataStyle.getLineColor());
            setViewVisible("LineColorResetButton", dataStyle.isLineColorSet());
        }

        // Reset LineDashButton
        ToggleButton lineDashButton = getView("LineDashButton", ToggleButton.class);
        configureLineDashButton(lineDashButton, dataStyle.getLineDash());

        // Reset LineDashBox
        View lineDashBox = getView("LineDashBox");
        ViewAnimUtils.setVisible(lineDashBox, lineDashButton.isSelected(), false, true).setLinear();

        // Reset ShowSymbolsCheckBox, ShowSymbolsBox
        boolean showSymbols = dataStyle.isShowSymbols();
        setViewValue("ShowSymbolsCheckBox", showSymbols);
        View showSymbolsBox = getView("ShowSymbolsBox");
        ViewAnimUtils.setVisible(showSymbolsBox, showSymbols, false, true);
        if (showSymbols) {
            setViewValue("SymbolSizeText", dataStyle.getSymbolSize());
            setViewVisible("SymbolSizeResetButton", dataStyle.getSymbolSize() != DataStyle.DEFAULT_SYMBOL_SIZE);
        }

        // Reset SymbolShapeButton
        ToggleButton symbolShapeButton = getView("SymbolShapeButton", ToggleButton.class);
        configureSymbolShapeButton(symbolShapeButton, dataStyle.getSymbol());

        // Reset SymbolsBox
        View symbolsBox = getView("SymbolsBox");
        ViewAnimUtils.setVisible(symbolsBox, symbolShapeButton.isSelected(), false, true).setLinear();
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

        // Handle ShowSymbolsCheckBox
        if (anEvent.equals("ShowSymbolsCheckBox")) {
            boolean showSymbols = anEvent.getBoolValue();
            dataStyle.setShowSymbols(showSymbols);
            if (!showSymbols)
                dataStyle.setShowLine(true);
        }

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