package snapcharts.appmisc;
import snap.geom.Side;
import snap.util.FileUtils;
import snap.util.FormatUtils;
import snap.util.JSONNode;
import snap.util.SnapUtils;
import snapcharts.model.*;

import java.io.File;
import java.util.List;

/**
 * Opens a SnapChart in Plotly.
 */
public class OpenInPlotly {

    // The string buffer
    private StringBuffer  _sb = new StringBuffer();

    // The current chart
    private Chart  _chart;

    /**
     * Opens chart in plotly.
     */
    public void openInPlotly(List<Chart> theCharts)
    {
        // Generate HTML
        String htmlStr = getHtmlFileString(theCharts);

        // Open HTML string in browser
        openHTMLStringInBrowser("Plotly.html", htmlStr);
    }

    /**
     * Returns the html file string.
     */
    public String getHtmlFileString(List<Chart> theCharts)
    {
        writeHtmlHeader();
        writeHtmlBody(theCharts);
        for (int i=0; i<theCharts.size(); i++)
            writeChart(theCharts.get(i), i);
        _sb.append("</html>\n");
        return _sb.toString();
    }

    /**
     * Writes the HTML header.
     */
    private void writeHtmlHeader()
    {
        _sb.append("<!DOCTYPE html>\n");
        _sb.append("<html lang='en' class=''>\n");
        _sb.append("<head>\n");
        _sb.append("<script src='https://cdn.plot.ly/plotly-latest.min.js'></script>\n");
        _sb.append("</head>\n\n");
    }

    /**
     * Writes the HTML body.
     */
    private void writeHtmlBody(List<Chart> theCharts)
    {
        String style = "min-width:310px;max-width:640px;height:480px;margin-top:30px;margin-left:30px;box-shadow:1px 1px 6px grey;";
        _sb.append("<body>\n");

        for (int i=0; i<theCharts.size(); i++)
            _sb.append("<div id='chartDiv").append(i).append("' style='").append(style).append("'></div>\n");

        _sb.append("<!-- Plotly charts will be drawn inside these DIVs -->\n");
        _sb.append("<br><br><br>\n");
        _sb.append("</body>\n");
    }

    /**
     * Writes a chart.
     */
    private void writeChart(Chart aChart, int anIndex)
    {
        // Set current chart
        _chart = aChart;

        // Write script open
        _sb.append("<script>\n");

        // Get DataSet info
        DataSetList dataSetList = aChart.getDataSetList();
        List<DataSet> dataSets = dataSetList.getDataSets();
        int dsetCount = dataSets.size();

        // Iterate over DataSets and write trace declaration for each: var trace0 = [ ... ]; var trace1 = [ ... ]; ...
        for (int i=0; i<dsetCount; i++) {
            DataSet dset = dataSets.get(i);
            writeDataSet(dset, i);
        }

        // Write Data declaration: var data [ trace0, trace1, ... ];
        _sb.append("var data = [");
        for (int i=0; i<dsetCount; i++)
            _sb.append(i>0 ? ", " : " ").append("trace").append(i);
        _sb.append(" ];\n\n");

        // Write Chart layout declaration
        writeChartLayout(aChart);

        // Add 'Edit chart' link
        _sb.append("var config = {\n");
        _sb.append("  showLink: true,\n");
        _sb.append("  scrollZoom: true,\n");
        _sb.append("  plotlyServerURL: 'https://chart-studio.plotly.com'\n");
        _sb.append("};\n\n");

        // Write Plotly invocation: Plotly.newPlot('myDiv', data);
        _sb.append("Plotly.newPlot('chartDiv").append(anIndex).append("', data, layout, config);\n\n");

        // Write script close
        _sb.append("</script>\n");
    }

    /**
     * Writes chart layout declaration.
     */
    private void writeChartLayout(Chart aChart)
    {
        // Create layoutJS JSON node
        JSONNode layoutJS = new JSONNode();

        // Create title JSON node
        JSONNode titleJS = new JSONNode();
        layoutJS.addKeyValue("title", titleJS);

        // Get title/subtitle and add to layoutJS
        String title = aChart.getHeader().getTitle();
        String subtitle = aChart.getHeader().getSubtitle();
        if (subtitle!=null && subtitle.length()>0)
            title = title + "<br>" + subtitle;
        titleJS.addKeyValue("text", title);

        // Write the axis layout
        AxisType[] axisTypes = aChart.getDataSetList().getAxisTypes();
        for (AxisType axisType : axisTypes)
            writeChartAxisLayout(aChart, axisType, layoutJS);

        // Write layout
        _sb.append("var layout").append(" = ");
        String layoutStr = layoutJS.toString();
        _sb.append(layoutStr);
        _sb.append(";\n\n");
    }

