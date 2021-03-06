package snapcharts.appmisc;
import snap.gfx.GFXEnv;
import snap.util.FileUtils;
import snap.util.SnapUtils;
import snap.view.*;
import snapcharts.app.App;
import snapcharts.model.Chart;
import snapcharts.model.DataSet;
import snapcharts.model.DataType;
import snapcharts.doc.Doc;
import java.io.File;

/**
 * A class to allow a desktop app to send a chart to SnapCharts.
 */
public class ChartClient extends ViewOwner {

    // The SnapChartURL
    private final int HTTP_PORT_LOCAL = 8008;
    private final String SNAPCHARTS_URL = "http://reportmill.com/snaptea/SnapCharts";
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
     * Open Chart in browser.
     */
    public void openChartDocInBrowser(Doc aChartDoc, String aFileName, boolean doLocal)
    {
        // Get bytes for file
        byte bytes[] = aChartDoc.getChartsFileXMLBytes();
        File file = FileUtils.getTempFile(aFileName);
        SnapUtils.writeBytes(bytes, file);
        SnapUtils.writeBytes(bytes, "/tmp/test.charts");

        // Get URL
        String snapChartsURL = doLocal ? SNAPCHARTS_URL_LOCAL : SNAPCHARTS_URL;
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
        dataSet.setDataType(DataType.XY);

        // Add some data
        for (double x=0; x<2*Math.PI; x+=Math.PI/100)
            dataSet.addPointXYZC(x, Math.sin(x), null, null);
        chart.addDataSet(dataSet);

        // Open Doc
        String filename = "NewChart." + Doc.CHARTS_FILE_EXTENSION;
        openChartDocInBrowser(doc, filename, false);
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
     * Standard main.
     */
    public static void main(String args[])
    {
        ChartClient chartClient = new ChartClient();
        chartClient.setWindowVisible(true);
    }
}
