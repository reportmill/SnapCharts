package snapcharts.apptools;

import snap.view.ComboBox;
import snap.view.ViewEvent;
import snap.view.ViewOwner;
import snapcharts.app.ChartPane;
import snapcharts.app.DataSetPane;
import snapcharts.model.Chart;
import snapcharts.model.DataSet;
import snapcharts.model.DataType;
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
     * Init UI.
     */
    @Override
    protected void initUI()
    {
        ComboBox<DataType> dataTypeComboBox = getView("DataTypeComboBox", ComboBox.class);
        dataTypeComboBox.setItems(DataType.values());
    }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Get DataSet
        DataSet dset = getDataSet();

        // Reset NameText
        setViewValue("NameText", dset.getName());

        // Reset DataTypeComboBox
        setViewValue("DataTypeComboBox", dset.getDataType());

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