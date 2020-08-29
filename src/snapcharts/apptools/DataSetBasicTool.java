package snapcharts.apptools;

import snap.view.ComboBox;
import snap.view.ViewEvent;
import snap.view.ViewOwner;
import snapcharts.app.ChartPane;
import snapcharts.model.DataSet;
import snapcharts.model.DataType;

/**
 * A class to manage UI to edit basic DataSet props.
 */
public class DataSetBasicTool extends ViewOwner {

    // The ChartPane
    private ChartPane _chartPane;

    /**
     * Constructor.
     */
    public DataSetBasicTool(ChartPane aChartPane)
    {
        _chartPane = aChartPane;
    }

    /**
     * Returns the DataSet.
     */
    public DataSet getDataSet()  { return _chartPane.getDataSet(); }

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
        if(anEvent.equals("NameText")) {
            dset.setName(anEvent.getStringValue());
            _chartPane.getDocPane().docItemNameChanged();
        }
    }
}