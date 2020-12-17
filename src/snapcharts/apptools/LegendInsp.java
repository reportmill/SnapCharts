package snapcharts.apptools;

import snap.geom.Pos;
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
        setViewValue("Align" + align.ordinal(), true);

    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get Legend
        Legend legend = getChart().getLegend();

        // If user hits anything, turn on legend (was Handle ShowLegendCheckBox)
        legend.setShowLegend(anEvent.getBoolValue()); // if(anEvent.equals("ShowLegendCheckBox"))

        // Handle AlignX
        String name = anEvent.getName();
        if (name.startsWith("Align")) {
            int val = StringUtils.intValue(name);
            Pos pos = Pos.values()[val];
            legend.setPosition(pos);
        }
    }
}