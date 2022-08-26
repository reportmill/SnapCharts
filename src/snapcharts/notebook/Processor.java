/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import javakit.reflect.Resolver;
import javakit.shell.JavaShell;
import javakit.shell.JavaText;
import javakit.shell.JavaTextDoc;
import java.util.List;

/**
 * This Processor implementation executes Java.
 */
public class Processor {

    // The Notebook this processor works with
    private Notebook  _notebook;

    // The JavaShell
    private JavaShell  _javaShell;

    // The JavaText
    private JavaText  _javaText;

    /**
     * Constructor.
     */
    public Processor(Notebook aNotebook)
    {
        // Set Notebook
        _notebook = aNotebook;

        // Create JavaShell
        _javaShell = new JavaShell();

        // Create JavaText
        _javaText = new JavaText();
        _javaText.setSuperClassName(ChartsREPL.class.getName());
        _javaText.addImport("snapcharts.data.*");

        // Link up StaticResolver for TeaVM
        if (Resolver.isTeaVM)
            javakit.reflect.StaticResolver.shared()._next = new snapcharts.notebook.StaticResolver();
    }

    /**
     * Returns the Notebook this processor works with.
     */
    public Notebook getNotebook()  { return _notebook; }

    /**
     * Returns the JavaText of the REPL class.
     */
    public JavaText getJavaText()
    {
        String javaBody = getJavaBodyText();
        _javaText.setBodyText(javaBody);
        return _javaText;
    }

    /**
     * Returns the JavaText.
     */
    protected String getJavaBodyText()
    {
        // Get requests
        String javaText = "";
        List<JavaEntry> javaEntries = getNotebook().getEntries();

        // Iterate over requests and append together
        for (JavaEntry javaEntry : javaEntries) {
            javaText += javaEntry.getText();
            if (!javaText.endsWith("\n"))
                javaText += '\n';
        }

        // Return
        return javaText;
    }

    /**
     * Resets all.
     */
    public void resetAll()
    {
        //JavaText javaText = getJavaText();
        //_javaShell.runJavaCode(javaText);

        // Run JavaCode
        JavaTextDoc javaDoc = _notebook.getJavaDoc();
        _javaShell.runJavaCode(javaDoc);
    }

    /**
     * Returns the snippet out for a snippet.
     */
    public Response createResponseForRequest(JavaEntry aJavaEntry)
    {
        // Get line values
        Object[] lineValues = _javaShell.getLineValues();

        // Get Request.StartLine as String and KeyChain
        int lineStart = aJavaEntry.getLineStart();
        int lineEnd = aJavaEntry.getLineEnd();
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
