/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import javakit.shell.JavaShell;
import javakit.shell.JavaTextDoc;

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
    }

    /**
     * Returns the Notebook this processor works with.
     */
    public Notebook getNotebook()  { return _notebook; }

    /**
     * Resets all.
     */
    public void resetAll()
    {
        // Run JavaCode
        JavaTextDoc javaDoc = _notebook.getJavaDoc();
        _javaShell.runJavaCode(javaDoc);
    }

    /**
     * Returns the value for given entry.
     */
    public Object getValueForJavaEntry(JavaEntry aJavaEntry)
    {
        // Get line values
        Object[] lineValues = _javaShell.getLineValues();

        // Get entry start/end line indexes
        int startLineIndex = aJavaEntry.getLineStart();
        int endLineIndex = aJavaEntry.getLineEnd();

        // Iterate over entry lines (backward) to find a value
        for (int i = endLineIndex; i >= startLineIndex; i--) {
            if (lineValues[i] != null) {
                Object lineValue = lineValues[i];
                if (!(lineValue instanceof String) || ((String) lineValue).length() > 0)
                    return lineValue;
            }
        }

        // Return not found
        return null;
    }
}
