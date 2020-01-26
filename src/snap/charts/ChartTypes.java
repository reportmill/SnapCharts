package snap.charts;

/**
 * A class to hold the different types of charts.
 */
public class ChartTypes {
    
    // The ChartView
    ChartView             _chartView;

    // A column chart
    ChartAreaBar          _colChart;
    
    // A line chart
    ChartAreaLine         _lineChart;

    // A pie chart
    ChartAreaPie          _pieChart;

    // A column chart 3D
    ChartAreaBar3D        _colChart3D;
    
/**
 * Creates the ChartTypes.
 */
public ChartTypes(ChartView aCV)  { _chartView = aCV; }

/**
 * Returns the type for given string.
 */
public ChartArea getChart(String aType)
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
public ChartAreaBar getColumnChart()
{
    if(_colChart!=null) return _colChart;
    _colChart = new ChartAreaBar(); _colChart.setChartView(_chartView); return _colChart;
}

/**
 * Returns the column chart.
 */
public ChartAreaBar3D getColumnChart3D()
{
    if(_colChart3D!=null) return _colChart3D;
    _colChart3D = new ChartAreaBar3D(); _colChart3D.setChartView(_chartView); return _colChart3D;
}

/**
 * Returns the line chart.
 */
public ChartAreaLine getLineChart()
{
    if(_lineChart!=null) return _lineChart;
    _lineChart = new ChartAreaLine(); _lineChart.setChartView(_chartView); return _lineChart;
}

/**
 * Returns the pie chart.
 */
public ChartAreaPie getPieChart()
{
    if(_pieChart!=null) return _pieChart;
    _pieChart = new ChartAreaPie(); _pieChart.setChartView(_chartView); return _pieChart;
}

}