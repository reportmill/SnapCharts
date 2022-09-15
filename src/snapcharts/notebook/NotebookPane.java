/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import snap.view.*;

/**
 * This class provides UI and editing for a notebook.
 */
public class NotebookPane extends ViewOwner {

    // The Notebook
    private Notebook  _notebook;

    // The NotebookView
    private NotebookView  _notebookView;

    // The HelpPane
    private HelpPane  _helpPane;

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
     * Initialize UI.
     */
    @Override
    protected void initUI()
    {
        // Get UI
        SplitView mainSplitView = getView("MainSplitView", SplitView.class);
        ScrollView notebookScrollView = getView("NotebookScrollView", ScrollView.class);

        // Create NotebookView
        Notebook notebook = getNotebook();
        _notebookView = new NotebookView();
        _notebookView.setGrowHeight(true);
        _notebookView.setNotebook(notebook);
        notebookScrollView.setContent(_notebookView);

        // Create/add HelpPane
        _helpPane = new HelpPane();
        View helpPaneUI = _helpPane.getUI();
        helpPaneUI.setPrefHeight(280);
        mainSplitView.addItem(helpPaneUI);

        // Add EscapeAction
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
        JavaEntryView javaEntryView = _notebookView.getActiveEntryView();
        if (javaEntryView == null) {
            _notebookView.focusLastEntry();
            return;
        }

        // Forward to RequestView - just return if handled
        javaEntryView.handleEscapeAction(anEvent);
        //if (anEvent.isConsumed())
        //    return;

        // If no requests, just return
        //if (_notebook.getEntries().size() == 1) {
        //    beep(); return;
        //}

        // Otherwise remove current request and select previous
        //JavaEntry javaEntry = javaEntryView.getEntry();
        //_notebook.removeEntry(javaEntry);
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
