/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.geom.Point;
import snap.geom.Rect;
import snap.gfx.*;
import snap.props.PropChange;
import snapcharts.charts.*;
import snapcharts.charts.traces.BarTrace;
import snapcharts.view.ChartHelper;
import snapcharts.view.TraceView;

/**
 * A TraceView subclass to display the contents of bar chart.
 */
public class BarTraceView extends TraceView {

    // The BarTrace
    private BarTrace _barTrace;

    // The number of traces to display
    protected int  _traceCount;

    // The number of values in trace(s)
    protected int  _pointCount;

    // The cached sections
    private Section[]  _sections;

    /**
     * Constructor.
     */
    public BarTraceView(ChartHelper aChartHelper, Trace aTrace, boolean isVisible)
    {
        super(aChartHelper, aTrace);

        // If not visible, just return
        if (!isVisible) {
            setVisible(false);
        }

        // Add top so top axis line isn't on edge
        //setPadding(5,0,0,0);
    }

    /**
     * Returns the BarTrace.
     */
    public BarTrace getBarTrace()
    {
        // If already set, just return
        if (_barTrace != null) return _barTrace;

        // Get/set BarTrace
        Trace trace = getTrace();
        if (trace instanceof BarTrace)
            return _barTrace = (BarTrace) trace;

        // Complain and create bogus new
        System.err.println("BarTraceView.getBarTrace: Trace isn't BarTrace");
        return _barTrace = (BarTrace) trace.copyForTraceClass(BarTrace.class);
    }

    /**
     * Returns the cached section (and section bars) objects.
     */
    protected Section[] getSections()
    {
        // If recalc not needed, just return
        Content content = getContent();
        Trace[] traces = content.getEnabledTraces();
        int traceCount = traces.length;
        int pointCount = content.getPointCount();
        if (_sections != null && _sections.length == pointCount && _traceCount == traceCount)
            return _sections;

        // Get BarTraceView info
        BarTrace barTrace = getBarTrace();
        double groupPad = barTrace.getGroupPadding();
        double barPad = barTrace.getBarPadding();
        double viewHeight = getHeight();
        boolean colorTraces = !barTrace.isColorValues();

        // Get number of traces, points and section width
        _traceCount = traceCount;
        _pointCount = pointCount;
        double sectionWidth = getWidth() / _pointCount;

        // Get group widths
        double groupWidthRatio = 1 - groupPad * 2;
        double groupWidth = groupWidthRatio >= 0 ? groupWidthRatio * sectionWidth : 1;
        double groupPadWidth = (sectionWidth - groupWidth) / 2;

        // Get width of individual bar (bar count + bar spaces + bar&space at either end)
        double barWidthRatio = 1 - barPad * 2;
        double barWidth = barWidthRatio >= 0 ? barWidthRatio * groupWidth / _traceCount : 1;
        double barPadWidth = barWidthRatio >= 0 ? barPad * groupWidth / _traceCount : 1;

        // Create new bars array
        Section[] sections = new Section[pointCount];

        // Iterate over sections
        for (int i = 0; i < _pointCount; i++) {

            // Create/set new section and section.bars
            Section section = sections[i] = new Section(i * sectionWidth, 0, sectionWidth, viewHeight);
            section.bars = new Bar[_traceCount];

            // Iterate over traces
            for (int j = 0; j < _traceCount; j++) {

                // Get data point
                Trace trace = traces[j];
                TracePoint dataPoint = trace.getPoint(i);
                double dataY = dataPoint.getY();
                double dispY = dataToViewY(dataY);

                // Draw bar
                Color color = colorTraces ? trace.getLineColor() : getColorMapColor(i);
                double barX = i * sectionWidth + groupPadWidth + (j * 2 + 1) * barPadWidth + j * barWidth;
                double barHeight = viewHeight - dispY;
                section.bars[j] = new Bar(dataPoint, barX, dispY, barWidth, barHeight, color);
            }
        }

        // Return sections
        return _sections = sections;
    }

    /**
     * Clears the sections when needed (change of data, size)
     */
    protected void clearSections()
    {
        _sections = null;
        repaint();
    }