    /**
     * Writes chart layout declaration.
     */
    private void writeChartAxisLayout(Chart aChart, AxisType anAxisType, JSONNode layoutJS)
    {
        // Get axis and axis name
        Axis axis = aChart.getAxisForType(anAxisType);
        String axisName = getAxisName(anAxisType);

        // Create axis JSONNode
        JSONNode axisJS = new JSONNode();

        // Set title
        String title = axis.getTitle();
        if (title !=null && title.length() > 0) {
            JSONNode titleJS = new JSONNode();
            titleJS.addKeyValue("text", title);
            axisJS.addKeyValue("title", titleJS);
        }

        // Add showline, ticks, mirror (whether axis should showline on opposite side)
        axisJS.addKeyValue("showline", true);
        axisJS.addKeyValue("ticks", "inside");
        axisJS.addKeyValue("mirror", true);

        // If X Axis, see if we need to add domain entry
        if (anAxisType == AxisType.X) {
            JSONNode domain = getXAxisDomain(aChart);
            if (domain != null)
                axisJS.addKeyValue("domain", domain);
        }

        // Set Y on particular side
        if (anAxisType.isAnyY() && anAxisType!=AxisType.Y) {
            axisJS.addKeyValue("side", axis.getSide().toString().toLowerCase());
            axisJS.addKeyValue("overlaying", "y");
            if (anAxisType == AxisType.Y2) {
                axisJS.addKeyValue("anchor", "x");
            }
            if (anAxisType == AxisType.Y3) {
                double position = getYAxisPosition(aChart, anAxisType);
                if (position > 0 && position < 1) {
                    axisJS.addKeyValue("anchor", "free");
                    axisJS.addKeyValue("position", .15);
                }
                else axisJS.addKeyValue("anchor", "x");
            }
            if (anAxisType == AxisType.Y4) {
                double position = getYAxisPosition(aChart, anAxisType);
                if (position > 0 && position < 1) {
                    axisJS.addKeyValue("anchor", "free");
                    axisJS.addKeyValue("position", .85);
                }
                else axisJS.addKeyValue("anchor", "x");
            }
        }

        // Add log support
        if (axis.isLog())
            axisJS.addKeyValue("type", "log");

        if (axisJS.getNodeCount() > 0) {
            layoutJS.addKeyValue(axisName, axisJS);
        }
    }

    /**
     * Writes a DataSet, e.g.: var trace1 = { x: [1, 2, 3, 4], y: [0, 2, 3, 5], type: 'scatter' };
     */
    private void writeDataSet(DataSet aDataSet, int anIndex)
    {
        // Get DataSet info
        int pointCount = aDataSet.getPointCount();
        DataType dataType = aDataSet.getDataType();
        int chanCount = dataType.getChannelCount();

        // Create new TraceJSON
        JSONNode traceJS = new JSONNode();

        // Add: type : 'scatter'
        switch (_chart.getType()) {
            case BAR:
            case BAR_3D:
                traceJS.addKeyValue("type", "bar"); break;
            case CONTOUR:
                traceJS.addKeyValue("type", "contour"); break;
            //case PIE: traceJS.addKeyValue("type", "pie"); break;
            case POLAR:
                traceJS.addKeyValue("type", "scatterpolar"); break;
            case LINE_3D:
                traceJS.addKeyValue("type", "scatter3d"); break;
            default:
                traceJS.addKeyValue("type", "scatter");
        }

        // If ChartType.AREA, add: fill: 'tozeroy'
        if (_chart.getType() == ChartType.AREA)
            traceJS.addKeyValue("fill", "tozeroy");

        // If ChartType.SCATTER, add: mode: 'markers'
        if (_chart.getType() == ChartType.SCATTER)
            traceJS.addKeyValue("mode", "markers");

        // If ChartType.CONTOUR, add: colorscale: 'Jet'
        if (_chart.getType() == ChartType.CONTOUR) {

            // colorscale
            traceJS.addKeyValue("colorscale", "Jet");

            // line { smoothing : 0 }
            JSONNode lineJS = new JSONNode("line", null);
            lineJS.addKeyValue("smoothing", "0");
            traceJS.addKeyValue("line", lineJS);

            // Add:
            traceJS.addKeyValue("autocontour", false);
            traceJS.addKeyValue("ncontours", "16");

            JSONNode contourJS = new JSONNode("contours", null);
            contourJS.addKeyValue("start", aDataSet.getMinZ());
            contourJS.addKeyValue("end", aDataSet.getMaxZ());
            contourJS.addKeyValue("size", (aDataSet.getMaxZ() - aDataSet.getMinZ()) / 16);
            traceJS.addKeyValue("contours", contourJS);
        }

        // Iterate over channels and add channel values for each
        for (int i=0; i<chanCount; i++) {

            // Get DataChan, DataChanString
            DataChan dataChan = dataType.getChannel(i);
            String dataChanStr = dataChan.toString().toLowerCase();

            // If polar do something
            if (_chart.getType() == ChartType.POLAR) {
                if (dataChan==DataChan.X) dataChanStr = "theta";
                else if (dataChan==DataChan.Y) dataChanStr = "r";
                else return;
                traceJS.addKeyValue("mode", "markers");
            }

            // Get Channel, create valsJS and add to traceJS
            JSONNode valsJS = new JSONNode();
            traceJS.addKeyValue(dataChanStr, valsJS);

            // Iterate over values and add to valsJS
            for (int j=0; j<pointCount; j++)
                valsJS.addValue(aDataSet.getValueForChannel(dataChan, j));
        }

        // If ChartType.LINE_3D, add: fill: 'tozeroy'
        if (_chart.getType() == ChartType.LINE_3D) {
            JSONNode valsJS = new JSONNode();
            traceJS.addKeyValue("z", valsJS);
            for (int j = 0; j < pointCount; j++)
                valsJS.addValue(anIndex);
            traceJS.addKeyValue("fill", "tozeroy");
        }

        // If not Y axis
        if (aDataSet.getAxisTypeY() != AxisType.Y)
            traceJS.addKeyValue("yaxis", aDataSet.getAxisTypeY().toString().toLowerCase());

        // Write trace
        _sb.append("var trace").append(anIndex).append(" = ");
        String traceString = traceJS.toString();
        _sb.append(traceString);
        _sb.append(";\n\n");
    }

