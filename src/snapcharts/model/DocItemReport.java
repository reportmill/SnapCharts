package snapcharts.model;
import rmdraw.scene.SGDoc;

/**
 * A DocItem subclass to hold a chart.
 */
public class DocItemReport extends DocItem {

    // The Report Doc
    private SGDoc  _doc;

    /**
     * Constructor.
     */
    public DocItemReport(SGDoc aDoc)
    {
        // Set Doc
        _doc = aDoc;

        // Start listening to prop changes
        //_chart.addPropChangeListener(aPC -> chartDidPropChange(aPC));
        //_chart.addDeepChangeListener((obj,aPC) -> chartDidPropChange(aPC));
    }

    /**
     * Returns the chart.
     */
    public SGDoc getReportDoc()  { return _doc; }

    /**
     * Override to return Chart name.
     */
    @Override
    public String getName()
    {
        String name = getReportDoc().getFilename();
        return name!=null ? name : "New Report";
    }

    /**
     * Override to return Chart.
     */
    @Override
    public ChartPart getChartPart()  { return null; }

    /**
     * Called when PropChanges.
     */
    /*private void chartDidPropChange(PropChange aPC)
    {
        // Get property name
        String propName = aPC.getPropName();

        // Handle DataSet add/remove
        if (propName==DataSetList.DataSet_Prop) {
            int ind = aPC.getIndex(); if (ind<0) return;
            DataSet newVal = (DataSet)aPC.getNewValue();
            if (newVal!=null)
                addItem(new DocItemDataSet(newVal));
            else removeItem(ind);
        }
    }*/
}
