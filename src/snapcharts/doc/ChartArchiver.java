package snapcharts.doc;
import snap.gfx.*;
import snap.text.NumberFormat;
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
     * Returns a ChartDoc for XML string.
     */
    public Doc getDocFromXMLBytes(byte[] xmlBytes)
    {
        Doc doc = (Doc) readFromXMLBytes(xmlBytes);
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
     * Override to set Chart.
     */
    @Override
    public <T> T copy(T anObj)
    {
        if (anObj instanceof ChartPart)
            setChart(((ChartPart) anObj).getChart());
        return super.copy(anObj);
    }

    /**
     * Creates the class map.
     */
    protected Map<String, Class> createClassMap()
    {
        // Create class map and add classes
        Map<String,Class> cmap = new HashMap();

        // Add classes
        cmap.put(TraceStyle.class.getSimpleName(), TraceStyle.class);
        cmap.put(Axis.class.getSimpleName(), Axis.class);
        cmap.put(AxisX.class.getSimpleName(), AxisX.class);
        cmap.put(AxisY.class.getSimpleName(), AxisY.class);
        cmap.put(Chart.class.getSimpleName(), Chart.class);
        cmap.put("ChartDoc", Doc.class); // Legacy - can go soon
        cmap.put(Doc.class.getSimpleName(), Doc.class);
        cmap.put(Trace.class.getSimpleName(), Trace.class);
        cmap.put(TraceList.class.getSimpleName(), TraceList.class);
        cmap.put("DataSet", Trace.class);
        cmap.put("DataSetList", TraceList.class);
        cmap.put(Header.class.getSimpleName(), Header.class);
        cmap.put(Legend.class.getSimpleName(), Legend.class);
        cmap.put(ContourAxis.class.getSimpleName(), ContourAxis.class);
        cmap.put(Marker.class.getSimpleName(), Marker.class);

        // Add Graphics classes (Border, Paint, Font, NumberFormat, Effect)
        cmap.put("color", Color.class);
        cmap.put("Color", Color.class);
        cmap.put("font", Font.class);
        cmap.put("Font", Font.class);
        cmap.put("NumberFormat", NumberFormat.class);
        cmap.put("EmptyBorder", Borders.EmptyBorder.class);
        cmap.put("BevelBorder", Borders.BevelBorder.class);
        cmap.put("EtchBorder", Borders.EtchBorder.class);
        cmap.put("LineBorder", Borders.LineBorder.class);
        cmap.put("NullBorder", Borders.NullBorder.class);
        cmap.put("GradientPaint", GradientPaint.class); //RMGradientFill.class
        cmap.put("ImagePaint", ImagePaint.class); //RMImageFill.class
        cmap.put("BlurEffect", BlurEffect.class);
        cmap.put("ShadowEffect", ShadowEffect.class);
        cmap.put("ReflectEffect", ReflectEffect.class);
        cmap.put("EmbossEffect", EmbossEffect.class);

        // Return map
        return cmap;
    }
}
