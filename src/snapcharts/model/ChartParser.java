package snapcharts.model;
import java.util.*;
import snap.gfx.Color;
import snap.geom.VPos;
import snap.util.*;
import snap.web.WebURL;

/**
 * A class to load chart parameters from JSON.
 */
public class ChartParser {
    
    // The chart
    private Chart _chart;

    /**
     * Create ChartParser for given ChartView.
     */
    public ChartParser()  { }

    /**
     * Returns a chart for Source.
     */
    public Doc getDocForSource(Object aSource)
    {
        WebURL url = WebURL.getURL(aSource);
        String name = url.getPathNameSimple();
        String jsonText = url.getText();
        Chart chart = getChartForJSONString(jsonText);
        chart.setName(name + " Chart");

        if (chart.getDataSetList().getName()==null)
            chart.getDataSetList().setName(name + " Data");

        Doc doc = new Doc();
        doc.setName(name + " Doc");
        doc.addChart(chart);
        return doc;
    }

    /**
     * Returns the chart.
     */
    public Chart getChartForJSONString(String aStr)
    {
        _chart = new Chart();
        parseString(aStr);
        return _chart;
    }

    /**
     * Parse given JSON string.
     */
    public void parseString(String aStr)
    {
        JSONNode json = new JSONParser().readString(aStr);

        if(json.isObject())
            parseChart(json);
    }

    /**
     * Parse given JSON object.
     */
    protected void parseChart(JSONNode aNode)
    {
        for(JSONNode child : aNode.getNodes()) { String key = child.getKey();

            switch(key.toLowerCase()) {
                case "chart": parseChartNode(child); break;
                case "colors": parseColors(child); break;
                case "legend": parseLegend(child); break;
                case "plotoptions": parsePlotOptions(child); break;
                case "series": parseSeries(child); break;
                case "subtitle": parseSubtitle(child); break;
                case "title": parseTitle(child); break;
                case "xaxis": parseXAxis(child); break;
                case "yaxis": parseYAxis(child); break;
                default: System.out.println("Unsupported node: " + key);
            }
        }
    }

    /**
     * Parse a chart node.
     */
    protected void parseChartNode(JSONNode aNode)
    {
        String type = aNode.getNodeString("type");
        if(type!=null) switch(type) {
            case "column": _chart.setType(ChartType.BAR); break;
            case "column3d": _chart.setType(ChartType.BAR_3D); break;
            case "line": _chart.setType(ChartType.LINE); break;
            case "pie": _chart.setType(ChartType.PIE); break;
        }
    }

    /**
     * Parse a Title node.
     */
    protected void parseTitle(JSONNode aNode)
    {
        String text = aNode.getNodeString("text");
        if(text!=null)
            _chart.setTitle(text);
    }

    /**
     * Parse a Subtitle node.
     */
    protected void parseSubtitle(JSONNode aNode)
    {
        String text = aNode.getNodeString("text");
        if(text!=null)
            _chart.setSubtitle(text);
    }

    /**
     * Parse a XAxis node.
     */
    protected void parseXAxis(JSONNode aNode)
    {
        // Get XAxis
        AxisX xaxis = _chart.getAxisX();

        // Iterate over nodes
        for(JSONNode child : aNode.getNodes()) { String key = child.getKey();
            switch(key.toLowerCase()) {

                // Handle title node
                case "title": parseXAxisTitle(child); break;

                // Handle categories array
                case "categories": parseXAxisCategories(child); break;

                // Handle labels node
                case "labels": parseXAxisLabels(child); break;

                // Handle tickLength
                case "ticklength": {
                    double val = child.getNumber().doubleValue();
                    xaxis.setTickLength(val);
                } break;

                // Handle default (complain)
                default: System.out.println("Unsupported node: xaxis." + key + " = " + child.getString());
            }
        }
    }

