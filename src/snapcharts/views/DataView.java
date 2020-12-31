package snapcharts.views;
import snap.geom.Point;
import snap.gfx.Painter;
import snap.view.ViewEvent;
import snapcharts.model.ChartPart;
import snapcharts.model.DataPoint;

/**
 * A class to display data (via DataArea).
 */
public class DataView<T extends ChartPart> extends ChartPartView<T> {

    // The ChartView
    private ChartView  _chartView;

    // The ChartHelper
    private ChartHelper  _chartHelper;

    // The DataAreas
    private DataArea[]  _dataAreas;

    // A helper class to handle Pan/Zoom
    private DataViewPanZoom  _panZoomer;

    // Constants
    protected static int DEFAULT_REVEAL_TIME = 2000;

    /**
     * Constructor.
     */
    public DataView(ChartView aChartView)
    {
        _chartView = aChartView;

        // Create/set PanZoomer
        _panZoomer = new DataViewPanZoom(this);
        enableEvents(MousePress, MouseDrag, MouseRelease, Scroll, MouseMove, MouseExit);
    }

    /**
     * Returns the ChartPart.
     */
    public T getChartPart()  { return null; }

    /**
     * Returns the ChartView.
     */
    @Override
    public ChartView getChartView()  { return _chartView; }

    /**
     * Returns the DataArea.
     */
    public DataArea[] getDataAreas()  { return _dataAreas; }

    /**
     * Sets the DataArea.
     */
    protected void setDataAreas(DataArea[] theDataAreas)
    {
        _chartHelper = _chartView.getChartHelper();

        // Remove old
        if (_dataAreas !=null) {
            for (DataArea dataArea : _dataAreas)
                removeChild(dataArea);
        }

        // Set new
        _dataAreas = theDataAreas;

        // Add DataAreas as children
        for (DataArea dataArea : _dataAreas) {
            addChild(dataArea);
            dataArea.setDataView(this);
        }
    }

    /**
     * Returns the X Axis View.
     */
    public AxisViewX getAxisViewX()  { return _chartHelper.getAxisViewX(); }

    /**
     * Returns the Y Axis View.
     */
    public AxisViewY getAxisViewY()  { return _chartHelper.getAxisViewY(); }

    /**
     * Return the ratio of the chart to show horizontally.
     */
    public double getReveal()
    {
        return _chartView!=null ? _chartView.getReveal() : 1;
    }

    /**
     * Sets the reation of the chart to show horizontally.
     */
    public void setReveal(double aValue)
    {
        for (DataArea dataArea : getDataAreas())
            dataArea.setReveal(aValue);
    }

    /**
     * Returns the time in milliseconds recommended for animation.
     */
    protected int getRevealTime()
    {
        int revealTime = 0;
        for (DataArea dataArea : getDataAreas())
            revealTime = Math.max(revealTime, dataArea.getRevealTime());
        return revealTime;
    }

    /**
     * Returns the data point for given X/Y.
     */
    public DataPoint getDataPointForXY(double aX, double aY)
    {
        DataArea dataArea = _chartHelper.getDataAreaForFirstAxisY(); if (dataArea==null) return null;
        Point pnt = dataArea.parentToLocal(aX, aY, this);
        return dataArea.getDataPointForXY(pnt.x, pnt.y);
    }

    /**
     * Returns the given data point X/Y in this view coords.
     */
    public Point getDataPointXYLocal(DataPoint aDP)
    {
        DataArea dataArea = _chartHelper.getDataAreaForFirstAxisY(); if (dataArea==null) return null;
        Point pnt = dataArea.getDataPointXYLocal(aDP);
        return dataArea.localToParent(pnt.x, pnt.y, this);
    }

    /**
     * Returns whether view is in ZoomSelectMode.
     */
    public boolean isZoomSelectMode()  { return _panZoomer.isZoomSelectMode(); }

    /**
     * Sets whether view is in ZoomSelectMode.
     */
    public void setZoomSelectMode(boolean aValue)
    {
        if (!getChart().getType().isXYType()) return;
        _panZoomer.setZoomSelectMode(aValue);
    }

    /**
     * Sets X/Y Axis min/max values for mouse drag points.
     */
    public void scaleAxesMinMaxForFactor(double aScale, boolean isAnimated)
    {
        if (!getChart().getType().isXYType()) return;
        _panZoomer.scaleAxesMinMaxForFactor(aScale, isAnimated);
    }

    /**
     * Actual method to layout children.
     */
    protected void layoutImpl()
    {
        double viewW = getWidth();
        double viewH = getHeight();
        for (DataArea dataArea : getDataAreas())
            dataArea.setSize(viewW, viewH);
    }

    /**
     * Override to paint grid.
     */
    @Override
    protected void paintFront(Painter aPntr)
    {
        // Make DataArea for lowest Y axis paint gridlines
        DataArea dataArea = _chartHelper.getDataAreaForFirstAxisY();
        if (dataArea != null) {
            dataArea.paintGridlines(aPntr);
            dataArea.paintBorder(aPntr);
        }
    }

    /**
     * Override to forward to PanZoom.
     */
    @Override
    protected void paintAbove(Painter aPntr)
    {
        _panZoomer.paintAbove(aPntr);
    }

    /**
     * Override to forward to PanZoom.
     */
    protected void processEvent(ViewEvent anEvent)
    {
        // Handle MouseMove: Set ChartView.TargPoint
        if (anEvent.isMouseMove()) {
            Point pnt = localToParent(anEvent.getX(), anEvent.getY(), _chartView);
            _chartView.setTargPoint(pnt);
        }

        // Handle MouseExit: Clear ChartView.TargPoint
        else if (anEvent.isMouseExit())
            _chartView.setTargPoint(null);

        // Forward to PanZoom
        else if (getChart().getType().isXYType())
            _panZoomer.processEvent(anEvent);

        // Handle MouseClick
        if (anEvent.isMouseClick() && anEvent.getClickCount()==1) {
            DataPoint dpnt = getDataPointForXY(anEvent.getX(), anEvent.getY());
            if (dpnt == _chartView.getSelDataPoint())
                dpnt = null;
            _chartView.setSelDataPoint(dpnt);
        }

        // Do normal version
        super.processEvent(anEvent);
    }
}
