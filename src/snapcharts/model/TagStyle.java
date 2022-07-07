/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.props.PropSet;

/**
 * This StyledChartPart subclass holds style attributes for data tags.
 */
public class TagStyle extends ChartPart {

    // The Trace that holds this TagStyle
    private Trace  _trace;

    // Constants for property defaults
    public static final Font DEFAULT_TAG_FONT = Font.Arial10;
    public static final Color DEFAULT_TAG_COLOR = null;
    public static final Color DEFAULT_TAG_BORDER_COLOR = null;
    public static final int DEFAULT_TAG_BORDER_WIDTH = 0;

    /**
     * Constructor.
     */
    public TagStyle(Trace aTrace)
    {
        super();
        _trace = aTrace;
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
     * Override to register props.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // Override Font
        aPropSet.getPropForName(Font_Prop).setDefaultValue(DEFAULT_TAG_FONT);
    }

    /**
     * Override to provide TagStyle defaults.
     */
    @Override
    public Object getPropDefault(String aPropName)
    {
        switch (aPropName) {

            // Handle Fill, LineColor
            case Fill_Prop: return getDefaultFill();
            case LineColor_Prop: return _trace.getLineColor();

            // Do normal version
            default: return super.getPropDefault(aPropName);
        }
    }
}