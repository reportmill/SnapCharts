package snapcharts.view;
import snap.view.ColView;
import snap.view.StringView;
import snap.view.ViewProxy;
import snapcharts.model.Chart;
import snapcharts.model.Header;

/**
 * A view to hold header elements at top of ChartView.
 */
public class HeaderView extends ChartPartView<Header> {

    // The title
    private StringView  _titleView;

    // The subtitle
    private StringView  _subtitleView;

    /**
     * Constructor.
     */
    public HeaderView()
    {
        // Create configure TitleView
        _titleView = new StringView();
        _titleView.setShrinkToFit(true);
        addChild(_titleView);

        // Create configure SubtitleView
        _subtitleView = new StringView();
        _subtitleView.setShrinkToFit(true);
        addChild(_subtitleView);
    }

    /**
     * Returns the ChartPart.
     */
    public Header getChartPart()  { return getChart().getHeader(); }

    /**
     * Called to reset view from Chart.
     */
    protected void resetView()
    {
        // Do normal version
        super.resetView();

        // Get info
        Chart chart = getChart();
        Header header = chart.getHeader();

        // Reset Align, Margin, Padding
        setAlign(header.getAlign());
        setMargin(header.getMargin());
        setPadding(header.getPadding());
        setSpacing(header.getSpacing());

        // Reset Title
        String title = header.getTitle();
        _titleView.setText(title);
        _titleView.setVisible(title != null && title.length() > 0);
        _titleView.setFont(header.getFont());
        _titleView.setTextFill(header.getTextFill());

        // Reset Subtitle
        String subtitle = header.getSubtitle();
        _subtitleView.setText(subtitle);
        _subtitleView.setVisible(subtitle != null && subtitle.length() > 0);
        _subtitleView.setFont(header.getSubtitleFont());
        _subtitleView.setTextFill(header.getTextFill());
    }

    /**
     * Override to use ColView layout.
     */
    @Override
    protected double getPrefWidthImpl(double aH)
    {
        ViewProxy<?> viewProxy = getViewProxy();
        return ColView.getPrefWidthProxy(viewProxy, aH);
    }

    /**
     * Override to use ColView layout.
     */
    @Override
    protected double getPrefHeightImpl(double aW)
    {
        ViewProxy<?> viewProxy = getViewProxy();
        return ColView.getPrefHeightProxy(viewProxy, aW);
    }

    /**
     * Override to use ColView layout.
     */
    @Override
    protected void layoutImpl()
    {
        ViewProxy<?> viewProxy = getViewProxy();
        ColView.layoutProxy(viewProxy);
        viewProxy.setBoundsInClient();
    }

    /**
     * Returns the ViewProxy to layout this HeaderView.
     */
    protected ViewProxy<?> getViewProxy()
    {
        ViewProxy<?> viewProxy = new ViewProxy<>(this);
        return viewProxy;
    }
}
