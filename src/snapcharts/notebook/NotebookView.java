/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import snap.geom.Rect;
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
    private Map<Entry,JavaEntryView>  _entryViews = new HashMap<>();

    // A map of ResponseView for Responses
    private Map<Entry,ResponseView>  _responseViews = new HashMap<>();

    // For resetEntriesLater
    private Runnable  _resetEntriesRun;

    // For resetEntriesLater
    private Runnable  _resetEntriesRunReal = () -> { resetEntries(); _resetEntriesRun = null; };

    /**
     * Constructor.
     */
    public NotebookView()
    {
        super();

        setPadding(10, 5, 5, 30);

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
    public JavaEntryView getEntryView(Entry anEntry)
    {
        // Get EntryView from EntryViews map and return if found
        JavaEntryView entryView = _entryViews.get(anEntry);
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
            JavaEntryView entryView = getEntryView(javaEntry);
            addChild(entryView);

            // Get Response and ResponseView and add
            Response response = javaEntry.getResponse();
            if (response != null) {
                ResponseView responseView = getResponseView(response);
                addChild(responseView);
            }
        }

        // Focus last entry view - - should really just move forward
        getEnv().runLater(() -> focusLastEntry());
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
        int index2 = index + offset;
        List<JavaEntry> javaEntries = _notebook.getEntries();
        JavaEntry nextJavaEntry = index2 < 0 ? null : index2 < javaEntries.size() ? javaEntries.get(index2) : null;
        if (nextJavaEntry == null)
            return;

        // Select/focus entry
        focusEntry(nextJavaEntry, true);
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
     * Focuses on given entry.
     */
    protected void focusEntry(JavaEntry anEntry, boolean doSelectAll)
    {
        // Focus entry
        JavaEntryView entryView = getEntryView(anEntry);
        TextArea textArea = entryView.getTextArea();
        if (doSelectAll)
            textArea.selectAll();
        textArea.requestFocus();

        // Scroll to visible, too
        getEnv().runLater(() -> {
            Rect bounds = entryView.getBoundsLocal();
            bounds.inset(-5);
            entryView.scrollToVisible(bounds);
        });
    }

    /**
     * Focuses on last JavaEntry.
     */
    protected void focusLastEntry()
    {
        List<JavaEntry> javaEntries = _notebook.getEntries();
        JavaEntry lastJavaEntry = javaEntries.get(javaEntries.size() - 1);
        focusEntry(lastJavaEntry, false);
    }

    /**
     * Called when Notebook changes.
     */
    private void notebookDidPropChange(PropChange aPC)
    {
        // Get prop name
        String propName = aPC.getPropName();

        // Handle NeedsUpdate
        if (propName == Notebook.NeedsUpdate_Prop && _notebook.isNeedsUpdate())
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
