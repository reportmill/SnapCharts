/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import snap.props.PropObject;
import snap.props.PropSet;

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

    // The Processor
    private Processor  _processor;

    // Constants for properties
    public static final String Request_Prop = "Request";

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
        firePropChange(Request_Prop, null, aRequest, anIndex);
    }

    /**
     * Removes a Request at given index.
     */
    public void removeRequest(int anIndex)
    {
        Request request = _requests.remove(anIndex);
        firePropChange(Request_Prop, request, null, anIndex);
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
        Response response = aRequest.getResponse();
        if (response != null)
            return response;

        // Create Response for Request
        response = createResponseForRequest(aRequest);

        // Set in request and return
        aRequest.setResponse(response);
        return response;
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

            // Get request/response
            Request request = requests.get(i);
            Response response = request.getResponse();
            if (response == null)
                continue;

            // If Response.Value is of given class, return it
            Object responseValue = response.getValue();
            if (aClass.isInstance(responseValue))
                return response;
        }

        // Return not found
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

    /**
     * Override to register props.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        aPropSet.addPropNamed(Request_Prop, Request[].class, EMPTY_OBJECT);
    }

    /**
     * Returns the prop value for given key.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        // Handle properties
        switch (aPropName) {

            // Request
            case Request_Prop: return getRequests();

            // Handle super class properties (or unknown)
            default: return super.getPropValue(aPropName);
        }
    }

    /**
     * Sets the prop value for given key.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        // Handle properties
        switch (aPropName) {

            // Request
            //case Request_Prop: setRequests();

            // Handle super class properties (or unknown)
            default: super.setPropValue(aPropName, aValue);
        }
    }
}
