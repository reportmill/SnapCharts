/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;
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

    // The Chart
    protected Chart  _chart;

    // Constants for properties
    public static final String Name_Prop = "Name";

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
        return chart != null ? chart.getType() : ChartType.SCATTER;
    }

    /**
     * Returns the parent part.
     */
    public ChartPart getParent()
    {
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
