/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.app;
import snap.geom.*;
import snap.gfx.*;
import snap.util.SnapEnv;
import snap.view.*;
import snapcharts.charts.*;
import snapcharts.view.*;

/**
 * A class to manage ChartPart selection in ChartPane.
 */
public class ChartPaneSel {

    // The ChartPane
    private ChartPane  _chartPane;

    // The ChartView
    private ChartView  _chartView;

    // The selected ChartPart
    private ChartPart  _selPart;

    // The targeted ChartPart
    private ChartPart  _targPart;

    // The cached ImageBox/image for painting current selection, target
    private ImageBox  _selImageBox, _targImageBox;

    // The last mouse event
    private ViewEvent  _lastMouseEvent;

    // The handle being resized
    private Pos  _resizeHandle;

    // Records the time of last scroll so paint can happen with/without selection
    private long  _lastScrollTime;

    // Constant for max time between scroll events to be considered part of same scroll loop
    private static int SCROLL_TIMEOUT = 100;

    // Constants
    private static Color  SEL_COLOR = Color.get("#039ED3");
    private static Effect  SEL_EFFECT = new ShadowEffect(8, SEL_COLOR.darker(), 0, 0);
    private static Border  SEL_BORDER = Border.createLineBorder(SEL_COLOR.brighter(), 3);
    private static Color  TARGET_COLOR = Color.LIGHTGRAY.copyForAlpha(.4);
    private static Stroke  TARGET_STROKE = Stroke.Stroke1;
    private static Effect  TARG_EFFECT = new ShadowEffect(8, Color.GRAY, 0, 0);
    private static Border  TARG_BORDER = Border.createLineBorder(Color.LIGHTGRAY, 3);

    /**
     * Constructor.
     */
    public ChartPaneSel(ChartPane aChartPane)
    {
        // Set ChartPane
        _chartPane = aChartPane;
        _chartView = aChartPane.getChartView();

        // Start listening to ChartView MouseEvents (for selection/targeting)
        _chartView.addEventFilter(e -> chartViewMouseEvent(e), View.MouseEvents);
        _chartView.addEventFilter(e -> repaintOnScrollStop(), View.Scroll);

        // Start listening to ChartBox MousePress (for select Chart)
        _chartPane._chartBox.addEventHandler(e -> chartBoxMousePress(e), View.MousePress);
    }

    /**
     * Returns the chart.
     */
    private Chart getChart()  { return _chartView.getChart(); }

    /**
     * Returns the selected part.
     */
    public ChartPart getSelChartPart()  { return _selPart; }

    /**
     * Sets the selected part.
     */
    public void setSelChartPart(ChartPart aChartPart)
    {
        // If already set, just return
        if (aChartPart == _selPart) return;

        // Set new part
        _selPart = aChartPart;

        // Reset
        _chartPane.resetLater();

        // Notify ChartPane of change
        _chartPane.chartPaneSelChanged();
        _chartPane.getUI().repaint();

        // If SelPart not Trace, clear SelDataPoint
        if (!(_selPart instanceof Trace)) {
            _chartView.setSelDataPoint(null);
        }
    }

    /**
     * Returns the selected ChartPartView.
     */
    public ChartPartView getSelView()
    {
        return getChartPartViewForPart(_selPart);
    }

    /**
     * Pops the selection.
     */
    public void popSelection()
    {
        // Get parent of SelChartPart and select
        ChartPart selPart = getSelChartPart();
        ChartPart parPart = selPart != null ? selPart.getParent() : null;
        if (parPart != null)
            setSelChartPart(parPart);
    }

    /**
     * Returns the targeted part.
     */
    public ChartPart getTargChartPart()  { return _targPart; }

    /**
     * Sets the targeted part.
     */
    public void setTargChartPart(ChartPart aChartPart)
    {
        // If already set, just return
        if (aChartPart == _targPart) return;

        // Set new part
        _targPart = aChartPart;

        // Repaint
        _chartPane.getUI().repaint();
    }

    /**
     * Returns the targeted ChartPartView.
     */
    public ChartPartView getTargView()
    {
        return getChartPartViewForPart(_targPart);
    }

    /**
     * Returns the cursor for the TargView.
     */
    protected Cursor getTargViewCursor()
    {
        ChartPartView targView = getTargView();
        if (targView != null && targView.isMovable())
            return Cursor.MOVE;
        if (targView instanceof ContentView)
            return Cursor.MOVE;
        return null;
    }

