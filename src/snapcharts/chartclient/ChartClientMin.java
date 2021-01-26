package snapcharts.chartclient;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * A class to allow a desktop app to send a chart to SnapCharts.
 */
public class ChartClientMin {

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
    private String _args = "-classpath " + _classpath + " snapcharts.app.App " + "fetch"; //App.APP_ARG_FETCH_CHART;
    private String _cmd = _java + ' ' + _args;

    // The SimpleServer
    static SimpleServerMin _server = new SimpleServerMin();

    // The Chart buffer
    StringBuffer _chartBuffer = new StringBuffer();

    /**
     * Constructor.
     */
    public ChartClientMin()
    {
    }

    /**
     * Open Chart.
     */
    public void openChartDoc(String aFileName)
    {
        // Get bytes for file
        String chartStr = _chartBuffer.toString();
        byte bytes[] = chartStr.getBytes();
        //File file = FileUtils.getTempFile(aFileName);
        //SnapUtils.writeBytes(bytes, file);
        //SnapUtils.writeBytes(bytes, "/tmp/test.simple");

        // Get URL
        boolean isLocal = true;
        String snapChartsURL = isLocal ? SNAPCHARTS_URL_LOCAL : SNAPCHARTS_URL;
        String urls = snapChartsURL + "?fetch=" + aFileName;

        // Configure and start simple server
        _server._port = HTTP_PORT_LOCAL;
        _server.setBytes(bytes); //file.getParentFile());
        _server.startServer();

        // Open SnapCharts
        try {
            String cmd = _cmd + ' ' + urls;
            //Runtime.getRuntime().exec(cmd);
            //App.main(new String[] { App.APP_ARG_FETCH_CHART, urls});
            try { Desktop.getDesktop().browse(new URI(urls)); }
            catch(Throwable e) { System.err.println(e.getMessage()); }
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
        _chartBuffer.setLength(0);
        _chartBuffer.append("Doc.Name=Sample Chart Doc\n");
        _chartBuffer.append("Chart.Name=New Chart\n");
        _chartBuffer.append("Chart.Title=New Chart\n");
        _chartBuffer.append("Chart.AxisX.Title=X Axis\n");
        _chartBuffer.append("Chart.AxisY.Title=Y Axis\n");
        _chartBuffer.append("DataSet.Name=Sample Data\n");

        // Add some data
        double dataX[] = new double[1000];
        double dataY[] = new double[1000];
        int len = 0;
        for (double x=0; x<2*Math.PI; x+=Math.PI/100) {
            dataX[len] = x;
            dataY[len++] = Math.cos(x);
        }
        dataX = Arrays.copyOf(dataX, len);
        dataY = Arrays.copyOf(dataY, len);
        String dataXStr = getStringForDoubleArray(dataX);
        String dataYStr = getStringForDoubleArray(dataY);
        _chartBuffer.append("DataSet.DataX=").append(dataXStr).append('\n');
        _chartBuffer.append("DataSet.DataY=").append(dataYStr).append('\n');

        // Open Doc
        openChartDoc("NewChart.simple");
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
     * HTTPServerPane provides UI for managing an HTTP-Server for this project.
     */
    public static class SimpleServerMin {

        // The bytes to return
        byte         _bytes[];

        // The HTTPServer
        HttpServer _server;

        // The port
        int            _port = 8008;

        // Cache-control: max-age=20
        String         _cacheControl = "max-age=20";

        // Whether server is running
        boolean        _running;

        // The last response code
        int            _respCode;

        // DateFormat for GMT time
        DateFormat _fmt;

        // Constants for response codes (http://en.wikipedia.org/wiki/List_of_HTTP_status_codes)
        public static final int WEB_RESPONSE_OK = 200;

        /**
         * Creates a new HTTPServerPane for SitePane.
         */
        public SimpleServerMin()
        {
            super();
        }

        /**
         * Sets the bytes
         */
        public void setBytes(byte theBytes[])
        {
            _bytes = theBytes;
        }

        /**
         * Returns the server.
         */
        public HttpServer getServer()
        {
            if(_server!=null) return _server;
            try { _server = createServer(); }
            catch(Exception e) { throw new RuntimeException(e); }
            return _server;
        }

        /**
         * Creates the server.
         */
        protected HttpServer createServer() throws IOException
        {
            HttpServer server = HttpServer.create(new InetSocketAddress(_port), 0);
            server.createContext("/", new SimpleHttpHandler());
            return server;
        }

        /**
         * Returns whether server is running.
         */
        public boolean isRunning()  { return _server!=null && _running; }

        /**
         * Starts the server.
         */
        public void startServer()
        {
            if (_running) return;
            try { getServer().start(); }
            catch(Exception e) { throw new RuntimeException(e); }
            _running = true;
            append("Started Server\n");
        }

        /**
         * Stops the server.
         */
        public void stopServer()
        {
            if (!_running) return;
            getServer().stop(0);
            _server = null; _running = false;
        }

        /**
         * Prints exchange to server.
         */
        void printExchange(HttpExchange anExch)
        {
            // Append Date
            append("["); append(new Date().toString()); append("] ");

            // Append method and path
            boolean isErr = _respCode!=WEB_RESPONSE_OK;
            String meth = anExch.getRequestMethod();
            String path = anExch.getRequestURI().getPath();
            append("\"");
            append(meth, isErr);
            append(" ", isErr);
            append(path, isErr); append("\" ");

            // If error print error
            if (_respCode!=WEB_RESPONSE_OK) {
                append("Error (");
                append(String.valueOf(_respCode), isErr); append(")");
            }

            // Otherwise append User-Agent
            else {
                Headers hdrs = anExch.getRequestHeaders();
                List<String> userAgents = hdrs.get("User-agent");
                if (userAgents!=null) {
                    String str = Arrays.toString(userAgents.toArray());
                    String str2 = '\"' + str + '\"';
                    append(str2);
                }
            }

            append("]\n");
        }

        /**
         * Appends to the text view.
         */
        void append(String aStr)  { append(aStr, false); }

        /**
         * Appends to the text view.
         */
        void append(String aStr, boolean isErr)
        {
            if (isErr)
                System.out.print(aStr);
            else System.out.print(aStr);
        }

        /**
         * Returns a GMT date string.
         */
        private String getGMT(Date aDate)
        {
            if (_fmt==null) {
                _fmt = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
                _fmt.setTimeZone(TimeZone.getTimeZone("GMT"));
            }

            return _fmt.format(aDate);
        }

        /**
         * A simple HttpHandler.
         */
        private class SimpleHttpHandler implements HttpHandler {

            /** Handle. */
            public void handle(HttpExchange anExch) throws IOException
            {
                // Get method
                String meth = anExch.getRequestMethod();
                //String path = anExch.getRequestURI().getPath();

                // Add ResponseHeaders: Server, Keep-alive
                Headers hdrs = anExch.getResponseHeaders();
                hdrs.add("server", "SnapCode 1.0");
                hdrs.add("Connection", "keep-alive");
                hdrs.add("Access-Control-Allow-Origin", "*");

                // Handle method
                if (meth.equals("HEAD"))
                    handleHead(anExch);
                else if (meth.equals("GET"))
                    handleGet(anExch);

                printExchange(anExch);
            }

            /** Handle HEAD. */
            public void handleHead(HttpExchange anExch) throws IOException
            {
                // Get path and URL
                String path = anExch.getRequestURI().getPath();

                // Get length and LastModified
                long len = _bytes.length;
                Date lastMod = new Date();
                //String ext = FilePathUtils.getExtension(url.getPath());

                // Add ResponseHeaders: last-modified, cache-control, content-length, content-type
                Headers hdrs = anExch.getResponseHeaders();
                hdrs.add("last-modified", getGMT(lastMod));
                hdrs.add("cache-control", _cacheControl);
                hdrs.add("content-length", String.valueOf(len));
                //String mtype = MIMEType.getType(ext);
                //if (mtype!=null) hdrs.add("content-type", mtype);

                // Get bytes and append
                anExch.sendResponseHeaders(WEB_RESPONSE_OK,-1);
            }

            /** Handle GET. */
            public void handleGet(HttpExchange anExch) throws IOException
            {
                // Get path and URL
                String path = anExch.getRequestURI().getPath();
                //WebURL url = getURL(path);
                //WebResponse resp = url.getResponse();

                // If response not OK, return error code
                //_respCode = resp.getCode();
                //if (resp.getCode()!=WebResponse.OK) { anExch.sendResponseHeaders(resp.getCode(),-1); return; }

                // Get length and LastModified
                //FileHeader fhdr = resp.getFileHeader();
                long len = _bytes.length; //fhdr.getSize();
                Date lastMod = new Date(); //(fhdr.getModTime());
                //String ext = FilePathUtils.getExtension(url.getPath());
                byte bytes[] = _bytes; //resp.getBytes();

                // Add ResponseHeaders: last-modified, content-length, content-type
                Headers hdrs = anExch.getResponseHeaders();
                hdrs.add("last-modified", getGMT(lastMod));
                hdrs.add("cache-control", _cacheControl);
                hdrs.add("content-length", String.valueOf(len));
                //String mtype = MIMEType.getType(ext);
                //if (mtype!=null) hdrs.add("content-type", mtype);

                // Append bytes
                anExch.sendResponseHeaders(200,bytes.length);
                OutputStream os = anExch.getResponseBody();
                os.write(bytes);
                os.close();
            }
        }
    }
}
