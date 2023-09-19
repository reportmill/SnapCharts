/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.repl;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.gfx.Image;
import snap.text.TextBlock;
import snap.util.Convert;
import snap.view.*;
import snapcharts.app.DataSetPane;
import snapcharts.data.DataSet;
import snapcharts.data.DoubleArray;
import snapcharts.doc.ChartArchiver;
import snapcharts.model.Chart;
import snapcharts.view.ChartView;
import java.lang.reflect.Array;

/**
 * This View subclass shows snippets.
 */
class ConsoleViewUtils {

    // Constants
    public static final Color ERROR_COLOR = Color.get("#CC0000");
    private static final Color DEFAULT_TEXTAREA_FILL = new Color(.95);
    private static final Color DEFAULT_TEXTAREA_TEXTFILL = Color.GRAY2;

    /**
     * Override to support custom content views for response values.
     */
    public static ConsoleItemBox createBoxViewForValue(Object value)
    {
        View contentView = createContentViewForValue(value);
        return new ConsoleItemBox(contentView);
    }

    /**
     * Override to support custom content views for response values.
     */
    public static View createContentViewForValue(Object value)
    {
        // Handle View
        if (value instanceof View)
            return (View) value;

        // Handle ViewOwner
        if (value instanceof ViewOwner)
            return createContentViewForViewOwner((ViewOwner) value);

        // Handle Chart
        if (value instanceof Chart)
            return createContentViewForChart((Chart) value);

        // Handle DataSet
        if (value instanceof DataSet)
            return createContentViewForDataSet((DataSet) value);

        // Handle Image
        if (value instanceof Image)
            return createContentViewForImage((Image) value);

        // Handle TextBlock
        if (value instanceof TextBlock)
            return createContentViewForTextBlock((TextBlock) value);

        // Do normal version
        String responseText = getStringForValue(value);
        TextBlock textBlock = createTextBlockForString(responseText);
        return createContentViewForTextBlock(textBlock);
    }

    /**
     * Creates TextBlock for String.
     */
    private static TextBlock createTextBlockForString(String aString)
    {
        // Create TextBlock and configure Style
        TextBlock textBlock = new TextBlock();
        textBlock.setDefaultStyle(textBlock.getDefaultStyle().copyFor(DEFAULT_TEXTAREA_TEXTFILL));
        textBlock.setDefaultStyle(textBlock.getDefaultStyle().copyFor(Font.Arial11));

        // Set string
        textBlock.setString(aString);

        // Return
        return textBlock;
    }

    /**
     * Creates content view for TextBlock.
     */
    private static View createContentViewForTextBlock(TextBlock textBlock)
    {
        // Create and configure TextArea
        TextArea textArea = new TextArea();
        textArea.setBorderRadius(4);
        textArea.setFill(DEFAULT_TEXTAREA_FILL);
        textArea.setEditable(true);

        // Configure TextArea Sizing
        textArea.setGrowWidth(true);
        textArea.setMinSize(30, 20);
        textArea.setPadding(5, 5, 5, 5);

        // Set TextBlock
        textArea.setSourceText(textBlock);

        // If large text, wrap in ScrollView
        if (textArea.getPrefHeight() > 300) {
            ScrollView scrollView = new ScrollView(textArea);
            scrollView.setBorderRadius(4);
            scrollView.setMaxHeight(300);
            scrollView.setGrowWidth(true);
            scrollView.setGrowHeight(true);
            return scrollView;
        }

        // Return
        return textArea;
    }

    /**
     * Creates content view for ViewOwner.
     */
    private static View createContentViewForViewOwner(ViewOwner aViewOwner)
    {
        View view = aViewOwner.getUI();
        return view;
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
     * Creates content view for Image.
     */
    private static View createContentViewForImage(Image anImage)
    {
        // Create imageView for image
        ImageView imageView = new ImageView(anImage);
        imageView.setMaxHeight(350);

        // Return
        return imageView;
    }

    /**
     * Returns the value as a string.
     */
    private static String getStringForValue(Object aValue)
    {
        // Handle null
        if (aValue == null)
            return "null";

        // Handle String
        if (aValue instanceof String)
            return (String) aValue;

        // Handle double[], DoubleArray
        if (aValue instanceof double[])
            return "double[] " + Convert.doubleArrayToString((double[]) aValue);
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

        // Handle exception
        if (aValue instanceof Exception) {
            Exception exception = (Exception) aValue;
            Throwable rootCause = exception;
            while (rootCause.getCause() != null) rootCause = rootCause.getCause();
            return rootCause.toString();
        }

        // Handle Boolean, Number
        if (aValue instanceof Number || aValue instanceof Boolean || aValue instanceof Character)
            return aValue.toString();

        // Handle Class
        if (aValue instanceof Class)
            return "Class: " + ((Class) aValue).getName();

        // Handle Array
        Class<?> valueClass = aValue.getClass();
        if (valueClass.isArray()) {
            StringBuffer sb = new StringBuffer();
            int length = Array.getLength(aValue);
            for (int i = 0; i < length; i++) {
                if (i > 0) sb.append(", ");
                Object val = Array.get(aValue, i);
                String str = getStringForValue(val);
                sb.append(str);
            }
            return sb.toString();
        }

        // Handle anything
        String className = aValue.getClass().getSimpleName();
        return className + ": " + aValue;
    }
}
