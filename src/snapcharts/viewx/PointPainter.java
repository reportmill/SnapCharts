/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.geom.*;
import snap.gfx.*;
import snap.text.NumberFormat;
import snap.text.StringBox;
import snap.util.FormatUtils;
import snapcharts.data.DataSet;
import snapcharts.charts.*;
import snapcharts.view.TraceView;
import java.util.ArrayList;
import java.util.List;

/**
 * A class to handle painting TraceView.Trace data symbols and tags.
 */
public class PointPainter {

    // The TraceView
    private TraceView  _traceView;

    // The Cached Symbol points
    private Point[]  _symbolPoints;

    // The Cached TagBoxes
    private TagBox[]  _tagBoxes;

    // Constant for offset from display point
    private int TAG_OFFSET = 10;

    /**
     * Constructor.
     */
    public PointPainter(TraceView aTraceView)
    {
        _traceView = aTraceView;
    }

    /**
     * Returns the array of Symbol points.
     */
    public Point[] getSymbolPoints()  { return _symbolPoints; }

    /**
     * Returns the array of tag boxes.
     */
    public TagBox[] getTagBoxes()  { return _tagBoxes; }

    /**
     * Caches Symbol points and Tag boxes..
     */
    public void paintSymbolsAndTagsPrep()
    {
        // Get ShowSymbol info
        Trace trace = _traceView.getTrace();
        boolean showPoints = trace.isShowPoints();
        PointStyle pointStyle = trace.getPointStyle();
        int symbolSize = pointStyle.getSymbolSize();
        double symbolShift = symbolSize / 2d;
        List<Point> symbolPointList = new ArrayList<>();

        // Get ShowTag info because TagBoxes are created
        boolean showTags = trace.isShowTags();
        TagStyle tagStyle = trace.getTagStyle();
        DataSet procData = _traceView.getTrace().getProcessedData();
        boolean hasZ = procData.getDataType().hasZ();
        Color tagBorderColor = tagStyle.getLineColor();
        double tagBorderWidth = tagStyle.getLineWidth();
        Border tagBorder = tagBorderWidth > 0 ? Border.createLineBorder(tagBorderColor, tagBorderWidth) : null;
        Font tagFont = tagStyle.getFont();
        NumberFormat tagFormat = NumberFormat.getFormat(tagStyle.getTextFormat());
        Pos tagPos = Pos.TOP_CENTER;
        double tagOffset = TAG_OFFSET + Math.round(symbolSize / 2);
        Rect contentBounds = _traceView.getBoundsLocal();
        List<StringBox> tagBoxList = new ArrayList<>();

        // Get DispData and start/end index for current visible range
        DataSet dispData = _traceView.getDisplayData();
        int startIndex = _traceView.getDispDataStartIndex();
        int endIndex = _traceView.getDispDataEndIndex();

        // Get VisPointCount and MaxPointCount
        int visPointCount = endIndex - startIndex + 1;
        int maxPointCount = pointStyle.getMaxPointCount();
        int skipPointCount = pointStyle.getSkipPointCount();
        int pointSpacing = pointStyle.getPointSpacing();

        // Get point increment (as real number, so we can round to point index for distribution)
        double incrementReal = 1;
        if (maxPointCount == 1)
            startIndex = endIndex;
        if (maxPointCount > 1 && maxPointCount < visPointCount)
            incrementReal = (visPointCount - 1) / (maxPointCount - 1d);

        // If SkipPointCount produces larger increment, reset increment (and round startIndex down to increment so points don't jump)
        if (skipPointCount + 1 > incrementReal) {
            incrementReal = skipPointCount + 1;
            while (startIndex > 0 && startIndex % (int) incrementReal != 0)
                startIndex--;
        }

        // Loop variables for point index (rounded) and point index (real)
        int index = startIndex;
        double indexReal = startIndex;
        double lastDispX = 0;
        double lastDispY = 0;

        // Iterate over point indexes by incrementReal (round to get nearest index)
        while (index <= endIndex) {

            // Get DispX/Y coords
            double dispX = dispData.getX(index);
            double dispY = dispData.getY(index);

            // If ShowTags, create TagBox
            if (showTags) {

                // Get StringBox for string, X/Y and TagPos
                double val = hasZ ? procData.getZ(index) : procData.getY(index);
                String valStr;
                if (tagFormat != null)
                    valStr = tagFormat.format(val);
                else valStr = FormatUtils.formatNum(val);

                // Create/add TagBox
                if (contentBounds.contains(dispX, dispY)) {
                    StringBox strBox = getTagStringBox(valStr, tagFont, tagBorder, dispX, dispY, tagPos, tagOffset);
                    if (pointSpacing > 0 && tagBoxList.size() > 0) {
                        StringBox lastBox = tagBoxList.get(tagBoxList.size() - 1);
                        if (intersectsCircleBounds(lastBox, strBox, pointSpacing)) {
                            index++;
                            indexReal = index;
                            continue;
                        }
                    }
                    tagBoxList.add(strBox);
                }
            }

            // If not ShowTags and Spacing is
            else if (pointSpacing > 0 && index > startIndex) {
                double distBetween = Point.getDistance(lastDispX, lastDispY, dispX, dispY);
                if (distBetween < symbolSize + pointSpacing) {
                    index++;
                    indexReal = index;
                    continue;
                }
                lastDispX = dispX;
                lastDispY = dispY;
            }

            // Get disp X/Y of symbol origin add to SymbolPointList
            if (showPoints) {
                double symbX = dispX - symbolShift;
                double symbY = dispY - symbolShift;
                symbolPointList.add(new Point(symbX, symbY));
            }

            // Calculate next index
            if (incrementReal > 1) {
                indexReal += incrementReal;
                index = (int) Math.round(indexReal);
            }
            else index++;
        }

        // Reset SymbolPoints, TagBoxes
        _symbolPoints = symbolPointList.toArray(new Point[0]);
        _tagBoxes = tagBoxList.toArray(new TagBox[0]);
    }

