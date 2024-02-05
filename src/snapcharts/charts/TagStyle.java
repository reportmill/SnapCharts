/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.charts;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.gfx.Paint;
import snap.props.PropSet;

/**
 * This StyledChartPart subclass holds style attributes for data tags.
 */
public class TagStyle extends ChartPart {

    // The Trace that holds this TagStyle
    private Trace  _trace;

    // Constants for property defaults
    public static final Color DEFAULT_TAG_LINE_COLOR = Trace.DEFAULT_DYNAMIC_COLOR;
    public static final int DEFAULT_TAG_LINE_WIDTH = 0;
    public static final Color DEFAULT_TAG_FILL = Trace.DEFAULT_DYNAMIC_COLOR;
    public static final Font DEFAULT_TAG_FONT = Font.Arial10;

    /**
     * Constructor.
     */
    public TagStyle(Trace aTrace)
    {
        super();
        _trace = aTrace;

        // Set super default values
        _lineWidth = DEFAULT_TAG_LINE_WIDTH;
        _font = DEFAULT_TAG_FONT;

        // Set defaults special: These are computed dynamic if not explicitly set
        _lineColor = Trace.DEFAULT_DYNAMIC_COLOR;
        _fill = Trace.DEFAULT_DYNAMIC_COLOR;
    }

    /**
     * Override to dynamically get line color if not explicitly set.
     */
    @Override
    public Color getLineColor()
    {
        if (_lineColor == Trace.DEFAULT_DYNAMIC_COLOR)
            return _trace.getDefaultLineColor();
        return super.getLineColor();
    }

    /**
     * Override to dynamically get fill color if not explicitly set.
     */
    @Override
    public Paint getFill()
    {
        if (_fill == Trace.DEFAULT_DYNAMIC_COLOR)
            return getDefaultFill();
        return super.getFill();
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
     * Override to register props.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // Override Font
        aPropSet.getPropForName(LineWidth_Prop).setDefaultValue(DEFAULT_TAG_LINE_WIDTH);
        aPropSet.getPropForName(Font_Prop).setDefaultValue(DEFAULT_TAG_FONT);
    }

    /**
     * Override to provide TagStyle defaults.
     */
    @Override
    public Object getPropDefault(String aPropName)
    {
        switch (aPropName) {

            // Handle LineColor, Fill
            case LineColor_Prop: return _trace.getLineColor();
            case Fill_Prop: return getDefaultFill();

            // Do normal version
            default: return super.getPropDefault(aPropName);
        }
    }
}