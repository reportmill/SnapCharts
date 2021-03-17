package snapcharts.model;
import snap.util.XMLArchiver;
import snap.util.XMLElement;

/**
 * A class to represent properties for a specific ChartType.
 */
public class ChartStyle extends ChartPart {

    // Whether to show line
    private boolean  _showLine = true;

    // The line width
    private int  _lineWidth = 1;

    // Whether to show symbols
    private boolean  _showSymbols;

    // Constants for properties
    public static final String ShowLine_Prop = "ShowLine";
    public static final String LineWidth_Prop = "LineWidth";
    public static final String ShowSymbols_Prop = "ShowSymbols";

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

        // Archive ShowSymbols
        if (isShowSymbols())
            e.add(ShowSymbols_Prop, true);

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

        // Unarchive ShowSymbols
        setShowSymbols(anElement.getAttributeBoolValue(ShowSymbols_Prop, false));

        // Return this part
        return this;
    }
}
