/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.app;
import snap.view.*;
import snap.viewx.Console;
import snapcharts.data.DataSet;
import snapcharts.data.DoubleArray;
import snapcharts.doc.ChartArchiver;
import snapcharts.model.Chart;
import snapcharts.view.ChartView;

/**
 * This View subclass shows snippets.
 */
public class QuickChartsUtils implements Console.Helper {

    /**
     * Constructor.
     */
    public QuickChartsUtils()
    {
        super();
    }

    /**
     * Console Helper method.
     */
    @Override
    public View createViewForObject(Object anObj)  { return createContentViewForValue(anObj); }

    /**
     * Console Helper method.
     */
    @Override
    public String createStringForObject(Object anObj)  { return getStringForValue(anObj); }

    /**
     * Override to support custom content views for response values.
     */
    public static View createContentViewForValue(Object value)
    {
        // Handle Chart
        if (value instanceof Chart)
            return createContentViewForChart((Chart) value);

        // Handle DataSet
        if (value instanceof DataSet)
            return createContentViewForDataSet((DataSet) value);

        // Return null
        return null;
    }

    /**
     * Creates content view for Chart.
     */
    private static View createContentViewForChart(Chart aChart)
    {
        // Create ChartView for Chart
        ChartView chartView = new ChartView();
        chartView.setChart(aChart);
        chartView.setPrefSize(500, 300);
        chartView.setGrowWidth(true);

        // Return
        return chartView;
    }

    /**
     * Creates content view for DataSet.
     */
    private static View createContentViewForDataSet(DataSet dataSet)
    {
        // Create/configure DataSetPane
        DataSetPane dataSetPane = new DataSetPane(dataSet);
        View dataSetPaneView = dataSetPane.getUI();
        dataSetPaneView.setPrefWidth(500);
        dataSetPaneView.setGrowWidth(true);
        dataSetPaneView.setMaxHeight(250);

        // Return
        return dataSetPaneView;
    }

    /**
     * Returns the value as a string.
     */
    private static String getStringForValue(Object aValue)
    {
        // Handle DoubleArray
        if (aValue instanceof DoubleArray) {
            double[] doubleArray = ((DoubleArray) aValue).doubleArray();
            return "DoubleArray " + getStringForValue(doubleArray);
        }

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

        // Return null
        return null;
    }
}
