package snapcharts.model;

/**
 * A class to hold the different types of charts.
 */
public class AreaTypes {

    // The Chart
    private Chart _chart;

    // A column chart
    private AreaBar  _colChart;

    // A line chart
    private AreaLine  _lineChart;

    // A pie chart
    private AreaPie  _pieChart;

    // A column chart 3D
    private AreaBar3D  _colChart3D;

    /**
     * Creates the ChartTypes.
     */
    public AreaTypes(Chart aChart)  { _chart = aChart; }

    /**
     * Returns the type for given string.
     */
    public Area getChart(ChartType aType)
    {
        switch(aType) {
            case BAR: return getColumnChart();
            case BAR_3D: return getColumnChart3D();
            case LINE: return getLineChart();
            case PIE: return getPieChart();
            default: return null;
        }
    }

    /**
     * Returns the column chart.
     */
    public AreaBar getColumnChart()
    {
        if(_colChart!=null) return _colChart;
        _colChart = new AreaBar(); _colChart.setChart(_chart); return _colChart;
    }

    /**
     * Returns the column chart.
     */
    public AreaBar3D getColumnChart3D()
    {
        if(_colChart3D!=null) return _colChart3D;
        _colChart3D = new AreaBar3D(); _colChart3D.setChart(_chart); return _colChart3D;
    }

    /**
     * Returns the line chart.
     */
    public AreaLine getLineChart()
    {
        if(_lineChart!=null) return _lineChart;
        _lineChart = new AreaLine(); _lineChart.setChart(_chart); return _lineChart;
    }

    /**
     * Returns the pie chart.
     */
    public AreaPie getPieChart()
    {
        if(_pieChart!=null) return _pieChart;
        _pieChart = new AreaPie(); _pieChart.setChart(_chart); return _pieChart;
    }
}