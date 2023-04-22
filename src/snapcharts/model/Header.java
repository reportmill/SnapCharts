/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;
import snap.geom.Insets;
import snap.gfx.*;
import snap.props.PropSet;
import snap.util.Convert;
import java.util.Objects;

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
    public static final Font DEFAULT_HEADER_FONT = Font.Arial14.getBold();
    public static final Insets  DEFAULT_HEADER_MARGIN = new Insets(0, 0, 8, 0);
    public static final double  DEFAULT_HEADER_SPACING = 2;

    /**
     * Constructor.
     */
    public Header()
    {
        super();

        // Override defaults
        _font = DEFAULT_HEADER_FONT;
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
        if (Objects.equals(aStr, getTitle())) return;
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
        if (Objects.equals(aStr, getSubtitle())) return;
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
     * Override to register props.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // Override Font, Margin, Spacing
        aPropSet.getPropForName(Font_Prop).setDefaultValue(DEFAULT_HEADER_FONT);
        aPropSet.getPropForName(Margin_Prop).setDefaultValue(DEFAULT_HEADER_MARGIN);
        aPropSet.getPropForName(Spacing_Prop).setDefaultValue(DEFAULT_HEADER_SPACING);

        // Title, Subtitle
        aPropSet.addPropNamed(Title_Prop, String.class, null);
        aPropSet.addPropNamed(Subtitle_Prop, String.class, null);
    }

    /**
     * Returns the prop value for given key.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        // Handle properties
        switch (aPropName) {

            // Handle Title, Subtitle
            case Title_Prop: return getTitle();
            case Subtitle_Prop: return getSubtitle();

            // Handle super class properties (or unknown)
            default: return super.getPropValue(aPropName);
        }
    }

    /**
     * Sets the prop value for given key.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        // Handle properties
        switch (aPropName) {

            // Handle Title, Subtitle
            case Title_Prop: setTitle(Convert.stringValue(aValue)); break;
            case Subtitle_Prop: setSubtitle(Convert.stringValue(aValue)); break;

            // Handle super class properties (or unknown)
            default: super.setPropValue(aPropName, aValue);
        }
    }
}
