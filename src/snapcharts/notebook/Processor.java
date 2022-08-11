/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import javakit.reflect.Resolver;
import javakit.shell.JavaShell;
import java.util.List;

/**
 * This Processor implementation executes Java.
 */
public class Processor {

    // The Notebook this processor works with
    private Notebook  _notebook;

    // The JavaShell
    private JavaShell  _javaShell;

    /**
     * Constructor.
     */
    public Processor(Notebook aNotebook)
    {
        // Set Notebook
        _notebook = aNotebook;

        // Create JavaShell
        _javaShell = new JavaShell();
        _javaShell.setREPLClassName(ChartsREPL.class.getName());
        _javaShell.addImport("snapcharts.data.*");

        // Link up StaticResolver for TeaVM
        if (Resolver.isTeaVM)
            javakit.reflect.StaticResolver.shared()._next = new snapcharts.notebook.StaticResolver();
    }

    /**
     * Returns the Notebook this processor works with.
     */
    public Notebook getNotebook()  { return _notebook; }

    /**
     * Returns the JavaText.
     */
    public String getJavaText()
    {
        String javaText = "";
        List<Request> requests = getNotebook().getRequests();
        for (Request request : requests) {
            javaText += request.getText();
            if (!javaText.endsWith("\n"))
                javaText += '\n';
        }
        return javaText;
    }

    /**
     * Resets all.
     */
    public void resetAll()
    {
        String javaText = getJavaText();

        _javaShell.runJavaCode(javaText);
    }

    /**
     * Returns the snippet out for a snippet.
     */
    public Response createResponseForRequest(Request aRequest)
    {
        // Get line values
        Object[] lineValues = _javaShell.getLineValues();

        // Get Request.StartLine as String and KeyChain
        int lineStart = aRequest.getLineStart();
        int lineEnd = aRequest.getLineEnd();
        Object lineValue = null;
        for (int i = lineEnd - 1; i >= lineStart; i--) {
            if (lineValues[i] != null) {
                lineValue = lineValues[i];
                if (!(lineValue instanceof String) || ((String) lineValue).length() > 0)
                    break;
            }
        }

        // Create Response and set value
        Response response = new Response();
        response.setValue(lineValue);

        // Return
        return response;
    }
}
