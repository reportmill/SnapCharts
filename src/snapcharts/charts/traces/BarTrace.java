/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.charts.traces;
import snapcharts.charts.Trace;
import snapcharts.charts.TraceType;

/**
 * A Trace subclass for Bar chart properties.
 */
public class BarTrace extends Trace {

    // The ratio of a section used to pad a group of bars
    double             _groupPad = .2;

    // The ratio of a group used to pad a bar
    double             _barPad = .1;

    // Whether to use colors for each series value instead of series
    boolean            _colorValues;

    // The number of series and values to chart
    int                _seriesCount, _pointCount;

    /**
     * Constructor.
     */
    public BarTrace()
    {
        super();
    }

    /**
     * Returns Type.
     */
    @Override
    public TraceType getType()  { return TraceType.Bar; }

    /**
     * Returns the group padding.
     */
    public double getGroupPadding()  { return _groupPad; }

    /**
     * Sets the group padding.
     */
    public void setGroupPadding(double aValue)
    {
        _groupPad = aValue;
        //clearCache(); repaint();
    }

    /**
     * Returns the bar padding.
     */
    public double getBarPadding()  { return _barPad; }

    /**
     * Sets the bar padding.
     */
    public void setBarPadding(double aValue)
    {
        _barPad = aValue;
        //clearCache(); repaint();
    }

    /**
     * Returns whether to use colors for each series value instead of series.
     */
    public boolean isColorValues()  { return _colorValues; }

    /**
     * Returns whether to use colors for each series value instead of series.
     */
    public void setColorValues(boolean aValue)
    {
        _colorValues = aValue;
        //clearCache();
    }

}
