/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import java.text.DecimalFormat;
import snap.geom.*;
import snap.gfx.*;
import snap.util.*;
import snapcharts.model.*;
import snapcharts.view.ChartHelper;
import snapcharts.view.ChartView;
import snapcharts.view.DataArea;
import snapcharts.view.DataView;

/**
 * A DataArea subclass to display the contents of pie chart.
 */
public class PieDataArea extends DataArea {
    
    // The cached wedges
    private Wedge[]  _wedges;
    
    // The pie center point
    private double  _pieX, _pieY;
    
    // The pie radius and diameter
    private double  _pieR, _pieD;
    
    // The last MouseMove point
    //private Point _lastMouseMovePoint;

    // Vars for animating SelDataPoint change
    private TracePoint _selPointLast;
    private double  _selPointMorph = 1;
    public boolean  _disableMorph;

    // The format
    private static DecimalFormat _fmt = new DecimalFormat("#.# %");

    // Constants
    private static final String SelDataPointMorph_Prop = "SelDataPointMorph";

    // Constants
    private static double LABEL_MARGIN = 30;
    private static double LABEL_PAD = 3;
    private static double PAD_TOP = 30, PAD_BOTTOM = 20, PAD_BOTTOM_MAX = 40;

    /**
     * Constructor.
     */
    public PieDataArea(ChartHelper aChartHelper, Trace aTrace)
    {
        super(aChartHelper, aTrace);
        setPadding(PAD_TOP, 10, PAD_BOTTOM, 10);
        setFont(Font.Arial12.getBold());
    }

    /**
     * Sets the DataView.
     */
    protected void setDataView(DataView aDataView)
    {
        super.setDataView(aDataView);
        getChartView().addPropChangeListener(pc -> selDataPointChanged(pc), ChartView.SelDataPoint_Prop);
    }

    /**
     * Returns the angles.
     */
    public double[] getAngles()
    {
        Trace trace = getTrace();
        int count = trace.getPointCount();

        // Get/set ratios, angles
        double[] ratios = getRatiosYtoTotalY(trace);
        double[] angles = new double[count];
        for (int i = 0; i < count; i++)
            angles[i] = Math.round(ratios[i] * 360);
        return angles;
    }

    /**
     * Returns the pie wedges.
     */
    protected Wedge[] getWedges()
    {
        // If wedges cached, just return
        if (_wedges!=null) return _wedges;

        // Get ratios and angles
        Trace trace = getTrace();
        double[] ratios = getRatiosYtoTotalY(trace);
        double[] angles = getAngles();

        // Get area bounds
        Insets ins = getInsetsAll();
        double areaX = ins.left;
        double areaY = ins.top;
        double areaW = getWidth() - ins.getWidth();
        double areaH = getHeight() - ins.getHeight();

        // Get chart size and insets and calculate pie radius, diameter and center x/y
        _pieD = areaH;
        _pieR = _pieD/2;
        _pieX = areaX + Math.round((areaW - _pieD)/2);
        _pieY = areaY + Math.round((areaH - _pieD)/2);

        // Iterate over angles and create/configure wedges
        Wedge wedges[] = new Wedge[angles.length];
        double start = 0;
        for (int i = 0; i < angles.length; i++) { double angle = angles[i];
            Wedge wedge = wedges[i] = new Wedge();
            wedge._start = start;
            wedge._angle = angle;
            String text = trace.getString(i);
            if (text!=null && text.length()>0)
                wedge._text = text + ": " + _fmt.format(ratios[i]);
            start += angle;
        }

        // Set label points so that they don't obscure each other
        setLabelPoints(wedges);

        // Return wedges
        return _wedges = wedges;
    }

    /**
     * Clears wedges when something changes.
     */
    private void clearWedges()
    {
        _wedges = null;
    }

