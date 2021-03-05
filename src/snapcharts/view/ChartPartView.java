package snapcharts.view;
import snap.gfx.Border;
import snap.gfx.Color;
import snap.gfx.Effect;
import snap.gfx.ShadowEffect;
import snap.view.ParentView;
import snapcharts.model.Chart;
import snapcharts.model.ChartPart;

/**
 * A superclass for ChartView views.
 */
public abstract class ChartPartView<T extends ChartPart> extends ParentView {

    // Whether view is selected
    private boolean  _selected;

    // Constants for properties
    public static final String Selected_Prop = "Selected";

    // The FocusEffect
    private Color FOCUSED_COLOR = Color.get("#039ed3");
    private Effect FOCUSED_EFFECT = new ShadowEffect(5, FOCUSED_COLOR, 0, 0);

    /**
     * Constructor.
     */
    public ChartPartView()
    {
        super();
    }

    /**
     * Returns the ChartPart.
     */
    public abstract T getChartPart();

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
     * Returns the ChartView.
     */
    public ChartView getChartView()
    {
        return getParent(ChartView.class);
    }

    /**
     * Returns the DataView.
     */
    public DataView getDataView()
    {
        ChartView chartView = getChartView();
        return chartView.getDataView();
    }

    /**
     * Called to reset view from updated Chart.
     */
    protected void resetView()
    {
        // Get Axis
        ChartPart chartPart = getChartPart(); if (chartPart==null) return;

        // Update basic props
        setFont(chartPart.getFont());
        setFill(chartPart.getFill());

        // Update Border
        Border border = chartPart.getBorder();
        if (border!=null)
            setBorder(border);

        // Update effect
        Effect eff = chartPart.getEffect();
        if (eff!=null)
            setEffect(eff);
    }
}
