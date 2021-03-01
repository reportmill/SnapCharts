package snapcharts.views;
import snap.geom.Insets;
import snap.geom.Point;
import snap.geom.Pos;
import snap.geom.Rect;
import snap.view.*;

/**
 * A parent view to correctly layout a content view even if rotated and/or scaled.
 */
public class WrapView extends ParentView {
    
    // The content
    private View  _content;

    /**
     * Create WrapView for given view.
     */
    public WrapView(View aView)
    {
        _content = aView;
        addChild(aView);
    }

    /**
     * Returns the content.
     */
    public View getContent()  { return _content; }

    /**
     * Sets the content.
     */
    public void setContent(View aView)
    {
        if (_content!=null) removeChild(_content);
        _content = aView;
        if (_content!=null) addChild(_content);
    }

    /**
     * Calculates the preferred width.
     */
    protected double getPrefWidthImpl(double aH)
    {
        Insets ins = getInsetsAll();
        if (_content==null) return ins.getWidth();
        double childW = _content.getPrefWidth();
        double childH = _content.getPrefHeight();
        Rect areaBnds = _content.localToParent(new Rect(0,0, childW, childH)).getBounds();
        double areaW = Math.ceil(areaBnds.width);
        return areaW + ins.getWidth();
    }

    /**
     * Calculates the preferred height.
     */
    protected double getPrefHeightImpl(double aW)
    {
        Insets ins = getInsetsAll();
        if (_content==null) return ins.getHeight();
        double childW = _content.getPrefWidth();
        double childH = _content.getPrefHeight();
        Rect areaBnds = _content.localToParent(new Rect(0,0, childW, childH)).getBounds();
        double areaH = Math.ceil(areaBnds.width);
        return areaH + ins.getHeight();
    }

    /**
     * Actual method to layout children.
     */
    protected void layoutImpl()
    {
        // Get size of parent and content and set
        if (_content==null) return;
        double viewW = getWidth();
        double viewH = getHeight();
        double childW = Math.min(_content.getPrefWidth(), Math.max(viewW, viewH)); // This is me being stupid/lazy
        double childH = _content.getPrefHeight();
        _content.setBounds(0, 0, childW, childH);

        // Get size of content in parent
        Rect bnds = _content.localToParent(new Rect(0,0, childW, childH)).getBounds();

        // Get location of content center based on parent align/insets and parent/content sizes
        Pos align = getAlign();
        Insets ins = getInsetsAll();
        double alignX = ViewUtils.getAlignX(align.getHPos());
        double alignY = ViewUtils.getAlignY(align.getVPos());
        double childX = ins.left + (viewW - ins.getWidth() - bnds.width)*alignX + bnds.width/2;
        double childY = ins.top + (viewH - ins.getHeight() - bnds.height)*alignY + bnds.height/2;

        // Get center point in content coords, translate to content origin and set content XY local
        Point pnt = _content.parentToLocal(childX,childY);
        pnt.x += _content.getTransX() - childW/2;
        pnt.y += _content.getTransY() - childH/2;
        _content.setXYLocal(pnt.x, pnt.y);
    }

    /**
     * Override to align to middle.
     */
    public Pos getDefaultAlign()  { return Pos.CENTER; }
}