    /**
     * Returns the ChartPartView for given ChartPart.
     */
    private ChartPartView getChartPartViewForPart(ChartPart aChartPart)
    {
        ChartHelper chartHelper = _chartView.getChartHelper();
        return chartHelper.getChartPartViewForPart(aChartPart);
    }

    /**
     * Called when ChartBox gets mouse press outside chart to reset SelChartPart to Chart.
     */
    private void chartBoxMousePress(ViewEvent anEvent)
    {
        double chartX = anEvent.getX() - _chartView.getX();
        double chartY = anEvent.getY() - _chartView.getY();
        if (!_chartView.contains(chartX, chartY))
            setSelChartPart(getChart());
    }

    /**
     * Called when ChartView gets mouse event.
     */
    private void chartViewMouseEvent(ViewEvent anEvent)
    {
        // Handle MouseMove
        if (anEvent.isMouseMove())
            mouseMoved(anEvent);

        // Handle MouseExit
        if (anEvent.isMouseExit()) {
            if (!ViewUtils.isMouseDown())
                setTargChartPart(null);
        }

        // Handle MousePress, MouseDrag, MouseRelease
        else if (anEvent.isMousePress())
            mousePressed(anEvent);
        else if (anEvent.isMouseDrag())
            mouseDragged(anEvent);
        else if (anEvent.isMouseRelease()) {
            mouseReleased(anEvent);
            _chartPane.getUI().repaint();
            _chartPane.resetLater();
        }
    }

    /**
     * MouseMoved.
     */
    private void mouseMoved(ViewEvent anEvent)
    {
        // If event over SelView and resizable, and over handle, set cursor and return
        ChartPartView selView = getSelView();
        if (selView != null && selView.isResizable()) {
            Pos handlePos = getHandlePosForPoint(selView, _chartView, anEvent.getX(), anEvent.getY());
            if (handlePos != null) {
                Cursor cursor = Cursor.get(handlePos);
                _chartView.setCursor(cursor);
                return;
            }
        }

        // Update target
        ChartPart hitPart = getChartPartForXY(anEvent.getX(), anEvent.getY());
        setTargChartPart(hitPart);

        // Update cursor
        Cursor cursor = getTargViewCursor();
        _chartView.setCursor(cursor);
    }

    /**
     * MousePressed.
     */
    private void mousePressed(ViewEvent anEvent)
    {
        _chartPane.getUI().repaint();
        _lastMouseEvent = anEvent;

        // If event over SelView and resizable, and over handle, set handle and return
        ChartPartView selView = getSelView();
        if (selView != null && selView.isResizable()) {
            Pos handlePos = getHandlePosForPoint(selView, _chartView, anEvent.getX(), anEvent.getY());
            if (handlePos != null) {
                _resizeHandle = handlePos;
                anEvent.consume();
                return;
            }
        }

        // If Movable, consume event
        ChartPartView targView = getTargView();
        if (targView != null && targView.isMovable())
            anEvent.consume();
    }

    /**
     * MouseDragged.
     */
    private void mouseDragged(ViewEvent anEvent)
    {
        // If resizing SelView, forward to SelView and return
        if (_resizeHandle != null) {
            ChartPartView selView = getSelView();
            selView.processResizeEvent(anEvent, _lastMouseEvent, _resizeHandle);
            _lastMouseEvent = anEvent;
            anEvent.consume();
        }

        // If moving TargView, forward to TargView and return
        ChartPartView targView = getTargView();
        if (targView != null && targView.isMovable()) {
            targView.processMoveEvent(anEvent, _lastMouseEvent);
            _lastMouseEvent = anEvent;
            anEvent.consume();
        }
    }

    /**
     * MouseReleased.
     */
    private void mouseReleased(ViewEvent anEvent)
    {
        // If resizing, clear and return
        if (_resizeHandle != null) {
            _resizeHandle = null;
            anEvent.consume();
            return;
        }

        // If Movable, consume event, ensure targView selected, return
        ChartPartView targView = getTargView();
        if (targView != null && targView.isMovable()) {
            anEvent.consume();
            ChartPart selPart = targView.getChartPart();
            setSelChartPart(selPart);
            return;
        }

        // Handle Click: Select part at mouse point
        boolean isClick = anEvent.isEventWithinTimeAndDist(-1, 1);
        if (isClick) {
            ChartPart hitPart = getChartPartForXY(anEvent.getX(), anEvent.getY());
            setSelChartPart(hitPart);
        }

        // Otherwise, clear selection
        else {
            Chart chart = getChart();
            if (!chart.getTraceType().is3D())
                setSelChartPart(chart);
        }
    }

