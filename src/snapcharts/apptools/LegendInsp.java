/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.apptools;
import snap.geom.Pos;
import snap.gfx.Border;
import snap.gfx.Color;
import snap.util.StringUtils;
import snap.view.ViewEvent;
import snapcharts.app.ChartPane;
import snapcharts.model.ChartPart;
import snapcharts.model.Legend;

/**
 * A class to manage UI to edit a ChartView Legend.
 */
public class LegendInsp extends ChartPartInsp {

    /**
     * Constructor.
     */
    public LegendInsp(ChartPane aChartPane)
    {
        super(aChartPane);
    }

    /**
     * Returns the name.
     */
    @Override
    public String getName()  { return "Legend Settings"; }

    /**
     * Returns the ChartPart.
     */
    @Override
    public ChartPart getChartPart()  { return getChart().getLegend(); }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Get Legend
        Legend legend = getChart().getLegend();

        // Reset ShowLegendCheckBox
        setViewValue("ShowLegendCheckBox", legend.isShowLegend());

        // Reset TitleText
        setViewValue("TitleText", legend.getTitle().getText());

        // Reset AlignX buttons
        Pos align = legend.getPosition();
        if (!legend.isShowLegend()) align = Pos.CENTER;
        setViewValue("Align" + align.ordinal(), true);

        // Reset InsideCheckBox
        setViewValue("InsideCheckBox", legend.isInside());

        // Reset FillColorButton, FillColorResetButton
        setViewValue("FillColorButton", legend.getFill() != null ? legend.getFill().getColor() : null);
        setViewVisible("FillColorResetButton", legend.getFill() != null);

        // Reset BorderColorButton
        Border border = legend.getBorder();
        setViewValue("BorderColorButton", border != null ? border.getColor() : null);

        // Reset BorderWidthText, BorderWidthResetButton
        setViewValue("BorderWidthText", border != null ? border.getWidth() : 0);
        setViewVisible("BorderResetButton", border != null);
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get Legend
        Legend legend = getChart().getLegend();

        // Handle ShowLegendCheckBox: Just used to turn off. Any other UI interaction turns legend on
        if (anEvent.equals("ShowLegendCheckBox") && !anEvent.getBoolValue())
            legend.setShowLegend(false);
        else legend.setShowLegend(true);

        // Handle TitleText
        if (anEvent.equals("TitleText"))
            legend.getTitle().setText(anEvent.getStringValue());

        // Handle AlignX
        String name = anEvent.getName();
        if (name.startsWith("Align")) {
            int val = StringUtils.intValue(name);
            Pos pos = Pos.values()[val];
            legend.setPosition(pos);
        }

        // Handle InsideCheckBox
        if (anEvent.equals("InsideCheckBox")) {
            legend.setInside(anEvent.getBoolValue());
            //legend.setEffect(anEvent.getBoolValue() ? new ShadowEffect() : null);
        }

        // Handle FillColorButton, FillColorResetButton
        if (anEvent.equals("FillColorButton")) {
            Color color = (Color) getViewValue("FillColorButton");
            legend.setFill(color);
        }
        if (anEvent.equals("FillColorResetButton"))
            legend.setFill(null);

        // Handle BorderColorButton
        Border border = legend.getBorder();
        Border borderNonNull = border != null ? border : Border.blackBorder();
        if (anEvent.equals("BorderColorButton")) {
            Color color = (Color) getViewValue("BorderColorButton");
            legend.setBorder(borderNonNull.copyForColor(color));
        }

        // Handle BorderWidthText, BorderWidthAdd1Button, BorderWidthSub1Button, BorderWidthResetButton
        if (anEvent.equals("BorderWidthText"))
            legend.setBorder(borderNonNull.copyForStrokeWidth(Math.max(anEvent.getIntValue(), 0)));
        if (anEvent.equals("BorderWidthAdd1Button"))
            legend.setBorder(border == null ? borderNonNull : border.copyForStrokeWidth(border.getWidth() + 1));
        if (anEvent.equals("BorderWidthSub1Button"))
            legend.setBorder(borderNonNull.copyForStrokeWidth(Math.max(borderNonNull.getWidth() - 1, 1)));
        if (anEvent.equals("BorderResetButton"))
            legend.setBorder(null);
    }
}