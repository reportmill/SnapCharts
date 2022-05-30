/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import snap.util.PropObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class manages collections of snippets.
 */
public class Notebook extends PropObject {

    // The name
    private String  _name;

    // The list of Request entries
    private List<Request>  _requests = new ArrayList<>();

    // A map of Response entries
    private Map<Request,Response>  _responses = new HashMap<>();

    // The Processor
    private Processor  _processor = new Processor();

    /**
     * Constructor.
     */
    public Notebook()  { }

    /**
     * Returns the name.
     */
    public String getName()  { return _name; }

    /**
     * Sets the name.
     */
    public void setName(String aName)
    {
        _name = aName;
    }

    /**
     * Returns the Requests.
     */
    public List<Request> getRequests()  { return _requests; }

    /**
     * Adds a Request.
     */
    public void addRequest(Request aRequest)
    {
        addRequest(aRequest, _requests.size());
    }

    /**
     * Adds a Request at given index.
     */
    public void addRequest(Request aRequest, int anIndex)
    {
        _requests.add(anIndex, aRequest);
        aRequest.setIndex(anIndex + 1);
    }

    /**
     * Removes a Request at given index.
     */
    public void removeRequest(int anIndex)
    {
        _requests.remove(anIndex);
    }

    /**
     * Removes given request.
     */
    public void removeRequest(Request aRequest)
    {
        int index = _requests.indexOf(aRequest);
        if (index >= 0)
            removeRequest(index);
    }

    /**
     * Returns the Response for a given request.
     */
    public Response getResponseForRequest(Request aRequest)
    {
        // Get response from map - just return if found
        Response response = _responses.get(aRequest);
        if (response != null)
            return response;

        // Create Response for Request
        response = createResponseForRequest(aRequest);

        // Add to map and return
        _responses.put(aRequest, response);
        return response;
    }

    /**
     * Removes the response for a given request.
     */
    public void removeResponseForRequest(Request aRequest)
    {
        _responses.remove(aRequest);
    }

    /**
     * Returns the snippet out for a snippet.
     */
    protected Response createResponseForRequest(Request aRequest)
    {
        Response response = _processor.createResponseForRequest(aRequest);
        response.setIndex(aRequest.getIndex());
        return response;
    }
}