    /**
     * Returns the ChartPart for given point XY in ChartView coords.
     */
    private ChartPart getChartPartForXY(double aX, double aY)
    {
        // Get the ChartPartView at XY
        ChartPartView hitView = getChartPartViewForXY(aX, aY);

        // Handle ContentView special: If TargDataPoint, return TracePoint.Trace
        if (hitView instanceof ContentView) {
            TracePoint dataPoint = _chartView.getTargDataPoint();
            if (dataPoint != null)
                return dataPoint.getTrace();
        }

        // Get chartPart for hit view and return
        ChartPart chartPart = hitView != null ? hitView.getChartPart() : getChart();
        return chartPart;
    }

    /**
     * Returns the ChartPart for given point XY in ChartView coords.
     */
    private ChartPartView getChartPartViewForXY(double aX, double aY)
    {
        // Check Markers first
        MarkerView markerView = getMarkerViewForXY(aX, aY);
        if (markerView != null)
            return markerView;

        // Get deepest selectable view for X/Y
        View view = _chartView.getChildChartPartViewDeepForXY(aX, aY);

        // Correct for Selecting/Targeting
        //while (view != null && !isSelectableView(view)) view = view.getParent();

        // If as ChartPartView (or null)
        return view instanceof ChartPartView ? (ChartPartView) view : null;
    }

    /**
     * Returns the MarkerView for given point XY in ChartView coords.
     */
    private MarkerView getMarkerViewForXY(double aX, double aY)
    {
        // Check Markers first
        MarkerView[] markerViews = _chartView.getMarkerViews();
        for (MarkerView markerView : markerViews) {

            // Get MarkerView bounds
            double markX = markerView.getX();
            double markY = markerView.getY();
            double markW = markerView.getWidth();
            double markH = markerView.getHeight();

            // Constrain min bounds width/height to 10 (centered) so we can still select line or thin markers
            if (markW < 10) {
                int markMidX = (int) Math.round(markX + markW / 2);
                markX = markMidX - 5; markW = 10;
            }
            if (markH < 10) {
                int markMidY = (int) Math.round(markY + markH / 2);
                markY = markMidY - 5; markH = 10;
            }

            // If MarkerView bounds contains XY, return it
            if (Rect.contains(markX, markY, markW, markH, aX, aY))
                return markerView;
        }

        // Return null since no MarkerView at point
        return null;
    }

    /**
     * Returns whether view is selectable view.
     */
    private boolean isSelectableView(View aView)
    {
        return aView instanceof HeaderView || aView instanceof AxisView || aView instanceof ContourAxisView ||
                aView instanceof ContentView || aView instanceof LegendView || aView instanceof MarkerView;
    }

    /**
     * Paints the selection and targeting.
     */
    protected void paintSelection(Painter aPntr, View aHostView)
    {
        // If MouseDown, just skip (because shadow render is kinda slow)
        if (ViewUtils.isMouseDown() || _lastScrollTime > 0)
            return;

        // Clip to ChartView
        aPntr.save();
        Rect clipBounds = _chartView.localToParent(_chartView.getBoundsLocal(), aHostView).getBounds();
        aPntr.clip(clipBounds);

        // If SelView set, paint it
        ChartPartView selView = getSelView();
        if (selView != null && selView != _chartView) {
            if (selView.isShowing())
                paintSelView(aPntr, selView, aHostView);
        }

        // If TargView set, paint it
        View targView = getTargView();
        if (targView != null && targView != selView && targView != _chartView) {
            if (targView.isShowing())
                paintTargView(aPntr, targView, aHostView);
        }

        // Restore
        aPntr.restore();
    }