    /**
     * Paints chart.
     */
    @Override
    protected void paintDataArea(Painter aPntr)
    {
        // Get wedges and other paint info
        Wedge wedges[] = getWedges();
        int selIndex = getSelPointIndex();
        int targIndex = getTargPointIndex();
        int selIndexLast = getSelPointLastIndex();
        double reveal = getReveal();
        double selPointMorph = getSelDataPointMorph();
        TraceStyle traceStyle = getDataStyle();

        // Set font
        aPntr.setFont(getFont());
        aPntr.setStroke(Stroke.Stroke1);

        // Iterate over wedges and paint wedge
        for (int i=0; i<wedges.length; i++) {

            // Get loop wedge and color
            Wedge wedge = wedges[i];
            Color color = traceStyle.getColorMapColor(i);

            // If targeted, paint targ area
            if (i==targIndex && i!=selIndex) {
                Arc arc = wedge.getArc(false, true, false, reveal, selPointMorph);
                aPntr.setColor(color.blend(Color.CLEARWHITE, .55)); aPntr.fill(arc);
                color = color.blend(Color.WHITE, .15);
            }

            // Paint arc
            Arc arc = wedge.getArc(i==selIndex, false, i==selIndexLast, reveal, selPointMorph);
            aPntr.setColor(color);
            aPntr.fill(arc);

            // Paint connector and white border
            if (reveal>=1)
                aPntr.draw(wedge.getLabelLine());
            aPntr.setColor(Color.WHITE);
            aPntr.draw(arc);
        }

        // Iterate over wedges and paint wedge label
        if (reveal>=1) {
            for (Wedge wedge : wedges) {
                String text = wedge._text;
                if (text != null && text.length() > 0) {
                    Point pnt = wedge.getLabelPoint();
                    aPntr.setColor(Color.BLACK);
                    aPntr.drawString(text, pnt.x, pnt.y);
                }
            }
        }
    }

    /**
     * Returns the X/Y point for given angle/radius.
     */
    public Point polarDataToDisplay(double anAngle, double aRadius)
    {
        double angRad = Math.toRadians(anAngle);
        double dispX = Math.round(getMidX() + aRadius*Math.cos(angRad));
        double dispY = Math.round(getMidY() + aRadius*Math.sin(angRad));
        return new Point(dispX, dispY);
    }

    /**
     * Returns the data point best associated with given x/y (null if none).
     * @return
     */
    public TracePoint getDataPointForLocalXY(double aX, double aY)
    {
        // Iterate over wedges and return point for wedge that contains given x/y
        Trace trace = getTrace();
        Wedge wedges[] = getWedges();
        for (int i=0; i<wedges.length; i++) { Wedge wedge = wedges[i];
            Arc arc = wedge.getArc();
            if (arc.contains(aX, aY))
                return trace.getPoint(i);
        }

        // Return null since no wedge contains point
        return null;
    }

    /**
     * Returns the given data point X/Y in this view coords.
     * @param aDP
     */
    @Override
    public Point getLocalXYForDataPoint(TracePoint aDP)
    {
        int ind = aDP.getIndex();
        Wedge wedges[] = getWedges();
        Wedge wedge = wedges[ind];
        double angle = wedge.getAngleMid();
        double radius = _pieR*3/4;
        return polarDataToDisplay(angle, radius);
    }

    /**
     * Called when a ChartPart changes.
     */
    @Override
    protected void chartPartDidChange(PropChange aPC)
    {
        // Do normal version
        super.chartPartDidChange(aPC);

        // Handle Data changes
        Object src = aPC.getSource();
        if (src instanceof Trace || src instanceof TraceList) {
            clearWedges();
        }
    }

    /**
     * Override to clear wedges.
     */
    @Override
    public void setWidth(double aValue)
    {
        if (aValue==getWidth()) return;
        super.setWidth(aValue);
        clearWedges();
    }

    /**
     * Override to clear wedges.
     */
    @Override
    public void setHeight(double aValue)
    {
        if (aValue==getHeight()) return;
        super.setHeight(aValue);
        clearWedges();
    }

    /**
     * Sets label points such that they don't overlap.
     */
    void setLabelPoints(Wedge wedges[])
    {
        // Set first and last wedge points
        if (wedges.length==0) return;
        wedges[0].getLabelPoint();
        wedges[wedges.length-1].getLabelPoint();

        // Set label points for pie right side
        for (int i=1; i<wedges.length; i++) {
            Wedge wedge = wedges[i], wedge2 = wedges[i-1];
            if (wedge.getAngleMid()>90) break;
            double angle = Math.max(wedge.getAngleMid(), wedge2._textAngle+2);
            wedge.getLabelPoint(angle);
            while (wedge.labelIntersects(wedge2))
                wedge.getLabelPoint(angle+=1);
        }

        // Set label points for pie left side
        for (int i=wedges.length-2; i>=0; i--) {
            Wedge wedge = wedges[i], wedge2 = wedges[i+1];
            if (wedge.getAngleMid()<90) break;
            double angle = Math.min(wedge.getAngleMid(), wedge2._textAngle-2);
            wedge.getLabelPoint(angle);
            while (wedge.labelIntersects(wedge2))
                wedge.getLabelPoint(angle-=1);
        }
    }

