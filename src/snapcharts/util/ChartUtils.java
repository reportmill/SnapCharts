package snapcharts.util;
import snapcharts.model.Chart;
import snapcharts.model.ChartType;
import snapcharts.model.Trace;
import snapcharts.model.TraceStyle;

/**
 * Utility methods for Chart.
 */
public class ChartUtils {

    // Constants for Scatter type
    public enum ScatterType { LINE, AREA, SCATTER, STACKED_AREA }

    /**
     * Returns the ScatterType for a Chart.
     */
    public static ScatterType getScatterType(Chart aChart)
    {
        // Get Traces (if none, just return LINE)
        Trace[] traces = aChart.getTraceList().getTraces();
        if (traces.length == 0)
            return ScatterType.LINE;

        // If all Traces ShowArea, return AREA
        boolean showArea = true;
        for (Trace trace : traces) {
            if (!trace.getTraceStyle().isShowArea()) {
                showArea = false;
                break;
            }
            if (trace.isStacked())
                return ScatterType.STACKED_AREA;
        }
        if (showArea)
            return ScatterType.AREA;

        // If all Traces ShowLine, return LINE
        boolean showLine = true;
        for (Trace trace : traces) {
            if (!trace.getTraceStyle().isShowLine()) {
                showLine = false;
                break;
            }
        }
        if (showLine)
            return ScatterType.LINE;

        // Return SCATTER
        return ScatterType.SCATTER;
    }

    /**
     * Sets the ScatterType.
     */
    public static void setScatterType(Chart aChart, ScatterType scatterType)
    {
        // Get booleans
        boolean isLine = scatterType == ScatterType.LINE;
        boolean isScatter = scatterType == ScatterType.SCATTER;
        boolean isArea = scatterType == ScatterType.AREA;
        boolean isStackedArea = scatterType == ScatterType.STACKED_AREA;

        aChart.setType(ChartType.SCATTER);

        // Configure Traces
        Trace[] traces = aChart.getTraceList().getTraces();
        for (Trace trace : traces) {
            TraceStyle traceStyle = trace.getTraceStyle();
            traceStyle.setShowLine(isLine || isArea || isStackedArea);
            traceStyle.setShowArea(isArea || isStackedArea);
            traceStyle.setShowPoints(isLine || isScatter);
            trace.setStacked(isStackedArea);
        }
    }

    /**
     * Sets the ScatterType.
     */
    public static String getScatterTypeString(Chart aChart)
    {
        ScatterType scatterType = getScatterType(aChart);
        switch (scatterType) {
            case LINE: return "Line";
            case AREA: return "Area";
            case STACKED_AREA: return "StackedArea";
            default: return "Scatter";
        }
    }
}
