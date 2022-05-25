/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import snap.util.KeyChain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class manages collections of snippets.
 */
public class Notebook {

    // The list of Request entries
    private List<Request>  _requests = new ArrayList<>();

    // A map of Response entries
    private Map<Request,Response>  _responses = new HashMap<>();

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
     * Returns the snippet out for a snippet.
     */
    protected Response createResponseForRequest(Request aRequest)
    {
        // Get Request.Text as String and KeyChain
        String text = aRequest.getText();
        KeyChain keyChain = KeyChain.getKeyChain(text);

        // Process keyChain
        String responseStr = KeyChain.getStringValue(new Object(), keyChain);

        // Create Response, set Text and return
        Response response = new Response();
        response.setText(responseStr);
        return response;
    }
}
