/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import snapcharts.doc.DocItem;
import snapcharts.model.ChartPart;

/**
 * A DocItem subclass to hold a chart.
 */
public class DocItemNotebook extends DocItem<Notebook> {

    // The Notebook
    private Notebook  _notebook;

    /**
     * Constructor.
     */
    public DocItemNotebook(Notebook aDoc)
    {
        super(aDoc);

        // Set Doc
        _notebook = aDoc;

        // Start listening to prop changes
        //_chart.addPropChangeListener(aPC -> chartDidPropChange(aPC));
        //_chart.addDeepChangeListener((obj,aPC) -> chartDidPropChange(aPC));
    }

    /**
     * Returns the Notebook.
     */
    public Notebook getNotebook()  { return _notebook; }

    /**
     * Override to return Notebook name.
     */
    @Override
    public String getName()
    {
        String name = getNotebook().getName();
        return name != null ? name : "New Notebook";
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
