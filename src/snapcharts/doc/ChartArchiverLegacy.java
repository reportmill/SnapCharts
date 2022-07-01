/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.doc;
import snap.util.XMLAttribute;
import snap.util.XMLElement;

/**
 * This class processes Legacy XML to make it conform to new stuff.
 */
public class ChartArchiverLegacy {

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

        for (XMLAttribute attr : anElement.getAttributes())
            processLegacyXMLAttribute(attr);
        for (XMLElement xml : anElement.getElements())
            processLegacyXMLDeep(xml);
    }

    /**
     * Process legacy XML.
     */
    protected void processLegacyXMLElement(XMLElement anElement)
    {
        String name = anElement.getName();

        switch (name) {

            // Handle TraceList/DataSetList: Move children to new <Traces> element
            case "DataSetList": anElement.setName("TraceList");
            case "TraceList":

                // Move children to <Traces>
                XMLElement[] traceXMLs = anElement.getElements().toArray(new XMLElement[0]);
                while (anElement.getElementCount() > 0) anElement.removeElement(0);
                XMLElement tracesXML = new XMLElement("Traces");
                for (XMLElement traceXML : traceXMLs)
                    tracesXML.addElement(traceXML);
                anElement.addElement(tracesXML);
                break;

            // Handle Charts: Rename to Items
            case "Charts": anElement.setName("Items"); break;

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
    protected void processLegacyXMLAttribute(XMLAttribute anAttr)
    {
        String name = anAttr.getName();
        switch (name) {
            case "name": anAttr.setName("Name"); break;
            case "size": anAttr.setName("Size"); break;
            case "value": anAttr.setName("Color"); break;
            case "ClassName": anAttr.setName("Class"); break;
            case "ShowSymbols": anAttr.setName("ShowPoints"); break;
            case "Type":
                String attrValue = anAttr.getValue();
                if (attrValue.equals("LINE") || attrValue.equals("AREA"))
                    anAttr.setValue("SCATTER");
                break;
            default:break;
        }
    }
}
