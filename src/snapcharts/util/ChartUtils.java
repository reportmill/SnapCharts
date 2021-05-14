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
    public enum ScatterType { LINE, AREA, SCATTER }

    /**
     * Returns the ScatterType for a Chart.
     */
    public static ScatterType getScatterType(Chart aChart)
    {
        DataSet[] dataSets = aChart.getDataSetList().getDataSets();
        if (dataSets.length == 0)
            return ScatterType.SCATTER;
        boolean showArea = true;
        for (DataSet dataSet : dataSets) {
            if (!dataSet.getDataStyle().isShowArea()) {
                showArea = false;
                break;
            }
        }
        if (showArea)
            return ScatterType.AREA;
        boolean showLine = true;
        for (DataSet dataSet : dataSets) {
            if (!dataSet.getDataStyle().isShowLine()) {
                showLine = false;
                break;
            }
        }
        if (showLine)
            return ScatterType.LINE;
        return ScatterType.SCATTER;
    }

    /**
     * Sets the ScatterType.
     */
    public static void setScatterType(Chart aChart, ScatterType scatterType)
    {
        aChart.setType(ChartType.SCATTER);
        DataSet[] dataSets = aChart.getDataSetList().getDataSets();
        for (DataSet dataSet : dataSets) {
            DataStyle dataStyle = dataSet.getDataStyle();
            dataStyle.setShowLine(scatterType == ScatterType.LINE || scatterType == ScatterType.AREA);
            dataStyle.setShowSymbols(scatterType == ScatterType.LINE || scatterType == ScatterType.SCATTER);
            dataStyle.setShowArea(scatterType == ScatterType.AREA);
        }
    }

    /**
     * Sets the ScatterType.
     */
    public static String getScatterTypeString(Chart aChart)
    {
        switch (getScatterType(aChart)) {
            case LINE: return "Line";
            case AREA: return "Area";
            default: return "Scatter";
        }
    }
}
