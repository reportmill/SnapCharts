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
import snapcharts.notebook.DocItemNotebook;
import snapcharts.notebook.Notebook;

import java.util.HashMap;
import java.util.Map;

/**
 * An archiver for SnapCharts document file.
 */
public class ChartArchiver2 extends PropArchiverXML {

    /**
     * Constructor.
     */
    public ChartArchiver2()
    {
        //setIgnoreCase(true);
    }

    /**
     * Converts given object to PropNode or primitive.
     */
    @Override
    protected Object convertNativeToNodeForPropRelationImpl(PropNode aParentNode, Prop aProp, Object nativeValue)
    {
        // Do normal version
        Object superVal = super.convertNativeToNodeForPropRelationImpl(aParentNode, aProp, nativeValue);

        // If DocItemGroup.Items (and Doc), replace Items with Items[].Content
        PropObject propObject = aParentNode.getPropObject();
        if (propObject instanceof DocItemGroup && aProp.getName() == DocItemGroup.Items_Prop) {
            PropNode[] docItemsNodes = (PropNode[]) superVal;
            for (int i = 0; i < docItemsNodes.length; i++) {
                PropNode docItemNode = docItemsNodes[i];
                PropNode contentPropNode = (PropNode) docItemNode.getNodeValueForPropName(DocItem.Content_Prop);
                docItemsNodes[i] = contentPropNode;
            }
        }

        // Return
        return superVal;
    }

    /**
     * Override to convert XML Doc.Items from array of DocItem.Content to array of DocItems.
     */
    @Override
    protected PropNode[] convertXMLToNodeForXMLRelationArray(PropNode aParentNode, Prop aProp, XMLElement anElement)
    {
        // Do normal version
        PropNode[] superVal = super.convertXMLToNodeForXMLRelationArray(aParentNode, aProp, anElement);

        //
        PropObject propObject = aParentNode.getPropObject();
        if (propObject instanceof DocItemGroup && aProp.getName() == DocItemGroup.Items_Prop) {
            PropNode[] docItemsNodes = superVal;
            for (int i = 0; i < docItemsNodes.length; i++) {
                PropNode docItemNode = docItemsNodes[i];
                Object docItemContent = docItemNode.getNative();
                DocItem docItem = null;
                if (docItemContent instanceof Chart)
                    docItem = new DocItemChart((Chart) docItemContent);
                else if (docItemContent instanceof Notebook)
                    docItem = new DocItemNotebook((Notebook) docItemContent);
                else {
                    System.err.println("ChartArchiver.getDocItemForContent: Unsupported: " + docItemContent.getClass());
                    docItem = new DocItemChart(new Chart());
                }

                //
                PropNode docItemNode2 = new PropNode(docItem, this);
                docItemsNodes[i] = docItemNode2;
            }
        }

        // Return
        return superVal;
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

    /**
     * A PropObjectProxy subclass for DataSet.
     */
    private static class DataSetProxy extends PropObjectProxy {

        // A basic DataSet
        private DataSet  _dataSet;

        // The DataArray
        private DataArray[] _dataArrays;

        /**
         * Constructor.
         */
        public DataSetProxy()
        {
            _dataSet = DataSet.newDataSet();
        }

        /**
         * Override to do final DataSet config.
         */
        @Override
        public Object getReal()
        {
            _dataSet.setDataArrays(_dataArrays);
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

            // Forward to DataSet
            _dataSet.setPropValue(aPropName, aValue);

            // Handle DataType
            if (aPropName == DataSet.DataType_Prop) {
                DataType dataType = _dataSet.getDataType();
                _dataArrays = new DataArray[dataType.getChannelCount()];
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
}
