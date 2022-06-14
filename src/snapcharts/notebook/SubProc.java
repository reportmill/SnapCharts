/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import snap.util.KeyChain;
import snap.util.SnapUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is base class to process specific kinds of requests (Math, Data, Chart, etc.).
 */
public class SubProc {

    // The parent Processor
    private Processor  _processor;

    // A map holding values
    protected Map<String,Double> _variables = new HashMap<>();

    /**
     * Constructor.
     */
    public SubProc(Processor aProcessor)
    {
        _processor = aProcessor;
        _variables = aProcessor._variables;
    }

    /**
     * Returns the Processor.
     */
    public Processor getProcessor()  { return _processor; }

    /**
     * Returns the Notebook.
     */
    public Notebook getNotebook()  { return _processor.getNotebook(); }

    /**
     * Handles processing functions supported by this class.
     */
    public Object getValueFunctionCall(Object aRoot, Object anObj, String functionName, KeyChain argListKC)
    {
        return null;
    }

    /**
     * Returns the double value for arg at given index in given arg list key chain.
     */
    public Double getDoubleValueForArgListArg(Object anObj, KeyChain argListKeyChain, int anIndex)
    {
        int argCount = argListKeyChain.getChildCount();
        KeyChain argKeyChain = argCount > anIndex ? argListKeyChain.getChildKeyChain(anIndex) : null;
        Object value = KeyChain.getValue(anObj, argKeyChain);
        return SnapUtils.getDouble(value);
    }
}
