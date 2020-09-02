package snapcharts.chartclient;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import snap.util.FilePathUtils;
import snap.web.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * HTTPServerPane provides UI for managing an HTTP-Server for this project.
 */
public class SimpleServer {

    // The WebSite path
    String         _sitePath;

    // The HTTPServer
    HttpServer     _server;

    // The port
    int            _port = 8008;

    // Cache-control: max-age=20
    String         _cacheControl = "max-age=20";

    // Whether server is running
    boolean        _running;

    // The last response code
    int            _respCode;

    // DateFormat for GMT time
    static DateFormat  _fmt;

    /**
     * Creates a new HTTPServerPane for SitePane.
     */
    public SimpleServer()
    {
        super();
    }

    /**
     * Sets the SitePath.
     */
    public void setSitePathForFile(File aFile)
    {
        _sitePath = FilePathUtils.getStandardized(aFile.getAbsolutePath());
    }

    /**
     * Returns a URL for given path.
     */
    public WebURL getURL(String aPath)
    {
        String path = FilePathUtils.getChild(_sitePath,aPath);
        return WebURL.getURL(path);
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
        boolean isErr = _respCode!=WebResponse.OK;
        String meth = anExch.getRequestMethod();
        String path = anExch.getRequestURI().getPath();
        append("\"");
        append(meth, isErr);
        append(" ", isErr);
        append(path, isErr); append("\" ");

        // If error print error
        if (_respCode!=WebResponse.OK) {
            append("Error (");
            append(String.valueOf(_respCode), isErr); append("): \"");
            append(WebResponse.getCodeString(_respCode), isErr); append("\"");
        }

        // Otherwise append User-Agent
        else {
            Headers hdrs = anExch.getRequestHeaders();
            List <String> userAgents = hdrs.get("User-agent");
            if (userAgents!=null) {
                String str = Arrays.toString(userAgents.toArray());
                String str2 = '\"' + str + '\"';
                append(str2);
            }
        }

        //for(String hdr : hdrs.keySet()) append(hdr + " = " + Arrays.toString(hdrs.get(hdr).toArray()) + '\n');
        append("\n");
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
    private static String getGMT(Date aDate)
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
            String path = anExch.getRequestURI().getPath();

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
            WebURL url = getURL(path);
            WebResponse resp = url.getHead();

            // If response not OK, return error code
            _respCode = resp.getCode();
            if (resp.getCode()!=WebResponse.OK) {
                anExch.sendResponseHeaders(resp.getCode(),-1);
                return;
            }

            // Get length and LastModified
            FileHeader fhdr = resp.getFileHeader();
            long len = fhdr.getSize();
            Date lastMod = new Date(fhdr.getModTime());
            String ext = FilePathUtils.getExtension(url.getPath());

            // Add ResponseHeaders: last-modified, cache-control, content-length, content-type
            Headers hdrs = anExch.getResponseHeaders();
            hdrs.add("last-modified", getGMT(lastMod));
            hdrs.add("cache-control", _cacheControl);
            hdrs.add("content-length", String.valueOf(len));
            String mtype = MIMEType.getType(ext);
            if (mtype!=null)
                hdrs.add("content-type", mtype);

            // Get bytes and append
            anExch.sendResponseHeaders(HTTPResponse.OK,-1);
        }

        /** Handle GET. */
        public void handleGet(HttpExchange anExch) throws IOException
        {
            // Get path and URL
            String path = anExch.getRequestURI().getPath();
            WebURL url = getURL(path);
            WebResponse resp = url.getResponse();

            // If response not OK, return error code
            _respCode = resp.getCode();
            if (resp.getCode()!=WebResponse.OK) {
                anExch.sendResponseHeaders(resp.getCode(),-1);
                return;
            }

            // Get length and LastModified
            FileHeader fhdr = resp.getFileHeader();
            long len = fhdr.getSize();
            Date lastMod = new Date(fhdr.getModTime());
            String ext = FilePathUtils.getExtension(url.getPath());
            byte bytes[] = resp.getBytes();

            // Add ResponseHeaders: last-modified, content-length, content-type
            Headers hdrs = anExch.getResponseHeaders();
            hdrs.add("last-modified", getGMT(lastMod));
            hdrs.add("cache-control", _cacheControl);
            hdrs.add("content-length", String.valueOf(len));
            String mtype = MIMEType.getType(ext);
            if (mtype!=null)
                hdrs.add("content-type", mtype);

            // Append bytes
            anExch.sendResponseHeaders(200,bytes.length);
            OutputStream os = anExch.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }
}