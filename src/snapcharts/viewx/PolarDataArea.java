/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.geom.*;
import snap.gfx.Color;
import snap.gfx.Painter;
import snap.gfx.Stroke;
import snapcharts.data.DataStore;
import snapcharts.data.DataStoreImpl;
import snapcharts.data.DataType;
import snapcharts.model.*;
import snapcharts.view.*;

/**
 * A DataArea subclass to display Polar charts.
 */
public class PolarDataArea extends DataArea {

    // The Polar ChartHelper
    private PolarChartHelper _polarHelper;

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
     * Returns Shape for painting dataset data line.
     */
    public Shape getDataLineShape()
    {
        return new DataLineShape();
    }

    /**
     * Override to do polar data to display coords conversion.
     */
    @Override
    protected DataStore getDisplayDataImpl()
    {
        // Get DataSet.PolarData
        DataSet dataSet = getDataSet();
        DataStore polarData = dataSet.getPolarData();
        int pointCount = polarData.getPointCount();

        // Get whether to convert to radians
        boolean convertToRadians = dataSet.getThetaUnit() != DataStore.ThetaUnit.Radians;

        // Create points array
        double[] dispX = new double[pointCount];
        double[] dispY = new double[pointCount];

        // Iterate over polar data points and covert/set in display points
        for (int i = 0; i < pointCount; i++) {

            // Get Theta and Radius
            double dataTheta = polarData.getT(i);
            double dataRad = polarData.getR(i);
            if (convertToRadians)
                dataTheta = Math.toRadians(dataTheta);

            // Convert to display coords
            dispX[i] = _polarHelper.polarDataToView(AxisType.X, dataTheta, dataRad);
            dispY[i] = _polarHelper.polarDataToView(AxisType.Y, dataTheta, dataRad);
        }

        // Create DataStore for points and return
        return new DataStoreImpl(DataType.XY, dispX, dispY);
    }

    /**
     * Paints chart.
     */
    @Override
    protected void paintDataArea(Painter aPntr)
    {
        // Get area
        double areaW = getWidth();
        double areaH = getHeight();

        // Get DataSet list
        DataSet dset = getDataSet();

        // Get Selection, Reveal info
        DataSetPoint selPoint = getChartView().getTargDataPoint();
        boolean isSelected = selPoint != null && selPoint.getDataSet() == dset;
        double reveal = getReveal();

        // Get style info
        DataStyle dataStyle = dset.getDataStyle();
        boolean showLine = dataStyle.isShowLine();
        Stroke dataStroke = dataStyle.getLineStroke();
        Color dataColor = getDataColor();
        boolean showSymbols = dataStyle.isShowSymbols();

        // Get path - if Reveal is active, get path spliced
        Shape path = getDataLineShape();
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

            // Iterate over DisplayData points
            DataStore displayData = getDisplayData();
            int pointCount = displayData.getPointCount();
            for (int i = 0; i < pointCount; i++) {
                double dispX = displayData.getX(i);
                double dispY = displayData.getY(i);

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
        DataSetPoint selDataPoint = getChartView().getTargDataPoint();
        int selIndex = selDataPoint.getIndex();

        // Get data X/Y and disp X/Y
        DataStore displayData = getDisplayData();
        double dispX = displayData.getX(selIndex);
        double dispY = displayData.getY(selIndex);

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
     * @return
     */
    @Override
    public DataSetPoint getDataPointForLocalXY(double aX, double aY)
    {
        // Constant for maximum display distance (in points)
        int MAX_SELECT_DISTANCE = 60;

        // Get data info
        DataStore displayData = getDisplayData();
        int pointCount = displayData.getPointCount();
        DataSetPoint dataPoint = null;
        double dist = MAX_SELECT_DISTANCE;

        // Iterate over points and get closest DataPoint
        for (int j = 0; j < pointCount; j++) {
            double dispX = displayData.getX(j);
            double dispY = displayData.getY(j);
            double dst = Point.getDistance(aX, aY, dispX, dispY);
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
     * @param aDP
     */
    @Override
    public Point getLocalXYForDataPoint(DataSetPoint aDP)
    {
        int index = aDP.getIndex();
        DataStore displayData = getDisplayData();
        if (index >= displayData.getPointCount())
            return new Point();
        double dispX = displayData.getX(index);
        double dispY = displayData.getY(index);
        return new Point(dispX, dispY);
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
     * A Shape implementation to display this PolarDataArea.DisplayData as data line.
     */
    private class DataLineShape extends Shape {

        /**
         * Return DataLinePathIter.
         */
        @Override
        public PathIter getPathIter(Transform aTransform)
        {
            return new DataLinePathIter(aTransform, PolarDataArea.this);
        }
    }

    /**
     * A PathIter implementation to display DataArea.DisplayData as data line.
     */
    private static class DataLinePathIter extends PathIter {

        // The X/Y display coords arrays
        private double[] _dispX, _dispY;

        // The DisplayData.PointCount
        private int _count;

        // The index
        private int _index;

        /**
         * Constructor.
         */
        public DataLinePathIter(Transform aTrans, DataArea aDataArea)
        {
            super(aTrans);

            // Get/set display points
            DataStore displayData = aDataArea.getDisplayData();
            _dispX = displayData.getDataX();
            _dispY = displayData.getDataY();
            _count = displayData.getPointCount();
        }

        /**
         * Returns whether there are remaining segments.
         */
        @Override
        public boolean hasNext()
        {
            return _index < _count;
        }

        /**
         * Returns next segment.
         */
        @Override
        public Seg getNext(double[] coords)
        {
            // Get next display X/Y coords
            double dispX = _dispX[_index];
            double dispY = _dispY[_index];

            // First segment is moveTo, then lineTos
            if (_index++ == 0)
                return moveTo(dispX, dispY, coords);
            return lineTo(dispX, dispY, coords);
        }
    }
}