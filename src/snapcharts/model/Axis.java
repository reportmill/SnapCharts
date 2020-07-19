package snapcharts.model;
import snap.geom.*;
import snap.gfx.Color;
import snap.util.XMLArchiver;
import snap.util.XMLElement;

/**
 * A class to represent a Chart Axis.
 */
public class Axis extends ChartPart {

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
    private double  _labelsMargin = 15;

    // THe grid line color
    private Color  _gridLineColor = GRID_LINES_COLOR;

    // The grid line
    private double  _gridLineDashArray[];

    // Constants for properties
    public static final String Type_Prop = "Type";
    public static final String Title_Prop = "Title";
    public static final String TitleAlign_Prop = "TitleAlign";
    public static final String TitleRotate_Prop = "TitleRotate";

    // Constants for default values
    static Color   AXIS_LABELS_COLOR = Color.GRAY;
    static Color   GRID_LINES_COLOR = Color.get("#E6");
    static Pos DEFAULT_TITLE_ALIGN = Pos.CENTER;

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
//    public double getLabelsOffset()
//    {
//        return getMaxLabelWidth() + getLabelsMargin();
//    }

    /**
     * Returns the distance between axis labels right edge and the axis.
     */
    public double getLabelsMargin()  { return _labelsMargin; }

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

        // Return this part
        return this;
    }
}
