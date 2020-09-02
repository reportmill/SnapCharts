package snapcharts.model;
import snap.util.XMLArchiver;
import snap.web.WebURL;

import java.util.HashMap;
import java.util.Map;

/**
 * An archiver for SnapCharts document file.
 */
public class ChartArchiver extends XMLArchiver {

    /**
     * Constructor.
     */
    public ChartArchiver()
    {
        setIgnoreCase(true);
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
        cmap.put(Area.class.getSimpleName(), Area.class);
        cmap.put(Axis.class.getSimpleName(), Axis.class);
        cmap.put(AxisX.class.getSimpleName(), AxisX.class);
        cmap.put(AxisY.class.getSimpleName(), AxisY.class);
        cmap.put(Chart.class.getSimpleName(), Chart.class);
        cmap.put("ChartDoc", Doc.class); // Legacy - can go soon
        cmap.put(Doc.class.getSimpleName(), Doc.class);
        cmap.put(DataSet.class.getSimpleName(), DataSet.class);
        cmap.put(DataSetList.class.getSimpleName(), DataSetList.class);
        return cmap;
    }
}
