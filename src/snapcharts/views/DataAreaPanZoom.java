package snapcharts.views;
import snap.geom.Point;
import snap.geom.Rect;
import snap.gfx.Color;
import snap.gfx.Painter;
import snap.gfx.Stroke;
import snap.view.ViewEvent;

/**
 * A DataArea subclass that can pan zoom with mouse.
 */
public abstract class DataAreaPanZoom extends DataArea {

    // Whether view is in zoom select mode
    private boolean  _zoomSelectMode;

    // The current ZoomSelect rect
    private Rect  _zoomSelectRect;

    // The DragInfo
    private DragInfo _dragInfo = new DragInfo();

    // Cached info from last MousePress
    private double _pressDataMinX;
    private double _pressDataMaxX;
    private double _pressDataMinY;
    private double _pressDataMaxY;

    // Constants
    private static final Stroke ZOOM_RECT_STROKE = Stroke.StrokeDash1;

    /**
     * Constructor.
     */
    public DataAreaPanZoom()
    {
        super();
        enableEvents(MousePress, MouseDrag, MouseRelease, Scroll);
    }

    /**
     * Returns whether view is in ZoomSelectMode.
     */
    public boolean isZoomSelectMode()  { return _zoomSelectMode; }

    /**
     * Sets whether view is in ZoomSelectMode.
     */
    public void setZoomSelectMode(boolean aValue)
    {
        // Set Mode
        _zoomSelectMode = aValue;

        // If turning off, clear ZoomSelectRect
        if (!aValue) {
            _zoomSelectRect = null;
        }
    }

    /**
     * Override to paint ZoomSelectRect, if ZoomSelectMode is set.
     */
    @Override
    protected void paintAbove(Painter aPntr)
    {
        // Paint ZoomSelectMode ZoomSelectRect
        if (isZoomSelectMode() && _zoomSelectRect!=null) {
            aPntr.setColor(Color.BLACK);
            aPntr.setStroke(ZOOM_RECT_STROKE);
            aPntr.draw(_zoomSelectRect);
        }
    }

    @Override
    protected void processEvent(ViewEvent anEvent)
    {
        AxisViewX axisX = getAxisX();
        AxisViewY axisY = getAxisY();
        _dragInfo.processEvent(anEvent);

        // Handle MousePress: Store Axis min/max values at MousePress
        if (anEvent.isMousePress()) {

            // If double-click, zoom in (or out, if modifier is down)
            if (anEvent.getClickCount()==2) {
                boolean isModDown = anEvent.isShiftDown() || anEvent.isAltDown() || anEvent.isShortcutDown();
                double scale = isModDown ? 2 : .5;
                scaleAxesMinMaxForFactorAndViewXY(scale, scale, anEvent.getX(), anEvent.getY(), true);
            }

            // If triple-click, reset axes
            if (anEvent.getClickCount()==3)
                resetAxesAnimated();

            // Store axes min/max values
            _pressDataMinX = axisX.getAxisMin();
            _pressDataMaxX = axisX.getAxisMax();
            _pressDataMinY = axisY.getAxisMin();
            _pressDataMaxY = axisY.getAxisMax();

            // Handle ZoomSelectMode
            if (isZoomSelectMode()) {
                _zoomSelectRect = new Rect(anEvent.getX(), anEvent.getY(), 1, 1);
            }
        }

        // Handle MouseDrag: Adjust axis min/max
        else if (anEvent.isMouseDrag()) {

            // Handle ZoomSelectMode
            if (isZoomSelectMode()) {
                Point p0 = new Point(_dragInfo.getPressX(), _dragInfo.getPressY());
                _zoomSelectRect = Rect.get(p0, anEvent.getPoint());
                repaint();
            }

            // Handle Shift axes
            else {
                double fromX = _dragInfo.getPressX();
                double fromY = _dragInfo.getPressY();
                double toX = _dragInfo.getDragX();
                double toY = _dragInfo.getDragY();
                shiftAxesMinMaxForDrag(fromX, fromY, toX, toY);
            }
        }

        // Handle MouseRelease
        else if (anEvent.isMouseRelease()) {

            // Handle ZoomSelectMode
            if (isZoomSelectMode()) {
                if (_zoomSelectRect.getWidth()>4 && _zoomSelectRect.getHeight()>4)
                    zoomAxesToRectAnimated(_zoomSelectRect);
                setZoomSelectMode(false);
                _zoomSelectRect = null;
            }

            // Handle click
            else if (anEvent.isMouseClick())
                getChartView().setTargPoint(anEvent.getPoint());
        }

        // Handle Scroll
        else if (anEvent.isScroll()) {
            if (isMouseDown()) return;
            scaleAxesMinMaxForScroll(anEvent);
        }

        // Do normal version
        super.processEvent(anEvent);
    }

