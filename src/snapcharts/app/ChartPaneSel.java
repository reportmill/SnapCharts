package snapcharts.app;
import snap.geom.Rect;
import snap.geom.RoundRect;
import snap.geom.Shape;
import snap.gfx.*;
import snap.view.*;
import snapcharts.model.*;
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

        // If SelPart not DataSet, clear SelDataPoint
        if (!(_selPart instanceof DataSet)) {
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

        // Update cursor
        Cursor cursor = getTargViewCursor();
        _chartView.setCursor(cursor);

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
        if (targView instanceof DataView)
            return Cursor.MOVE;
        return null;
    }

    /**
     * Returns the ChartPartView for given ChartPart.
     */
    private ChartPartView getChartPartViewForPart(ChartPart aChartPart)
    {
        // Handle Chart
        if (aChartPart instanceof Chart)
            return _chartView;

        // Handle Header
        if (aChartPart instanceof Header)
            return _chartView.getHeaderView();

        // Handle Axis
        if (aChartPart instanceof Axis) {
            Axis axis = (Axis) aChartPart;
            AxisType axisType = axis.getType();
            return _chartView.getChartHelper().getAxisView(axisType);
        }

        // Handle Legend
        if (aChartPart instanceof Legend)
            return _chartView.getLegendView();

        // Handle Marker
        if (aChartPart instanceof Marker) {
            for (MarkerView markerView : _chartView.getMarkerViews())
                if (markerView.getMarker() == aChartPart)
                    return markerView;
        }

        // Handle DataStyle
        if (aChartPart instanceof DataSetList || aChartPart instanceof DataSet)
            return _chartView.getDataView();

        // Handle unknown
        return null;
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
        if (anEvent.isMouseMove()) {
            ChartPart hitPart = getChartPartForXY(anEvent.getX(), anEvent.getY());
            setTargChartPart(hitPart);
        }

        // Handle MouseExit
        if (anEvent.isMouseExit()) {
            setTargChartPart(null);
        }

        // Handle MousePress
        else if (anEvent.isMousePress())
            _chartView.repaint();

        // Handle MouseRelease: If click, select part at event point, otherwise select chart
        else if (anEvent.isMouseRelease()) {

            // Handle Click: Select part at mouse point
            boolean isClick = anEvent.isEventWithinTimeAndDist(-1, 1);
            if (isClick) {
                ChartPart hitPart = getChartPartForXY(anEvent.getX(), anEvent.getY());
                setSelChartPart(hitPart);
            }

            // Otherwise, clear selection
            else setSelChartPart(getChart());

            // Repaint because targ paint might change
            _chartView.repaint();
        }
    }

    /**
     * Returns the ChartPart for given point XY in ChartView coords.
     */
    private ChartPart getChartPartForXY(double aX, double aY)
    {
        // Get the ChartPartView at XY
        ChartPartView hitView = getChartPartViewForXY(aX, aY);

        // Handle DataView special: If TargDataPoint, return DataPoint.DataSet
        if (hitView instanceof DataView) {
            DataPoint dataPoint = _chartView.getTargDataPoint();
            if (dataPoint != null)
                return dataPoint.getDataSet();
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
        View view = ViewUtils.getDeepestChildAt(_chartView, aX, aY, ChartPartView.class);

        // Correct for Selecting/Targeting
        while (view != null && !isSelectableView(view))
            view = view.getParent();

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
        return aView instanceof AxisView || aView instanceof LegendView || aView instanceof HeaderView ||
                aView instanceof DataView || aView instanceof MarkerView;
    }

    /**
     * Paints the selection and targeting.
     */
    protected void paintSelection(Painter aPntr, View aHostView)
    {
        // If MouseDown, just skip (because shadow render is kinda slow)
        if (ViewUtils.isMouseDown())
            return;

        // If SelView set, paint it
        View selView = getSelView();
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
    }

    /**
     * Paints the selection.
     */
    protected void paintSelView(Painter aPntr, View selView, View aHostView)
    {
        // Get SelView bounds shape in HostView coords
        Rect selViewBounds = selView.getBoundsLocal();
        if (selView instanceof AxisViewX)
            selViewBounds = selViewBounds.getInsetRect(-20, 0);
        if (selView instanceof AxisViewY)
            selViewBounds = selViewBounds.getInsetRect(0, -20);
        int inset = selView.getBorder() != null || selView.getFill() != null || selView instanceof DataView ? -3 : -1;
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
    }

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
        int inset = targView.getBorder() != null || targView.getFill() != null || targView instanceof DataView ? -3 : -1;
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
    public static ImageBox getImageBoxForShapeAndEffect(Shape aShape, Effect anEffect, Border aBorder)
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
}
