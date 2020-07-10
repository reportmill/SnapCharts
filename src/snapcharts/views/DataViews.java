package snapcharts.views;

/**
 * A class to hold the different types of charts.
 */
public class DataViews {
    
    // The ChartView
    private ChartView  _chartView;

    // A column chart
    private DataViewBar  _colChart;
    
    // A line chart
    private DataViewLine _lineChart;

    // A pie chart
    private DataViewPie _pieChart;

    // A column chart 3D
    private DataViewBar3D _colChart3D;
    
/**
 * Creates the ChartTypes.
 */
public DataViews(ChartView aCV)  { _chartView = aCV; }

/**
 * Returns the type for given string.
 */
public DataView getChart(String aType)
{
    switch(aType) {
        case ChartView.BAR_TYPE: return getColumnChart();
        case ChartView.BAR3D_TYPE: return getColumnChart3D();
        case ChartView.LINE_TYPE: return getLineChart();
        case ChartView.PIE_TYPE: return getPieChart();
        default: return null;
    }
}

/**
 * Returns the column chart.
 */
public DataViewBar getColumnChart()
{
    if(_colChart!=null) return _colChart;
    _colChart = new DataViewBar(); _colChart.setChartView(_chartView); return _colChart;
}

/**
 * Returns the column chart.
 */
public DataViewBar3D getColumnChart3D()
{
    if(_colChart3D!=null) return _colChart3D;
    _colChart3D = new DataViewBar3D(); _colChart3D.setChartView(_chartView); return _colChart3D;
}

/**
 * Returns the line chart.
 */
public DataViewLine getLineChart()
{
    if(_lineChart!=null) return _lineChart;
    _lineChart = new DataViewLine(); _lineChart.setChartView(_chartView); return _lineChart;
}

/**
 * Returns the pie chart.
 */
public DataViewPie getPieChart()
{
    if(_pieChart!=null) return _pieChart;
    _pieChart = new DataViewPie(); _pieChart.setChartView(_chartView); return _pieChart;
}

}