/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.doc;
import snap.gfx.*;
import snap.props.*;
import snap.text.NumberFormat;
import snap.util.XMLElement;
import snapcharts.data.*;
import snapcharts.model.*;
import java.util.HashMap;
import java.util.Map;

/**
 * An archiver for SnapCharts document file.
 */
public class ChartArchiver extends PropArchiverXML {

    /**
     * Constructor.
     */
    public ChartArchiver()
    {
        super();
    }

    /**
     * Convenience.
     */
    public Doc getDocFromXMLBytes(byte[] xmlBytes)
    {
        Doc doc = (Doc) readPropObjectFromXMLBytes(xmlBytes);
        return doc;
    }

    /**
     * Returns a ChartPart for XML source.
     */
    public ChartPart getChartPartFromXMLSource(Object anObj)
    {
        ChartPart cpart = (ChartPart) readPropObjectFromXMLSource(anObj);
        return cpart;
    }

    /**
     * Returns an xml element for a given object.
     */
    public XMLElement writeToXML(PropObject anObj)
    {
        XMLElement xml = convertPropObjectToXML(anObj);
        return xml;
    }

    /**
     * Writes given object to XML and returns the XML bytes.
     */
    public byte[] writeToXMLBytes(PropObject anObj)
    {
        XMLElement xml = writeToXML(anObj);
        return xml.getBytes();
    }

    /**
     * Returns a copy of the given object using archival.
     */
    public <T extends PropObject> T copy(T anObj)
    {
        XMLElement xml = convertPropObjectToXML(anObj);
        return (T) readPropObjectFromXML(xml);
    }

    /**
     * Override to handle DataSet.
     */
    @Override
    protected PropObject createPropObjectForXML(PropNode aParent, Prop aProp, XMLElement anElement)
    {
        // Handle DataSet
        if (aProp != null && aProp.getDefaultPropClass() == DataSet.class)
            return new DataSetProxy();

        // Do normal version
        return super.createPropObjectForXML(aParent, aProp, anElement);
    }