    /**
     * Translate Axes min/max values for mouse drag points.
     */
    private void shiftAxesMinMaxForDrag(double dispX0, double dispY0, double dispX1, double dispY1)
    {
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
    private void scaleAxesMinMaxForScroll(ViewEvent aScrollEvent)
    {
        // Get scale: Assume + 1x per 100 points (1.5 inches). If scale down, limit to .5
        double scroll = aScrollEvent.getScrollY();
        double scale = Math.max(1 + scroll / 100, .5);

        // Get Mouse X/Y
        double dispX = aScrollEvent.getX();
        double dispY = aScrollEvent.getY();

        // Do scaleAxes
        scaleAxesMinMaxForFactorAboutViewXY(scale, scale, dispX, dispY, false);
    }

    /**
     * Sets X/Y Axis min/max values for mouse drag points.
     */
    public void scaleAxesMinMaxForFactor(double aScale, boolean isAnimated)
    {
        scaleAxesMinMaxForFactorAndViewXY(aScale, aScale, getWidth()/2, getHeight()/2, isAnimated);
    }

    /**
     * Scales Axes min/max by factor (about axis center) and translates axis center to given X/Y.
     */
    public void scaleAxesMinMaxForFactorAndViewXY(double aScaleX, double aScaleY, double dispX, double dispY, boolean isAnimated)
    {
        // Clear target point to remove mouse-over display
        Point targPoint = getChartView().getTargPoint();

        // Get Data X and scale X Axis
        double dataX = viewToDataX(dispX);
        AxisView axisViewX = getAxisX();
        scaleAxisMinMaxForFactorAndDataMid(axisViewX, aScaleX, dataX, isAnimated);

        // Get Data Y and scale Y Axis
        double dataY = viewToDataY(dispY);
        AxisView axisViewY = getAxisY();
        scaleAxisMinMaxForFactorAndDataMid(axisViewY, aScaleY, dataY, isAnimated);

        // If animated, restore targPoint when done
        if (isAnimated && targPoint!=null) {
            axisViewY.getAnim(0).setOnFinish(() -> {
                if (getChartView().getTargPoint() == null)
                    getChartView().setTargPoint(targPoint);
            });
        }
    }

    /**
     * Sets Axis min/max values for scale and center (in data coords).
     */
    private void scaleAxisMinMaxForFactorAndDataMid(AxisView anAxisView, double aScale, double dataMid, boolean isAnimated)
    {
        // Get Axis min/max/mid points
        double axisMin = anAxisView.getAxisMin();
        double axisMax = anAxisView.getAxisMax();
        double axisMid = axisMin + (axisMax - axisMin) / 2;

        // Get translation from axis mid point to given dataCoord
        double trans = dataMid - axisMid;

        // Convert min/max by translating axis mid to zero, applying scale, translating back plus offset of old/new mid
        double min2 = (axisMin - axisMid) * aScale + axisMid + trans;
        double max2 = (axisMax - axisMid) * aScale + axisMid + trans;
        anAxisView.setAxisMinMax(min2, max2, isAnimated);
    }

    /**
     * Scales Axes min/max by factor about given X/Y (in display coords).
     */
    public void scaleAxesMinMaxForFactorAboutViewXY(double aScaleX, double aScaleY, double dispX, double dispY, boolean isAnimated)
    {
        // Clear target point to remove mouse-over display
        Point targPoint = getChartView().getTargPoint();

        // Get Data X and scale Axis
        double dataX = viewToDataX(dispX);
        AxisView axisViewX = getAxisX();
        scaleAxisMinMaxForFactorAboutDataCoord(axisViewX, aScaleX, dataX, isAnimated);

        // Get Data Y and scale Axis
        double dataY = viewToDataY(dispY);
        AxisView axisViewY = getAxisY();
        scaleAxisMinMaxForFactorAboutDataCoord(axisViewY, aScaleY, dataY, isAnimated);

        // If animated, restore targPoint when done
        if (isAnimated && targPoint!=null) {
            axisViewY.getAnim(0).setOnFinish(() -> {
                if (getChartView().getTargPoint() == null)
                    getChartView().setTargPoint(targPoint);
            });
        }
    }

    /**
     * Scales Axis min/max by factor about given data coord.
     */
    private void scaleAxisMinMaxForFactorAboutDataCoord(AxisView anAxisView, double aScale, double dataMid, boolean isAnimated)
    {
        double minX = anAxisView.getAxisMin();
        double maxX = anAxisView.getAxisMax();
        double minX2 = (minX - dataMid) * aScale + dataMid;
        double maxX2 = (maxX - dataMid) * aScale + dataMid;
        anAxisView.setAxisMinMax(minX2, maxX2, isAnimated);
    }

    /**
     * Resets Axes to original bounds.
     */
    public void resetAxes()
    {
        getAxisX().resetAxes();
        getAxisY().resetAxes();
    }

    /**
     * Resets Axes to original bounds.
     */
    public void resetAxesAnimated()
    {
        getAxisX().resetAxesAnimated();
        getAxisY().resetAxesAnimated();
    }

    /**
     * Zoom axes to rect.
     */
    public void zoomAxesToRectAnimated(Rect aRect)
    {
        double scaleX = aRect.width / getWidth();
        double scaleY = aRect.height / getHeight();
        scaleAxesMinMaxForFactorAndViewXY(scaleX, scaleY, aRect.getMidX(), aRect.getMidY(), true);
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
