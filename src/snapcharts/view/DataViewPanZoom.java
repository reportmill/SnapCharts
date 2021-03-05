package snapcharts.view;
import snap.geom.Point;
import snap.geom.Rect;
import snap.gfx.Color;
import snap.gfx.Painter;
import snap.gfx.Stroke;
import snap.view.ViewEvent;

/**
 * A helper class to help DataView do Pan/Zoom.
 */
public class DataViewPanZoom {

    // The DataView
    private DataView  _dataView;

    // Whether view is in zoom select mode
    private boolean  _zoomSelectMode;

    // The current ZoomSelect rect
    private Rect _zoomSelectRect;

    // The last MousePress point
    private Point  _pressPoint;

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
    public DataViewPanZoom(DataView aDataView)
    {
        _dataView = aDataView;
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
     * Some conveniences.
     */
    public ChartView getChartView()  { return _dataView.getChartView(); }
    public AxisViewX getAxisX()  { return _dataView.getAxisViewX(); }
    public AxisViewY getAxisY()  { return _dataView.getAxisViewY(); }
    public double getWidth()  { return _dataView.getWidth(); }
    public double getHeight()  { return _dataView.getHeight(); }

    /**
     * Paint ZoomSelectRect, if ZoomSelectMode is set.
     */
    protected void paintAbove(Painter aPntr)
    {
        // Paint ZoomSelectMode ZoomSelectRect
        if (isZoomSelectMode() && _zoomSelectRect!=null) {
            aPntr.setColor(Color.BLACK);
            aPntr.setStroke(ZOOM_RECT_STROKE);
            aPntr.draw(_zoomSelectRect);
        }
    }

    /**
     * Handle ProcessEvent for DataView.
     */
    protected void processEvent(ViewEvent anEvent)
    {
        AxisViewX axisX = getAxisX();
        AxisViewY axisY = getAxisY();

        // Handle MousePress: Store Axis min/max values at MousePress
        if (anEvent.isMousePress()) {

            // Handle MousePress
            _pressPoint = anEvent.getPoint();

            // If double-click, zoom in (or out, if modifier is down)
            if (anEvent.getClickCount()==2) {
                boolean isModDown = anEvent.isShiftDown() || anEvent.isAltDown() || anEvent.isShortcutDown();
                double scale = isModDown ? 2 : .5;
                scaleAxesMinMaxForFactorAndViewXY(scale, scale, anEvent.getX(), anEvent.getY(), true);
            }

            // If triple-click, reset axes
            if (anEvent.getClickCount()==3) {
                ChartView chartView = getChartView();
                chartView.getChartHelper().resetAxesAnimated();
            }

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
                _zoomSelectRect = Rect.get(_pressPoint, anEvent.getPoint());
                _dataView.repaint();
            }

            // Handle Shift axes
            else {
                double fromX = _pressPoint.x;
                double fromY = _pressPoint.y;
                double toX = anEvent.getX();
                double toY = anEvent.getY();
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
            if (_dataView.isMouseDown()) return;
            scaleAxesMinMaxForScroll(anEvent);
        }
    }

    /**
     * Translate Axes min/max values for mouse drag points.
     */
    private void shiftAxesMinMaxForDrag(double dispX0, double dispY0, double dispX1, double dispY1)
    {
        // Calculate new X axis min/max for
        AxisViewX axisX = getAxisX();
        double dataX1 = axisX.viewToData(dispX0);
        double dataX2 = axisX.viewToData(dispX1);
        double dispMinX = _pressDataMinX - (dataX2 - dataX1);
        double dispMaxX = _pressDataMaxX - (dataX2 - dataX1);

        // Set new X Axis min/max
        axisX.setAxisMin(dispMinX);
        axisX.setAxisMax(dispMaxX);

        // Adjust Y Axis Min/Max for mouse drag
        AxisViewY axisY = getAxisY();
        double dataY1 = axisY.viewToData(dispY0);
        double dataY2 = axisY.viewToData(dispY1);
        double dispMinY = _pressDataMinY - (dataY2 - dataY1);
        double dispMaxY = _pressDataMaxY - (dataY2 - dataY1);

        // Set new Y Axis min/max
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
        AxisView axisX = getAxisX();
        double dataX = axisX.viewToData(dispX);
        scaleAxisMinMaxForFactorAndDataMid(axisX, aScaleX, dataX, isAnimated);

        // Get Data Y and scale Y Axis
        AxisView axisY = getAxisY();
        double dataY = axisY.viewToData(dispY);
        scaleAxisMinMaxForFactorAndDataMid(axisY, aScaleY, dataY, isAnimated);

        // If animated, restore targPoint when done
        if (isAnimated && targPoint!=null) {
            axisX.getAnim(0).setOnFinish(() -> {
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
        AxisView axisX = getAxisX();
        double dataX = axisX.viewToData(dispX);
        scaleAxisMinMaxForFactorAboutDataCoord(axisX, aScaleX, dataX, isAnimated);

        // Get Data Y and scale Axis
        AxisView axisY = getAxisY();
        double dataY = axisY.viewToData(dispY);
        scaleAxisMinMaxForFactorAboutDataCoord(axisY, aScaleY, dataY, isAnimated);

        // If animated, restore targPoint when done
        if (isAnimated && targPoint!=null) {
            axisY.getAnim(0).setOnFinish(() -> {
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
     * Zoom axes to rect.
     */
    public void zoomAxesToRectAnimated(Rect aRect)
    {
        double scaleX = aRect.width / getWidth();
        double scaleY = aRect.height / getHeight();
        scaleAxesMinMaxForFactorAndViewXY(scaleX, scaleY, aRect.getMidX(), aRect.getMidY(), true);
    }
}
