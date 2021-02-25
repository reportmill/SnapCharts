package snapcharts.doc;
import java.util.*;
import snap.gfx.Color;
import snap.geom.VPos;
import snap.util.*;
import snapcharts.model.*;
import snapcharts.util.MapWriter;

/**
 * A class to load chart parameters from JSON.
 */
public class ChartWriter {
    
    // The chart view
    private Chart _chart;

    /**
     * Create ChartWriter for given ChartView.
     */
    public ChartWriter(Chart aChart)  { _chart = aChart; }

    /**
     * Returns a String for chart.
     */
    public String getString()
    {
        Map map = writeAll();
        return new MapWriter().getString(map);
    }

    /**
     * Returns a String for chart with <Script> tags.
     */
    public String getStringWithScript()
    {
        String str0 = "<script src=\"http://reportmill.com/rmc/RMCharts.js\" defer></script>\n";
        String str1 = "var params = " + getString() + ";\n\n";
        String str2 = "window.onload = function() { ReportMill.chart(\"container\", params); }\n\n";
        String str3 = str0 + "<script defer>\n\n" + str1 + str2 + "</script>";
        return str3;
    }

    /**
     * Returns a String for chart with <Script> tags.
     */
    public String getStringWithHTML()
    {
        String str0 = "<HTML>\n<head>\n\n";
        String str1 = getStringWithScript();
        String str2 = "\n\n</head>\n\n<body>\n\n";
        String str3 = "<div id=\"container\" ";
        String str4 = "style=\"min-width:310px;max-width:800px;height:400px;box-shadow:1px 2px 4px grey;\"></div>\n\n";
        String str5 = "</body>\n</HTML>";
        return str0 + str1 + str2 + str3 + str4 + str5;
    }

    /**
     * Write chart to map.
     */
    public Map writeAll()
    {
        // Create map
        Map map = new HashMap();

        // Write chart
        writeChart(map);
        writeColors(map);
        writeLegend(map);
        writePlotOptions(map);
        writeSeries(map);
        writeSubtitle(map);
        writeTitle(map);
        writeXAxis(map);
        writeYAxis(map);

        return map;
    }

    /**
     * Write a chart node.
     */
    protected void writeChart(Map aMap)
    {
        // Create map
        Map map = new HashMap();

        // Write type
        switch(_chart.getType()) {
            case BAR: map.put("type", "column"); break;
            case LINE: map.put("type", "line"); break;
            case PIE: map.put("type", "pie"); break;
            default: System.err.println("ChartWriter.writeChart: Unknown type: " + _chart.getType());
        }

        if (!map.isEmpty()) aMap.put("chart", map);
    }

    /**
     * Write a Title node.
     */
    protected void writeTitle(Map aMap)
    {
        Map map = new HashMap();
        String text = _chart.getHeader().getTitle();
        if (text!=null) map.put("text", text);

        if (!map.isEmpty()) aMap.put("title", map);
    }

    /**
     * Write a Subtitle node.
     */
    protected void writeSubtitle(Map aMap)
    {
        Map map = new HashMap();

        String text = _chart.getHeader().getSubtitle();
        if (text!=null) map.put("text", text);

        if (!map.isEmpty()) aMap.put("subtitle", map);
    }

    /**
     * Write a XAxis node.
     */
    protected void writeXAxis(Map aMap)
    {
        // Create map
        Map map = new HashMap();

        // Get XAxis
        AxisX xaxis = _chart.getAxisX();

        // Handle title node
        writeXAxisTitle(map);

        // Handle categories array
        writeXAxisCategories(map);

        // Handle labels node
        writeXAxisLabels(map);

        // Handle tickLength
        double val = xaxis.getTickLength();
        if (val!=999) map.put("tickLength", val);

        // Add map
        if (!map.isEmpty()) aMap.put("xaxis", map);
    }

    /**
     * Write xaxis categories node.
     */
    protected void writeXAxisCategories(Map aMap)
    {
        // Iterate over array
        List <String> cats = _chart.getAxisX().getCategories();
        if (cats!=null) aMap.put("categories", cats);
    }

    /**
     * Write a XAxis labels node.
     */
    protected void writeXAxisLabels(Map aMap)
    {
        // Get YAxis
        AxisX xaxis = _chart.getAxisX();

        // Handle style node
        //case "style": writeXAxisLabelsStyle(child); break;
    }

