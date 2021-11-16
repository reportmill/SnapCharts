package snapcharts.view;
import snap.geom.Insets;
import snap.gfx.*;
import snap.view.Label;
import snap.view.View;
import snapcharts.model.*;

/**
 * A placeholder view to paint entry graphic.
 */
public class LegendEntryView extends Label {

    // The Trace
    private Trace  _trace;

    // The DataStyle
    private TraceStyle _traceStyle;

    // The SymbolStyle
    private SymbolStyle _symbolStyle;

    // Constants
    private static Color DISABLED_COLOR = Color.LIGHTGRAY;

    /**
     * Constructor.
     */
    public LegendEntryView(Legend aLegend, Trace aTrace)
    {
        super();
        _trace = aTrace;
        _traceStyle = aTrace.getTraceStyle();
        _symbolStyle = _traceStyle.getSymbolStyle();

        // Set ShowText
        setShowText(true);

        // Set TextFill
        Paint textFill = aTrace.isDisabled() ? DISABLED_COLOR : aLegend.getTextFill();
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
        protected double getPrefHeightImpl(double aW)
        {
            // Get marked height of line/area/symbol
            double markedHeight = 0;
            if (_traceStyle.isShowLine())
                markedHeight = _traceStyle.getLineWidth();
            if (_traceStyle.isShowArea())
                markedHeight += AREA_HEIGHT - markedHeight / 2;
            if (_traceStyle.isShowSymbols())
                markedHeight = Math.max(_symbolStyle.getSymbol().getSize(), markedHeight);

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
            boolean showArea = _traceStyle.isShowArea();
            boolean showLine = _traceStyle.isShowLine();
            boolean showSymbols = _traceStyle.isShowSymbols();
            boolean disabled = _trace.isDisabled();
            double lineWidth = showLine ? _traceStyle.getLineWidth() : 0;
            double lineY = areaY + areaH / 2;
            if (showArea)
                lineY -= AREA_HEIGHT / 2;

            // Handle ShowArea
            if (showArea) {
                Color fillColor = _traceStyle.getFillColor();
                double lineMidY = lineY + lineWidth / 2;
                aPntr.fillRectWithPaint(areaX, lineMidY, areaW, AREA_HEIGHT, fillColor);
            }

            // Handle ShowLine
            if (showLine) {
                Color lineColor = _traceStyle.getLineColor();
                if (disabled) lineColor = DISABLED_COLOR;
                Stroke lineStroke = _traceStyle.getLineStroke();
                aPntr.setColor(lineColor);
                aPntr.setStroke(lineStroke);
                aPntr.drawLine(areaX, lineY, areaX + areaW, lineY);
            }

            // Handle ShowSymbol
            if (showSymbols) {

                // Get symbol fill color
                Color fillColor = _symbolStyle.getFillColor();
                if (disabled) fillColor = DISABLED_COLOR;

                // Get symbol border color
                double borderWidth = _symbolStyle.getLineWidth();
                Color borderColor = _symbolStyle.getLineColor();
                if (disabled) borderColor = DISABLED_COLOR;

                // Paint Symbol at midpoint
                Symbol symbol = _symbolStyle.getSymbol(); //.copyForSize(8);
                double areaMidX = areaX + areaW / 2;
                symbol.paintSymbol(aPntr, fillColor, borderColor, borderWidth, areaMidX, lineY);
            }
        }
    }
}
