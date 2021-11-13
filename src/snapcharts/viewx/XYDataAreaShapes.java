/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.geom.*;
import snapcharts.model.*;
import snapcharts.view.DataArea;

/**
 * This class holds a set of Shape/PathIter classes to paint DataArea line and area.
 */
public class XYDataAreaShapes {

    /**
     * Returns the shape to draw data line, with option to include all (otherwise, it represents visible only).
     */
    public static Shape getLineShape(DataArea aDataArea, boolean doAll)
    {
        return new DataLineShape(aDataArea, doAll);
    }

    /**
     * Returns the shape to fill data area.
     */
    public static Shape getAreaShape(DataArea aDataArea)
    {
        return new DataAreaShape(aDataArea);
    }

    /**
     * A Shape implementation to display DataArea.DataSet as data line.
     */
    private static class DataLineShape extends Shape {

        // The DataArea
        protected DataArea  _dataArea;

        // Whether to include all data points (as opposed to only visible)
        protected boolean  _includeAll;

        // The PointJoin method to connect points
        protected PointJoin  _pointJoin;

        /**
         * Constructor.
         */
        public DataLineShape(DataArea aDataArea)
        {
            this(aDataArea, false);
        }

        /**
         * Constructor.
         */
        public DataLineShape(DataArea aDataArea, boolean isIncludeAll)
        {
            _dataArea = aDataArea;
            _includeAll = isIncludeAll;
            _pointJoin = _dataArea.getDataStyle().getPointJoin();
        }

        /**
         * Return DataLinePathIter.
         */
        @Override
        public PathIter getPathIter(Transform aTransform)
        {
            // Get normal PathIter for data line
            PathIter pathIter = new DataLinePathIter(aTransform, _dataArea, _includeAll);

            // Apply PointJoin PathIter, if needed
            if (_pointJoin != PointJoin.Line)
                pathIter = XYPointJoins.getPathIterForPointJoin(_pointJoin, pathIter, _dataArea);

            // Return PathIter
            return pathIter;
        }
    }

    /**
     * This DataLineShape subclass extends the line to form a closed area down to ZeroY.
     */
    private static class DataAreaShape extends DataLineShape {

        /**
         * Constructor.
         */
        public DataAreaShape(DataArea aDataArea)
        {
            super(aDataArea);
        }

        /**
         * Override to return DataAreaPathIter.
         */
        @Override
        public PathIter getPathIter(Transform aTransform)
        {
            // If Stacked and there is a PreviousStackedDataArea, return DataAreaToNextPathIter
            boolean isStacked = _dataArea.getDataSet().isStacked();
            if (isStacked) {
                DataArea previousStackedDataArea = _dataArea.getPreviousStackedDataArea();
                if (previousStackedDataArea != null)
                    return new DataAreaToNextPathIter(aTransform, _dataArea);
            }

            // Otherwise, just create DataAreaToZeroPathIter
            return new DataAreaToZeroPathIter(aTransform, _dataArea);
        }
    }

    /**
     * A PathIter implementation to display DataArea.DataSet as data line.
     */
    private static class DataLinePathIter extends PathIter {

