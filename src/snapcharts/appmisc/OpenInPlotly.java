package snapcharts.appmisc;
import snap.geom.Pos;
import snap.geom.Side;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.text.NumberFormat;
import snap.text.TextFormat;
import snap.util.*;
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
        // Write script open
        _sb.append("<script>\n");

        // Get Trace info
        Content content = aChart.getContent();
        Trace[] traces = content.getTraces();
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
        JSObject layoutJS = new JSObject();

        // Create title JSON node
        JSObject titleJS = new JSObject();
        layoutJS.setValue("title", titleJS);

        // Get title/subtitle and add to layoutJS
        String title = aChart.getHeader().getTitle();
        String subtitle = aChart.getHeader().getSubtitle();
        if (subtitle!=null && subtitle.length()>0)
            title = title + "<br>" + subtitle;
        titleJS.setNativeValue("text", title);

        // Write the axis layout
        AxisType[] axisTypes = aChart.getContent().getAxisTypes();
        for (AxisType axisType : axisTypes)
            writeChartAxisLayout(aChart, axisType, layoutJS);

        // I don't like this
        TraceType traceType = aChart.getTraceType();
        if (traceType == TraceType.Contour3D)
            writeChartAxisLayout(aChart, AxisType.Z, layoutJS);

        // Write the legend layout
        writeChartLegendLayout(aChart, layoutJS);

        // Write the chart Scene (3D) layout
        if (traceType.is3D())
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
    private void writeChartAxisLayout(Chart aChart, AxisType anAxisType, JSObject layoutJS)
    {
        // Get axis and axis name
        Axis axis = aChart.getAxisForType(anAxisType);
        String axisName = getAxisName(anAxisType);

        // Create axis JSONNode
        JSObject axisJS = new JSObject();

        // Set title
        String title = axis.getTitle();
        if (title !=null && title.length() > 0) {
            JSObject titleJS = new JSObject();
            titleJS.setNativeValue("text", title);
            axisJS.setValue("title", titleJS);
        }

        // Add showline, ticks, mirror (whether axis should showline on opposite side)
        axisJS.setNativeValue("showline", true);
        axisJS.setNativeValue("ticks", "inside");
        axisJS.setNativeValue("mirror", true);

        // If explicit axis range is set, configure axis.range
        JSValue axisRangeJS = getAxisRange(axis);
        if (axisRangeJS != null)
            axisJS.setValue("range", axisRangeJS);

        // Otherwise, if ZeroRequired add rangemode = 'tozero'
        else if (axis.isZeroRequired())
            axisJS.setNativeValue("rangemode", "tozero");

        // If X Axis, see if we need to add domain entry so additional axes don't overlap data area
        if (anAxisType == AxisType.X) {
            JSValue domain = getXAxisDomain(aChart);
            if (domain != null)
                axisJS.setValue("domain", domain);
        }

        // Set Y on particular side
        if (anAxisType.isAnyY() && anAxisType != AxisType.Y) {
            axisJS.setNativeValue("side", axis.getSide().toString().toLowerCase());
            axisJS.setNativeValue("overlaying", "y");
            if (anAxisType == AxisType.Y2) {
                axisJS.setNativeValue("anchor", "x");
            }
            if (anAxisType == AxisType.Y3) {
                double position = getYAxisPosition(aChart, anAxisType);
                if (position > 0 && position < 1) {
                    axisJS.setNativeValue("anchor", "free");
                    axisJS.setNativeValue("position", .15);
                }
                else axisJS.setNativeValue("anchor", "x");
            }
            if (anAxisType == AxisType.Y4) {
                double position = getYAxisPosition(aChart, anAxisType);
                if (position > 0 && position < 1) {
                    axisJS.setNativeValue("anchor", "free");
                    axisJS.setNativeValue("position", .85);
                }
                else axisJS.setNativeValue("anchor", "x");
            }
        }

        // Add exponentformat
        TextFormat textFormat = axis.getTextFormat();
        if (textFormat instanceof NumberFormat) {
            NumberFormat numberFormat = (NumberFormat) textFormat;
            if (numberFormat.getExpStyle() == NumberFormat.ExpStyle.Scientific)
                axisJS.setNativeValue("exponentformat", "power");
        }

        // Add log support
        if (axis.isLog())
            axisJS.setNativeValue("type", "log");

        // Add tickangle support
        if (!axis.isTickLabelAutoRotate()) {
            double angle = axis.getTickLabelRotation();
            axisJS.setNativeValue("tickangle", -angle);
        }

        // Add TickFont family, size
        JSObject tickfontJS = new JSObject();
        Font axisFont = axis.getFont();
        if (axisFont == null)
            axisFont = axis.getFont();
        tickfontJS.setNativeValue("family", axisFont.getFamily());
        tickfontJS.setNativeValue("size", axisFont.getSize());
        axisJS.setValue("tickfont", tickfontJS);

        // Add Axis json node to layout node (if needed)
        if (axisJS.getValueCount() > 0)
            layoutJS.setValue(axisName, axisJS);
    }

    /**
     * Writes a Trace, e.g.: var trace1 = { x: [1, 2, 3, 4], y: [0, 2, 3, 5], type: 'scatter' };
     */
    private void writeTrace(Trace aTrace, int anIndex)
    {
        // Create new TraceJSON
        JSObject traceJS = new JSObject();

        // Set the trace name
        traceJS.setNativeValue("name", aTrace.getName());

        // Add: type : 'scatter'
        TraceType traceType = aTrace.getType();
        if (traceType == TraceType.Bar || traceType == TraceType.Bar3D)
            traceJS.setNativeValue("type", "bar");
        else if (traceType == TraceType.Contour)
            traceJS.setNativeValue("type", "contour");
        else if (traceType == TraceType.Polar)
                traceJS.setNativeValue("type", "scatterpolar");
        else if (traceType == TraceType.Line3D)
                traceJS.setNativeValue("type", "scatter3d");
        else if (traceType == TraceType.Contour3D)
                traceJS.setNativeValue("type", "mesh3d");
        else traceJS.setNativeValue("type", "scatter");
        //case PIE: traceJS.addKeyValue("type", "pie"); break;

        // Handle Stacked
        if (aTrace.isStacked())
            traceJS.setNativeValue("stackgroup", "one");

        // If ShowArea, add: fill: 'tozeroy'
        if (aTrace.isShowArea() && !aTrace.isStacked())
            traceJS.setNativeValue("fill", "tozeroy");

        // If TraceType.Scatter, add: mode: 'markers'
        if (traceType == TraceType.Scatter || traceType == TraceType.Polar) {

            // Set mode: lines | markers | lines+markers
            boolean isShowLine = aTrace.isShowLine();
            boolean isShowPoints = aTrace.isShowPoints();
            String modeStr = isShowLine && isShowPoints ? "lines+markers" :
                    isShowLine ? "lines" : isShowPoints ? "markers" : "";
            traceJS.setNativeValue("mode", modeStr);

            // Configure scatter plot line node
            if (isShowLine) {

                // Create line node and add to trace
                JSObject lineJS = new JSObject();
                traceJS.setValue("line", lineJS);

                // Set the line.color
                Color color = aTrace.getLineColor();
                String colorStr = getPlotlyColorString(color);
                lineJS.setNativeValue("color", colorStr);

                // Set the line.width
                lineJS.setNativeValue("width", aTrace.getLineWidth());
            }
        }

        // Get Trace DataSet info
        DataSet dataSet = aTrace.getProcessedData();
        int pointCount = dataSet.getPointCount();
        DataType dataType = dataSet.getDataType();
        int chanCount = dataType.getChannelCount();

        // If TraceType.CONTOUR, add: colorscale: 'Jet'
        if (traceType == TraceType.Contour) {

            // colorscale
            traceJS.setNativeValue("colorscale", "Jet");

            // line { smoothing : 0 }
            JSObject lineJS = new JSObject();
            lineJS.setNativeValue("smoothing", "0");
            traceJS.setValue("line", lineJS);

            // Add:
            traceJS.setNativeValue("autocontour", false);
            traceJS.setNativeValue("ncontours", "16");

            JSObject contourJS = new JSObject();
            contourJS.setNativeValue("start", dataSet.getMinZ());
            contourJS.setNativeValue("end", dataSet.getMaxZ());
            contourJS.setNativeValue("size", (dataSet.getMaxZ() - dataSet.getMinZ()) / 16);
            traceJS.setValue("contours", contourJS);
        }

        // If TraceType.CONTOUR_3D, configure
        if (traceType == TraceType.Contour3D) {

            // colorscale
            traceJS.setNativeValue("colorscale", "Jet");

            // Add:
            traceJS.setNativeValue("autocontour", false);
            traceJS.setNativeValue("ncontours", "16");

            JSObject contourJS = new JSObject();
            contourJS.setNativeValue("start", dataSet.getMinZ());
            contourJS.setNativeValue("end", dataSet.getMaxZ());
            contourJS.setNativeValue("size", (dataSet.getMaxZ() - dataSet.getMinZ()) / 16);
            traceJS.setValue("contours", contourJS);

            // Set intensity to Z values
            JSArray intensityJS = new JSArray();
            for (int i = 0; i < pointCount; i++) {
                Object val = dataSet.getZ(i);
                intensityJS.addNativeValue(val);
            }
            traceJS.setValue("intensity", intensityJS);
        }

        // Iterate over channels and add channel values for each
        for (int i = 0; i < chanCount; i++) {

            // Get DataChan, DataChanString
            DataChan dataChan = dataType.getChannel(i);
            String dataChanStr = dataChan.toString().toLowerCase();

            // If TraceType is Polar, remap dataChan string
            if (traceType == TraceType.Polar) {
                if (dataChan == DataChan.T || dataChan == DataChan.X)
                    dataChanStr = "theta";
                else if (dataChan == DataChan.R || dataChan == DataChan.Y)
                    dataChanStr = "r";
                else System.err.println("OpenInPlotly.writeTrace: Unknown Polar DataChan: " + dataChan);
            }

            // Get Channel, create valsJS and add to traceJS
            JSArray valsJS = new JSArray();
            traceJS.setValue(dataChanStr, valsJS);

            // Iterate over values and add to valsJS
            for (int j = 0; j < pointCount; j++) {
                Object val = dataSet.getValueForChannel(dataChan, j);
                valsJS.addNativeValue(val);
            }
        }

        // If TraceType.LINE_3D, add: fill: 'tozeroy'
        if (traceType == TraceType.Line3D) {
            JSArray valsJS = new JSArray();
            traceJS.setValue("z", valsJS);
            for (int j = 0; j < pointCount; j++)
                valsJS.addNativeValue(anIndex);
            traceJS.setNativeValue("fill", "tozeroy");
        }

        // If Y axis is Y1/Y2/Y3, swap that in
        AxisType axisTypeY = aTrace.getAxisTypeY();
        if (axisTypeY != AxisType.Y)
            traceJS.setNativeValue("yaxis", axisTypeY.toString().toLowerCase());

        // Write trace
        _sb.append("var trace").append(anIndex).append(" = ");
        String traceString = traceJS.toString();
        _sb.append(traceString);
        _sb.append(";\n\n");
    }

    /**
     * Writes chart legend layout declaration.
     */
    private void writeChartLegendLayout(Chart aChart, JSObject layoutJS)
    {
        Legend legend = aChart.getLegend();
        if (!legend.isShowLegend()) return;
        JSObject legendJS = new JSObject();

        Pos legendPos = legend.getPosition();
        if (legendPos == Pos.TOP_CENTER) {
            legendJS.setNativeValue("orientation", "h");
            legendJS.setNativeValue("yanchor", "bottom");
            legendJS.setNativeValue("y", 1.008);
        }
        else if (legendPos == Pos.BOTTOM_CENTER) {
            legendJS.setNativeValue("orientation", "h");
            legendJS.setNativeValue("y", -.2);
        }

        // Add to layout node
        if (legendJS.getValueCount() > 0)
            layoutJS.setValue("legend", legendJS);
    }

    /**
     * Writes the chart Scene (3D) layout.
     */
    private void writeChartSceneLayout(Chart aChart, JSObject layoutJS)
    {
        // Create Scene JSON
        JSObject sceneJS = new JSObject();

        // Get AxisTypes
        AxisType[] axisTypes = aChart.getContent().getAxisTypes();
        if (!ArrayUtils.contains(axisTypes, AxisType.Z))
            axisTypes = ArrayUtils.add(axisTypes, AxisType.Z);

        // Iterate over axisTypes and add to Scene JSON
        for (AxisType axisType : axisTypes) {

            String key = getAxisName(axisType);
            JSValue axisJS = layoutJS.getValue(key);
            if (axisJS != null) {
                layoutJS.setValue(key, null);
                sceneJS.setValue(key, axisJS);
            }
        }

        // Add to layout node
        if (sceneJS.getValueCount() > 0)
            layoutJS.setValue("scene", sceneJS);
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
    private JSArray getAxisRange(Axis anAxis)
    {
        if (anAxis.getMinBound() != AxisBound.VALUE || anAxis.getMaxBound() != AxisBound.VALUE)
            return null;
        double axisMin = anAxis.getMinValue();
        double axisMax = anAxis.getMaxValue();
        JSArray node = new JSArray();
        node.addNativeValue(axisMin);
        node.addNativeValue(axisMax);
        return node;
    }

    /**
     * Returns the suggested area range for a chart (default 0 - 1), which might need to be pulled in if additional axes are present.
     *
     * It's rather lame that Plotly doesn't handle this kind of layout automatically.
     */
    private JSArray getXAxisDomain(Chart aChart)
    {
        AxisType[] axisTypes = aChart.getContent().getAxisTypes();
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
        JSArray node = new JSArray();
        node.addNativeValue(start);
        node.addNativeValue(end);
        return node; //"[" + FormatUtils.formatNum("#.#", start) + ", " + FormatUtils.formatNum("#.#", end) + "]";
    }

    /**
     * Returns the suggested Y axis position for a chart - which is a start/end values
     */
    private double getYAxisPosition(Chart aChart, AxisType anAxisType)
    {
        AxisType[] axisTypes = aChart.getContent().getAxisTypes();
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
