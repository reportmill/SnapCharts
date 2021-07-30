/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;
import snap.gfx.Color;
import snap.util.XMLArchiver;
import snap.util.XMLElement;

/**
 * This StyledChartPart subclass holds style attributes for symbols.
 */
public class SymbolStyle extends StyledChartPart {

    // The DataStyle that holds this SymbolStyle
    private DataStyle  _dataStyle;

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
    public SymbolStyle(DataStyle aDataStyle)
    {
        super();
        _dataStyle = aDataStyle;
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
        return _dataStyle.getLineColor();
    }

    /**
     * Returns the default fill color.
     */
    private Color getDefaultFillColor()
    {
        if (getLineWidth() > 0 && !isLineColorSet())
            return Color.WHITE;
        return _dataStyle.getDefaultLineColor();
    }

    /**
     * Override to prevent client code from using border instead of line props.
     */
    @Override
    public boolean isBorderSupported()  { return false; }

    /**
     * Override to provide SymbolStyle defaults.
     */
    @Override
    public Object getPropDefault(String aPropName)
    {
        switch (aPropName) {

            // LineColor_Prop
            case LineColor_Prop: return getDefaultLineColor();

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
        e.setName("SymbolStyle");

        // Archive SymbolSize
        if (getSymbolSize() != getPropDefaultInt(SymbolSize_Prop))
            e.add(SymbolSize_Prop, getSymbolSize());

        // Archive SymbolId
        if (getSymbolId() != 0)
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
