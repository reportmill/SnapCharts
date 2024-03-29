/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.charts;
import java.util.*;
import java.util.stream.Stream;
import snap.gfx.Color;
import snap.props.Prop;
import snap.props.PropChange;
import snap.props.PropChangeListener;
import snap.props.PropSet;
import snapcharts.data.DataSet;
import snapcharts.charts.traces.ScatterTrace;
import snapcharts.data.MinMax;

/**
 * A class to manage a list of Traces.
 */
public class Content extends ChartPart {

    // The list of traces
    private List<Trace>  _traceList = new ArrayList<>();

    // The Traces array
    private Trace[]  _traces;

    // The Enabled Traces array
    private Trace[]  _enabledTraces;

    // The AxisTypes
    private AxisType[] _axisTypes;

    // A map of MinMax values for axis types
    private Map<AxisType,MinMax>  _minMaxs = new HashMap<>();

    // A PropChangeListener to notify this ParentPart of child changes
    private PropChangeListener _tracePropChangeLsnr = pc -> traceDidPropChange(pc);

    // Constants for properties
    public static final String Trace_Prop = "Trace";
    public static final String Traces_Prop = "Traces";

    // Constants for property defaults
    public static final Color DEFAULT_CONTENT_LINE_COLOR = Color.GRAY;
    public static final int DEFAULT_CONTENT_LINE_WIDTH = 1;

    /**
     * Constructor.
     */
    public Content()
    {
        super();

        // Override default values
        _lineColor = DEFAULT_CONTENT_LINE_COLOR;
        _lineWidth = DEFAULT_CONTENT_LINE_WIDTH;
    }

    /**
     * Override to return this Content.
     */
    @Override
    public Content getContent()  { return this; }

    /**
     * Returns the array of traces.
     */
    public Trace[] getTraces()
    {
        // If already set, just return
        if (_traces != null) return _traces;

        // Create, set, return
        Trace[] traces = _traceList.toArray(new Trace[0]);
        return _traces = traces;
    }

    /**
     * Sets the array of traces.
     */
    public void setTraces(Trace[] theTraces)
    {
        // Remove current traces
        while (getTraceCount() > 0)
            removeTrace(0);

        // Add new traces
        for (Trace trace : theTraces)
            addTrace(trace);
    }

    /**
     * Returns whether no traces.
     */
    public boolean isEmpty()
    {
        return _traceList.isEmpty();
    }

    /**
     * Returns the number of traces.
     */
    public int getTraceCount()
    {
        return _traceList.size();
    }

    /**
     * Returns the individual trace at given index.
     */
    public Trace getTrace(int anIndex)
    {
        return _traceList.get(anIndex);
    }

    /**
     * Sets a new given trace at given index to replace existing one.
     */
    public void setTrace(Trace aTrace, int anIndex)
    {
        // Remove old trace
        Trace oldTrace = _traceList.remove(anIndex); if (oldTrace == aTrace) return;
        oldTrace._parent = null;
        oldTrace.removePropChangeListener(_tracePropChangeLsnr);

        // Add trace at index
        _traceList.add(anIndex, aTrace);
        aTrace._parent = this;
        aTrace._index = anIndex;
        aTrace.addPropChangeListener(_tracePropChangeLsnr);

        // Clear caches
        clearCachedValues();

        // FirePropChange
        firePropChange(Trace_Prop, oldTrace, aTrace, anIndex);
    }

    /**
     * Adds a new trace.
     */
    public void addTrace(Trace aTrace)
    {
        addTrace(aTrace, getTraceCount());
    }

    /**
     * Adds a new trace.
     */
    public void addTrace(Trace aTrace, int anIndex)
    {
        // Add trace at index
        _traceList.add(anIndex, aTrace);
        aTrace._parent = this;
        aTrace.addPropChangeListener(_tracePropChangeLsnr);

        // Reset indexes
        for (int i = 0; i < _traceList.size(); i++)
            getTrace(i)._index = i;
        clearCachedValues();

        // FirePropChange
        firePropChange(Trace_Prop, null, aTrace, anIndex);
    }

    /**
     * Removes the trace at index.
     */
    public Trace removeTrace(int anIndex)
    {
        Trace trace = _traceList.remove(anIndex);
        trace._parent = null;
        trace.removePropChangeListener(_tracePropChangeLsnr);
        clearCachedValues();
        firePropChange(Trace_Prop, trace, null, anIndex);
        return trace;
    }

    /**
     * Removes the given trace.
     */
    public int removeTrace(Trace aTrace)
    {
        int index = _traceList.indexOf(aTrace);
        if (index >= 0)
            removeTrace(index);
        return index;
    }

    /**
     * Clears the traces.
     */
    public void clear()
    {
        while (getTraceCount() != 0)
            removeTrace(getTraceCount()-1);
        clearCachedValues();
    }

    /**
     * Sets a trace type for given trace.
     */
    public Trace setTraceType(Trace aTrace, TraceType aTraceType)
    {
        // If already set, just return
        if (aTrace.getType() == aTraceType) return aTrace;

        // Create new, replace old
        Trace newTrace = aTrace.copyForTraceClass(aTraceType.getTraceClass());
        int index = aTrace.getIndex();
        setTrace(newTrace, index);

        // Return
        return newTrace;
    }

