/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import snap.util.KeyChain;

/**
 * This class processes math requests.
 */
public class SubProcMath extends SubProc {

    /**
     * Constructor.
     */
    public SubProcMath(Processor aProcessor)
    {
        super(aProcessor);
    }

    /**
     * Handles processing functions supported by this class.
     */
    public Object getValueFunctionCall(Object aRoot, Object anObj, String functionName, KeyChain argListKC)
    {
        switch (functionName) {

            // Handle Sin, Cos
            case "sin": return sinFunc(anObj, argListKC);
            case "cos": return cosFunc(anObj, argListKC);
        }

        return null;
    }

    /**
     * A Sin function takes KeyChain.
     */
    public Object sinFunc(Object anObj, KeyChain aKeyChain)
    {
        KeyChain arg0 = aKeyChain.getChildKeyChain(0);
        double value = KeyChain.getDoubleValue(anObj, arg0);
        double sinVal = Math.sin(value);
        return sinVal;
    }

    /**
     * A Sin function takes KeyChain.
     */
    public Object cosFunc(Object anObj, KeyChain aKeyChain)
    {
        KeyChain arg0 = aKeyChain.getChildKeyChain(0);
        double value = KeyChain.getDoubleValue(anObj, arg0);
        double sinVal = Math.cos(value);
        return sinVal;
    }
}