        // The DataArea
        protected DataArea _dataArea;

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
        public DataLinePathIter(Transform aTrans, DataArea aDataArea, boolean isShowAll)
        {
            super(aTrans);

            // Set DataArea
            _dataArea = aDataArea;

            // Get/set display points
            DataStore dispData = aDataArea.getDisplayData();
            _dispX = dispData.getDataX();
            _dispY = dispData.getDataY();

            // Get/set startIndex, endIndex, count
            _startIndex = !isShowAll ? aDataArea.getDispDataStartOutsideIndex() : 0;
            _endIndex = !isShowAll ? aDataArea.getDispDataEndOutsideIndex() : (dispData.getPointCount() - 1);
            _count = _endIndex - _startIndex + 1;

            // If PointJoin.Spline, we might need extra point to prevent jumping
            PointJoin pointJoin = aDataArea.getDataStyle().getPointJoin();
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
     * A PathIter implementation to display DataArea.DataSet as data line.
     */
    private static class ReversedDataLinePathIter extends DataLinePathIter {

        /**
         * Constructor.
         */
        public ReversedDataLinePathIter(Transform aTrans, DataArea aDataArea)
        {
            super(aTrans, aDataArea, false);
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
    private static class DataAreaToZeroPathIter extends PathIter {

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
        public DataAreaToZeroPathIter(Transform aTrans, DataArea aDataArea)
        {
            super(aTrans);

            // Get DataLinePathIter for DataArea
            DataLinePathIter dataLinePathIter = new DataLinePathIter(aTrans, aDataArea, false);
            _dataLinePathIter = dataLinePathIter;

            // Apply PointJoint PathIter, if needed
            PointJoin pointJoin = aDataArea.getDataStyle().getPointJoin();
            if (pointJoin != PointJoin.Line)
                _dataLinePathIter = XYPointJoins.getPathIterForPointJoin(pointJoin, _dataLinePathIter, aDataArea);

            // Get StartDispX, EndDispX
            _startDispX = dataLinePathIter._dispX[dataLinePathIter._startIndex];
            _endDispX = dataLinePathIter._dispX[dataLinePathIter._endIndex];

            // Calculate display Y for data Y == 0
            _zeroDispY = aDataArea.dataToViewY(0);
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
     * This DataLinePathIter subclass adds additional segments to create a closed area path to adjacent DataArea.DataLine.
     */
    private static class DataAreaToNextPathIter extends PathIter {

        // The primary DataArea
        private DataArea  _dataArea;

        // The PathIter to draw data line DataArea (may be wrapped in PointJoin PathIter)
        private PathIter  _dataLinePathIter;

        // The adjacent DataArea
        private DataArea  _nextDataArea;

        // The PathIter to draw data line for adjacent DataArea (reversed to create shape)
        private PathIter  _nextDataAreaPathIter;

        // Whether data line iter is done
        private boolean  _dataLineDone;

        // Whether data area iter is done
        private boolean  _allDone;

        /**
         * Constructor.
         */
        public DataAreaToNextPathIter(Transform aTrans, DataArea aDataArea)
        {
            super(aTrans);
            _dataArea = aDataArea;

            // Get DataLinePathIter for DataArea
            _dataLinePathIter = new DataLinePathIter(aTrans, aDataArea, false);

            // Apply PointJoint PathIter, if needed
            PointJoin pointJoin = _dataArea.getDataStyle().getPointJoin();
            if (pointJoin != PointJoin.Line)
                _dataLinePathIter = XYPointJoins.getPathIterForPointJoin(pointJoin, _dataLinePathIter, aDataArea);

            // Get next data area
            _nextDataArea = _dataArea.getPreviousStackedDataArea();
        }

        /**
         * Override to provide for additional segments to create area shape down to display 0.
         */
        @Override
        public boolean hasNext()
        {
            // If still iterating over DataLine, return true
            if (_dataLinePathIter.hasNext())
                return true;

            // Mark DataLineDone
            _dataLineDone = true;

            // If nextDataArea iteration not started or not finished, continue
            if (_nextDataAreaPathIter == null || _nextDataAreaPathIter.hasNext())
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
                return _dataLinePathIter.getNext(coords);

            // If starting NextDataArea, create its PathIter, and add connecting LineTo to its end point
            if (_nextDataAreaPathIter == null) {

                // Create/set NextDataAreaPathIter
                ReversedDataLinePathIter nextDataAreaPathIter = new ReversedDataLinePathIter(_trans, _nextDataArea);
                _nextDataAreaPathIter = nextDataAreaPathIter;

                // Apply PointJoin if needed
                PointJoin pointJoin = _nextDataArea.getDataStyle().getPointJoin();
                PointJoin pointJoinReverse = pointJoin.getReverse(); // Turns HV to VH
                if (pointJoinReverse != PointJoin.Line)
                    _nextDataAreaPathIter = XYPointJoins.getPathIterForPointJoin(pointJoinReverse, _nextDataAreaPathIter, _nextDataArea);

                // Eat first segment (MoveTo)
                if (_nextDataAreaPathIter.hasNext())
                    _nextDataAreaPathIter.getNext(coords);

                // Handle lineTo connecting line between data sets
                int endIndex = _nextDataArea.getDispDataEndOutsideIndex();
                double dispX = nextDataAreaPathIter._dispX[endIndex];
                double dispY = nextDataAreaPathIter._dispY[endIndex];
                return lineTo(dispX, dispY, coords);
            }

            // If NextDataArea PathIter still iterating, let it provide segment
            if (_nextDataAreaPathIter.hasNext())
                return _nextDataAreaPathIter.getNext(coords);

            // Mark all done and return close path
            _allDone = true;
            return close();
        }
    }
}
