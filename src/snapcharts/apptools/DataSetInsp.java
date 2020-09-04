package snapcharts.apptools;
import snap.gfx.Color;
import snap.gfx.Image;
import snap.view.*;
import snap.viewx.TextPane;
import snapcharts.app.ChartPane;
import snapcharts.app.Collapser;
import snapcharts.apptools.DataSetBasicTool;

/**
 * A class to manage the inspector for ChartSetPane.
 */
public class DataSetInsp extends ChartPartInsp {

    // The ColView that holds UI for child inspectors
    private ColView  _inspColView;

    // The DataSetBasicTool
    private DataSetBasicTool  _dsetBasic;

    /**
     * Constructor.
     */
    public DataSetInsp(ChartPane aChartPane)
    {
        super(aChartPane);
    }

    /**
     * Returns the name.
     */
    @Override
    public String getName()  { return "DataSet Settings"; }

    /**
     * Initializes UI panel for the inspector.
     */
    public void initUI()
    {
        // Get InspColView
        _inspColView = getView("InspColView", ColView.class);

        // Get ChartBasicTool
        _dsetBasic = new DataSetBasicTool(_chartPane);
        _inspColView.addChild(_dsetBasic.getUI());
        Collapser.createCollapserAndLabel(_dsetBasic.getUI(), "DataSet Settings");
    }

    /**
     * Refreshes the inspector for the current editor selection.
     */
    public void resetUI()
    {
        // Reset child inspectors
        _dsetBasic.resetLater();
    }

    /**
     * Handles changes to the inspector UI controls.
     */
    public void respondUI(ViewEvent anEvent)
    {
        // Handle ViewGeneralButton
        //if (anEvent.equals("ViewGeneralButton")) setInspector(_viewTool);
    }
}