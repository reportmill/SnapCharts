/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;
import snap.props.PropSet;
import snap.util.Convert;
import java.util.Objects;

/**
 * This ChartPart subclass represents chart parts that are primarily text, like the Legend Title.
 */
public class ChartText extends ChartPart {

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
     * Override to register props.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // Text
        aPropSet.addPropNamed(Text_Prop, String.class, null);
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
            case Text_Prop: setText(Convert.stringValue(aValue)); break;

            // Handle superclass
            default: super.setPropValue(aPropName, aValue); break;
        }
    }
}
