package snapcharts.apptools;

import snap.view.ViewEvent;
import snap.view.ViewOwner;
import snapcharts.app.ChartPane;
import snapcharts.app.DataSetPane;
import snapcharts.model.Chart;
import snapcharts.model.DataSet;
import snapcharts.views.ChartView;

/**
 * A class to manage UI to edit basic DataSet props.
 */
public class DataSetBasicTool extends ViewOwner {

    // The DataSetPane
    private DataSetPane  _dsetPane;

    /**
     * Constructor.
     */
    public DataSetBasicTool(DataSetPane aDSP)
    {
        _dsetPane = aDSP;
    }

    /**
     * Returns the DataSet.
     */
    public DataSet getDataSet()  { return _dsetPane.getDataSet(); }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Get DataSet
        DataSet dset = getDataSet();

        // Reset NameText, SubtitleText, YAxisTitleText
        setViewValue("NameText", dset.getName());

        // Reset StrokeWidth
        setViewValue("LineWidthText", 1);

        // Reset ShowSymbolsCheckBox
        //setViewValue("ShowSymbolsCheckBox", dset.isShowSymbols());
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get DataSet
        DataSet dset = getDataSet();

        // Handle NameText
        if(anEvent.equals("NameText"))
            dset.setName(anEvent.getStringValue());
    }
}