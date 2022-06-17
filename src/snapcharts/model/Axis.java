/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;
import snap.geom.*;
import snap.gfx.Color;
import snap.gfx.Stroke;
import snap.props.PropSet;
import snap.text.NumberFormat;
import snap.text.NumberFormat.ExpStyle;
import snap.util.*;
import snapcharts.util.MinMax;
import java.util.Objects;

/**
 * A class to represent a Chart Axis.
 */
public abstract class Axis extends ChartPart {

    // The Title
    private String  _title;

    // The title rotation
    private double  _titleRotation;

    // The Axis Min Bounding
    private AxisBound  _minBound = AxisBound.AUTO;

    // The Axis Min Bounding
    private AxisBound  _maxBound = AxisBound.AUTO;

    // The Axis Min Value (if MinBounding is VALUE)
    private double  _minValue;

    // The Axis Max Value (if MaxBounding is VALUE)
    private double  _maxValue;

    // Whether Zero should always be included in auto range
    private boolean  _zeroRequired;

    // Whether to paint Zero line in distinct color when in range
    private boolean  _showZeroLine;

    // Whether axis is log10 based
    private boolean  _log;

    // Whether axis should show log minor labels
    private boolean  _showLogMinorLabels;

    // Whether axis repeats/wraps values
    private boolean  _wrapAxis;

    // The wrap min/max value
    private MinMax  _wrapMinMax;

    // The Side this axis is on
    protected Side  _side;

    // Whether to show grid lines
    private boolean  _showGrid;

    // The grid line color
    private Color  _gridColor;

    // The grid line width
    private int  _gridWidth;

    // The grid line
    private double[]  _gridDash;

    // The amount of space separating gridlines (in axis data coords)
    private double  _gridSpacing;

    // The base location of first grid line (usually zero)
    private double  _gridBase;

    // The length of hash mark drawn perpendicular to the axis line for each interval
    private double  _tickLength;

    // The position of the tick
    private TickPos  _tickPos;

    // The number of minor tick marks between major ticks
    private int  _minorTickCount;

    // Whether to show TickLabels
    private boolean  _showTickLabels;

    // Whether TickLabels auto rotate
    private boolean  _tickLabelAutoRotate;

    // The angle of the TickLabels
    private double _tickLabelRotation;

    // Constants for properties
    public static final String Title_Prop = "Title";
    public static final String TitleRotation_Prop = "TitleRotation";
    public static final String MinBound_Prop = "MinBound";
    public static final String MaxBound_Prop = "MaxBound";
    public static final String MinValue_Prop = "MinValue";
    public static final String MaxValue_Prop = "MaxValue";
    public static final String ZeroRequired_Prop = "ZeroRequired";
    public static final String ShowZeroLine_Prop = "ShowZeroLine";
    public static final String Log_Prop = "Logarithmic";
    public static final String ShowLogMinorLabels_Prop = "ShowLogMinorLabels";
    public static final String Side_Prop = "Side";
    public static final String WrapAxis_Prop = "WrapAxis";
    public static final String WrapMinMax_Prop = "WrapMinMax";
    public static final String ShowGrid_Prop = "ShowGrid";
    public static final String GridColor_Prop = "GridColor";
    public static final String GridWidth_Prop = "GridWidth";
    public static final String GridDash_Prop = "GridDash";
    public static final String GridSpacing_Prop = "GridSpacing";
    public static final String GridBase_Prop = "GridBase";
    public static final String TickLength_Prop = "TickLength";
    public static final String TickPos_Prop = "TickPos";
    public static final String MinorTickCount_Prop = "MinorTickCount";
    public static final String ShowTickLabels_Prop = "ShowTickLabels";
    public static final String TickLabelAutoRotate_Prop = "TickLabelAutoRotate";
    public static final String TickLabelRotation_Prop = "TickLabelRotation";