    /**
     * Paints symbols.
     */
    public void paintSymbols(Painter aPntr)
    {
        // Get info
        Trace trace = _traceView.getTrace();
        PointStyle pointStyle = trace.getPointStyle();
        Symbol symbol = pointStyle.getSymbol();
        Color symbolColor = pointStyle.getFillColor();  //color.darker().darker()
        Shape symbolShape = symbol.getShape();

        // Get Symbol border info
        Color symbolBorderColor = pointStyle.getLineColor();
        double symbolBorderWidth = pointStyle.getLineWidth();

        // Get whether showing points only
        boolean pointsOnly = !(trace.isShowLine() || trace.isShowArea());
        if (symbolBorderWidth == 0 && pointsOnly)
            symbolBorderWidth = 1;

        // Get SymbolBorderStroke
        Stroke symbolBorderStroke = symbolBorderWidth > 0 ? Stroke.getStroke(symbolBorderWidth) : null;

        // Iterate over SymbolPoints and paint
        Point[] symbolPoints = getSymbolPoints();
        for (Point symbolPoint : symbolPoints) {

            // Get disp X/Y of symbol origin and translate there
            double symbX = symbolPoint.x;
            double symbY = symbolPoint.y;
            aPntr.translate(symbX, symbY);

            // Set color and fill symbol shape
            aPntr.setColor(symbolColor);
            aPntr.fill(symbolShape);

            // If only points, also stroke outline of shape
            if (symbolBorderStroke != null) {
                aPntr.setStroke(symbolBorderStroke);
                aPntr.setColor(symbolBorderColor);
                aPntr.draw(symbolShape);
            }

            // Translate back
            aPntr.translate(-symbX, -symbY);
        }
    }

    /**
     * Paints the data tags.
     */
    public void paintTags(Painter aPntr)
    {
        TagBox[] tagBoxes = getTagBoxes();
        Trace trace = _traceView.getTrace();
        TagStyle tagStyle = trace.getTagStyle();
        Color fillColor = tagStyle.getFillColor();

        for (TagBox sbox : tagBoxes)
        {
            if (fillColor != null) {
                aPntr.setColor(fillColor);
                aPntr.fill(sbox);
            }
            sbox.paint(aPntr);
        }
    }

    /**
     * Returns a StringBox for String, data point location (in display coords) and offset direction (as position).
     */
    private static StringBox getTagStringBox(String aStr, Font aFont, Border aBorder, double aX, double aY, Pos aPos, double aDist)
    {
        TagBox sbox = new TagBox(aStr);
        sbox.setFont(aFont);
        sbox.setBorder(aBorder);
        sbox.setPadding(3, 5, 3, 5);
        sbox.setCenteredXY(aX, aY);
        sbox.offsetInDirection(aPos, aDist);
        sbox.offsetFromCenterToBoundsPosition(aPos);
        return sbox;
    }

