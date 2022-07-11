package snapcharts.util;
import snapcharts.model.Chart;
import snapcharts.model.Trace;
import snapcharts.model.TraceType;

/**
 * Utility methods for Chart.
 */
public class ChartUtils {

    // Constants for Scatter type
    public enum ScatterType {

        // Scatter types
        LINE, AREA, SCATTER, STACKED_AREA;

        // Identify convenience
        public boolean isLine()  { return this == ChartUtils.ScatterType.LINE; }
        public boolean isScatter()  { return this == ChartUtils.ScatterType.SCATTER; }
        public boolean isArea()  { return this == ChartUtils.ScatterType.AREA; }
        public boolean isStackedArea()  { return this == ChartUtils.ScatterType.STACKED_AREA; }

        // Trace Properties
        public boolean isTraceShowLine()  { return isLine() || isArea() || isStackedArea(); }
        public boolean isTraceShowArea()  { return isArea() || isStackedArea(); }
        public boolean isTraceShowPoints()  { return isLine() || isScatter(); }
        public boolean isTraceStacked()  { return isStackedArea(); }
    }

    /**
     * Returns the ScatterType for a Chart.
     */
    public static ScatterType getScatterType(Chart aChart)
    {
        // Get Traces (if none, just return LINE)
        Trace[] traces = aChart.getContent().getTraces();
        if (traces.length == 0)
            return ScatterType.LINE;

        // If all Traces ShowArea, return AREA
        boolean showArea = true;
        for (Trace trace : traces) {
            if (!trace.isShowArea()) {
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
            if (!trace.isShowLine()) {
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
        // Configure Traces
        Trace[] traces = aChart.getContent().getTraces();
        for (Trace trace : traces) {
            trace.setType(TraceType.Scatter);
            trace.setShowLine(scatterType.isTraceShowLine());
            trace.setShowArea(scatterType.isTraceShowArea());
            trace.setShowPoints(scatterType.isTraceShowPoints());
            trace.setStacked(scatterType.isTraceStacked());
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
