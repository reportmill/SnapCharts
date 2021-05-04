package snapcharts.doc;
import snap.util.XMLArchiver;
import snap.web.WebURL;
import snapcharts.model.*;

import java.util.HashMap;
import java.util.Map;

/**
 * An archiver for SnapCharts document file.
 */
public class ChartArchiver extends XMLArchiver {

    // The Chart (gets set from inside its fromXML
    private Chart  _chart;

    /**
     * Constructor.
     */
    public ChartArchiver()
    {
        setIgnoreCase(true);
    }

    /**
     * Returns the Chart.
     */
    public Chart getChart()  { return _chart; }

    /**
     * Sets the Chart.
     */
    public void setChart(Chart aChart)
    {
        _chart = aChart;
    }

    /**
     * Returns a ChartDoc for XML source.
     */
    public Doc getDocFromXMLSource(Object anObj)
    {
        Doc doc = (Doc)readFromXMLSource(anObj);
        if (doc!=null) {
            WebURL url = getSourceURL();
            if (url!=null && !url.getString().contains("localhost"))
               doc.setSourceURL(getSourceURL());
        }
        return doc;
    }

    /**
     * Returns a ChartPart for XML source.
     */
    public ChartPart getChartPartFromXMLSource(Object anObj)
    {
        ChartPart cpart = (ChartPart)readFromXMLSource(anObj);
        return cpart;
    }
    /**
     * Creates the class map.
     */
    protected Map<String, Class> createClassMap()
    {
        // Create class map and add classes
        Map cmap = new HashMap();

        // Add classes
        cmap.put(DataStyle.class.getSimpleName(), DataStyle.class);
        cmap.put(Axis.class.getSimpleName(), Axis.class);
        cmap.put(AxisX.class.getSimpleName(), AxisX.class);
        cmap.put(AxisY.class.getSimpleName(), AxisY.class);
        cmap.put(Chart.class.getSimpleName(), Chart.class);
        cmap.put("ChartDoc", Doc.class); // Legacy - can go soon
        cmap.put(Doc.class.getSimpleName(), Doc.class);
        cmap.put(DataSet.class.getSimpleName(), DataSet.class);
        cmap.put(DataSetList.class.getSimpleName(), DataSetList.class);
        cmap.put(Header.class.getSimpleName(), Header.class);
        cmap.put(Legend.class.getSimpleName(), Legend.class);
        return cmap;
    }
}
