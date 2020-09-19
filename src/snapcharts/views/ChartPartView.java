package snapcharts.views;
import snap.gfx.Color;
import snap.gfx.Effect;
import snap.gfx.ShadowEffect;
import snap.view.ParentView;
import snapcharts.model.Chart;
import snapcharts.model.DataSetList;

/**
 * A superclass for ChartView views.
 */
public class ChartPartView extends ParentView {

    // Whether view is selected
    private boolean  _selected;

    // Constants for properties
    public static final String Selected_Prop = "Selected";

    // The FocusEffect
    private Color FOCUSED_COLOR = Color.get("#039ed3");
    private Effect FOCUSED_EFFECT = new ShadowEffect(5, FOCUSED_COLOR, 0, 0);

    /**
     * Returns whether selected.
     */
    public boolean isSelected()  { return _selected; }

    /**
     * Sets whether selected.
     */
    public void setSelected(boolean aValue)
    {
        // If value already set, just return
        if (aValue==isSelected()) return;

        // Set value
        firePropChange(Selected_Prop, _selected, _selected = aValue);

        // Update effect
        if (!aValue && getEffect()==FOCUSED_EFFECT)
            setEffect(null);
        if (aValue && getEffect()==null)
            setEffect(FOCUSED_EFFECT);
    }

    /**
     * Returns the chart.
     */
    public Chart getChart()
    {
        ChartView chartView = getChartView();
        return chartView.getChart();
    }

    /**
     * Returns the DataSetList.
     */
    public DataSetList getDataSetList()
    {
        Chart chart = getChart();
        return chart!=null ? chart.getDataSetList() : null;
    }

    /**
     * Returns the ChartView.
     */
    public ChartView getChartView()
    {
        return getParent(ChartView.class);
    }

    /**
     * Returns the ChartArea.
     */
    public ChartArea getChartArea()
    {
        ChartView chartView = getChartView();
        return chartView.getChartArea();
    }

    /**
     * Called to reset view from updated Chart.
     */
    protected void resetView()  { }
}
