package snapcharts.view;
import snap.geom.Point;
import snap.geom.Rect;
import snap.gfx.Color;
import snap.gfx.Painter;
import snap.gfx.Stroke;
import snap.view.ViewEvent;
import snapcharts.model.AxisType;
import snapcharts.util.MinMax;

import java.util.HashMap;
import java.util.Map;

/**
 * A helper class to help DataView do Pan/Zoom.
 */
public class DataViewPanZoom {

    // The ChartHelper
    private ChartHelper  _chartHelper;

    // The DataView
    private DataView  _dataView;

    // Whether view is in zoom select mode
    private boolean  _zoomSelectMode;

    // The current ZoomSelect rect
    private Rect _zoomSelectRect;

    // The last MousePress point
    private Point  _pressPoint;

    // A Map of MinMax for each Axis on mousePress
    private Map<AxisType, MinMax>  _axesMinMaxOnPress =  new HashMap<>();

    // Constants
    private static final Stroke ZOOM_RECT_STROKE = Stroke.StrokeDash1;

    /**
     * Constructor.
     */
    public DataViewPanZoom(DataView aDataView)
    {
        _dataView = aDataView;
        _chartHelper = _dataView.getChartHelper();
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
        _chartHelper = _dataView.getChartHelper();

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
                _chartHelper.resetAxesAnimated();
            }

            // Store axes min/max values on press
            AxisView[] axisViews = _chartHelper.getAxisViews();
            for (AxisView axisView : axisViews) {
                double axisMin = axisView.getAxisMin();
                double axisMax = axisView.getAxisMax();
                _axesMinMaxOnPress.put(axisView.getAxisType(), new MinMax(axisMin, axisMax));
            }

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
        // Iterate over axes and scale
        AxisView[] axisViews = _chartHelper.getAxisViews();
        for (AxisView axisView : axisViews) {

            // Get Axis MinMax on press (I don't think this can ever be null)
            AxisType axisType = axisView.getAxisType();
            MinMax pressMinMax = _axesMinMaxOnPress.get(axisType);
            if (pressMinMax == null) { System.err.println("PanZoom: Null axis min/max"); return; }

            // Calculate new axis min/max for
            double dispXY0 = axisType == AxisType.X ? dispX0 : dispY0;
            double dispXY1 = axisType == AxisType.X ? dispX1 : dispY1;
            double dataXY1 = axisView.viewToData(dispXY0);
            double dataXY2 = axisView.viewToData(dispXY1);
            double dispMinXY = pressMinMax.getMin() - (dataXY2 - dataXY1);
            double dispMaxXY = pressMinMax.getMax() - (dataXY2 - dataXY1);

            // Set new X Axis min/max
            axisView.setAxisMin(dispMinXY);
            axisView.setAxisMax(dispMaxXY);
        }
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

        // Iterate over axes and scale
        AxisView[] axisViews = _chartHelper.getAxisViews();
        for (AxisView axisView : axisViews) {
            AxisType axisType = axisView.getAxisType();
            double dispXY = axisType == AxisType.X ? dispX : dispY;
            double dataXY = axisView.viewToData(dispXY);
            double scaleXY = axisType == AxisType.X ? aScaleX : aScaleY;
            scaleAxisMinMaxForFactorAndDataMid(axisView, scaleXY, dataXY, isAnimated);
        }

        // If animated, restore targPoint when done
        if (isAnimated && targPoint!=null) {
            AxisView axisViewX = _chartHelper.getAxisViewX();
            axisViewX.getAnim(0).setOnFinish(() -> {
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

        // Iterate over axes and scale
        AxisView[] axisViews = _chartHelper.getAxisViews();
        for (AxisView axisView : axisViews) {
            AxisType axisType = axisView.getAxisType();
            double dispXY = axisType == AxisType.X ? dispX : dispY;
            double dataXY = axisView.viewToData(dispXY);
            double scaleXY = axisType == AxisType.X ? aScaleX : aScaleY;
            scaleAxisMinMaxForFactorAboutDataCoord(axisView, scaleXY, dataXY, isAnimated);
        }

        // If animated, restore targPoint when done
        if (isAnimated && targPoint!=null) {
            AxisView axisViewX = _chartHelper.getAxisViewX();
            axisViewX.getAnim(0).setOnFinish(() -> {
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
