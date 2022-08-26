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
        entryView = createEntryView(anEntry);
        _entryViews.put(anEntry, entryView);
        return entryView;
    }

    /**
     * Creates an EntryView for given Entry.
     */
    protected EntryView createEntryView(Entry anEntry)
    {
        // Handle Request
        if (anEntry instanceof JavaEntry)
            return new JavaEntryView(this, (JavaEntry) anEntry);

        // Handle Response
        if (anEntry instanceof Response)
            return new ResponseView(this, (Response) anEntry);

        // Complain
        return null;
    }

    /**
     * Removes an entry view from cache.
     */
    public void removeEntryView(Entry anEntry)
    {
        _entryViews.remove(anEntry);
    }

    /**
     * Resets entry views.
     */
    protected void resetEntries()
    {
        // Remove all children
        removeChildren();

        // Make sure there is an empty entry
        if (!_notebook.isEmptyEntrySet())
            _notebook.addEmptyEntry();

        // Get list of requests
        List<JavaEntry> javaEntries = _notebook.getEntries();

        // Reset all
        Processor processor = _notebook.getProcessor();
        processor.resetAll();

        // Iterate over requests and add request/response views
        for (JavaEntry javaEntry : javaEntries) {

            // Get EntryView and add
            EntryView requestView = getEntryView(javaEntry);
            addChild(requestView);

            // Get Response and ResponseView and add
            Response response = _notebook.getResponseForEntry(javaEntry);
            EntryView responseView = getEntryView(response);
            addChild(responseView);
        }

        // Focus last request view - - should really just move forward
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
     * Process Request
     */
    public void processRequest(JavaEntry aJavaEntry)
    {
//        // If empty request remove request and response from notebook
//        if (aRequest.isEmpty()) {
//            _notebook.removeRequest(aRequest);
//            removeEntryView(aRequest);
//        }
//
//        // If Request is PendingRequest, add to Notebook and queue up new PendingRequest
//        else if (aRequest == _pendingRequest) {
//            _notebook.addRequest(_pendingRequest);
//            _pendingRequest = new Request();
//            _pendingRequest.setIndex(_notebook.getRequests().size() + 1);
//        }
//
//        // Otherwise remove Response and ResponseView for Request
//        else {
//            removeEntryView(aRequest);
//            Response response = aRequest.getResponse();
//            if (response != null) {
//                removeEntryView(response);
//                aRequest.setResponse(null);
//            }
//        }

        // Reset Entries
        resetEntriesLater();
    }

    /**
     * Called when Request view gets tab key.
     */
    protected void handleTabKey(ViewEvent anEvent)
    {
        // Get current request
        JavaEntryView javaEntryView = anEvent.getView().getParent(JavaEntryView.class); if (javaEntryView == null) return;
        javaEntryView.getTextArea().setSel(1000);
        JavaEntry javaEntry = javaEntryView.getEntry();

        // Get next request (depending on tab or shift-tab)
        int index = javaEntry.getIndex();
        int offset = anEvent.isShiftDown() ? -1 : 1;
        int index2 = index - 1 + offset;
        List<JavaEntry> javaEntries = _notebook.getEntries();
        JavaEntry nextJavaEntry = index2 < 0 ? null : index2 < javaEntries.size() ? javaEntries.get(index2) : null;
        if (nextJavaEntry == null) return;

        // Get next request TextArea and select/focus
        JavaEntryView nextJavaEntryView = (JavaEntryView) getEntryView(nextJavaEntry);
        TextArea textArea = nextJavaEntryView.getTextArea();
        textArea.selectAll();
        textArea.requestFocus();
    }

    /**
     * Returns the selected request view.
     */
    public JavaEntryView getActiveRequestView()
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
        // Handle Request Add/Remove
        String propName = aPC.getPropName();
        if (propName == Notebook.Request_Prop) {

            // Handle Request Removed
            if (aPC.getNewValue() == null) {

                // Remove RequestView
                JavaEntry javaEntry = (JavaEntry) aPC.getOldValue();
                removeEntryView(javaEntry);

                // Remove ResponseView
                Response response = javaEntry.getResponse();
                if (response != null) {
                    removeEntryView(response);
                    javaEntry.setResponse(null);
                }
            }
        }

        // Reset entries
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
