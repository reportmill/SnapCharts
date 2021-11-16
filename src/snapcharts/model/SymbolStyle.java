/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;
import snap.gfx.Color;
import snap.util.PropDefaults;
import snap.util.SnapUtils;
import snap.util.XMLArchiver;
import snap.util.XMLElement;

/**
 * This ChartPart subclass holds style attributes for symbols.
 */
public class SymbolStyle extends ChartPart {

    // The DataStyle that holds this SymbolStyle
    private TraceStyle _traceStyle;

    // The Symbol Size
    private int  _symbolSize = DEFAULT_SYMBOL_SIZE;

    // The Symbol Id
    private int  _symbolId;

    // The cached symbol
    private Symbol  _symbol;

    // Constants for properties
    public static final String SymbolSize_Prop = "SymbolSize";
    public static final String SymbolId_Prop = "SymbolId";

    // Constants for property defaults
    public static final int DEFAULT_SYMBOL_SIZE = 8;
    public static final Color DEFAULT_SYMBOL_FILL = null;
    public static final Color DEFAULT_SYMBOL_BORDER_COLOR = null; //Color.BLACK;
    public static final int DEFAULT_SYMBOL_BORDER_WIDTH = 0;

    /**
     * Constructor.
     */
    public SymbolStyle(TraceStyle aTraceStyle)
    {
        super();
        _traceStyle = aTraceStyle;
    }

    /**
     * Returns the Symbol size.
     */
    public int getSymbolSize()  { return _symbolSize; }

    /**
     * Sets the Symbol size.
     */
    public void setSymbolSize(int aValue)
    {
        if (aValue == getSymbolSize()) return;
        firePropChange(SymbolSize_Prop, _symbolSize, _symbolSize = aValue);
        _symbol = null;
    }

    /**
     * Returns the Symbol Id.
     */
    public int getSymbolId()  { return _symbolId; }

    /**
     * Sets the Symbol Id.
     */
    public void setSymbolId(int aValue)
    {
        if (aValue == getSymbolId()) return;
        firePropChange(SymbolId_Prop, _symbolId, _symbolId = aValue);
        _symbol = null;
    }

    /**
     * Returns the Symbol.
     */
    public Symbol getSymbol()
    {
        // If already set, just return
        if (_symbol != null) return _symbol;

        // Get, set, return
        Symbol symbol = Symbol.getSymbolForId(_symbolId).copyForSize(_symbolSize);
        return _symbol = symbol;
    }

    /**
     * Returns the default fill color.
     */
    private Color getDefaultLineColor()
    {
        return _traceStyle.getLineColor();
    }

    /**
     * Returns the default fill color.
     */
    private Color getDefaultFillColor()
    {
        if (getLineWidth() > 0 && !isLineColorSet())
            return Color.WHITE;
        return _traceStyle.getDefaultLineColor();
    }

    /**
     * Override to prevent client code from using border instead of line props.
     */
    @Override
    public boolean isBorderSupported()  { return false; }

    /**
     * Override to register props.
     */
    @Override
    protected void initPropDefaults(PropDefaults aPropDefaults)
    {
        // Do normal version
        super.initPropDefaults(aPropDefaults);

        // Add Props
        aPropDefaults.addProps(SymbolSize_Prop, SymbolId_Prop);
    }

    /**
     * Returns the prop value for given key.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        // Handle properties
        switch (aPropName) {

            // SymbolSize, SymbolId
            case SymbolSize_Prop: return getSymbolSize();
            case SymbolId_Prop: return getSymbolId();

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

            // SymbolSize, SymbolId
            case SymbolSize_Prop: setSymbolSize(SnapUtils.intValue(aValue)); break;
            case SymbolId_Prop: setSymbolId(SnapUtils.intValue(aValue)); break;

            // Handle super class properties (or unknown)
            default: super.setPropValue(aPropName, aValue); break;
        }
    }

    /**
     * Override to provide SymbolStyle defaults.
     */
    @Override
    public Object getPropDefault(String aPropName)
    {
        switch (aPropName) {

            // LineColor_Prop
            case LineColor_Prop: return getDefaultLineColor();

            // SymbolSize, SymbolId
            case SymbolSize_Prop: return DEFAULT_SYMBOL_SIZE;
            case SymbolId_Prop: return 0;

            // Fill_Prop
            case Fill_Prop: return getDefaultFillColor();

            // Do normal version
            default: return super.getPropDefault(aPropName);
        }
    }

    /**
     * Archival.
     */
    @Override
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Archive basic attributes
        XMLElement e = super.toXML(anArchiver);

        // Archive SymbolSize
        if (!isPropDefault(SymbolSize_Prop))
            e.add(SymbolSize_Prop, getSymbolSize());

        // Archive SymbolId
        if (!isPropDefault(SymbolSize_Prop))
            e.add(SymbolId_Prop, getSymbolId());

        // Return xml
        return e;
    }

    /**
     * Unarchival.
     */
    @Override
    public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive basic attributes
        super.fromXML(anArchiver, anElement);


        // Unarchive SymbolSize
        if (anElement.hasAttribute(SymbolSize_Prop))
            setSymbolSize(anElement.getAttributeIntValue(SymbolSize_Prop));

        // Unarchive SymbolId
        if (anElement.hasAttribute(SymbolId_Prop))
            setSymbolId(anElement.getAttributeIntValue(SymbolId_Prop, 0));

        // Return
        return this;
    }
}
