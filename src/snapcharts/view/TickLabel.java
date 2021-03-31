package snapcharts.view;
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
}
