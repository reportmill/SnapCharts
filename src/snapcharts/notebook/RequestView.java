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

        // Register TextArea
        _textArea.setEditable(true);
        _textArea.addEventFilter(e -> textAreaKeyPressed(e), ViewEvent.Type.KeyPress);
    }

    /**
     * Called when TextArea gets Shift+Enter.
     */
    private void textAreaKeyPressed(ViewEvent anEvent)
    {
        // Handle Shift+Enter
        if (anEvent.getKeyCode() == KeyCode.ENTER && anEvent.isShiftDown()) {

            // Get TextArea.Text and
            Request request = getEntry();
            String text = _textArea.getText().trim();
            request.setText(text);
            if (!text.equals(_textArea.getText()))
                _textArea.setText(text);

            // Process request
            _notebookView.processRequest(request);
            _textArea.setSel(text.length());
            anEvent.consume();
        }
    }

    @Override
    public void requestFocus()
    {
        _textArea.requestFocus();
    }
}
