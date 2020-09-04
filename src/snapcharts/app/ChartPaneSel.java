package snapcharts.app;
import snap.gfx.Color;
import snap.gfx.Effect;
import snap.gfx.ShadowEffect;
import snap.view.View;
import snap.view.ViewEvent;
import snap.view.ViewUtils;
import snapcharts.model.Chart;
import snapcharts.model.ChartPart;
import snapcharts.views.*;

/**
 * A class to manage ChartPart selection in ChartPane.
 */
public class ChartPaneSel {

    // The ChartPane
    private ChartPane  _chartPane;

    // The ChartView
    private ChartView  _chartView;

    // The selected ChartPartView
    private ChartPartView  _selView;

    // The FocusEffect
    private Color FOCUSED_COLOR = Color.get("#039ed3");
    private Effect  FOCUSED_EFFECT = new ShadowEffect(5, FOCUSED_COLOR, 0, 0);

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
     * Returns the selected ChartPartView.
     */
    public ChartPartView getSelView()  { return _selView; }

    /**
     * Sets the selected ChartPartView.
     */
    public void setSelView(ChartPartView aView)
    {
        if (aView==_selView) return;

        // Clear effect
        if (_selView!=null && _selView.getEffect()==FOCUSED_EFFECT)
            _selView.setEffect(null);

        // Set SelView
        _selView = aView;

        // Set Effect
        if (_selView!=null)
            _selView.setEffect(FOCUSED_EFFECT);

        // Repaint/reset
        _chartPane.resetLater();
        _chartView.repaint();

        // Notify ChartPaneInsp
        _chartPane._insp.chartPaneSelChanged();
    }

    /**
     * Returns the selected part.
     */
    public ChartPart getSelChartPart()
    {
        Chart chart = _chartPane.getChart();
        ChartPartView view = getSelView();
        if (view instanceof ChartViewTop)
            return chart.getHeader();
        if (view instanceof AxisViewX)
            return chart.getAxisX();
        if (view instanceof AxisViewY)
            return chart.getAxisY();
        if (view instanceof LegendView)
            return chart.getLegend();
        return chart;
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
        return aView instanceof AxisView || aView instanceof LegendView || aView instanceof ChartViewTop;
    }
}
