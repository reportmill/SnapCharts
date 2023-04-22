/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.doc;
import snap.props.PropSet;
import snap.props.PropObject;
import snap.util.*;
import snapcharts.model.Chart;
import snapcharts.model.ChartPart;
import snapcharts.model.Trace;
import snapcharts.model.PageDisplay;
import java.util.ArrayList;
import java.util.List;

/**
 * A DocItem subclass that has child items.
 */
public class DocItemGroup<T extends PropObject> extends DocItemParent<T> {

    // Whether group pages should be portrait
    private boolean  _portrait = true;

    // How pages should be displayed (Single, Continuous).
    private PageDisplay  _pageDisplay = PageDisplay.SINGLE;

    // The number of items to show per page
    private int  _itemsPerPage = 2;

    // The relative scale of the charts to emphasize text vs data
    private double  _chartScale = CHART_SCALE_NATURAL;

    // Constants for properties
    public static final String Portrait_Prop = "Portrait";
    public static final String PageDisplay_Prop = "PageDisplay";
    public static final String ItemsPerPage_Prop = "ItemsPerPage";
    public static final String ChartScale_Prop = "ChartScale";

    // Constants for property defaults
    private static final boolean  DEFAULT_PORTRAIT = true;
    private static final PageDisplay  DEFAULT_PAGE_DISPLAY = PageDisplay.SINGLE;
    private static final int DEFAULT_ITEMS_PER_PAGE = 2;
    private static final double DEFAULT_CHART_SCALE = 1;

    // Constants for ChartScale
    public static final double CHART_SCALE_LARGEST_TEXT = .5;
    public static final double CHART_SCALE_LARGER_TEXT = .75;
    public static final double CHART_SCALE_NATURAL = 1;
    public static final double CHART_SCALE_LARGER_DATA = 1.5;
    public static final double CHART_SCALE_LARGEST_DATA = 2;

    /**
     * Constructor.
     */
    public DocItemGroup()
    {
        super();
    }

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
     * Returns how pages should be displayed (Single, Continuous).
     */
    public PageDisplay getPageDisplay()  { return _pageDisplay; }

    /**
     * Sets how pages should be displayed (Single, Continuous).
     */
    public void setPageDisplay(PageDisplay aValue)
    {
        if (aValue == getPageDisplay()) return;
        firePropChange(PageDisplay_Prop, _pageDisplay, _pageDisplay = aValue);
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
     * Sets the number of items to show per page, adjusting Portrait/ChartScale if it will help.
     */
    public void setItemsPerPageAndMore(int aValue)
    {
        setItemsPerPage(aValue);
        switch (aValue) {
            case 1: setPortrait(false); break;
            case 2: setPortrait(true); break;
            case 3: setPortrait(true); break;
            case 4:
                setPortrait(false);
                if (getChartScale() < DocItemGroup.CHART_SCALE_LARGER_TEXT)
                    setChartScale(DocItemGroup.CHART_SCALE_LARGER_TEXT);
                break;
            case 6:
                setPortrait(true);
                if (getChartScale() < DocItemGroup.CHART_SCALE_NATURAL)
                    setChartScale(DocItemGroup.CHART_SCALE_NATURAL);
                break;
            case 9:
                setPortrait(false);
                if (getChartScale() < DocItemGroup.CHART_SCALE_NATURAL)
                    setChartScale(DocItemGroup.CHART_SCALE_NATURAL);
                break;
            default: break;
        }
    }

    /**
     * Returns the scale of charts to either emphasize text (.5 - <1), emphasize data (>1 - 1.5) or balanced (1).
     */
    public double getChartScale()  { return _chartScale; }

    /**
     * Sets the scale of charts to either emphasize text (.5 - <1), emphasize data (>1 - 1.5) or balanced (1).
     */
    public void setChartScale(double aValue)
    {
        if (MathUtils.equals(aValue, getChartScale())) return;
        firePropChange(ChartScale_Prop, _chartScale, _chartScale = aValue);
    }

    /**
     * Returns the charts.
     */
    public List<Chart> getCharts()
    {
        List<Chart> charts = new ArrayList<>();
        for (DocItem<?> item : getDocItems())
            if (item instanceof DocItemChart)
                charts.add(((DocItemChart)item).getChart());
        return charts;
    }

    /**
     * Adds a chart.
     */
    public DocItemChart addChart(Chart aChart)
    {
        DocItemChart chartDocItem = new DocItemChart(aChart);
        addItem(chartDocItem);
        return chartDocItem;
    }

    /**
     * Adds a chart at given index.
     */
    public DocItemChart addChart(Chart aChart, int anIndex)
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

        // Handle Trace
        if (aChartPart instanceof Trace) {
            for (DocItem item : getDocItems())
                if (item instanceof DocItemChart)
                    item.addChartPart(aChartPart, null);
        }

        // Do normal version (nothing)
        return super.addChartPart(aChartPart, aChildItem);
    }

