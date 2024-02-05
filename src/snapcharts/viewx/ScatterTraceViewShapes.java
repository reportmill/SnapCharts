/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.geom.*;
import snapcharts.data.DataSet;
import snapcharts.charts.*;
import snapcharts.view.TraceView;

/**
 * This class holds a set of Shape/PathIter classes to paint TraceView line and area.
 */
public class ScatterTraceViewShapes {

    /**
     * Returns the shape to draw data line, with option to include all (otherwise, it represents visible only).
     */
    public static Shape getLineShape(TraceView aTraceView, boolean doAll)
    {
        return new TraceLineShape(aTraceView, doAll);
    }

    /**
     * Returns the shape to fill data area.
     */
    public static Shape getAreaShape(TraceView aTraceView)
    {
        return new TraceAreaShape(aTraceView);
    }

    /**
     * A Shape implementation to display TraceView.DataSet as data line.
     */
    private static class TraceLineShape extends Shape {

        // The TraceView
        protected TraceView  _traceView;

        // Whether to include all data points (as opposed to only visible)
        protected boolean  _includeAll;

        // The PointJoin method to connect points
        protected PointJoin  _pointJoin;

        /**
         * Constructor.
         */
        public TraceLineShape(TraceView aTraceView)
        {
            this(aTraceView, false);
        }

        /**
         * Constructor.
         */
        public TraceLineShape(TraceView aTraceView, boolean isIncludeAll)
        {
            _traceView = aTraceView;
            _includeAll = isIncludeAll;
            _pointJoin = _traceView.getTrace().getPointJoin();
        }

        /**
         * Return DataLinePathIter.
         */
        @Override
        public PathIter getPathIter(Transform aTransform)
        {
            // Get normal PathIter for data line
            PathIter pathIter = new TraceLinePathIter(aTransform, _traceView, _includeAll);

            // Apply PointJoin PathIter, if needed
            if (_pointJoin != PointJoin.Line)
                pathIter = XYPointJoins.getPathIterForPointJoin(_pointJoin, pathIter, _traceView);

            // Return PathIter
            return pathIter;
        }
    }

    /**
     * This DataLineShape subclass extends the line to form a closed area down to ZeroY.
     */
    private static class TraceAreaShape extends TraceLineShape {

        /**
         * Constructor.
         */
        public TraceAreaShape(TraceView aTraceView)
        {
            super(aTraceView);
        }

        /**
         * Override to return TraceAreaPathIter.
         */
        @Override
        public PathIter getPathIter(Transform aTransform)
        {
            // If Stacked and there is a PreviousStackedTraceView, return TraceAreaToNextPathIter
            boolean isStacked = _traceView.getTrace().isStacked();
            if (isStacked) {
                TraceView previousStackedTraceView = _traceView.getPreviousStackedTraceView();
                if (previousStackedTraceView != null)
                    return new TraceAreaToNextPathIter(aTransform, _traceView);
            }

            // Otherwise, just create TraceAreaToZeroPathIter
            return new TraceAreaToZeroPathIter(aTransform, _traceView);
        }
    }

    /**
     * A PathIter implementation to display TraceView.DataSet as data line.
     */
    private static class TraceLinePathIter extends PathIter {

        // The TraceView
        protected TraceView  _traceView;

        // The X/Y display coords arrays
        protected double[] _dispX, _dispY;

        // The start/end indexes
        protected int _startIndex, _endIndex;

        // The count
        protected int _count;

        // The index
        protected int _index;

