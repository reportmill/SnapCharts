package snapcharts.view;
import snap.geom.Insets;
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
        View legendGraphic = new LegendGraphic();
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

        // Constant for default width of graphic
        private static final int DEFAULT_WIDTH = 30;

        // Constant for height of 'ShowArea' part of graphic
        private static final int AREA_HEIGHT = 6;

        /**
         * Constructor.
         */
        public LegendGraphic()
        {
            // Basic config
            setPrefWidth(DEFAULT_WIDTH);
        }

        /**
         * Override to return height to fit style.
         */
        @Override
        protected double getPrefHeightImpl(double aW)
        {
            // Get marked height of line/area/symbol
            double markedHeight = 0;
            if (_dataStyle.isShowLine())
                markedHeight = _dataStyle.getLineWidth();
            if (_dataStyle.isShowArea())
                markedHeight += AREA_HEIGHT - markedHeight / 2;
            if (_dataStyle.isShowSymbols())
                markedHeight = Math.max(_dataStyle.getSymbol().getSize(), markedHeight);

            // Return markedHeight plus insets height
            Insets ins = getInsetsAll();
            return Math.ceil(markedHeight + ins.getHeight());
        }

        /**
         * Override to paint sample of DataStyle line.
         */
        @Override
        protected void paintFront(Painter aPntr)
        {
            // Get area
            Insets ins = getInsetsAll();
            double areaX = ins.left;
            double areaY = ins.top;
            double areaW = getWidth() - ins.getWidth();
            double areaH = getHeight() - ins.getHeight();

            // Get info
            boolean showArea = _dataStyle.isShowArea();
            boolean showLine = _dataStyle.isShowLine();
            boolean showSymbols = _dataStyle.isShowSymbols();
            boolean disabled = _dataSet.isDisabled();
            double lineWidth = showLine ? _dataStyle.getLineWidth() : 0;
            double lineY = areaY + (areaH - lineWidth) / 2;
            if (showArea)
                lineY -= AREA_HEIGHT / 2;

            // Handle ShowArea
            if (showArea) {
                Color fillColor = _dataStyle.getFillColor();
                double lineMidY = lineY + lineWidth / 2;
                aPntr.fillRectWithPaint(areaX, lineMidY, areaW, AREA_HEIGHT, fillColor);
            }

            // Handle ShowLine
            if (showLine) {
                Color lineColor = _dataStyle.getLineColor();
                if (disabled) lineColor = DISABLED_COLOR;
                Stroke lineStroke = _dataStyle.getLineStroke();
                aPntr.setColor(lineColor);
                aPntr.setStroke(lineStroke);
                aPntr.drawLine(areaX, lineY, areaX + areaW, lineY);
            }

            // Handle ShowSymbol
            if (showSymbols) {

                // Get symbol fill color
                Color fillColor = _dataStyle.getSymbolColor();
                if (disabled) fillColor = DISABLED_COLOR;

                // Get symbol border color
                int borderWidth = _dataStyle.getSymbolBorderWidth();
                Color borderColor = _dataStyle.getSymbolBorderColor();
                if (disabled) borderColor = DISABLED_COLOR;

                // Paint Symbol at midpoint
                Symbol symbol = _dataStyle.getSymbol(); //.copyForSize(8);
                double areaMidX = areaX + areaW / 2;
                double lineMidY = lineY + lineWidth / 2;
                symbol.paintSymbol(aPntr, fillColor, borderColor, borderWidth, areaMidX, lineMidY);
            }
        }
    }
}
