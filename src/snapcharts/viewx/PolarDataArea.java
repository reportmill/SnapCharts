/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.geom.*;
import snap.gfx.Color;
import snap.gfx.Painter;
import snap.gfx.Stroke;
import snap.util.PropChange;
import snapcharts.model.*;
import snapcharts.modelx.PolarStyle;
import snapcharts.view.*;

/**
 * A DataArea subclass to display Polar charts.
 */
public class PolarDataArea extends DataArea {

    // The Polar ChartHelper
    private PolarChartHelper _polarHelper;

    // The Path2D for painting DataSet
    private Path2D  _dataPath;

    // The Data path points in display coords
    private Point[]  _dispPoints;

    // The TailShape
    private Shape  _tailShape;

    // Constants for defaults
    protected static Stroke Stroke3 = new Stroke(3, Stroke.Cap.Round, Stroke.Join.Round, 0);
    protected static Stroke Stroke5 = new Stroke(5, Stroke.Cap.Round, Stroke.Join.Round, 0);

    /**
     * Constructor.
     */
    public PolarDataArea(ChartHelper aChartHelper, DataSet aDataSet)
    {
        super(aChartHelper, aDataSet);
        _polarHelper = (PolarChartHelper) aChartHelper;
    }

    /**
     * Returns Path2D for painting dataset.
     */
    public Path2D getDataPath()
    {
        // If already set, just return
        if (_dataPath !=null) return _dataPath;

        // Get display points and create new path
        Point[] dispPoints = getDisplayPoints();
        Path2D path = new Path2D();

        // Iterate over data points
        for (int i = 0, iMax = dispPoints.length; i < iMax; i++) {
            Point point = dispPoints[i];
            if (i == 0)
                path.moveTo(point.x, point.y);
            else path.lineTo(point.x, point.y);
        }

        // Return path
        return _dataPath = path;
    }

    /**
     * Returns data line points in display coords.
     */
    public Point[] getDisplayPoints()
    {
        // If already set, just return
        if (_dispPoints != null) return _dispPoints;

        // Get dataset info
        DataSet dataSet = getDataSet();
        DataStore dataStore = dataSet.getPolarData();
        int pointCount = dataSet.getPointCount();
        AxisType axisTypeY = getAxisTypeY();

        // Get whether to convert to radians
        DataStyle dataStyle = dataSet.getDataStyle();
        PolarStyle polarStyle = dataStyle instanceof PolarStyle ? (PolarStyle) dataStyle : null;
        boolean convertToRadians = polarStyle != null && polarStyle.getThetaUnit() != PolarStyle.ThetaUnit.Radians;

        // Create points array
        Point[] dispPoints = new Point[pointCount];

        // Iterate over polar data points and covert/set in display points
        for (int j = 0; j < pointCount; j++) {

            // Get Theta and Radius
            double dataTheta = dataStore.getT(j);
            double dataRad = dataStore.getR(j);
            if (convertToRadians)
                dataTheta = Math.toRadians(dataTheta);

            // Convert to display coords
            double dispX = _polarHelper.polarDataToView(AxisType.X, dataTheta, dataRad);
            double dispY = _polarHelper.polarDataToView(axisTypeY, dataTheta, dataRad);
            dispPoints[j] = new Point(dispX, dispY);
        }

        // Set/return points
        return _dispPoints = dispPoints;
    }

    /**
     * Clears the DataPath.
     */
    private void clearDataPath()
    {
        _dataPath = null;
        _dispPoints = null;
        repaint();
    }

