package snapcharts.views;
import snap.view.ViewEvent;

/**
 * A DataView subclass that can pan zoom with mouse.
 */
public abstract class DataViewPanZoom extends DataView {

    // The DragInfo
    private DragInfo _dragInfo = new DragInfo();

    private double _pressDataMinX;
    private double _pressDataMaxX;
    private double _pressDataMinY;
    private double _pressDataMaxY;

    /**
     * Constructor.
     */
    public DataViewPanZoom()
    {
        super();
        enableEvents(MousePress, MouseDrag, MouseRelease, Scroll);
    }

    @Override
    protected void processEvent(ViewEvent anEvent)
    {
        AxisViewX axisX = getAxisX();
        AxisViewY axisY = getAxisY();
        _dragInfo.processEvent(anEvent);

        // Handle MousePress: Store Axis min/max values at MousePress
        if (anEvent.isMousePress()) {

            // If double-click, reset
            if (anEvent.getClickCount()==2) {
                axisX.setAxisMin(AxisView.UNSET_DOUBLE);
                axisX.setAxisMax(AxisView.UNSET_DOUBLE);
                axisY.setAxisMin(AxisView.UNSET_DOUBLE);
                axisY.setAxisMax(AxisView.UNSET_DOUBLE);
            }

            // Store axes min/max values
            _pressDataMinX = axisX.getIntervals().getMin();
            _pressDataMaxX = axisX.getIntervals().getMax();
            _pressDataMinY = axisY.getIntervals().getMin();
            _pressDataMaxY = axisY.getIntervals().getMax();
        }

        // Handle MouseDrag: Adjust axis min/max
        else if (anEvent.isMouseDrag()) {
            double fromX = _dragInfo.getPressX();
            double fromY = _dragInfo.getPressY();
            double toX = _dragInfo.getDragX();
            double toY = _dragInfo.getDragY();
            shiftAxesMinMaxForDrag(fromX, fromY, toX, toY);
        }

        // Handle Scroll
        else if (anEvent.isScroll()) {
            scaleAxesMinMaxForDrag(anEvent);
        }

        // Do normal version
        super.processEvent(anEvent);
    }

    /**
     * Translate Axes min/max values for mouse drag points.
     */
    private void shiftAxesMinMaxForDrag(double dispX0, double dispY0, double dispX1, double dispY1)
    {
        // Clear target point to remove mouse-over display
        getChartView().setTargPoint(null);

        // Calculate new X axis min/max for
        double dataX1 = viewToDataX(dispX0);
        double dataX2 = viewToDataX(dispX1);
        double dispMinX = _pressDataMinX - (dataX2 - dataX1);
        double dispMaxX = _pressDataMaxX - (dataX2 - dataX1);

        // Set new X Axis min/max
        AxisViewX axisX = getAxisX();
        axisX.setAxisMin(dispMinX);
        axisX.setAxisMax(dispMaxX);

        // Adjust Y Axis Min/Max for mouse drag
        double dataY1 = viewToDataY(dispY0);
        double dataY2 = viewToDataY(dispY1);
        double dispMinY = _pressDataMinY - (dataY2 - dataY1);
        double dispMaxY = _pressDataMaxY - (dataY2 - dataY1);

        // Set new Y Axis min/max
        AxisViewY axisY = getAxisY();
        axisY.setAxisMin(dispMinY);
        axisY.setAxisMax(dispMaxY);
    }

    /**
     * Sets X/Y Axis min/max values for mouse drag points.
     */
    private void scaleAxesMinMaxForDrag(ViewEvent aScrollEvent)
    {
        // Clear target point to remove mouse-over display
        getChartView().setTargPoint(null);

        // Get scale: Assume + 1x per 100 points (1.5 inches). If scale down, limit to .5
        double scroll = aScrollEvent.getScrollY();
        double scale = Math.max(1 + scroll/100, .5);

        // Get Mouse X/Y
        double dispX = aScrollEvent.getX();
        double dispY = aScrollEvent.getY();
        double dataX = viewToDataX(dispX);
        double dataY = viewToDataY(dispY);

        // Scale
        AxisView axisX = getAxisX();
        double minX = axisX.getIntervals().getMin();
        double maxX = axisX.getIntervals().getMax();
        double minX2 = (minX - dataX)*scale + dataX;
        double maxX2 = (maxX - dataX)*scale + dataX;
        axisX.setAxisMin(minX2);
        axisX.setAxisMax(maxX2);

        // Scale
        AxisView axisY = getAxisY();
        double minY = axisY.getIntervals().getMin();
        double maxY = axisY.getIntervals().getMax();
        double minY2 = (minY - dataY)*scale + dataY;
        double maxY2 = (maxY - dataY)*scale + dataY;
        axisY.setAxisMin(minY2);
        axisY.setAxisMax(maxY2);
    }

    /**
     * A class to manage drag info.
     */
    private static class DragInfo {

        // The MousePress
        private ViewEvent  _press;

        // The Drag
        private ViewEvent  _drag;

        /**
         * processEvent.
         */
        protected void processEvent(ViewEvent anEvent)
        {
            // Handle MousePress
            if (anEvent.isMousePress())
                _press = anEvent;

            // Handle MouseDrag
            else if (anEvent.isMouseDrag())
                _drag = anEvent;
        }

        public double getPressX()  { return _press.getX(); }

        public double getPressY()  { return _press.getY(); }

        public double getDragX()  { return _drag.getX(); }

        public double getDragY()  { return _drag.getY(); }

        /**
         * Returns the offset X.
         */
        public double getDX()
        {
            return _drag.getX() - _press.getX();
        }

        /**
         * Returns the offset Y.
         */
        public double getDY()
        {
            return _drag.getY() - _press.getY();
        }
    }
}
