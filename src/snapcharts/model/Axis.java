package snapcharts.model;
import snap.geom.*;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.util.SnapUtils;
import snap.util.XMLArchiver;
import snap.util.XMLElement;
import snapcharts.util.MinMax;

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

    // The grid line color
    private Color  _gridLineColor = GRID_LINES_COLOR;

    // The grid line
    private double  _gridLineDashArray[];

    // The Side this axis
    private Side  _side;

    // Constants for properties
    public static final String Type_Prop = "Type";
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

    // Constants for default values
    static Color   AXIS_LABELS_COLOR = Color.GRAY;
    static Color   GRID_LINES_COLOR = Color.get("#E6");
    static Pos DEFAULT_TITLE_ALIGN = Pos.CENTER;

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
     * Returns the grid line color.
     */
    public Color getGridLineColor()  { return _gridLineColor; }

    /**
     * Returns the grid line color.
     */
    public void setGridLineColor(Color aColor)  { _gridLineColor = aColor; }

    /**
     * Returns the grid line dash array.
     */
    public double[] getGridLineDashArray()  { return _gridLineDashArray; }

    /**
     * Returns the grid line dash array.
     */
    public void setGridLineDashArray(double theVals[])  { _gridLineDashArray = theVals; }


    /**
     * Returns the side this axis is shown on.
     */
    public Side getSide()
    {
        return _side!=null ? _side : getSideDefault();
    }

    /**
     * Sets the side this axis is shown on.
     */
    public void setSide(Side aSide)
    {
        if (aSide==getSide()) return;
        if (aSide==null || aSide.isHorizontal() != getSideDefault().isHorizontal())
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

        // Return this part
        return this;
    }
}