    /**
     * Write a XAxis title node.
     */
    protected void writeXAxisTitle(Map aMap)
    {
        // Create map
        Map map = new HashMap();

        // Get XAxis
        AxisX xaxis = _chart.getAxisX();

        // Handle xaxis.title.align
        /*case "align": {
            String val = child.getString().toLowerCase();
            if (val.equals("high")) xaxis.getTitleView().getParent().setAlign(VPos.TOP);
            else if (val.equals("low")) xaxis.getTitleView().getParent().setAlign(VPos.BOTTOM);
        } break;*/

        // Handle xaxis.title.rotation
        /*case "rotation": {
            double rot = child.getNumber().doubleValue();
            yaxis.getTitleView().setRotate(rot);
        } break;*/

        // Handle xaxis.title.text
        /*case "offset": {
            double val = child.getNumber().doubleValue();
            yaxis.setTitleOffset(val);
        } break;*/

        // Handle xaxis.title.text
        /*case "text": {
            String text = child.getString();
            yaxis.setTitle(text);
        } break;*/

        // Handle xaxis.title.X
        /*case "x": {
            double x = child.getNumber().doubleValue();
            yaxis.getTitleView().setTransX(x);
        } break;*/

        // Handle xaxis.title.Y
        /*case "y": {
            double y = child.getNumber().doubleValue();
            yaxis.getTitleView().setTransY(y);
        } break;*/

        // Add map
        if (!map.isEmpty()) aMap.put("title", map);
    }

    /**
     * Write a YAxis node.
     */
    protected void writeYAxis(Map aMap)
    {
        // Create map
        Map map = new HashMap();

        // Get YAxis
        AxisY yaxis = _chart.getAxisY();

        // Handle gridLineColor node
        Color gridLineColor = yaxis.getGridLineColor();
        if (gridLineColor!=null) map.put("gridLineColor", gridLineColor);

        // Handle gridlineDashStyle node
        double gridlineDashStyle[] = yaxis.getGridLineDashArray();
        if (gridlineDashStyle!=null) {
            if (ArrayUtils.equals(gridlineDashStyle, new double[] { 4d, 3d }))
                map.put("gridlineDashStyle", "dash");
            else if (ArrayUtils.equals(gridlineDashStyle, new double[] { 8d, 3d }))
                map.put("gridlineDashStyle", "longdash");
        }

        // Handle labels node
        writeYAxisLabels(map);

        // Handle title node
        writeYAxisTitle(map);

        // Add map
        if (!map.isEmpty()) aMap.put("yaxis", map);
    }

    /**
     * Write a YAxis labels node.
     */
    protected void writeYAxisLabels(Map aMap)
    {
        // Create map
        Map map = new HashMap();

        // Get YAxis
        AxisY yaxis = _chart.getAxisY();

        // Handle style node
        writeYAxisLabelsStyle(map);

        // Add map
        if (!map.isEmpty()) aMap.put("labels", map);
    }

    /**
     * Write a YAxis labels style node.
     */
    protected void writeYAxisLabelsStyle(Map aMap)
    {
        // Create map
        Map map = new HashMap();

        // Get YAxis
        AxisY yaxis = _chart.getAxisY();

        // Add map
        if (!map.isEmpty()) aMap.put("style", map);
    }

    /**
     * Write a YAxis title node.
     */
    protected void writeYAxisTitle(Map aMap)
    {
        // Create map
        Map map = new HashMap();

        // Get YAxis
        AxisY yaxis = _chart.getAxisY();

        // Handle yaxis.title.align
        VPos align = yaxis.getTitleAlign().getVPos();
        if (align==VPos.TOP) map.put("align", "high");
        else if (align==VPos.BOTTOM) map.put("align", "low");

        // Handle yaxis.title.rotation
        double rotation = yaxis.getTitleRotate();
        if (rotation!=0) map.put("rotation", rotation);

        // Handle yaxis.title.offset
        //double offset = yaxis.getTitleOffset();
        //if (offset!=0) map.put("offset", offset);

        // Handle yaxis.title.style
        writeYAxisTitleStyle(map);

        // Handle yaxis.title.text
        String text = yaxis.getTitle();
        if (text!=null) map.put("text", text);

        // Handle yaxis.title.x
        //double x = yaxis.getTitleX();
        //if (x!=0) map.put("x", x);

        // Handle yaxis.title.y
        //double y = yaxis.getTitleY();
        //if (y!=0) map.put("y", y);

        // Add map
        if (!map.isEmpty()) aMap.put("title", map);
    }

