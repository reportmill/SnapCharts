package snapcharts.viewx;
import snap.geom.*;
import snapcharts.model.*;
import snapcharts.view.AxisView;
import snapcharts.view.AxisViewX;
import snapcharts.view.ChartHelper;
import snapcharts.view.DataArea;

/**
 * A class to paint the data line for an XY chart.
 */
public class XYPainter {

    // The DataArea
    private DataArea  _dataArea;

    // The RawData
    private RawData  _rawData;

    // The display X/Y coords
    private double[]  _dispX, _dispY;

    // The Dataline shape
    private Shape  _dataShape;

    // The combined length of all display point path segments
    private double _arcLen;

    // An array of all segment arc lengths
    private double[] _arcLens;

    /**
     * Constructor.
     */
    public XYPainter(DataArea aDataArea)
    {
        _dataArea = aDataArea;
    }

    /**
     * Returns the number of display points.
     */
    public int getDispPointCount()
    {
        double[] dispX = getDispX();
        return dispX.length;
    }

    /**
     * Returns the X display coord at given index.
     */
    public double getDispX(int anIndex)
    {
        double[] dispX = getDispX();
        return dispX[anIndex];
    }

    /**
     * Returns the Y display coord at given index.
     */
    public double getDispY(int anIndex)
    {
        double[] dispY = getDispY();
        return dispY[anIndex];
    }

    /**
     * Returns X display coords array.
     */
    private double[] getDispX()
    {
        if (_dispX == null) loadDisplayCoords();
        return _dispX;
    }

    /**
     * Returns Y display coords array.
     */
    private double[] getDispY()
    {
        if (_dispX == null) loadDisplayCoords();
        return _dispY;
    }

    /**
     * Returns the RawData.
     */
    private RawData getRawData()
    {
        // If already set, just return
        if (_rawData != null) return _rawData;

        // Get DataSet and RawData
        DataSet dataSet = _dataArea.getDataSet();
        RawData rawData = dataSet.getProcessedData();

        // If WrapAxis, wrap RawData inside RawDataWrapper for wrap range and axis range
        AxisViewX axisViewX = _dataArea.getAxisViewX();
        Axis axisX = axisViewX.getAxis();
        if (axisX.isWrapAxis()) {
            double wrapMin = axisX.getWrapMinMax().getMin();
            double wrapMax = axisX.getWrapMinMax().getMax();
            double axisMin = axisViewX.getAxisMin();
            double axisMax = axisViewX.getAxisMax();
            rawData = new RawDataWrapper(rawData, wrapMin, wrapMax, axisMin, axisMax);
        }

        // Set/return
        return _rawData = rawData;
    }

    /**
     * Loads the display coords.
     */
    private void loadDisplayCoords()
    {
        RawData rawData = getRawData();
        int pointCount = rawData.getPointCount();
        double[] dispX = new double[pointCount];
        double[] dispY = new double[pointCount];

        // Get ChartHelper and AxisViews
        ChartHelper chartHelper = _dataArea.getChartHelper();
        AxisView axisViewX = _dataArea.getAxisViewX();
        AxisView axisViewY = _dataArea.getAxisViewY();

        // Iterate over data points
        for (int i = 0; i < pointCount; i++) {

            // Get data X/Y and disp X/Y
            double dataX = rawData.getX(i);
            double dataY = rawData.getY(i);
            dispX[i] = chartHelper.dataToView(axisViewX, dataX);
            dispY[i] = chartHelper.dataToView(axisViewY, dataY);
        }

        // Set/return
        _dispX = dispX;
        _dispY = dispY;
    }

    /**
     * Returns the XY data line shape.
     */
    public Shape getDataShape()
    {
        // If already set, just return
        if (_dataShape != null) return _dataShape;

        // Create basic data shape from display coord arrays
        double[] dispX = getDispX();
        double[] dispY = getDispY();
        Shape dataShape = new XYDisplayCoordsShape(dispX, dispY);

        // If area, close path
        boolean isArea = _dataArea.getChartType() == ChartType.AREA;
        if (isArea) {
            double areaW = _dataArea.getWidth();
            double areaH = _dataArea.getHeight();
            int pointCount = dispX.length;
            double point0y = dispY[0];
            double pointLastY = dispY[pointCount-1];
            Path2D path = new Path2D(dataShape);
            path.lineTo(areaW, pointLastY);
            path.lineTo(areaW, areaH);
            path.lineTo(0, areaH);
            path.lineTo(0, point0y);
            path.close();
            dataShape = path;
        }

        // Set/return
        return _dataShape = dataShape;
    }

    /**
     * Returns the combined length of all display point path segments.
     */
    public double getArcLength()
    {
        if (_arcLen <= 0) getArcLengths();
        return _arcLen;
    }

    /**
     * Returns an array of all segment arc lengths.
     */
    public double[] getArcLengths()
    {
        // If already set just return
        if (_arcLens != null) return _arcLens;

        // Get Display coords and count
        double[] dispX = getDispX();
        double[] dispY = getDispY();
        int pointCount = dispX.length;

        // Iterate over data points
        double[] arcLens = new double[pointCount];
        double arcLenTotal = 0;
        for (int i = 0; i < pointCount; i++) {
            if (i > 0) {
                arcLens[i] = Point.getDistance(dispX[i - 1], dispY[i - 1], dispX[i], dispY[i]);
                arcLenTotal += arcLens[i];
            }
        }

        // Set/return
        _arcLen = arcLenTotal;
        return _arcLens = arcLens;
    }

    /**
     * A Shape implementation to display current Painter display coords.
     */
    private class XYDisplayCoordsShape extends Shape {

        // The X/Y display coords arrays
        private double[]  _dispX, _dispY;

        /**
         * Constructor.
         */
        public XYDisplayCoordsShape(double[] dispX, double[] dispY)
        {
            _dispX = dispX;
            _dispY = dispY;
        }

        @Override
        public PathIter getPathIter(Transform aT)
        {
            return new XYDisplayCoordsPathIter(aT, _dispX, _dispY);
        }
    }

    /**
     * A PathIter implementation to for synced array of polygon XY coords.
     */
    private class XYDisplayCoordsPathIter extends PathIter {

        // The X/Y display coords arrays
        private double[]  _dispX, _dispY;

        // The count
        private int  _count;

        // The index
        private int  _index;

        /**
         * Constructor.
         */
        public XYDisplayCoordsPathIter(Transform aTrans, double[] dispX, double[] dispY)
        {
            super(aTrans);
            _dispX = dispX;
            _dispY = dispY;
            _count = dispX.length;
        }

        @Override
        public boolean hasNext()  { return _index < _count; }

        @Override
        public Seg getNext(double[] coords)
        {
            double dispX = _dispX[_index];
            double dispY = _dispY[_index];
            if (_index++ == 0)
                return moveTo(dispX, dispY, coords);
            return lineTo(dispX, dispY, coords);
        }
    }
}
