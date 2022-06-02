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
    private Processor  _processor;

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
     * Returns whether Response for a given request is set.
     */
    public boolean isResponseForRequestSet(Request aRequest)
    {
        return _responses.containsKey(aRequest);
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
        Processor processor = getProcessor();
        Response response = processor.createResponseForRequest(aRequest);
        response.setIndex(aRequest.getIndex());
        return response;
    }

    /**
     * Returns the processor.
     */
    public Processor getProcessor()
    {
        if (_processor != null) return _processor;
        Processor processor = new Processor(this);
        return _processor = processor;
    }

    /**
     * Returns the last response of given class.
     */
    public Response getLastResponseForValueClass(Class aClass)
    {
        // Get list of Requests
        List<Request> requests = getRequests();

        // Iterate over requests (backwards) and return first Response.Value for given class
        for (int i = requests.size() - 1; i >= 0; i--) {
            Request request = requests.get(i);
            if (!isResponseForRequestSet(request))
                continue;
            Response response = getResponseForRequest(request);
            Object responseValue = response.getValue();
            if (aClass.isInstance(responseValue))
                return response;
        }

        // Return null since none found
        return null;
    }

    /**
     * Returns the last response of given class.
     */
    public <T> T getLastResponseValueForClass(Class<T> aClass)
    {
        Response response = getLastResponseForValueClass(aClass);
        T responseValue = response != null ? (T) response.getValue() : null;
        return responseValue;
    }
}