    /**
     * Paints chart.
     */
    protected void paintDataArea(Painter aPntr)
    {
        // Get area
        double areaW = getWidth();
        double areaH = getHeight();

        // Get DataSet list
        DataSet dset = getDataSet();

        // Get Selection, Reveal info
        DataPoint selPoint = getChartView().getTargDataPoint();
        boolean isSelected = selPoint != null && selPoint.getDataSet() == dset;
        double reveal = getReveal();

        // Get style info
        DataStyle dataStyle = dset.getDataStyle();
        boolean showLine = dataStyle.isShowLine();
        Stroke dataStroke = dataStyle.getLineStroke();
        Color dataColor = getDataColor();
        boolean showSymbols = dataStyle.isShowSymbols();

        // Get path - if Reveal is active, get path spliced
        Shape path = getDataPath();
        if (reveal < 1)
            path = new SplicerShape(path, 0, reveal);

        // Handle selected
        if (isSelected)
            dataStroke = dataStroke.copyForWidth(dataStroke.getWidth() + 1);

        // Set dataset color, stroke and paint
        aPntr.setColor(dataColor);
        aPntr.setStroke(dataStroke);

        // If ChartType.LINE, draw path
        if (showLine) {
            aPntr.setColor(dataColor.blend(Color.CLEAR, .98));
            aPntr.draw(path);
            aPntr.setColor(dataColor);
            aPntr.draw(path);
        }

        // If Reveal is active, paint end point
        if (path instanceof SplicerShape) {
            SplicerShape splicer = (SplicerShape) path;
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

        // If reveal is not full (1) then clip
        if (reveal < 1) {
            aPntr.save();
            aPntr.clipRect(0, 0, areaW * reveal, areaH);
        }

        // Draw dataset points
        if (showSymbols) {

            // Iterate over points
            Point[] points = getDisplayPoints();
            for (int i = 0, iMax = points.length; i < iMax; i++) {
                Point point = points[i];
                double dispX = point.x;
                double dispY = point.y;

                // Get symbol and color and paint
                Symbol symbol = getDataSymbol();
                double symbolOffset = symbol.getSize() / 2d;
                Shape symbolShape = symbol.getShape().copyFor(new Transform(dispX - symbolOffset, dispY - symbolOffset));
                aPntr.setColor(dataColor);
                aPntr.fill(symbolShape);
            }
        }

        // Paint selected point
        if (isSelected)
            paintSelPoint(aPntr);

        // If reveal not full, restore gstate
        if (reveal < 1)
            aPntr.restore();
    }

    /**
     * Paints selected point.
     */
    protected void paintSelPoint(Painter aPntr)
    {
        // Get info
        DataPoint selDataPoint = getChartView().getTargDataPoint();
        int selIndex = selDataPoint.getIndex();

        // Get data X/Y and disp X/Y
        Point[] points = getDisplayPoints();
        Point selPoint = points[selIndex];
        double dispX = selPoint.x;
        double dispY = selPoint.y;

        // Get data color and symbol
        Color dataColor = getDataColor();
        Symbol dataSymbol = getDataSymbol();
        double symbolOffset = dataSymbol.getSize() / 2d;
        Shape dataSymbolShape = dataSymbol.getShape().copyFor(new Transform(dispX - symbolOffset, dispY - symbolOffset));

        // Set color for glow effect
        aPntr.setColor(dataColor.blend(Color.CLEARWHITE, .5));
        aPntr.fill(new Ellipse(dispX - 10, dispY - 10, 20, 20));

        // Get symbol
        aPntr.setStroke(Stroke5);
        aPntr.setColor(Color.WHITE);
        aPntr.draw(dataSymbolShape);
        aPntr.setStroke(Stroke3);
        aPntr.setColor(dataColor);
        aPntr.draw(dataSymbolShape);
    }

    /**
     * Returns the data point closest to given x/y in local coords (null if none).
     */
    @Override
    public DataPoint getDataPointForLocalXY(double aX, double aY)
    {
        // Constant for maximum display distance (in points)
        int MAX_SELECT_DISTANCE = 60;

        // Get data info
        Point[] points = getDisplayPoints();
        int pointCount = points.length;
        DataPoint dataPoint = null;
        double dist = MAX_SELECT_DISTANCE;

        // Iterate over points and get closest DataPoint
        for (int j = 0; j < pointCount; j++) {
            Point point = points[j];
            double dst = Point.getDistance(aX, aY, point.x, point.y);
            if (dst < dist) {
                dist = dst;
                dataPoint = getDataSet().getPoint(j);
            }
        }

        // Return DataPoint
        return dataPoint;
    }

    /**
     * Returns the given data point X/Y in this view coords.
     */
    @Override
    public Point getLocalXYForDataPoint(DataPoint aDP)
    {
        int index = aDP.getIndex();
        Point[] displayPoints = getDisplayPoints();
        return index < displayPoints.length ? displayPoints[index] : new Point();
    }

    /**
     * Returns the tail shape.
     */
    public Shape getTailShape()
    {
        // If already set, just return
        if (_tailShape != null) return _tailShape;

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
     * Override to clear display points.
     */
    @Override
    protected void chartPartDidChange(PropChange aPC)
    {
        // Do normal version
        super.chartPartDidChange(aPC);

        // Handle Data changes
        Object src = aPC.getSource();
        if (src == getDataSet() || src instanceof Axis || src instanceof DataStyle) {
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
}