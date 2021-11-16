package snapcharts.doc;
import snap.util.PropChange;
import snap.util.PropDefaults;
import snapcharts.model.Chart;
import snapcharts.model.ChartPart;
import snapcharts.model.Trace;
import snapcharts.model.TraceList;

/**
 * A DocItem subclass to hold a chart.
 */
public class DocItemChart extends DocItem<Chart> {

    /**
     * Constructor.
     */
    public DocItemChart(Chart aChart)
    {
        super(aChart);

        // Add Items for Traces
        TraceList traceList = _content.getTraceList();
        for (Trace trace : traceList.getTraces())
            addItem(new DocItemDataSet(trace));

        // Start listening to prop changes
        _content.addPropChangeListener(aPC -> chartDidPropChange(aPC));
        _content.addDeepChangeListener((obj,aPC) -> chartDidPropChange(aPC));
    }

    /**
     * Returns the chart.
     */
    public Chart getChart()  { return _content; }

    /**
     * Override to return true.
     */
    public boolean isParent()  { return true; }

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
            getChart().getTraceList().addTrace(trace, ind);
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
        if (propName == TraceList.Trace_Prop) {
            int ind = aPC.getIndex(); if (ind < 0) return;
            Trace newVal = (Trace) aPC.getNewValue();
            if (newVal != null)
                addItem(new DocItemDataSet(newVal));
            else removeItem(ind);
        }
    }

    /**
     * Override to provide prop/relation names.
     */
    @Override
    protected void initPropDefaults(PropDefaults aPropDefaults)
    {
        // Do normal version
        super.initPropDefaults(aPropDefaults);

        // Remove Items so DocItemDataSets don't get archived (they are archived with chart)
        aPropDefaults.removeRelations(Items_Prop);
    }
}
