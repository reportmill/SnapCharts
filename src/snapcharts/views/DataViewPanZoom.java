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

        if (anEvent.isMousePress()) {
            _pressDataMinX = axisX.getAxisMin();
            _pressDataMaxX = axisX.getAxisMax();
            _pressDataMinY = axisY.getAxisMin();
            _pressDataMaxY = axisY.getAxisMax();
        }

        if (anEvent.isMouseDrag()) {
            double data1 = viewToDataX(_dragInfo.getPressX());
            double data2 = viewToDataX(_dragInfo.getDragX());
            getAxisX().setAxisMin(_pressDataMinX - (data2 - data1));
            getAxisX().setAxisMax(_pressDataMaxX - (data2 - data1));
        }
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

        public double getDragX()  { return _drag.getX(); }

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
