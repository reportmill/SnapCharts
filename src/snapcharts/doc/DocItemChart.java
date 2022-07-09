/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.doc;
import snap.props.Prop;
import snap.props.PropChange;
import snap.props.PropSet;
import snapcharts.model.Chart;
import snapcharts.model.ChartPart;
import snapcharts.model.Trace;
import snapcharts.model.Content;

/**
 * A DocItem subclass to hold a chart.
 */
public class DocItemChart extends DocItemParent<Chart> {

    /**
     * Constructor.
     */
    public DocItemChart(Chart aChart)
    {
        super();

        // Set chart as content
        setContent(aChart);

        // Add Items for Traces
        Content content = _content.getContent();
        for (Trace trace : content.getTraces())
            addItem(new DocItemTrace(trace));

        // Start listening to prop changes
        _content.addPropChangeListener(aPC -> chartDidPropChange(aPC));
        _content.addDeepChangeListener((obj,aPC) -> chartDidPropChange(aPC));
    }

    /**
     * Returns the chart.
     */
    public Chart getChart()  { return _content; }

    /**
     * Override to return Chart name.
     */
    @Override
    public String getName()
    {
        return getChart().getName();
    }

    /**
     * Override to return Chart.
     */
    @Override
    public ChartPart getChartPart()  { return getChart(); }

    /**
     * Override to accept Traces.
     */
    public DocItem addChartPart(ChartPart aChartPart, DocItem aChildItem)
    {
        // Handle add Trace
        if (aChartPart instanceof Trace) {
            Trace trace = (Trace) aChartPart;
            int ind = aChildItem != null ? aChildItem.getIndex() + 1 : getItemCount();
            getChart().getContent().addTrace(trace, ind);
            return getItem(ind);
        }

        // Otherwise, try parent
        return super.addChartPart(aChartPart, aChildItem);
    }

    /**
     * Called when PropChanges.
     */
    private void chartDidPropChange(PropChange aPC)
    {
        // Get property name
        String propName = aPC.getPropName();

        // Handle Trace add/remove
        if (propName == Content.Trace_Prop) {
            int ind = aPC.getIndex(); if (ind < 0) return;
            Trace newVal = (Trace) aPC.getNewValue();
            if (newVal != null)
                addItem(new DocItemTrace(newVal));
            else removeItem(ind);
        }
    }

    /**
     * Override to provide prop/relation names.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // Remove Items so DocItemDataSets don't get archived (they are archived with chart)
        Prop itemsProp = aPropSet.getPropForName(DocItems_Prop);
        aPropSet.removeProp(itemsProp);
    }
}
