package snapcharts.model;
import snap.geom.*;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.gfx.Stroke;
import snap.util.ArrayUtils;
import snap.util.SnapUtils;
import snap.util.XMLArchiver;
import snap.util.XMLElement;
import snapcharts.util.MinMax;

import java.util.Objects;

/**
 * A class to represent a Chart Axis.
 */
public abstract class Axis extends ChartPart {

    // The Title
    private String  _title;

    // The title alignment
    private Pos  _titleAlign = DEFAULT_TITLE_ALIGN;

    // The title rotation
    private double  _titleRot;

    // The length of the vertical tick lines drawn from the X axis down twards it's labels and title
    private double  _tickLength = 8;

    // The Axis Min Bounding
    private AxisBound _minBound = AxisBound.AUTO;

    // The Axis Min Bounding
    private AxisBound _maxBound = AxisBound.AUTO;

    // The Axis Min Value (if MinBounding is VALUE)
    private double  _minValue = 0;

    // The Axis Max Value (if MaxBounding is VALUE)
    private double  _maxValue = 0;

    // Whether Zero should always be included
    private boolean  _zeroRequired;

    // Whether axis is log10 based
    private boolean  _log;

    // Whether axis repeats/wraps values
    private boolean  _wrapAxis;

    // The wrap min/max value
    private MinMax  _wrapMinMax = DEFAULT_WRAP_MINMAX;

    // The Side this axis
    private Side  _side;

    // Whether to show grid lines
    private boolean  _showGrid = DEFAULT_SHOW_GRID;

    // The grid line color
    private Color  _gridColor = DEFAULT_GRID_COLOR;

    // The grid line width
    private int  _gridWidth = DEFAULT_GRID_WIDTH;

    // The grid line
    private double[]  _gridDash = DEFAULT_GRID_DASH;

    // Constants for properties
    public static final String Title_Prop = "Title";
    public static final String TitleAlign_Prop = "TitleAlign";
    public static final String TitleRotate_Prop = "TitleRotate";
    public static final String MinBound_Prop = "MinBound";
    public static final String MaxBound_Prop = "MaxBound";
    public static final String MinValue_Prop = "MinValue";
    public static final String MaxValue_Prop = "MaxValue";
    public static final String ZeroRequired_Prop = "ZeroRequired";
    public static final String Log_Prop = "Logarithmic";
    public static final String Side_Prop = "Side";
    public static final String WrapAxis_Prop = "WrapAxis";
    public static final String WrapMinMax_Prop = "WrapMinMax";
    public static final String ShowGrid_Prop = "ShowGrid";
    public static final String GridColor_Prop = "GridColor";
    public static final String GridWidth_Prop = "GridWidth";
    public static final String GridDash_Prop = "GridDash";

    // Constants for default values
    protected static Pos DEFAULT_TITLE_ALIGN = Pos.CENTER;
    public static MinMax DEFAULT_WRAP_MINMAX = new MinMax(0, 360);
    public static final boolean DEFAULT_SHOW_GRID = true;
    public static final Color DEFAULT_GRID_COLOR = Color.get("#E6");
    public static final int DEFAULT_GRID_WIDTH = 1;
    public static final double[] DEFAULT_GRID_DASH = Stroke.DASH_SOLID;

    /**
     * Constructor.
     */
    public Axis()
    {
        super();
    }

    /**
     * Returns the axis type.
     */
    public abstract AxisType getType();

    /**
     * Returns the YAxis title.
     */
    public String getTitle()  { return _title; }

    /**
     * Sets the YAxis title.
     */
    public void setTitle(String aStr)
    {
        if (aStr== _title) return;
        firePropChange(Title_Prop, _title, _title =aStr);
    }

    /**
     * Returns the title alignment.
     */
    public Pos getTitleAlign()  { return _titleAlign; }

    /**
     * Sets the title alignment.
     */
    public void setTitleAlign(Pos aPos)
    {
        if (aPos==_titleAlign) return;
        firePropChange(TitleAlign_Prop, _titleAlign, _titleAlign = aPos);
    }

