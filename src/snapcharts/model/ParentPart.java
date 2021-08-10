/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;
import snap.util.ArrayUtils;
import snap.util.PropChange;

/**
 * This ChartPart class can have child ChartParts.
 */
public class ParentPart extends ChartPart {

    // The children
    private ChartPart[]  _children = new ChartPart[0];

    /**
     * Returns the children.
     */
    public ChartPart[] getChildren()  { return _children; }

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
        _children = ArrayUtils.add(_children, aChartPart, anIndex);
        aChartPart._parent = this;
        aChartPart.addPropChangeListener(pc -> childDidPropChange(aChartPart, pc));
    }

    /**
     * Called when a child has a property change.
     */
    protected void childDidPropChange(ChartPart aChartPart, PropChange aPropChange)
    {
        Chart chart = getChart();
        chart.chartPartDidPropChange(aPropChange);
    }
}
