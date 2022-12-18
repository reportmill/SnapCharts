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
        return convertPropObjectToJSON(anObj);
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
     * Override to handle DataSet.
     */
    @Override
    protected PropObject createPropObjectForFormatNode(PropNode aParent, Prop aProp, Object aFormatNode)
    {
        // Handle DataSet
        if (aProp != null && aProp.getDefaultPropClass() == DataSet.class)
            return new ChartArchiver.DataSetProxy();

        // Do normal version
        return super.createPropObjectForFormatNode(aParent, aProp, aFormatNode);
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
