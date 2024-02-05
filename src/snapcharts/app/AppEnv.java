/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.app;
import snap.gfx.GFXEnv;
import snap.util.FileUtils;
import snap.util.SnapUtils;
import snap.web.WebURL;
import snapcharts.doc.Doc;
import snapcharts.charts.Chart;
import java.io.File;
import java.io.IOException;

/**
 * A class to allow certain functionality to be pluggable depending on platform (desktop/web).
 */
public class AppEnv {

    // The shared instance
    private static AppEnv  _shared;

    // Constants
    private static final String SNAPCHARTS_URL = "https://reportmill.com/snaptea/SnapCharts/classes.js";
    private static final String SNAPCHARTS_URL_LOCAL = "http://localhost:8080/classes.js";

    /**
     * Open a chart doc in browser.
     */
    public void openChartsDocInBrowser(Doc aDoc, boolean isLocal)
    {
        String filename = "OpenInSnapCharts.html";
        String htmlString = getHTMLString(aDoc, isLocal);
        byte[] htmlBytes = htmlString.getBytes();
        openFilenameBytes(filename, htmlBytes);
    }

    /**
     * Open a chart doc in browser.
     */
    public void openChartInBrowser(Chart aChart, boolean isLocal)
    {
        Doc doc = new Doc();
        doc.addChart(aChart);
        openChartsDocInBrowser(doc, isLocal);
    }

    /**
     * Opens the given filename + bytes as a file.
     */
    private void openFilenameBytes(String aFilename, byte[] theBytes)
    {
        // Get file
        File file = SnapUtils.isTeaVM ? new File('/' + aFilename) :
            FileUtils.getTempFile(aFilename);

        // TeaVM seems to sometimes use remnants of old file. This has been fixed
        if (SnapUtils.isTeaVM)
            try { file.delete(); }
            catch (Exception e) { System.err.println("DevPaneViewOwners.showInSnapBuilder: Error deleting file"); }

        // Write HTML string to temp HTML file
        try { FileUtils.writeBytes(file, theBytes); }
        catch (IOException e)
        {
            System.err.println("openFilenameBytes write error: " + e);
            return;
        }

        // Open temp HTML file
        if (SnapUtils.isTeaVM)
            GFXEnv.getEnv().openFile(file);
        else GFXEnv.getEnv().openURL(file);
    }

    /**
     * Returns the HTML to open UI in SnapBuilder web.
     */
    private String getHTMLString(Doc aDoc, boolean isLocal)
    {
        // Get URL string for SnapCharts script
        String urls = isLocal ? SNAPCHARTS_URL_LOCAL : SNAPCHARTS_URL;

        // Get SelOwner and XML string
        String docFilename = aDoc.getFilename();
        WebURL docURL = aDoc.getSourceURL();
        String docPath = docURL != null ? docURL.getPath() : ('/' + docFilename);

        // Get docString
        String docString = aDoc.getChartsFileXMLString();

        // Create StringBuffer and add header
        StringBuffer sb = new StringBuffer();
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html>\n");

        // Add header/title
        String title = "SnapCharts: " + docPath;
        sb.append("<head>\n");
        sb.append("<title>").append(title).append("</title>\n");
        sb.append("</head>\n");

        // Add body
        sb.append("<body>\n");
        sb.append("</body>\n");

        // Add SnapBuilder Script
        sb.append("<script type=\"text/javascript\" charset=\"utf-8\" src=\"" + urls + "\"></script>\n");

        // Add script with inline OpenOnLaunchFilename/OpenOnLaunchString args and main() entry point
        sb.append("<script>\n\n");
        sb.append("const openOnLaunchFilename = '").append(docFilename).append("';\n");
        sb.append("const openOnLaunchString = `\n");
        sb.append(docString);
        sb.append("\n`;\n\n");
        sb.append("main(['OpenOnLaunchFilename', openOnLaunchFilename, 'OpenOnLaunchString', openOnLaunchString]);\n\n");
        sb.append("</script>\n\n");

        // Close HTML and return string
        sb.append("</html>\n");
        return sb.toString();
    }

    /**
     * Returns the shared instance.
     */
    public static AppEnv getEnv()
    {
        // If already set, just return
        if (_shared != null) return _shared;

        if (_shared == null)
            return _shared = new AppEnv();

        // Use generic for TEAVM, otherwise Swing version
        String cname = SnapUtils.getPlatform()==SnapUtils.Platform.TEAVM ? "snapcharts.app.AppEnv" : "snapcharts.app.AppEnvSwing";

        // Try to get/set class name instance
        try
        {
            return _shared = (AppEnv) Class.forName(cname).newInstance();
        }
        catch(Exception e)
        {
            System.err.println("AppEnv.getEnv: Can't set env: " + cname + ", " + e);
            return _shared = new AppEnv();
        }
    }
}
