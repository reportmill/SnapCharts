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
        enableEvents(MousePress, MouseDrag, MouseRelease);
    }

    @Override
    protected void processEvent(ViewEvent anEvent)
    {
        AxisViewX axisX = getAxisX();
        AxisViewY axisY = getAxisY();
        _dragInfo.processEvent(anEvent);

        // Handle MousePress: Store Axis min/max values at MousePress
        if (anEvent.isMousePress()) {
            _pressDataMinX = axisX.getIntervals().getMin();
            _pressDataMaxX = axisX.getIntervals().getMax();
            _pressDataMinY = axisY.getIntervals().getMin();
            _pressDataMaxY = axisY.getIntervals().getMax();
        }

        // Handle MouseDrag: Adjust axis min/max
        if (anEvent.isMouseDrag()) {

            // Adjust X Axis Min/Max for mouse drag
            double dataX1 = viewToDataX(_dragInfo.getPressX());
            double dataX2 = viewToDataX(_dragInfo.getDragX());
            double dispMinX = _pressDataMinX - (dataX2 - dataX1);
            double dispMaxX = _pressDataMaxX - (dataX2 - dataX1);
            axisX.setAxisMin(dispMinX);
            axisX.setAxisMax(dispMaxX);

            // Adjust Y Axis Min/Max for mouse drag
            double dataY1 = viewToDataY(_dragInfo.getPressY());
            double dataY2 = viewToDataY(_dragInfo.getDragY());
            double dispMinY = _pressDataMinY - (dataY2 - dataY1);
            double dispMaxY = _pressDataMaxY - (dataY2 - dataY1);
            axisY.setAxisMin(dispMinY);
            axisY.setAxisMax(dispMaxY);
            getChartView().setTargPoint(null);
        }

        // Do normal version
        super.processEvent(anEvent);
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
