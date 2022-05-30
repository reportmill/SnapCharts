/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import snap.view.*;

/**
 * This View subclass shows snippets.
 */
public class RequestView extends EntryView<Request> {

    /**
     * Constructor.
     */
    public RequestView(NotebookView aNotebookView, Request aRequest)
    {
        super(aNotebookView, aRequest);
    }

    /**
     * Override for custom request TextArea.
     */
    @Override
    protected TextArea createTextArea()
    {
        TextArea textArea = super.createTextArea();
        textArea.setEditable(true);
        textArea.addEventFilter(e -> textAreaKeyPressed(e), ViewEvent.Type.KeyPress);
        return textArea;
    }

    /**
     * Called when TextArea gets Shift+Enter.
     */
    private void textAreaKeyPressed(ViewEvent anEvent)
    {
        TextArea textArea = (TextArea) getContent();

        // Handle Shift+Enter
        if (anEvent.getKeyCode() == KeyCode.ENTER && !anEvent.isShiftDown()) {

            // Get TextArea.Text and
            Request request = getEntry();
            String text = textArea.getText().trim();
            request.setText(text);
            if (!text.equals(textArea.getText()))
                textArea.setText(text);

            // Process request
            _notebookView.processRequest(request);
            textArea.setSel(text.length());
            anEvent.consume();
        }
    }
}
