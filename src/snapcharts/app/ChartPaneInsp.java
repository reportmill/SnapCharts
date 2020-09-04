package snapcharts.app;
import snap.gfx.Color;
import snap.gfx.Image;
import snap.view.*;
import snap.viewx.TextPane;
import snapcharts.apptools.*;
import snapcharts.model.*;

/**
 * A class to manage inspector.
 */
public class ChartPaneInsp extends ViewOwner {

    // The ChartPane
    protected ChartPane _chartPane;
    
    // The Title label
    private Label  _titleLabel;
    
    // The ScrollView that holds UI for child inspectors
    private ScrollView _inspScroll;
    
    // The ColView that holds UI for child inspectors
    private ColView  _inspColView;

    // The ChartInsp
    private ChartInsp _chartInsp;

    // The HeaderInsp
    private HeaderInsp _headerInsp;

    // The AxisInsp
    private AxisInsp  _axisXInsp;

    // The AxisInsp
    private AxisInsp  _axisYInsp;

    // The LegendInsp
    private LegendInsp  _legendInsp;

    // The DataSet Inspector
    private DataSetInsp _dsetInsp;

    // The array of ChartPartInsp
    private ChartPartInsp  _allInspectors[];

    /**
     * Constructor.
     */
    public ChartPaneInsp(ChartPane anEP)
    {
        _chartPane = anEP;
    }

    /**
     * Returns the ChartPane.
     */
    public ChartPane getEditorPane()  { return _chartPane; }

    /**
     * Initializes UI panel for the inspector.
     */
    public void initUI()
    {
        // Get/configure TitleLabel
        _titleLabel = getView("TitleLabel", Label.class);
        _titleLabel.setTextFill(Color.GRAY);

        // Get/configure SearchText: radius, prompt, image, animation
        TextField searchText = getView("SearchTextField", TextField.class);
        searchText.setPromptText("Search");
        searchText.getLabel().setImage(Image.get(TextPane.class, "Find.png"));
        TextField.setBackLabelAlignAnimatedOnFocused(searchText, true);
        //searchText.addEventFilter(e -> ViewUtils.runLater(() -> textFieldKeyTyped(e)), KeyPress);

        // Get/configure ContentBox
        _inspScroll = getView("ContentBox", ScrollView.class);
        _inspScroll.setBorder(null);
        _inspScroll.setBarSize(12);
        _inspScroll.setFillWidth(true);

        // Get InspColView
        _inspColView = getView("InspColView", ColView.class);

        // Only Add Chart stuff for ChartMode
        boolean chartMode = !_chartPane._dataSetMode;
        if (chartMode) {

            // Create/add ChartInsp
            _chartInsp = new ChartInsp(_chartPane);
            addInspector(_chartInsp, false);

            // Create/add HeaderInsp
            _headerInsp = new HeaderInsp(_chartPane);
            addInspector(_headerInsp, false);

            // Create/add AxisXInsp
            _axisXInsp = new AxisInsp(_chartPane, AxisType.X);
            addInspector(_axisXInsp, false);

            // Create/add AxisYInsp
            _axisYInsp = new AxisInsp(_chartPane, AxisType.Y);
            addInspector(_axisYInsp, false);

            // Create/add LegendInsp
            _legendInsp = new LegendInsp(_chartPane);
            addInspector(_legendInsp, false);
        }

        // Add DataSetInsp
        _dsetInsp = new DataSetInsp(_chartPane);
        addInspector(_dsetInsp, false);

        // Set all inspectors
        _allInspectors = new ChartPartInsp[] { _chartInsp, _headerInsp, _axisXInsp, _axisYInsp, _legendInsp, _dsetInsp };
        if (!chartMode)
            _allInspectors = new ChartPartInsp[] { _dsetInsp };
        runLater(() -> chartPaneSelChanged());
    }

    /**
     * Adds an inspector.
     */
    private void addInspector(ChartPartInsp aChartPartInsp, boolean isShowing)
    {
        // Get UI view and add to inspector
        View inspUI = aChartPartInsp.getUI();
        _inspColView.addChild(inspUI);

        // Trigger Collapser create
        Collapser collapser = aChartPartInsp.getCollapser();
        if (!isShowing)
            collapser.setCollapsed(true);

        // Add listener to update ChartPartInsp.Sel when label is clicked
        Label label = aChartPartInsp.getLabel();
        label.addEventFilter(e -> runLater(() -> chartPartInspLabelMousePress(aChartPartInsp)), MousePress);
    }

    /**
     * Refreshes the inspector for the current editor selection.
     */
    public void resetUI()
    {
        // Get editor (and just return if null) and tool for selected shapes
        ChartPane chartPane = getEditorPane();

        // Reset DataSetInsp
        boolean isDataSetSelected = chartPane.getDataSet()!=null;
        if (isDataSetSelected) {
            _dsetInsp.getUI().setVisible(true);
            _dsetInsp.resetLater();
        }
        else _dsetInsp.getUI().setVisible(false);
    }

    /**
     * Handles changes to the inspector UI controls.
     */
    public void respondUI(ViewEvent anEvent)  { }

    /**
     * Returns the inspector for given ChartPart.
     */
    public ChartPartInsp getChartPartInsp(ChartPart aChartPart)
    {
        if (aChartPart instanceof Header) return _headerInsp;
        if (aChartPart instanceof AxisX) return _axisXInsp;
        if (aChartPart instanceof AxisY) return _axisYInsp;
        if (aChartPart instanceof Legend) return _legendInsp;
        return _chartInsp;
    }

    /**
     * Called when label gets mouse press.
     */
    public void chartPartInspLabelMousePress(ChartPartInsp anInsp)
    {
        ChartPart chartPart = anInsp.getChartPart();
        _chartPane.getSel().setSelChartPart(chartPart);
    }

    /**
     * Called when the selection changes.
     */
    public void chartPaneSelChanged()
    {
        // Get SelPart and SelInsp
        ChartPart selPart = _chartPane.getSel().getSelChartPart();
        ChartPartInsp selPartInsp = getChartPartInsp(selPart);

        // Iterate over all ChartPaneInsp and make SelPartInsp is expanded (and others not)
        for (ChartPartInsp insp : _allInspectors)
            insp.setSelected(insp==selPartInsp);
    }
}