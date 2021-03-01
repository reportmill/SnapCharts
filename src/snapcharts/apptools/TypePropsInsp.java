package snapcharts.apptools;
import snap.view.ColView;
import snap.view.Label;
import snap.view.ViewEvent;
import snapcharts.app.ChartPane;
import snapcharts.model.Chart;
import snapcharts.model.ChartPart;
import snapcharts.model.ChartType;
import snapcharts.model.ChartTypeProps;

/**
 * A class to manage UI to edit a ChartTypeProps.
 */
public class TypePropsInsp extends ChartPartInsp {

    // The View that holds the child insp
    private ColView  _inspBox;

    // The Current ChartPartInsp
    private ChartPartInsp  _currentInsp;

    // The ContourPropsInsp
    private ContourPropsInsp  _contourPropsInsp;

    /**
     * Constructor.
     */
    public TypePropsInsp(ChartPane aChartPane)
    {
        super(aChartPane);
    }

    /**
     * Returns the name.
     */
    @Override
    public String getName()
    {
        if (!_chartPane.isUISet()) return "ChartType Settings";
        Chart chart = getChart();
        ChartType chartType = chart.getType();
        return chartType.getStringPlain() + " Settings";
    }

    /**
     * Returns the ChartPart.
     */
    @Override
    public ChartPart getChartPart()  { return getChart().getTypeProps(); }

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
     * Returns the ChartPartInsp for chart type.
     */
    private ChartPartInsp getChartPropsInsp()
    {
        ChartType chartType = getChart().getType();
        switch (chartType) {
            case CONTOUR: return getContourPropsInsp();
            default: return null;
        }
    }

    /**
     * Returns the ContourPropsInsp.
     */
    private ContourPropsInsp getContourPropsInsp()
    {
        if (_contourPropsInsp != null) return _contourPropsInsp;
        ContourPropsInsp insp = new ContourPropsInsp(getChartPane());
        return _contourPropsInsp = insp;
    }

    @Override
    protected void initUI()
    {
        _inspBox = getUI(ColView.class);
    }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Get TypeProps
        ChartTypeProps typeProps = getChart().getTypeProps();

        ChartPartInsp chartTypeInsp = getChartPropsInsp();
        setCurrentInspector(chartTypeInsp);

        if (chartTypeInsp != null)
            chartTypeInsp.resetLater();

        // Update title
        String title = chartTypeInsp != null ? chartTypeInsp.getName() : getName();
        Label label = getCollapser().getLabel();
        label.setText(title);
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get TypeProps
        ChartTypeProps typeProps = getChart().getTypeProps();

        // Handle TitleText, SubtitleText
        //if(anEvent.equals("TitleText")) header.setTitle(anEvent.getStringValue());
        //if(anEvent.equals("SubtitleText")) header.setSubtitle(anEvent.getStringValue());
    }
}