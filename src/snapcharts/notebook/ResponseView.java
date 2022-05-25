/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import snap.gfx.Color;

/**
 * This View subclass shows snippets.
 */
public class ResponseView extends EntryView<Response> {

    /**
     * Constructor.
     */
    public ResponseView(NotebookView aNotebookView, Response aResponse)
    {
        // Do normal version
        super(aNotebookView, aResponse);

        // Set TextArea background to gray
        _textArea.setFill(Color.GRAY9);
    }
}