    /**
     * Override to provide prop/relation names.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // Add Portrait, PageDisplay, ItemsPerPage, ChartScale
        aPropSet.addPropNamed(Portrait_Prop, boolean.class, DEFAULT_PORTRAIT);
        aPropSet.addPropNamed(PageDisplay_Prop, PageDisplay.class, DEFAULT_PAGE_DISPLAY);
        aPropSet.addPropNamed(ItemsPerPage_Prop, int.class, DEFAULT_ITEMS_PER_PAGE);
        aPropSet.addPropNamed(ChartScale_Prop, double.class, DEFAULT_CHART_SCALE);
    }

    /**
     * Override for DocItem properties.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        switch (aPropName) {

            // Portrait, PageDisplay_Prop, ItemsPerPage, ChartScale
            case Portrait_Prop: return isPortrait();
            case PageDisplay_Prop: return getPageDisplay();
            case ItemsPerPage_Prop: return getItemsPerPage();
            case ChartScale_Prop: return getChartScale();

            // Do normal version
            default: return super.getPropValue(aPropName);
        }
    }

    /**
     * Override for DocItem properties.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        switch (aPropName) {

            // Portrait, PageDisplay_Prop, ItemsPerPage, ChartScale
            case Portrait_Prop: setPortrait(Convert.boolValue(aValue)); break;
            case PageDisplay_Prop: setPageDisplay((PageDisplay) aValue); break;
            case ItemsPerPage_Prop: setItemsPerPage(Convert.intValue(aValue)); break;
            case ChartScale_Prop: setChartScale(Convert.doubleValue(aValue)); break;

            // Do normal version
            default: super.setPropValue(aPropName, aValue);
        }
    }

    /**
     * Archival.
     */
    @Override
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Do normal version
        XMLElement e = super.toXML(anArchiver);

        // Archive Portrait, ItemsPerPage, ChartScale
        if (!isPortrait())
            e.add(Portrait_Prop, isPortrait());
        if (getItemsPerPage() != 2)
            e.add(ItemsPerPage_Prop, getItemsPerPage());
        if (getChartScale() != 1)
            e.add(ChartScale_Prop, getChartScale());

        // Archive charts
        XMLElement chartsXML = new XMLElement("Charts");
        e.add(chartsXML);
        for (DocItem docItem : getDocItems()) {
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

        // Unarchive Portrait, ItemsPerPage, ChartScale
        if(anElement.hasAttribute(Portrait_Prop))
            setPortrait(anElement.getAttributeBoolValue(Portrait_Prop));
        if(anElement.hasAttribute(ItemsPerPage_Prop))
            setItemsPerPage(anElement.getAttributeIntValue(ItemsPerPage_Prop));
        if(anElement.hasAttribute(ChartScale_Prop))
            setChartScale(anElement.getAttributeDoubleValue(ChartScale_Prop));

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
