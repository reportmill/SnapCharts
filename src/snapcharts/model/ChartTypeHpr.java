package snapcharts.model;

/**
 * A class to hold the different types of charts.
 */
public class ChartTypeHpr {

    // The Chart
    private Chart  _chart;

    // Line chart properties
    private LineProps  _lineProps;

    // Bar chart properties
    private BarProps  _barProps;

    // Pie chart properties
    private PieProps  _pieProps;

    // Contour chart properties
    private ContourProps  _contourProps;

    // Bar3D chart properties
    private Bar3DProps  _bar3DProps;

    /**
     * Constructor.
     */
    public ChartTypeHpr(Chart aChart)  { _chart = aChart; }

    /**
     * Returns the current ChartTypeProps.
     */
    public ChartTypeProps getTypeProps()
    {
        ChartType chartType = _chart.getType();
        return getTypePropsForChartType(chartType);
    }

    /**
     * Returns the ChartTypeProps for given ChartType.
     */
    public ChartTypeProps getTypePropsForChartType(ChartType aType)
    {
        switch(aType) {
            case LINE: return getLineProps();
            case BAR: return getBarProps();
            case PIE: return getPieProps();
            case CONTOUR: return getContourProps();
            case BAR_3D: return getBar3DProps();
            default: return null;
        }
    }

    /**
     * Returns line chart properties.
     */
    public LineProps getLineProps()
    {
        if (_lineProps !=null) return _lineProps;
        _lineProps = new LineProps();
        _lineProps.setChart(_chart);
        _lineProps.addPropChangeListener(pc -> _chart.chartPartDidPropChange(pc));
        return _lineProps;
    }

    /**
     * Returns bar chart properties.
     */
    public BarProps getBarProps()
    {
        if(_barProps !=null) return _barProps;
        _barProps = new BarProps();
        _barProps.setChart(_chart);
        _barProps.addPropChangeListener(pc -> _chart.chartPartDidPropChange(pc));
        return _barProps;
    }

    /**
     * Returns pie chart properties.
     */
    public PieProps getPieProps()
    {
        if(_pieProps !=null) return _pieProps;
        _pieProps = new PieProps();
        _pieProps.setChart(_chart);
        _pieProps.addPropChangeListener(pc -> _chart.chartPartDidPropChange(pc));
        return _pieProps;
    }

    /**
     * Returns contour chart properties.
     */
    public ContourProps getContourProps()
    {
        if(_contourProps != null) return _contourProps;
        _contourProps = new ContourProps();
        _contourProps.setChart(_chart);
        _contourProps.addPropChangeListener(pc -> _chart.chartPartDidPropChange(pc));
        return _contourProps;
    }

    /**
     * Returns Bar3D chart properties.
     */
    public Bar3DProps getBar3DProps()
    {
        if(_bar3DProps !=null) return _bar3DProps;
        _bar3DProps = new Bar3DProps();
        _bar3DProps.setChart(_chart);
        _bar3DProps.addPropChangeListener(pc -> _chart.chartPartDidPropChange(pc));
        return _bar3DProps;
    }
}