package snapcharts.apptools;
import snap.gfx.Color;
import snap.view.*;
import snapcharts.app.ChartPane;
import snapcharts.data.DataUnit;
import snapcharts.charts.*;
import snapcharts.data.DataType;

/**
 * A class to manage UI to edit basic Trace props.
 */
public class TraceInsp extends ChartPartInsp {

    // The Current ChartPartInsp
    private ChartPartInsp  _currentInsp;

    // The View that holds the child insp
    private ColView  _inspBox;

    // The LineStyleInsp
    private TraceLineStyleInsp  _lineStyleInsp;

    // The AreaStyleInsp
    private TraceAreaStyleInsp  _areaStyleInsp;

    // The PointStyleInsp
    private TracePointStyleInsp  _pointStyleInsp;

    // The TagStyleInsp
    private TraceTagStyleInsp  _tagStyleInsp;

    // The TagSpacingInsp
    private TraceSpacingInsp  _spacingInsp;

    // The Current ChartPartInsp
    private ChartPartInsp  _traceStyleInsp;

    // The View that holds the TraceStyle insp
    private ColView  _traceStyleInspBox;

    // The PolarTraceInsp
    private PolarTraceInsp _polarTraceInsp;

    // The ContourTraceInsp
    private ContourTraceInsp  _contourTraceInsp;

    /**
     * Constructor.
     */
    public TraceInsp(ChartPane aChartPane)
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
    public Trace getTrace()
    {
        // If SelChartPart is Trace, return it
        ChartPart selPart = _chartPane.getSelChartPart();
        if (selPart instanceof Trace)
            return (Trace) selPart;

        // Otherwise, return first Trace ( Chart.Content.Traces[0] )
        Chart chart = getChart();
        Content content = chart.getContent();
        Trace[] traces = content.getTraces();
        return traces.length > 0 ? traces[0] : null;
    }

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
        ComboBox<DataUnit> thetaUnitComboBox = getView("ThetaUnitComboBox", ComboBox.class);
        thetaUnitComboBox.setItems(DataUnit.getAllUnits());

        getView("ExprXText", TextField.class).setPromptText("x * 2 + 5");
        getView("ExprYText", TextField.class).setPromptText("y * 2 + 5");
        getView("ExprZText", TextField.class).setPromptText("z * 2 + 5");

        // Get InspectorBox
        _inspBox = getView("InspectorBox", ColView.class);

        // Create inspectors
        _lineStyleInsp = new TraceLineStyleInsp(_chartPane);
        _areaStyleInsp = new TraceAreaStyleInsp(_chartPane);
        _pointStyleInsp = new TracePointStyleInsp(_chartPane, this);
        _tagStyleInsp = new TraceTagStyleInsp(_chartPane, this);

        // Get index
        View traceStyleInspSep = getView("TraceStyleInspSep");
        int spacingInspInsertIndex = traceStyleInspSep.indexInParent();

        // Add SpacingInsp
        _spacingInsp = new TraceSpacingInsp(_chartPane);
        ViewUtils.addChild(getUI(ParentView.class), _spacingInsp.getUI(), spacingInspInsertIndex);
        _spacingInsp.getUI().setVisible(false);

