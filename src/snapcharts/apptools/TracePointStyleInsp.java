/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.apptools;
import snap.geom.Shape;
import snap.gfx.Color;
import snap.util.SnapUtils;
import snap.view.*;
import snapcharts.app.ChartPane;
import snapcharts.model.*;

/**
 * A class to manage UI to edit a TraceStyle.
 */
public class TracePointStyleInsp extends ChartPartInsp {

    // The TraceInsp
    private TraceInsp  _traceInsp;

    // Constants
    private static final Color SYMBOL_COLOR = Color.DARKGRAY;

    /**
     * Constructor.
     */
    public TracePointStyleInsp(ChartPane aChartPane, TraceInsp aTraceInsp)
    {
        super(aChartPane);
        _traceInsp = aTraceInsp;
    }

    /**
     * Returns the name.
     */
    @Override
    public String getName()  { return "Trace Point Settings"; }

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
     * Initialize UI.
     */
    @Override
    protected void initUI()
    {
        // Hide SymbolsBox
        setViewVisible("SymbolsBox", false);

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
        // Get Trace
        Trace trace = getTrace(); if (trace == null) return;

        // Reset ShowPointsCheckBox
        boolean showPoints = trace.isShowPoints();
        setViewValue("ShowPointsCheckBox", showPoints);

        // Reset SymbolColorButton, SymbolColorResetButton
        PointStyle pointStyle = trace.getPointStyle();
        Color symbolColor = pointStyle.getFillColor();
        setViewValue("SymbolColorButton", symbolColor);
        setViewVisible("SymbolColorResetButton", pointStyle.isFillSet());

        // Reset SymbolSizeText, SymbolSizeResetButton
        setViewValue("SymbolSizeText", pointStyle.getSymbolSize());
        setViewVisible("SymbolSizeResetButton", pointStyle.getSymbolSize() != PointStyle.DEFAULT_SYMBOL_SIZE);

        // Reset SymbolShapeButton
        ToggleButton symbolShapeButton = getView("SymbolShapeButton", ToggleButton.class);
        configureSymbolShapeButton(symbolShapeButton, pointStyle.getSymbol());

        // Reset SymbolsBox
        View symbolsBox = getView("SymbolsBox");
        ViewAnimUtils.setVisible(symbolsBox, symbolShapeButton.isSelected(), false, true);

        // Reset SymbolBorderColorButton, SymbolBorderColorResetButton
        Color symbolBorderColor = pointStyle.getLineColor();
        setViewValue("SymbolBorderColorButton", symbolBorderColor);
        setViewVisible("SymbolBorderColorResetButton", pointStyle.isLineColorSet());

        // Reset SymbolBorderWidthText, SymbolBorderWidthResetButton
        double symbolBorderWidth = pointStyle.getLineWidth();
        setViewValue("SymbolBorderWidthText", symbolBorderWidth);
        setViewVisible("SymbolBorderWidthResetButton", symbolBorderWidth != PointStyle.DEFAULT_SYMBOL_BORDER_WIDTH);
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get Trace, PointStyle
        Trace trace = getTrace(); if (trace == null) return;
        PointStyle pointStyle = trace.getPointStyle();

        // Handle ShowPointsCheckBox
        if (anEvent.equals("ShowPointsCheckBox")) {
            boolean showPoints = anEvent.getBoolValue();
            trace.setShowPoints(showPoints);
            if (!showPoints)
                trace.setShowLine(true);
            _traceInsp.resetLater();
        }

        // Handle SymbolColorButton, SymbolColorResetButton
        if (anEvent.equals("SymbolColorButton")) {
            Color color = (Color) getViewValue("SymbolColorButton");
            pointStyle.setFill(color);
        }
        if (anEvent.equals("SymbolColorResetButton"))
            pointStyle.setFill(PointStyle.DEFAULT_SYMBOL_FILL);

        // Handle SymbolSizeText, SymbolSizeAdd1Button, SymbolSizeSub1Button, SymbolSizeResetButton
        if (anEvent.equals("SymbolSizeText"))
            pointStyle.setSymbolSize(Math.max(anEvent.getIntValue(), 6));
        if (anEvent.equals("SymbolSizeAdd1Button"))
            pointStyle.setSymbolSize(pointStyle.getSymbolSize() + 1);
        if (anEvent.equals("SymbolSizeSub1Button"))
            pointStyle.setSymbolSize(Math.max(pointStyle.getSymbolSize() - 1, 6));
        if (anEvent.equals("SymbolSizeResetButton"))
            pointStyle.setSymbolSize(PointStyle.DEFAULT_SYMBOL_SIZE);

        // Handle SymbolShapeButton_
        String eventName = anEvent.getName();
        if (eventName.startsWith("SymbolShapeButton_")) {
            int id = SnapUtils.intValue(eventName);
            pointStyle.setSymbolId(id);
        }

        // Handle SymbolBorderColorButton, SymbolBorderColorResetButton
        if (anEvent.equals("SymbolBorderColorButton")) {
            Color color = (Color) getViewValue("SymbolBorderColorButton");
            pointStyle.setLineColor(color);
        }
        if (anEvent.equals("SymbolBorderColorResetButton"))
            pointStyle.setLineColor(PointStyle.DEFAULT_SYMBOL_BORDER_COLOR);

        // Handle SymbolBorderWidthText, SymbolBorderWidthAdd1Button, SymbolBorderWidthSub1Button, SymbolBorderWidthResetButton
        if (anEvent.equals("SymbolBorderWidthText"))
            pointStyle.setLineWidth(Math.max(anEvent.getIntValue(), 0));
        if (anEvent.equals("SymbolBorderWidthAdd1Button"))
            pointStyle.setLineWidth(pointStyle.getLineWidth() + 1);
        if (anEvent.equals("SymbolBorderWidthSub1Button"))
            pointStyle.setLineWidth(Math.max(pointStyle.getLineWidth() - 1, 0));
        if (anEvent.equals("SymbolBorderWidthResetButton"))
            pointStyle.setLineWidth(PointStyle.DEFAULT_SYMBOL_BORDER_WIDTH);
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