package snapcharts.model;
import snap.geom.*;
import snap.gfx.Color;
import snap.util.SnapUtils;
import snap.util.XMLArchiver;
import snap.util.XMLElement;

/**
 * A class to represent a Chart Axis.
 */
public abstract class Axis extends ChartPart {

    // The Title
    private String _title;

    // The title alignment
    private Pos _titleAlign = DEFAULT_TITLE_ALIGN;

    // The title rotation
    private double _titleRot;

    // Title offset - distance from title left edge to axis
    private Double  _titleOffset;

    // Title margin - distance of title right edge to labels
    private double  _titleMargin = 10;

    // Title x/y - additional offset for title
    private double  _titleX, _titleY;

    // Labels margin - distance of labels right edge to axis
    private double  _labelsMargin = 5;

    // The length of the vertical tick lines drawn from the X axis down twards it's labels and title
    private double  _tickLength = 8;

    // The Axis Min BoundType
    private AxisBoundType  _minType = AxisBoundType.AUTO;

    // The Axis Min BoundType
    private AxisBoundType  _maxType = AxisBoundType.AUTO;

    // The Axis Min Value (if MinBoundType is VALUE)
    private double  _minValue = 0;

    // The Axis Max Value (if MaxBoundType is VALUE)
    private double  _maxValue = 0;

    // Whether Zero should always be included
    private boolean  _zeroRequired;

    // The grid line color
    private Color  _gridLineColor = GRID_LINES_COLOR;

    // The grid line
    private double  _gridLineDashArray[];

    // Constants for properties
    public static final String Type_Prop = "Type";
    public static final String Title_Prop = "Title";
    public static final String TitleAlign_Prop = "TitleAlign";
    public static final String TitleRotate_Prop = "TitleRotate";
    public static final String MinBoundType_Prop = "MinBoundType";
    public static final String MaxBoundType_Prop = "MaxBoundType";
    public static final String MinValue_Prop = "MinValue";
    public static final String MaxValue_Prop = "MaxValue";
    public static final String ZeroRequired_Prop = "ZeroRequired";

    // Constants for default values
    static Color   AXIS_LABELS_COLOR = Color.GRAY;
    static Color   GRID_LINES_COLOR = Color.get("#E6");
    static Pos DEFAULT_TITLE_ALIGN = Pos.CENTER;

    // A type for AxisBounds
    public enum AxisBoundType {

        /** Bound type for get pleasing value from data value. */
        AUTO,

        // Bound type for use exact data value. */
        DATA,

        // Bound type for user provided value
        VALUE;

        public static AxisBoundType get(String aValue)
        {
            try { return AxisBoundType.valueOf(aValue.toUpperCase()); }
            catch (Exception e) { return AUTO; }
        }
    }

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
     * Returns the distance from title left edge to axis.
     */
    public double getTitleOffset()
    {
        return _titleOffset!=null ? _titleOffset : 0;
    }

    /**
     * Sets the distance from title left edge to axis.
     */
    public void setTitleOffset(double aValue)
    {
        _titleOffset = aValue>=0? aValue : null;
    }

    /**
     * Returns the distance between the title and axis labels.
     */
    public double getTitleMargin()  { return _titleMargin; }

    /**
     * Sets the distance between the title and axis labels.
     */
    public void setTitleMargin(double aValue)  { _titleMargin = aValue; }

    /**
     * Returns the additional offset of title.
     */
    public double getTitleX()  { return _titleX; }

    /**
     * Returns the additional offset of title.
     */
    public void setTitleX(double aValue)  { _titleX = aValue; }

    /**
     * Returns the additional offset of title.
     */
    public double getTitleY()  { return _titleY; }

    /**
     * Returns the additional offset of title.
     */
    public void setTitleY(double aValue)  { _titleY = aValue; }

    /**
     * Returns the distance between axis labels left edge and axis.
     */
    //public double getLabelsOffset()  { return getMaxLabelWidth() + getLabelsMargin(); }

    /**
     * Returns the distance between axis labels right edge and the axis.
     */
    public double getLabelsMargin()  { return _labelsMargin; }

    /**
     * Returns the length of the vertical tick lines drawn from the X axis down twards it's labels and title.
     */
    public double getTickLength()  { return _tickLength; }

    /**
     * Sets the length of the vertical tick lines drawn from the X axis down twards it's labels and title.
     */
    public void setTickLength(double aValue)  { _tickLength = aValue; }

    /**
     * Returns the Axis Min BoundType.
     */
    public AxisBoundType getMinBoundType()  { return _minType; }

    /**
     * Sets the Axis Min BoundType.
     */
    public void setMinBoundType(AxisBoundType aBoundType)
    {
        if (aBoundType==_minType) return;
        firePropChange(MinBoundType_Prop, _minType, _minType = aBoundType);
    }

    /**
     * Returns the Axis Max BoundType.
     */
    public AxisBoundType getMaxBoundType()  { return _maxType; }

    /**
     * Sets the Axis Max BoundType.
     */
    public void setMaxBoundType(AxisBoundType aBoundType)
    {
        if (aBoundType==_maxType) return;
        firePropChange(MaxBoundType_Prop, _maxType, _maxType = aBoundType);
    }

    /**
     * Returns the Axis Min Value (if BoundType is VALUE).
     */
    public double getMinValue()  { return _minValue; }

    /**
     * Sets the Axis Min Value (if BoundType is VALUE).
     */
    public void setMinValue(double aValue)
    {
        if (aValue==_minValue) return;
        firePropChange(MinValue_Prop, _minValue, _minValue = aValue);
    }

    /**
     * Returns the Axis Max Value (if BoundType is VALUE).
     */
    public double getMaxValue()  { return _maxValue; }

    /**
     * Sets the Axis Max Value (if BoundType is VALUE).
     */
    public void setMaxValue(double aValue)
    {
        if (aValue==_maxValue) return;
        firePropChange(MaxValue_Prop, _maxValue, _maxValue = aValue);
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
     * Returns the prop value keys.
     */
    @Override
    protected String[] getPropKeysLocal()
    {
        return new String[] {
                MinBoundType_Prop, MaxBoundType_Prop,
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
            case MinBoundType_Prop: return getMinBoundType();
            case MaxBoundType_Prop: return getMaxBoundType();
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
            case MinBoundType_Prop: setMinBoundType(AxisBoundType.get(SnapUtils.stringValue(aValue))); break;
            case MaxBoundType_Prop: setMaxBoundType(AxisBoundType.get(SnapUtils.stringValue(aValue))); break;
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
            case MinBoundType_Prop: return AxisBoundType.AUTO;
            case MaxBoundType_Prop: return AxisBoundType.AUTO;
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

        // Archive ZeroRequired
        if (isZeroRequired())
            e.add(ZeroRequired_Prop, true);

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
        setZeroRequired(anElement.getAttributeBoolValue(ZeroRequired_Prop, false));

        // Return this part
        return this;
    }
}
