package snapcharts.view;
import snap.geom.Point;
import snap.geom.Rect;
import snap.gfx.Color;
import snap.gfx.Painter;
import snap.gfx.Stroke;
import snap.view.View;
import snap.view.ViewEvent;
import snapcharts.charts.AxisType;
import snapcharts.data.MinMax;
import java.util.HashMap;
import java.util.Map;

/**
 * A helper class to help ContentView do Pan/Zoom.
 */
public class ChartHelperPanZoom {

    // The ChartHelper
    private ChartHelper  _chartHelper;

    // Whether view is in zoom select mode
    private boolean  _zoomSelectMode;

    // The current ZoomSelect rect
    private Rect  _zoomSelectRect;

    // The last MousePress point
    private Point  _pressPoint;

    // A Map of MinMax for each Axis on mousePress
    private Map<AxisType, MinMax>  _axesMinMaxOnPress =  new HashMap<>();

    // The ChartView.TargPoint on last MousePress
    private Point  _targPointOnPress;

    // Constants
    private static final Stroke ZOOM_RECT_STROKE = Stroke.StrokeDash1;

    /**
     * Constructor.
     */
    public ChartHelperPanZoom(ChartHelper aChartHelper)
    {
        _chartHelper = aChartHelper;
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
    private ChartView getChartView()  { return _chartHelper.getChartView(); }
    private ContentView getContentView()  { return _chartHelper.getContentView(); }
    private double getWidth()  { return getContentView().getWidth(); }
    private double getHeight()  { return getContentView().getHeight(); }

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
     * Handle ProcessEvent for ContentView.
     */
    protected void processEventForChartPartView(View aView, ViewEvent anEvent)
    {
        // Handle MousePress: Store Axis min/max values at MousePress
        if (anEvent.isMousePress()) {

            // Cache MousePress Point, and ChartView.TargPoint
            _pressPoint = anEvent.getPoint();
            _targPointOnPress = getChartView().getTargPoint();

            // Store axes min/max values on press
            AxisView[] axisViews = _chartHelper.getAxisViews();
            for (AxisView axisView : axisViews) {
                double axisMin = axisView.getAxisMin();
                double axisMax = axisView.getAxisMax();
                _axesMinMaxOnPress.put(axisView.getAxisType(), new MinMax(axisMin, axisMax));
            }
        }

        // Handle ContentView events
        if (aView instanceof ContentView)
            processEventForContentView((ContentView) aView, anEvent);

        // Handle AxisView events
        else if (aView instanceof AxisView)
            processEventForAxisView((AxisView) aView, anEvent);

    }

    /**
     * Handle ProcessEvent for ContentView.
     */
    protected void processEventForContentView(ContentView contentView, ViewEvent anEvent)
    {
        // Handle MousePress: Store Axis min/max values at MousePress
        if (anEvent.isMousePress()) {

            // If double-click, zoom in (or out, if modifier is down)
            if (anEvent.getClickCount() == 2)
                scaleAxesMinMaxForViewAndEvent(contentView, anEvent);

            // If triple-click, reset axes
            else if (anEvent.getClickCount() == 3)
                _chartHelper.resetAxesAnimated();

            // Handle ZoomSelectMode
            if (isZoomSelectMode())
                _zoomSelectRect = new Rect(anEvent.getX(), anEvent.getY(), 1, 1);
        }

        // Handle MouseDrag: Adjust axis min/max
        else if (anEvent.isMouseDrag()) {

            // Handle ZoomSelectMode
            if (isZoomSelectMode()) {
                _zoomSelectRect = Rect.get(_pressPoint, anEvent.getPoint());
                contentView.repaint();
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
            else if (anEvent.isMouseClick()) {
                ChartView chartView = getChartView();
                Point pnt = contentView.localToParent(anEvent.getX(), anEvent.getY(), chartView);
                chartView.setTargPoint(pnt);
            }
        }

        // Handle Scroll
        else if (anEvent.isScroll()) {
            if (contentView.isMouseDown()) return;
            scaleAxesMinMaxForViewAndScroll(contentView, anEvent);
        }

        // Consume event
        if (anEvent.isMouseEvent() || anEvent.isScroll())
            anEvent.consume();
    }

    /**
     * Handle ProcessEvent for AxisView.
     */
    protected void processEventForAxisView(AxisView axisView, ViewEvent anEvent)
    {
        // Get AxisType
        AxisType axisType = axisView.getAxisType();

        // Handle MousePress: Store Axis min/max values at MousePress
        if (anEvent.isMousePress()) {

            // If double-click, zoom in (or out, if modifier is down)
            if (anEvent.getClickCount() == 2)
                scaleAxesMinMaxForViewAndEvent(axisView, anEvent);

            // If triple-click, reset axes
            else if (anEvent.getClickCount() == 3) {
                _chartHelper.resetAxesAnimated();
            }

            // Handle ZoomSelectMode
            if (isZoomSelectMode()) {
                if (axisType.isX())
                    _zoomSelectRect = new Rect(anEvent.getX(), 0, 1, getHeight());
                else _zoomSelectRect = new Rect(0, anEvent.getY(), getWidth(), 1);
            }
        }

        // Handle MouseDrag: Adjust axis min/max
        else if (anEvent.isMouseDrag()) {

            // Handle ZoomSelectMode
            if (isZoomSelectMode()) {
                _zoomSelectRect = Rect.get(_pressPoint, anEvent.getPoint());
                if (axisType.isX()) {
                    _zoomSelectRect.setY(0);
                    _zoomSelectRect.setHeight(getHeight());
                }
                else {
                    _zoomSelectRect.setX(0);
                    _zoomSelectRect.setWidth(getWidth());
                }
                getContentView().repaint();
            }

            // Handle Shift axes
            else {
                double fromXY = axisType.isX() ? _pressPoint.x : _pressPoint.y;
                double toXY = axisType.isX() ? anEvent.getX() : anEvent.getY();
                shiftAxisMinMaxForDrag(axisView, fromXY, toXY);
            }
        }

        // Handle MouseRelease
        else if (anEvent.isMouseRelease()) {

            // Handle ZoomSelectMode
            if (isZoomSelectMode()) {
                if (_zoomSelectRect.width > 4 && _zoomSelectRect.height > 4)
                    zoomAxesToRectAnimated(_zoomSelectRect);
                setZoomSelectMode(false);
                _zoomSelectRect = null;
            }
        }

        // Handle Scroll
        else if (anEvent.isScroll()) {
            if (axisView.isMouseDown()) return;
            scaleAxesMinMaxForViewAndScroll(axisView, anEvent);
        }

        // Consume event
        if (anEvent.isMouseEvent() || anEvent.isScroll())
            anEvent.consume();
    }

    /**
     * Translate Axes min/max values for mouse drag points.
     */
    private void shiftAxesMinMaxForDrag(double dispX0, double dispY0, double dispX1, double dispY1)
    {
        // Iterate over axes and shift each (using X or Y coords based on AxisType)
        AxisView[] axisViews = _chartHelper.getAxisViews();
        for (AxisView axisView : axisViews) {
            AxisType axisType = axisView.getAxisType();
            double dispXY0 = axisType == AxisType.X ? dispX0 : dispY0;
            double dispXY1 = axisType == AxisType.X ? dispX1 : dispY1;
            shiftAxisMinMaxForDrag(axisView, dispXY0, dispXY1);
        }
    }

    /**
     * Translate Axes min/max values for mouse drag points.
     */
    private void shiftAxisMinMaxForDrag(AxisView anAxisView, double dispXY0, double dispXY1)
    {
        // Get Axis MinMax on press (I don't think this can ever be null)
        AxisType axisType = anAxisView.getAxisType();
        MinMax pressMinMax = _axesMinMaxOnPress.get(axisType);
        if (pressMinMax == null) { System.err.println("PanZoom: Null axis min/max"); return; }

        // Calculate new axis min/max for
        double dataXY1 = anAxisView.viewToData(dispXY0);
        double dataXY2 = anAxisView.viewToData(dispXY1);
        double dispMinXY = pressMinMax.getMin() - (dataXY2 - dataXY1);
        double dispMaxXY = pressMinMax.getMax() - (dataXY2 - dataXY1);

        // Set new X Axis min/max
        anAxisView.setAxisMin(dispMinXY);
        anAxisView.setAxisMax(dispMaxXY);
   }

    /**
     * Sets X/Y Axis min/max values for mouse drag points.
     */
    public void scaleAxesMinMaxForFactor(double aScale, boolean isAnimated)
    {
        double dispX = getWidth() / 2;
        double dispY = getHeight() / 2;
        scaleAxesMinMaxForFactorAndViewXY(null, aScale, aScale, dispX, dispY, isAnimated);
    }

    /**
     * Scales Axes min/max by factor (about axis center) and translates axis center to given X/Y.
     */
    private void scaleAxesMinMaxForViewAndEvent(View aView, ViewEvent anEvent)
    {
        // Get scale for Event
        boolean isModDown = anEvent.isAltDown();
        double scale = isModDown ? 2 : .5;

        // Get Axes for View
        AxisView[] axisViews = null;
        if (aView instanceof AxisView)
            axisViews = new AxisView[] { (AxisView) aView };

        // Scale Axes
        scaleAxesMinMaxForFactorAndViewXY(axisViews, scale, scale, anEvent.getX(), anEvent.getY(), true);
    }

    /**
     * Scales Axes min/max by factor (about axis center) and translates axis center to given X/Y.
     */
    public void scaleAxesMinMaxForFactorAndViewXY(AxisView[] axisViews, double aScaleX, double aScaleY, double dispX, double dispY, boolean isAnimated)
    {
        // If AxisViews not set, use all
        if (axisViews == null)
            axisViews = _chartHelper.getAxisViews();

        // Iterate over axes and scale
        for (AxisView axisView : axisViews) {
            AxisType axisType = axisView.getAxisType();
            double dispXY = axisType.isX() ? dispX : dispY;
            double dataXY = axisView.viewToData(dispXY);
            double scaleXY = axisType.isX() ? aScaleX : aScaleY;
            scaleAxisMinMaxForFactorAndDataMid(axisView, scaleXY, dataXY, isAnimated);
        }

        // If animated, restore targPoint when done
        if (isAnimated && _targPointOnPress != null) {
            axisViews[0].getAnim(0).setOnFinish(() -> {
                if (getChartView().getTargPoint() == null)
                    getChartView().setTargPoint(_targPointOnPress);
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
     * Sets X/Y Axis min/max values for mouse drag points.
     */
    private void scaleAxesMinMaxForViewAndScroll(View aView, ViewEvent aScrollEvent)
    {
        // Get scale: Assume + 1x per 100 points (1.5 inches). If scale down, limit to .5
        double scroll = aScrollEvent.getScrollY();
        double scale = Math.max(1 + scroll / 100, .5);

        // Get Mouse X/Y
        double dispX = aScrollEvent.getX();
        double dispY = aScrollEvent.getY();

        // Get AxisViews
        AxisView[] axisViews = _chartHelper.getAxisViews();
        if (aView instanceof AxisView)
            axisViews = new AxisView[] { (AxisView) aView };

        // Do scaleAxes
        scaleAxesMinMaxForFactorAboutViewXY(axisViews, scale, scale, dispX, dispY, false);
    }

    /**
     * Scales Axes min/max by factor about given X/Y (in display coords).
     */
    private void scaleAxesMinMaxForFactorAboutViewXY(AxisView[] axisViews, double aScaleX, double aScaleY, double dispX, double dispY, boolean isAnimated)
    {
        // Iterate over axes and scale
        for (AxisView axisView : axisViews) {
            AxisType axisType = axisView.getAxisType();
            double dispXY = axisType == AxisType.X ? dispX : dispY;
            double dataXY = axisView.viewToData(dispXY);
            double scaleXY = axisType == AxisType.X ? aScaleX : aScaleY;
            scaleAxisMinMaxForFactorAboutDataCoord(axisView, scaleXY, dataXY, isAnimated);
        }

        // If animated, restore targPoint when done
        if (isAnimated && _targPointOnPress != null) {
            axisViews[0].getAnim(0).setOnFinish(() -> {
                if (getChartView().getTargPoint() == null)
                    getChartView().setTargPoint(_targPointOnPress);
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
        double viewW = getWidth();
        double viewH = getHeight();
        double scaleX = aRect.width / viewW;
        double scaleY = aRect.height / viewH;
        double dispX = aRect.getMidX();
        double dispY = aRect.getMidY();
        scaleAxesMinMaxForFactorAndViewXY(null, scaleX, scaleY, dispX, dispY, true);
    }
}
