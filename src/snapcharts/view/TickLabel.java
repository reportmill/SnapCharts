package snapcharts.view;
import snap.gfx.Font;
import snap.gfx.Painter;
import snap.text.TextRun;
import snap.view.StringView;

/**
 * This is a View subclass to show a tick label for an AxisView and coord.
 */
public class TickLabel extends StringView {

    // The coordinate of the label
    private double  _coord;

    // A TextRun to handle exponents if needed
    private TextRun  _expText;

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
        // If exponent is detected, Create ExpText to size/paint
        int index = aValue.indexOf("x10^");
        if (index > 0) {

            // Get base string and set
            String str = aValue.substring(0, index + 3);
            super.setText(str);

            // Get exponent string and create exponent TextRun (ExpText)
            String expStr = aValue.substring(index + 4);
            _expText = new TextRun(expStr);
            _expText.setFontSizing(false);

            // Add bogus padding
            setPadding(4, 0, 4, 0);
        }

        // Otherwise, do normal version
        else super.setText(aValue);
    }

    /**
     * Override to add ExpText if label has exponent.
     */
    @Override
    protected double getPrefWidthImpl(double aH)
    {
        // Do normal version and just return if no ExpText
        double prefW = super.getPrefWidthImpl(aH);
        if (_expText == null)
            return prefW;

        // Update ExpText and add ExpText.Width to PrefW
        Font font = getFont();
        Font expFont = font.deriveFont(Math.round(font.getSize()) * .7);
        _expText.setFont(expFont);
        _expText.setTextFill(getTextFill());
        prefW += _expText.getTextWidth() + 1;
        return prefW;
    }

    /**
     * Override to paint ExpText if set.
     */
    @Override
    protected void paintString(Painter aPntr)
    {
        // Do normal version
        super.paintString(aPntr);

        // If ExpText, update XY and paint it
        if (_expText != null) {
            double expX = getTextWidth() + 1;
            double expY = getPadding().top + Math.round(getAscent() * .5);
            _expText.paintString(aPntr, expX, expY);
        }
    }
}
