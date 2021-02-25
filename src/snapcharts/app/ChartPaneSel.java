package snapcharts.app;
import snap.view.View;
import snap.view.ViewEvent;
import snap.view.ViewUtils;
import snapcharts.model.*;
import snapcharts.views.*;

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

    /**
     * Constructor.
     */
    public ChartPaneSel(ChartPane aChartPane)
    {
        // Set ChartPane
        _chartPane = aChartPane;
        _chartView = aChartPane.getChartView();

        // Start listening to ChartView
        _chartView.addEventFilter(e -> chartViewMouseEvent(e), View.MousePress);
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

        // Clear last SelView.Selected
        ChartPartView selViewOld = getSelView();
        if (selViewOld != null)
            selViewOld.setSelected(false);

        // Set new part
        _selPart = aChartPart;

        // Set new SelView.Selected
        ChartPartView selViewNew = getSelView();
        if (selViewNew != null)
            selViewNew.setSelected(true);

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
     * Returns the ChartPartView for given ChartPart.
     */
    private ChartPartView getChartPartViewForPart(ChartPart aChartPart)
    {
        if (aChartPart instanceof Chart)
            return _chartView;
        if (aChartPart instanceof Header)
            return _chartView.getHeaderView();
        if (aChartPart instanceof Axis) {
            Axis axis = (Axis) aChartPart;
            AxisType axisType = axis.getType();
            return _chartView.getChartHelper().getAxisView(axisType);
        }
        if (aChartPart instanceof Legend)
            return _chartView.getLegendView();
        if (aChartPart instanceof ChartTypeProps)
            return _chartView.getDataView();
        return null;
    }

    /**
     * Called when ChartView gets mouse event.
     */
    private void chartViewMouseEvent(ViewEvent anEvent)
    {
        // Handle MousePress
        if (anEvent.isMousePress()) {
            ChartPartView partView = getChartPartViewForXY(anEvent.getX(), anEvent.getY());
            setSelView(partView);
        }
    }

    /**
     * Returns the ChartPart for XY values.
     */
    private ChartPartView getChartPartViewForXY(double aX, double aY)
    {
        View view = ViewUtils.getDeepestChildAt(_chartView, aX, aY, ChartPartView.class);
        while (view!=null && !isSelectableView(view))
            view = view.getParent();
        return view instanceof ChartPartView ? (ChartPartView)view : null;
    }

    /**
     * Returns whether view is selectable view.
     */
    private boolean isSelectableView(View aView)
    {
        return aView instanceof AxisView || aView instanceof LegendView || aView instanceof HeaderView || aView instanceof DataView;
    }
}