    /**
     * Paints the selection.
     */
    protected void paintSelView(Painter aPntr, ChartPartView selView, View aHostView)
    {
        // Get SelView bounds shape in HostView coords
        Rect selViewBounds = selView.getBoundsLocal();
        if (selView instanceof AxisViewX)
            selViewBounds = selViewBounds.getInsetRect(-20, 0);
        if (selView instanceof AxisViewY)
            selViewBounds = selViewBounds.getInsetRect(0, -20);
        int inset = selView.getBorder() != null || selView.getFill() != null || selView instanceof ContentView ? -3 : -1;
        selViewBounds = selViewBounds.getInsetRect(inset);

        // Get SelView bounds as RoundRect shape in HostView coords
        Shape selViewShape = new RoundRect(selViewBounds, 4);
        Shape selViewShapeInHost = selView.localToParent(selViewShape, aHostView);

        // Get image box for SelView bounds shape
        ImageBox imgBox = getSelImageBoxForShape(selViewShapeInHost);

        // Paint ImageBox at SelView origin in Host coords
        Rect selViewBoundsInHost = selViewShapeInHost.getBounds();
        aPntr.setOpacity(.7);
        imgBox.paintImageBox(aPntr, selViewBoundsInHost.x, selViewBoundsInHost.y);
        aPntr.setOpacity(1);

        // If resizable, paint handles
        if (selView.isResizable())
            paintSelViewHandles(aPntr, selView, aHostView);
    }

    /**
     * Paints the selection.
     */
    protected void paintSelViewHandles(Painter aPntr, ChartPartView selView, View aHostView)
    {
        Rect[] handleRects = getHandleRects(selView, aHostView);
        for (Rect handleRect : handleRects)
            paintSelViewHandleForRect(aPntr, handleRect);
    }

    /**
     * Paints the selection.
     */
    protected void paintSelViewHandleForRect(Painter aPntr, Rect handleRect)
    {
        Image handleImage = getHandleImage();
        aPntr.drawImage(handleImage, handleRect.x - 4, handleRect.y - 4);
    }

    /**
     * Returns the handle image.
     */
    private Image getHandleImage()
    {
        if (_handleImage != null) return _handleImage;
        ShapeView shapeView = new ShapeView();
        shapeView.setShape(new Rect(0, 0, 9, 9));
        shapeView.sizeToShape();
        shapeView.setFill(Color.WHITE);
        shapeView.setBorder(Color.DARKGRAY, 1);
        shapeView.setEffect(new ShadowEffect(3, Color.LIGHTGRAY, 0, 0));
        BoxView boxView = new BoxView(shapeView);
        boxView.setPadding(4, 4, 4, 4);
        boxView.setSizeToPrefSize();
        Image image = ViewUtils.getImageForScale(boxView, -1);
        return _handleImage = image;
    }

    // Handle image
    private static Image  _handleImage;

    /**
     * Paints the targeting.
     */
    protected void paintTargView(Painter aPntr, View targView, View aHostView)
    {
        // Get TargView bounds
        Rect targViewBounds = targView.getBoundsLocal();
        if (targView instanceof AxisViewX)
            targViewBounds = targViewBounds.getInsetRect(-20, 0);
        if (targView instanceof AxisViewY)
            targViewBounds = targViewBounds.getInsetRect(0, -20);
        int inset = targView.getBorder() != null || targView.getFill() != null || targView instanceof ContentView ? -3 : -1;
        targViewBounds = targViewBounds.getInsetRect(inset);

        // Get TargView bounds as RoundRect shape in HostView coords
        Shape targViewShape = new RoundRect(targViewBounds, 4);
        Shape targViewShapeInHost = targView.localToParent(targViewShape, aHostView);

        // If MouseDown, paint effect
        if (ViewUtils.isMouseDown()) {
            ImageBox imgBox = getTargImageBoxForShape(targViewShapeInHost);
            Rect targViewBoundsInHost = targViewShapeInHost.getBounds();
            aPntr.setOpacity(.7);
            imgBox.paintImageBox(aPntr, targViewBoundsInHost.x, targViewBoundsInHost.y);
            aPntr.setOpacity(1);
        }

        // Otherwise, Paint TargViewShapeInHost
        else {
            aPntr.setStroke(TARGET_STROKE);
            aPntr.setColor(TARGET_COLOR);
            aPntr.draw(targViewShapeInHost);
        }
    }

    /**
     * Returns handle rects for view.
     */
    private Rect[] getHandleRects(ChartPartView aView, View aHostView)
    {
        // Get View bounds in HostView coords
        Point viewXY = aView.localToParent(0, 0, aHostView);
        Rect viewBounds = new Rect(viewXY.x, viewXY.y, aView.getWidth(), aView.getHeight());

        // Iterate over handle positions and get handle rect for each
        Pos[] posValues = aView.getHandlePositions();
        Rect[] handleRects = new Rect[posValues.length];
        for (int i = 0; i < posValues.length; i++) {
            Pos pos = posValues[i];
            handleRects[i] = getHandleRect(viewBounds, pos);
        }

        // Return handle rects
        return handleRects;
    }

