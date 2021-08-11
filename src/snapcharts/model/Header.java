/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;
import snap.geom.Insets;
import snap.gfx.Font;
import snap.util.SnapUtils;
import snap.util.XMLArchiver;
import snap.util.XMLElement;

/**
 * A ChartPart to represent Header.
 */
public class Header extends ChartPart {

    // The title
    private String  _title;

    // The subtitle
    private String  _subtitle;

    // The subtitle font
    private Font  _subtitleFont;

    // Constants for properties
    public static final String Title_Prop = "Title";
    public static final String Subtitle_Prop = "Subtitle";

    // Constants for defaults
    public static final Insets  DEFAULT_HEADER_MARGIN = new Insets(0, 0, 8, 0);
    public static final double  DEFAULT_HEADER_SPACING = 2;
    public static final Font  DEFAULT_TITLE_FONT = Font.Arial14.getBold();

    /**
     * Constructor.
     */
    public Header()
    {
        super();

        // Override defaults
        _margin = DEFAULT_HEADER_MARGIN;
        _spacing = DEFAULT_HEADER_SPACING;
    }

    /**
     * Returns the title.
     */
    public String getTitle()  { return _title; }

    /**
     * Sets the title.
     */
    public void setTitle(String aStr)
    {
        if (SnapUtils.equals(aStr, getTitle())) return;
        firePropChange(Title_Prop, _title, _title = aStr);
    }

    /**
     * Returns the subtitle.
     */
    public String getSubtitle()  { return _subtitle; }

    /**
     * Sets the subtitle.
     */
    public void setSubtitle(String aStr)
    {
        if (SnapUtils.equals(aStr, getSubtitle())) return;
        firePropChange(Subtitle_Prop, _subtitle, _subtitle = aStr);
    }

    /**
     * Returns the subtitle font.
     */
    public Font getSubtitleFont()
    {
        if (_subtitleFont != null) return _subtitleFont;

        // If not set, return Header font reduced by 2
        Font headerFont = getFont();
        Font subtitleFont = headerFont.isBold() ? headerFont.getBold() : headerFont;
        subtitleFont = subtitleFont.deriveFont(subtitleFont.getSize() - 2);
        return subtitleFont;
    }

    /**
     * Sets the subtitle font.
     */
    public void setSubtitleFont(Font aFont)
    {
        _subtitleFont = aFont;
    }

    /**
     * Returns the prop default value for given key.
     */
    @Override
    public Object getPropDefault(String aPropName)
    {
        // Handle properties
        switch (aPropName) {

            // Handle Margin, Spacing
            case Margin_Prop: return DEFAULT_HEADER_MARGIN;
            case Spacing_Prop: return DEFAULT_HEADER_SPACING;

            // Handle Font
            case Font_Prop: return DEFAULT_TITLE_FONT;

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

        // Archive Title, Subtitle
        if (getTitle()!=null && getTitle().length()>0)
            e.add(Title_Prop, getTitle());
        if (getSubtitle()!=null && getSubtitle().length()>0)
            e.add(Subtitle_Prop, getSubtitle());

        // Return element
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

        // Unarchive Title, Subtitle
        setTitle(anElement.getAttributeValue(Title_Prop));
        setSubtitle(anElement.getAttributeValue(Subtitle_Prop));

        // Return this part
        return this;
    }
}
