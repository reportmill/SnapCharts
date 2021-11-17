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
        // Get TraceStyle
        TraceStyle traceStyle = getTraceStyle(); if (traceStyle == null) return;

        // Reset ShowSymbolsCheckBox
        boolean showSymbols = traceStyle.isShowSymbols();
        setViewValue("ShowSymbolsCheckBox", showSymbols);

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

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get TraceStyle, SymbolStyle
        TraceStyle traceStyle = getTraceStyle(); if (traceStyle == null) return;
        SymbolStyle symbolStyle = traceStyle.getSymbolStyle();

        // Handle ShowSymbolsCheckBox
        if (anEvent.equals("ShowSymbolsCheckBox")) {
            boolean showSymbols = anEvent.getBoolValue();
            traceStyle.setShowSymbols(showSymbols);
            if (!showSymbols)
                traceStyle.setShowLine(true);
            _traceInsp.resetLater();
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
        String eventName = anEvent.getName();
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