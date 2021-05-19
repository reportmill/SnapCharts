package snapcharts.viewx;
import snap.geom.*;
import snapcharts.model.*;
import snapcharts.view.DataArea;

/**
 * A class to paint the data line for an XY chart.
 */
public class XYPainter {

    // The DataArea
    private DataArea  _dataArea;

    // The shape to draw data line
    private Shape  _dataLineShape;

    // The shape to fill data area
    private Shape  _dataAreaShape;

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
     * Returns the shape to draw data line.
     */
    public Shape getDataLineShape()
    {
        // If already set, just return
        if (_dataLineShape != null) return _dataLineShape;

        // Create basic data shape from display coord arrays
        RawData dispData = _dataArea.getDispData();
        double[] dispX = dispData.getDataX();
        double[] dispY = dispData.getDataY();
        Shape dataLineShape = new XYDisplayCoordsShape(dispX, dispY);

        // Set/return
        return _dataLineShape = dataLineShape;
    }

    /**
     * Returns the shape to fill data area.
     */
    public Shape getDataAreaShape()
    {
        // If already set, just return
        if (_dataAreaShape != null) return _dataAreaShape;

        // Get data line shape and area bounds
        Shape dataLineShape = getDataLineShape();
        double areaW = _dataArea.getWidth();
        double areaH = _dataArea.getHeight();

        // Close path
        RawData dispData = _dataArea.getDispData();
        double[] dispX = dispData.getDataX();
        double[] dispY = dispData.getDataY();
        int pointCount = dispX.length;
        double point0y = dispY[0];
        double pointLastY = dispY[pointCount-1];
        Path2D path = new Path2D(dataLineShape);
        path.lineTo(areaW, pointLastY);
        path.lineTo(areaW, areaH);
        path.lineTo(0, areaH);
        path.lineTo(0, point0y);
        path.close();

        // Set/return
        return _dataAreaShape = path;
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
        RawData dispData = _dataArea.getDispData();
        double[] dispX = dispData.getDataX();
        double[] dispY = dispData.getDataY();
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
