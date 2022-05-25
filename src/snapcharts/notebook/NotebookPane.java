/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import snap.view.ScrollView;
import snap.view.View;
import snap.view.ViewOwner;

/**
 * This class provides UI and editing for a notebook.
 */
public class NotebookPane extends ViewOwner {

    // The NotebookView
    private NotebookView  _notebookView;

    /**
     * Create UI.
     */
    @Override
    protected View createUI()
    {
        Notebook notebook = new Notebook();
        _notebookView = new NotebookView();
        _notebookView.setNotebook(notebook);

        ScrollView scrollView = new ScrollView(_notebookView);
        scrollView.setMinSize(800, 800);
        return scrollView;
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
     * Standard main method.
     */
    public static void main(String[] args)
    {
        new NotebookPane().setWindowVisible(true);
    }
}
