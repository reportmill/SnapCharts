/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import snap.gfx.Color;
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

    // A pending request used to add new request
    private Request  _pendingRequest;

    // A map of EntryView for Entries
    private Map<Entry,EntryView>  _entryViews = new HashMap<>();

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

        // Create the PendingRequest
        _pendingRequest = new Request();
        _pendingRequest.setIndex(1);

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
    protected EntryView createEntryView(Entry anEntry)
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

        // Get list of requests
        List<Request> requests = _notebook.getRequests();

        // Iterate over requests and add request/response views
        for (Request request : requests) {

            // Get EntryView and add
            EntryView requestView = getEntryView(request);
            addChild(requestView);

            // Get Response and ResponseView and add
            Response response = _notebook.getResponseForRequest(request);
            EntryView responseView = getEntryView(response);
            addChild(responseView);
        }

        // Add pending request
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
     * Process Request
     */
    public void processRequest(Request aRequest)
    {
        // If empty request remove request and response from notebook
        if (aRequest.isEmpty()) {
            _notebook.removeRequest(aRequest);
            _notebook.removeResponseForRequest(aRequest);
            removeEntryView(aRequest);
        }

        // If Request is PendingRequest, add to Notebook and queue up new PendingRequest
        else if (aRequest == _pendingRequest) {
            _notebook.addRequest(_pendingRequest);
            _pendingRequest = new Request();
            _pendingRequest.setIndex(_notebook.getRequests().size() + 1);
        }

        // Otherwise remove Response and ResponseView for Request
        else {
            _notebook.removeResponseForRequest(aRequest);
            removeEntryView(aRequest);
        }

        // Reset Entries
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
