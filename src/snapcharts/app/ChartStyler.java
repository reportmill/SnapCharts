package snapcharts.app;

import rmdraw.editors.Styler;
import snap.gfx.Border;
import snap.gfx.Effect;
import snap.gfx.Font;
import snap.gfx.Paint;
import snap.util.Undoer;
import snap.view.View;
import snapcharts.model.ChartPart;

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
        ChartPart chartPart = getSelPart();
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
        ChartPart chartPart = getSelPart();
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
     * Returns the font of editor's selected shape.
     */
    public Font getFont()
    {
        ChartPart chartPart = getSelPart();
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
     * Returns the current effect.
     */
    public Effect getEffect()
    {
        ChartPart chartPart = getSelPart();
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
        ChartPart chartPart = getSelPart();
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
     * Returns the client View.
     */
    public View getClientView()  { return _editor.getUI(); }

    /**
     * Returns the Selected ChartPart.
     */
    private ChartPart getSelPart()
    {
        return _editor.getSel().getSelChartPart();
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
