/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.geom.*;
import snap.gfx.*;
import snap.props.PropChange;
import snapcharts.charts.*;
import snapcharts.view.ChartHelper;
import snapcharts.view.TraceView;
import snapcharts.view.ContentView;

/**
 * A TraceView subclass to display TraceType Scatter.
 */
public class ScatterTraceView extends TraceView {

    // The TailShape
    private Shape  _tailShape;

    // A PointPainter to handle painting symbols and tags
    private PointPainter  _pointPainter = new PointPainter(this);

    // The arc length of the DataLineShape
    private double  _dataLineArcLength;

    // Constants for defaults
    protected static Stroke Stroke3 = new Stroke(3, Stroke.Cap.Round, Stroke.Join.Round, 0);
    protected static Stroke Stroke5 = new Stroke(5, Stroke.Cap.Round, Stroke.Join.Round, 0);

    /**
     * Constructor.
     */
    public ScatterTraceView(ChartHelper aChartHelper, Trace aTrace)
    {
        super(aChartHelper, aTrace);
    }

    /**
     * Returns the Shape used to paint the Trace line.
     */
    public Shape getTraceLineShape()
    {
        return ScatterTraceViewShapes.getLineShape(this, false);
    }

    /**
     * Returns the Shape used to paint the Trace filled area shape.
     */
    public Shape getTraceAreaShape()
    {
        return ScatterTraceViewShapes.getAreaShape(this);
    }

    /**
     * Returns the length of the trace line shape.
     */
    public double getTraceLineShapeArcLength()
    {
        if (_dataLineArcLength > 0) return _dataLineArcLength;
        Shape dataLineShape = ScatterTraceViewShapes.getLineShape(this, true);
        double arcLength = dataLineShape.getArcLength();
        return _dataLineArcLength = arcLength;
    }

    /**
     * Paints the TraceView (TraceType specific painting).
     */
    @Override
    protected void paintTrace(Painter aPntr)
    {
        // Get area bounds
        double areaW = getWidth();
        double areaH = getHeight();

        // Get whether TraceView/Trace is selected or targeted
        boolean isSelected = isSelectedOrTargeted();

        // Get Trace info
        Trace trace = getTrace();
        boolean showLine = trace.isShowLine();
        boolean showPoints = trace.isShowPoints();
        boolean showArea = trace.isShowArea();

        // Get DataColor, DataStroke
        Color dataColor = getDataColor();
        Stroke dataStroke = trace.getLineStroke();

        // If reveal is not full (1) then clip
        double reveal = getReveal();
        if (reveal < 1 && (showPoints || showArea)) {
            aPntr.save();
            aPntr.clipRect(0, 0, areaW * reveal, areaH);
        }

        // If ShowArea, fill path, too
        if (showArea) {
            Shape traceAreaShape = getTraceAreaShape();
            Color traceFillColor = trace.getFillColor();
            aPntr.setColor(traceFillColor);
            aPntr.fill(traceAreaShape);
        }

        // Get dataShape (path) (if Reveal is active, get shape as SplicerShape so we can draw partial/animated)
        Shape dataShape = getTraceLineShape();
        if (reveal < 1 && showLine)
            dataShape = new SplicerShape(dataShape, 0, reveal);

        // If ShowLine, draw path
        if (isSelected && showLine) {
            aPntr.setStrokePure(true);

            // If selected, draw path
            Color selColor = dataColor.blend(Color.CLEARWHITE, .75);
            Stroke selStroke = dataStroke.copyForWidth(dataStroke.getWidth() * 3 + 8).copyForDashes(null);
            aPntr.setColor(selColor);
            aPntr.setStroke(selStroke);
            aPntr.draw(dataShape);

            // If selected, draw path
            Color selColor2 = dataColor.blend(Color.WHITE, 1);
            Stroke selStroke2 = dataStroke.copyForWidth(dataStroke.getWidth() + 2);
            aPntr.setColor(selColor2);
            aPntr.setStroke(selStroke2);
            aPntr.draw(dataShape);

            aPntr.setStrokePure(false);
            aPntr.setColor(dataColor);
            aPntr.setStroke(dataStroke);
        }

        // Set color, stroke
        aPntr.setColor(dataColor);
        aPntr.setStroke(dataStroke);

        // If ShowLine, draw path
        if (showLine) {
            aPntr.setStrokePure(true);
            aPntr.draw(dataShape);
            aPntr.setStrokePure(false);
        }

        // If Reveal is active, paint TailShape
        if (dataShape instanceof SplicerShape)
            paintTailShape(aPntr, (SplicerShape) dataShape);

        // Paint selected point
        if (isSelected)
            paintSelDataPoint(aPntr);

        // If ShowPoints or ShowTags
        boolean showTags = trace.isShowTags();
        if (showPoints || showTags)
            _pointPainter.paintSymbolsAndTagsPrep();

        // If ShowPoints, paint symbols
        if (showPoints)
            paintSymbols(aPntr);

        // If reveal not full, resture gstate
        if (reveal < 1 && (showPoints || showArea))
            aPntr.restore();
    }