    /**
     * Parse xaxis categories node.
     */
    protected void parseXAxisCategories(JSONNode aNode)
    {
        // Complain if not array
        if(!aNode.isArray()) { System.err.println("ChartParser.parseXAxisCategories: Node not array"); return; }

        // Iterate over array
        List <String> strings = new ArrayList<>();
        for(int i=0;i<aNode.getNodeCount();i++) { JSONNode strNode = aNode.getNode(i);
            strings.add(strNode.getString()); }

        // Set categories string list
        _chart.getAxisX().setCategories(strings);
    }

    /**
     * Parse a XAxis labels node.
     */
    protected void parseXAxisLabels(JSONNode aNode)
    {
        // Get XAxis
        AxisX xaxis = _chart.getAxisX();

        // Iterate over nodes
        for(JSONNode child : aNode.getNodes()) { String key = child.getKey();
            switch(key.toLowerCase()) {

                // Handle style node
                //case "style": parseXAxisLabelsStyle(child); break;

                // Handle default (complain)
                default: System.out.println("Unsupported node: xaxis.labels." + key + " = " + child.getString());
            }
        }
    }

    /**
     * Parse a XAxis title node.
     */
    protected void parseXAxisTitle(JSONNode aNode)
    {
        // Get XAxis
        AxisX xaxis = _chart.getAxisX();

        // Iterate over nodes
        for(JSONNode child : aNode.getNodes()) { String key = child.getKey();
            switch(key.toLowerCase()) {

                // Handle xaxis.title.align
                /*case "align": {
                    String val = child.getString().toLowerCase();
                    if(val.equals("high")) xaxis.getTitleView().getParent().setAlign(VPos.TOP);
                    else if(val.equals("low")) xaxis.getTitleView().getParent().setAlign(VPos.BOTTOM);
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

                // Handle default (complain)
                default: System.out.println("Unsupported node: xaxis.title." + key + " = " + child.getString());
            }
        }
    }

    /**
     * Parse a YAxis node.
     */
    protected void parseYAxis(JSONNode aNode)
    {
        // Get YAxis
        AxisY yaxis = _chart.getAxisY();

        // Iterate over nodes
        for(JSONNode child : aNode.getNodes()) { String key = child.getKey();
            switch(key.toLowerCase()) {

                // Handle gridLineColor node
                case "gridlinecolor": {
                    String cstr = child.getString(); Color color = Color.get(cstr);
                    if(color!=null) yaxis.setGridLineColor(color);
                    else System.out.println("ChartParser: Invalid string for yaxis.gridlinecolor: " + cstr);
                } break;

                // Handle gridlineDashStyle node
                case "gridlinedashstyle": {
                    String str = child.getString();
                    if(str.equals("dash")) yaxis.setGridLineDashArray(new double[] { 4d, 3d });
                    else if(str.equals("longdash")) yaxis.setGridLineDashArray(new double[] { 8d, 3d });
                } break;

                // Handle labels node
                case "labels": parseYAxisLabels(child); break;

                // Handle title node
                case "title": parseYAxisTitle(child); break;

                // Handle default (complain)
                default: System.out.println("Unsupported node: yaxis." + key + " = " + child.getString());
            }
        }
    }

    /**
     * Parse a YAxis labels node.
     */
    protected void parseYAxisLabels(JSONNode aNode)
    {
        // Get YAxis
        AxisY yaxis = _chart.getAxisY();

        // Iterate over nodes
        for(JSONNode child : aNode.getNodes()) { String key = child.getKey();
            switch(key.toLowerCase()) {

                // Handle style node
                case "style": parseYAxisLabelsStyle(child); break;

                // Handle default (complain)
                default: System.out.println("Unsupported node: yaxis.labels." + key + " = " + child.getString());
            }
        }
    }

    /**
     * Parse a YAxis labels style node.
     */
    protected void parseYAxisLabelsStyle(JSONNode aNode)
    {
        // Get YAxis
        AxisY yaxis = _chart.getAxisY();

        // Iterate over nodes
        for(JSONNode child : aNode.getNodes()) { String key = child.getKey();
            switch(key.toLowerCase()) {

                // Handle default (complain)
                default: System.out.println("Unsupported node: yaxis.labels.style." + key + " = " + child.getString());
            }
        }
    }

    /**
     * Parse a YAxis title node.
     */
    protected void parseYAxisTitle(JSONNode aNode)
    {
        // Get YAxis
        AxisY yaxis = _chart.getAxisY();

        // Iterate over nodes
        for(JSONNode child : aNode.getNodes()) { String key = child.getKey();
            switch(key.toLowerCase()) {

                // Handle yaxis.title.align
                case "align": {
                    String val = child.getString().toLowerCase();
                    if(val.equals("high")) yaxis.setTitleAlignY(VPos.TOP);
                    else if(val.equals("low")) yaxis.setTitleAlignY(VPos.BOTTOM);
                } break;

                // Handle yaxis.title.rotation
                case "rotation": {
                    double rot = child.getNumber().doubleValue();
                    yaxis.setTitleRotate(rot);
                } break;

                // Handle yaxis.title.offset
                case "offset": {
                    double val = child.getNumber().doubleValue();
                    yaxis.setTitleOffset(val);
                } break;

                // Handle yaxis.title.style
                case "style": parseYAxisTitleStyle(child); break;

                // Handle yaxis.title.text
                case "text": {
                    String text = child.getString();
                    yaxis.setTitle(text);
                } break;

                // Handle yaxis.title.x
                case "x": {
                    double x = child.getNumber().doubleValue();
                    yaxis.setTitleX(x);
                } break;

                // Handle yaxis.title.y
                case "y": {
                    double y = child.getNumber().doubleValue();
                    yaxis.setTitleY(y);
                } break;

                // Handle default (complain)
                default: System.out.println("Unsupported node: yaxis.title." + key + " = " + child.getString());
            }
        }
    }

    /**
     * Parse a YAxis title style node.
     */
    protected void parseYAxisTitleStyle(JSONNode aNode)
    {
        // Get YAxis
        AxisY yaxis = _chart.getAxisY();

        // Iterate over nodes
        for(JSONNode child : aNode.getNodes()) { String key = child.getKey();
            switch(key.toLowerCase()) {

                // Handle default (complain)
                default: System.out.println("Unsupported node: yaxis.title.style." + key + " = " + child.getString());
            }
        }
    }

    /**
     * Parse a Legend node.
     */
    protected void parseLegend(JSONNode aNode)
    {
        // Get enabled node
        JSONNode enabledNode = aNode.getNode("enabled");
        if(enabledNode!=null)
            _chart.setShowLegend(enabledNode.getBoolean());
    }

    /**
     * Parse plotOptions node.
     */
    protected void parsePlotOptions(JSONNode aNode)
    {
        // Iterate over nodes
        for(JSONNode child : aNode.getNodes()) { String key = child.getKey();
            switch(key.toLowerCase()) {

                // Handle plotOptions.column
                case "column": parsePlotOptionsColumn(child); break;

                // Handle plotOptions.series
                case "series": parsePlotOptionsSeries(child); break;

                // Handle default (complain)
                default: System.out.println("Unsupported node: plotOptions." + key + " = " + child.getString());
            }
        }
    }

    /**
     * Parse plotOptions.column node.
     */
    protected void parsePlotOptionsColumn(JSONNode aNode)
    {
        // Get column chart
        AreaBar colChart = _chart.getAreaTypes().getColumnChart();

        // Iterate over nodes
        for(JSONNode child : aNode.getNodes()) { String key = child.getKey();
            switch(key.toLowerCase()) {

                // Handle colorByPoint
                case "colorbypoint": {
                    boolean val = child.getBoolean();
                    colChart.setColorValues(val);
                } break;

                // Handle groupPadding
                case "grouppadding": {
                    double val = SnapUtils.doubleValue(child.getNumber());
                    colChart.setGroupPadding(val);
                } break;

                // Handle pointPadding
                case "pointpadding": {
                    double val = SnapUtils.doubleValue(child.getNumber());
                    colChart.setBarPadding(val);
                } break;

                // Handle default (complain)
                default: System.out.println("Unsupported node: plotOptions.column." + key + " = " + child.getString());
            }
        }
    }

    /**
     * Parse plotOptions.series node.
     */
    protected void parsePlotOptionsSeries(JSONNode aNode)
    {
        // Iterate over nodes
        for(JSONNode child : aNode.getNodes()) { String key = child.getKey();
            switch(key.toLowerCase()) {

                // Handle pointStart
                case "pointstart": {
                    int start = SnapUtils.intValue(child.getNumber());
                    _chart.setDataSetStartValue(start);
                } break;

                // Handle default (complain)
                default: System.out.println("Unsupported node: plotOptions.series." + key + " = " + child.getString());
            }
        }
    }

    /**
     * Parse series node.
     */
    protected void parseSeries(JSONNode aNode)
    {
        // Complain if not series
        if(!aNode.isArray()) { System.err.println("ChartParser.parseSeries: Series is not array"); return; }

        // Iterate over array
        for(int i=0;i<aNode.getNodeCount();i++) { JSONNode seriesNode = aNode.getNode(i);

            // Create dataset
            DataSet dset = new DataSet();
            _chart.addDataSet(dset);

            // Iterate over nodes
            for(JSONNode child : seriesNode.getNodes()) { String key = child.getKey();
                switch(key.toLowerCase()) {

                    // Handle name
                    case "name": {
                        String name = child.getString();
                        dset.setName(name);
                    } break;

                    // Handle data
                    case "data": parseSeriesData(child, dset); break;

                    // Handle default (complain)
                    default: System.out.println("Unsupported node: series[" + i + "]." + key + " = " + child.getString());
                }
            }
        }
    }

    /**
     * Parse dataset data node.
     */
    protected void parseSeriesData(JSONNode aNode, DataSet aDataSet)
    {
        // Complain if not array
        if(!aNode.isArray()) { System.err.println("ChartParser.series.data: Series.data is not array"); return; }

        // Iterate over array
        for(int i=0;i<aNode.getNodeCount();i++) { JSONNode dataNode = aNode.getNode(i);

            // Handle Node is object
            if(dataNode.isObject()) {
                String name = null; double val = 0;

                // Iterate over nodes
                for(JSONNode child : dataNode.getNodes()) { String key = child.getKey();
                    switch(key.toLowerCase()) {

                        // Handle name
                        case "name": name = child.getString(); break;

                        // Handle y
                        case "y": val = SnapUtils.doubleValue(child.getNumber()); break;

                        // Handle default (complain)
                        default: System.out.println("Unsupported node: series[]." + key + " = " + child.getString());
                    }
                }

                // Add point
                aDataSet.addPointCY(name, val);
            }

            // Handle Node is number
            else {
                Number value = dataNode.getNumber();
                if(value!=null)
                    aDataSet.addPointXY(null, value.doubleValue());
            }
        }
    }

    /**
     * Parse chart colors.
     */
    protected void parseColors(JSONNode aNode)
    {
        // Complain if not array
        if(!aNode.isArray()) { System.err.println("Colors is not array"); return; }

        // Iterate over nodes to get color strings
        List <Color> colors = new ArrayList();
        for(int i=0;i<aNode.getNodeCount();i++) { JSONNode colorNode = aNode.getNode(i);

            // Get color
            String str = colorNode.getString();
            Color color = Color.get(str);
            if(color==null) System.err.println("ChartParser.parseColors: Invalid color: " + str);
            else colors.add(color);
        }

        // Set colors
        _chart.setColors(colors.toArray(new Color[colors.size()]));
    }
}