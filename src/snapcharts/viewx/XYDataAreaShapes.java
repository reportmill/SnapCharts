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
        protected DataArea _dataArea;

        // Whether to include all data points (as opposed to only visible)
        protected boolean  _includeAll;

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
        }

        /**
         * Return DataLinePathIter.
         */
        @Override
        public PathIter getPathIter(Transform aT)
        {
            return new DataLinePathIter(aT, _dataArea, _includeAll);
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
        public PathIter getPathIter(Transform aT)
        {
            // If Stacked and there is a PreviousStackedDataArea, return DataAreaToNextPathIter
            boolean isStacked = _dataArea.getDataSet().isStacked();
            if (isStacked) {
                DataArea previousStackedDataArea = _dataArea.getPreviousStackedDataArea();
                if (previousStackedDataArea != null)
                    return new DataAreaToNextPathIter(aT, _dataArea);
            }

            // Otherwise, just return DataAreaToZeroPathIter
            return new DataAreaToZeroPathIter(aT, _dataArea);
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
            DataStore dispData = aDataArea.getDispData();
            _dispX = dispData.getDataX();
            _dispY = dispData.getDataY();

            // Get/set startIndex, endIndex, count
            _startIndex = !isShowAll ? aDataArea.getDispDataStartOutsideIndex() : 0;
            _endIndex = !isShowAll ? aDataArea.getDispDataEndOutsideIndex() : (dispData.getPointCount() - 1);
            _count = _endIndex - _startIndex + 1;
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

            // Increment index, return lineTo
            _index++;
            return lineTo(dispX, dispY, coords);
        }
    }

    /**
     * This DataLinePathIter subclass adds additional segments to create a closed path down to zero Y.
     */
    private static class DataAreaToZeroPathIter extends DataLinePathIter {

        // Whether data line iter is done
        private boolean _dataLineDone;

        // The ZeroPath index
        private int _tozeroPathIndex;

        // The y value in display coords for dataY == 0
        private double _zeroDispY;

        /**
         * Constructor.
         */
        public DataAreaToZeroPathIter(Transform aTrans, DataArea aDataArea)
        {
            super(aTrans, aDataArea, false);

            // Calculate display Y for data Y == 0
            _zeroDispY = _dataArea.dataToViewY(0);
        }

        /**
         * Override to provide for additional segments to create area shape down to display 0.
         */
        @Override
        public boolean hasNext()
        {
            // Try normal version
            if (super.hasNext())
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
                return super.getNext(coords);

            // Bump the counter for area segments
            _tozeroPathIndex++;

            // Handle ToZero segment 1: Drop down with line to ZeroDispY
            if (_tozeroPathIndex == 1) {
                double dispX = _dispX[_endIndex];
                return lineTo(dispX, _zeroDispY, coords);
            }

            // Handle ToZero segment 2: Move left with line to StartIndex.x
            if (_tozeroPathIndex == 2) {
                double dispX = _dispX[_startIndex];
                return lineTo(dispX, _zeroDispY, coords);
            }

            // Handle ToZero segment 3: Just close
            return close();
        }
    }

    /**
     * This DataLinePathIter subclass adds additional segments to create a closed area path to adjacent DataArea.DataLine.
     */
    private static class DataAreaToNextPathIter extends DataLinePathIter {

        // The adjacent DataArea
        private DataArea _nextDataArea;

        // The adjacent DataArea
        private ReversedDataLinePathIter _nextDataAreaPathIter;

        // Whether data line iter is done
        private boolean _dataLineDone;

        // Whether data area iter is done
        private boolean _allDone;

        /**
         * Constructor.
         */
        public DataAreaToNextPathIter(Transform aTrans, DataArea aDataArea)
        {
            super(aTrans, aDataArea, false);

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
            if (super.hasNext())
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
                return super.getNext(coords);

            // If starting NextDataArea, create its PathIter, and add connecting LineTo to its end point
            if (_nextDataAreaPathIter == null) {
                _nextDataAreaPathIter = new ReversedDataLinePathIter(_trans, _nextDataArea);
                int endIndex = _nextDataArea.getDispDataEndOutsideIndex();
                double dispX = _nextDataAreaPathIter._dispX[endIndex];
                double dispY = _nextDataAreaPathIter._dispY[endIndex];
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
