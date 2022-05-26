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
        Notebook notebook = getNotebook();
        _notebookView = new NotebookView();
        _notebookView.setNotebook(notebook);

        ScrollView scrollView = new ScrollView(_notebookView);
        //scrollView.setMinSize(800, 800);
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
        NotebookPane notebookPane = new NotebookPane();
        notebookPane.getUI().setPrefSize(800, 800);
        notebookPane.setWindowVisible(true);
    }
}
