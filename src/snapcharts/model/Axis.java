package snapcharts.model;

import snap.geom.HPos;
import snap.geom.Pos;
import snap.geom.VPos;
import snap.gfx.Color;
import snap.util.DeepChangeListener;
import snap.util.PropChange;
import snap.util.PropChangeListener;
import snap.util.PropChangeSupport;

/**
 * A class to represent a Chart Axis.
 */
public class Axis {

    // The Chart that owns this dataset
    protected Chart  _chart;

    // The Title
    private String _titleView;

    // The title alignment
    private Pos _titleAlign = Pos.CENTER;

    // The title rotation
    private double _titleRot;

    // Title offset - distance from title left edge to axis
    Double         _titleOffset;

    // Title margin - distance of title right edge to labels
    double         _titleMargin = 10;

    // Title x/y - additional offset for title
    double         _titleX, _titleY;

    // Labels margin - distance of labels right edge to axis
    double         _labelsMargin = 15;

    // THe grid line color
    Color _gridLineColor = GRID_LINES_COLOR;

    // The grid line
    double         _gridLineDashArray[];

    // PropertyChangeSupport
    private PropChangeSupport _pcs = PropChangeSupport.EMPTY;

    // Constants for properties
    public static final String Type_Prop = "Type";
    public static final String Title_Prop = "Title";
    public static final String TitleAlign_Prop = "TitleAlign";
    public static final String TitleRotate_Prop = "TitleRotate";

    // Constants for default values
    static Color   AXIS_LABELS_COLOR = Color.GRAY;
    static Color   GRID_LINES_COLOR = Color.get("#E6");

    /**
     * Returns the chart.
     */
    public Chart getChart()  { return _chart; }

    /**
     * Returns the dataset.
     */
    public DataSet getDataSet()  { return _chart.getDataSet(); }

    /**
     * Returns the YAxis title.
     */
    public String getTitle()  { return _titleView; }

    /**
     * Sets the YAxis title.
     */
    public void setTitle(String aStr)
    {
        if (aStr==_titleView) return;
        firePropChange(Title_Prop, _titleView, _titleView=aStr);
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
     * Add listener.
     */
    public void addPropChangeListener(PropChangeListener aPCL)
    {
        if(_pcs== PropChangeSupport.EMPTY) _pcs = new PropChangeSupport(this);
        _pcs.addPropChangeListener(aPCL);
    }

    /**
     * Remove listener.
     */
    public void removePropChangeListener(PropChangeListener aPCL)  { _pcs.removePropChangeListener(aPCL); }

    /**
     * Fires a property change for given property name, old value, new value and index.
     */
    protected void firePropChange(String aProp, Object oldVal, Object newVal)
    {
        if(!_pcs.hasListener(aProp)) return;
        firePropChange(new PropChange(this, aProp, oldVal, newVal));
    }

    /**
     * Fires a property change for given property name, old value, new value and index.
     */
    protected void firePropChange(String aProp, Object oldVal, Object newVal, int anIndex)
    {
        if(!_pcs.hasListener(aProp)) return;
        firePropChange(new PropChange(this, aProp, oldVal, newVal, anIndex));
    }

    /**
     * Fires a given property change.
     */
    protected void firePropChange(PropChange aPC)  { _pcs.firePropChange(aPC); }

    /**
     * Add DeepChange listener.
     */
    public void addDeepChangeListener(DeepChangeListener aDCL)
    {
        if(_pcs==PropChangeSupport.EMPTY) _pcs = new PropChangeSupport(this);
        _pcs.addDeepChangeListener(aDCL);
    }

    /**
     * Remove DeepChange listener.
     */
    public void removeDeepChangeListener(DeepChangeListener aPCL)  { _pcs.removeDeepChangeListener(aPCL); }
}
