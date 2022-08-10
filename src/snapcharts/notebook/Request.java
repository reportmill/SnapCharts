/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;

/**
 * This class represents an entry created by the user with an expression, command or code.
 */
public class Request extends Entry {

    // The number of lines in the request text
    private int  _lineCount;

    // The start line indexes relative to all notebook requests
    private int  _lineStart;

    /**
     * Returns the number of text lines in this entry.
     */
    public int getLineCount()  { return _lineCount; }

    /**
     * Returns the start line relative to notebook.
     */
    public int getLineStart()  { return _lineStart; }

    /**
     * Returns the end line relative to notebook.
     */
    public int getLineEnd()  { return _lineStart + _lineCount; }

    /**
     * Returns the start line relative to notebook.
     */
    protected void setLineStart(int aValue)  { _lineStart = aValue; }

    /**
     * Returns whether request is empty.
     */
    public boolean isEmpty()
    {
        String text = getText().trim();
        return text.length() == 0;
    }

    /**
     * Override to update LineCount
     */
    public void setText(String aValue)
    {
        super.setText(aValue);

        // Set LineCount
        String[] lines = aValue.split("\n");
        int lineCount = lines.length;
        if (aValue.startsWith("\n")) lineCount++;
        _lineCount = lineCount;
    }

}
