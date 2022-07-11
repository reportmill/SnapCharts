/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.doc;
import snap.util.XMLAttribute;
import snap.util.XMLElement;
import snapcharts.model.Chart;
import snapcharts.model.TraceType;

/**
 * This class processes Legacy XML to make it conform to new stuff.
 */
public class ChartArchiverLegacy {

    // Handle legacy ChartTypes
    private boolean  _isLine, _isArea;

    /**
     * Returns whether XML is legacy.
     */
    public static boolean isLegacyXML(XMLElement anElement)
    {
        return anElement.getElement("Charts") != null;
    }

    /**
     * Process legacy XML.
     */
    public static void processLegacyXML(XMLElement anElement)
    {
        new ChartArchiverLegacy().processLegacyXMLDeep(anElement);
    }

    /**
     * Process legacy XML.
     */
    protected void processLegacyXMLDeep(XMLElement anElement)
    {
        processLegacyXMLElement(anElement);

        XMLAttribute[] attrs = anElement.getAttributes().toArray(new XMLAttribute[0]);
        for (XMLAttribute attr : attrs)
            processLegacyXMLAttribute(anElement, attr);
        for (XMLElement xml : anElement.getElements())
            processLegacyXMLDeep(xml);

        // Special handling for old Chart.Type (ChartType)
        if (anElement.getName().equals("Chart"))
            processLegacyChartType(anElement);
    }

    /**
     * Process legacy XML.
     */
    protected void processLegacyXMLElement(XMLElement anElement)
    {
        String name = anElement.getName();

        switch (name) {

            // Handle TraceList/DataSetList: Move children to new <Traces> element
            case "DataSetList":
            case "TraceList": anElement.setName(Chart.Content_Prop);

                // Move children to <Traces>
                XMLElement[] traceXMLs = anElement.getElements().toArray(new XMLElement[0]);
                while (anElement.getElementCount() > 0) anElement.removeElement(0);
                XMLElement tracesXML = new XMLElement("Traces");
                for (XMLElement traceXML : traceXMLs)
                    tracesXML.addElement(traceXML);
                anElement.addElement(tracesXML);
                break;

            // Handle Charts: Rename to Items
            case "Charts": anElement.setName(Doc.Items_Prop); break;

            // Handle Trace/DataSet: Rename to Trace
            case "DataSet": anElement.setName("Trace");
            case "Trace":

                // If DataStyle element, move all attributes to Trace
                XMLElement dataStyleXML = anElement.getElement("DataStyle");
                if (dataStyleXML != null) {
                    for (XMLAttribute attr : dataStyleXML.getAttributes())
                        anElement.addAttribute(attr);
                    anElement.removeElement(dataStyleXML);
                }

                // Create new DataSet xml and add to Trace with DataType
                XMLElement dataSetXML = new XMLElement("DataSetNew");
                anElement.addElement(dataSetXML);
                if (anElement.hasAttribute("DataType")) {
                    dataSetXML.add("DataType", anElement.getAttributeValue("DataType"));
                    anElement.removeAttribute("DataType");
                }

                // If DataX/Y/Z/C, move to new DataSet
                for (XMLElement childXML : anElement.getElements().toArray(new XMLElement[0])) {
                    String cname = childXML.getName();
                    if (cname.equals("DataX") || cname.equals("DataY") || cname.equals("DataZ") || cname.equals("DataC")) {
                        anElement.removeElement(childXML);
                        dataSetXML.addElement(childXML);
                    }
                }
                break;

            // Handle DataSetNew
            case "DataSetNew": anElement.setName("DataSet"); break;

            // Handle DataX, DataY, DataZ, DataC
            case "DataX": anElement.setName("X"); break;
            case "DataY": anElement.setName("Y"); break;
            case "DataZ": anElement.setName("Z"); break;
            case "DataC": anElement.setName("C"); break;

            // Default
            default: break;
        }
    }

    /**
     * Process legacy XML.
     */
    protected void processLegacyXMLAttribute(XMLElement anElement, XMLAttribute anAttr)
    {
        String name = anAttr.getName();
        switch (name) {
            case "name": anAttr.setName("Name"); break;
            case "size": anAttr.setName("Size"); break;
            case "value": anAttr.setName("Color"); break;
            case "ClassName": anAttr.setName("Class"); break;
            case "ShowSymbols": anAttr.setName("ShowPoints"); break;
            default:break;
        }
    }

    /**
     * Handles legacy Chart.Type (ChartType). Converts it to TraceType and propagates it to Chart.Content.Traces.
     */
    protected void processLegacyChartType(XMLElement chartXML)
    {
        // Get ChartTypeString
        XMLAttribute chartTypeAttr = chartXML.getAttribute("Type");
        String chartTypeStr = chartTypeAttr.getValue();
        chartXML.removeAttribute(chartTypeAttr);

        // Really old stuff: Turn legacy ChartType LINE/AREA to Scatter
        if (chartTypeStr.equals("LINE")) { _isLine = true; chartTypeAttr.setName(chartTypeStr = "SCATTER"); }
        else if(chartTypeStr.equals("AREA")) { _isArea = true; chartTypeAttr.setName(chartTypeStr = "SCATTER"); }
        chartTypeStr = chartTypeStr.replace("_", "");

        // Get DefaultTraceType for ChartType
        TraceType traceType = TraceType.getTypeForName(chartTypeStr);

        // Add DefaultTraceType to all Chart.Content.Traces.[Trace] elements
        XMLElement contentXML = chartXML.getElement("Content");
        XMLElement tracesXML = contentXML.getElement("Traces");
        XMLElement[] traceXMLs = tracesXML.getElements("Trace").toArray(new XMLElement[0]);
        for (XMLElement traceXML : traceXMLs) {
            XMLAttribute typeAttr = new XMLAttribute("Type", traceType.getName());
            traceXML.addAttribute(typeAttr, 1);
        }
    }

}
