package snapcharts.appmisc;
import snap.geom.Pos;
import snap.geom.Side;
import snap.gfx.Color;
import snap.text.NumberFormat;
import snap.text.TextFormat;
import snap.util.ArrayUtils;
import snap.util.FileUtils;
import snap.util.JSONNode;
import snap.util.SnapUtils;
import snapcharts.data.DataChan;
import snapcharts.data.DataSet;
import snapcharts.data.DataType;
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
        writeHtmlHeader(theCharts.size() == 1);
        writeHtmlBody(theCharts);
        for (int i=0; i<theCharts.size(); i++)
            writeChart(theCharts.get(i), i);
        _sb.append("</html>\n");
        return _sb.toString();
    }

    /**
     * Writes the HTML header.
     */
    private void writeHtmlHeader(boolean isSingle)
    {
        _sb.append("<!DOCTYPE html>\n");
        _sb.append("<html lang='en' class=''>\n");
        _sb.append("<head>\n");
        _sb.append("<script src='https://cdn.plot.ly/plotly-latest.min.js'></script>\n");
        if (isSingle) {
            _sb.append("<style type='text/css'>\n");
            _sb.append("html { height: 100%; }\n");
            _sb.append("body { height: 95%; }\n");
            _sb.append("</style>\n\n");
        }
        _sb.append("</head>\n\n");
    }

    /**
     * Writes the HTML body.
     */
    private void writeHtmlBody(List<Chart> theCharts)
    {
        String style = "min-width:310px;max-width:640px;height:480px;margin-top:30px;margin-left:30px;box-shadow:1px 1px 6px grey;";
        _sb.append("<body>\n");

        // If only one chart, make it grow
        if (theCharts.size() == 1) {
            style = "width:75%;height:60%;margin-top:30px;margin-left:30px;box-shadow:1px 1px 6px grey;";
        }

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

        // Get Trace info
        TraceList traceList = aChart.getTraceList();
        Trace[] traces = traceList.getTraces();
        int traceCount = traces.length;

        // Iterate over Traces and write trace declaration for each: var trace0 = [ ... ]; var trace1 = [ ... ]; ...
        for (int i = 0; i < traceCount; i++) {
            Trace trace = traces[i];
            writeTrace(trace, i);
        }

        // Write Data declaration: var data [ trace0, trace1, ... ];
        _sb.append("var data = [");
        for (int i=0; i<traceCount; i++)
            _sb.append(i>0 ? ", " : " ").append("trace").append(i);
        _sb.append(" ];\n\n");

        // Write Chart layout declaration
        writeChartLayout(aChart);

        // Add 'Edit chart' link
        _sb.append("var config = {\n");
        _sb.append("  showLink: true,\n");
        _sb.append("  scrollZoom: true,\n");
        _sb.append("  responsive: true,\n");
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
        AxisType[] axisTypes = aChart.getTraceList().getAxisTypes();
        for (AxisType axisType : axisTypes)
            writeChartAxisLayout(aChart, axisType, layoutJS);

        // I don't like this
        if (aChart.getType() == ChartType.CONTOUR_3D)
            writeChartAxisLayout(aChart, AxisType.Z, layoutJS);

        // Write the legend layout
        writeChartLegendLayout(aChart, layoutJS);

        // Write the chart Scene (3D) layout
        if (aChart.getType().is3D())
            writeChartSceneLayout(aChart, layoutJS);

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

        // If explicit axis range is set, configure axis.range
        JSONNode axisRangeJS = getAxisRange(axis);
        if (axisRangeJS != null)
            axisJS.addKeyValue("range", axisRangeJS);

        // Otherwise, if ZeroRequired add rangemode = 'tozero'
        else if (axis.isZeroRequired())
            axisJS.addKeyValue("rangemode", "tozero");

        // If X Axis, see if we need to add domain entry so additional axes don't overlap data area
        if (anAxisType == AxisType.X) {
            JSONNode domain = getXAxisDomain(aChart);
            if (domain != null)
                axisJS.addKeyValue("domain", domain);
        }

        // Set Y on particular side
        if (anAxisType.isAnyY() && anAxisType != AxisType.Y) {
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

        // Add exponentformat
        TextFormat textFormat = axis.getTextFormat();
        if (textFormat instanceof NumberFormat) {
            NumberFormat numberFormat = (NumberFormat) textFormat;
            if (numberFormat.getExpStyle() == NumberFormat.ExpStyle.Scientific)
                axisJS.addKeyValue("exponentformat", "power");
        }

        // Add log support
        if (axis.isLog())
            axisJS.addKeyValue("type", "log");

        // Add tickangle support
        if (!axis.isTickLabelAutoRotate()) {
            double angle = axis.getTickLabelRotation();
            axisJS.addKeyValue("tickangle", -angle);
        }

        // Add TickFont family, size
        JSONNode tickfontJS = new JSONNode();
        tickfontJS.addKeyValue("family", axis.getFont().getFamily());
        tickfontJS.addKeyValue("size", axis.getFont().getSize());
        axisJS.addKeyValue("tickfont", tickfontJS);

        // Add Axis json node to layout node (if needed)
        if (axisJS.getNodeCount() > 0)
            layoutJS.addKeyValue(axisName, axisJS);
    }

    /**
     * Writes a Trace, e.g.: var trace1 = { x: [1, 2, 3, 4], y: [0, 2, 3, 5], type: 'scatter' };
     */
    private void writeTrace(Trace aTrace, int anIndex)
    {
        // Create new TraceJSON
        JSONNode traceJS = new JSONNode();

        // Set the trace name
        traceJS.addKeyValue("name", aTrace.getName());

        // Add: type : 'scatter'
        ChartType chartType = _chart.getType();
        switch (chartType) {
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
            case CONTOUR_3D:
                traceJS.addKeyValue("type", "mesh3d"); break;
            default:
                traceJS.addKeyValue("type", "scatter");
        }

        // Handle Stacked
        if (aTrace.isStacked())
            traceJS.addKeyValue("stackgroup", "one");

        // If ShowArea, add: fill: 'tozeroy'
        if (aTrace.isShowArea() && !aTrace.isStacked())
            traceJS.addKeyValue("fill", "tozeroy");

        // If ChartType.SCATTER, add: mode: 'markers'
        if (chartType == ChartType.SCATTER || chartType == ChartType.POLAR) {

            // Set mode: lines | markers | lines+markers
            boolean isShowLine = aTrace.isShowLine();
            boolean isShowPoints = aTrace.isShowPoints();
            String modeStr = isShowLine && isShowPoints ? "lines+markers" :
                    isShowLine ? "lines" : isShowPoints ? "markers" : "";
            traceJS.addKeyValue("mode", modeStr);

            // Configure scatter plot line node
            if (isShowLine) {

                // Create line node and add to trace
                JSONNode lineJS = new JSONNode("line", null);
                traceJS.addKeyValue("line", lineJS);

                // Set the line.color
                Color color = aTrace.getLineColor();
                String colorStr = getPlotlyColorString(color);
                lineJS.addKeyValue("color", colorStr);

                // Set the line.width
                lineJS.addKeyValue("width", aTrace.getLineWidth());
            }
        }

        // Get Trace DataSet info
        DataSet dataSet = aTrace.getProcessedData();
        int pointCount = dataSet.getPointCount();
        DataType dataType = dataSet.getDataType();
        int chanCount = dataType.getChannelCount();

        // If ChartType.CONTOUR, add: colorscale: 'Jet'
        if (chartType == ChartType.CONTOUR) {

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
            contourJS.addKeyValue("start", dataSet.getMinZ());
            contourJS.addKeyValue("end", dataSet.getMaxZ());
            contourJS.addKeyValue("size", (dataSet.getMaxZ() - dataSet.getMinZ()) / 16);
            traceJS.addKeyValue("contours", contourJS);
        }

        // If ChartType.CONTOUR_3D, configure
        if (chartType == ChartType.CONTOUR_3D) {

            // colorscale
            traceJS.addKeyValue("colorscale", "Jet");

            // Add:
            traceJS.addKeyValue("autocontour", false);
            traceJS.addKeyValue("ncontours", "16");

            JSONNode contourJS = new JSONNode("contours", null);
            contourJS.addKeyValue("start", dataSet.getMinZ());
            contourJS.addKeyValue("end", dataSet.getMaxZ());
            contourJS.addKeyValue("size", (dataSet.getMaxZ() - dataSet.getMinZ()) / 16);
            traceJS.addKeyValue("contours", contourJS);

            // Set intensity to Z values
            JSONNode intensityJS = new JSONNode();
            for (int i = 0; i < pointCount; i++) {
                Object val = dataSet.getZ(i);
                intensityJS.addValue(val);
            }
            traceJS.addKeyValue("intensity", intensityJS);
        }

        // Iterate over channels and add channel values for each
        for (int i = 0; i < chanCount; i++) {

            // Get DataChan, DataChanString
            DataChan dataChan = dataType.getChannel(i);
            String dataChanStr = dataChan.toString().toLowerCase();

            // If ChartType is Polar, remap dataChan string
            if (chartType == ChartType.POLAR) {
                if (dataChan == DataChan.T || dataChan == DataChan.X)
                    dataChanStr = "theta";
                else if (dataChan == DataChan.R || dataChan == DataChan.Y)
                    dataChanStr = "r";
                else System.err.println("OpenInPlotly.writeTrace: Unknown Polar DataChan: " + dataChan);
            }

            // Get Channel, create valsJS and add to traceJS
            JSONNode valsJS = new JSONNode();
            traceJS.addKeyValue(dataChanStr, valsJS);

            // Iterate over values and add to valsJS
            for (int j = 0; j < pointCount; j++) {
                Object val = dataSet.getValueForChannel(dataChan, j);
                valsJS.addValue(val);
            }
        }

        // If ChartType.LINE_3D, add: fill: 'tozeroy'
        if (chartType == ChartType.LINE_3D) {
            JSONNode valsJS = new JSONNode();
            traceJS.addKeyValue("z", valsJS);
            for (int j = 0; j < pointCount; j++)
                valsJS.addValue(anIndex);
            traceJS.addKeyValue("fill", "tozeroy");
        }

        // If Y axis is Y1/Y2/Y3, swap that in
        AxisType axisTypeY = aTrace.getAxisTypeY();
        if (axisTypeY != AxisType.Y)
            traceJS.addKeyValue("yaxis", axisTypeY.toString().toLowerCase());

        // Write trace
        _sb.append("var trace").append(anIndex).append(" = ");
        String traceString = traceJS.toString();
        _sb.append(traceString);
        _sb.append(";\n\n");
    }

    /**
     * Writes chart legend layout declaration.
     */
    private void writeChartLegendLayout(Chart aChart, JSONNode layoutJS)
    {
        Legend legend = aChart.getLegend();
        if (!legend.isShowLegend()) return;
        JSONNode legendJS = new JSONNode();

        Pos legendPos = legend.getPosition();
        if (legendPos == Pos.TOP_CENTER) {
            legendJS.addKeyValue("orientation", "h");
            legendJS.addKeyValue("yanchor", "bottom");
            legendJS.addKeyValue("y", 1.008);
        }
        else if (legendPos == Pos.BOTTOM_CENTER) {
            legendJS.addKeyValue("orientation", "h");
            legendJS.addKeyValue("y", -.2);
        }

        // Add to layout node
        if (legendJS.getNodeCount() > 0)
            layoutJS.addKeyValue("legend", legendJS);
    }

    /**
     * Writes the chart Scene (3D) layout.
     */
    private void writeChartSceneLayout(Chart aChart, JSONNode layoutJS)
    {
        // Create Scene JSON
        JSONNode sceneJS = new JSONNode();

        // Get AxisTypes
        AxisType[] axisTypes = aChart.getTraceList().getAxisTypes();
        if (!ArrayUtils.contains(axisTypes, AxisType.Z))
            axisTypes = ArrayUtils.add(axisTypes, AxisType.Z);

        // Iterate over axisTypes and add to Scene JSON
        for (AxisType axisType : axisTypes) {

            String key = getAxisName(axisType);
            JSONNode axisJS = layoutJS.getNode(key);
            if (axisJS != null) {
                layoutJS.removeNode(axisJS);
                sceneJS.addKeyValue(key, axisJS);
            }
        }

        // Add to layout node
        if (sceneJS.getNodeCount() > 0)
            layoutJS.addKeyValue("scene", sceneJS);
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
     * Returns the range for an axis if explicitly set (null, otherwise).
     */
    private JSONNode getAxisRange(Axis anAxis)
    {
        if (anAxis.getMinBound() != AxisBound.VALUE || anAxis.getMaxBound() != AxisBound.VALUE)
            return null;
        double axisMin = anAxis.getMinValue();
        double axisMax = anAxis.getMaxValue();
        JSONNode node = new JSONNode();
        node.addValue(axisMin);
        node.addValue(axisMax);
        return node;
    }

    /**
     * Returns the suggested area range for a chart (default 0 - 1), which might need to be pulled in if additional axes are present.
     *
     * It's rather lame that Plotly doesn't handle this kind of layout automatically.
     */
    private JSONNode getXAxisDomain(Chart aChart)
    {
        AxisType[] axisTypes = aChart.getTraceList().getAxisTypes();
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
        AxisType[] axisTypes = aChart.getTraceList().getAxisTypes();
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
     * Returns a Plotly color string for given color.
     */
    private String getPlotlyColorString(Color aColor)
    {
        boolean hasAlpha = aColor.getAlpha() < 1;
        String prefix = hasAlpha ? "rgba(" : "rgb(";
        StringBuffer sb = new StringBuffer(prefix);
        sb.append(aColor.getRedInt()).append(',');
        sb.append(aColor.getGreenInt()).append(',');
        sb.append(aColor.getBlueInt());
        if (hasAlpha)
            sb.append(aColor.getAlpha());
        sb.append(')');
        return sb.toString();
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
