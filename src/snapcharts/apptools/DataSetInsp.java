package snapcharts.apptools;

import snap.view.ComboBox;
import snap.view.ViewEvent;
import snapcharts.app.ChartPane;
import snapcharts.model.AxisType;
import snapcharts.model.ChartPart;
import snapcharts.model.DataSet;
import snapcharts.model.DataType;

/**
 * A class to manage UI to edit basic DataSet props.
 */
public class DataSetInsp extends ChartPartInsp {

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
     * Returns the ChartPart.
     */
    @Override
    public ChartPart getChartPart()  { return getDataSet(); }

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
        DataSet dset = getDataSet(); if (dset==null) return;

        // Reset NameText
        setViewValue("NameText", dset.getName());

        // Reset DataTypeComboBox
        setViewValue("DataTypeComboBox", dset.getDataType());

        // Reset YAxisButton, Y2AxisButton, Y3AxisButton, Y4AxisButton
        setViewValue("YAxisButton", dset.getAxisTypeY() == AxisType.Y);
        setViewValue("Y2AxisButton", dset.getAxisTypeY() == AxisType.Y2);
        setViewValue("Y3AxisButton", dset.getAxisTypeY() == AxisType.Y3);
        setViewValue("Y4AxisButton", dset.getAxisTypeY() == AxisType.Y4);

        // Reset StrokeWidth
        setViewValue("LineWidthText", 1);

        // Reset ShowSymbolsCheckBox
        setViewValue("ShowSymbolsCheckBox", dset.isShowSymbols());
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get DataSet
        DataSet dset = getDataSet(); if (dset==null) return;

        // Handle NameText
        if (anEvent.equals("NameText")) {
            dset.setName(anEvent.getStringValue());
            _chartPane.getDocPane().docItemNameChanged();
        }

        // Handle DataTypeCombo
        if (anEvent.equals("DataTypeComboBox")) {
            DataType dataType = (DataType) getViewSelItem("DataTypeComboBox");
            dset.setDataType(dataType);
        }

        // Reset YAxisButton, Y2AxisButton, Y3AxisButton, Y4AxisButton
        if (anEvent.equals("YAxisButton"))
            dset.setAxisTypeY(AxisType.Y);
        if (anEvent.equals("Y2AxisButton"))
            dset.setAxisTypeY(AxisType.Y2);
        if (anEvent.equals("Y3AxisButton"))
            dset.setAxisTypeY(AxisType.Y3);
        if (anEvent.equals("Y4AxisButton"))
            dset.setAxisTypeY(AxisType.Y4);

        // Handle ShowSymbolsCheckBox
        if (anEvent.equals("ShowSymbolsCheckBox"))
            dset.setShowSymbols(anEvent.getBoolValue());
    }
}