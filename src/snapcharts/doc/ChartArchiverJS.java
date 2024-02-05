/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.doc;
import snap.props.*;
import snap.util.JSObject;
import snapcharts.data.*;
import snapcharts.charts.*;
import java.util.Map;

/**
 * An archiver for SnapCharts document file.
 */
public class ChartArchiverJS extends PropArchiverJS {

    /**
     * Constructor.
     */
    public ChartArchiverJS()
    {
        super();

        // Register DataSet to use DataSetProxy for archival/unarchival
        PropArchiverHpr.setProxyClassForClass(DataSet.class, ChartArchiver.DataSetProxy.class);
    }

    /**
     * Convenience.
     */
    public Doc getDocFromJSONBytes(byte[] xmlBytes)
    {
        return (Doc) readPropObjectFromJSONBytes(xmlBytes);
    }

    /**
     * Returns a ChartPart for JSON source.
     */
    public ChartPart getChartPartFromJSONSource(Object anObj)
    {
        return (ChartPart) readPropObjectFromJSONSource(anObj);
    }

    /**
     * Returns an JSON object for a given PropObject.
     */
    public JSObject writeToJSON(PropObject anObj)
    {
        return writePropObjectToJSON(anObj);
    }

    /**
     * Writes given object to JSON and returns the JSON bytes.
     */
    public byte[] writeToJSONBytes(PropObject anObj)
    {
        JSObject jsonObj = writeToJSON(anObj);
        String jsonStr = jsonObj.toString();
        return jsonStr.getBytes();
    }

    /**
     * Creates the class map.
     */
    @Override
    protected Map<String, Class<?>> createClassMap()
    {
        return new ChartArchiver().getClassMap();
    }
}
