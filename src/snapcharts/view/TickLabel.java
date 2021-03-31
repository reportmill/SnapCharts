package snapcharts.view;
import snap.gfx.Painter;
import snap.view.StringView;

/**
 * This is a View subclass to show a tick label for an AxisView and coord.
 */
public class TickLabel extends StringView {

    // The AxisView that holds this TickLabel
    private AxisView  _axisView;

    // The coordinate of the label
    private double  _coord;

    /**
     * Constructor.
     */
    public TickLabel(AxisView anAxisView, double aCoord)
    {
        _axisView = anAxisView;
        _coord = aCoord;
    }

    /**
     * Returns the tick label coord.
     */
    public double getCoord()  { return _coord; }

    /**
     * Paints the TickLabel.
     */
    public void paintTickLabel(Painter aPntr)
    {
        aPntr.translate(getX(), getY());
        paintAll(aPntr);
        aPntr.translate(-getX(), - getY());
    }
}
