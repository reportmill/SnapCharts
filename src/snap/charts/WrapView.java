package snap.charts;
import snap.geom.Insets;
import snap.geom.Point;
import snap.geom.Pos;
import snap.geom.Rect;
import snap.view.*;

/**
 * A parent view to corrently layout a content view even if rotated and/or scaled.
 */
public class WrapView extends ParentView {
    
    // The content
    View      _content;

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
    if(_content!=null) removeChild(_content);
    _content = aView;
    if(_content!=null) addChild(_content);
}
    
/**
 * Calculates the preferred width.
 */
protected double getPrefWidthImpl(double aH)
{
    Insets ins = getInsetsAll(); if(_content==null) return ins.getWidth();
    double w = _content.getPrefWidth(), h = _content.getPrefHeight();
    Rect bnds = _content.localToParent(new Rect(0,0,w,h)).getBounds();
    return bnds.width + ins.getWidth();
}

/**
 * Calculates the preferred height.
 */
protected double getPrefHeightImpl(double aW)
{
    Insets ins = getInsetsAll(); if(_content==null) return ins.getHeight();
    double w = _content.getPrefWidth(), h = _content.getPrefHeight();
    Rect bnds = _content.localToParent(new Rect(0,0,w,h)).getBounds();
    return bnds.height + ins.getHeight();
}

/**
 * Actual method to layout children.
 */
protected void layoutImpl()
{
    // Get size of parent and content and set
    double pw = getWidth(), ph = getHeight(); if(_content==null) return;
    double cw = _content.getPrefWidth(), ch = _content.getPrefHeight();
    _content.setBounds(0, 0, cw, ch);
    
    // Get size of content in parent
    Rect bnds = _content.localToParent(new Rect(0,0,cw,ch)).getBounds();
    
    // Get location of content center based on parent align/insets and parent/content sizes
    Pos align = getAlign(); Insets ins = getInsetsAll();
    double ax = ViewUtils.getAlignX(align.getHPos()), ay = ViewUtils.getAlignY(align.getVPos());
    double x = ins.left + (pw - ins.getWidth() - bnds.width)*ax + bnds.width/2;
    double y = ins.top + (ph - ins.getHeight() - bnds.height)*ay + bnds.height/2;
    
    // Get center point in content coords, translate to content origin and set content XY local
    Point pnt = _content.parentToLocal(x,y);
    pnt.x -= cw/2; pnt.y -= ch/2; pnt.x +=  + _content.getTransX(); pnt.y += _content.getTransY();
    _content.setXYLocal(pnt.x, pnt.y);
}

/**
 * Override to align to middle.
 */
public Pos getDefaultAlign()  { return Pos.CENTER; }

}