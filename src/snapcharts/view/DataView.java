package snapcharts.view;
import snap.geom.Point;
import snap.gfx.Painter;
import snap.view.ViewEvent;
import snapcharts.model.Chart;
import snapcharts.model.ChartPart;
import snapcharts.model.DataPoint;
import snapcharts.model.DataSetList;

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
    public static int DEFAULT_REVEAL_TIME = 2000;

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
    public T getChartPart()
    {
        Chart chart = getChart();
        DataSetList dataList = chart.getDataSetList();
        ChartPart chartPart = dataList.getDataSetCount() > 0 ? dataList.getDataSet(0) : dataList;
        return (T) chartPart;
    }

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
        _chartHelper = getChartHelper();

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
        DataArea[] dataAreas = getDataAreas();
        for (DataArea dataArea : dataAreas)
            dataArea.setReveal(aValue);
    }

    /**
     * Returns the time in milliseconds recommended for animation.
     */
    protected int getRevealTime()
    {
        DataArea[] dataAreas = getDataAreas();
        int revealTime = 0;
        for (DataArea dataArea : dataAreas)
            revealTime = Math.max(revealTime, dataArea.getRevealTime());
        return revealTime;
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
            DataPoint dpnt = _chartHelper.getDataPointForViewXY(this, anEvent.getX(), anEvent.getY());
            if (dpnt == _chartView.getSelDataPoint())
                dpnt = null;
            _chartView.setSelDataPoint(dpnt);
        }

        // Do normal version
        super.processEvent(anEvent);
    }

    /**
     * Override to notify ChartHelper.
     */
    @Override
    public void setWidth(double aValue)
    {
        if (aValue==getWidth()) return;
        super.setWidth(aValue);
        _chartHelper.dataViewDidChangeSize();
    }

    /**
     * Override to notify ChartHelper.
     */
    @Override
    public void setHeight(double aValue)
    {
        if (aValue==getHeight()) return;
        super.setHeight(aValue);
        _chartHelper.dataViewDidChangeSize();
    }

    /**
     * Override to not set focus effect.
     */
    @Override
    public void setSelected(boolean aValue)
    {
        super.setSelected(aValue);
        setEffect(null);
    }
}
