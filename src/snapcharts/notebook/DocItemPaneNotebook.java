/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import snap.view.View;
import snapcharts.app.DocItemPane;

/**
 * This DocItemPane subclass supports DocItemReport.
 */
public class DocItemPaneNotebook extends DocItemPane<DocItemNotebook> {

    // The NotebookPane
    private NotebookPane  _notebookPane;

    /**
     * Constructor.
     */
    public DocItemPaneNotebook(DocItemNotebook aDocItem)
    {
        super(aDocItem);

        // Create NotebookPane
        _notebookPane = new NotebookPane();

        Notebook notebook = aDocItem.getNotebook();
        _notebookPane.setNotebook(notebook);
    }

    /**
     *
     */
    @Override
    protected View createUI()
    {
        return _notebookPane.getUI();
    }
}
