package snapcharts.chartclient;
import snap.gfx.GFXEnv;
import snap.util.FileUtils;
import snap.util.SnapUtils;
import snap.view.*;
import snapcharts.app.App;
import snapcharts.model.Chart;
import snapcharts.model.DataSet;
import snapcharts.model.Doc;
import java.io.File;
import java.text.DecimalFormat;

/**
 * A class to allow a desktop app to send a chart to SnapCharts.
 */
public class ChartClient extends ViewOwner {

    // The SnapChartURL
    private final int HTTP_PORT_LOCAL = 8008;
    private final String SNAPCHARTS_URL = "https://reportmill.com/snaptea/SnapCharts";
    private final String SNAPCHARTS_URL_LOCAL = "http://localhost:8080";

    // Ivars for launching SnapCharts
    private String _java = "/Library/Java/JavaVirtualMachines/jdk1.8.0_111.jdk/Contents/Home/bin/java";
    private String _classpath = "/Users/jeff/SnapDev/SnapKit/out/production/classes:" +
        "/Users/jeff/SnapDev/SnapKit/out/production/resources:" +
        "/Users/jeff/SnapDev/SnapCharts/out/production/classes:" +
        "/Users/jeff/SnapDev/SnapCharts/out/production/resources";
    private String _args = "-classpath " + _classpath + " snapcharts.app.App " + App.APP_ARG_FETCH_CHART;
    private String _cmd = _java + ' ' + _args;

    // The SimpleServer
    SimpleServer _server = new SimpleServer();

    /**
     * Open Chart.
     */
    public void openChartDoc(String aFileName, Doc aChartDoc)
    {
        // Get bytes for file
        byte bytes[] = aChartDoc.getChartsFileXMLBytes();
        File file = FileUtils.getTempFile(aFileName);
        SnapUtils.writeBytes(bytes, file);
        SnapUtils.writeBytes(bytes, "/tmp/test.charts");

        // Get URL
        boolean isLocal = false;
        String snapChartsURL = isLocal ? SNAPCHARTS_URL_LOCAL : SNAPCHARTS_URL;
        String urls = snapChartsURL + "?fetch=" + aFileName;

        // Configure and start simple server
        _server._port = HTTP_PORT_LOCAL;
        _server.setSitePathForFile(file.getParentFile());
        _server.startServer();

        // Open SnapCharts
        try {
            String cmd = _cmd + ' ' + urls;
            //Runtime.getRuntime().exec(cmd);
            //App.main(new String[] { App.APP_ARG_FETCH_CHART, aFileName });
            GFXEnv.getEnv().openURL(urls);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a Simple Sample and open in SnapCharts.
     */
    public void openSimpleSample()
    {
        // Create doc
        Doc doc = new Doc();
        doc.setName("Sample Chart");

        // Create Chart
        Chart chart = new Chart();
        chart.setName("New Chart");
        chart.getHeader().setTitle("New Chart");
        chart.getAxisY().setTitle("Y Axis");
        chart.getAxisX().setTitle("X Axis");
        doc.addChart(chart);

        // Create DataSet
        DataSet dataSet = new DataSet();
        dataSet.setName("Sample Data");

        // Add some data
        for (double x=0; x<2*Math.PI; x+=Math.PI/100)
            dataSet.addPointXY(x, Math.sin(x));
        chart.addDataSet(dataSet);

        // Open Doc
        openChartDoc("NewChart." + Doc.CHARTS_FILE_EXTENSION, doc);
    }

    /**
     * Create UI.
     */
    @Override
    protected View createUI()
    {
        ColView mainColView = new ColView();
        mainColView.setPadding(20, 20, 20, 20);
        mainColView.setSpacing(20);

        Button button = new Button("Open Simple Sample");
        button.setName("OpenSample");
        mainColView.addChild(button);
        return mainColView;
    }

    /**
     * respondUI.
     */
    @Override
    protected void respondUI(ViewEvent anEvent)
    {
        if (anEvent.equals("OpenSample")) //openSimpleSample();
            new ChartClientMin().openSimpleSample();
    }

    /**
     * Return string for double array.
     */
    public static String getStringForDoubleArray(double theValues[])
    {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i=0, iMax=theValues.length; ; i++) {
            String str = _doubleFmt.format(theValues[i]);
            sb.append(str);
            if (i == iMax)
                return sb.append(']').toString();
            sb.append(", ");
        }
    }

    // A formatter to format double without exponent
    private static DecimalFormat _doubleFmt = new DecimalFormat("0.##############");

    /**
     * Standard main.
     */
    public static void main(String args[])
    {
        ChartClient chartClient = new ChartClient();
        chartClient.setWindowVisible(true);
    }
}