    // Constants for default values
    public static final String DEFAULT_TITLE = null;
    public static final double DEFAULT_TITLE_ROTATION = 0;
    public static final boolean  DEFAULT_ZERO_REQUIRED = false;
    public static final boolean  DEFAULT_SHOW_ZERO_LINE = false;
    public static final boolean  DEFAULT_LOG = false;
    public static final boolean  DEFAULT_SHOW_LOG_MINOR_LABELS = false;
    public static MinMax  DEFAULT_WRAP_MINMAX = new MinMax(0, 360);
    public static final boolean  DEFAULT_SHOW_GRID = true;
    public static final Color  DEFAULT_GRID_COLOR = Color.get("#E6");
    public static final int  DEFAULT_GRID_WIDTH = 1;
    public static final double[]  DEFAULT_GRID_DASH = Stroke.DASH_SOLID;
    public static final double  DEFAULT_TICK_LENGTH = 7;
    public static final TickPos  DEFAULT_TICK_POS = TickPos.Inside;
    public static final int  DEFAULT_MINOR_TICK_COUNT = 0;
    public static final boolean  DEFAULT_SHOW_TICK_LABELS = true;
    public static final boolean  DEFAULT_TICK_LABEL_AUTO_ROTATE = true;

    // Constants for default overrides
    public static final Color  DEFAULT_AXIS_LINE_COLOR = Color.DARKGRAY;
    public static final int  DEFAULT_AXIS_LINE_WIDTH = 1;
    public static final Pos  DEFAULT_AXIS_ALIGN = Pos.CENTER;
    protected static Color  DEFAULT_AXIS_TEXT_FILL = Color.DARKGRAY;
    public static final NumberFormat  DEFAULT_AXIS_TEXT_FORMAT = new NumberFormat(null, ExpStyle.Financial);

    // Constants for layout
    protected static final int DEFAULT_AXIS_PAD = 5;
    protected static final double DEFAULT_AXIS_SPACING = 8;

    // Constant for GridBase special values
    public static final double GRID_BASE_DATA_MIN = -Float.MAX_VALUE;
    public static final double GRID_BASE_DATA_MAX = Float.MAX_VALUE;

    // Constants for Tick position
    public enum TickPos { Inside, Outside, Across, Off }

    /**
     * Constructor.
     */
    public Axis()
    {
        super();

        // Set default property values
        _wrapMinMax = DEFAULT_WRAP_MINMAX;
        _showGrid = DEFAULT_SHOW_GRID;
        _gridColor = DEFAULT_GRID_COLOR;
        _gridWidth = DEFAULT_GRID_WIDTH;
        _gridDash = DEFAULT_GRID_DASH;
        _tickLength = DEFAULT_TICK_LENGTH;
        _tickPos = DEFAULT_TICK_POS;
        _showTickLabels = DEFAULT_SHOW_TICK_LABELS;
        _textFormat = DEFAULT_AXIS_TEXT_FORMAT;
        _tickLabelAutoRotate = DEFAULT_TICK_LABEL_AUTO_ROTATE;

        // Override default property values
        _lineColor = DEFAULT_AXIS_LINE_COLOR;
        _lineWidth = DEFAULT_AXIS_LINE_WIDTH;
        _textFill = DEFAULT_AXIS_TEXT_FILL;
        _align = DEFAULT_AXIS_ALIGN;
    }

    /**
     * Returns the axis type.
     */
    public abstract AxisType getType();

    /**
     * Returns the Axis title.
     */
    public String getTitle()  { return _title; }

    /**
     * Sets the Axis title.
     */
    public void setTitle(String aStr)
    {
        if (Objects.equals(aStr, _title)) return;
        firePropChange(Title_Prop, _title, _title =aStr);
    }

    /**
     * Returns the title rotation in degrees.
     */
    public double getTitleRotation()  { return _titleRotation; }

    /**
     * Sets the title rotation in degrees.
     */
    public void setTitleRotation(double aValue)
    {
        if (aValue == _titleRotation) return;
        firePropChange(TitleRotation_Prop, _titleRotation, _titleRotation = aValue);
    }

    /**
     * Returns the Axis Min Bound.
     */
    public AxisBound getMinBound()  { return _minBound; }

    /**
     * Sets the Axis Min Bound.
     */
    public void setMinBound(AxisBound aBound)
    {
        if (aBound == _minBound) return;
        firePropChange(MinBound_Prop, _minBound, _minBound = aBound);
    }

    /**
     * Returns the Axis Max Bound.
     */
    public AxisBound getMaxBound()  { return _maxBound; }

    /**
     * Sets the Axis Max Bound.
     */
    public void setMaxBound(AxisBound aBound)
    {
        if (aBound == _maxBound) return;
        firePropChange(MaxBound_Prop, _maxBound, _maxBound = aBound);
    }

