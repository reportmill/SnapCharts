package snapcharts.apptools;
import snap.geom.Shape;
import snap.gfx.Color;
import snap.util.SnapUtils;
import snap.view.*;
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

        // Configure SymbolXButton(s)
        for (int i=0; i<Symbol.SYMBOL_COUNT; i++) {
            Shape shape = Symbol.getShapeForId(i);
            ShapeView shapeView = new ShapeView(shape);
            shapeView.setFill(Color.BLACK);
            Button symbolButton = getView("Symbol" + i + "Button", Button.class);
            symbolButton.setGraphic(shapeView);
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
        ChartPart selPart = _chartPane.getSel().getSelChartPart(); if (selPart == null) return;
        DataStyle dataStyle = selPart.getDataStyle(); if (dataStyle == null) return;

        // Reset ShowLineCheckBox, LineWidthText
        boolean showLine = dataStyle.isShowLine();
        setViewValue("ShowLineCheckBox", showLine);
        getView("LineStyleBox").setVisible(showLine);
        if (showLine) {
            setViewValue("LineWidthText", dataStyle.getLineWidth());
            setViewEnabled("LineWidthResetButton", dataStyle.getLineWidth() != 1);
        }

        // Reset ShowSymbolsCheckBox
        setViewValue("ShowSymbolsCheckBox", dataStyle.isShowSymbols());

        // Reset SymbolShapeButton
        Symbol symbol = dataStyle.getSymbol();
        Shape shape = symbol.getShape();
        ShapeView shapeView = new ShapeView(shape);
        shapeView.setFill(Color.BLACK);
        ToggleButton symbolShapeButton = getView("SymbolShapeButton", ToggleButton.class);
        symbolShapeButton.setGraphic(shapeView);

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
        ChartPart selPart = _chartPane.getSel().getSelChartPart(); if (selPart == null) return;
        DataStyle dataStyle = selPart.getDataStyle(); if (dataStyle == null) return;

        // Handle ShowLineCheckBox, LineWidthText
        if (anEvent.equals("ShowLineCheckBox")) {
            boolean showLine = anEvent.getBoolValue();
            dataStyle.setShowLine(showLine);
            if (!showLine)
                dataStyle.setShowSymbols(true);
        }
        if (anEvent.equals("LineWidthText"))
            dataStyle.setLineWidth(Math.max(anEvent.getIntValue(), 1));

        // Handle LineWidthAdd1Button, LineWidthSub1Button, LineWidthResetButton
        if (anEvent.equals("LineWidthAdd1Button"))
            dataStyle.setLineWidth(dataStyle.getLineWidth() + 1);
        if (anEvent.equals("LineWidthSub1Button"))
            dataStyle.setLineWidth(Math.max(dataStyle.getLineWidth() - 1, 1));
        if (anEvent.equals("LineWidthResetButton"))
            dataStyle.setLineWidth(1);

        // Handle ShowSymbolsCheckBox
        if (anEvent.equals("ShowSymbolsCheckBox")) {
            boolean showSymbols = anEvent.getBoolValue();
            dataStyle.setShowSymbols(showSymbols);
            if (!showSymbols)
                dataStyle.setShowLine(true);
        }

        // Handle SymbolXButton
        String name = anEvent.getName();
        if (name.startsWith("Symbol") && name.endsWith("Button")) {
            int id = SnapUtils.intValue(name);
            dataStyle.setSymbolId(id);
        }
    }
}