    /**
     * Creates the class map.
     */
    @Override
    protected Map<String, Class<?>> createClassMap()
    {
        // Create class map and add classes
        Map<String,Class<?>> cmap = new HashMap();

        // Chart classes
        cmap.put(Doc.class.getSimpleName(), Doc.class);
        cmap.put(Chart.class.getSimpleName(), Chart.class);
        cmap.put(Header.class.getSimpleName(), Header.class);
        cmap.put(Axis.class.getSimpleName(), Axis.class);
        cmap.put(AxisX.class.getSimpleName(), AxisX.class);
        cmap.put(AxisY.class.getSimpleName(), AxisY.class);
        cmap.put(Legend.class.getSimpleName(), Legend.class);
        cmap.put(ContourAxis.class.getSimpleName(), ContourAxis.class);
        cmap.put(Marker.class.getSimpleName(), Marker.class);

        // Trace classes
        cmap.put(Content.class.getSimpleName(), Content.class);
        cmap.put(Trace.class.getSimpleName(), Trace.class);
        cmap.put(PointStyle.class.getSimpleName(), PointStyle.class);
        cmap.put(TagStyle.class.getSimpleName(), TagStyle.class);
        cmap.put(TraceStyle.class.getSimpleName(), TraceStyle.class);

        // Add Graphics classes (Border, Paint, Font, NumberFormat, Effect)
        cmap.put(Color.class.getSimpleName(), Color.class);
        cmap.put(Font.class.getSimpleName(), Font.class);
        cmap.put(NumberFormat.class.getSimpleName(), NumberFormat.class);
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

    /**
     * A PropObjectProxy subclass for DataSet.
     */
    protected static class DataSetProxy extends PropObjectProxy {

        // A basic DataSet
        private DataSet  _dataSet;

        // The DataArrays
        private DataArray[]  _dataArrays;

        // The DataUnits
        private DataUnit[]  _dataUnits;

        /**
         * Constructor.
         */
        public DataSetProxy()
        {
            _dataSet = DataSet.newDataSet();

            // Init DataArrays
            DataType dataType = _dataSet.getDataType();
            _dataArrays = new DataArray[dataType.getChannelCount()];
            _dataUnits = new DataUnit[dataType.getChannelCount()];
        }

        /**
         * Override to do final DataSet config.
         */
        @Override
        public Object getReal()
        {
            // Set DataUnits in DataArrays
            for (int i = 0; i < _dataUnits.length; i++) {
                DataUnit dataUnit = _dataUnits[i]; if (dataUnit == null) continue;;
                DataArray dataArray = _dataArrays[i];
                if (dataArray instanceof NumberArray)
                    ((NumberArray) dataArray).setUnit(dataUnit);
            }

            // Set DataArrays
            _dataSet.setDataArrays(_dataArrays);

            // Return
            return _dataSet;
        }

        /**
         * Override to configure props for this class.
         */
        @Override
        protected void initProps(PropSet aPropSet)
        {
            // Do normal version
            super.initProps(aPropSet);

            // Get props for DataSet
            Prop[] dataSetProps = _dataSet.getPropSet().getProps();

            // Add DataSet props - should probably add copies
            for (Prop prop : dataSetProps)
                aPropSet.addProp(prop);
        }

        /**
         * Override to forward to DataSet.
         */
        @Override
        public Prop[] getPropsForArchivalExtra()
        {
            return _dataSet.getPropsForArchivalExtra();
        }

        /**
         * Override to forward to DataSet.
         */
        @Override
        public Object getPropValue(String aPropName)
        {
            return _dataSet.getPropValue(aPropName);
        }

        /**
         * Override to Handle DataType props.
         */
        @Override
        public void setPropValue(String aPropName, Object aValue)
        {
            // Set DataArrays
            int dataIndex = getDataChannelIndexForPropName(aPropName);
            if (dataIndex >= 0) {
                if (aValue instanceof double[])
                    _dataArrays[dataIndex] = new NumberArray((double[]) aValue);
                else if (aValue instanceof String[])
                    _dataArrays[dataIndex] = new StringArray((String[]) aValue);
                else System.err.println("DataSetProxy.setPropValue: Error setting data: " + aPropName);
                return;
            }

            // Set DataUnits
            if (aValue instanceof DataUnit && aPropName.endsWith("Unit")) {
                dataIndex = getDataChannelIndexForPropName(aPropName.replace("Unit", ""));
                if (dataIndex >= 0)
                    _dataUnits[dataIndex] = (DataUnit) aValue;
                else System.err.println("DataSetProxy.setPropValue: Error setting data unit: " + aPropName);
                return;
            }

            // Forward to DataSet
            _dataSet.setPropValue(aPropName, aValue);

            // Handle DataType
            if (aPropName == DataSet.DataType_Prop) {
                DataType dataType = _dataSet.getDataType();
                _dataArrays = new DataArray[dataType.getChannelCount()];
                _dataUnits = new DataUnit[dataType.getChannelCount()];
            }
        }

        /**
         * Returns the channel/DataArrays index for given channel prop name (X, Y, Z, etc.)
         */
        private int getDataChannelIndexForPropName(String aPropName)
        {
            DataType dataType = _dataSet.getDataType();
            for (int i = 0, iMax = dataType.getChannelCount(); i < iMax; i++)
                if (dataType.getChannel(i).toString().equals(aPropName))
                    return i;
            return -1;
        }
    }

    /**
     * Override to preprocess Legacy XML if needed.
     */
    @Override
    public PropObject readPropObjectFromXML(XMLElement anElement)
    {
        // Preprocess XML
        if (ChartArchiverLegacy.isLegacyXML(anElement))
            ChartArchiverLegacy.processLegacyXML(anElement);

        // Do normal version
        return super.readPropObjectFromXML(anElement);
    }
}
