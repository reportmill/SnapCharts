package snapcharts.view;
import snap.geom.Insets;
import snap.gfx.*;
import snap.view.Label;
import snap.view.View;
import snapcharts.charts.*;

/**
 * A placeholder view to paint entry graphic.
 */
public class LegendEntryView extends Label {

    // The Trace
    private Trace  _trace;

    // The PointStyle
    private PointStyle  _pointStyle;

    // Constants
    private static Color DISABLED_COLOR = Color.LIGHTGRAY;

    /**
     * Constructor.
     */
    public LegendEntryView(Legend aLegend, Trace aTrace)
    {
        super();
        _trace = aTrace;
        _pointStyle = _trace.getPointStyle();

        // Set ShowText
        setShowText(true);

        // Set text color
        Color textColor = aTrace.isDisabled() ? DISABLED_COLOR : aLegend.getTextFill();
        setTextColor(textColor);

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
        return text != null && !text.isEmpty();
    }

    /**
     * Sets whether this view is showing text.
     */
    public void setShowText(boolean aValue)
    {
        if (aValue == isShowText()) return;
        String text = aValue ? _trace.getName() : null;
        setText(text);
    }

    /**
     * Inner class for legend graphic.
     */
    private class LegendGraphic extends View {

        // Constant for default width of graphic
        private static final int DEFAULT_WIDTH = 30;

        // Constant for height of 'ShowArea' part of graphic
        private static final int AREA_HEIGHT = 8;

        /**
         * Constructor.
         */
        public LegendGraphic()
        {
            // Basic config
            setPrefWidth(DEFAULT_WIDTH);

            // Set padding to get better centering for default font size
            // This is bogus - Label.StringView really needs a UseGlyphSizing property so StringView is centered
            setPadding(new Insets(0, 0, 1, 0));
        }

        /**
         * Override to return height to fit style.
         */
        @Override
        protected double computePrefHeight(double aW)
        {
            // Get marked height of line/area/symbol
            double markedHeight = 0;
            if (_trace.isShowLine())
                markedHeight = _trace.getLineWidth();
            if (_trace.isShowArea())
                markedHeight += AREA_HEIGHT - markedHeight / 2;
            if (_trace.isShowPoints())
                markedHeight = Math.max(_pointStyle.getSymbol().getSize(), markedHeight);

            // Return markedHeight plus insets height
            Insets ins = getInsetsAll();
            return Math.ceil(markedHeight + ins.getHeight());
        }

        /**
         * Override to paint sample of Trace line.
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
            boolean showArea = _trace.isShowArea();
            boolean showLine = _trace.isShowLine();
            boolean showPoints = _trace.isShowPoints();
            boolean disabled = _trace.isDisabled();
            double lineWidth = showLine ? _trace.getLineWidth() : 0;
            double lineY = areaY + areaH / 2;
            if (showArea)
                lineY -= AREA_HEIGHT / 2;

            // Handle ShowArea
            if (showArea) {
                Color fillColor = _trace.getFillColor();
                double lineMidY = lineY + lineWidth / 2;
                aPntr.fillRectWithPaint(areaX, lineMidY, areaW, AREA_HEIGHT, fillColor);
            }

            // Handle ShowLine
            if (showLine) {
                Color lineColor = _trace.getLineColor();
                if (disabled) lineColor = DISABLED_COLOR;
                Stroke lineStroke = _trace.getLineStroke();
                aPntr.setColor(lineColor);
                aPntr.setStroke(lineStroke);
                aPntr.drawLine(areaX, lineY, areaX + areaW, lineY);
            }

            // Handle ShowSymbol
            if (showPoints) {

                // Get symbol fill color
                Color fillColor = _pointStyle.getFillColor();
                if (disabled) fillColor = DISABLED_COLOR;

                // Get symbol border color
                double borderWidth = _pointStyle.getLineWidth();
                Color borderColor = _pointStyle.getLineColor();
                if (disabled) borderColor = DISABLED_COLOR;

                // Paint Symbol at midpoint
                Symbol symbol = _pointStyle.getSymbol(); //.copyForSize(8);
                double areaMidX = areaX + areaW / 2;
                symbol.paintSymbol(aPntr, fillColor, borderColor, borderWidth, areaMidX, lineY);
            }
        }
    }
}
