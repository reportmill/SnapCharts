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

    // The name
    private String  _name;

    // The border
    private Border  _border;

    // The Fill
    private Paint  _fill;

    // The Effect
    private Effect  _effect;

    // The Font
    private Font  _font;

    // The opacity
    private double  _opacity = 1;

    // The Chart
    protected Chart  _chart;

    // Constants for properties
    public static final String Name_Prop = "Name";
    public static final String Border_Prop = "Border";
    public static final String Fill_Prop = "Fill";
    public static final String Font_Prop = "Font";
    public static final String Effect_Prop = "Effect";
    public static final String Opacity_Prop = "Opacity";

    /**
     * Constructor.
     */
    public ChartPart()  { }

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
    public Chart getChart()  { return _chart; }

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
        return chart != null ? chart.getType() : ChartType.LINE;
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
     * Returns the font of ChartPart.
     */
    public Font getFont()
    {
        if (_font!=null)
            return _font;
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
     * Returns the prop keys.
     */
    @Override
    protected String[] getPropKeysLocal()
    {
        return new String[] {
                Name_Prop
        };
    }

    /**
     * Returns the prop value for given key.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        // Handle properties
        switch (aPropName) {
            case Name_Prop: return getName();
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
            case Name_Prop: setName(SnapUtils.stringValue(aValue)); break;
            default: System.err.println("ChartPart.setPropValue: Unknown prop: " + aPropName);
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
            case Name_Prop: return null;
            case Font_Prop: return Font.Arial12;
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

        // Archive name
        if (getName()!=null && getName().length()>0) e.add(Name_Prop, getName());

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

        // Return this part
        return this;
    }
}