        /**
         * Constructor.
         */
        public TraceLinePathIter(Transform aTrans, TraceView aTraceView, boolean isShowAll)
        {
            super(aTrans);

            // Set TraceView
            _traceView = aTraceView;

            // Get/set display points
            DataSet dispData = aTraceView.getDisplayData();
            _dispX = dispData.getDataX();
            _dispY = dispData.getDataY();

            // Get/set startIndex, endIndex, count
            _startIndex = !isShowAll ? aTraceView.getDispDataStartOutsideIndex() : 0;
            _endIndex = !isShowAll ? aTraceView.getDispDataEndOutsideIndex() : (dispData.getPointCount() - 1);
            _count = _endIndex - _startIndex + 1;

            // If PointJoin.Spline, we might need extra point to prevent jumping
            PointJoin pointJoin = aTraceView.getTrace().getPointJoin();
            if (pointJoin == PointJoin.Spline) {
                if (_startIndex > 0)
                    _startIndex--;
                if (_endIndex < dispData.getPointCount() - 1)
                    _endIndex++;
                _count = _endIndex - _startIndex + 1;
            }
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
            double dispX = _dispX[_startIndex + _index];
            double dispY = _dispY[_startIndex + _index];

            // First segment is moveTo, then lineTos
            if (_index++ == 0)
                return moveTo(dispX, dispY, coords);
            return lineTo(dispX, dispY, coords);
        }
    }

    /**
     * A PathIter implementation to display TraceView.DataSet as data line.
     */
    private static class ReversedTraceLinePathIter extends TraceLinePathIter {

        /**
         * Constructor.
         */
        public ReversedTraceLinePathIter(Transform aTrans, TraceView aTraceView)
        {
            super(aTrans, aTraceView, false);
        }

        /**
         * Returns next segment.
         */
        @Override
        public Seg getNext(double[] coords)
        {
            // Get next display X/Y coords
            double dispX = _dispX[_endIndex - _index];
            double dispY = _dispY[_endIndex - _index];

            // First segment is moveTo, then lineTos
            if (_index++ == 0)
                return moveTo(dispX, dispY, coords);
            return lineTo(dispX, dispY, coords);
        }
    }

    /**
     * This DataLinePathIter subclass adds additional segments to create a closed path down to zero Y.
     */
    private static class TraceAreaToZeroPathIter extends PathIter {

        // The PathIter for the DataLine
        private PathIter  _dataLinePathIter;

        // Whether data line iter is done
        private boolean  _dataLineDone;

        // The ZeroPath index
        private int  _tozeroPathIndex;

        // The X value in display coords at start and end of DataLine
        private double  _startDispX, _endDispX;

        // The y value in display coords for dataY == 0
        private double  _zeroDispY;

        /**
         * Constructor.
         */
        public TraceAreaToZeroPathIter(Transform aTrans, TraceView aTraceView)
        {
            super(aTrans);

            // Get DataLinePathIter for TraceView
            TraceLinePathIter traceLinePathIter = new TraceLinePathIter(aTrans, aTraceView, false);
            _dataLinePathIter = traceLinePathIter;

            // Apply PointJoint PathIter, if needed
            PointJoin pointJoin = aTraceView.getTrace().getPointJoin();
            if (pointJoin != PointJoin.Line)
                _dataLinePathIter = XYPointJoins.getPathIterForPointJoin(pointJoin, _dataLinePathIter, aTraceView);

            // Get StartDispX, EndDispX
            _startDispX = traceLinePathIter._dispX[traceLinePathIter._startIndex];
            _endDispX = traceLinePathIter._dispX[traceLinePathIter._endIndex];

            // Calculate display Y for data Y == 0
            _zeroDispY = aTraceView.dataToViewY(0);
        }

        /**
         * Override to provide for additional segments to create area shape down to display 0.
         */
        @Override
        public boolean hasNext()
        {
            // Try normal version
            if (_dataLinePathIter.hasNext())
                return true;

            // Otherwise, mark DataLineDone and return whether there are remaining toZeroPath segments
            _dataLineDone = true;
            return _tozeroPathIndex < 3;
        }

        /**
         * Override to provide for additional segments to create area shape down to display 0.
         */
        @Override
        public Seg getNext(double[] coords)
        {
            // If still iterating over DataLine, do normal version
            if (!_dataLineDone)
                return _dataLinePathIter.getNext(coords);

            // Bump the counter for area segments
            _tozeroPathIndex++;

            // Handle ToZero segment 1: Drop down with line to ZeroDispY
            if (_tozeroPathIndex == 1) {
                return lineTo(_endDispX, _zeroDispY, coords);
            }

            // Handle ToZero segment 2: Move left with line to StartIndex.x
            if (_tozeroPathIndex == 2) {
                return lineTo(_startDispX, _zeroDispY, coords);
            }

            // Handle ToZero segment 3: Just close
            return close();
        }
    }

