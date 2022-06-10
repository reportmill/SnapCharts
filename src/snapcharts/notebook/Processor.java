/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import snap.util.KeyChain;
import snap.util.SnapUtils;
import java.util.HashMap;
import java.util.Map;

/**
 * This class processes request expressions.
 */
public class Processor implements KeyChain.FunctionHandler {

    // The Notebook this processor works with
    private Notebook  _notebook;

    // A map holding values
    protected Map<String,Double>  _variables = new HashMap<>();

    // The Math Processor
    private SubProcMath  _mathProcessor;

    // The Data Processor
    protected SubProcData  _dataProcessor;

    // The Plot Processor
    protected SubProcPlot  _plotProcessor;

    // The UI Processor
    private SubProcUI _uiProcessor;

    /**
     * Constructor.
     */
    public Processor(Notebook aNotebook)
    {
        _notebook = aNotebook;

        // Create sub processors
        _mathProcessor = new SubProcMath(this);
        _dataProcessor = new SubProcData(this);
        _plotProcessor = new SubProcPlot(this);
        _uiProcessor = new SubProcUI(this);
    }

    /**
     * Returns the Notebook this processor works with.
     */
    public Notebook getNotebook()  { return _notebook; }

    /**
     * Returns the snippet out for a snippet.
     */
    public Response createResponseForRequest(Request aRequest)
    {
        // Get Request.Text as String and KeyChain
        String text = aRequest.getText();
        KeyChain keyChain = KeyChain.getKeyChain(text);

        // Process keyChain
        Object responseValue = getValue(this, keyChain);

        // Create Response and set value
        Response response = new Response();
        response.setValue(responseValue);

        // Return
        return response;
    }

    /**
     * Returns a value for given object and keychain.
     */
    public Object getValue(Object anObj, KeyChain aKC)
    {
        // Register as FunctionHandler
        KeyChain.setFunctionHandler(this);

        // Get value
        Object value = KeyChain.getValue(anObj, aKC);

        // Return
        return value;
    }

    /**
     * Returns the string value of a keychain.
     */
    public String getStringValue(Object anObj, KeyChain aKC)
    {
        // Get value
        Object value;
        try { value = getValue(anObj, aKC); }

        // If exception, set as response
        catch (Exception e) {
            value = e.toString();
        }

        // Convert to string and return
        String str = value != null ? SnapUtils.stringValue(value) : "null";
        return str;
    }

    /**
     * Handles FunctionCall KeyChains.
     */
    @Override
    public Object getValueFunctionCall(Object aRoot, Object anObj, KeyChain aKeyChain)
    {
        // Get function name (lowercase) and arg list
        String functionName = aKeyChain.getChildString(0).toLowerCase();
        KeyChain argList = aKeyChain.getChildKeyChain(1);

        // Dispatch for function name
        switch (functionName) {

            // Handle text
            case "text": return text(anObj, argList);
        }

        // Try Math Processor
        Object value = _mathProcessor.getValueFunctionCall(aRoot, anObj, functionName, argList);
        if (value != null)
            return value;

        // Try Data Processor
        value = _dataProcessor.getValueFunctionCall(aRoot, anObj, functionName, argList);
        if (value != null)
            return value;

        // Try Plot Processor
        value = _plotProcessor.getValueFunctionCall(aRoot, anObj, functionName, argList);
        if (value != null)
            return value;

        // Try UI Processor
        value = _uiProcessor.getValueFunctionCall(aRoot, anObj, functionName, argList);
        if (value != null)
            return value;

        // Complain and return null since function not found
        System.err.println("Processor.getValueFunctionCall: Unknown function: " + functionName);
        return null;
    }

    /**
     * Returns the nested keyChain as a string.
     */
    public String text(Object anObj, KeyChain aKeyChain)
    {
        KeyChain keyChain = aKeyChain.getChildKeyChain(0);
        Object value = getValue(anObj, keyChain);
        String str = ProcessorUtils.getStringForValue(value);
        return str;
    }
}
