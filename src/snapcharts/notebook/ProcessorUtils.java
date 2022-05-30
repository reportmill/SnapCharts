/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import snapcharts.data.DataSet;
import snapcharts.doc.ChartArchiver;
import snapcharts.model.Chart;

/**
 * This class provides some utility methods to help processor.
 */
public class ProcessorUtils {

    /**
     * Returns the value as a string.
     */
    public static String getStringForValue(Object aValue)
    {
        // Handle String
        if (aValue instanceof String)
            return (String) aValue;

        // Handle Chart
        if (aValue instanceof Chart) {
            Chart chart = (Chart) aValue;
            ChartArchiver chartArchiver = new ChartArchiver();
            String chartStr = chartArchiver.writeToXML(chart).toString();
            return chartStr;
        }

        // Handle DataSet
        if (aValue instanceof DataSet) {
            DataSet dataSet = (DataSet) aValue;
            ChartArchiver chartArchiver = new ChartArchiver();
            String chartStr = chartArchiver.writeToXML(dataSet).toString();
            return chartStr;
        }

        // Handle null
        if (aValue == null)
            return "null";

        // Handle anything
        return aValue.toString();
    }
}