    /**
     * This DataLinePathIter subclass adds additional segments to create a closed area path to adjacent TraceView.DataLine.
     */
    private static class TraceAreaToNextPathIter extends PathIter {

        // The primary TraceView
        private TraceView  _traceView;

        // The PathIter to draw data line (may be wrapped in PointJoin PathIter)
        private PathIter  _traceLinePathIter;

        // The adjacent TraceView
        private TraceView  _nextTraceView;

        // The PathIter to draw data line for adjacent TraceView (reversed to create shape)
        private PathIter  _nextTraceLinePathIter;

        // Whether data line iter is done
        private boolean  _dataLineDone;

        // Whether trace area iter is done
        private boolean  _allDone;

        /**
         * Constructor.
         */
        public TraceAreaToNextPathIter(Transform aTrans, TraceView aTraceView)
        {
            super(aTrans);
            _traceView = aTraceView;

            // Get DataLinePathIter for TraceView
            _traceLinePathIter = new TraceLinePathIter(aTrans, aTraceView, false);

            // Apply PointJoint PathIter, if needed
            PointJoin pointJoin = _traceView.getTrace().getPointJoin();
            if (pointJoin != PointJoin.Line)
                _traceLinePathIter = XYPointJoins.getPathIterForPointJoin(pointJoin, _traceLinePathIter, aTraceView);

            // Get next TraceView
            _nextTraceView = _traceView.getPreviousStackedTraceView();
        }

        /**
         * Override to provide for additional segments to create area shape down to display 0.
         */
        @Override
        public boolean hasNext()
        {
            // If still iterating over DataLine, return true
            if (_traceLinePathIter.hasNext())
                return true;

            // Mark DataLineDone
            _dataLineDone = true;

            // If nextTraceLine iteration not started or not finished, continue
            if (_nextTraceLinePathIter == null || _nextTraceLinePathIter.hasNext())
                return true;

            // Return whether everything is done
            return _allDone;
        }

        /**
         * Override to provide for additional segments to create area shape down to display 0.
         */
        @Override
        public Seg getNext(double[] coords)
        {
            // If still iterating over DataLine, do normal version
            if (!_dataLineDone)
                return _traceLinePathIter.getNext(coords);

            // If starting NextTraceView, create its PathIter, and add connecting LineTo to its end point
            if (_nextTraceLinePathIter == null) {

                // Create/set NextTraceLinePathIter
                ReversedTraceLinePathIter nextTraceLinePathIter = new ReversedTraceLinePathIter(_trans, _nextTraceView);
                _nextTraceLinePathIter = nextTraceLinePathIter;

                // Apply PointJoin if needed
                PointJoin pointJoin = _nextTraceView.getTrace().getPointJoin();
                PointJoin pointJoinReverse = pointJoin.getReverse(); // Turns HV to VH
                if (pointJoinReverse != PointJoin.Line)
                    _nextTraceLinePathIter = XYPointJoins.getPathIterForPointJoin(pointJoinReverse, _nextTraceLinePathIter, _nextTraceView);

                // Eat first segment (MoveTo)
                if (_nextTraceLinePathIter.hasNext())
                    _nextTraceLinePathIter.getNext(coords);

                // Handle lineTo connecting line between data sets
                int endIndex = _nextTraceView.getDispDataEndOutsideIndex();
                double dispX = nextTraceLinePathIter._dispX[endIndex];
                double dispY = nextTraceLinePathIter._dispY[endIndex];
                return lineTo(dispX, dispY, coords);
            }

            // If NextTrace PathIter still iterating, let it provide segment
            if (_nextTraceLinePathIter.hasNext())
                return _nextTraceLinePathIter.getNext(coords);

            // Mark all done and return close path
            _allDone = true;
            return close();
        }
    }
}