        // Get TraceStyleInspBox
        _traceStyleInspBox = getView("TraceStyleInspBox", ColView.class);
    }

    @Override
    protected void initShowing()
    {
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
            setViewSelItem("ThetaUnitComboBox", trace.getThetaUnit());

        // Reset YAxisButton, Y2AxisButton, Y3AxisButton, Y4AxisButton
        boolean isMultiYEnabled = trace.getType().isMultiYAxisType();
        setViewVisible("AxisTypeYBox", isMultiYEnabled);
        if (isMultiYEnabled) {
            AxisType axisTypeY = trace.getAxisTypeY();
            setViewValue("YAxisButton", axisTypeY == AxisType.Y);
            setViewValue("Y2AxisButton", axisTypeY == AxisType.Y2);
            setViewValue("Y3AxisButton", axisTypeY == AxisType.Y3);
            setViewValue("Y4AxisButton", axisTypeY == AxisType.Y4);

            // If only one dataset, hide the Y3/Y4 buttons
            boolean showAllAxes = getTrace().getContent().getTraceCount() > 1;
            setViewVisible("Y3AxisButton", showAllAxes);
            setViewVisible("Y4AxisButton", showAllAxes);
        }

        // Reset ExprXText, ExprYText, ExprZText
        setViewValue("ExprXText", trace.getExprX());
        setViewValue("ExprYText", trace.getExprY());
        setViewValue("ExprZText", trace.getExprZ());
        setViewVisible("ExprZBox", dataType.hasZ());

        // Reset StackedCheckBox, ShowLegendEntryCheckBox
        setViewValue("StackedCheckBox", trace.isStacked());
        setViewValue("ShowLegendEntryCheckBox", trace.isShowLegendEntry());

        // Reset LineStyleButton, AreaStyleButton, PointsStyleButton, TagsStyleButton
        setViewValue("LineStyleButton", _currentInsp == _lineStyleInsp);
        setViewValue("AreaStyleButton", _currentInsp == _areaStyleInsp);
        setViewValue("PointsStyleButton", _currentInsp == _pointStyleInsp);
        setViewValue("TagsStyleButton", _currentInsp == _tagStyleInsp);

        // Reset fonts
        setButtonHighlight("LineStyleButton", trace.isShowLine());
        setButtonHighlight("AreaStyleButton", trace.isShowArea());
        setButtonHighlight("PointsStyleButton", trace.isShowPoints());
        setButtonHighlight("TagsStyleButton", trace.isShowTags());

        // Update child inspector
        ChartPartInsp traceTypeInsp = getCurrentInspector();
        //setCurrentInspector(traceTypeInsp);
        if (traceTypeInsp != null)
            traceTypeInsp.resetLater();

        // Update SpacingInsp.Visible
        boolean isPointsOrTags = trace.isShowPoints() || trace.isShowTags();
        boolean isPointsOrTagsInsp = _currentInsp == _pointStyleInsp || _currentInsp == _tagStyleInsp;
        boolean isShowSpacing = isPointsOrTags && isPointsOrTagsInsp;
        _spacingInsp.getUI().setVisible(isShowSpacing);
        if (isShowSpacing)
            _spacingInsp.resetLater();

        // Update TraceStyleInsp
        ChartPartInsp traceStyleInsp = getTraceStyleInspForTrace();
        setTraceStyleInsp(traceStyleInsp);
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
            DataUnit thetaUnit = (DataUnit) anEvent.getSelItem();
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
            Trace[] traces = trace.getContent().getTraces();
            for (Trace trc : traces)
                trc.setStacked(anEvent.getBoolValue());
        }

        // Handle ShowLegendEntryCheckBox
        if (anEvent.equals("ShowLegendEntryCheckBox"))
            trace.setShowLegendEntry(anEvent.getBoolValue());

        // Handle LineStyleButton, AreaStyleButton, PointsStyleButton, TagsStyleButton
        if (anEvent.equals("LineStyleButton")) {
            setCurrentInspector(_lineStyleInsp);
            if (anEvent.getParentEvent() != null && anEvent.getParentEvent().getClickCount() == 2)
                trace.setShowLine(!trace.isShowLine());
        }
        if (anEvent.equals("AreaStyleButton")) {
            setCurrentInspector(_areaStyleInsp);
            if (anEvent.getParentEvent() != null && anEvent.getParentEvent().getClickCount() == 2)
                trace.setShowArea(!trace.isShowArea());
        }
        if (anEvent.equals("PointsStyleButton")) {
            setCurrentInspector(_pointStyleInsp);
            if (anEvent.getParentEvent() != null && anEvent.getParentEvent().getClickCount() == 2)
                trace.setShowPoints(!trace.isShowPoints());
        }
        if (anEvent.equals("TagsStyleButton")) {
            setCurrentInspector(_tagStyleInsp);
            if (anEvent.getParentEvent() != null && anEvent.getParentEvent().getClickCount() == 2)
                trace.setShowTags(!trace.isShowTags());
        }
    }

    /**
     * Returns the current inspector.
     */
    private ChartPartInsp getCurrentInspector()  { return _currentInsp; }

    /**
     * Sets the current inspector.
     */
    private void setCurrentInspector(ChartPartInsp anInsp)
    {
        // If already set, just return
        if (anInsp == getCurrentInspector()) return;

        // If old, remove it
        if (_currentInsp != null)
            _inspBox.removeChild(_currentInsp.getUI());

        // Set new
        _currentInsp = anInsp;

        // If new, add UI
        if(_currentInsp != null)
            _inspBox.addChild(_currentInsp.getUI());
    }

    /**
     * Returns the TraceStyle inspector.
     */
    private ChartPartInsp getTraceStyleInsp()  { return _traceStyleInsp; }

    /**
     * Sets the TraceStyle inspector.
     */
    private void setTraceStyleInsp(ChartPartInsp anInsp)
    {
        // If already set, just return
        if (anInsp == getTraceStyleInsp()) return;

        // If old, remove it
        if (_traceStyleInsp != null)
            _traceStyleInspBox.removeChild(_traceStyleInsp.getUI());

        // Set new
        _traceStyleInsp = anInsp;

        // If new, add UI
        if(_traceStyleInsp != null)
            _traceStyleInspBox.addChild(_traceStyleInsp.getUI());

        // Update TraceStyleInspSep.Visible, TraceStyleInspLabelBox.Visible, TraceStyleInspLabel.Text
        setViewVisible("TraceStyleInspSep", _traceStyleInsp != null);
        setViewVisible("TraceStyleInspLabelBox", _traceStyleInsp != null);
        if (_traceStyleInsp != null)
            setViewText("TraceStyleInspLabel", _traceStyleInsp.getName());
    }

    /**
     * Returns the TraceStyleInsp for Trace.
     */
    private ChartPartInsp getTraceStyleInspForTrace()
    {
        // Get TraceType
        Trace trace = getTrace();
        TraceType traceType = trace != null ? trace.getType() : null;
        if (traceType == null)
            traceType = TraceType.Scatter;

        // Return TraceInsp for TraceType
        String traceTypeName = traceType.getName();
        switch (traceTypeName) {
            case "Polar": return getPolarTraceInsp();
            case "Contour": return getContourTraceInsp();
            case "PolarContour": return getContourTraceInsp();
            default: return null;
        }
    }

    /**
     * Returns the PolarTraceInsp.
     */
    private PolarTraceInsp getPolarTraceInsp()
    {
        if (_polarTraceInsp != null) return _polarTraceInsp;
        PolarTraceInsp insp = new PolarTraceInsp(getChartPane());
        return _polarTraceInsp = insp;
    }

    /**
     * Returns the ContourTraceInsp.
     */
    private ContourTraceInsp getContourTraceInsp()
    {
        if (_contourTraceInsp != null) return _contourTraceInsp;
        ContourTraceInsp insp = new ContourTraceInsp(getChartPane());
        return _contourTraceInsp = insp;
    }

    /**
     * Override to initialize Line/Area/Points inspector and SpacingInsp.
     */
    @Override
    public void setSelected(boolean aValue)
    {
        if (aValue == isSelected()) return;

        // If first call, initialize Line/Area/Point inspector to primary trace painting
        Trace trace = getTrace();
        if (_currentInsp == null && trace != null) {
            if (trace.isShowLine())
                setCurrentInspector(_lineStyleInsp);
            else if (trace.isShowArea())
                setCurrentInspector(_areaStyleInsp);
            else if (trace.isShowPoints())
                setCurrentInspector(_pointStyleInsp);
        }

        // Update SpacingInsp.Visible
        boolean isPointsOrTags = trace != null && (trace.isShowPoints() || trace.isShowTags());
        boolean isPointsOrTagsInsp = _currentInsp == _pointStyleInsp || _currentInsp == _tagStyleInsp;
        boolean isShowSpacing = isPointsOrTags && isPointsOrTagsInsp;
        _spacingInsp.getUI().setVisible(isShowSpacing);

        // Update TraceStyleInsp
        ChartPartInsp traceStyleInsp = getTraceStyleInspForTrace();
        setTraceStyleInsp(traceStyleInsp);
        setViewVisible("TraceStyleInspSep", _traceStyleInsp != null);
        setViewVisible("TraceStyleInspLabelBox", _traceStyleInsp != null);

        // Do normal version
        super.setSelected(aValue);
    }

    /**
     * Sets button to be highlighted for given button name and boolean.
     */
    private void setButtonHighlight(String aName, boolean aValue)
    {
        ButtonBase view = getView(aName, ButtonBase.class);
        Color color = (Color) view.getLabel().getTextColor();
        Color color2 = aValue ? Color.BLACK : Color.DARKGRAY;
        if (color != color2)
            view.getLabel().setTextColor(color2);
    }
}