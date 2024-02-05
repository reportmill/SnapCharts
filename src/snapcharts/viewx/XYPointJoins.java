package snapcharts.viewx;
import snap.geom.*;
import snapcharts.charts.PointJoin;
import snapcharts.view.TraceView;

/**
 * This class holds PathIters for PointJoins
 */
public class XYPointJoins {

    /**
     * Returns the PathIter for a PointJoin.
     */
    public static PathIter getPathIterForPointJoin(PointJoin pointJoin, PathIter pathIter, TraceView aTraceView)
    {
        // For Other PointJoints, wrap in special PathIter to turn line segments into specified join
        switch (pointJoin) {
            case Line: return pathIter;
            case StepHV: return new StepHVPathIter(pathIter);
            case StepVH: return new StepVHPathIter(pathIter);
            case StepHVH: return new StepHVHPathIter(pathIter);
            case Spline: return new SplinePathIter(pathIter);
            case Y0Between: return new Y0BetweenPathIter(pathIter, aTraceView);
            default:
                System.err.println("DataLineShape.getPathIter: Unknown PointJoint: " + pointJoin);
                return pathIter;
        }
    }

    /**
     * A PathIter for PointJoin.HV that turns LineTos into two segments.
     */
    public static class StepHVPathIter extends PathIter {

        // The original PathIter
        protected PathIter _pathIter;

        // Next segment
        protected Seg _nextSeg;

        // Next segment coords
        protected double[]  _nextCoords = new double[6];

        // Next segment step
        protected int  _stepCount;

        // The last X/Y
        protected double  _lastX, _lastY;

        // The next X/Y
        protected double  _nextX, _nextY;

        /**
         * Constructor.
         */
        public StepHVPathIter(PathIter aPathIter)
        {
            _pathIter = aPathIter;
        }

        /**
         * Override for HVPathIter.
         */
        @Override
        public boolean hasNext()
        {
            // If still processing NextSeg, return true
            if (_nextSeg != null)
                return true;

            // Get original HasNext - if false, just return false
            boolean hasNext = _pathIter.hasNext();
            if (!hasNext)
                return false;

            // Otherwise get next seg
            _nextSeg = _pathIter.getNext(_nextCoords);
            _nextX = _nextCoords[0];
            _nextY = _nextCoords[1];
            _stepCount = 0;
            return true;
        }

        /**
         * Override.
         */
        @Override
        public Seg getNext(double[] coords)
        {
            // Handle LineTo (expected/common case)
            if (_nextSeg == Seg.LineTo) {

                // If first derived step, bump step count and return H line
                if (_stepCount == 0) {
                    _stepCount++;
                    return lineTo(_nextX, _lastY, coords);
                }

                // Otherwise clear NextSeg, update LastX/Y, and return line to original LineTo point
                _nextSeg = null;
                return lineTo(_lastX = _nextX, _lastY = _nextY, coords);
            }

            // If MoveTo, just pass along
            if (_nextSeg == Seg.MoveTo) {
                _nextSeg = null;
                return moveTo(_lastX = _nextCoords[0], _lastY = _nextCoords[1], coords);
            }

            // If Close, just pass along
            if (_nextSeg == Seg.Close) {
                _nextSeg = null;
                return close();
            }

            // Otherwise complain but forward on (should never happen)
            System.err.println("HVPathIter.getNext: Unexpected Seg Type: " + _nextSeg);
            Seg nextSeg = _nextSeg;
            _nextSeg = null;
            int segCount = nextSeg.getCount();
            System.arraycopy(_nextCoords, 0, coords, 0, segCount * 2);
            _lastX = coords[segCount * 2 - 2];
            _lastY = coords[segCount * 2 - 1];
            return nextSeg;
        }
    }

    /**
     * A PathIter for PointJoin.VH that turns LineTos into two segments.
     */
    public static class StepVHPathIter extends StepHVPathIter {

        /**
         * Constructor.
         */
        public StepVHPathIter(PathIter aPathIter)
        {
            super(aPathIter);
        }

        /**
         * Override.
         */
        @Override
        public Seg getNext(double[] coords)
        {
            // If LineTo, just pass along
            if (_nextSeg == Seg.LineTo) {

                // If first derived step, bump step count and return H line
                if (_stepCount == 0) {
                    _stepCount++;
                    return lineTo(_lastX, _nextY, coords);
                }
            }

            // Do normal version
            return super.getNext(coords);
        }
    }

    /**
     * A PathIter for PointJoin.HVH that turns LineTos into three segments.
     */
    public static class StepHVHPathIter extends StepHVPathIter {

        /**
         * Constructor.
         */
        public StepHVHPathIter(PathIter aPathIter)
        {
            super(aPathIter);
        }

        /**
         * Override.
         */
        @Override
        public Seg getNext(double[] coords)
        {
            // If LineTo, just pass along
            if (_nextSeg == Seg.LineTo) {

                // If first derived step, bump step count and return H line to mid point
                if (_stepCount == 0) {
                    _stepCount++;
                    double midX = _lastX + (_nextX - _lastX) / 2;
                    return lineTo(_lastX = midX, _lastY, coords);
                }

                // If second derived step, bump step count and return V line to mid point
                if (_stepCount == 1) {
                    _stepCount++;
                    return lineTo(_lastX, _nextY, coords);
                }
            }

            // Do normal version
            return super.getNext(coords);
        }
    }

    /**
     * A PathIter for PointJoin.Y0Between that turns LineTos into three segments.
     */
    public static class Y0BetweenPathIter extends StepHVPathIter {

        // The y value in display coords for dataY == 0
        private double  _zeroDispY;

