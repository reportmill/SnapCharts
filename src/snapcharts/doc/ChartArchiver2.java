/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.doc;
import snap.gfx.*;
import snap.props.Prop;
import snap.props.PropArchiverXML;
import snap.props.PropNode;
import snap.props.PropObject;
import snap.text.NumberFormat;
import snapcharts.model.*;
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
    protected Object convertNativeToNodeForPropRelationImpl(PropNode aPropNode, Prop aProp, Object nativeValue)
    {
        // Do normal version
        Object superVal = super.convertNativeToNodeForPropRelationImpl(aPropNode, aProp, nativeValue);

        // If DocItemGroup.Items (and Doc), replace Items with Items[].Content
        PropObject propObject = aPropNode.getPropObject();
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
}
