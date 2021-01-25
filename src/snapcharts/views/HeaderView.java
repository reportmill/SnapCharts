package snapcharts.views;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.view.ColView;
import snap.view.ScaleBox;
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

    // The ScaleBox in case header doesn't fit
    private ScaleBox  _scaleBox;

    // The ColView to hold children in ScaleBox
    private ColView  _colView;

    // Constants
    private final Font DEFAULT_TITLE_FONT = Header.DEFAULT_TITLE_FONT;
    private final Font DEFAULT_SUBTITLE_FONT = Font.Arial12;
    private final Color DEFAULT_SUBTITLE_COLOR = Color.GRAY;

    /**
     * Constructor.
     */
    public HeaderView()
    {
        // Basic config
        setAlign(Pos.CENTER);
        setSpacing(8);
        setMargin(0, 0, 8, 0);
        setClipToBounds(true);

        // Create ScaleBox, ColView
        _colView = new ColView();
        _colView.setAlign(Pos.TOP_CENTER);
        _colView.setSpacing(2);
        _scaleBox = new ScaleBox(_colView);
        _scaleBox.setKeepAspect(true);
        _scaleBox.setAlign(Pos.BOTTOM_CENTER);
        addChild(_scaleBox);

        // Create configure TitleView
        _titleView = new StringView();
        _titleView.setFont(DEFAULT_TITLE_FONT);
        _colView.addChild(_titleView);

        // Create configure SubtitleView
        _subtitleView = new StringView();
        _subtitleView.setFont(DEFAULT_SUBTITLE_FONT);
        _subtitleView.setTextFill(DEFAULT_SUBTITLE_COLOR);
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

        // Reset Subtitle
        String subtitle = header.getSubtitle();
        _subtitleView.setText(subtitle);
        _subtitleView.setVisible(subtitle != null && subtitle.length() > 0);
    }

    /**
     * Override to use ColView layout.
     */
    @Override
    protected double getPrefWidthImpl(double aH)
    {
        //return ColView.getPrefWidth(this, aH);
        return _scaleBox.getPrefWidth(aH);
    }

    /**
     * Override to use ColView layout.
     */
    @Override
    protected double getPrefHeightImpl(double aW)
    {
        //return ColView.getPrefHeight(this, aW);
        return _scaleBox.getPrefHeight(aW);
    }

    /**
     * Override to use ColView layout.
     */
    @Override
    protected void layoutImpl()
    {
        //ColView.layout(this, false);
        Insets ins = getInsetsAll();
        double areaX = ins.left;
        double areaY = ins.top;
        double areaW = getWidth() - ins.getWidth();
        double areaH = getHeight() - ins.getHeight();
        _scaleBox.setBounds(areaX, areaY, areaW, areaH);
    }
}
