/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;
import snap.gfx.*;
import snap.util.*;
import snapcharts.doc.ChartArchiver;
import snapcharts.doc.Doc;
import java.util.*;

/**
 * Base class for parts of a chart: Axis, Area, Legend, etc.
 */
public class ChartPart extends PropObject implements XMLArchiver.Archivable {

    // The Chart
    protected Chart  _chart;

    // The parent part
    protected ChartPart  _parent;

    // The name
    private String  _name;

    // The border
    protected Border  _border;

    // The line color
    protected Color  _lineColor;

    // The line width
    protected int  _lineWidth;

    // The line dash
    protected double[]  _lineDash;

    // The Fill
    protected Paint  _fill;

    // The Effect
    protected Effect  _effect;

    // The opacity
    protected double  _opacity ;

    // The Font
    protected Font  _font;

    // The Text Fill
    protected Paint  _textFill;

    // Constants for properties
    public static final String Name_Prop = "Name";
    public static final String Border_Prop = "Border";
    public static final String LineColor_Prop = "LineColor";
    public static final String LineWidth_Prop = "LineWidth";
    public static final String LineDash_Prop = "LineDash";
    public static final String Fill_Prop = "Fill";
    public static final String Effect_Prop = "Effect";
    public static final String Opacity_Prop = "Opacity";
    public static final String Font_Prop = "Font";
    public static final String TextFill_Prop = "TextFill";

    // Constants for defaults
    public static final Border DEFAULT_BORDER = null;
    public static final Color DEFAULT_LINE_COLOR = null;
    public static final int DEFAULT_LINE_WIDTH = 0;
    public static final double[] DEFAULT_LINE_DASH = null;
    public static final Paint DEFAULT_FILL = null;
    public static final Effect DEFAULT_EFFECT = null;
    public static final double DEFAULT_OPACTIY = 1;
    public static final Font DEFAULT_FONT = Font.Arial12;
    public static final Color DEFAULT_TEXT_FILL = Color.BLACK;

    // Constant for unset border
    private static Border UNSET_BORDER = new Borders.NullBorder();

    /**
     * Constructor.
     */
    public ChartPart()
    {
        super();

        // Set default property values
        _border = UNSET_BORDER;
        _lineColor = DEFAULT_LINE_COLOR;
        _lineWidth = DEFAULT_LINE_WIDTH;
        _lineDash = DEFAULT_LINE_DASH;
        _effect = DEFAULT_EFFECT;
        _opacity = DEFAULT_OPACTIY;
        _textFill = DEFAULT_TEXT_FILL;
    }

    /**
     * Returns the Doc.
     */
    public Doc getDoc()
    {
        Chart chart = getChart();
        return chart!=null ? chart.getDoc() : null;
    }

    /**
     * Returns the chart.
     */
    public Chart getChart()
    {
        if (_chart != null)
            return _chart;
        if (_parent instanceof Chart)
            return (Chart) _parent;
        return _parent != null ? _parent.getChart() : null;
    }

    /**
     * Sets the chart.
     */
    protected void setChart(Chart aChart)
    {
        _chart = aChart;
    }

    /**
     * Returns the ChartType.
     */
    public ChartType getChartType()
    {
        Chart chart = getChart();
        return chart != null ? chart.getType() : ChartType.SCATTER;
    }

    /**
     * Returns the parent part.
     */
    public ChartPart getParent()
    {
        if (_parent != null)
            return _parent;
        if (this instanceof Chart) return null;
        if (this instanceof DataSet) return getDataSetList();
        return getChart();
    }

    /**
     * Returns the DataStyle.
     */
    public DataStyle getDataStyle()
    {
        Chart chart = getChart();
        return chart != null ? chart.getDataStyle() : null;
    }

    /**
     * Returns the dataset.
     */
    public DataSetList getDataSetList()  { return _chart.getDataSetList(); }

    /**
     * Returns the name.
     */
    public String getName()  { return _name; }

    /**
     * Sets the name.
     */
    public void setName(String aName)
    {
        // If value already set, just return
        if (Objects.equals(aName, _name)) return;

        // Set value and fire prop change
        firePropChange(Name_Prop, _name, _name = aName);
    }

    /**
     * Returns the ChartPart border.
     */
    public Border getBorder()
    {
        // If border not supported, use Line Prop version
        if (!isBorderSupported())
            return getBorderUsingLineProps();

        // If explicitly set, just return
        if (_border != UNSET_BORDER)
            return _border;

        // Return default border
        return (Border) getPropDefault(Border_Prop);
    }

    /**
     * Sets the ChartPart border.
     */
    public void setBorder(Border aBorder)
    {
        // If border not supported, use Line Prop version
        if (!isBorderSupported())  {
            setBorderUsingLineProps(aBorder);
            return;
        }

        // If already set, just return
        if (Objects.equals(aBorder, getBorder())) return;

        // If given border is default, replace with UNSET_BORDER
        if (Objects.equals(aBorder, getPropDefault(Border_Prop)))
            aBorder = UNSET_BORDER;

        // Set and firePropChange
        firePropChange(Border_Prop, _border, _border = aBorder);
    }

