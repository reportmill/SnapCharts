/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
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

    // A pending snippet used to add another snippet
    private Request  _pendingRequest;

    // A map of EntryView for Entries
    private Map<Entry,EntryView>  _entryViews = new HashMap<>();

    /**
     * Constructor.
     */
    public NotebookView()
    {
        super();

        setPadding(25, 5, 5, 40);

        // Create the PendingSnippet
        _pendingRequest = new Request();

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
    public EntryView createEntryView(Entry anEntry)
    {
        // Handle Request
        if (anEntry instanceof Request)
            return new RequestView(this, (Request) anEntry);

        // Handle Response
        if (anEntry instanceof Response)
            return new ResponseView(this, (Response) anEntry);

        // Complain
        return null;
    }

    /**
     * Resets entry views.
     */
    protected void resetEntries()
    {
        // Remove all children
        removeChildren();

        // Get list of snippets
        List<Request> requests = _notebook.getRequests();

        // Iterate over snippets and add
        for (Request request : requests) {

            // Get EntryView and add
            EntryView requestView = getEntryView(request);
            addChild(requestView);

            // Get snippet out
            Response response = _notebook.getResponseForRequest(request);
            EntryView responseView = getEntryView(response);
            addChild(responseView);
        }

        // Add pending snippet
        Request pendingRequest = _pendingRequest;
        EntryView pendingView = getEntryView(pendingRequest);
        addChild(pendingView);

        // Focus pending
        pendingView.requestFocus();
    }

    /**
     * Resets EntryViews.
     */
    protected void resetEntriesLater()
    {
        ViewUtils.runLater(() -> resetEntries());
    }

    /**
     * Process snippet.
     */
    public void processRequest(Request aRequest)
    {
        if (aRequest == _pendingRequest) {
            _notebook.addRequest(_pendingRequest);
            _pendingRequest = new Request();
            resetEntriesLater();
        }
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
