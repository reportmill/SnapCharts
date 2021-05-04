package snapcharts.model;
import snap.util.XMLArchiver;
import snap.util.XMLElement;

/**
 * A class to represent properties to render data for a specific ChartType.
 */
public class DataStyle extends ChartPart {

    // Whether to show line
    private boolean  _showLine = true;

    // The line width
    private int  _lineWidth = 1;

    // Whether to show symbols
    private boolean  _showSymbols;

    // The Symbol Id
    private int  _symbolId;

    // The cached symbol
    private Symbol  _symbol;

    // Constants for properties
    public static final String ShowLine_Prop = "ShowLine";
    public static final String LineWidth_Prop = "LineWidth";
    public static final String ShowSymbols_Prop = "ShowSymbols";
    public static final String SymbolId_Prop = "SymbolId";

    /**
     * Returns whether to show line for this DataSet.
     */
    public boolean isShowLine()
    {
        return _showLine;
    }

    /**
     * Sets whether to show line for this DataSet.
     */
    public void setShowLine(boolean aValue)
    {
        if (aValue == isShowLine()) return;
        firePropChange(ShowLine_Prop, _showLine, _showLine = aValue);
    }

    /**
     * Returns line width.
     */
    public int getLineWidth()
    {
        return _lineWidth;
    }

    /**
     * Sets line width.
     */
    public void setLineWidth(int aValue)
    {
        if (aValue == getLineWidth()) return;
        firePropChange(LineWidth_Prop, _lineWidth, _lineWidth = aValue);
    }

    /**
     * Returns whether to show symbols for this DataSet.
     */
    public boolean isShowSymbols()
    {
        return _showSymbols;
    }

    /**
     * Sets whether to show symbols for this DataSet.
     */
    public void setShowSymbols(boolean aValue)
    {
        if (aValue == isShowSymbols()) return;
        firePropChange(ShowSymbols_Prop, _showSymbols, _showSymbols = aValue);
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
        Symbol symbol = Symbol.getSymbolForId(_symbolId);
        return _symbol = symbol;
    }

    /**
     * Archival.
     */
    @Override
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Archive basic attributes
        XMLElement e = super.toXML(anArchiver);

        // Archive ShowLine, LineWidth
        if (!isShowLine())
            e.add(ShowLine_Prop, false);
        if (getLineWidth() != 1)
            e.add(LineWidth_Prop, getLineWidth());

        // Archive ShowSymbols, SymbolId
        if (isShowSymbols())
            e.add(ShowSymbols_Prop, true);
        if (getSymbolId() != 0)
            e.add(SymbolId_Prop, getSymbolId());

        // Return element
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

        // Unarchive ShowLine, LineWidth
        if (anElement.hasAttribute(ShowLine_Prop))
            setShowLine(anElement.getAttributeBoolValue(ShowLine_Prop));
        if (anElement.hasAttribute(LineWidth_Prop))
            setLineWidth(anElement.getAttributeIntValue(ShowLine_Prop));

        // Unarchive ShowSymbols, SymbolId
        setShowSymbols(anElement.getAttributeBoolValue(ShowSymbols_Prop, false));
        setSymbolId(anElement.getAttributeIntValue(SymbolId_Prop, 0));

        // Return this part
        return this;
    }
}
