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

    // Constants
    protected static Stroke TARGET_BORDER_STROKE = new Stroke(2);
    protected static Color TARGET_BORDER_COLOR = Color.BLUE.blend(Color.CLEARWHITE, .5);

    /**
     * Constructor.
     */
    public ChartPaneSel(ChartPane aChartPane)
    {
        // Set ChartPane
        _chartPane = aChartPane;
        _chartView = aChartPane.getChartView();

        // Start listening to ChartView
        _chartView.addEventFilter(e -> chartViewMouseEvent(e), View.MouseEvents);
    }

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

        // Notify ChartPaneInsp
        _chartPane._insp.chartPaneSelChanged();
    }

    /**
     * Returns the selected ChartPartView.
     */
    public ChartPartView getSelView()
    {
        return getChartPartViewForPart(_selPart);
    }

    /**
     * Sets the selected ChartPartView.
     */
    public void setSelView(ChartPartView aView)
    {
        ChartPart chartPart = aView!=null ? aView.getChartPart() : _chartView.getChart();
        setSelChartPart(chartPart);
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
    }

    /**
     * Returns the targeted ChartPartView.
     */
    public ChartPartView getTargView()
    {
        return getChartPartViewForPart(_targPart);
    }

    /**
     * Sets the targeted ChartPartView.
     */
    public void setTargView(ChartPartView aView)
    {
        ChartPart chartPart = aView!=null ? aView.getChartPart() : _chartView.getChart();
        setTargChartPart(chartPart);
        _chartPane.getUI().repaint();
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

        // Handle ChartStyle
        if (aChartPart instanceof DataSetList || aChartPart instanceof DataSet)
            return _chartView.getDataView();

        // Handle unknown
        return null;
    }

    /**
     * Called when ChartView gets mouse event.
     */
    private void chartViewMouseEvent(ViewEvent anEvent)
    {
        // Handle MouseMove
        if (anEvent.isMouseMove()) {
            ChartPartView partView = getChartPartViewForXY(anEvent.getX(), anEvent.getY(), true);
            setTargView(partView);
        }

        // Handle MouseExit
        if (anEvent.isMouseExit()) {
            setTargView(null);
        }

        // Handle MousePress
        else if (anEvent.isMousePress()) {
            ChartPartView partView = getChartPartViewForXY(anEvent.getX(), anEvent.getY(), false);
            setSelView(partView);
        }
    }

    /**
     * Returns the ChartPart for XY values.
     */
    private ChartPartView getChartPartViewForXY(double aX, double aY, boolean isTargeting)
    {
        // Get deepest selectable view for X/Y
        View view = ViewUtils.getDeepestChildAt(_chartView, aX, aY, ChartPartView.class);

        // Correct for Selecting/Targeting
        while (!isTargeting && view!=null && !isSelectableView(view))
            view = view.getParent();
        while (isTargeting && view!=null && !isTargetableView(view))
            view = view.getParent();

        // If DataView but no TargDataPoint, return ChartView
        if (!isTargeting && view instanceof DataView && _chartView.getTargDataPoint() == null)
            return _chartView;

        // If as ChartPartView (or null)
        return view instanceof ChartPartView ? (ChartPartView) view : null;
    }

    /**
     * Returns whether view is selectable view.
     */
    private boolean isSelectableView(View aView)
    {
        return aView instanceof AxisView || aView instanceof LegendView || aView instanceof HeaderView || aView instanceof DataView;
    }

    /**
     * Returns whether view is targetable view.
     */
    private boolean isTargetableView(View aView)
    {
        return aView instanceof AxisView || aView instanceof LegendView || aView instanceof HeaderView || aView instanceof DataView;
    }

    /**
     * Paints the selection and targeting.
     */
    protected void paintSelection(Painter aPntr, View aHostView)
    {
        // If SelView set, paint it
        View selView = getSelView();
        if (selView != null && selView != _chartView)
            paintSelView(aPntr, selView, aHostView);

        // If TargView set, paint it
        View targView = getTargView();
        if (targView != null && targView != selView && targView != _chartView)
            paintTargView(aPntr, targView, aHostView);
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
        Color selColor = Color.get("#039ED3");
        Effect selEffect = new ShadowEffect(8, selColor.darker(), 0, 0);
        Border selBorder = Border.createLineBorder(selColor.brighter(), 3);
        ImageBox imgBox = getImageBoxForShapeAndEffect(selViewShapeInHost, selEffect, selBorder);

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

        // Paint TargViewShapeInHost
        Stroke TARGET_STROKE = Stroke.Stroke1;
        Color TARGET_COLOR = Color.LIGHTGRAY.copyForAlpha(.4);
        aPntr.setStroke(TARGET_STROKE);
        aPntr.setColor(TARGET_COLOR);
        aPntr.draw(targViewShapeInHost);
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
