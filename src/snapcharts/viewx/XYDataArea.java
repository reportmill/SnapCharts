/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.geom.*;
import snap.gfx.*;
import snap.util.PropChange;
import snapcharts.model.*;
import snapcharts.view.ChartHelper;
import snapcharts.view.DataArea;
import snapcharts.view.DataView;
import snapcharts.view.PointPainter;

/**
 * A DataArea subclass to display common XY ChartTypes: LINE, AREA, SCATTER.
 */
public class XYDataArea extends DataArea {

    // The XYPainter (an object to provide data line path/shape)
    private XYPainter  _xyPainter = new XYPainter(this);

    // The TailShape
    private Shape  _tailShape;

    // A PointPainter to handle painting symbols and tags
    private PointPainter  _pointPainter = new PointPainter(this);

    // Constants for defaults
    protected static Stroke Stroke3 = new Stroke(3, Stroke.Cap.Round, Stroke.Join.Round, 0);
    protected static Stroke Stroke5 = new Stroke(5, Stroke.Cap.Round, Stroke.Join.Round, 0);

    /**
     * Constructor.
     */
    public XYDataArea(ChartHelper aChartHelper, DataSet aDataSet)
    {
        super(aChartHelper, aDataSet);
    }

    /**
     * Paints the DataArea (ChartType/DataSet specific painting).
     */
    @Override
    protected void paintDataArea(Painter aPntr)
    {
        // Get area bounds
        double areaW = getWidth();
        double areaH = getHeight();

        // Get whether DataArea/DataSet is selected or targeted
        boolean isSelected = isSelectedOrTargeted();

        // Get DataStyle info
        DataStyle dataStyle = getDataStyle();
        boolean showLine = dataStyle.isShowLine();
        boolean showSymbols = dataStyle.isShowSymbols();
        boolean showArea = dataStyle.isShowArea();

        // Get DataColor, DataStroke
        Color dataColor = getDataColor();
        Stroke dataStroke = dataStyle.getLineStroke();

        // If reveal is not full (1) then clip
        double reveal = getReveal();
        if (reveal < 1 && (showSymbols || showArea)) {
            aPntr.save();
            aPntr.clipRect(0, 0, areaW * reveal, areaH);
        }

        // If ShowArea, fill path, too
        if (showArea) {
            Shape dataAreaShape = _xyPainter.getDataAreaShape();
            Color dataAreaColor = dataStyle.getFillColor();
            aPntr.setColor(dataAreaColor);
            aPntr.fill(dataAreaShape);
        }

        // Get dataShape (path) (if Reveal is active, get shape as SplicerShape so we can draw partial/animated)
        Shape dataShape = _xyPainter.getDataLineShape();
        if (reveal < 1 && showLine)
            dataShape = new SplicerShape(dataShape, 0, reveal);

        // If ChartType.LINE, draw path
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

        // If ChartType.LINE, draw path
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

        // If ShowSymbols, paint symbols
        if (showSymbols)
            paintSymbols(aPntr);

        // If reveal not full, resture gstate
        if (reveal < 1 && (showSymbols || showArea))
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
     * Paints tags for DataSet.
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
        DataPoint targPoint = getTargDataPoint();
        DataPoint dataPoint = targPoint != null ? targPoint : getSelDataPoint();
        if (dataPoint == null)
            return;

        // Get info
        DataSet dataSet = getDataSet();
        int pointIndex = dataPoint.getIndex();

        // Get data X/Y and disp X/Y
        double dataX = dataSet.getX(pointIndex);
        double dataY = dataSet.getY(pointIndex);
        double dispX = dataToViewX(dataX);
        double dispY = dataToViewY(dataY);

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
        DataStyle dataStyle = dataSet.getDataStyle();
        boolean showSymbols = dataStyle.isShowSymbols();
        if (!showSymbols) {
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
        // If not Line chart or DataSet.Disabled, return default
        DataStyle dataStyle = getDataStyle();
        boolean showSymbolsOrFill = dataStyle.isShowSymbols() || dataStyle.isShowArea();
        if (showSymbolsOrFill || getDataSet().isDisabled())
            return DataView.DEFAULT_REVEAL_TIME;

        // Calc factor to modify default time
        double maxLen = _xyPainter.getArcLength();
        double factor = Math.max(1, Math.min(maxLen / 500, 2));

        // Return default time times factor
        return (int) Math.round(factor * DataView.DEFAULT_REVEAL_TIME);
    }

    /**
     * Clears the DataPath.
     */
    private void clearDataPath()
    {
        _xyPainter = new XYPainter(this);
        repaint();
    }

    /**
     * Called when a ChartPart changes.
     */
    protected void chartPartDidChange(PropChange aPC)
    {
        // Do normal version
        super.chartPartDidChange(aPC);

        // Clear XYPainter
        Object src = aPC.getSource();
        if (src == getDataSet() || src instanceof Axis) {
            clearDataPath();
        }
    }

    /**
     * Called when DataView changes size.
     */
    @Override
    protected void dataViewDidChangeSize()
    {
        // Do normal version
        super.dataViewDidChangeSize();

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