        /**
         * Constructor.
         */
        public Y0BetweenPathIter(PathIter aPathIter, TraceView aTraceView)
        {
            super(aPathIter);

            // Calculate display Y for data Y == 0
            _zeroDispY = aTraceView.dataToViewY(0);
            Transform xfm = aPathIter.getTransform();
            if (xfm != null && !xfm.isIdentity())
                _zeroDispY = xfm.transformY(0, _zeroDispY);
        }

        /**
         * Override.
         */
        @Override
        public Seg getNext(double[] coords)
        {
            // If LineTo, just pass along
            if (_nextSeg == Seg.LineTo) {

                // If first derived step, lineTo 25% next X and zero Y
                if (_stepCount == 0) {
                    _stepCount++;
                    double f25 = _lastX + (_nextX - _lastX) * .25;
                    return lineTo(f25, _zeroDispY, coords);
                }

                // If second derived step, lineTo 25% next X and zero Y
                if (_stepCount == 1) {
                    _stepCount++;
                    double f75 = _lastX + (_nextX - _lastX) * .75;
                    return lineTo(f75, _zeroDispY, coords);
                }
            }

            // Do normal version
            return super.getNext(coords);
        }
    }

    /**
     * A PathIter for PointJoin.Spline that turns LineTos into CurveTo segments.
     */
    public static class SplinePathIter extends PathIter {

        // The original PathIter
        protected PathIter _pathIter;

        // Next 3 segments
        protected Seg _seg0, _seg1, _seg2;

        // Next 3 segment points
        protected Point  _segPoint0 = new Point(), _segPoint1 = new Point(), _segPoint2 = new Point();

        // Next 2 segment slopes
        protected Vector _segVector0 = new Vector(), _segVector1 = new Vector();

        // Next segment coords
        protected double[]  _nextCoords = new double[6];

        // Whether Spline needs a moveTo
        protected boolean  _needsMoveTo = true;

        /**
         * Constructor.
         */
        public SplinePathIter(PathIter aPathIter)
        {
            _pathIter = aPathIter;

            // Fill the Seg pipe
            if (_pathIter.hasNext())
                while (_seg0 == null)
                    doCycle();
        }

        /**
         * Override for SplinePathIter.
         */
        @Override
        public boolean hasNext()
        {
            return _seg1 != null;
        }

        /**
         * Override.
         */
        @Override
        public Seg getNext(double[] coords)
        {
            // If MoveTo is needed, return that
            if (_needsMoveTo) {
                _needsMoveTo = false;
                return moveTo(_segPoint0.x, _segPoint0.y, coords);
            }

            // Get line points
            double x0 = _segPoint0.x;
            double y0 = _segPoint0.y;
            double x1 = _segPoint1.x;
            double y1 = _segPoint1.y;

            // Constant for distance down line
            double lambda = .5;

            // Get point on line at lambda, and get distance of that sub-line projected onto slope at point0
            double lambdaX0 = x0 + (x1 - x0) * lambda;
            double lambdaY0 = y0 + (y1 - y0) * lambda;
            double dist0 = Vector.getProjectedDistance(lambdaX0 - x0, lambdaY0 - y0, _segVector0.x, _segVector0.y);
            dist0 = Math.abs(dist0);

            // Set control point 1 to same distance along vector at point 0
            double cp0x = x0 + _segVector0.x * dist0;
            double cp0y = y0 + _segVector0.y * dist0;

            // Get point on line at (inverse) lambda, and get distance of that sub-line projected onto slope at point1
            double lambdaX1 = x0 + (x1 - x0) * (1 - lambda);
            double lambdaY1 = y0 + (y1 - y0) * (1 - lambda);
            double dist1 = Vector.getProjectedDistance(lambdaX1 - x1, lambdaY1 - y1, _segVector1.x, _segVector1.y);
            dist1 = Math.abs(dist1);

            // Set control point 2 to same distance along vector at point 1 (reverse direction)
            double cp1x = x1 - _segVector1.x * dist1;
            double cp1y = y1 - _segVector1.y * dist1;

            // Get cubicTo segment, doCycle for next point, return
            Seg cubicTo = cubicTo(cp0x, cp0y, cp1x, cp1y, x1, y1, coords);
            doCycle();
            return cubicTo;
        }

        /**
         * Shifts the staged points by one and loads the next PathIter point.
         */
        private void doCycle()
        {
            // Shift Seg1 to Seg0
            _seg0 = _seg1;
            _segPoint0.setPoint(_segPoint1);
            _segVector0.setXY(_segVector1);

            // Shift Seg2 to Seg1
            _seg1 = _seg2;
            _segPoint1.setPoint(_segPoint2);

            // Clear Seg2
            _seg2 = null;

            // Get original HasNext - if true, reload Seg2
            boolean hasNext = _pathIter.hasNext();
            if (hasNext) {
                _seg2 = _pathIter.getNext(_nextCoords);
                _segPoint2.setXY(_nextCoords[0], _nextCoords[1]);
                if (_seg2 != Seg.LineTo && !_needsMoveTo)
                    System.err.println("SplinePathIter.doCycle: Unexpected Seg type: " + _seg2);
            }

            // Set the slope vector at point 1
            Point p2 = _seg2 != null ? _segPoint2 : _seg1 != null ? _segPoint1 : _segPoint0;
            Point p0 = _seg0 != null ? _segPoint0 : _seg1 != null ? _segPoint1 : _segPoint2;
            _segVector1.x = (p2.x - p0.x);
            _segVector1.y = (p2.y - p0.y);
            _segVector1.normalize();

            // If Y value flips (not continuously increasing or decreasing), just use slope 0 for smoother splines
            if (_seg0 != null && _seg1 != null && _seg2 != null && (_segPoint1.y - _segPoint0.y) * (_segPoint2.y - _segPoint1.y) < 0)
                _segVector1.y = 0;
        }
    }
}
