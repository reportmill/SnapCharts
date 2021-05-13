package snapcharts.model;
import snap.gfx.Color;
import snap.gfx.Stroke;
import snap.util.ArrayUtils;
import snap.util.XMLArchiver;
import snap.util.XMLElement;
import java.util.Objects;

/**
 * A class to represent properties to render data for a specific ChartType.
 */
public class DataStyle extends ChartPart {

    // The DataSet that owns the this style
    protected ChartPart  _parent;

    // Whether to show line
    private boolean  _showLine = true;

    // The line width
    private int  _lineWidth = DEFAULT_LINE_WIDTH;

    // The line dash
    private double[]  _lineDash = DEFAULT_LINE_DASH;

    // The line color
    private Color  _lineColor;

    // Whether to show symbols
    private boolean  _showSymbols;

    // The Symbol Id
    private int  _symbolId;

    // The Symbol Size
    private int  _symbolSize = DEFAULT_SYMBOL_SIZE;

    // The cached symbol
    private Symbol  _symbol;

    // Constants for properties
    public static final String ShowLine_Prop = "ShowLine";
    public static final String LineWidth_Prop = "LineWidth";
    public static final String LineDash_Prop = "LineDash";
    public static final String LineColor_Prop = "LineColor";
    public static final String ShowSymbols_Prop = "ShowSymbols";
    public static final String SymbolId_Prop = "SymbolId";
    public static final String SymbolSize_Prop = "SymbolSize";

    // Constants for property defaults
    public static final int DEFAULT_LINE_WIDTH = 1;
    public static final double[] DEFAULT_LINE_DASH = null;
    public static final int DEFAULT_SYMBOL_SIZE = 8;

    /**
     * Constructor.
     */
    public DataStyle()
    {
        super();
    }

    /**
     * Override to return DataSet.
     */
    @Override
    public ChartPart getParent()
    {
        return _parent;
    }

    /**
     * Sets the parent.
     */
    protected void setParent(ChartPart aParent)
    {
        _parent = aParent;
    }

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
     * Returns the line color.
     */
    public Color getLineColor()
    {
        // If set, just return
        if (_lineColor != null) return _lineColor;

        // Get from DataSet
        ChartPart parent = getParent();
        if (parent instanceof DataSet) {
            DataSet dataSet = (DataSet) parent;
            int index = dataSet.getIndex();
            return getColorMapColor(index);
        }

        // Shouldn't get here
        return Color.BLACK;
    }

    /**
     * Sets the line color.
     */
    public void setLineColor(Color aColor)
    {
        if (Objects.equals(aColor, _lineColor)) return;
        firePropChange(LineColor_Prop, _lineColor, _lineColor = aColor);
    }

    /**
     * Returns whether line color is explicitly set.
     */
    public boolean isLineColorSet()  { return _lineColor != null; }

    /**
     * Returns the color map color at index.
     */
    public Color getColorMapColor(int anIndex)
    {
        Chart chart = getChart();
        return chart.getColor(anIndex);
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
     * Returns the line dash.
     */
    public double[] getLineDash()  { return _lineDash; }

    /**
     * Sets the line dash.
     */
    public void setLineDash(double[] aDashArray)
    {
        if (ArrayUtils.equals(aDashArray, _lineDash)) return;
        firePropChange(LineDash_Prop, _lineDash, _lineDash = aDashArray);
    }

    /**
     * Returns the line stroke.
     */
    public Stroke getLineStroke()
    {
        Stroke stroke = new Stroke(_lineWidth, Stroke.Cap.Butt, Stroke.Join.Round, 10, _lineDash, 0);
        return stroke;
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
     * Override to define more defaults
     */
    @Override
    public Object getPropDefault(String aPropName)
    {
        if (aPropName == SymbolSize_Prop)
            return DEFAULT_SYMBOL_SIZE;
        return super.getPropDefault(aPropName);
    }

    /**
     * Archival.
     */
    @Override
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Archive basic attributes
        XMLElement e = super.toXML(anArchiver);

        // Archive ShowLine, LineColor
        if (!isShowLine())
            e.add(ShowLine_Prop, false);
        if (isLineColorSet())
            e.add(LineColor_Prop, getLineColor().toHexString());

        // Archive LineWidth, LineDash
        if (getLineWidth() != DEFAULT_LINE_WIDTH)
            e.add(LineWidth_Prop, getLineWidth());
        if (!ArrayUtils.equals(_lineDash, DEFAULT_LINE_DASH)) {
            String dashStr = Stroke.getDashArrayNameOrString(_lineDash);
            e.add(LineDash_Prop, dashStr);
        }

        // Archive ShowSymbols, SymbolId, SymbolSize
        if (isShowSymbols())
            e.add(ShowSymbols_Prop, true);
        if (getSymbolId() != 0)
            e.add(SymbolId_Prop, getSymbolId());
        if (getSymbolSize() != getPropDefaultInt(SymbolSize_Prop))
            e.add(SymbolSize_Prop, getSymbolSize());

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

        // Unarchive ShowLine, LineColor, LineWidth
        if (anElement.hasAttribute(ShowLine_Prop))
            setShowLine(anElement.getAttributeBoolValue(ShowLine_Prop));
        if (anElement.hasAttribute(LineColor_Prop)) {
            Color color = Color.get('#' + anElement.getAttributeValue(LineColor_Prop));
            setLineColor(color);
        }

        // Unarchive LineWidth, LineDash
        if (anElement.hasAttribute(LineWidth_Prop))
            setLineWidth(anElement.getAttributeIntValue(ShowLine_Prop));
        if (anElement.hasAttribute(LineDash_Prop)) {
            String dashStr = anElement.getAttributeValue(LineDash_Prop);
            double[] dashArray = Stroke.getDashArray(dashStr);
            setLineDash(dashArray);
        }

        // Unarchive ShowSymbols, SymbolId, SymbolSize
        setShowSymbols(anElement.getAttributeBoolValue(ShowSymbols_Prop, false));
        setSymbolId(anElement.getAttributeIntValue(SymbolId_Prop, 0));
        if (anElement.hasAttribute(SymbolSize_Prop))
            setSymbolSize(anElement.getAttributeIntValue(SymbolSize_Prop));

        // Return this part
        return this;
    }
}
