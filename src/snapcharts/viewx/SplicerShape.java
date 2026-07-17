package snapcharts.viewx;
import snap.geom.*;

/**
 * This is a shape that wraps around another shape and can provide parts.
 */
class SplicerShape extends Shape {

    // The original shape
    private PathWithArcLengths _path;

    // The start parametric ratio
    private double _start;

    // The end parametric ratio
    private double _end;

    // The tail point
    private Point _tailPoint = Point.ZERO;

    // The tail angle
    private double _tailAngle = Double.NaN;

    /**
     * Constructor.
     */
    public SplicerShape(Shape aShape, double aStart, double anEnd)
    {
        _path = aShape instanceof PathWithArcLengths ? (PathWithArcLengths) aShape : new PathWithArcLengths(aShape);
        _start = aStart;
        _end = anEnd;
    }

    /**
     * Returns the tail point.
     */
    public Point getTailPoint()  { return _tailPoint; }

    /**
     * Returns the tail angle.
     */
    public double getTailAngle()  { return !Double.isNaN(_tailAngle) ? _tailAngle : 0; }

    /**
     * Override to return iterator.
     */
    @Override
    public PathIter getPathIter(Transform aTrans)
    {
        // Get pathIter from shape (just return if start/end at ends)
        PathIter pathIter = _path.getPathIter(aTrans);
        if (_start <= 0 && _end >= 1)
            return pathIter;

        // Create SplicerIter and return
        return new SplicerIter(aTrans);
    }

    /**
     * A PathIter for splicer.
     */
    private class SplicerIter extends PathIter {

        // The total length of shape
        private double  _lenAll;

        // The max length
        private double  _lenMax;

        // The current running length
        private double  _lenRun;

        // Total number of original path Segs
        private int  _segCount;

        // Current path Seg index
        private int  _segIndex;

        /**
         * Constructor.
         */
        public SplicerIter(Transform aTrans)
        {
            super(aTrans);
            _segCount = _path.getSegCount();
            _lenAll = _path.getArcLength();
            _lenMax = _lenAll * _end;
            if (_start > 0)
                System.err.println("SplicerShape.SplicerIter: Start value not supported yet");
        }

        /**
         * Return true if at end of range or out of Segs.
         */
        @Override
        public boolean hasNext()  { return _segIndex < _segCount; }

        /**
         * Return next Seg and points, trimming if at end of range.
         */
        @Override
        public Seg getNext(double[] coords)
        {
            // Get seg and points (just return if zero length - (MoveTo or Close))
            Seg seg = _path.getSegAndPointsForIndex(_segIndex, coords, _trans);
            if (seg == Seg.MoveTo || seg == Seg.Close) {
                _segIndex++;
                return seg;
            }

            // If running length plus current seg length under max, just return full seg
            double len = _path.getArcLengthForSegIndex(_segIndex);
            if (_lenRun + len <= _lenMax) {
                _lenRun += len;
                _segIndex++;
                return seg;
            }

            // Get length remainder and ratio of final seg
            double lenRem = _lenMax - _lenRun;
            double segRatio = lenRem / len;

            // Get partial segment for ratio of final seg
            Segment segment = _path.getSegment(_segIndex, _trans);
            segment.split(segRatio);
            segment.getEndCoords(coords);

            // Get Tail angle
            Segment.PointAndAngle pointAndAngle = segment.getPointAndAngle(1);
            _tailPoint = _trans == null ? pointAndAngle.point() : _trans.transformXY(pointAndAngle.x(), pointAndAngle.y());
            _tailAngle = pointAndAngle.angle();

            // Set finished and return
            _lenRun = _lenMax;
            _segIndex = _segCount;
            return seg;
        }
    }

    /**
     * This extended Path2D provides support for ArcLength measurement.
     */
    private static class PathWithArcLengths extends Path2D {

        // The total arc length of path
        private double _arcLength;

        // The arc lengths for each seg
        private double[] _arcLengths;

        /**
         * Constructor.
         */
        public PathWithArcLengths(Shape aShape)
        {
            super(aShape);
        }

        /**
         * Returns the segment at index.
         */
        public Segment getSegment(int anIndex, Transform aTrans)
        {
            double[] points = new double[8];
            Seg seg = getSegAndSegmentPointsForIndex(anIndex, points, aTrans);
            return Segment.newSegmentForSegAndPoints(seg, points);
        }

        /**
         * Returns all points (start/control/end) for a given seg index, by copying to given array (should be length of 8).
         */
        public Seg getSegAndSegmentPointsForIndex(int anIndex, double[] theCoords, Transform aTrans)
        {
            Seg seg = getSeg(anIndex);
            int segPointIndex = getSegPointIndex(anIndex) * 2 - 2;

            switch (seg) {

                // Handle Close special: PointIndex is to index in ClosePointIndexes (an array of each close {start,end} index)
                case Close -> {

                    // Copy last point
                    theCoords[0] = _points[segPointIndex];
                    theCoords[1] = _points[segPointIndex + 1];

                    // Get previous MoveTo SegIndex
                    int moveToSegIndex = anIndex;
                    while (moveToSegIndex > 0 && getSeg(moveToSegIndex) != Seg.MoveTo)
                        moveToSegIndex--;

                    // Get MoveTo PointIndex and copy points
                    int moveToPointIndex = getSegPointIndex(moveToSegIndex) * 2;
                    theCoords[2] = _points[moveToPointIndex];
                    theCoords[3] = _points[moveToPointIndex + 1];
                }

                // Handle MoveTo: Probably not used, but copy move to point
                case MoveTo -> System.arraycopy(_points, segPointIndex + 2, theCoords, 0, 2);

                // Copy Seg points to given point coord array
                default -> {
                    int pointCount = seg.getCount() + 1;
                    System.arraycopy(_points, segPointIndex, theCoords, 0, pointCount * 2);
                }
            }

            // If Transform, transform
            if (aTrans != null)
                aTrans.transformXYArray(theCoords, seg.getCount() + 1);

            // Return
            return seg;
        }

        /**
         * Returns the total arc length of path.
         */
        public double getArcLength()
        {
            if (_arcLengths != null) return _arcLength;
            getArcLengths();
            return _arcLength;
        }

        /**
         * Returns the arc length of path segment at index.
         */
        public double getArcLengthForSegIndex(int anIndex)
        {
            double[] arcLens = getArcLengths();
            return arcLens[anIndex];
        }

        /**
         * Returns the array of arc lengths for all path segments.
         */
        private double[] getArcLengths()
        {
            if (_arcLengths != null) return _arcLengths;

            int segCount = getSegCount();
            double[] arcLengths = new double[segCount];
            double[] points = new double[8];
            double arcLength = 0;

            for (int i = 0; i < segCount; i++) {
                Seg seg = getSegAndSegmentPointsForIndex(i, points, null);
                double arcLen = seg.getArcLengthForPoints(points);
                arcLengths[i] = arcLen;
                arcLength += arcLen;
            }

            _arcLength = arcLength;
            return _arcLengths = arcLengths;
        }

        /**
         * Override to clear ArcLengths.
         */
        @Override
        protected void shapeChanged()  { super.shapeChanged(); _arcLengths = null; }
    }
}
