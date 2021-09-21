package snapcharts.view;
import snap.view.StringView;

/**
 * This is a View subclass to show a tick label for an AxisView and coord.
 */
public class TickLabel extends StringView {

    // The coordinate of the label
    private double  _coord;

    // Constants for defaults
    private static final boolean DEFAULT_TICK_FONT_SIZING = false;

    /**
     * Constructor.
     */
    public TickLabel(double aCoord)
    {
        _coord = aCoord;

        // Override defaults
        _fontSizing = DEFAULT_TICK_FONT_SIZING;
    }

    /**
     * Returns the tick label coord.
     */
    public double getCoord()  { return _coord; }
}
