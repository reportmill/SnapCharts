/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;
import snap.util.ArrayUtils;
import snap.props.PropChange;
import snap.props.PropSet;

/**
 * This ChartPart class can have child ChartParts.
 */
public class ParentPart extends ChartPart {

    // The children
    private ChartPart[]  _children = new ChartPart[0];

    // Constants for properties
    public static final String Children_Prop = "Children";

    /**
     * Returns the children.
     */
    public ChartPart[] getChildren()  { return _children; }

    /**
     * Sets the children.
     */
    public void setChildren(ChartPart[] aChildArray)
    {
        firePropChange(Children_Prop, _children, _children = aChildArray);
    }

    /**
     * Returns the number of children.
     */
    public int getChildCount()  { return _children.length; }

    /**
     * Returns the specific child at given index.
     */
    public ChartPart getChild(int anIndex)  { return _children[anIndex]; }

    /**
     * Adds a child.
     */
    public void addChild(ChartPart aChartPart)
    {
        addChild(aChartPart, getChildCount());
    }

    /**
     * Adds a child at given index.
     */
    public void addChild(ChartPart aChartPart, int anIndex)
    {
        // Add Child to list
        _children = ArrayUtils.add(_children, aChartPart, anIndex);

        // Set parent
        aChartPart._parent = this;

        // Listen for PropChange and forward to Chart
        aChartPart.addPropChangeListener(pc -> childDidPropChange(pc));
    }

    /**
     * Called when a child has a property change.
     */
    protected void childDidPropChange(PropChange aPropChange)
    {
        Chart chart = getChart();
        chart.chartPartDidPropChange(aPropChange);
    }

    /**
     * Override to register props.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // Children
        aPropSet.addPropNamed(Children_Prop, ChartPart[].class, new ChartPart[0]);
    }

    /**
     * Override for Chart properties.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        switch (aPropName) {

            // Children
            case Children_Prop: return getChildren();

            // Do normal version
            default: return super.getPropValue(aPropName);
        }
    }

    /**
     * Override for Chart properties.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        switch (aPropName) {

            // Children
            case Children_Prop: setChildren((ChartPart[]) aValue); break;

            // Do normal version
            default: super.setPropValue(aPropName, aValue);
        }
    }
}
