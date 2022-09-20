/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import javakit.shell.JavaTextDocBlock;
import snap.text.TextDoc;

/**
 * This entry subclass represents a snippet of Java code with an expression, statement or other code.
 */
public class JavaEntry {

    // The notebook that contains this entry
    private Notebook  _notebook;

    // The index in notebook request/response list
    private int  _index;

    // The JavaTextDoc.Block
    private JavaTextDocBlock  _block;

    // The Response
    private Response  _response;

    /**
     * Constructor.
     */
    public JavaEntry(Notebook aNotebook, JavaTextDocBlock aBlock)
    {
        _notebook = aNotebook;
        _block = aBlock;
    }

    /**
     * Returns the notebook that contains this entry.
     */
    public Notebook getNotebook()  { return _notebook; }

    /**
     * Returns the index in Notebook request/response list.
     */
    public int getIndex()  { return _index; }

    /**
     * Sets the index in Notebook request/response list.
     */
    protected void setIndex(int anIndex)
    {
        _index = anIndex;
    }

    /**
     * Returns the JavaTextDoc.Block.
     */
    public JavaTextDocBlock getJavaBlock()  { return _block; }

    /**
     * Returns the text.
     */
    public String getText()
    {
        TextDoc textDoc = _block.getTextDoc();
        return textDoc.getString();
    }

    /**
     * Sets the text.
     */
    public void setText(String aValue)
    {
        TextDoc textDoc = _block.getTextDoc();
        int length = textDoc.length();
        textDoc.replaceChars(aValue, 0, length);
    }

    /**
     * Returns the start line relative to notebook.
     */
    public int getLineStart()  { return _block.getStartLine().getIndex(); }

    /**
     * Returns the end line relative to notebook.
     */
    public int getLineEnd()  { return _block.getEndLine().getIndex(); }

    /**
     * Returns whether entry is empty.
     */
    public boolean isEmpty()
    {
        return _block.isEmpty();
    }

    /**
     * Gets the Response.
     */
    public Response getResponse()  { return _response; }

    /**
     * Sets the Response.
     */
    public void setResponse(Response aResponse)
    {
        _response = aResponse;
    }

    /**
     * Returns the previous entry.
     */
    public JavaEntry getPrevEntry()
    {
        int index = getIndex();
        if (index > 0) {
            Notebook notebook = getNotebook();
            return notebook.getEntries().get(index - 1);
        }
        return null;
    }

    /**
     * Standard toString method.
     */
    @Override
    public String toString()
    {
        String className = getClass().getSimpleName();
        String propStrings = toStringProps();
        return className + " { " + propStrings + " }";
    }

    /**
     * Standard toString method.
     */
    protected String toStringProps()
    {
        String textProp = "Text:" + getText();
        return textProp;
    }
}
