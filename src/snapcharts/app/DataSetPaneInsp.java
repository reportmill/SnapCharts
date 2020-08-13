package snapcharts.app;
import snap.gfx.Color;
import snap.gfx.Image;
import snap.util.PropChangeListener;
import snap.view.*;
import snap.viewx.TextPane;
import snapcharts.apptools.DataSetBasicTool;

/**
 * A class to manage the inspector for ChartSetPane.
 */
public class DataSetPaneInsp extends ViewOwner {

    // The DataSetPane
    protected DataSetPane _epane;

    // The Title label
    private Label  _titleLabel;

    // The ScrollView that holds UI for child inspectors
    private ScrollView _inspScroll;

    // The ColView that holds UI for child inspectors
    private ColView  _inspColView;

    // The DataSetBasicTool
    private DataSetBasicTool  _dsetBasic;

    /**
     * Constructor.
     */
    public DataSetPaneInsp(DataSetPane anDP)
    {
        _epane = anDP;
    }

    /**
     * Returns the DataSetPane.
     */
    public DataSetPane getDataSetPane()  { return _epane; }

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

        // Get ChartBasicTool
        _dsetBasic = new DataSetBasicTool(_epane);
        _inspColView.addChild(_dsetBasic.getUI());
        Collapser.createCollapserAndLabel(_dsetBasic.getUI(), "Basic Properties");
        //cpsr.setCollapsed(true);
        //_inspColView.addPropChangeListener(pc -> { if (_inspColView.isShowing()) cpsr.setExpandedAnimated(true); });
    }

    /**
     * Refreshes the inspector for the current editor selection.
     */
    public void resetUI()
    {
        // Get editor (and just return if null) and tool for selected shapes
        DataSetPane dpane = getDataSetPane();

        // Get inspector title from owner and set
        String title = "Inspector";
        _titleLabel.setText(title);

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