    /**
     * Paints symbols.
     */
    protected void paintSymbols(Painter aPntr)
    {
        _pointPainter.paintSymbols(aPntr);
    }

    /**
     * Paints tags for Trace.
     */
    @Override
    protected void paintDataTags(Painter aPntr)
    {
        _pointPainter.paintTags(aPntr);
    }

    /**
     * Paints selected point.
     */
    protected void paintSelDataPoint(Painter aPntr)
    {
        // Get targeted or selected datapoint (targeted takes precidence)
        TracePoint targPoint = getTargDataPoint();
        TracePoint dataPoint = targPoint != null ? targPoint : getSelDataPoint();
        if (dataPoint == null)
            return;

        // Get disp X/Y for DataPoint
        Point dispXY = getLocalXYForDataPoint(dataPoint);
        double dispX = dispXY.x;
        double dispY = dispXY.y;

        // Get data color and symbol
        Color dataColor = getDataColor();
        Symbol dataSymbol = getDataSymbol();
        double symbolOffset = dataSymbol.getSize() / 2d;
        Shape dataSymbolShape = dataSymbol.getShape().copyFor(new Transform(dispX - symbolOffset, dispY - symbolOffset));

        // Set color for glow effect
        aPntr.setColor(dataColor.blend(Color.CLEARWHITE, .5));
        double haloSize = dataSymbol.getSize() * 2 + 4;
        double haloOffset = haloSize / 2d;
        aPntr.fill(new Ellipse(dispX - haloOffset, dispY - haloOffset, haloSize, haloSize));

        // Paint large white outline
        aPntr.setStroke(Stroke5);
        aPntr.setColor(Color.WHITE);
        aPntr.draw(dataSymbolShape);

        // Paint selected symbol
        Trace trace = getTrace();
        boolean showPoints = trace.isShowPoints();
        if (!showPoints) {
            aPntr.setStroke(Stroke3);
            aPntr.setColor(dataColor);
            aPntr.draw(dataSymbolShape);
        }
    }

    /**
     * Paints the TailShape.
     */
    private void paintTailShape(Painter aPntr, SplicerShape splicer)
    {
        Point tailPoint = splicer.getTailPoint();
        double tailAngle = splicer.getTailAngle();
        Shape tailShape = getTailShape();
        Rect tailShapeBounds = tailShape.getBounds();
        double tailShapeX = tailPoint.x - tailShapeBounds.getMidX();
        double tailShapeY = tailPoint.y - tailShapeBounds.getMidY();
        aPntr.save();
        aPntr.rotateAround(tailAngle, tailPoint.x, tailPoint.y);
        aPntr.translate(tailShapeX, tailShapeY);
        aPntr.fill(tailShape);
        aPntr.restore();
    }

    /**
     * Returns the tail shape.
     */
    public Shape getTailShape()
    {
        // If already set, just return
        if (_tailShape!=null) return _tailShape;

        // Create/configure/set TailShape
        Path2D path = new Path2D();
        path.moveTo(0, 0);
        path.lineTo(16, 6);
        path.lineTo(0, 12);
        path.lineTo(5, 6);
        path.close();
        return _tailShape = path;
    }

    /**
     * Override to return RevealTime based on path length.
     */
    @Override
    protected int getRevealTime()
    {
        // If not Line chart or Trace.Disabled, return default
        Trace trace = getTrace();
        boolean showPointsOrArea = trace.isShowPoints() || trace.isShowArea();
        if (showPointsOrArea || getTrace().isDisabled())
            return ContentView.DEFAULT_REVEAL_TIME;

        // Calc factor to modify default time
        double maxLen = getTraceLineShapeArcLength();
        double factor = Math.max(1, Math.min(maxLen / 500, 2));

        // Return default time times factor
        return (int) Math.round(factor * ContentView.DEFAULT_REVEAL_TIME);
    }

    /**
     * Clears the DataPath.
     */
    private void clearDataPath()
    {
        _dataLineArcLength = 0;
        repaint();
    }

    /**
     * Called when a ChartPart changes.
     */
    protected void chartPartDidChange(PropChange aPC)
    {
        // Do normal version
        super.chartPartDidChange(aPC);

        // Clear cached data path info
        Object src = aPC.getSource();
        if (src == getTrace() || src instanceof Axis) {
            clearDataPath();
        }
    }

    /**
     * Called when ContentView changes size.
     */
    @Override
    protected void contentViewDidChangeSize()
    {
        // Do normal version
        super.contentViewDidChangeSize();

        // Clear DataPath
        clearDataPath();
    }

    /**
     * Called when AxisView changes properties.
     */
    @Override
    protected void axisViewDidChange(PropChange aPC)
    {
        // Do normal version
        super.axisViewDidChange(aPC);

        // Clear DataPath
        clearDataPath();
    }
}