    /**
     * Sets the ChartPart border.
     */
    public void setBorder(Color aColor, double aBorderWidth)
    {
        Border border = Border.createLineBorder(aColor, aBorderWidth);
        setBorder(border);
    }

    /**
     * Returns whether line color is explicitly set.
     */
    public boolean isLineColorSet()  { return _lineColor != null; }

    /**
     * Returns the line color.
     */
    public Color getLineColor()
    {
        // If set, just return
        if (_lineColor != null) return _lineColor;

        // Return default
        return (Color) getPropDefault(LineColor_Prop);
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
     * Returns whether fill is set.
     */
    public boolean isFillSet()  { return _fill != null; }

    /**
     * Returns the fill of ChartPart.
     */
    public Paint getFill()
    {
        // If explicitly set, just return
        if (_fill != null) return _fill;

        // Return default fill
        return (Paint) getPropDefault(Fill_Prop);
    }

    /**
     * Sets the fill of ChartPart.
     */
    public void setFill(Paint aPaint)
    {
        if (Objects.equals(aPaint, _fill)) return;
        firePropChange(Fill_Prop, _fill, _fill = aPaint);
    }

    /**
     * Returns the fill color.
     */
    public Color getFillColor()
    {
        Paint fill = getFill();
        return fill != null ? fill.getColor() : null;
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
     * Returns whether the font is set.
     */
    public boolean isFontSet()  { return _font != null; }

    /**
     * Returns the font of ChartPart.
     */
    public Font getFont()
    {
        // If explicitly set, just return
        if (_font != null) return _font;

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
     * Returns the fill of ChartPart text.
     */
    public Paint getTextFill()  { return _textFill; }

    /**
     * Sets the fill of ChartPart text.
     */
    public void setTextFill(Paint aFill)
    {
        if (Objects.equals(aFill, _textFill)) return;
        firePropChange(TextFill_Prop, _textFill, _textFill = aFill);
    }

    /**
     * Returns whether border is supported.
     */
    public boolean isBorderSupported()  { return true; }

    /**
     * Override to create border from line props.
     */
    private Border getBorderUsingLineProps()
    {
        double lineWidth = getLineWidth(); if (lineWidth <= 0) return null;
        Color lineColor = getLineColor();
        System.err.println(getClass().getSimpleName() + ".getBorder: Should probably call line prop methods instead");
        return Border.createLineBorder(lineColor, lineWidth);
    }

    /**
     * Override to set line props from border.
     */
    private void setBorderUsingLineProps(Border aBorder)
    {
        Color lineColor = aBorder != null ? aBorder.getColor() : null;
        int lineWidth = aBorder != null ? (int) Math.round(aBorder.getWidth()) : 0;
        setLineColor(lineColor); setLineWidth(lineWidth);
        System.err.println(getClass().getSimpleName() + ".setBorder: Should probably call line prop methods instead");
    }

    /**
     * Returns the prop value for given key.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        // Handle properties
        switch (aPropName) {

            // Handle Name Prop
            case Name_Prop: return getName();

            // LineColor, LineWidth, LineDash
            case LineColor_Prop: return getLineColor();
            case LineWidth_Prop: return getLineWidth();
            case LineDash_Prop: return getLineDash();

            // Handle super class properties (or unknown)
            default: System.err.println("ChartPart.getPropValue: Unknown prop: " + aPropName); return null;
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

            // Handle Name
            case Name_Prop: setName(SnapUtils.stringValue(aValue)); break;

            // LineColor, LineWidth, LineDash
            case LineColor_Prop: setLineColor((Color) aValue); break;
            case LineWidth_Prop: setLineWidth(SnapUtils.intValue(aValue)); break;
            case LineDash_Prop: setLineDash((double[]) aValue); break;

            // Handle super class properties (or unknown)
            default: System.err.println("ChartPart.setPropValue: Unknown prop: " + aPropName);
        }
    }

    /**
     * Returns whether give prop is set to default.
     */
    public boolean isPropDefault(String aPropName)
    {
        switch (aPropName) {

            // Border
            case Border_Prop: return _border == UNSET_BORDER;

            // Default
            default: {
                Object propValue = getPropValue(aPropName);
                Object propDefault = getPropDefault(aPropName);
                return Objects.equals(propValue, propDefault);
            }
        }
    }

    /**
     * Returns the value for given key.
     */
    @Override
    public Object getPropDefault(String aPropName)
    {
        // Handle properties
        switch (aPropName) {

            // Name
            case Name_Prop: return null;

            // Border
            case Border_Prop: return DEFAULT_BORDER;

            // LineColor, LineWidth, LineDash
            case LineColor_Prop: return DEFAULT_LINE_COLOR;
            case LineWidth_Prop: return DEFAULT_LINE_WIDTH;
            case LineDash_Prop: return DEFAULT_LINE_DASH;

            // Fill, Effect
            case Fill_Prop: return DEFAULT_FILL;
            case Effect_Prop: return DEFAULT_EFFECT;

            // Font, TextFill
            case Font_Prop: return DEFAULT_FONT;
            case TextFill_Prop: return DEFAULT_TEXT_FILL;

            // Superclass props
            default: System.err.println("ChartPart.getPropDefault: Unknown prop: " + aPropName); return null;
        }
    }

    /**
     * Archival.
     */
    @Override
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Get new element with class name
        String cname = getClass().getSimpleName();
        XMLElement e = new XMLElement(cname);

        // Archive Name
        if (getName() != null && getName().length() > 0)
            e.add(Name_Prop, getName());

        // Archive Border
        if (!isPropDefault(Border_Prop)) {
            Border border = getBorder(); if (border == null) border = new Borders.NullBorder();
            XMLElement borderXML = border.toXML(anArchiver);
            e.add(Border_Prop, borderXML);
        }

        // Archive LineColor, LineWidth, LineDash
        if (!isPropDefault(LineColor_Prop))
            e.add(LineColor_Prop, getLineColor().toHexString());
        if (!isPropDefault(LineWidth_Prop))
            e.add(LineWidth_Prop, getLineWidth());
        if (!ArrayUtils.equals(_lineDash, DEFAULT_LINE_DASH)) {
            String dashStr = Stroke.getDashArrayNameOrString(_lineDash);
            e.add(LineDash_Prop, dashStr);
        }

        // Archive Fill
        if (isFillSet()) {
            Paint fill = getFill();
            XMLElement fillXML = fill.toXML(anArchiver);
            e.add(Fill_Prop, fillXML);
        }

        // Archive Effect
        if (!isPropDefault(Effect_Prop)) {
            Effect effect = getEffect();
            XMLElement effectXML = effect.toXML(anArchiver);
            e.add(Effect_Prop, effectXML);
        }

        // Archive Opacity
        if (getOpacity() != DEFAULT_OPACTIY)
            e.add(Opacity_Prop, getOpacity());

        // Archive Font
        if (isFontSet()) {
            Font font = getFont();
            XMLElement fontXML = font.toXML(anArchiver);
            e.add(Font_Prop, fontXML);
        }

        // Archive TextFill
        if (!isPropDefault(TextFill_Prop)) {
            Paint textFill = getTextFill();
            XMLElement textFillXML = textFill.toXML(anArchiver);
            e.add(TextFill_Prop, textFillXML);
        }

        // Return element
        return e;
    }

    /**
     * Unarchival.
     */
    @Override
    public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Go ahead and set chart
        if (anArchiver instanceof ChartArchiver) {
            Chart chart = ((ChartArchiver) anArchiver).getChart();
            setChart(chart);
        }

        // Unarchive Name
        if (anElement.hasAttribute(Name_Prop))
            setName(anElement.getAttributeValue(Name_Prop));

        // Unarchive Border
        XMLElement borderXML = anElement.get(Border_Prop);
        if (borderXML != null) {
            Border border = (Border) anArchiver.fromXML(borderXML, null);
            if (border instanceof Borders.NullBorder) border = null;
            setBorder(border);
        }

        // Unarchive LineColor, LineWidth, LineDash
        if (anElement.hasAttribute(LineColor_Prop)) {
            Color color = Color.get('#' + anElement.getAttributeValue(LineColor_Prop));
            setLineColor(color);
        }
        if (anElement.hasAttribute(LineWidth_Prop))
            setLineWidth(anElement.getAttributeIntValue(LineWidth_Prop));
        if (anElement.hasAttribute(LineDash_Prop)) {
            String dashStr = anElement.getAttributeValue(LineDash_Prop);
            double[] dashArray = Stroke.getDashArray(dashStr);
            setLineDash(dashArray);
        }

        // Unarchive Fill
        XMLElement fillXML = anElement.get(Fill_Prop);
        if (fillXML != null) {
            Paint fill = (Paint) anArchiver.fromXML(fillXML, null);
            setFill(fill);
        }

        // Unarchive Effect
        XMLElement effectXML = anElement.get(Effect_Prop);
        if (effectXML != null) {
            Effect effect = (Effect) anArchiver.fromXML(effectXML, null);
            setEffect(effect);
        }

        // Unarchive Opacity
        if (anElement.hasAttribute(Opacity_Prop))
            setOpacity(anElement.getAttributeDoubleValue(Opacity_Prop));

        // Unarchive Font
        XMLElement fontXML = anElement.get(Font_Prop);
        if (fontXML != null) {
            Font font = (Font) anArchiver.fromXML(fontXML, null);
            setFont(font);
        }

        // Unarchive TextFill
        XMLElement textFillXML = anElement.get(TextFill_Prop);
        if (textFillXML != null) {
            Paint textFill = (Paint) anArchiver.fromXML(textFillXML, null);
            setTextFill(textFill);
        }

        // Return this part
        return this;
    }
}
