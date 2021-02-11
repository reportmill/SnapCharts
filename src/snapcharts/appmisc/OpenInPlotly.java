package snapcharts.appmisc;
import snap.util.FileUtils;
import snap.util.JSONNode;
import snap.util.SnapUtils;
import snapcharts.model.*;
import java.util.List;

/**
 * Opens a SnapChart in Plotly.
 */
public class OpenInPlotly {

    // The string builder
    StringBuilder  _sb = new StringBuilder();

    /**
     * Opens chart in plotly.
     */
    public void openInPlotly(Chart aChart)
    {
        // Generate HTML
        String htmlStr = getHtmlFileString(aChart);

        //File file = FileUtils.getTempFile("Plotly.html");
        //File file = new File("/tmp/Plotly.html");
        //try { FileUtils.writeBytes(file, htmlStr.getBytes()); }
        //catch (Exception e) { throw new RuntimeException(e); }
        //GFXEnv.getEnv().openURL(file);

        // Simple open file (I know this works in TeaVM)
        String file = SnapUtils.getTempDir() + "Plotly.html";
        SnapUtils.writeBytes(htmlStr.getBytes(), file);
        FileUtils.openFile(file);
    }

    /**
     * Returns the html file string.
     */
    public String getHtmlFileString(Chart aChart)
    {
        writeHtmlHeader();
        writeHtmlBody();
        writeChart(aChart, 0);
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
    private void writeHtmlBody()
    {
        String style = "min-width:310px;max-width:640px;height:480px;margin-left:18px;box-shadow:1px 1px 6px grey;";
        _sb.append("<body>\n");
        _sb.append("<div id='chartDiv0' style='").append(style).append("'>\n");
        _sb.append("<!-- Plotly chart will be drawn inside this DIV -->\n");
        _sb.append("</body>\n");
    }

    /**
     * Writes a chart.
     */
    private void writeChart(Chart aChart, int anIndex)
    {
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

        // Write Plotly invocation: Plotly.newPlot('myDiv', data);
        _sb.append("Plotly.newPlot('chartDiv").append(anIndex).append("', data, layout);\n\n");

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

        // Get title/subtitle and add to layoutJS
        String title = aChart.getHeader().getTitle();
        String subtitle = aChart.getHeader().getSubtitle();
        if (subtitle!=null && subtitle.length()>0)
            title = title + "<br>" + subtitle;
        layoutJS.addKeyValue("title", title);

        // Write layout
        _sb.append("var layout").append(" = ");
        String layoutStr = layoutJS.toString();
        _sb.append(layoutStr);
        _sb.append(";\n\n");
    }

    /**
     * Writes a DataSet.
     */
    private void writeDataSet(DataSet aDataSet, int anIndex)
    {
        int pointCount = aDataSet.getPointCount();
        DataType dataType = aDataSet.getDataType();
        int chanCount = dataType.getChannelCount();

        // Create new TraceJSON and set type to 'scatter'
        JSONNode traceJS = new JSONNode();
        traceJS.addKeyValue("type", "scatter");

        // Iterate over channels and add channel values for each
        for (int i=0; i<chanCount; i++) {

            // Get Channel, create valsJS and add to traceJS
            DataChan dataChan = dataType.getChannel(i);
            JSONNode valsJS = new JSONNode();
            traceJS.addKeyValue(dataChan.toString().toLowerCase(), valsJS);

            // Iterate over values and add to valsJS
            for (int j=0; j<pointCount; j++)
                valsJS.addValue(aDataSet.getValueForChannel(dataChan, j));
        }

        // Write trace
        _sb.append("var trace").append(anIndex).append(" = ");
        String traceString = traceJS.toString();
        _sb.append(traceString);
        _sb.append(";\n\n");
    }
}
