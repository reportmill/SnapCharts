package snapcharts.apptools;
import snap.view.*;
import snapcharts.app.ChartPane;
import snapcharts.model.*;

/**
 * This class manages UI to edit Chart.Content. Though many of the properties are really for Axis.
 */
public class ContentInsp extends ChartPartInsp {

    // The Current ExtraInsp
    private ChartPartInsp  _extraInsp;

    // The View that holds the ExtraInsp
    private ColView  _extraInspBox;

    // The ContentStyleInsp
    private ContentStyleInsp  _styleInsp;

    // The ContentGridInsp
    private ContentGridInsp  _gridInsp;

    // The Content3DInsp
    private Content3DInsp  _3dInsp;

    /**
     * Constructor.
     */
    public ContentInsp(ChartPane aChartPane)
    {
        super(aChartPane);
    }

    /**
     * Returns the name.
     */
    @Override
    public String getName()  { return "Content Settings"; }

    /**
     * Returns the ChartPart.
     */
    @Override
    public ChartPart getChartPart()
    {
        Chart chart = getChart();
        Content content = chart.getContent();
        return content;
    }

    /**
     * Initialize UI.
     */
    @Override
    protected void initUI()
    {
        // Create
        _styleInsp = new ContentStyleInsp(_chartPane);

        // Create GridInsp
        _gridInsp = new ContentGridInsp(_chartPane);

        // Create 3DInsp
        _3dInsp = new Content3DInsp(_chartPane);

        // Get ExtraInspBox
        _extraInspBox = getView("ExtraInspBox", ColView.class);

        // Set default
        setExtraInsp(_styleInsp);
    }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        ChartPartInsp selInsp = getExtraInsp();
        setViewValue("BorderFillButton", selInsp == _styleInsp);
        setViewValue("GridButton", selInsp == _gridInsp);
        setViewValue("3DButton", selInsp == _3dInsp);

        Chart chart = getChart();
        boolean is3D = chart.getType().is3D();
        setViewEnabled("3DButton", is3D);

        selInsp.resetLater();
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle BorderFillButton
        if (anEvent.equals("BorderFillButton"))
            setExtraInsp(_styleInsp);
        if (anEvent.equals("GridButton"))
            setExtraInsp(_gridInsp);
        if (anEvent.equals("3DButton"))
            setExtraInsp(_3dInsp);
    }

    /**
     * Returns the Extra inspector.
     */
    private ChartPartInsp getExtraInsp()  { return _extraInsp; }

    /**
     * Sets the Extra inspector.
     */
    private void setExtraInsp(ChartPartInsp anInsp)
    {
        // If already set, just return
        if (anInsp == getExtraInsp()) return;

        // If old, remove it
        if (_extraInsp != null)
            _extraInspBox.removeChild(_extraInsp.getUI());

        // Set new
        _extraInsp = anInsp;

        // If new, add UI
        if(_extraInsp != null)
            _extraInspBox.addChild(_extraInsp.getUI());
    }

    /**
     * Shows the 3D inspector.
     */
    public void showContent3D()
    {
        getView("3DButton", ButtonBase.class).fire();
    }
}