/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.appx;
import rmdraw.scene.SGDoc;
import snapcharts.app.ChartPane;
import snapcharts.app.DocItemPane;
import snapcharts.app.DocPane;
import snapcharts.doc.Doc;
import snapcharts.doc.DocItem;
import snapcharts.doc.DocItemChart;
import snapcharts.model.Chart;
import java.util.Map;

/**
 * This DocPane subclass allows for new SnapCharts features like reporting and Chart annotations.
 */
public class DocPanePro extends DocPane {

    /**
     * Override to use ChartPanePro and ReportPane.
     */
    @Override
    protected DocItemPane createItemPane(DocItem anItem)
    {
        // Handle DocItemChart
        if (anItem instanceof DocItemChart) {
            DocItemChart docItemChart = (DocItemChart) anItem;
            Chart chart = docItemChart.getChart();
            ChartPane pane = new ChartPanePro();
            pane.setChart(chart);
            return pane;
        }

        // Handle DocItemReport
        if (anItem instanceof DocItemReport) {
            DocItemReport docItemReport = (DocItemReport) anItem;
            SGDoc doc = docItemReport.getReportDoc();
            ReportPane reportPane = new ReportPane();
            reportPane.setReportDoc(doc);
            return reportPane;
        }

        // Otherwise, do normal version
        return super.createItemPane(anItem);
    }

    /**
     * Override to add Report type.
     */
    @Override
    protected Map<String, Class> getDocItemTypes()
    {
        Map<String,Class> docItemTypes = super.getDocItemTypes();
        docItemTypes.put("Report", SGDoc.class);
        return docItemTypes;
    }

    /**
     * Creates a new DocItem for given class.
     */
    protected void createNewDocItem(Class aClass)
    {
        // Handle Report
        if (aClass == SGDoc.class) {

            // Get sel index
            DocItem item = getSelItem();
            while (item != null && item != getDoc() && item.getParent() != getDoc()) item = item.getParent();
            int index = item == null || item == getDoc() ? 0 : (item.getIndex() + 1);

            SGDoc rptDoc = new SGDoc();
            DocItem rptDocItem = new DocItemReport(rptDoc);
            Doc doc = getDoc();
            doc.addItem(rptDocItem, index);
            setSelItem(rptDocItem);
        }

        // Otherwise, do normal version
        else super.createNewDocItem(aClass);
    }
}