    /**
     * Returns handle rect for view and pos
     */
    private Rect getHandleRect(Rect aRect, Pos aPos)
    {
        Point posPoint = aRect.getPoint(aPos);
        return new Rect(posPoint.x - 4, posPoint.y - 4, 8, 8);
    }

    /**
     * Returns a handle position for given rect.
     */
    private Pos getHandlePosForPoint(ChartPartView aView, View aHostView, double aX, double aY)
    {
        Rect[] handleRects = getHandleRects(aView, aHostView);
        for (int i = 0; i < handleRects.length; i++) {
            Rect handleRect = handleRects[i];
            if (handleRect.contains(aX, aY))
                return aView.getHandlePositions()[i];
        }

        return null;
    }

    /**
     * Returns the image box for sel view.
     */
    private ImageBox getSelImageBoxForShape(Shape selViewShapeInHost)
    {
        // If ImageBox with same size already set, use it
        if (_selImageBox != null && _selImageBox.getWidth() == selViewShapeInHost.getWidth() &&
                _selImageBox.getHeight() == selViewShapeInHost.getHeight())
            return _selImageBox;

        // Get image box for SelView bounds shape
        ImageBox imgBox = getImageBoxForShapeAndEffect(selViewShapeInHost, SEL_EFFECT, SEL_BORDER);
        return _selImageBox = imgBox;
    }

    /**
     * Returns the image box for targ view.
     */
    private ImageBox getTargImageBoxForShape(Shape targViewShapeInHost)
    {
        // If ImageBox with same size already set, use it
        if (_targImageBox != null && _targImageBox.getWidth() == targViewShapeInHost.getWidth() &&
                _targImageBox.getHeight() == targViewShapeInHost.getHeight())
            return _targImageBox;

        // Get image box for SelView bounds shape
        ImageBox imgBox = getImageBoxForShapeAndEffect(targViewShapeInHost, TARG_EFFECT, TARG_BORDER);
        return _targImageBox = imgBox;
    }

    /**
     * Returns an image for given effect and shape.
     */
    private static ImageBox getImageBoxForShapeAndEffect(Shape aShape, Effect anEffect, Border aBorder)
    {
        // Create ShapeView for given Shape, effect and border
        Rect bnds = aShape.getBounds();
        Shape shape = aShape.copyForBounds(0, 0, bnds.width, bnds.height);
        ShapeView shapeView = new ShapeView(shape);
        shapeView.setBorder(aBorder);
        shapeView.setFill(aBorder.getColor());
        shapeView.setEffect(anEffect);

        // Get image and stamp out inner shadow
        ImageBox imgBox = ViewUtils.getImageBoxForScale(shapeView, -1);
        Image img = imgBox.getImage();
        Painter pntr = img.getPainter();
        pntr.setColor(Color.CLEAR);
        pntr.translate(-imgBox.getImageBounds().x, -imgBox.getImageBounds().y);
        pntr.setComposite(Painter.Composite.SRC_IN);
        pntr.clip(shape); // For browser, because browser has slightly different idea of SRC_IN
        pntr.fill(shape);
        pntr.setComposite(Painter.Composite.SRC_OVER);

        // Return image
        return imgBox;
    }

    /**
     * Called to register for repaint
     */
    protected void repaintOnScrollStop()
    {
        // The browser is fast enough to paint selection
        if (SnapEnv.isTeaVM) {
            _chartView.repaint(); return; }

        // If not scrolling, set time, check for stop and repaint
        if (_lastScrollTime == 0) {
            _lastScrollTime = System.currentTimeMillis();
            checkScrollStop();
            _chartPane.getUI().repaint();
        }

        // If scrolling, just update last time
        else _lastScrollTime = System.currentTimeMillis();
    }

    /**
     * Called periodically to see if scrolling has stopped.
     */
    private void checkScrollStop()
    {
        // If max time between last scroll event has elapsed, reset LastScrollTime and repaint
        long time = System.currentTimeMillis();
        if (time - _lastScrollTime > SCROLL_TIMEOUT) {
            _lastScrollTime = 0;
            _chartPane.getUI().repaint();
        }

        // Otherwise kick off another periodic check
        else ViewUtils.runDelayed(() -> checkScrollStop(), SCROLL_TIMEOUT);
    }
}
