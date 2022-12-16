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
import snapcharts.modelx.*;
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
        Map<String,Class<?>> classMap = new HashMap<>();

        // Chart classes
        classMap.put(Doc.class.getSimpleName(), Doc.class);
        classMap.put(Chart.class.getSimpleName(), Chart.class);
        classMap.put(Header.class.getSimpleName(), Header.class);
        classMap.put(Axis.class.getSimpleName(), Axis.class);
        classMap.put(AxisX.class.getSimpleName(), AxisX.class);
        classMap.put(AxisY.class.getSimpleName(), AxisY.class);
        classMap.put(Legend.class.getSimpleName(), Legend.class);
        classMap.put(ContourAxis.class.getSimpleName(), ContourAxis.class);
        classMap.put(Marker.class.getSimpleName(), Marker.class);
        classMap.put(Content.class.getSimpleName(), Content.class);

        // Trace classes
        classMap.put(Trace.class.getSimpleName(), ScatterTrace.class);
        classMap.put(ScatterTrace.class.getSimpleName(), ScatterTrace.class);
        classMap.put(BarTrace.class.getSimpleName(), BarTrace.class);
        classMap.put(Bar3DTrace.class.getSimpleName(), Bar3DTrace.class);
        classMap.put(PieTrace.class.getSimpleName(), PieTrace.class);
        classMap.put(Pie3DTrace.class.getSimpleName(), Pie3DTrace.class);
        classMap.put(ContourTrace.class.getSimpleName(), ContourTrace.class);
        classMap.put(Contour3DTrace.class.getSimpleName(), Contour3DTrace.class);
        classMap.put(PolarTrace.class.getSimpleName(), PolarTrace.class);
        classMap.put(PolarContourTrace.class.getSimpleName(), PolarContourTrace.class);
        classMap.put(PointStyle.class.getSimpleName(), PointStyle.class);
        classMap.put(TagStyle.class.getSimpleName(), TagStyle.class);

        // Add Graphics classes (Border, Paint, Font, NumberFormat, Effect)
        classMap.put(Color.class.getSimpleName(), Color.class);
        classMap.put(Font.class.getSimpleName(), Font.class);
        classMap.put(NumberFormat.class.getSimpleName(), NumberFormat.class);
        classMap.put("BevelBorder", Borders.BevelBorder.class);
        classMap.put("EtchBorder", Borders.EtchBorder.class);
        classMap.put("LineBorder", Borders.LineBorder.class);
        classMap.put("GradientPaint", GradientPaint.class); //RMGradientFill.class
        classMap.put("ImagePaint", ImagePaint.class); //RMImageFill.class
        classMap.put("BlurEffect", BlurEffect.class);
        classMap.put("ShadowEffect", ShadowEffect.class);
        classMap.put("ReflectEffect", ReflectEffect.class);
        classMap.put("EmbossEffect", EmbossEffect.class);

        // Return
        return classMap;
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

    /**
     * A PropObjectProxy subclass for DataSet.
     */
    protected static class DataSetProxy extends PropObjectProxy<DataSet> {

        // The DataArrays
        private DataArray[]  _dataArrays;

        // The DataUnits
        private DataUnit[]  _dataUnits;

        /**
         * Constructor.
         */
        public DataSetProxy()
        {
            _real = DataSet.newDataSet();

            // Init DataArrays
            DataType dataType = _real.getDataType();
            _dataArrays = new DataArray[dataType.getChannelCount()];
            _dataUnits = new DataUnit[dataType.getChannelCount()];
        }

        /**
         * Override to do final DataSet config.
         */
        @Override
        public DataSet getReal()
        {
            // Set DataUnits in DataArrays
            for (int i = 0; i < _dataUnits.length; i++) {
                DataUnit dataUnit = _dataUnits[i]; if (dataUnit == null) continue;
                DataArray dataArray = _dataArrays[i];
                if (dataArray instanceof NumberArray)
                    ((NumberArray) dataArray).setUnit(dataUnit);
            }

            // Set DataArrays
            _real.setDataArrays(_dataArrays);

            // Return
            return _real;
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
                    _dataArrays[dataIndex] = new DoubleArray((double[]) aValue);
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
            _real.setPropValue(aPropName, aValue);

            // Handle DataType
            if (aPropName == DataSet.DataType_Prop) {
                DataType dataType = _real.getDataType();
                _dataArrays = new DataArray[dataType.getChannelCount()];
                _dataUnits = new DataUnit[dataType.getChannelCount()];
            }
        }

        /**
         * Returns the channel/DataArrays index for given channel prop name (X, Y, Z, etc.)
         */
        private int getDataChannelIndexForPropName(String aPropName)
        {
            DataType dataType = _real.getDataType();
            for (int i = 0, iMax = dataType.getChannelCount(); i < iMax; i++)
                if (dataType.getChannel(i).toString().equals(aPropName))
                    return i;
            return -1;
        }
    }
}
