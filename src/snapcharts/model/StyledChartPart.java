package snapcharts.model;
import snap.gfx.*;
import snap.util.MathUtils;
import snap.util.XMLArchiver;
import snap.util.XMLElement;
import java.util.Objects;

/**
 * This ChartPart subclass represents chart parts that have style attributes and are directly painted in
 * the chart (Chart, Header, Axis, Legend, etc.).
 */
public class StyledChartPart extends ChartPart {

    // The border
    private Border  _border = (Border) getPropDefault(Border_Prop);

    // The Fill
    private Paint  _fill = (Paint) getPropDefault(Fill_Prop);

    // The Effect
    private Effect  _effect = DEFAULT_EFFECT;

    // The Font
    private Font  _font;

    // The opacity
    private double  _opacity = DEFAULT_OPACTIY;

    // Constants for properties
    public static final String Border_Prop = "Border";
    public static final String Fill_Prop = "Fill";
    public static final String Effect_Prop = "Effect";
    public static final String Opacity_Prop = "Opacity";
    public static final String Font_Prop = "Font";

    // Constants for defaults
    public static final Border DEFAULT_BORDER = null;
    public static final Paint DEFAULT_FILL = null;
    public static final Effect DEFAULT_EFFECT = null;
    public static final double DEFAULT_OPACTIY = 1;
    public static final Font DEFAULT_FONT = Font.Arial12;

    /**
     * Constructor.
     */
    public StyledChartPart()
    {
        super();
    }

    /**
     * Returns the ChartPart border.
     */
    public Border getBorder()  { return _border; }

    /**
     * Sets the ChartPart border.
     */
    public void setBorder(Border aBorder)
    {
        if (Objects.equals(aBorder, _border)) return;
        firePropChange(Border_Prop, _border, _border=aBorder);
    }

    /**
     * Sets the ChartPart border.
     */
    public void setBorder(Color aColor, double aBorderWidth)
    {
        setBorder(Border.createLineBorder(aColor, aBorderWidth));
    }

    /**
     * Returns the fill of ChartPart.
     */
    public Paint getFill()  { return _fill; }

    /**
     * Sets the fill of ChartPart.
     */
    public void setFill(Paint aPaint)
    {
        if (Objects.equals(aPaint, _fill)) return;
        firePropChange(Fill_Prop, _fill, _fill = aPaint);
    }

    /**
     * Returns the ChartPart effect.
     */
    public Effect getEffect()  { return _effect; }

    /**
     * Sets the ChartPart effect.
     */
    public void setEffect(Effect anEffect)
    {
        if (Objects.equals(anEffect, _effect)) return;
        firePropChange(Effect_Prop, _effect, _effect=anEffect);
    }

    /**
     * Returns the ChartPart opacity.
     */
    public double getOpacity()  { return _opacity; }

    /**
     * Sets the ChartPart opacity.
     */
    public void setOpacity(double aValue)
    {
        if (MathUtils.equals(aValue, _opacity)) return;
        firePropChange(Opacity_Prop, _opacity, _opacity=aValue);
    }

    /**
     * Returns the font of ChartPart.
     */
    public Font getFont()
    {
        // If font explicitly set, just return
        if (_font != null)
            return _font;

        // Get font from prop-default and return
        Font font = (Font) getPropDefault(Font_Prop);
        return font;
    }

    /**
     * Sets the ChartPart font.
     */
    public void setFont(Font aFont)
    {
        if (Objects.equals(aFont, _font)) return;
        firePropChange(Font_Prop, _font, _font = aFont);
    }

    /**
     * Returns the value for given key.
     */
    @Override
    public Object getPropDefault(String aPropName)
    {
        // Handle properties
        switch (aPropName) {

            case Border_Prop: return DEFAULT_BORDER;

            case Fill_Prop: return DEFAULT_FILL;

            case Font_Prop: return DEFAULT_FONT;

            default: return super.getPropDefault(aPropName);
        }
    }

    /**
     * Archival.
     */
    @Override
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Get new element with class name
        XMLElement e = super.toXML(anArchiver);

        // Archive name
        if (getName()!=null && getName().length()>0)
            e.add(Name_Prop, getName());

        // Return element
        return e;
    }

    /**
     * Unarchival.
     */
    @Override
    public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Do normal version
        super.fromXML(anArchiver, anElement);

        // Unarchive Name
        if (anElement.hasAttribute(Name_Prop))
            setName(anElement.getAttributeValue(Name_Prop));

        // Return this part
        return this;
    }
}
