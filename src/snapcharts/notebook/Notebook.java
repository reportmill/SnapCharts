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

    // The list of Snippets
    private List<Request>  _requests = new ArrayList<>();

    // A map of processed snippets
    private Map<Request, Response>  _responses = new HashMap<>();

    /**
     * Returns the snippets.
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
     * Returns the snippet out for a snippet.
     */
    public Response getResponseForRequest(Request aRequest)
    {
        Response response = _responses.get(aRequest);
        if (response != null)
            return response;

        response = createResponseForRequest(aRequest);
        _responses.put(aRequest, response);
        return response;
    }

    /**
     * Returns the snippet out for a snippet.
     */
    protected Response createResponseForRequest(Request aRequest)
    {
        Response response = new Response();

        String text = aRequest.getText();
        KeyChain keyChain = KeyChain.getKeyChain(text);
        String outString = KeyChain.getStringValue(new Object(), keyChain);
        response.setText(outString);
        return response;
    }
}
