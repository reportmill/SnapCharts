package snapcharts.view;
import snap.geom.Point;
import snap.geom.Pos;
import snap.geom.Rect;
import snap.gfx.Border;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.gfx.Painter;
import snap.text.StringBox;
import snap.util.FormatUtils;
import snapcharts.model.DataStyle;
import snapcharts.model.DataStore;
import java.util.ArrayList;
import java.util.List;

/**
 * A class to handle painting DataArea.DataSet data tags.
 */
public class TagPainter {

    // The DataArea
    private DataArea  _dataArea;

    // The Cached TagBoxes
    private TagBox[]  _tagBoxes;

    // Constant for offset from display point
    private int TAG_OFFSET = 10;

    /**
     * Constructor.
     */
    public TagPainter(DataArea aDataArea)
    {
        _dataArea = aDataArea;
    }

    /**
     * Returns the array of tag boxes.
     */
    public TagBox[] getTagBoxes()
    {
        // If already set, just return
        if (_tagBoxes != null) return _tagBoxes;

        // Get info
        DataStyle dataStyle = _dataArea.getDataStyle();
        Font font = dataStyle.getTagFont();

        // Get position
        Pos tagPos = Pos.TOP_CENTER;
        double symbolSize = dataStyle.getSymbolSize();
        double tagOffset = TAG_OFFSET + Math.round(symbolSize / 2);

        // Declare LastBox var for checks
        List<StringBox> tagBoxList = new ArrayList<>();

        // Get info
        DataStore procData = _dataArea.getProcessedData();
        boolean hasZ = procData.getDataType().hasZ();
        Color tagBorderColor = dataStyle.getTagBorderColor();
        int tagBorderWidth = dataStyle.getTagBorderWidth();
        Border tagBorder = tagBorderWidth > 0 ? Border.createLineBorder(tagBorderColor, tagBorderWidth) : null;

        // Get DispData and start/end index for current visible range
        DataStore dispData = _dataArea.getDispData();
        int startIndex = _dataArea.getDispDataStartIndex();
        int endIndex = _dataArea.getDispDataEndIndex();

        // Get VisPointCount and MaxPointCount
        int visPointCount = endIndex - startIndex + 1;
        int maxPointCount = dataStyle.getMaxPointCount();

        // Get point increment (as real number, so we can round to point index for distribution)
        double incrementReal = 1;
        if (maxPointCount == 1)
            startIndex = endIndex;
        if (maxPointCount > 1 && maxPointCount < visPointCount)
            incrementReal = (visPointCount - 1) / (maxPointCount - 1d);

        // Loop variables for point index (rounded) and point index (real)
        int index = startIndex;
        double indexReal = startIndex;
        Rect dataBounds = _dataArea.getBoundsLocal();

        // Iterate over point indexes by incrementReal (round to get nearest index)
        while (index <= endIndex) {

            // Get StringBox for string, X/Y and TagPos
            double val = hasZ ? procData.getZ(index) : procData.getY(index);
            String valStr = FormatUtils.formatNum(val);
            double disX = dispData.getX(index);
            double disY = dispData.getY(index);

            // Create/add TagBox
            if (dataBounds.contains(disX, disY)) {
                StringBox strBox = getTagStringBox(valStr, font, tagBorder, disX, disY, tagPos, tagOffset);
                tagBoxList.add(strBox);
            }

            // Calculate next index
            if (incrementReal > 1) {
                indexReal += incrementReal;
                index = (int) Math.round(indexReal);
            }
            else index++;
        }

        // Paint boxes last, so they will be above all data-point lines/dots
        TagBox[] tagBoxes = tagBoxList.toArray(new TagBox[0]);
        return _tagBoxes = tagBoxes;
    }

    /**
     * Paints the data tags.
     */
    public void paintTags(Painter aPntr)
    {
        TagBox[] tagBoxes = getTagBoxes();
        DataStyle dataStyle = _dataArea.getDataStyle();
        Color fillColor = dataStyle.getTagColor();

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
}
