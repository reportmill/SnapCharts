package snapcharts.views;
import snap.geom.RoundRect;
import snap.geom.Shape;
import snap.gfx.Border;
import snap.gfx.Color;
import snap.gfx.Effect;
import snap.gfx.ShadowEffect;
import snap.view.ColView;
import snapcharts.app.ChartPane;
import snapcharts.model.Chart;
import java.util.List;

/**
 * A view to show a page of charts.
 */
public class PageView extends ColView {

    // The charts
    private List<Chart> _charts;

    // Constants
    public static Border PAGE_BORDER = Border.createLineBorder(Color.GRAY, 1);
    public static Effect PAGE_SHADOW = new ShadowEffect(10, Color.DARKGRAY, 0, 0).copySimple();

    /**
     * Constructor.
     */
    public PageView()
    {
        setPadding(36,36,36, 36);
        setSpacing(20);
        setVertical(true);
        setFill(Color.WHITE);
        setBorder(PAGE_BORDER);
        setEffect(PAGE_SHADOW);
    }

    /**
     * Adds a chart.
     */
    public void addChart(Chart aChart)
    {
        ChartView chartView = new ChartView();
        chartView.setGrowWidth(true);
        chartView.setGrowHeight(true);
        chartView.setBorder(ChartPane.CHART_BORDER);
        chartView.setEffect(ChartPane.CHART_SHADOW);
        chartView.setChart(aChart);
        addChild(chartView);
    }

    /**
     *
     */
    @Override
    public void setVertical(boolean aValue)
    {
        super.setVertical(aValue);

        if (aValue)
            setPrefSize(612, 792);
        else setPrefSize(792, 612);
    }

    /**
     * Override to handle optional rounding radius.
     */
    public Shape getBoundsShape()
    {
        return new RoundRect(0,0, getWidth(), getHeight(), 4);
    }
}
