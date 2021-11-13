package snapcharts.apptools;

import snap.view.ComboBox;
import snap.view.TextField;
import snap.view.ViewEvent;
import snapcharts.app.ChartPane;
import snapcharts.model.AxisType;
import snapcharts.model.ChartPart;
import snapcharts.model.DataSet;
import snapcharts.data.DataType;

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

        getView("ExprXText", TextField.class).setPromptText("x * 2 + 5");
        getView("ExprYText", TextField.class).setPromptText("y * 2 + 5");
        getView("ExprZText", TextField.class).setPromptText("z * 2 + 5");
    }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Get DataSet
        DataSet dataSet = getDataSet(); if (dataSet == null) return;

        // Reset NameText
        setViewValue("NameText", dataSet.getName());

        // Reset DataTypeComboBox
        setViewValue("DataTypeComboBox", dataSet.getDataType());

        // Reset YAxisButton, Y2AxisButton, Y3AxisButton, Y4AxisButton
        boolean isMultiYEnabled = dataSet.getChartType().isMultiYAxisType();
        getView("AxisTypeYBox").setVisible(isMultiYEnabled);
        if (isMultiYEnabled) {
            AxisType axisTypeY = dataSet.getAxisTypeY();
            setViewValue("YAxisButton", axisTypeY == AxisType.Y);
            setViewValue("Y2AxisButton", axisTypeY == AxisType.Y2);
            setViewValue("Y3AxisButton", axisTypeY == AxisType.Y3);
            setViewValue("Y4AxisButton", axisTypeY == AxisType.Y4);
        }

        // Reset ExprXText, ExprYText, ExprZText
        setViewValue("ExprXText", dataSet.getExprX());
        setViewValue("ExprYText", dataSet.getExprY());
        setViewValue("ExprZText", dataSet.getExprZ());
        DataType dataType = dataSet.getDataType();
        setViewVisible("ExprZBox", dataType.hasZ());

        // Reset StackedCheckBox, ShowLegendEntryCheckBox
        setViewValue("StackedCheckBox", dataSet.isStacked());
        setViewValue("ShowLegendEntryCheckBox", dataSet.isShowLegendEntry());
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get DataSet
        DataSet dataSet = getDataSet(); if (dataSet == null) return;

        // Handle NameText
        if (anEvent.equals("NameText")) {
            dataSet.setName(anEvent.getStringValue());
            _chartPane.getDocPane().docItemNameChanged();
        }

        // Handle DataTypeCombo
        if (anEvent.equals("DataTypeComboBox")) {
            DataType dataType = (DataType) getViewSelItem("DataTypeComboBox");
            dataSet.setDataType(dataType);
        }

        // Reset YAxisButton, Y2AxisButton, Y3AxisButton, Y4AxisButton
        if (anEvent.equals("YAxisButton"))
            dataSet.setAxisTypeY(AxisType.Y);
        if (anEvent.equals("Y2AxisButton"))
            dataSet.setAxisTypeY(AxisType.Y2);
        if (anEvent.equals("Y3AxisButton"))
            dataSet.setAxisTypeY(AxisType.Y3);
        if (anEvent.equals("Y4AxisButton"))
            dataSet.setAxisTypeY(AxisType.Y4);

        // Handle ExprXText, ExprYText, ExprZText
        if (anEvent.equals("ExprXText"))
            dataSet.setExprX(anEvent.getStringValue());
        if (anEvent.equals("ExprYText"))
            dataSet.setExprY(anEvent.getStringValue());
        if (anEvent.equals("ExprZText"))
            dataSet.setExprZ(anEvent.getStringValue());

        // Handle StackedCheckBox: Set them all
        if (anEvent.equals("StackedCheckBox")) {
            DataSet[] dataSets = dataSet.getDataSetList().getDataSets();
            for (DataSet dset : dataSets)
                dset.setStacked(anEvent.getBoolValue());
        }

        // Handle ShowLegendEntryCheckBox
        if (anEvent.equals("ShowLegendEntryCheckBox"))
            dataSet.setShowLegendEntry(anEvent.getBoolValue());
    }
}