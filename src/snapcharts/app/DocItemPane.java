/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.app;
import snap.view.View;
import snap.view.ViewAnimUtils;
import snap.view.ViewOwner;

/**
 * The base class for DocItem editors.
 */
public class DocItemPane extends ViewOwner {

    // The DocPane that holds this DocItemPane
    private DocPane  _docPane;

    // Whether inspector is showing
    private boolean  _showInsp = true;

    /**
     * Constructor.
     */
    public DocItemPane()
    {
        super();
    }

    /**
     * Returns the DocPane.
     */
    public DocPane getDocPane()  { return _docPane; }

    /**
     * Sets the DocPane.
     */
    public void setDocPane(DocPane aDP)
    {
        _docPane = aDP;
    }

    /**
     * Returns the view for the DocItem.
     */
    public View getItemView()  { return null; }

    /**
     * Returns whether inspector is visible.
     */
    public boolean isShowInspector()  { return _showInsp; }

    /**
     * Sets whether inspector is visible.
     */
    public void setShowInspector(boolean aValue)
    {
        // If already set, just return
        if (aValue == isShowInspector()) return;

        // Set value
        _showInsp = aValue;

        // Get inspector and view
        ViewOwner insp = getInspector(); if (insp==null) return;
        View inspView = insp.getUI();

        // Set visible (animated)
        ViewAnimUtils.setVisible(inspView, aValue, true, false);
    }

    /**
     * Returns the inspector.
     */
    public ViewOwner getInspector()  { return null; }
}