    /**
     * Changes padding to have an extra 20 points on bottom if label needed there.
     * */
    public void fixPaddingForBottomLabelIfNeeded()
    {
        double angles[] = getAngles();
        double start = -90;
        boolean hasBottomWedge = false;
        Insets ins = getPadding();

        for (double a : angles) { double ang = start + a/2;
            if (ang>60 && ang<120) {
                hasBottomWedge = true; break; }
            start += a;
        }
        if (hasBottomWedge && ins.bottom!=PAD_BOTTOM_MAX)
            setPadding(PAD_TOP, 10, PAD_BOTTOM_MAX, 10);
        else if (!hasBottomWedge && ins.bottom!=PAD_BOTTOM)
            setPadding(PAD_TOP, 10, PAD_BOTTOM, 10);
    }

    /**
     * Returns/sets the measure (from 0 to 1) of change of newly set SelDataPoint.
     */
    double getSelDataPointMorph()  { return _selPointMorph; }

    /**
     * Sets the measure (from 0 to 1) of change of newly set SelDataPoint.
     */
    void setSelDataPointMorph(double aValue)
    {
        _selPointMorph = aValue;
        repaint();
    }

    /**
     * Called when ChartView.SelDataPoint changes.
     */
    void selDataPointChanged(PropChange aPC)
    {
        // If not showing, just return
        if (!isShowing() || _disableMorph) return;

        // Cache last point and configure SelDataPointMorph to change from 0 to 1 over time
        _selPointLast = (TracePoint) aPC.getOldValue();
        setSelDataPointMorph(0);
        getAnimCleared(400).setValue(SelDataPointMorph_Prop, 1).setLinear().setOnFinish(a -> _selPointLast = null).play();
    }

    /**
     * Convenience methods to return ChartView SelDataPoint.Index, SelDataPointLast.Index & TargDataPoint.Index.
     */
    int getSelPointIndex()
    {
        TracePoint dp = getChartView().getSelDataPoint();
        return dp != null ? dp.getIndex() : -1;
    }
    int getSelPointLastIndex()
    {
        return _selPointLast != null ? _selPointLast.getIndex() : -1;
    }
    int getTargPointIndex()
    {
        TracePoint dp = getChartView().getTargDataPoint();
        return dp != null ? dp.getIndex() : -1;
    }

    /**
     * Returns the value for given key.
     */
    public Object getPropValue(String aPropName)
    {
        if (aPropName.equals(SelDataPointMorph_Prop))
            return getSelDataPointMorph();
        return super.getPropValue(aPropName);
    }

    /**
     * Sets the value for given key.
     */
    public void setPropValue(String aPropName, Object aValue)
    {
        if (aPropName.equals(SelDataPointMorph_Prop))
            setSelDataPointMorph(SnapUtils.doubleValue(aValue));
        else super.setPropValue(aPropName, aValue);
    }

    /**
     * Returns an array of trace ratios.
     */
    private static double[] getRatiosYtoTotalY(Trace aTrace)
    {
        double total = getTotalY(aTrace);
        int count = aTrace.getPointCount();
        double ratios[] = new double[count];
        for (int i=0;i<count;i++) ratios[i] = aTrace.getY(i)/total;
        return ratios;
    }

    /**
     * Returns the total of all values.
     */
    private static double getTotalY(Trace aTrace)
    {
        double total = 0;
        int count = aTrace.getPointCount();
        for (int i=0; i<count; i++)
            total += aTrace.getY(i);
        return total;
    }

    /**
     * A class to hold cached wedge data.
     */
    protected class Wedge {

        // The start and sweep angles
        double _start, _angle, _textAngle;

        // Label text
        String _text;

        // Cached Arc and label point
        Arc    _arc; Point  _textPoint;
        double _tw, _th;
        Shape _labelLine;

        /** Returns the basic arc. */
        public Arc getArc()
        {
            if (_arc!=null) return _arc;
            return _arc = new Arc(_pieX, _pieY, _pieD, _pieD, getAngleStart(), _angle);
        }

