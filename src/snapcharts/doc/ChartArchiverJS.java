/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.doc;
import snap.props.*;
import snap.util.JSObject;
import snapcharts.data.*;
import snapcharts.model.*;
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
    }

    /**
     * Convenience.
     */
    public Doc getDocFromJSONBytes(byte[] xmlBytes)
    {
        Doc doc = (Doc) readPropObjectFromJSONBytes(xmlBytes);
        return doc;
    }

    /**
     * Returns a ChartPart for XML source.
     */
    public ChartPart getChartPartFromJSONSource(Object anObj)
    {
        ChartPart cpart = (ChartPart) readPropObjectFromJSONSource(anObj);
        return cpart;
    }

    /**
     * Returns an xml element for a given object.
     * This top level method encodes resources, in addition to doing the basic toXML stuff.
     */
    public JSObject writeToJSON(PropObject anObj)
    {
        JSObject json = convertPropObjectToJSON(anObj);
        return json;
    }

    /**
     * Writes given object to JSON and returns the JSON bytes.
     */
    public byte[] writeToJSONBytes(PropObject anObj)
    {
        JSObject json = writeToJSON(anObj);
        String jsonStr = json.toString();
        byte[] jsonBytes = jsonStr.getBytes();
        return jsonBytes;
    }

    /**
     * Returns a copy of the given object using archival.
     */
    public <T extends PropObject> T copy(T anObj)
    {
        JSObject json = convertPropObjectToJSON(anObj);
        return (T) readPropObjectFromJSON(json);
    }

    /**
     * Override to handle DataSet.
     */
    @Override
    protected PropObject createPropObjectForJSON(PropNode aParent, Prop aProp, JSObject anObjectJS)
    {
        // Handle DataSet
        if (aProp != null && aProp.getDefaultPropClass() == DataSet.class)
            return new ChartArchiver.DataSetProxy();

        // Do normal version
        return super.createPropObjectForJSON(aParent, aProp, anObjectJS);
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