    /**
     * Returns the Axis Min Value (if AxisBound is VALUE).
     */
    public double getMinValue()  { return _minValue; }

    /**
     * Sets the Axis Min Value (if AxisBound is VALUE).
     */
    public void setMinValue(double aValue)
    {
        if (aValue == _minValue) return;
        firePropChange(MinValue_Prop, _minValue, _minValue = aValue);
    }

    /**
     * Returns the Axis Max Value (if AxisBound is VALUE).
     */
    public double getMaxValue()  { return _maxValue; }

    /**
     * Sets the Axis Max Value (if AxisBound is VALUE).
     */
    public void setMaxValue(double aValue)
    {
        if (aValue == _maxValue) return;
        firePropChange(MaxValue_Prop, _maxValue, _maxValue = aValue);
    }

    /**
     * Returns the min/max value.
     */
    public MinMax getMinMax()
    {
        double min = getMinValue();
        double max = getMaxValue();
        return new MinMax(min, max);
    }

    /**
     * Returns whether Zero should always be included in auto range.
     */
    public boolean isZeroRequired()  { return _zeroRequired; }

    /**
     * Sets whether Zero should always be included in auto range.
     */
    public void setZeroRequired(boolean aValue)
    {
        if (aValue == isZeroRequired()) return;
        firePropChange(ZeroRequired_Prop, _zeroRequired, _zeroRequired = aValue);
    }

    /**
     * Returns whether Zero line should be painted with distinct style when in visible range.
     */
    public boolean isShowZeroLine()  { return _showZeroLine; }

    /**
     * Sets whether Zero line should be painted with distinct style when in visible range.
     */
    public void setShowZeroLine(boolean aValue)
    {
        if (aValue == isShowZeroLine()) return;
        firePropChange(ShowZeroLine_Prop, _showZeroLine, _showZeroLine = aValue);
    }

    /**
     * Returns whether axis should be rendered in log10 terms.
     */
    public boolean isLog()  { return _log; }

    /**
     * Sets whether axis should be rendered in log10 terms.
     */
    public void setLog(boolean aValue)
    {
        if (aValue == isLog()) return;
        firePropChange(Log_Prop, _log, _log = aValue);
    }

    /**
     * Returns whether axis should show log minor labels.
     */
    public boolean isShowLogMinorLabels()  { return _showLogMinorLabels; }

    /**
     * Sets whether axis should show log minor labels.
     */
    public void setShowLogMinorLabels(boolean aValue)
    {
        if (aValue == _showLogMinorLabels) return;
        firePropChange(ShowLogMinorLabels_Prop, _showLogMinorLabels, _showLogMinorLabels = aValue);
    }

    /**
     * Returns whether axis repeats/wraps values.
     */
    public boolean isWrapAxis()  { return _wrapAxis; }

    /**
     * Sets whether axis repeats/wraps values.
     */
    public void setWrapAxis(boolean aValue)
    {
        if (aValue == isWrapAxis()) return;
        firePropChange(WrapAxis_Prop, _wrapAxis, _wrapAxis = aValue);
    }

    /**
     * Returns the min/max range to repeat/wrap data values.
     */
    public MinMax getWrapMinMax()  { return _wrapMinMax; }

    /**
     * Sets the min/max range to repeat/wrap data values.
     */
    public void setWrapMinMax(MinMax aMinMax)
    {
        if (Objects.equals(aMinMax, getWrapMinMax())) return;
        firePropChange(WrapMinMax_Prop, _wrapMinMax, _wrapMinMax = aMinMax);
    }

    /**
     * Returns the side this axis is shown on.
     */
    public Side getSide()
    {
        return _side;
    }

    /**
     * Sets the side this axis is shown on.
     */
    public void setSide(Side aSide)
    {
        // If already set, just return
        if (aSide == getSide()) return;

        // If trying to set to invalid side, complain
        if (aSide == null || aSide.isLeftOrRight() != getSideDefault().isLeftOrRight())
            throw new IllegalArgumentException("Axis.setSide: Can't set Axis side to " + aSide);

        // Set and firePropChange
        firePropChange(Side_Prop, _side, _side = aSide);
    }

