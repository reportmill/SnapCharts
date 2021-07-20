package snapcharts.view;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.view.ColView;
import snap.view.StringView;
import snapcharts.model.Chart;
import snapcharts.model.Header;

/**
 * A view to hold header elements at top of ChartView.
 */
public class HeaderView<T extends Header> extends ChartPartView<T> {

    // The title
    private StringView  _titleView;

    // The subtitle
    private StringView  _subtitleView;

    // The ColView to hold children in ScaleBox
    private ColView  _colView;

    /**
     * Constructor.
     */
    public HeaderView()
    {
        // Basic config
        setAlign(Pos.CENTER);
        setSpacing(8);
        setMargin(0, 0, 8, 0);

        // Create ColView to hold TitleView, SubtitleView
        _colView = new ColView();
        _colView.setAlign(Pos.BOTTOM_CENTER);
        _colView.setSpacing(2);
        addChild(_colView);

        // Create configure TitleView
        _titleView = new StringView();
        _titleView.setShrinkToFit(true);
        _colView.addChild(_titleView);

        // Create configure SubtitleView
        _subtitleView = new StringView();
        _subtitleView.setShrinkToFit(true);
        _colView.addChild(_subtitleView);
    }

    /**
     * Returns the ChartPart.
     */
    public T getChartPart()  { return (T) getChart().getHeader(); }

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
        Insets ins = getInsetsAll();
        double prefW = _colView.getPrefWidth(aH);
        return prefW + ins.getWidth();
    }

    /**
     * Override to use ColView layout.
     */
    @Override
    protected double getPrefHeightImpl(double aW)
    {
        Insets ins = getInsetsAll();
        double prefH = _colView.getPrefHeight(aW);
        return prefH + ins.getHeight();
    }

    /**
     * Override to use ColView layout.
     */
    @Override
    protected void layoutImpl()
    {
        Insets ins = getInsetsAll();
        double areaX = ins.left;
        double areaY = ins.top;
        double areaW = getWidth() - ins.getWidth();
        double areaH = getHeight() - ins.getHeight();
        _colView.setBounds(areaX, areaY, areaW, areaH);
    }
}
