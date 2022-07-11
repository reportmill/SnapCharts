/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;
import java.util.HashMap;
import java.util.Map;

/**
 * A class to hold the multiple TraceStyles for different TraceTypes.
 */
public class TraceStyleHpr {

    // The parent Trace that holds this TraceStyleHpr
    private Trace  _trace;

    // A map of styles for classes
    private Map<Class,TraceStyle>  _traceStyles = new HashMap<>();

    /**
     * Constructor.
     */
    public TraceStyleHpr(Trace aTrace)
    {
        _trace = aTrace;
    }

    /**
     * Returns the current TraceStyle.
     */
    public TraceStyle getTraceStyle()
    {
        TraceType traceType = _trace.getType();
        return getTraceStyleForTraceType(traceType);
    }

    /**
     * Returns the TraceStyle for given TraceType.
     */
    public TraceStyle getTraceStyleForTraceType(TraceType aType)
    {
        TraceStyle traceStyle = _traceStyles.get(aType.getStyleClass());
        if (traceStyle != null)
            return traceStyle;


        traceStyle = createTraceStyleForTraceType(aType);
        _traceStyles.put(aType.getStyleClass(), traceStyle);
        return traceStyle;
    }

    /**
     * Returns the TraceStyle for given TraceType.
     */
    private TraceStyle createTraceStyleForTraceType(TraceType aType)
    {
        // Create TraceStyle and set parent
        Class<? extends TraceStyle> styleClass = aType.getStyleClass();
        TraceStyle traceStyle = createTraceStyleForClass(styleClass);
        traceStyle.setParent(_trace);

        // Register to notify Chart of changes
        traceStyle.addPropChangeListener(pc -> _trace.childChartPartDidPropChange(pc));

        // Return TraceStyle
        return traceStyle;
    }

    /**
     * Returns the TraceStyle for given TraceType.
     */
    private TraceStyle createTraceStyleForClass(Class<? extends TraceStyle> aClass)
    {
        try { return aClass.newInstance(); }
        catch (Exception e)  { throw new RuntimeException(e); }
    }
}