/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import snap.view.ScrollView;
import snap.view.View;
import snap.view.ViewEvent;
import snap.view.ViewOwner;

/**
 * This class provides UI and editing for a notebook.
 */
public class NotebookPane extends ViewOwner {

    // The Notebook
    private Notebook  _notebook;

    // The NotebookView
    private NotebookView  _notebookView;

    /**
     * Returns the Notebook.
     */
    public Notebook getNotebook()
    {
        if (_notebook != null) return _notebook;
        Notebook notebook = new Notebook();
        return _notebook = notebook;
    }

    /**
     * Sets the Notebook.
     */
    public void setNotebook(Notebook aNotebook)
    {
        _notebook = aNotebook;

        if (_notebookView != null) {
            _notebookView.setNotebook(aNotebook);
            _notebookView.resetEntriesLater();
        }
    }

    /**
     * Create UI.
     */
    @Override
    protected View createUI()
    {
        // Create NotebookView
        Notebook notebook = getNotebook();
        _notebookView = new NotebookView();
        _notebookView.setGrowHeight(true);
        _notebookView.setNotebook(notebook);

        // Create ScrollView for NotebookView
        ScrollView scrollView = new ScrollView(_notebookView);
        scrollView.setFillWidth(true);

        // Return
        return scrollView;
    }

    /**
     * Initialize UI.
     */
    @Override
    protected void initUI()
    {
        addKeyActionFilter("EscapeAction", "ESCAPE");
    }

    /**
     * Called when first showing.
     */
    @Override
    protected void initShowing()
    {
        _notebookView.resetEntriesLater();
    }

    /**
     * Respond to UI.
     */
    @Override
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle EscapeAction
        if (anEvent.equals("EscapeAction"))
            handleEscapeAction(anEvent);
    }

    /**
     * Escape out of current request editing.
     */
    protected void handleEscapeAction(ViewEvent anEvent)
    {
        // Get active RequestView (just return if none)
        JavaEntryView javaEntryView = _notebookView.getActiveRequestView();
        if (javaEntryView == null) {
            _notebookView.focusLastJavaEntry();
            return;
        }

        // Forward to RequestView - just return if handled
        javaEntryView.handleEscapeAction(anEvent);
        if (anEvent.isConsumed())
            return;

        // If no requests, just return
        if (_notebook.getEntries().size() == 1) {
            beep(); return; }

        // Otherwise remove current request and select previous
        JavaEntry javaEntry = javaEntryView.getEntry();

        // Remove request
        _notebook.removeEntry(javaEntry);
    }

    /**
     * Standard main method.
     */
    public static void main(String[] args)
    {
        NotebookPane notebookPane = new NotebookPane();
        notebookPane.getUI().setPrefSize(800, 800);
        notebookPane.setWindowVisible(true);
    }
}
