/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import snap.gfx.Color;
import snap.props.PropChange;
import snap.view.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This View subclass is used to display a Notebook.
 */
public class NotebookView extends ParentView {

    // The notebook
    private Notebook  _notebook;

    // A map of EntryView for Entries
    private Map<Entry,EntryView>  _entryViews = new HashMap<>();

    // A map of ResponseView for Responses
    private Map<Entry,ResponseView>  _responseViews = new HashMap<>();

    // For resetEntriesLater
    private Runnable  _resetEntriesRun;

    // For resetEntriesLater
    private Runnable  _resetEntriesRunReal = () -> { resetEntries(); _resetEntriesRun = null; };

    // Constants
    public static Color BACK_FILL = new Color(226, 232, 246);

    /**
     * Constructor.
     */
    public NotebookView()
    {
        super();

        setFill(BACK_FILL);
        setPadding(25, 5, 5, 40);

        resetEntriesLater();
    }

    /**
     * Returns the notebook.
     */
    public Notebook getNotebook()  { return _notebook; }

    /**
     * Sets the notebook.
     */
    public void setNotebook(Notebook aNotebook)
    {
        _notebook = aNotebook;
        _notebook.addPropChangeListener(pc -> notebookDidPropChange(pc));
    }

    /**
     * Returns an EntryView for given Entry.
     */
    public EntryView getEntryView(Entry anEntry)
    {
        // Get EntryView from EntryViews map and return if found
        EntryView entryView = _entryViews.get(anEntry);
        if (entryView != null)
            return entryView;

        // Create EntryView for Entry, add to map and return
        entryView = new JavaEntryView(this, (JavaEntry) anEntry);
        _entryViews.put(anEntry, entryView);
        return entryView;
    }

    /**
     * Removes an entry view from cache.
     */
    public void removeEntryView(Entry anEntry)
    {
        _entryViews.remove(anEntry);
    }

    /**
     * Returns an ResponseView for given Response.
     */
    public ResponseView getResponseView(Response aResponse)
    {
        // Get EntryView from EntryViews map and return if found
        ResponseView entryView = _responseViews.get(aResponse);
        if (entryView != null)
            return entryView;

        // Create ResponseView for Response, add to map and return
        entryView =  new ResponseView(this, aResponse);
        _responseViews.put(aResponse, entryView);
        return entryView;
    }

    /**
     * Resets entry views.
     */
    protected void resetEntries()
    {
        // Remove all child views
        removeChildren();

        // Make sure there is an empty entry
        if (!_notebook.isEmptyEntrySet())
            _notebook.addEmptyEntry();

        // Update notebook (and clear ResponseView cache)
        if (_notebook.isNeedsUpdate()) {
            _notebook.updateNotebook();
            _responseViews.clear();
        }

        // Get entries
        List<JavaEntry> javaEntries = _notebook.getEntries();

        // Iterate over entries and add entry/response views
        for (JavaEntry javaEntry : javaEntries) {

            // Get EntryView and add
            EntryView entryView = getEntryView(javaEntry);
            addChild(entryView);

            // Get Response and ResponseView and add
            Response response = javaEntry.getResponse();
            if (response != null) {
                ResponseView responseView = getResponseView(response);
                addChild(responseView);
            }
        }

        // Focus last entry view - - should really just move forward
        focusLastJavaEntry();
    }

    /**
     * Resets EntryViews.
     */
    protected void resetEntriesLater()
    {
        if (_resetEntriesRun == null)
            ViewUtils.runLater(_resetEntriesRun = _resetEntriesRunReal);
    }

    /**
     * Called when EntryView gets tab key.
     */
    protected void handleTabKey(ViewEvent anEvent)
    {
        // Get current entry
        JavaEntryView javaEntryView = anEvent.getView().getParent(JavaEntryView.class); if (javaEntryView == null) return;
        javaEntryView.getTextArea().setSel(1000);
        JavaEntry javaEntry = javaEntryView.getEntry();

        // Get next entry (depending on tab or shift-tab)
        int index = javaEntry.getIndex();
        int offset = anEvent.isShiftDown() ? -1 : 1;
        int index2 = index - 1 + offset;
        List<JavaEntry> javaEntries = _notebook.getEntries();
        JavaEntry nextJavaEntry = index2 < 0 ? null : index2 < javaEntries.size() ? javaEntries.get(index2) : null;
        if (nextJavaEntry == null)
            return;

        // Get next entryView TextArea and select/focus
        JavaEntryView nextJavaEntryView = (JavaEntryView) getEntryView(nextJavaEntry);
        TextArea textArea = nextJavaEntryView.getTextArea();
        textArea.selectAll();
        textArea.requestFocus();
    }

    /**
     * Returns the selected entry view.
     */
    public JavaEntryView getActiveEntryView()
    {
        WindowView win = getWindow(); if (win == null) return null;
        View focusedView = win.getFocusedView(); if (focusedView == null) return null;
        JavaEntryView javaEntryView = focusedView.getParent(JavaEntryView.class);
        return javaEntryView;
    }

    /**
     * Focuses on last JavaEntry.
     */
    protected void focusLastJavaEntry()
    {
        List<JavaEntry> javaEntries = _notebook.getEntries();
        JavaEntry lastJavaEntry = javaEntries.get(javaEntries.size() - 1);
        EntryView lastJavaEntryView = getEntryView(lastJavaEntry);
        lastJavaEntryView.requestFocus();
    }

    /**
     * Called when Notebook changes.
     */
    private void notebookDidPropChange(PropChange aPC)
    {
        // Get prop name
        String propName = aPC.getPropName();

        // Handle NeedsUpdate
        if (propName == Notebook.NeedsUpdate_Prop)
            resetEntriesLater();
    }

    @Override
    protected double getPrefWidthImpl(double aH)
    {
        ColViewProxy viewProxy = getViewProxy();
        double prefW = viewProxy.getPrefWidth(aH);
        return prefW;
    }

    @Override
    protected double getPrefHeightImpl(double aW)
    {
        ColViewProxy viewProxy = getViewProxy();
        double prefH = viewProxy.getPrefHeight(aW);
        return prefH;
    }

    @Override
    protected void layoutImpl()
    {
        ColViewProxy viewProxy = getViewProxy();
        viewProxy.layoutView();
    }

    @Override
    protected ColViewProxy<?> getViewProxy()
    {
        ColViewProxy colViewProxy = new ColViewProxy(this);
        colViewProxy.setFillWidth(true);
        return colViewProxy;
    }
}
