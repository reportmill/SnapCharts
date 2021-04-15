package snapcharts.apptools;

import snap.geom.Pos;
import snap.gfx.Color;
import snap.gfx.ShadowEffect;
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

        // Reset AlignX buttons
        Pos align = legend.getPosition();
        if (!legend.isShowLegend()) align = Pos.CENTER;
        setViewValue("Align" + align.ordinal(), true);

        // Reset InsideCheckBox
        setViewValue("InsideCheckBox", legend.isInside());
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get Legend
        Legend legend = getChart().getLegend();

        // Handle ShowLegendCheckBox: Just used to turn off. Any other UI interaction turns legend on
        if(anEvent.equals("ShowLegendCheckBox") && !anEvent.getBoolValue())
            legend.setShowLegend(false);
        else legend.setShowLegend(true);

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
    }
}