package snapcharts.apptools;

import snap.view.ComboBox;
import snap.view.TextField;
import snap.view.ViewEvent;
import snapcharts.app.ChartPane;
import snapcharts.data.DataStore;
import snapcharts.model.AxisType;
import snapcharts.model.ChartPart;
import snapcharts.model.Trace;
import snapcharts.data.DataType;

/**
 * A class to manage UI to edit basic Trace props.
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
    public String getName()  { return "Trace Settings"; }

    /**
     * Returns the ChartPart.
     */
    @Override
    public ChartPart getChartPart()  { return getTrace(); }

    /**
     * Returns the Trace.
     */
    public Trace getTrace()  { return _chartPane.getTrace(); }

    /**
     * Init UI.
     */
    @Override
    protected void initUI()
    {
        // Configure DataTypeComboBox
        ComboBox<DataType> dataTypeComboBox = getView("DataTypeComboBox", ComboBox.class);
        dataTypeComboBox.setItems(DataType.values());

        // Configure ThetaUnitComboBox
        ComboBox<DataStore.ThetaUnit> thetaUnitComboBox = getView("ThetaUnitComboBox", ComboBox.class);
        thetaUnitComboBox.setItems(DataStore.ThetaUnit.values());

        getView("ExprXText", TextField.class).setPromptText("x * 2 + 5");
        getView("ExprYText", TextField.class).setPromptText("y * 2 + 5");
        getView("ExprZText", TextField.class).setPromptText("z * 2 + 5");
    }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Get Trace
        Trace trace = getTrace(); if (trace == null) return;

        // Reset NameText
        setViewValue("NameText", trace.getName());

        // Reset DataTypeComboBox
        DataType dataType = trace.getDataType();
        setViewValue("DataTypeComboBox", trace.getDataType());

        // Reset ThetaUnitComboBox
        boolean isPolar = dataType.isPolar();
        setViewVisible("ThetaUnitBox", isPolar);
        if (isPolar)
            setViewValue("ThetaUnitComboBox", trace.getThetaUnit());

        // Reset YAxisButton, Y2AxisButton, Y3AxisButton, Y4AxisButton
        boolean isMultiYEnabled = trace.getChartType().isMultiYAxisType();
        getView("AxisTypeYBox").setVisible(isMultiYEnabled);
        if (isMultiYEnabled) {
            AxisType axisTypeY = trace.getAxisTypeY();
            setViewValue("YAxisButton", axisTypeY == AxisType.Y);
            setViewValue("Y2AxisButton", axisTypeY == AxisType.Y2);
            setViewValue("Y3AxisButton", axisTypeY == AxisType.Y3);
            setViewValue("Y4AxisButton", axisTypeY == AxisType.Y4);
        }

        // Reset ExprXText, ExprYText, ExprZText
        setViewValue("ExprXText", trace.getExprX());
        setViewValue("ExprYText", trace.getExprY());
        setViewValue("ExprZText", trace.getExprZ());
        setViewVisible("ExprZBox", dataType.hasZ());

        // Reset StackedCheckBox, ShowLegendEntryCheckBox
        setViewValue("StackedCheckBox", trace.isStacked());
        setViewValue("ShowLegendEntryCheckBox", trace.isShowLegendEntry());
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get Trace
        Trace trace = getTrace(); if (trace == null) return;

        // Handle NameText
        if (anEvent.equals("NameText")) {
            trace.setName(anEvent.getStringValue());
            _chartPane.getDocPane().docItemNameChanged();
        }

        // Handle DataTypeCombo
        if (anEvent.equals("DataTypeComboBox")) {
            DataType dataType = (DataType) getViewSelItem("DataTypeComboBox");
            trace.setDataType(dataType);
        }

        // Handle ThetaUnitComboBox
        if (anEvent.equals("ThetaUnitComboBox")) {
            DataStore.ThetaUnit thetaUnit = (DataStore.ThetaUnit) anEvent.getSelItem();
            trace.setThetaUnit(thetaUnit);
        }

        // Reset YAxisButton, Y2AxisButton, Y3AxisButton, Y4AxisButton
        if (anEvent.equals("YAxisButton"))
            trace.setAxisTypeY(AxisType.Y);
        if (anEvent.equals("Y2AxisButton"))
            trace.setAxisTypeY(AxisType.Y2);
        if (anEvent.equals("Y3AxisButton"))
            trace.setAxisTypeY(AxisType.Y3);
        if (anEvent.equals("Y4AxisButton"))
            trace.setAxisTypeY(AxisType.Y4);

        // Handle ExprXText, ExprYText, ExprZText
        if (anEvent.equals("ExprXText"))
            trace.setExprX(anEvent.getStringValue());
        if (anEvent.equals("ExprYText"))
            trace.setExprY(anEvent.getStringValue());
        if (anEvent.equals("ExprZText"))
            trace.setExprZ(anEvent.getStringValue());

        // Handle StackedCheckBox: Set them all
        if (anEvent.equals("StackedCheckBox")) {
            Trace[] traces = trace.getTraceList().getTraces();
            for (Trace trc : traces)
                trc.setStacked(anEvent.getBoolValue());
        }

        // Handle ShowLegendEntryCheckBox
        if (anEvent.equals("ShowLegendEntryCheckBox"))
            trace.setShowLegendEntry(anEvent.getBoolValue());
    }
}