    /**
     * Returns the title alignment.
     */
    public HPos getTitleAlignX()  { return _titleAlign.getHPos(); }

    /**
     * Sets the title alignment.
     */
    public void setTitleAlignX(HPos aPos)  { setTitleAlign(Pos.get(aPos, getTitleAlignY())); }

    /**
     * Returns the title alignment.
     */
    public VPos getTitleAlignY()  { return _titleAlign.getVPos(); }

    /**
     * Sets the title alignment.
     */
    public void setTitleAlignY(VPos aPos)  { setTitleAlign(Pos.get(getTitleAlignX(), aPos)); }

    /**
     * Returns the title rotation in degrees.
     */
    public double getTitleRotate()  { return _titleRot; }

    /**
     * Sets the title rotation in degrees.
     */
    public void setTitleRotate(double aValue)
    {
        if (aValue==_titleRot) return;
        firePropChange(TitleRotate_Prop, _titleRot, _titleRot=aValue);
    }

    /**
     * Returns the length of the vertical tick lines drawn from the X axis down twards it's labels and title.
     */
    public double getTickLength()  { return _tickLength; }

    /**
     * Sets the length of the vertical tick lines drawn from the X axis down twards it's labels and title.
     */
    public void setTickLength(double aValue)  { _tickLength = aValue; }

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
        if (aValue==_minValue) return;
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
        if (aValue==_maxValue) return;
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
     * Returns the Axis Min value based on MinBound (either AxisBound.[VALUE | DATA]) and ZeroRequired.
     */
    public double getMinValueForBoundAndZeroRequired()
    {
        // Get Min based on whether Axis.MinBound is AxisBound.VALUE or AxisBound.DATA
        AxisBound minBound = getMinBound();
        double min;
        if (minBound == AxisBound.VALUE)
            min = getMinValue();
        else {
            DataSetList dsetList = getDataSetList();
            min = dsetList.getMinForAxis(getType());
        }

        // If ZeroRequired and min greater than zero, reset min
        if (isZeroRequired() && min>0)
            min = 0;

        // Return min
        return min;
    }

    /**
     * Returns the Axis Max value based on MaxBound (either AxisBound.[VALUE | DATA]) and ZeroRequired.
     */
    public double getMaxValueForBoundAndZeroRequired()
    {
        // Get Max based on whether Axis.MaxBound is AxisBound.VALUE or AxisBound.DATA
        AxisBound maxBound = getMaxBound();
        double max;
        if (maxBound == AxisBound.VALUE)
            max = getMaxValue();
        else {
            DataSetList dsetList = getDataSetList();
            max = dsetList.getMaxForAxis(getType());
        }

        // If ZeroRequired and max less than zero, reset max
        if (isZeroRequired() && max<0)
            max = 0;

        // Return max
        return max;
    }

    /**
     * Returns whether Zero should always be included.
     */
    public boolean isZeroRequired()  { return _zeroRequired; }

    /**
     * Sets whether Zero should always be included.
     */
    public void setZeroRequired(boolean aValue)
    {
        if (aValue==isZeroRequired()) return;
        firePropChange(ZeroRequired_Prop, _zeroRequired, _zeroRequired=aValue);
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
        if (aValue==isLog()) return;
        firePropChange(Log_Prop, _log, _log = aValue);
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
        return _side != null ? _side : getSideDefault();
    }

