package snapcharts.view;
import snap.view.ColViewProxy;
import snap.view.StringView;
import snapcharts.charts.Chart;
import snapcharts.charts.Header;

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

        // Reset Title
        String title = header.getTitle();
        _titleView.setText(title);
        _titleView.setVisible(title != null && title.length() > 0);
        _titleView.setFont(header.getFont());
        _titleView.setTextColor(header.getTextFill());

        // Reset Subtitle
        String subtitle = header.getSubtitle();
        _subtitleView.setText(subtitle);
        _subtitleView.setVisible(subtitle != null && subtitle.length() > 0);
        _subtitleView.setFont(header.getSubtitleFont());
        _subtitleView.setTextColor(header.getTextFill());
    }

    /**
     * Override to use ColView layout.
     */
    @Override
    protected double getPrefWidthImpl(double aH)
    {
        ColViewProxy<?> viewProxy = getViewProxy();
        return viewProxy.getPrefWidth(aH);
    }

    /**
     * Override to use ColView layout.
     */
    @Override
    protected double getPrefHeightImpl(double aW)
    {
        ColViewProxy<?> viewProxy = getViewProxy();
        return viewProxy.getPrefHeight(aW);
    }

    /**
     * Override to use ColView layout.
     */
    @Override
    protected void layoutImpl()
    {
        ColViewProxy<?> viewProxy = getViewProxy();
        viewProxy.layoutView();
    }

    /**
     * Returns the ViewProxy to layout this HeaderView.
     */
    protected ColViewProxy<?> getViewProxy()
    {
        ColViewProxy<?> viewProxy = new ColViewProxy<>(this);
        return viewProxy;
    }
}
