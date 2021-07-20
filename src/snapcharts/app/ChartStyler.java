package snapcharts.app;
import snap.gfx.*;
import snap.styler.Styler;
import snap.util.Undoer;
import snap.view.View;
import snapcharts.model.ChartPart;
import snapcharts.model.DataSet;
import snapcharts.model.StyledChartPart;

/**
 * ChartStyler.
 */
public class ChartStyler extends Styler {

    // The editor
    private ChartPane _editor;

    /**
     * Constructor.
     */
    public ChartStyler(ChartPane anEditor)
    {
        _editor = anEditor;
    }

    /**
     * Returns the currently selected border.
     */
    public Border getBorder()
    {
        StyledChartPart chartPart = getSelPart();
        return chartPart!=null ? chartPart.getBorder() : null;
    }

    /**
     * Sets the currently selected border.
     */
    public void setBorder(Border aBorder)
    {
        getSelPart().setBorder(aBorder);
    }

    /**
     * Returns the fill of currently selected view.
     */
    public Paint getFill()
    {
        StyledChartPart chartPart = getSelPart();
        return chartPart!=null ? chartPart.getFill() : null;
    }

    /**
     * Sets the fill of currently selected views.
     */
    public void setFill(Paint aPaint)
    {
        getSelPart().setFill(aPaint);
    }

    /**
     * Returns the current effect.
     */
    public Effect getEffect()
    {
        StyledChartPart chartPart = getSelPart();
        return chartPart!=null ? chartPart.getEffect() : null;
    }

    /**
     * Sets the current effect.
     */
    public void setEffect(Effect anEffect)
    {
        getSelPart().setEffect(anEffect);
    }

    /**
     * Returns the current opacity.
     */
    public double getOpacity()
    {
        StyledChartPart chartPart = getSelPart();
        return chartPart!=null ? chartPart.getOpacity() : 1;
    }

    /**
     * Sets the currently selected opacity.
     */
    public void setOpacity(double aValue)
    {
        setUndoTitle("Transparency Change");
        getSelPart().setOpacity(aValue);
    }

    /**
     * Returns the font of editor's selected shape.
     */
    public Font getFont()
    {
        StyledChartPart chartPart = getSelPart();
        return chartPart!=null ? chartPart.getFont() : Font.Arial12;
    }

    /**
     * Sets the current font.
     */
    public void setFont(Font aFont)
    {
        getSelPart().setFont(aFont);
    }

    /**
     * Returns the text color current text.
     */
    public Color getTextColor()
    {
        StyledChartPart chartPart = getSelPart();
        Paint textFill = chartPart != null ? chartPart.getTextFill() : null;
        return textFill != null ? textFill.getColor() : null;
    }

    /**
     * Sets the text color current text.
     */
    public void setTextColor(Color aColor)
    {
        StyledChartPart chartPart = getSelPart();
        if (chartPart != null)
            chartPart.setTextFill(aColor);
    }

    /**
     * Returns the client View.
     */
    public View getClientView()  { return _editor.getUI(); }

    /**
     * Returns the Selected ChartPart.
     */
    private StyledChartPart getSelPart()
    {
        ChartPart chartPart = _editor.getSelChartPart();
        if (chartPart instanceof DataSet)
            chartPart = ((DataSet) chartPart).getDataStyle();
        return chartPart instanceof StyledChartPart ? (StyledChartPart) chartPart : null;
    }

    /**
     * Sets undo title.
     */
    public void setUndoTitle(String aTitle)
    {
        Undoer undoer = _editor.getUndoer();
        if (undoer!=null)
            undoer.setUndoTitle(aTitle);
    }
}