    /**
     * Returns the default side for this axis.
     */
    public Side getSideDefault()
    {
        switch (getType()) {
            case X: return Side.TOP;
            case Y: return Side.LEFT;
            case Y2: return Side.RIGHT;
            case Y3: return Side.LEFT;
            case Y4: return Side.RIGHT;
            default: throw new RuntimeException("Axis.getSide: Unknown AxisType: " + getType());
        }
    }

    /**
     * Returns whether to show grid lines.
     */
    public boolean isShowGrid()  { return _showGrid; }

    /**
     * Sets whether to show grid lines.
     */
    public void setShowGrid(boolean aValue)
    {
        if (aValue == _showGrid) return;
        firePropChange(ShowGrid_Prop, _showGrid, _showGrid = aValue);
    }

    /**
     * Returns the grid line color.
     */
    public Color getGridColor()  { return _gridColor; }

    /**
     * Returns the grid line color.
     */
    public void setGridColor(Color aColor)
    {
        if (Objects.equals(aColor, _gridColor)) return;
        firePropChange(GridColor_Prop, _gridColor, _gridColor = aColor);
    }

    /**
     * Returns the grid line width.
     */
    public int getGridWidth()  { return _gridWidth; }

    /**
     * Returns the grid line width.
     */
    public void setGridWidth(int aValue)
    {
        if (aValue == _gridWidth) return;
        firePropChange(GridWidth_Prop, _gridWidth, _gridWidth = aValue);
    }

    /**
     * Returns the grid line dash array.
     */
    public double[] getGridDash()  { return _gridDash; }

    /**
     * Returns the grid line dash array.
     */
    public void setGridDash(double[] aDashArray)
    {
        if (ArrayUtils.equals(aDashArray, _gridDash)) return;
        firePropChange(GridDash_Prop, _gridDash, _gridDash = aDashArray);
    }

    /**
     * Returns the grid stroke.
     */
    public Stroke getGridStroke()
    {
        if (_gridDash == null)
            return Stroke.getStroke(_gridWidth);
        return new Stroke(_gridWidth, _gridDash, 0);
    }

    /**
     * Returns the base location of first grid line (usually zero).
     */
    public double getGridBase()  { return _gridBase; }

    /**
     * Returns the base location of first grid line (usually zero).
     */
    public void setGridBase(double aValue)
    {
        if (MathUtils.equals(aValue, _gridBase)) return;
        firePropChange(GridBase_Prop, _gridBase, _gridBase = aValue);
    }

    /**
     * Returns the amount of space separating gridlines (in axis data coords).
     */
    public double getGridSpacing()  { return _gridSpacing; }

    /**
     * Returns the amount of space separating gridlines (in axis data coords).
     */
    public void setGridSpacing(double aValue)
    {
        if (MathUtils.equals(aValue, _gridSpacing)) return;
        firePropChange(GridSpacing_Prop, _gridSpacing, _gridSpacing = aValue);
    }

    /**
     * Returns the length of hash mark drawn perpendicular to the axis line for each interval.
     */
    public double getTickLength()  { return _tickLength; }

    /**
     * Sets the length of hash mark drawn perpendicular to the axis line for each interval.
     */
    public void setTickLength(double aValue)
    {
        if (aValue == getTickLength()) return;
        firePropChange(TickLength_Prop, _tickLength, _tickLength = aValue);
    }

    /**
     * Returns the position of the tick mark (can be inside axis, ouside axis, across axis line, or off).
     */
    public TickPos getTickPos()  { return _tickPos; }

    /**
     * Sets the position of the tick mark (can be inside axis, ouside axis, across axis line, or off).
     */
    public void setTickPos(TickPos aValue)
    {
        if (aValue == getTickPos()) return;
        firePropChange(TickPos_Prop, _tickPos, _tickPos = aValue);
    }

    /**
     * Returns the number of minor tick marks between major ticks.
     */
    public int getMinorTickCount()  { return _minorTickCount; }

    /**
     * Sets the number of minor tick marks between major ticks.
     */
    public void setMinorTickCount(int aValue)
    {
        if (aValue == _minorTickCount) return;
        firePropChange(MinorTickCount_Prop, _minorTickCount, _minorTickCount = aValue);
    }

    /**
     * Returns whether to show Tick labels.
     */
    public boolean isShowTickLabels()  { return _showTickLabels; }

