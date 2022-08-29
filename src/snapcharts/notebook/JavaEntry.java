/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import javakit.shell.JavaTextDocBlock;

/**
 * This entry subclass represents a snippet of Java code with an expression, statement or other code.
 */
public class JavaEntry extends Entry {

    // The JavaTextDoc.Block
    private JavaTextDocBlock  _block;

    // The Response
    private Response  _response;

    /**
     * Constructor.
     */
    public JavaEntry(JavaTextDocBlock aBlock)
    {
        _block = aBlock;
    }

    /**
     * Returns the JavaTextDoc.Block.
     */
    public JavaTextDocBlock getJavaBlock()  { return _block; }

    /**
     * Returns the start line relative to notebook.
     */
    public int getLineStart()  { return _block.getStartLine().getIndex(); }

    /**
     * Returns the end line relative to notebook.
     */
    public int getLineEnd()  { return _block.getEndLine().getIndex(); }

    /**
     * Returns whether request is empty.
     */
    public boolean isEmpty()
    {
        int length = _block.length();
        return length == 0;
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
}
