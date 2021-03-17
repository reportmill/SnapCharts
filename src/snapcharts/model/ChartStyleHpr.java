package snapcharts.model;
import snapcharts.modelx.*;

/**
 * A class to hold the different types of charts.
 */
public class ChartStyleHpr {

    // The parent ChartPart that holds this ChartStyleHpr
    private ChartPart  _parent;

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
    public ChartStyleHpr(ChartPart aChartPart)
    {
        _parent = aChartPart;
    }

    /**
     * Returns the Chart.
     */
    public Chart getChart()
    {
        return _parent.getChart();
    }

    /**
     * Returns the current ChartStyle.
     */
    public ChartStyle getChartStyle()
    {
        Chart chart = getChart();
        ChartType chartType = chart.getType();
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
            default: return getXYStyle();
        }
    }

    /**
     * Returns the ChartStyle for given ChartType.
     */
    private ChartStyle createChartStyleForChartType(ChartType aType)
    {
        ChartStyle chartStyle = createChartStyleForChartTypeRaw(aType);
        Chart chart = getChart();
        chartStyle.setChart(chart);
        chartStyle.addPropChangeListener(pc -> chart.chartPartDidPropChange(pc));
        return chartStyle;
    }

    /**
     * Returns the ChartStyle for given ChartType.
     */
    private ChartStyle createChartStyleForChartTypeRaw(ChartType aType)
    {
        // Get instance
        switch(aType) {
            case LINE: return new XYStyle();
            case BAR: return new BarStyle();
            case PIE: return new PieStyle();
            case CONTOUR: return new ContourStyle();
            case POLAR_CONTOUR: return new ContourStyle();
            case BAR_3D: return new Bar3DStyle();
            default: return new XYStyle();
        }
    }

    /**
     * Returns XY chart properties.
     */
    public XYStyle getXYStyle()
    {
        if (_xyStyle !=null) return _xyStyle;
        return _xyStyle = (XYStyle) createChartStyleForChartType(ChartType.LINE);
    }

    /**
     * Returns bar chart properties.
     */
    public BarStyle getBarStyle()
    {
        if(_barStyle !=null) return _barStyle;
        return _barStyle = (BarStyle) createChartStyleForChartType(ChartType.BAR);
    }

    /**
     * Returns pie chart properties.
     */
    public PieStyle getPieStyle()
    {
        if(_pieStyle !=null) return _pieStyle;
        return _pieStyle = (PieStyle) createChartStyleForChartType(ChartType.PIE);
    }

    /**
     * Returns contour chart properties.
     */
    public ContourStyle getContourStyle()
    {
        if(_contourStyle != null) return _contourStyle;
        return _contourStyle = (ContourStyle) createChartStyleForChartType(ChartType.CONTOUR);
    }

    /**
     * Returns Bar3D chart properties.
     */
    public Bar3DStyle getBar3DStyle()
    {
        if(_bar3DStyle !=null) return _bar3DStyle;
        return _bar3DStyle = (Bar3DStyle) createChartStyleForChartType(ChartType.BAR_3D);
    }
}