    /**
     * Write a YAxis title style node.
     */
    protected void writeYAxisTitleStyle(Map aMap)
    {
        // Create map
        Map map = new HashMap();

        // Get YAxis
        AxisY yaxis = _chart.getAxisY();

        // Add map
        if (!map.isEmpty()) aMap.put("style", map);
    }

    /**
     * Write a Legend node.
     */
    protected void writeLegend(Map aMap)
    {
        // Create map
        Map map = new HashMap();

        // Get enabled node
        boolean enabled = _chart.getLegend().isShowLegend();
        if (enabled) map.put("enabled", enabled);

        // Add map
        if (!map.isEmpty()) aMap.put("legend", map);
    }

    /**
     * Write plotOptions node.
     */
    protected void writePlotOptions(Map aMap)
    {
        // Create map
        Map map = new HashMap();

        // Handle plotOptions.column
        writePlotOptionsColumn(map);

        // Handle plotOptions.series
        writePlotOptionsSeries(map);

        // Add map
        if (!map.isEmpty()) aMap.put("plotOptions", map);
    }

    /**
     * Write plotOptions.column node.
     */
    protected void writePlotOptionsColumn(Map aMap)
    {
        // Create map
        Map map = new HashMap();

        // Get column chart
        BarProps colChart = _chart.getTypeHelper().getBarProps();

        // Handle colorByPoint
        boolean colorByPoint = colChart.isColorValues();
        if (colorByPoint) map.put("colorByPoint", colorByPoint);

        // Handle groupPadding
        double groupPadding = colChart.getGroupPadding();
        if (groupPadding!=999) map.put("groupPadding", groupPadding);

        // Handle pointPadding
        double pointPadding = colChart.getBarPadding();
        if (pointPadding!=999) map.put("pointPadding", pointPadding);

        // Add map
        if (!map.isEmpty()) aMap.put("column", map);
    }

    /**
     * Write plotOptions.series node.
     */
    protected void writePlotOptionsSeries(Map aMap)
    {
        // Create map
        Map map = new HashMap();

        // Handle pointStart
        int pointStart = _chart.getDataSetStartValue();
        if (pointStart!=0) map.put("pointStart", pointStart);

        // Add map
        if (!map.isEmpty()) aMap.put("series", map);
    }

    /**
     * Write series node.
     */
    protected void writeSeries(Map aMap)
    {
        // Create list
        List list = new ArrayList();

        // Iterate over series
        DataSetList dset = _chart.getDataSetList();
        for (DataSet series : dset.getDataSets()) {

            // Create map
            Map map = new HashMap();

            // Handle name
            String name = series.getName();
            if (name!=null) map.put("name", name);

            // Handle data
            writeSeriesData(map, series);

            // Add map
            if (!map.isEmpty()) list.add(map);
        }

        // Add list
        if (!list.isEmpty()) aMap.put("series", list);
    }

    /**
     * Write series data node.
     */
    protected void writeSeriesData(Map aMap, DataSet aDataSet)
    {
        // Create list
        List list = new ArrayList();
        boolean rich = false;

        // Iterate over points
        for (int i=0, iMax=aDataSet.getPointCount(); i<iMax; i++) {

            // Create map
            Map map = new HashMap();

            // Handle name
            String name = aDataSet.getC(i);
            if (name!=null) {
                map.put("name", name);
                rich = true;
            }

            // Handle y
            Double valY = aDataSet.getValueY(i);
            if (valY != null)
                map.put("y", valY);

            // Add map
            if (!map.isEmpty()) list.add(map);
        }

        // If not rich, just get values
        if (!rich) {
            List list2 = new ArrayList();
            for (Map m : (List <Map>)list)
                list2.add(m.get("y"));
            list = list2;
        }

        // Add list
        if (!list.isEmpty())
            aMap.put("data", list);
    }

    /**
     * Write chart colors.
     */
    protected void writeColors(Map aMap)
    {
        // Create list
        List list = new ArrayList();

        Color colors[] = _chart.getColors();

        // Add list
        if (!list.isEmpty()) aMap.put("colors", list);
    }
}