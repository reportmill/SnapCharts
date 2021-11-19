/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.apptools;
import snap.geom.Line;
import snap.geom.Shape;
import snap.gfx.Border;
import snap.gfx.Color;
import snap.gfx.Stroke;
import snap.util.SnapUtils;
import snap.view.*;
import snapcharts.app.ChartPane;
import snapcharts.model.*;

/**
 * A class to manage UI to edit a TraceStyle.
 */
public class TraceLineStyleInsp extends ChartPartInsp {

    /**
     * Constructor.
     */
    public TraceLineStyleInsp(ChartPane aChartPane)
    {
        super(aChartPane);
    }

    /**
     * Returns the name.
     */
    @Override
    public String getName()  { return "Line Style Settings"; }

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
        // Hide LineDashBox
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
    }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Get TraceStyle
        TraceStyle traceStyle = getTraceStyle(); if (traceStyle == null) return;

        // Reset ShowLineCheckBox
        boolean showLine = traceStyle.isShowLine();
        setViewValue("ShowLineCheckBox", showLine);

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

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get TraceStyle
        TraceStyle traceStyle = getTraceStyle(); if (traceStyle == null) return;

        // Handle ShowLineCheckBox
        if (anEvent.equals("ShowLineCheckBox")) {
            boolean showLine = anEvent.getBoolValue();
            traceStyle.setShowLine(showLine);
            if (!showLine)
                traceStyle.setShowPoints(true);
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
}