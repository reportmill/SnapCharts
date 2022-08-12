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

    // A pending request used to add new request
    protected Request  _pendingRequest;

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

        // Update request lineStart
        int lineStart = 0;
        for (Request request : requests) {
            request.setLineStart(lineStart);
            lineStart += request.getLineCount();
        }

        // Reset all
        Processor processor = _notebook.getProcessor();
        processor.resetAll();

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
            removeEntryView(aRequest);
            Response response = aRequest.getResponse();
            if (response != null) {
                removeEntryView(response);
                aRequest.setResponse(null);
            }
        }

        // Reset Entries
        resetEntriesLater();
    }

    /**
     * Called when Request view gets tab key.
     */
    protected void handleTabKey(ViewEvent anEvent)
    {
        // Get current request
        RequestView requestView = anEvent.getView().getParent(RequestView.class); if (requestView == null) return;
        requestView.getTextArea().setSel(1000);
        Request request = requestView.getEntry();

        // Get next request (depending on tab or shift-tab)
        int index = request.getIndex();
        int offset = anEvent.isShiftDown() ? -1 : 1;
        int index2 = index - 1 + offset;
        List<Request> requests = _notebook.getRequests();
        Request nextRequest = index2 < 0 ? null : index2 < requests.size() ? requests.get(index2) : _pendingRequest;
        if (nextRequest == null) return;

        // Get next request TextArea and select/focus
        RequestView nextRequestView = (RequestView) getEntryView(nextRequest);
        TextArea textArea = nextRequestView.getTextArea();
        textArea.selectAll();
        textArea.requestFocus();
    }

    /**
     * Returns the selected request view.
     */
    public RequestView getActiveRequestView()
    {
        WindowView win = getWindow(); if (win == null) return null;
        View focusedView = win.getFocusedView(); if (focusedView == null) return null;
        RequestView requestView = focusedView.getParent(RequestView.class);
        return requestView;
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
                Request request = (Request) aPC.getOldValue();
                removeEntryView(request);

                // Remove ResponseView
                Response response = request.getResponse();
                if (response != null) {
                    removeEntryView(response);
                    request.setResponse(null);
                }

                // If request was last, make it new pending
                if (aPC.getIndex() >= _notebook.getRequests().size())
                    _pendingRequest = request;
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
