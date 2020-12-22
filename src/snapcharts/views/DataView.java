package snapcharts.views;
import snap.geom.Insets;
import snap.geom.Point;
import snap.gfx.Painter;
import snap.view.ViewEvent;
import snapcharts.model.ChartPart;
import snapcharts.model.Intervals;

/**
 * A class to display data (via DataArea).
 */
public class DataView<T extends ChartPart> extends ChartPartView<T> {

    // The ChartView
    private ChartView  _chartView;

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
        enableEvents(MousePress, MouseDrag, MouseRelease, Scroll);
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
    public AxisViewX getAxisX()  { return _chartView.getAxisX(); }

    /**
     * Returns the Y Axis View.
     */
    public AxisViewY getAxisY()  { return _chartView.getAxisY(); }

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
     * Returns the X axis intervals for active datasets.
     */
    public Intervals getIntervalsX()
    {
        return getAxisX().getIntervals();
    }

    /**
     * Returns the Y axis intervals for active datasets.
     */
    public Intervals getIntervalsY()
    {
        return getAxisY().getIntervals();
    }

    /**
     * Converts a point from dataset coords to view coords.
     */
    public Point dataToView(double dataX, double dataY)
    {
        double dispX = dataToViewX(dataX);
        double dispY = dataToViewY(dataY);
        return new Point(dispX, dispY);
    }

    /**
     * Converts a X coord from data coords to view coords.
     */
    public double dataToViewX(double dataX)
    {
        Insets ins = getInsetsAll();
        Intervals intervals = getIntervalsX();
        double dataMin = intervals.getMin();
        double dataMax = intervals.getMax();
        double axisX = ins.left;
        double areaW = getWidth() - ins.getWidth();
        return axisX + (dataX - dataMin)/(dataMax - dataMin)*areaW;
    }

    /**
     * Converts a X coord from data coords to view coords.
     */
    public double dataToViewY(double dataY)
    {
        Insets ins = getInsetsAll();
        Intervals intervals = getIntervalsY();
        double dataMin = intervals.getMin();
        double dataMax = intervals.getMax();
        double areaY = ins.top;
        double areaH = getHeight() - ins.getHeight();
        return areaY + areaH - (dataY - dataMin)/(dataMax - dataMin)*areaH;
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
        // Forward to PanZoom
        if (getChart().getType().isXYType())
            _panZoomer.processEvent(anEvent);

        // Do normal version
        super.processEvent(anEvent);
    }
}