    /**
     * Paints chart.
     */
    protected void paintTrace(Painter aPntr)
    {
        // Get selected point index (section index)
        TracePoint dataPoint = getChartView().getTargDataPoint();
        int selIndex = dataPoint != null ? dataPoint.getIndex() : -1;

        double viewW = getWidth();
        double viewH = getHeight();
        Section[] sections = getSections();

        // If reveal is not full (1) then clip
        if (getReveal() < 1) {
            aPntr.save();
            aPntr.clipRect(0, viewH * (1 - getReveal()), viewW, viewH * getReveal());
        }

        // Iterate over sections
        for (int i = 0; i < _pointCount; i++) {
            Section section = sections[i];

            // If selected section, draw background
            if (i == selIndex) {
                aPntr.setColor(Color.get("#4488FF09"));
                aPntr.fillRect(i * section.width, 0, section.width, viewH);
            }

            // Iterate over traces and draw bars
            for (int j = 0; j < _traceCount; j++) {
                Bar bar = section.bars[j];
                aPntr.setColor(bar.color);
                aPntr.fillRect(bar.x, bar.y, bar.width, bar.height - .5);
            }
        }

        // If reveal not full, restore gstate
        if (getReveal() < 1)
            aPntr.restore();
    }

    /**
     * Returns the data point best associated with given x/y (null if none).
     */
    @Override
    public TracePoint getDataPointForLocalXY(double aX, double aY)
    {
        // Get sections array
        Section[] sections = getSections();

        // Iterate over sections (points) and bars (trace) and if bar contains point, return data point
        for (int i = 0; i < _pointCount; i++) {
            Section section = sections[i];
            for (int j = 0; j < _traceCount; j++) {
                Bar bar = section.bars[j];
                if (bar.contains(aX, aY))
                    return bar.point;
            }
        }

        // Return null since bar not found for point
        return null;
    }

    /**
     * Returns the given data point X/Y in this view coords.
     *
     * @param aDP
     */
    @Override
    public Point getLocalXYForDataPoint(TracePoint aDP)
    {
        // Get sections array
        Section[] sections = getSections();

        // Iterate over sections (points) and bars (trace) and if bar contains point, return data point
        for (int i = 0; i < _pointCount; i++) {
            Section section = sections[i];
            for (int j = 0; j < _traceCount; j++) {
                Bar bar = section.bars[j];
                if (bar.point.equals(aDP)) {
                    double dispX = Math.round(bar.x + bar.width / 2);
                    double dispY = Math.round(bar.y);
                    return new Point(dispX, dispY);
                }
            }
        }

        // Return zero point since bar not found for point - should not be possible
        System.err.println("BarTraceView.getLocalXYForDataPoint: Point not found");
        return Point.ZERO;
    }

    /**
     * Called when a ChartPart changes.
     */
    @Override
    protected void chartPartDidChange(PropChange aPC)
    {
        // Do normal version
        super.chartPartDidChange(aPC);

        // Handle Data changes
        Object src = aPC.getSource();
        if (src instanceof Trace || src instanceof Content || src instanceof Axis) {
            clearSections();
        }
    }

    /**
     * Override to clear sections.
     */
    @Override
    public void setWidth(double aValue)
    {
        if (aValue == getWidth()) return;
        super.setWidth(aValue);
        clearSections();
    }

    /**
     * Override to clear sections.
     */
    @Override
    public void setHeight(double aValue)
    {
        if (aValue == getHeight()) return;
        super.setHeight(aValue);
        clearSections();
    }

    /**
     * A class to hold section information.
     */
    protected class Section {

        // Points
        double x, y, width, height;
        Bar bars[];

        /**
         * Creates a Section.
         */
        public Section(double aX, double aY, double aW, double aH)
        {
            x = aX;
            y = aY;
            width = aW;
            height = aH;
        }

        /**
         * Returns whether section contains point.
         */
        public boolean contains(double aX, double aY)
        {
            return Rect.contains(x, y, width, height, aX, aY);
        }
    }

    /**
     * A class to hold bar information.
     */
    protected class Bar {

        // Points
        TracePoint point;
        double x, y, width, height;
        Color color;

        /**
         * Creates a bar.
         */
        public Bar(TracePoint aDP, double aX, double aY, double aW, double aH, Color aColor)
        {
            point = aDP;
            x = aX;
            y = aY;
            width = aW;
            height = aH;
            color = aColor;
        }

        /**
         * Returns whether bar contains point.
         */
        public boolean contains(double aX, double aY)
        {
            return Rect.contains(x, y, width, height, aX, aY);
        }
    }
}