package snapcharts.util;
import snapcharts.model.Chart;
import snapcharts.model.ChartType;
import snapcharts.model.DataSet;
import snapcharts.model.DataStyle;

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
        // Get DataSets (if none, just return LINE)
        DataSet[] dataSets = aChart.getDataSetList().getDataSets();
        if (dataSets.length == 0)
            return ScatterType.LINE;

        // If all DataSets ShowArea, return AREA
        boolean showArea = true;
        for (DataSet dataSet : dataSets) {
            if (!dataSet.getDataStyle().isShowArea()) {
                showArea = false;
                break;
            }
            if (dataSet.isStacked())
                return ScatterType.STACKED_AREA;
        }
        if (showArea)
            return ScatterType.AREA;

        // If all DataSets ShowLine, return LINE
        boolean showLine = true;
        for (DataSet dataSet : dataSets) {
            if (!dataSet.getDataStyle().isShowLine()) {
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

        // Configure DataSets
        DataSet[] dataSets = aChart.getDataSetList().getDataSets();
        for (DataSet dataSet : dataSets) {
            DataStyle dataStyle = dataSet.getDataStyle();
            dataStyle.setShowLine(isLine || isArea || isStackedArea);
            dataStyle.setShowSymbols(isLine || isScatter);
            dataStyle.setShowArea(isArea || isStackedArea);
            dataSet.setStacked(isStackedArea);
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
