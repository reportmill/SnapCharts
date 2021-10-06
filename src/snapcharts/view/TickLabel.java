package snapcharts.view;
import snap.gfx.Font;
import snap.gfx.Painter;
import snap.view.StringView;

/**
 * This is a View subclass to show a tick label for an AxisView and coord.
 */
public class TickLabel extends StringView {

    // The coordinate of the label
    private double  _coord;

    // A StringView to handle exponents if needed
    private StringView  _expView;

    /**
     * Constructor.
     */
    public TickLabel(double aCoord)
    {
        _coord = aCoord;

        // Size to character outlines
        setFontSizing(false);
    }

    /**
     * Returns the tick label coord.
     */
    public double getCoord()  { return _coord; }

    /**
     * Override to handle exponents.
     */
    @Override
    public void setText(String aValue)
    {
        // If exponent is detected, Create ExpView to size/paint
        int index = aValue.indexOf("x10^");
        if (index > 0) {

            // Get base string and set
            String str = aValue.substring(0, index + 3);
            super.setText(str);

            // Get exponent string and create exponent StringView (ExpView)
            String expStr = aValue.substring(index + 4);
            _expView = new StringView(expStr);
            _expView.setFontSizing(false);

            // Add bogus padding
            setPadding(4, 0, 4, 0);
        }

        // Otherwise, do normal version
        else super.setText(aValue);
    }

    /**
     * Override to add ExpView if label has exponent.
     */
    @Override
    protected double getPrefWidthImpl(double aH)
    {
        // Do normal version and just return if no ExpView
        double prefW = super.getPrefWidthImpl(aH);
        if (_expView == null)
            return prefW;

        // Update ExpView and add ExpView.Width to PrefW
        Font font = getFont();
        Font expFont = font.deriveFont(Math.round(font.getSize()) * .7);
        _expView.setFont(expFont);
        _expView.setTextFill(getTextFill());
        _expView.setSizeToPrefSize();
        prefW += _expView.getWidth() + 1;
        return prefW;
    }

    /**
     * Override to paint ExpView if set.
     */
    @Override
    protected void paintString(Painter aPntr)
    {
        // Do normal version
        super.paintString(aPntr);

        // If ExpView, update XY and paint it
        if (_expView != null) {
            double expX = getTextWidth() + 1;
            double expY = getPadding().top + Math.round(getAscent() * .5) - _expView.getHeight();
            _expView.setXY(expX, expY);
            _expView.paintStringView(aPntr);
        }
    }
}