    /**
     * Returns the axis name.
     */
    private String getAxisName(AxisType anAxisType)
    {
        switch (anAxisType)
        {
            case X: return "xaxis";
            case Y: return "yaxis";
            case Y2: return "yaxis2";
            case Y3: return "yaxis3";
            case Y4: return "yaxis4";
            case Z: return "zaxis";
            default: throw new RuntimeException("OpenInPlotly.getAxisName: Unknown axis: " + anAxisType);
        }
    }

    /**
     * Returns the suggested domain for a chart - which is a start/end values
     */
    private JSONNode getXAxisDomain(Chart aChart)
    {
        AxisType[] axisTypes = aChart.getDataSetList().getAxisTypes();
        if (axisTypes.length <= 2)
            return null;
        int left = 0, right = 0;
        for (AxisType axisType : axisTypes) {
            Axis axis = aChart.getAxisForType(axisType);
            if (axis.getSide() == Side.LEFT) left ++;
            if (axis.getSide() == Side.RIGHT) right ++;
        }

        double start = left==2 ? .3 : 0;
        double end = right==2 ? .7 : 1;
        JSONNode node = new JSONNode();
        node.addValue(start);
        node.addValue(end);
        return node; //"[" + FormatUtils.formatNum("#.#", start) + ", " + FormatUtils.formatNum("#.#", end) + "]";
    }

    /**
     * Returns the suggested Y axis position for a chart - which is a start/end values
     */
    private double getYAxisPosition(Chart aChart, AxisType anAxisType)
    {
        AxisType[] axisTypes = aChart.getDataSetList().getAxisTypes();
        Side side = aChart.getAxisForType(anAxisType).getSide();
        int count = 0;
        for (AxisType axisType : axisTypes) {
            if (axisType == anAxisType)
                break;
            if (aChart.getAxisForType(axisType).getSide() == side)
                count++;
        }
        if (side == Side.RIGHT)
            return 1 - count * .15;
        return count * .15;
    }

    /**
     * Writes current text to file.
     */
    private void openHTMLStringInBrowser(String aName, String aStr)
    {
        //File file = FileUtils.getTempFile("Plotly.html");
        //File file2 = new File("/tmp/Plotly.html");
        //try { FileUtils.writeBytes(file2, aStr.getBytes()); }
        //catch (Exception e) { throw new RuntimeException(e); }
        //GFXEnv.getEnv().openURL(file);

        // Get filename and file
        String filename = SnapUtils.getTempDir() + aName;
        File file = FileUtils.getFile(filename);

        // TeaVM seems to sometimes use remnants of old file?
        if (SnapUtils.isTeaVM) {
            try { file.delete(); }
            catch (Exception e) { System.err.println("OpenInPlotly.writeToFile: Error deleting file"); }
        }

        // Get bytes
        byte[] bytes = aStr.getBytes();

        // Write bytes to file
        try { FileUtils.writeBytes(file, bytes); }
        catch(Exception e) { throw new RuntimeException(e); }

        // Open file
        FileUtils.openFile(file);
    }
}