    /**
     * Sets whether to show tick labels.
     */
    public void setShowTickLabels(boolean aValue)
    {
        if (aValue == isShowTickLabels()) return;
        firePropChange(ShowTickLabels_Prop, _showTickLabels, _showTickLabels = aValue);
    }

    /**
     * Returns whether tick labels auto rotate.
     */
    public boolean isTickLabelAutoRotate()
    {
        return _tickLabelAutoRotate;
    }

    /**
     * Sets whether tick labels auto rotate.
     */
    public void setTickLabelAutoRotate(boolean aValue)
    {
        if (aValue == _tickLabelAutoRotate) return;
        firePropChange(TickLabelAutoRotate_Prop, _tickLabelAutoRotate, _tickLabelAutoRotate = aValue);
    }

    /**
     * Returns the auto rotation angle.
     */
    public double getTickLabelAutoRotation()
    {
        return 0;
    }

    /**
     * Returns the angle of the tick labels in degrees.
     */
    public double getTickLabelRotation()
    {
        if (isTickLabelAutoRotate())
            return getTickLabelAutoRotation();

        return _tickLabelRotation;
    }

    /**
     * Sets the angle of the tick labels in degrees.
     */
    public void setTickLabelRotation(double anAngle)
    {
        if (anAngle == _tickLabelRotation) return;
        firePropChange(TickLabelRotation_Prop, _tickLabelRotation, _tickLabelRotation = anAngle);
    }

