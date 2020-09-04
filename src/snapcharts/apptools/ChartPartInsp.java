package snapcharts.apptools;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.gfx.Border;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.view.Label;
import snap.view.View;
import snap.view.ViewHost;
import snap.view.ViewOwner;
import snapcharts.app.ChartPane;
import snapcharts.app.Collapser;
import snapcharts.model.Chart;
import snapcharts.model.ChartPart;

/**
 * A ViewOwner subclass for ChartPart inspectors.
 */
public abstract class ChartPartInsp extends ViewOwner {

    // The ChartPane
    protected ChartPane _chartPane;

    // The Label
    private Label  _label;

    // The Collapser
    protected Collapser  _collapser;

    // Whether inspector is selected
    private boolean  _selected;

    // Constants
    private static Color LABEL_FILL = new Color("#e0e0e4");
    private static Color LABEL_FILL_SEL = new Color("#e0e6f0");
    private static Border LABEL_BORDER_SEL = Border.createLineBorder(LABEL_FILL_SEL.darker(), 1).copyForInsets(Insets.EMPTY);

    /**
     * Constructor.
     */
    public ChartPartInsp(ChartPane aChartPane)
    {
        _chartPane = aChartPane;
    }

    /**
     * Returns the name.
     */
    public abstract String getName();

    /**
     * Returns the ChartPane.
     */
    public ChartPane getChartPane()  { return _chartPane; }

    /**
     * Returns the Chart.
     */
    public Chart getChart()  { return _chartPane.getChart(); }

    /**
     * Returns the ChartPart.
     */
    public abstract ChartPart getChartPart();

    /**
     * Returns the Collapser.
     */
    public Collapser getCollapser()
    {
        if (_collapser!=null) return _collapser;

        // Get/add label
        Label label = getLabel();
        View view = getUI();
        ViewHost host = view.getHost();
        int index = view.indexInHost();
        host.addGuest(label, index);

        // Add collaper and label
        //_collapser = Collapser.createCollapserAndLabel(getUI(), getName());
        _collapser = new Collapser(getUI(), label);
        return _collapser;
    }

    /**
     * Returns whether inspector is selected.
     */
    public boolean isSelected()  { return _selected; }

    /**
     * Sets whether inspector is selected.
     */
    public void setSelected(boolean aValue)
    {
        if (aValue==isSelected()) return;
        _selected = aValue;

        Collapser collapser = getCollapser();
        if (aValue && !collapser.isExpanded())
            collapser.setExpandedAnimated(true);
        if (!aValue && collapser.isExpanded())
            collapser.setCollapsedAnimated(true);

        getLabel().setFill(aValue ? LABEL_FILL_SEL : LABEL_FILL);
        getLabel().setBorder(aValue ? LABEL_BORDER_SEL : null);
    }

    /**
     * Returns the label.
     */
    public Label getLabel()
    {
        if (_label!=null) return _label;

        String text = getName();
        Label label = new Label(text);
        label.setName(text + "Label");
        label.setFill(LABEL_FILL);
        label.setFont(Font.Arial14);
        label.getStringView().setGrowWidth(true);
        label.setTextFill(Color.GRAY);
        label.setAlign(Pos.CENTER);
        label.setPadding(4,4,4,10);
        label.setMargin(4,8,4,8);
        label.setRadius(10);
        return _label = label;
    }

}
