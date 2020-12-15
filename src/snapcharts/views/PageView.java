package snapcharts.views;
import snap.geom.RoundRect;
import snap.geom.Shape;
import snap.gfx.*;
import snap.view.ParentView;
import snapcharts.app.ChartPane;
import snapcharts.model.Chart;
import java.util.ArrayList;
import java.util.List;

/**
 * A view to show a page of charts.
 */
public class PageView extends ParentView {

    // The number of rows
    private int  _rowCount;

    // The number of columns
    private int  _colCount;

    // A class to help with layout
    private PageViewLayout  _layout = new PageViewLayout(this);

    // The charts
    private List<ChartView> _chartViews = new ArrayList<>();

    // The ChartShadow
    private Effect  _chartShadow = ChartPane.CHART_SHADOW;

    // Constants
    public static Paint PAGE_FILL = new Color(253, 253, 255);
    public static Border PAGE_BORDER = Border.createLineBorder(Color.GRAY, 1);
    public static Effect PAGE_SHADOW = new ShadowEffect(10, Color.DARKGRAY, 0, 0).copySimple();

    /**
     * Constructor.
     */
    private PageView()
    {
        setPadding(36,36,36, 36);
        setSpacing(20);
        setVertical(true);
        setPrefSize(612, 792);
        setFill(PAGE_FILL);
        setBorder(PAGE_BORDER);
        setEffect(PAGE_SHADOW);
    }

    /**
     * Constructor.
     */
    public PageView(int aPlotCount)
    {
        this();
        switch (aPlotCount)
        {
            case 1: _rowCount = _colCount = 1; break;
            case 2: _rowCount = 2; _colCount = 1; break;
            case 3: _rowCount = 3; _colCount = 1; break;
            case 4: _rowCount = 2; _colCount = 2; break;
            case 6: _rowCount = 3; _colCount = 2; break;
            case 9: _rowCount = 3; _colCount = 3; break;
            default: throw new RuntimeException("PageView.init: Unsupported plot count");
        }

        //int shadowRad = aPlotCount >=4 ? 6 : aPlotCount >= 3 ? 8 : aPlotCount >= 2 ? 10 : 12;
        //_chartShadow = new ShadowEffect(shadowRad, Color.DARKGRAY, 0, 0).copySimple();
    }

    /**
     * Constructor.
     */
    public PageView(int aRowCount, int aColCount)
    {
        this();
        _rowCount = aRowCount;
        _colCount = aColCount;
    }

    /**
     * The number of rows.
     */
    public int getRowCount()  { return _rowCount; }

    /**
     * The number of columns.
     */
    public int getColCount()  { return _colCount; }

    /**
     * The number of plots.
     */
    public int getChartCount()  { return _chartViews.size(); }

    /**
     * Adds a chart.
     */
    public void addChart(Chart aChart)
    {
        ChartView chartView = new ChartView();
        chartView.setGrowWidth(true);
        chartView.setGrowHeight(true);
        //chartView.setBorder(ChartPane.CHART_BORDER);
        //chartView.setEffect(_chartShadow);
        chartView.setChart(aChart);
        addChild(chartView);
        _chartViews.add(chartView);
    }

    @Override
    protected void layoutImpl()
    {
        _layout.layoutPage();
    }

    /**
     *
     */
    @Override
    public void setVertical(boolean aValue)
    {
        if (aValue==isVertical()) return;
        super.setVertical(aValue);

        // Swap row/col count
        int rows = getRowCount();
        _rowCount = getColCount();
        _colCount = rows;

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