    /**
     * Sets the side this axis is shown on.
     */
    public void setSide(Side aSide)
    {
        if (aSide == getSide()) return;
        if (aSide == null || aSide.isHorizontal() != getSideDefault().isHorizontal())
            throw new IllegalArgumentException("Axis.setSide: Can't set Axis side to " + aSide);
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
     * Returns the prop value keys.
     */
    @Override
    protected String[] getPropKeysLocal()
    {
        return new String[] {
                MinBound_Prop, MaxBound_Prop,
                MinValue_Prop, MaxValue_Prop
        };
    }

    /**
     * Returns the prop value for given key.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        // Handle properties
        switch (aPropName) {
            case MinBound_Prop: return getMinBound();
            case MaxBound_Prop: return getMaxBound();
            case MinValue_Prop: return getMinValue();
            case MaxValue_Prop: return getMaxValue();
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
            case MinBound_Prop: setMinBound(AxisBound.get(SnapUtils.stringValue(aValue))); break;
            case MaxBound_Prop: setMaxBound(AxisBound.get(SnapUtils.stringValue(aValue))); break;
            case MinValue_Prop: setMinValue(SnapUtils.doubleValue(aValue)); break;
            case MaxValue_Prop: setMaxValue(SnapUtils.doubleValue(aValue)); break;

            // Handle WrapMinMax
            case WrapMinMax_Prop:
                MinMax minMax = MinMax.getMinMax(aValue);
                setWrapMinMax(minMax);
                break;

            // Handle super class properties (or unknown)
            default: super.setPropValue(aPropName, aValue);
        }
    }

    /**
     * Returns the prop default value for given key.
     */
    @Override
    public Object getPropDefault(String aPropName)
    {
        // Handle properties
        switch (aPropName) {
            case Font_Prop: return Font.Arial12;
            case MinBound_Prop: return AxisBound.AUTO;
            case MaxBound_Prop: return AxisBound.AUTO;
            case MinValue_Prop: return 0;
            case MaxValue_Prop: return 5;
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

        // Archive Title, TitleAlign, TitleRotate
        if (getTitle()!=null && getTitle().length()>0)
            e.add(Title_Prop, getTitle());
        if (getTitleAlign()!= DEFAULT_TITLE_ALIGN)
            e.add(TitleAlign_Prop, getTitleAlign());
        if (getTitleRotate()!=0)
            e.add(TitleRotate_Prop, getTitleRotate());

        // Archive ZeroRequired, Log
        if (isZeroRequired())
            e.add(ZeroRequired_Prop, true);
        if (isLog())
            e.add(Log_Prop, true);

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

        // Unarchive Title, TitleAlign, TitleRotate
        setTitle(anElement.getAttributeValue(Title_Prop));
        setTitleAlign(Pos.get(anElement.getAttributeValue(TitleAlign_Prop, DEFAULT_TITLE_ALIGN.toString())));
        setTitleRotate(anElement.getAttributeDoubleValue(TitleRotate_Prop));

        // Unachive ZeroRequired
        if (anElement.hasAttribute(ZeroRequired_Prop))
            setZeroRequired(anElement.getAttributeBoolValue(ZeroRequired_Prop, false));
        if (anElement.hasAttribute(Log_Prop))
            setLog(anElement.getAttributeBoolValue(Log_Prop));

        // Unarchive WrapAxis, WrapMinMax
        boolean isWrapAxis = anElement.getAttributeBoolValue(WrapAxis_Prop, false);
        if (isWrapAxis) {
            MinMax minMax = MinMax.getMinMax(anElement.getAttributeValue(WrapMinMax_Prop));
            if (minMax != null)
                setWrapMinMax(minMax);
        }

        // Unarchive ShowLine, LineColor
        if (anElement.hasAttribute(ShowGrid_Prop))
            setShowGrid(anElement.getAttributeBoolValue(ShowGrid_Prop));
        if (anElement.hasAttribute(GridColor_Prop)) {
            Color color = Color.get('#' + anElement.getAttributeValue(GridColor_Prop));
            setGridColor(color);
        }

        // Unarchive GridWidth, GridDash
        if (anElement.hasAttribute(GridWidth_Prop))
            setGridWidth(anElement.getAttributeIntValue(ShowGrid_Prop));
        if (anElement.hasAttribute(GridDash_Prop)) {
            String dashStr = anElement.getAttributeValue(GridDash_Prop);
            double[] dashArray = Stroke.getDashArray(dashStr);
            setGridDash(dashArray);
        }

        // Return this part
        return this;
    }
}
