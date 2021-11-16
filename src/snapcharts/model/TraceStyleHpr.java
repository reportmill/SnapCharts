/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;
import snapcharts.modelx.*;

/**
 * A class to hold the multiple DataStyles for different ChartTypes.
 */
public class TraceStyleHpr {

    // The parent ChartPart that holds this DataStyleHpr
    private ChartPart  _parent;

    // Line chart properties
    private XYStyle  _xyStyle;

    // Bar chart properties
    private BarStyle  _barStyle;

    // Pie chart properties
    private PieStyle  _pieStyle;

    // Polar chart properties
    private PolarStyle  _polarStyle;

    // Contour chart properties
    private ContourStyle  _contourStyle;

    // Bar3D chart properties
    private Bar3DStyle  _bar3DStyle;

    /**
     * Constructor.
     */
    public TraceStyleHpr(ChartPart aChartPart)
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
     * Returns the current DataStyle.
     */
    public TraceStyle getDataStyle()
    {
        Chart chart = getChart();
        ChartType chartType = chart.getType();
        return getDataStyleForChartType(chartType);
    }

    /**
     * Returns the DataStyle for given ChartType.
     */
    public TraceStyle getDataStyleForChartType(ChartType aType)
    {
        switch(aType) {
            case SCATTER: return getXYStyle();
            case BAR: return getBarStyle();
            case PIE: return getPieStyle();
            case POLAR: return getPolarStyle();
            case CONTOUR: return getContourStyle();
            case POLAR_CONTOUR: return getContourStyle();
            case BAR_3D: return getBar3DStyle();
            default: return getXYStyle();
        }
    }

    /**
     * Returns the DataStyle for given ChartType.
     */
    private TraceStyle createDataStyleForChartType(ChartType aType)
    {
        // Create DataStyle and set parent
        TraceStyle traceStyle = createDataStyleForChartTypeRaw(aType);
        traceStyle.setParent(_parent);
        traceStyle.setChart(getChart());

        // Register to notify Chart of changes
        Chart chart = getChart();
        traceStyle.addPropChangeListener(pc -> chart.chartPartDidPropChange(pc));

        // Return DataStyle
        return traceStyle;
    }

    /**
     * Returns the DataStyle for given ChartType.
     */
    private TraceStyle createDataStyleForChartTypeRaw(ChartType aType)
    {
        // Get instance
        switch(aType) {
            case SCATTER: return new XYStyle();
            case BAR: return new BarStyle();
            case PIE: return new PieStyle();
            case POLAR: return new PolarStyle();
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
        if (_xyStyle != null) return _xyStyle;
        return _xyStyle = (XYStyle) createDataStyleForChartType(ChartType.SCATTER);
    }

    /**
     * Returns bar chart properties.
     */
    public BarStyle getBarStyle()
    {
        if(_barStyle != null) return _barStyle;
        return _barStyle = (BarStyle) createDataStyleForChartType(ChartType.BAR);
    }

    /**
     * Returns pie chart properties.
     */
    public PieStyle getPieStyle()
    {
        if(_pieStyle != null) return _pieStyle;
        return _pieStyle = (PieStyle) createDataStyleForChartType(ChartType.PIE);
    }

    /**
     * Returns Polar chart properties.
     */
    public PolarStyle getPolarStyle()
    {
        if(_polarStyle != null) return _polarStyle;
        return _polarStyle = (PolarStyle) createDataStyleForChartType(ChartType.POLAR);
    }

    /**
     * Returns contour chart properties.
     */
    public ContourStyle getContourStyle()
    {
        if(_contourStyle != null) return _contourStyle;
        return _contourStyle = (ContourStyle) createDataStyleForChartType(ChartType.CONTOUR);
    }

    /**
     * Returns Bar3D chart properties.
     */
    public Bar3DStyle getBar3DStyle()
    {
        if(_bar3DStyle != null) return _bar3DStyle;
        return _bar3DStyle = (Bar3DStyle) createDataStyleForChartType(ChartType.BAR_3D);
    }
}