    /**
     * Override to register props.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // Override super defaults: LineWidth, LineColor, TextFill, TextFormat, Align, Spacing
        aPropSet.getPropForName(LineWidth_Prop).setDefaultValue(DEFAULT_AXIS_LINE_WIDTH);
        aPropSet.getPropForName(LineColor_Prop).setDefaultValue(DEFAULT_AXIS_LINE_COLOR);
        aPropSet.getPropForName(TextFill_Prop).setDefaultValue(DEFAULT_AXIS_TEXT_FILL);
        aPropSet.getPropForName(TextFormat_Prop).setDefaultValue(DEFAULT_AXIS_TEXT_FORMAT);
        aPropSet.getPropForName(Align_Prop).setDefaultValue(DEFAULT_AXIS_ALIGN);
        aPropSet.getPropForName(Spacing_Prop).setDefaultValue(DEFAULT_AXIS_SPACING);

        // Title, TitleRotation
        aPropSet.addPropNamed(Title_Prop, String.class, DEFAULT_TITLE);
        aPropSet.addPropNamed(TitleRotation_Prop, double.class, DEFAULT_TITLE_ROTATION);

        // MinBound, MaxBound, MinValue, MaxValue
        aPropSet.addPropNamed(MinBound_Prop, AxisBound.class, AxisBound.AUTO);
        aPropSet.addPropNamed(MaxBound_Prop, AxisBound.class, AxisBound.AUTO);
        aPropSet.addPropNamed(MinValue_Prop, double.class, 0d);
        aPropSet.addPropNamed(MaxValue_Prop, double.class, 5d);

        // WrapAxis, WrapMinMax
        aPropSet.addPropNamed(WrapAxis_Prop, boolean.class, false);
        aPropSet.addPropNamed(WrapMinMax_Prop, MinMax.class, DEFAULT_WRAP_MINMAX);

        // ZeroRequired, ShowZeroLine, Log, ShowLogMinorLabels
        aPropSet.addPropNamed(ZeroRequired_Prop, boolean.class, DEFAULT_ZERO_REQUIRED);
        aPropSet.addPropNamed(ShowZeroLine_Prop, boolean.class, DEFAULT_SHOW_ZERO_LINE);
        aPropSet.addPropNamed(Log_Prop, boolean.class, DEFAULT_LOG);
        aPropSet.addPropNamed(ShowLogMinorLabels_Prop, boolean.class, DEFAULT_SHOW_LOG_MINOR_LABELS);

        // GridSpacing, GridBase
        aPropSet.addPropNamed(GridSpacing_Prop, double.class, 0d);
        aPropSet.addPropNamed(GridBase_Prop, double.class, 0d);

        // TickLength, TickPos, MinorTickCount
        aPropSet.addPropNamed(TickLength_Prop, double.class, DEFAULT_TICK_LENGTH);
        aPropSet.addPropNamed(TickPos_Prop, TickPos.class, DEFAULT_TICK_POS);
        aPropSet.addPropNamed(MinorTickCount_Prop, int.class, DEFAULT_MINOR_TICK_COUNT);

        // ShowTickLabels, TickLabelAutoRotate, TickLabelRotation
        aPropSet.addPropNamed(ShowTickLabels_Prop, boolean.class, DEFAULT_SHOW_TICK_LABELS);
        aPropSet.addPropNamed(TickLabelAutoRotate_Prop, boolean.class, DEFAULT_TICK_LABEL_AUTO_ROTATE);
        aPropSet.addPropNamed(TickLabelRotation_Prop, double.class, DEFAULT_TICK_LABEL_AUTO_ROTATE);
    }

    /**
     * Returns the prop value for given key.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        // Handle properties
        switch (aPropName) {

            // Title, TitleRotation
            case Title_Prop: return getTitle();
            case TitleRotation_Prop: return getTitleRotation();

            // MinBound, MaxBound, MinValue, MaxValue
            case MinBound_Prop: return getMinBound();
            case MaxBound_Prop: return getMaxBound();
            case MinValue_Prop: return getMinValue();
            case MaxValue_Prop: return getMaxValue();

            // WrapAxis, WrapMinMax
            case WrapAxis_Prop: return isWrapAxis();
            case WrapMinMax_Prop: return getWrapMinMax();

            // ZeroRequired, ShowZeroLine, Log, ShowLogMinorLabels
            case ZeroRequired_Prop: return isZeroRequired();
            case ShowZeroLine_Prop: return isShowZeroLine();
            case Log_Prop: return isLog();
            case ShowLogMinorLabels_Prop: return isShowLogMinorLabels();

            // GridSpacing, GridBase
            case GridSpacing_Prop: return getGridSpacing();
            case GridBase_Prop: return getGridBase();

            // TickLength, TickPos, MinorTickCount
            case TickLength_Prop: return getTickLength();
            case TickPos_Prop: return getTickPos();
            case MinorTickCount_Prop: return getMinorTickCount();

            // ShowTickLabels, TickLabelAutoRotate, TickLabelRotation
            case ShowTickLabels_Prop: return isShowTickLabels();
            case TickLabelAutoRotate_Prop: return isTickLabelAutoRotate();
            case TickLabelRotation_Prop: return getTickLabelRotation();

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

            // Title, TitleRotation
            case Title_Prop: setTitle(SnapUtils.stringValue(aValue)); break;
            case TitleRotation_Prop: setTitleRotation(SnapUtils.doubleValue(aValue)); break;

            // MinBound, MaxBound, MinValue, MaxValue
            case MinBound_Prop: setMinBound(AxisBound.get(SnapUtils.stringValue(aValue))); break;
            case MaxBound_Prop: setMaxBound(AxisBound.get(SnapUtils.stringValue(aValue))); break;
            case MinValue_Prop: setMinValue(SnapUtils.doubleValue(aValue)); break;
            case MaxValue_Prop: setMaxValue(SnapUtils.doubleValue(aValue)); break;

            // WrapAxis, WrapMinMax
            case WrapAxis_Prop: setWrapAxis(SnapUtils.boolValue(aValue)); break;
            case WrapMinMax_Prop: {
                MinMax minMax = MinMax.getMinMax(aValue);
                setWrapMinMax(minMax);
                break;
            }

            // ZeroRequired, ShowZeroLine, Log, ShowLogMinorLabels
            case ZeroRequired_Prop: setZeroRequired(SnapUtils.boolValue(aValue));
            case ShowZeroLine_Prop: setShowZeroLine(SnapUtils.boolValue(aValue));
            case Log_Prop: setLog(SnapUtils.boolValue(aValue)); break;
            case ShowLogMinorLabels_Prop: setShowLogMinorLabels(SnapUtils.boolValue(aValue)); break;

            // GridSpacing, GridBase
            case GridSpacing_Prop: setGridSpacing(SnapUtils.doubleValue(aValue)); break;
            case GridBase_Prop: setGridBase(SnapUtils.doubleValue(aValue)); break;

            // TickLength, TickPos, MinorTickCount
            case TickLength_Prop: setTickLength(SnapUtils.intValue(aValue)); break;
            case TickPos_Prop: setTickPos((TickPos) aValue); break;
            case MinorTickCount_Prop: setMinorTickCount(SnapUtils.intValue(aValue)); break;

            // ShowTickLabels, TickLabelAutoRotate, TickLabelRotation
            case ShowTickLabels_Prop: setShowTickLabels(SnapUtils.boolValue(aValue)); break;
            case TickLabelAutoRotate_Prop: setTickLabelAutoRotate(SnapUtils.boolValue(aValue)); break;
            case TickLabelRotation_Prop: setTickLabelRotation(SnapUtils.doubleValue(aValue)); break;

            // Handle super class properties (or unknown)
            default: super.setPropValue(aPropName, aValue);
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

        // Archive Title, TitleRotation
        if (getTitle() != null && getTitle().length() > 0)
            e.add(Title_Prop, getTitle());
        if (getTitleRotation() != 0)
            e.add(TitleRotation_Prop, getTitleRotation());

        // Archive MinBound, MaxBounds, MinValue, MaxValue
        if (!isPropDefault(MinBound_Prop))
            e.add(MinBound_Prop, getMinBound());
        if (!isPropDefault(MaxBound_Prop))
            e.add(MaxBound_Prop, getMaxBound());
        if (getMinBound() == AxisBound.VALUE)
            e.add(MinValue_Prop, getMinValue());
        if (getMaxBound() == AxisBound.VALUE)
            e.add(MaxValue_Prop, getMaxValue());

        // Archive ZeroRequired, ShowZeroLine, Log, ShowLogMinorLabels
        if (!isPropDefault(ZeroRequired_Prop))
            e.add(ZeroRequired_Prop, true);
        if (!isPropDefault(ShowZeroLine_Prop))
            e.add(ShowZeroLine_Prop, isShowZeroLine());
        if (!isPropDefault(Log_Prop))
            e.add(Log_Prop, isLog());
        if (!isPropDefault(ShowLogMinorLabels_Prop))
            e.add(ShowLogMinorLabels_Prop, isShowLogMinorLabels());

        // Archive WrapAxis, WrapMinMax
        if (isWrapAxis()) {
            e.add(WrapAxis_Prop, true);
            e.add(WrapMinMax_Prop, getWrapMinMax().getStringRep());
        }

        // Archive ShowGrid, GridColor
        if (isShowGrid() != DEFAULT_SHOW_GRID)
            e.add(ShowGrid_Prop, false);
        if (!getGridColor().equals(DEFAULT_GRID_COLOR))
            e.add(GridColor_Prop, getGridColor().toHexString());

        // Archive GridWidth, GridDash
        if (getGridWidth() != DEFAULT_GRID_WIDTH)
            e.add(GridWidth_Prop, getGridWidth());
        double[] gridDash = getGridDash();
        if (!ArrayUtils.equals(gridDash, DEFAULT_GRID_DASH)) {
            String dashStr = Stroke.getDashArrayNameOrString(gridDash);
            e.add(GridDash_Prop, dashStr);
        }

        // Archive GridSpacing, GridBase
        if (!isPropDefault(GridSpacing_Prop))
            e.add(GridSpacing_Prop, getGridSpacing());
        if (!isPropDefault(GridBase_Prop))
            e.add(GridBase_Prop, getGridBase());

        // Archive TickLength, TickPos, MinorTickCount
        if (!isPropDefault(TickLength_Prop))
            e.add(TickLength_Prop, getTickLength());
        if (!isPropDefault(TickPos_Prop))
            e.add(TickPos_Prop, getTickPos());
        if (!isPropDefault(MinorTickCount_Prop))
            e.add(MinorTickCount_Prop, getMinorTickCount());

        // Archive ShowTickLabels, TickLabelAutoRotate, TickLabelRotation
        if (!isPropDefault(ShowTickLabels_Prop))
            e.add(ShowTickLabels_Prop, isShowTickLabels());
        if (!isPropDefault(TickLabelAutoRotate_Prop))
            e.add(TickLabelAutoRotate_Prop, isTickLabelAutoRotate());
        if (!isPropDefault(TickLabelRotation_Prop))
            e.add(TickLabelRotation_Prop, getTickLabelRotation());

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

        // Unarchive Title, TitleRotate
        if (anElement.hasAttribute(Title_Prop))
            setTitle(anElement.getAttributeValue(Title_Prop));
        if (anElement.hasAttribute(TitleRotation_Prop))
            setTitleRotation(anElement.getAttributeDoubleValue(TitleRotation_Prop));

        // Unarchive MinBound, MaxBounds, MinValue, MaxValue
        if (anElement.hasAttribute(MinBound_Prop))
            setMinBound(anElement.getAttributeEnumValue(MinBound_Prop, AxisBound.class, null));
        if (anElement.hasAttribute(MaxBound_Prop))
            setMaxBound(anElement.getAttributeEnumValue(MaxBound_Prop, AxisBound.class, null));
        if (anElement.hasAttribute(MinValue_Prop))
            setMinValue(anElement.getAttributeDoubleValue(MinValue_Prop));
        if (anElement.hasAttribute(MaxValue_Prop))
            setMaxValue(anElement.getAttributeDoubleValue(MaxValue_Prop));

        // Unachive ZeroRequired, ShowZeroLine, Log, ShowLogMinorLabels
        if (anElement.hasAttribute(ZeroRequired_Prop))
            setZeroRequired(anElement.getAttributeBoolValue(ZeroRequired_Prop));
        if (anElement.hasAttribute(ShowZeroLine_Prop))
            setShowZeroLine(anElement.getAttributeBoolValue(ShowZeroLine_Prop));
        if (anElement.hasAttribute(Log_Prop))
            setLog(anElement.getAttributeBoolValue(Log_Prop));
        if (anElement.hasAttribute(ShowLogMinorLabels_Prop))
            setShowLogMinorLabels(anElement.getAttributeBoolValue(ShowLogMinorLabels_Prop));

        // Unarchive WrapAxis, WrapMinMax
        boolean isWrapAxis = anElement.getAttributeBoolValue(WrapAxis_Prop, false);
        if (isWrapAxis) {
            MinMax minMax = MinMax.getMinMax(anElement.getAttributeValue(WrapMinMax_Prop));
            if (minMax != null)
                setWrapMinMax(minMax);
        }

        // Unarchive ShowGrid, GridColor
        if (anElement.hasAttribute(ShowGrid_Prop))
            setShowGrid(anElement.getAttributeBoolValue(ShowGrid_Prop));
        if (anElement.hasAttribute(GridColor_Prop)) {
            Color color = Color.get('#' + anElement.getAttributeValue(GridColor_Prop));
            setGridColor(color);
        }

        // Unarchive GridWidth, GridDash
        if (anElement.hasAttribute(GridWidth_Prop))
            setGridWidth(anElement.getAttributeIntValue(GridWidth_Prop));
        if (anElement.hasAttribute(GridDash_Prop)) {
            String dashStr = anElement.getAttributeValue(GridDash_Prop);
            double[] dashArray = Stroke.getDashArray(dashStr);
            setGridDash(dashArray);
        }

        // Unarchive GridSpacing, GridBase
        if (anElement.hasAttribute(GridSpacing_Prop))
            setGridSpacing(anElement.getAttributeDoubleValue(GridSpacing_Prop));
        if (anElement.hasAttribute(GridBase_Prop))
            setGridBase(anElement.getAttributeDoubleValue(GridBase_Prop));

        // Unarchive TickLength, TickPos, MinorTickCount
        if (anElement.hasAttribute(TickLength_Prop))
            setTickLength(anElement.getAttributeDoubleValue(TickLength_Prop));
        if (anElement.hasAttribute(TickPos_Prop))
            setTickPos(anElement.getAttributeEnumValue(TickPos_Prop, TickPos.class, DEFAULT_TICK_POS));
        if (anElement.hasAttribute(MinorTickCount_Prop))
            setMinorTickCount(anElement.getAttributeIntValue(MinorTickCount_Prop));

        // Unarchive ShowTickLabels, TickLabelAutoRotate, TickLabelRotation
        if (anElement.hasAttribute(ShowTickLabels_Prop))
            setShowTickLabels(anElement.getAttributeBoolValue(ShowTickLabels_Prop));
        if (anElement.hasAttribute(TickLabelAutoRotate_Prop))
            setTickLabelAutoRotate(anElement.getAttributeBoolValue(TickLabelAutoRotate_Prop));
        if (anElement.hasAttribute(TickLabelRotation_Prop))
            setTickLabelRotation(anElement.getAttributeDoubleValue(TickLabelRotation_Prop));

        // Return this part
        return this;
    }
}
