package snapcharts.apptools;
import snap.view.ViewEvent;
import snapcharts.app.ChartPane;
import snapcharts.model.ChartPart;
import snapcharts.model.Header;

/**
 * A class to manage UI to edit a ChartView Header.
 */
public class ContourAxisInsp extends ChartPartInsp {

    /**
     * Constructor.
     */
    public ContourAxisInsp(ChartPane aChartPane)
    {
        super(aChartPane);
    }

    /**
     * Returns the name.
     */
    @Override
    public String getName()  { return "Color Bar Settings"; }

    /**
     * Returns the ChartPart.
     */
    @Override
    public ChartPart getChartPart()  { return getChart().getHeader(); }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Get Header
        Header header = getChart().getHeader();

        // Reset TitleText, SubtitleText
        setViewValue("TitleText", header.getTitle());
        setViewValue("SubtitleText", header.getSubtitle());
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get Header
        Header header = getChart().getHeader();

        // Handle TitleText, SubtitleText
        if(anEvent.equals("TitleText")) header.setTitle(anEvent.getStringValue());
        if(anEvent.equals("SubtitleText")) header.setSubtitle(anEvent.getStringValue());
    }
}