    /**
     * Returns the enabled traces.
     */
    public Trace[] getEnabledTraces()
    {
        if (_enabledTraces != null) return _enabledTraces;

        Trace[] traces = getTraces();
        Trace[] traces2 = Stream.of(traces).filter(i -> i.isEnabled()).toArray(size -> new Trace[size]);
        return _enabledTraces = traces2;
    }

    /**
     * The AxisTypes currently in use.
     */
    public AxisType[] getAxisTypes()
    {
        // If already set, just return
        if (_axisTypes != null) return _axisTypes;

        // Get set of unique axes
        Set<AxisType> typesSet = new HashSet<>();
        typesSet.add(AxisType.X);
        Trace[] traces = getEnabledTraces();
        for (Trace trace : traces)
            typesSet.add(trace.getAxisTypeY());

        // Convert to array, sort, set and return
        AxisType[] types = typesSet.toArray(new AxisType[0]);
        Arrays.sort(types);
        return _axisTypes = types;
    }

    /**
     * Returns the MinMax for given axis.
     */
    public MinMax getMinMaxForAxis(AxisType anAxisType)
    {
        MinMax minMax = _minMaxs.get(anAxisType);
        if (minMax != null)
            return minMax;

        minMax = getMinMaxForAxisImpl(anAxisType);
        _minMaxs.put(anAxisType, minMax);
        return minMax;
    }

    /**
     * Returns the MinMax for given axis.
     */
    private MinMax getMinMaxForAxisImpl(AxisType anAxisType)
    {
        // If empty, just return silly range
        Trace[] traces = getEnabledTraces();
        if (traces.length == 0 || getPointCount() == 0)
            return new MinMax(0, 5);

        // Handle X
        if (anAxisType == AxisType.X) {
            double min = Double.MAX_VALUE;
            double max = -Double.MAX_VALUE;
            for (Trace trace : traces) {
                DataSet procData = trace.getProcessedData();
                min = Math.min(min, procData.getMinX());
                max = Math.max(max, procData.getMaxX());
            }
            return new MinMax(min, max);
        }

        // Handle Y
        if (anAxisType.isAnyY()) {
            double min = Double.MAX_VALUE;
            double max = -Double.MAX_VALUE;
            for (Trace trace : traces) {
                if (anAxisType == trace.getAxisTypeY()) {
                    DataSet procData = trace.getProcessedData();
                    min = Math.min(min, procData.getMinY());
                    max = Math.max(max, procData.getMaxY());
                }
            }
            if (min == Double.MAX_VALUE)
                return new MinMax(0, 5);
            return new MinMax(min, max);
        }

        // Complain
        throw new RuntimeException("Content.getMinForAxis: Unknown axis: " + anAxisType);
    }

    /**
     * Returns the min value for given axis.
     */
    public double getMinForAxis(AxisType anAxisType)
    {
        return getMinMaxForAxis(anAxisType).getMin();
    }

    /**
     * Returns the max value for given axis.
     */
    public double getMaxForAxis(AxisType anAxisType)
    {
        return getMinMaxForAxis(anAxisType).getMax();
    }

    /**
     * Returns the number of points in traces.
     */
    public int getPointCount()
    {
        Trace[] traces = getEnabledTraces(); if (traces.length == 0) return 0;
        int pc = Integer.MAX_VALUE;
        for (Trace trace : traces)
            pc = Math.min(trace.getPointCount(), pc);
        return pc;
    }

    /**
     * Sets the point count.
     */
    public void setPointCount(int aValue)
    {
        Trace[] traces = getTraces();
        for (Trace trace : traces)
            trace.setPointCount(aValue);
    }

    /**
     * Called when a Trace changes a property.
     */
    private void traceDidPropChange(PropChange aPC)
    {
        clearCachedValues();

        // Handle Disabled: if Pie chart, clear other
        String propName = aPC.getPropName();
        if (propName == Trace.Disabled_Prop) {
            Trace trace = (Trace) aPC.getSource();
            boolean isPie = getChart().getTraceType() == TraceType.Pie;
            if (isPie && !trace.isDisabled()) {
                for (Trace ds : getTraces())
                    ds.setDisabled(ds != trace);
            }
        }

        // Fire PropChange
        firePropChange(aPC);
    }

    /**
     * Clears cached values.
     */
    private void clearCachedValues()
    {
        _traces = null;
        _enabledTraces = null;
        _axisTypes = null;
        _minMaxs.clear();
    }

    /**
     * Override to register props.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // Override super defaults: LineColor, LineWidth
        aPropSet.getPropForName(LineColor_Prop).setDefaultValue(DEFAULT_CONTENT_LINE_COLOR);
        aPropSet.getPropForName(LineWidth_Prop).setDefaultValue(DEFAULT_CONTENT_LINE_WIDTH);

        // Traces
        Prop tracesProp = aPropSet.addPropNamed(Traces_Prop, Trace[].class, EMPTY_OBJECT);
        tracesProp.setDefaultPropClass(ScatterTrace.class);
    }

    /**
     * Override to support properties for this class.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        switch (aPropName) {

            // Traces
            case Traces_Prop: return getTraces();

            // Do normal version
            default: return super.getPropValue(aPropName);
        }
    }

    /**
     * Override to support properties for this class.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        switch (aPropName) {

            // Traces
            case Traces_Prop: setTraces((Trace[]) aValue); break;

            // Do normal version
            default: super.setPropValue(aPropName, aValue); break;
        }
    }
}