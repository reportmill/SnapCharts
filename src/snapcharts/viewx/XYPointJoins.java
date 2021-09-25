package snapcharts.viewx;
import snap.geom.PathIter;
import snap.geom.Seg;
import snapcharts.model.PointJoin;

/**
 * This class holds PathIters for PointJoins
 */
public class XYPointJoins {

    /**
     * Returns the PathIter for a PointJoin.
     */
    public static PathIter getPathIterForPointJoin(PointJoin pointJoin, PathIter pathIter)
    {
        // For Other PointJoints, wrap in special PathIter to turn line segments into specified join
        switch (pointJoin) {
            case Line: return pathIter;
            case HV: return new HVPathIter(pathIter);
            case VH: return new VHPathIter(pathIter);
            case HVH: return new HVHPathIter(pathIter);
            case Spline: return new SplinePathIter(pathIter);
            default:
                System.err.println("DataLineShape.getPathIter: Unknown PointJoint: " + pointJoin);
                return pathIter;
        }
    }

    /**
     * A PathIter for PointJoin.HV that turns LineTos into two segments.
     */
    public static class HVPathIter extends PathIter {

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
        public HVPathIter(PathIter aPathIter)
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
    public static class VHPathIter extends HVPathIter {

        /**
         * Constructor.
         */
        public VHPathIter(PathIter aPathIter)
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
    public static class HVHPathIter extends HVPathIter {

        /**
         * Constructor.
         */
        public HVHPathIter(PathIter aPathIter)
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
     * A PathIter for PointJoin.Spline that turns LineTos into CurveTo segments.
     */
    public static class SplinePathIter extends HVPathIter {

        /**
         * Constructor.
         */
        public SplinePathIter(PathIter aPathIter)
        {
            super(aPathIter);
        }

        /**
         * Override.
         */
        @Override
        public Seg getNext(double[] coords)
        {
            // If LineTo, Create/return spline
            if (_nextSeg == Seg.LineTo) {
                _nextSeg = null;
                double lastX = _lastX; _lastX = _nextX;
                double lastY = _lastY; _lastY = _nextY;
                return cubicTo(_nextX, lastY, lastX, _nextY, _nextX, _nextY, coords);
            }

            // Do normal version
            return super.getNext(coords);
        }
    }
}