        /** Returns the arc with given reveal or selection status. */
        public Arc getArc(boolean isSel, boolean isTarg, boolean isSelLast, double aReveal, double selMorph)
        {
            // If no reveal or selection, return normal arc
            if (aReveal>=1 && !isSel && !isTarg && !isSelLast)
                return getArc();

            // Get arc start/sweep angles, x/y points and diameter
            double start = -90 + _start*aReveal, angle = _angle*aReveal;
            double px = _pieX, py = _pieY, diam = _pieD;

            // If targeted, increase diam by 20
            if (isTarg) {
                diam += 20;
                px -= 10;
                py -= 10;
            }

            // If selected, move x/y by 10 points from center of wedge
            if (isSel) {
                double ang2 = Math.toRadians(start + angle/2);
                px += 10*selMorph*Math.cos(ang2);
                py += 10*selMorph*Math.sin(ang2);
            }

            // If selected, move x/y by 10 points from center of wedge
            else if (isSelLast) {
                double ang2 = Math.toRadians(start + angle/2);
                px += 10*(1-selMorph)*Math.cos(ang2);
                py += 10*(1-selMorph)*Math.sin(ang2);
            }

            // If reveal, modify diameter and move to new center
            if (aReveal<1) {
                diam *= aReveal;
                px += _pieR*(1-aReveal);
                py += _pieR*(1-aReveal);
            }

            // Create arc and return
            return new Arc(px, py, diam, diam, start, angle);
        }

        /** Returns the mid angle. */
        public double getAngleStart()  { return -90 + _start; }
        public double getAngleMid()  { return getAngleStart() + _angle/2; }

        /** Returns the label point. */
        public Point getLabelPoint()
        {
            if (_textPoint!=null) return _textPoint;
            return _textPoint = getLabelPoint(getAngleMid());
        }

        /** Returns the label point for specific angle. */
        public Point getLabelPoint(double anAngle)
        {
            _textAngle = anAngle;
            double radius = _pieR + LABEL_MARGIN;
            double angRad = Math.toRadians(anAngle);
            Font font = getFont();
            double px = _pieX + _pieR + radius*Math.cos(angRad);
            double py = _pieY + _pieR + radius*Math.sin(angRad) + font.getAscent();
            Rect bnds = _text!=null ? font.getStringBounds(_text) : new Rect();
            _tw = bnds.width;
            _th = bnds.height;
            if (anAngle>90) px -= bnds.width;
            px = Math.round(px);
            py = Math.round(py);
            return _textPoint = new Point(px, py);
        }

        /** Returns the X/Y point for given angle/radius. */
        public Point polarDataToDisplay(double anAngle, double aRadius)
        {
            double angRad = Math.toRadians(anAngle);
            double dispX = Math.round(getMidX() + aRadius*Math.cos(angRad));
            double dispY = Math.round(getMidY() + aRadius*Math.sin(angRad));
            return new Point(dispX, dispY);
        }

        /** Returns whether label rect intersects another wedge label rect. */
        public boolean labelIntersects(Wedge aWedge)
        {
            Font font = getFont();
            Rect bnds0 = new Rect(_textPoint.x, _textPoint.y - font.getAscent(), _tw, _th);
            Rect bnds1 = new Rect(aWedge._textPoint.x, aWedge._textPoint.y - font.getAscent(), aWedge._tw, aWedge._th);
            bnds0.inset(-LABEL_PAD);
            bnds1.inset(-LABEL_PAD);
            return bnds0.intersects(bnds1);
        }

        /** Returns the connector line. */
        public Shape getLabelLine()
        {
            if (_labelLine!=null) return _labelLine;
            double angRad = Math.toRadians(getAngleMid());
            double p0x = _pieX + _pieR + (_pieR-5)*Math.cos(angRad);
            double p0y = _pieY + _pieR + (_pieR-5)*Math.sin(angRad);
            double cp0x = _pieX + _pieR + (_pieR+20)*Math.cos(angRad);
            double cp0y = _pieY + _pieR + (_pieR+20)*Math.sin(angRad);
            double p1x = _textPoint.x; if (getAngleMid()>90) p1x += _tw + 4; else p1x -= 4;
            double p1y = _textPoint.y - getFont().getAscent() + _th/2;
            double cp1x = p1x; if (getAngleMid()>90) cp1x += 10; else cp1x -=10;
            double cp1y = p1y;
            return _labelLine = new Cubic(p0x, p0y, cp0x, cp0y, cp1x, cp1y, p1x, p1y);
        }
    }
}