    /**
     * A StringBox subclass.
     */
    private static class TagBox extends StringBox {

        /**
         * Constructor.
         */
        public TagBox(String aStr)
        {
            super(aStr);
        }

        /**
         * Offsets the box in given direction (position) by given distance (in display points).
         * Box is also be offset by half box size (depending on direction).
         */
        public void offsetInDirection(Pos aPos, double aDist)
        {
            Point offset = getOffsetForDistance(aPos, aDist);
            offset(Math.round(offset.x), Math.round(offset.y));
        }

        /**
         * Offsets the box in given direction (position) by given distance (in display points).
         * Box is also be offset by half box size (depending on direction).
         */
        public void offsetFromCenterToBoundsPosition(Pos aPos)
        {
            Point offset = getOffsetFromCenterOfRectSize(aPos, getWidth(), getHeight());
            offset(Math.round(offset.x), Math.round(offset.y));
        }
    }

    /**
     * Returns the offset of given distance in the direction of this position.
     */
    private static Point getOffsetForDistance(Pos aPos, double aDist)
    {
        if (aPos == Pos.CENTER)
            return new Point(0,0);
        double angle = getAngleRad(aPos);
        double dx = Math.cos(angle) * aDist;
        double dy = Math.sin(angle) * aDist;
        return new Point(dx, dy);
    }

    /**
     * Returns the point for this position in given rect.
     */
    private static Point getPointForRect(Pos aPos, double aX, double aY, double aW, double aH)
    {
        double x = aX + aW * aPos.getHPos().doubleValue();
        double y = aY + aH * aPos.getVPos().doubleValue();
        return new Point(x, y);
    }

    /**
     * Returns the offset (as point) from rect center to this position for given rect size.
     */
    private static Point getOffsetFromCenterOfRectSize(Pos aPos, double aW, double aH)
    {
        double dx = aW * aPos.getHPos().doubleValue() - aW / 2;
        double dy = aH * aPos.getVPos().doubleValue() - aH / 2;
        return new Point(dx, dy);
    }

    /**
     * Returns the distance to rect position from center for given rect.
     */
    private static double getDistanceFromCenterOfRectSize(Pos aPos, double aW, double aH)
    {
        Point pnt = getPointForRect(aPos,0, 0, aW, aH);
        return pnt.getDistance(aW/2, aH/2);
    }

    /**
     * Returns the angle for position in degrees.
     */
    private static double getAngleDeg(Pos aPos)
    {
        switch (aPos)
        {
            case CENTER_RIGHT: return 0;
            case BOTTOM_RIGHT: return 45;
            case BOTTOM_CENTER: return 90;
            case BOTTOM_LEFT: return 135;
            case CENTER_LEFT: return 180;
            case TOP_LEFT: return 225;
            case TOP_CENTER: return 270;
            case TOP_RIGHT: return 315;
            default: return 0;
        }
    }

    /**
     * Returns the angle for position in radians.
     */
    private static double getAngleRad(Pos aPos)  { return Math.toRadians(getAngleDeg(aPos)); }

    /**
     * Returns the distance between two rects.
     */
    private static double getDistanceBetween(RectBase aRect1, RectBase aRect2)
    {
        if (aRect1.intersectsShape(aRect2))
            return 0;
        double dx1 = aRect2.x - aRect1.getMaxX();
        double dx2 = aRect1.x - aRect2.getMaxX();
        double dy1 = aRect2.y - aRect1.getMaxY();
        double dy2 = aRect1.y - aRect2.getMaxY();
        double dx = Math.max(dx1, dx2);
        double dy = Math.max(dy1, dy2);
        double dist = dx < 0 ? dy : dy < 0 ? dx : Math.min(dx, dy);
        return dist;
    }

    /**
     * Returns given rects overlap bounds circles with given extra spacing.
     */
    public boolean intersectsCircleBounds(RectBase box1, RectBase box2, int extraSpacing)
    {
        double radiusOfBoth = box1.width / 2 + box2.width / 2 + extraSpacing;
        double dist = Point.getDistance(box1.getMidX(), box1.getMidY(), box2.getMidX(), box2.getMidY());
        return dist < radiusOfBoth;
    }
}
