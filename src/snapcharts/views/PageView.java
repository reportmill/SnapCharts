package snapcharts.views;
import snap.gfx.Color;
import snap.gfx.ShadowEffect;
import snap.view.ColView;
import snapcharts.model.Chart;
import java.util.List;

/**
 * A view to show a page of charts.
 */
public class PageView extends ColView {

    // The charts
    private List<Chart> _charts;

    /**
     * Constructor.
     */
    public PageView()
    {
        setPadding(40,40,40, 40);
        setVertical(true);
        setFill(Color.WHITE);
        setBorder(Color.BLACK, 1);
        setEffect(new ShadowEffect());
    }

    /**
     * Adds a chart.
     */
    public void addChart(Chart aChart)
    {
        ChartView chartView = new ChartView();
        chartView.setGrowWidth(true);
        chartView.setBorder(Color.BLACK, 1);
        chartView.setEffect(new ShadowEffect());
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
}
