/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.util.XMLArchiver;
import snap.util.XMLElement;

/**
 * This StyledChartPart subclass holds style attributes for data tags.
 */
public class TagStyle extends StyledChartPart {

    // The DataStyle that holds this SymbolStyle
    private DataStyle  _dataStyle;

    // Constants for property defaults
    public static final Font DEFAULT_TAG_FONT = Font.Arial10;
    public static final Color DEFAULT_TAG_COLOR = null;
    public static final Color DEFAULT_TAG_BORDER_COLOR = null;
    public static final int DEFAULT_TAG_BORDER_WIDTH = 0;

    /**
     * Constructor.
     */
    public TagStyle(DataStyle aDataStyle)
    {
        super();
        _dataStyle = aDataStyle;
    }

    /**
     * Returns the default tag fill color.
     */
    private Color getDefaultFill()
    {
        if (getLineWidth() <= 0)
            return null;
        return Color.WHITE;
    }

    /**
     * Override to prevent client code from using border instead of line props.
     */
    @Override
    public boolean isBorderSupported()  { return false; }

    /**
     * Override to provide TagStyle defaults.
     */
    @Override
    public Object getPropDefault(String aPropName)
    {
        switch (aPropName) {

            // Handle LineColor_Prop
            case LineColor_Prop: return _dataStyle.getLineColor();

            // Handle LineColor_Prop
            case Fill_Prop: return getDefaultFill();

            // Handle Font
            case Font_Prop: return DEFAULT_TAG_FONT;

            // Do normal version
            default: return super.getPropDefault(aPropName);
        }
    }

    /**
     * Archival.
     */
    @Override
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Archive basic attributes
        XMLElement e = super.toXML(anArchiver);
        e.setName("TagStyle");

        // Return xml
        return e;
    }

    /**
     * Unarchival.
     */
    @Override
    public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive basic attributes
        super.fromXML(anArchiver, anElement);

        // Return
        return this;
    }
}