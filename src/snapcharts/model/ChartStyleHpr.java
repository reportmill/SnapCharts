package snapcharts.model;
import snapcharts.modelx.*;

/**
 * A class to hold the different types of charts.
 */
public class ChartStyleHpr {

    // The Chart
    private Chart  _chart;

    // Line chart properties
    private XYStyle _xyStyle;

    // Bar chart properties
    private BarStyle _barStyle;

    // Pie chart properties
    private PieStyle _pieStyle;

    // Contour chart properties
    private ContourStyle _contourStyle;

    // Bar3D chart properties
    private Bar3DStyle  _bar3DStyle;

    /**
     * Constructor.
     */
    public ChartStyleHpr(Chart aChart)  { _chart = aChart; }

    /**
     * Returns the current ChartStyle.
     */
    public ChartStyle getChartStyle()
    {
        ChartType chartType = _chart.getType();
        return getChartStyleForChartType(chartType);
    }

    /**
     * Returns the ChartStyle for given ChartType.
     */
    public ChartStyle getChartStyleForChartType(ChartType aType)
    {
        switch(aType) {
            case LINE: return getXYStyle();
            case BAR: return getBarStyle();
            case PIE: return getPieStyle();
            case CONTOUR: return getContourStyle();
            case POLAR_CONTOUR: return getContourStyle();
            case BAR_3D: return getBar3DStyle();
            default: return null;
        }
    }

    /**
     * Returns XY chart properties.
     */
    public XYStyle getXYStyle()
    {
        if (_xyStyle !=null) return _xyStyle;
        _xyStyle = new XYStyle();
        _xyStyle.setChart(_chart);
        _xyStyle.addPropChangeListener(pc -> _chart.chartPartDidPropChange(pc));
        return _xyStyle;
    }

    /**
     * Returns bar chart properties.
     */
    public BarStyle getBarStyle()
    {
        if(_barStyle !=null) return _barStyle;
        _barStyle = new BarStyle();
        _barStyle.setChart(_chart);
        _barStyle.addPropChangeListener(pc -> _chart.chartPartDidPropChange(pc));
        return _barStyle;
    }

    /**
     * Returns pie chart properties.
     */
    public PieStyle getPieStyle()
    {
        if(_pieStyle !=null) return _pieStyle;
        _pieStyle = new PieStyle();
        _pieStyle.setChart(_chart);
        _pieStyle.addPropChangeListener(pc -> _chart.chartPartDidPropChange(pc));
        return _pieStyle;
    }

    /**
     * Returns contour chart properties.
     */
    public ContourStyle getContourStyle()
    {
        if(_contourStyle != null) return _contourStyle;
        _contourStyle = new ContourStyle();
        _contourStyle.setChart(_chart);
        _contourStyle.addPropChangeListener(pc -> _chart.chartPartDidPropChange(pc));
        return _contourStyle;
    }

    /**
     * Returns Bar3D chart properties.
     */
    public Bar3DStyle getBar3DStyle()
    {
        if(_bar3DStyle !=null) return _bar3DStyle;
        _bar3DStyle = new Bar3DStyle();
        _bar3DStyle.setChart(_chart);
        _bar3DStyle.addPropChangeListener(pc -> _chart.chartPartDidPropChange(pc));
        return _bar3DStyle;
    }
}