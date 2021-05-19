package snapcharts.model;
import snap.gfx.Color;
import snap.gfx.Font;
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

    // The line color
    private Color  _lineColor;

    // The line width
    private int  _lineWidth = DEFAULT_LINE_WIDTH;

    // The line dash
    private double[]  _lineDash = DEFAULT_LINE_DASH;

    // The color to paint the data area
    private Color  _fillColor;

    // The FillMode
    private FillMode  _fillMode = FillMode.None;

    // Whether to show symbols
    private boolean  _showSymbols;

    // The color to paint symbols
    private Color  _symbolColor;

    // The Symbol Size
    private int  _symbolSize = DEFAULT_SYMBOL_SIZE;

    // The color to paint symbol borders (if border width > 0)
    private Color  _symbolBorderColor = DEFAULT_SYMBOL_BORDER_COLOR;

    // The Symbol border width
    private int  _symbolBorderWidth = DEFAULT_SYMBOL_BORDER_WIDTH;

    // The Symbol Id
    private int  _symbolId;

    // Whether to show data tags
    private boolean  _showTags;

    // The font to paint tag background
    private Font  _tagFont = DEFAULT_TAG_FONT;

    // The color to paint tag background
    private Color  _tagColor = DEFAULT_TAG_COLOR;

    // The color to paint data tag box border
    private Color  _tagBorderColor = DEFAULT_TAG_BORDER_COLOR;

    // The data tag box border width
    private int  _tagBorderWidth = DEFAULT_TAG_BORDER_WIDTH;

    // The cached symbol
    private Symbol  _symbol;

    // Constants for properties
    public static final String ShowLine_Prop = "ShowLine";
    public static final String LineColor_Prop = "LineColor";
    public static final String LineWidth_Prop = "LineWidth";
    public static final String LineDash_Prop = "LineDash";
    public static final String FillColor_Prop = "FillColor";
    public static final String FillMode_Prop = "FillMode";
    public static final String ShowSymbols_Prop = "ShowSymbols";
    public static final String SymbolColor_Prop = "SymbolColor";
    public static final String SymbolSize_Prop = "SymbolSize";
    public static final String SymbolId_Prop = "SymbolId";
    public static final String SymbolBorderColor_Prop = "SymbolBorderColor";
    public static final String SymbolBorderWidth_Prop = "SymbolBorderWidth";
    public static final String ShowTags_Prop = "ShowTags";
    public static final String TagFont_Prop = "TagFont";
    public static final String TagColor_Prop = "TagColor";
    public static final String TagBorderColor_Prop = "TagBorderColor";
    public static final String TagBorderWidth_Prop = "TagBorderWidth";

    // Constants for property defaults
    public static final int DEFAULT_LINE_WIDTH = 1;
    public static final double[] DEFAULT_LINE_DASH = null;
    public static final FillMode DEFAULT_FILL_MODE = FillMode.None;
    public static final int DEFAULT_SYMBOL_SIZE = 8;
    public static final Color DEFAULT_SYMBOL_BORDER_COLOR = Color.BLACK;
    public static final int DEFAULT_SYMBOL_BORDER_WIDTH = 0;
    public static final Font DEFAULT_TAG_FONT = Font.Arial10;
    public static final Color DEFAULT_TAG_COLOR = null;
    public static final Color DEFAULT_TAG_BORDER_COLOR = null;
    public static final int DEFAULT_TAG_BORDER_WIDTH = 0;

    // Constant for how dataset area should be filled
    public enum FillMode { None, ToZeroY, ToNextY, ToZeroX, ToNextX, ToSelf, ToNext };

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
     * Returns whether to show line for DataSet.
     */
    public boolean isShowLine()
    {
        return _showLine;
    }

    /**
     * Sets whether to show line for DataSet.
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
     * Returns whether to paint area for data.
     */
    public boolean isShowArea()
    {
        return _fillMode != FillMode.None;
    }

    /**
     * Sets whether to paint area for data.
     */
    public void setShowArea(boolean aValue)
    {
        FillMode fillMode = aValue ? FillMode.ToZeroY : FillMode.None;
        setFillMode(fillMode);
    }

    /**
     * Returns the color to fill the data area.
     */
    public Color getFillColor()
    {
        // If set, just return
        if (_fillColor != null) return _fillColor;

        // Get from LineColor, half transparent
        return getLineColor().copyForAlpha(.5);
    }

    /**
     * Sets the color to fill the data area.
     */
    public void setFillColor(Color aColor)
    {
        if (Objects.equals(aColor, _fillColor)) return;
        firePropChange(FillColor_Prop, _fillColor, _fillColor = aColor);
    }

    /**
     * Returns whether fill color is explicitly set.
     */
    public boolean isFillColorSet()  { return _fillColor != null; }

    /**
     * Returns the FillMode (how/whether to paint the data area).
     */
    public FillMode getFillMode()  { return _fillMode; }

    /**
     * Sets the FillMode (how/whether to paint the data area).
     */
    public void setFillMode(FillMode aFillMode)
    {
        if (aFillMode == _fillMode) return;
        firePropChange(FillMode_Prop, _fillMode, _fillMode = aFillMode);
    }

    /**
     * Returns whether to show symbols for DataSet.
     */
    public boolean isShowSymbols()
    {
        return _showSymbols;
    }

    /**
     * Sets whether to show symbols for DataSet.
     */
    public void setShowSymbols(boolean aValue)
    {
        if (aValue == isShowSymbols()) return;
        firePropChange(ShowSymbols_Prop, _showSymbols, _showSymbols = aValue);
    }

    /**
     * Returns the color to fill symbols.
     */
    public Color getSymbolColor()
    {
        // If set, just return
        if (_symbolColor != null) return _symbolColor;

        // Get from LineColor
        return getLineColor();
    }

    /**
     * Sets the color to fill symbols.
     */
    public void setSymbolColor(Color aColor)
    {
        if (Objects.equals(aColor, _symbolColor)) return;
        firePropChange(SymbolColor_Prop, _symbolColor, _symbolColor = aColor);
        _symbol = null;
    }

    /**
     * Returns whether symbol color is explicitly set.
     */
    public boolean isSymbolColorSet()  { return _symbolColor != null; }

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
     * Returns the color to stroke symbols.
     */
    public Color getSymbolBorderColor()
    {
        return _symbolBorderColor;
    }

    /**
     * Sets the color to stroke symbols.
     */
    public void setSymbolBorderColor(Color aColor)
    {
        if (Objects.equals(aColor, _symbolBorderColor)) return;
        firePropChange(SymbolBorderColor_Prop, _symbolBorderColor, _symbolBorderColor = aColor);
    }

    /**
     * Returns the Symbol border width.
     */
    public int getSymbolBorderWidth()  { return _symbolBorderWidth; }

    /**
     * Sets the Symbol border width.
     */
    public void setSymbolBorderWidth(int aValue)
    {
        if (aValue == getSymbolBorderWidth()) return;
        firePropChange(SymbolBorderWidth_Prop, _symbolBorderWidth, _symbolBorderWidth = aValue);
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
     * Returns whether to show data tags for DataSet.
     */
    public boolean isShowTags()
    {
        return _showTags;
    }

    /**
     * Sets whether to show data tags for DataSet.
     */
    public void setShowTags(boolean aValue)
    {
        if (aValue == isShowTags()) return;
        firePropChange(ShowTags_Prop, _showTags, _showTags = aValue);
    }

    /**
     * Returns the font to paint data tag text.
     */
    public Font getTagFont()
    {
        return _tagFont;
    }

    /**
     * Sets the font to paint data tag text.
     */
    public void setTagFont(Font aFont)
    {
        if (Objects.equals(aFont, _tagFont)) return;
        firePropChange(TagFont_Prop, _tagFont, _tagFont = aFont);
    }

    /**
     * Returns the color to fill data tag boxes.
     */
    public Color getTagColor()
    {
        return _tagColor;
    }

    /**
     * Sets the color to fill data tag boxes.
     */
    public void setTagColor(Color aColor)
    {
        if (Objects.equals(aColor, _tagColor)) return;
        firePropChange(TagColor_Prop, _tagColor, _tagColor = aColor);
    }

    /**
     * Returns the border color for data tag boxes.
     */
    public Color getTagBorderColor()
    {
        // If set, just return
        if (_tagBorderColor != null) return _tagBorderColor;

        // User line color
        return getLineColor();
    }

    /**
     * Sets the border color for data tag boxes.
     */
    public void setTagBorderColor(Color aColor)
    {
        if (Objects.equals(aColor, _tagBorderColor)) return;
        firePropChange(TagBorderColor_Prop, _tagBorderColor, _tagBorderColor = aColor);
    }

    /**
     * Returns whether tag border color is explicitly set.
     */
    public boolean isTagBorderColorSet()  { return _tagBorderColor != null; }

    /**
     * Returns the border width for data tag boxes.
     */
    public int getTagBorderWidth()  { return _tagBorderWidth; }

    /**
     * Sets the border width for data tag boxes.
     */
    public void setTagBorderWidth(int aValue)
    {
        if (aValue == getTagBorderWidth()) return;
        firePropChange(TagBorderWidth_Prop, _tagBorderWidth, _tagBorderWidth = aValue);
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

        // Archive FillColor, FillMode
        if (isFillColorSet())
            e.add(FillColor_Prop, getFillColor().toHexString());
        if (getFillMode() != DEFAULT_FILL_MODE)
            e.add(FillMode_Prop, getFillMode());

        // Archive ShowSymbols
        if (isShowSymbols())
            e.add(ShowSymbols_Prop, true);

        // Archive SymbolColor, SymbolSize
        if (isSymbolColorSet())
            e.add(SymbolColor_Prop, getSymbolColor().toHexString());
        if (getSymbolSize() != getPropDefaultInt(SymbolSize_Prop))
            e.add(SymbolSize_Prop, getSymbolSize());

        // Archive SymbolId
        if (getSymbolId() != 0)
            e.add(SymbolId_Prop, getSymbolId());

        // Archive SymbolBorderColor, SymbolBorderWidth
        if (!Objects.equals(getSymbolBorderColor(), DEFAULT_SYMBOL_BORDER_COLOR))
            e.add(SymbolBorderColor_Prop, getSymbolBorderColor().toHexString());
        if (getSymbolBorderWidth() != DEFAULT_SYMBOL_BORDER_WIDTH)
            e.add(SymbolBorderWidth_Prop, getSymbolBorderWidth());

        // Archive ShowTags
        if (isShowTags())
            e.add(ShowTags_Prop, true);

        // Archive TagFont
        if (!Objects.equals(getTagFont(), DEFAULT_TAG_FONT)) {
            XMLElement tagFontXML = getTagFont().toXML(anArchiver);
            tagFontXML.setName(TagFont_Prop);
            e.addElement(tagFontXML);
        }

        // Archive TagColor
        if (!Objects.equals(getTagColor(), DEFAULT_TAG_COLOR))
            e.add(TagColor_Prop, getTagColor().toHexString());

        // Archive TagBorderColor, TagBorderWidth
        if (isTagBorderColorSet())
            e.add(TagBorderColor_Prop, getTagBorderColor().toHexString());
        if (getTagBorderWidth() != DEFAULT_TAG_BORDER_WIDTH)
            e.add(TagBorderWidth_Prop, getTagBorderWidth());

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

        // Unarchive FillColor, FillMode
        if (anElement.hasAttribute(FillColor_Prop)) {
            Color color = Color.get('#' + anElement.getAttributeValue(FillColor_Prop));
            setFillColor(color);
        }
        if (anElement.hasAttribute(FillMode_Prop))
            setFillMode(anElement.getAttributeEnumValue(FillMode_Prop, FillMode.class, DEFAULT_FILL_MODE));

        // Unarchive ShowSymbols
        if (anElement.hasAttribute(ShowSymbols_Prop))
            setShowSymbols(anElement.getAttributeBoolValue(ShowSymbols_Prop));

        // Unarchive SymbolColor, SymbolSize
        if (anElement.hasAttribute(SymbolColor_Prop)) {
            Color color = Color.get('#' + anElement.getAttributeValue(SymbolColor_Prop));
            setSymbolColor(color);
        }
        if (anElement.hasAttribute(SymbolSize_Prop))
            setSymbolSize(anElement.getAttributeIntValue(SymbolSize_Prop));

        // Unarchive SymbolId
        setSymbolId(anElement.getAttributeIntValue(SymbolId_Prop, 0));

        // Unarchive SymbolBorderColor, SymbolBorderWidth
        if (anElement.hasAttribute(SymbolBorderColor_Prop)) {
            Color color = Color.get('#' + anElement.getAttributeValue(SymbolBorderColor_Prop));
            setSymbolBorderColor(color);
        }
        if (anElement.hasAttribute(SymbolBorderWidth_Prop))
            setSymbolBorderWidth(anElement.getAttributeIntValue(SymbolBorderWidth_Prop));

        // Unarchive ShowTags
        if (anElement.hasAttribute(ShowTags_Prop))
            setShowTags(anElement.getAttributeBoolValue(ShowTags_Prop));

        // Unarchive TagFont
        XMLElement tagFontXML = anElement.getElement(TagFont_Prop);
        if (tagFontXML != null) {
            Font font = (Font) new Font().fromXML(anArchiver, tagFontXML);
            setTagFont(font);
        }

        // Unarchive TagColor
        if (anElement.hasAttribute(TagColor_Prop)) {
            Color color = Color.get('#' + anElement.getAttributeValue(TagColor_Prop));
            setTagColor(color);
        }

        // Unarchive TagBorderColor, TagBorderWidth
        if (anElement.hasAttribute(TagBorderColor_Prop)) {
            Color color = Color.get('#' + anElement.getAttributeValue(TagBorderColor_Prop));
            setTagBorderColor(color);
        }
        if (anElement.hasAttribute(TagBorderWidth_Prop))
            setTagBorderWidth(anElement.getAttributeIntValue(TagBorderWidth_Prop));

        // Return this part
        return this;
    }
}
