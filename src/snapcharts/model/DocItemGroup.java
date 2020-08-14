package snapcharts.model;
import snap.util.XMLArchiver;
import snap.util.XMLElement;
import snapcharts.app.ChartSetPane;
import snapcharts.app.DocItemPane;

import java.util.ArrayList;
import java.util.List;

/**
 * A DocItem subclass that has child items.
 */
public class DocItemGroup extends DocItem {

    // Whether group pages should be portrait
    private boolean  _portrait = true;

    // The number of items to show per page
    private int _itemsPerPage = 2;

    // Constants for properties
    public static final String ItemsPerPage_Prop = "ItemsPerPage";
    public static final String Portrait_Prop = "Portrait";

    /**
     * Returns whether pages are portrait.
     */
    public boolean isPortrait()  { return _portrait; }

    /**
     * Sets whether pages are portrait.
     */
    public void setPortrait(boolean aValue)
    {
        if (aValue==isPortrait()) return;
        firePropChange(Portrait_Prop, _portrait, _portrait = aValue);
    }

    /**
     * Returns the number of items to show per page.
     */
    public int getItemsPerPage()  { return _itemsPerPage; }

    /**
     * Sets the number of items to show per page.
     */
    public void setItemsPerPage(int aValue)
    {
        if (aValue==getItemsPerPage()) return;
        firePropChange(ItemsPerPage_Prop, _itemsPerPage, _itemsPerPage = aValue);
    }

    /**
     * Returns the charts.
     */
    public List<Chart> getCharts()
    {
        List<Chart> charts = new ArrayList<>();
        for (DocItem item : getItems())
            if (item instanceof DocItemChart)
                charts.add(((DocItemChart)item).getChart());
        return charts;
    }

    /**
     * Adds a chart.
     */
    public DocItem addChart(Chart aChart)
    {
        DocItemChart chartDocItem = new DocItemChart(aChart);
        addItem(chartDocItem);
        return chartDocItem;
    }

    /**
     * Adds a chart at given index.
     */
    public DocItem addChart(Chart aChart, int anIndex)
    {
        DocItemChart chartDocItem = new DocItemChart(aChart);
        addItem(chartDocItem, anIndex);
        return chartDocItem;
    }

    /**
     * Override to accept Chart.
     */
    public DocItem addChartPart(ChartPart aChartPart, DocItem aChildItem)
    {
        // Handle Chart
        if (aChartPart instanceof Chart) {
            Chart chart = (Chart) aChartPart;
            int ind = aChildItem!=null ? aChildItem.getIndex() + 1 : getItemCount();
            return addChart(chart, ind);
        }

        // Handle DataSet
        if (aChartPart instanceof DataSet) {
            for (DocItem item : getItems())
                if (item instanceof DocItemChart)
                    item.addChartPart(aChartPart, null);
        }

        // Do normal version (nothing)
        return super.addChartPart(aChartPart, aChildItem);
    }

    /**
     * Creates the ItemPane.
     */
    @Override
    protected DocItemPane createItemPane()
    {
        ChartSetPane pane = new ChartSetPane();
        pane.setDocItem(this);
        return pane;
    }

    /**
     * Archival.
     */
    @Override
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Do normal version
        XMLElement e = super.toXML(anArchiver);

        // Archive Portrait, ItemsPerPage
        if(!isPortrait()) e.add(Portrait_Prop, isPortrait());
        if(getItemsPerPage()!=2) e.add(ItemsPerPage_Prop, getItemsPerPage());

        // Archive charts
        XMLElement chartsXML = new XMLElement("Charts");
        e.add(chartsXML);
        for (DocItem docItem : getItems()) {
            DocItemChart chartDocItem = docItem instanceof DocItemChart ? (DocItemChart)docItem : null;
            if (chartDocItem!=null)
                chartsXML.add(anArchiver.toXML(chartDocItem.getChart()));
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
        // Do normal version
        super.fromXML(anArchiver, anElement);

        // Unarchive Portrait, ItemsPerPage
        if(anElement.hasAttribute(Portrait_Prop))
            setPortrait(anElement.getAttributeBoolValue(Portrait_Prop));
        if(anElement.hasAttribute(ItemsPerPage_Prop))
            setItemsPerPage(anElement.getAttributeIntValue(ItemsPerPage_Prop));

        // Unarchive charts
        XMLElement chartsXML = anElement.get("Charts");
        if (chartsXML!=null) {
            List<XMLElement> chartXMLs = chartsXML.getElements("Chart");
            for (XMLElement chartXML : chartXMLs) {
                Chart chart = (Chart)anArchiver.fromXML(chartXML, this);
                if (chart!=null)
                    addChart(chart);
            }
        }

        // Return this part
        return this;
    }
}
