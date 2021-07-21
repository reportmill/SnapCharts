package snapcharts.view;
import snap.geom.Insets;
import snap.geom.Shape;
import snap.geom.Transform;
import snap.gfx.*;
import snap.view.Label;
import snap.view.View;
import snapcharts.model.DataSet;
import snapcharts.model.DataStyle;
import snapcharts.model.Legend;
import snapcharts.model.Symbol;

/**
 * A placeholder view to paint entry graphic.
 */
public class LegendEntryView extends Label {

    // The DataSet
    private DataSet _dataSet;

    // The DataStyle
    private DataStyle _dataStyle;

    // Constants
    private static Color DISABLED_COLOR = Color.LIGHTGRAY;

    /**
     * Constructor.
     */
    public LegendEntryView(Legend aLegend, DataSet aDataSet)
    {
        super();
        _dataSet = aDataSet;
        _dataStyle = aDataSet.getDataStyle();

        // Set ShowText
        setShowText(true);

        // Set TextFill
        Paint textFill = aDataSet.isDisabled() ? DISABLED_COLOR : aLegend.getTextFill();
        setTextFill(textFill);

        // Create/add LegendGraphic
        View legendGraphic = new LegendGraphic(aDataSet);
        setGraphic(legendGraphic);
    }

    /**
     * Returns whether this view is showing text.
     */
    public boolean isShowText()
    {
        String text = getText();
        return text != null;
    }

    /**
     * Sets whether this view is showing text.
     */
    public void setShowText(boolean aValue)
    {
        if (aValue == isShowText()) return;
        String text = aValue ? _dataSet.getName() : null;
        setText(text);
    }

    /**
     * Inner class for legend graphic.
     */
    private class LegendGraphic extends View {

        /**
         * Constructor.
         */
        public LegendGraphic(DataSet aDataSet)
        {
            // Basic config
            setPrefSize(30, 20);
            setPadding(2, 2, 2, 2);

            // Set DataSet, DataStyle
            _dataSet = aDataSet;
            _dataStyle = aDataSet.getDataStyle();
        }

        @Override
        protected void paintFront(Painter aPntr)
        {
            // Get area
            Insets ins = getInsetsAll();
            double areaX = ins.left;
            double areaY = ins.top;
            double areaW = getWidth() - ins.getWidth();
            double areaH = getHeight() - ins.getHeight();

            // Whether disabled
            boolean disabled = _dataSet.isDisabled();

            // Handle ShowArea
            if (_dataStyle.isShowArea()) {
                Color fillColor = _dataStyle.getFillColor();
                aPntr.fillRectWithPaint(2, 9, 20, 7, fillColor);
            }

            // Handle ShowLine
            if (_dataStyle.isShowLine()) {
                Color lineColor = _dataStyle.getLineColor();
                if (disabled) lineColor = DISABLED_COLOR;
                double lineWidth = _dataStyle.getLineWidth();
                double lineY = areaY + (areaH - lineWidth) / 2;
                aPntr.fillRectWithPaint(areaX, lineY, areaW, lineWidth, lineColor);
            }

            // Handle ShowSymbol
            if (_dataStyle.isShowSymbols()) {

                // Get symbol fill color
                Color fillColor = _dataStyle.getSymbolColor();
                if (disabled) fillColor = DISABLED_COLOR;

                // Get symbol border color
                int borderWidth = _dataStyle.getSymbolBorderWidth();
                Color borderColor = _dataStyle.getSymbolBorderColor();
                if (disabled) borderColor = DISABLED_COLOR;

                // Paint Symbol at midpoint
                Symbol symbol = _dataStyle.getSymbol().copyForSize(8);
                double areaMidX = areaX + areaW / 2;
                double areaMidY = areaY + areaH / 2;
                symbol.paintSymbol(aPntr, fillColor, borderColor, borderWidth, areaMidX, areaMidY);
            }
        }
    }
}
