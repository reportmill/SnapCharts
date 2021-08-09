/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;
import snap.util.SnapUtils;
import snap.util.XMLArchiver;
import snap.util.XMLElement;
import java.util.Objects;

/**
 * This StyledChartPart subclass represents chart parts that are primarily text, like the Legend Title.
 */
public class ChartText extends StyledChartPart {

    // The text
    private String  _text;

    // Constants for properties
    public static final String Text_Prop = "Text";

    /**
     * Returns the text.
     */
    public String getText()  { return _text; }

    /**
     * Sets the text.
     */
    public void setText(String aString)
    {
        if (Objects.equals(aString, getText())) return;
        firePropChange(Text_Prop, _text, _text = aString);
    }

    /**
     * Override support ChartText props.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        switch (aPropName) {

            // Handle Text
            case Text_Prop: return getText();

            // Handle superclass
            default: return super.getPropValue(aPropName);
        }
    }

    /**
     * Override support ChartText props.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        switch (aPropName) {

            // Handle Text
            case Text_Prop: setText(SnapUtils.stringValue(aValue)); break;

            // Handle superclass
            default: super.setPropValue(aPropName, aValue); break;
        }
    }

    /**
     * Archival.
     */
    @Override
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Do normal version
        XMLElement e = super.toXML(anArchiver);

        // Archive Text
        if (getText() != null && getText().length() > 0)
            e.add(Text_Prop, getText());

        // Return xml
        return e;
    }

    /**
     * Unarchival.
     */
    @Override
    public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Do normal version
        super.fromXML(anArchiver, anElement);

        // Unarchive Text
        if (anElement.hasAttribute(Text_Prop))
            setText(anElement.getAttributeValue(Text_Prop));

        // Return this object
        return this;
    }
}
