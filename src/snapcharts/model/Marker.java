package snapcharts.model;
import snap.geom.Pos;
import snap.gfx.Image;
import snap.props.PropSet;
import snap.util.SnapUtils;
import snap.util.XMLArchiver;
import snap.util.XMLElement;

import java.util.Objects;

/**
 * This ChartPart subclass is used to highlight or annotate an area on the chart.
 */
public class Marker extends ChartPart {

    // The name of marker
    private String  _name;

    // The marker location
    private double  _x, _y;

    // The marker size
    private double  _width, _height;

    // The Coordinate space of x coords
    private CoordSpace  _coordSpaceX;

    // The coordinate space of y coords
    private CoordSpace  _coordSpaceY;

    // Whether X coordinates are provided as a fraction of CoordSpace.Max - CoordSpace.Min
    private boolean  _fractionalX;

    // Whether X coordinates are provided as a fraction of CoordSpace.Max - CoordSpace.Min
    private boolean  _fractionalY;

    // The Text for the marker
    private String  _text;

    // Whether Text is positioned outside bounds X
    private boolean  _textOutsideX;

    // Whether Text is positioned outside bounds Y
    private boolean  _textOutsideY;

    // Whether to fit/wrap text inside marker bounds
    private boolean  _fitTextToBounds;

    // Whether to show text in axis (between tick labels and axis label)
    private boolean  _showTextInAxis;

    // Image to be shown for this marker
    private Image  _image;

    // SVG to be shown for this marker
    private String  _svg;

    // Constant for coordinate space
    public enum CoordSpace  {

        // The constants
        X, Y, Y2, Y3, Y4, DataView, ChartView;

        // Map to AxisType
        public AxisType getAxisType()  { return getAxisTypeForCoordSpace(this); }

        // Returns whether space is 'display' space (DataView or ChartView)
        public boolean isDisplaySpace()  { return this == DataView || this == ChartView; }
    }

    // Constants for properties
    public static final String Name_Prop = "Name";
    public static final String X_Prop = "X";
    public static final String Y_Prop = "Y";
    public static final String Width_Prop = "Width";
    public static final String Height_Prop = "Height";
    public static final String CoordSpaceX_Prop = "CoordSpaceX";
    public static final String CoordSpaceY_Prop = "CoordSpaceY";
    public static final String FractionalX_Prop = "FractionalX";
    public static final String FractionalY_Prop = "FractionalY";
    public static final String Text_Prop = "Text";
    public static final String TextOutsideX_Prop = "TextOutsideX";
    public static final String TextOutsideY_Prop = "TextOutsideY";
    public static final String FitTextToBounds_Prop = "FitTextToBounds";
    public static final String ShowTextInAxis_Prop = "ShowTextInAxis";
    public static final String Image_Prop = "Image";
    public static final String SVG_Prop = "SVG";

    // Constants for defaults
    private static final Pos DEFAULT_MARKER_ALIGN = Pos.CENTER;
    private static final CoordSpace  DEFAULT_COORD_SPACE = CoordSpace.DataView;

    /**
     * Constructor.
     */
    public Marker()
    {
        super();

        // Override default property values
        _align = DEFAULT_MARKER_ALIGN;

        // Set default values
        _coordSpaceX = DEFAULT_COORD_SPACE;
        _coordSpaceY = DEFAULT_COORD_SPACE;
    }

    /**
     * Returns the name of marker.
     */
    public String getName()  { return _name; }

    /**
     * Sets the name of marker.
     */
    public void setName(String aString)
    {
        if (Objects.equals(aString, _name)) return;
        firePropChange(Name_Prop, _name, _name = aString);
    }

    /**
     * Returns the X location of marker.
     */
    public double getX()  { return _x; }

    /**
     * Sets the X location of marker.
     */
    public void setX(double aValue)
    {
        if (aValue == _x) return;
        firePropChange(X_Prop, _x, _x = aValue);
    }

    /**
     * Returns the Y location of marker.
     */
    public double getY()  { return _y; }

    /**
     * Sets the Y location of marker.
     */
    public void setY(double aValue)
    {
        if (aValue == _y) return;
        firePropChange(Y_Prop, _y, _y = aValue);
    }

    /**
     * Returns the width of marker.
     */
    public double getWidth()  { return _width; }

    /**
     * Sets the width of marker.
     */
    public void setWidth(double aValue)
    {
        if (aValue == _width) return;
        firePropChange(Width_Prop, _width, _width = aValue);
    }

    /**
     * Returns the height of marker.
     */
    public double getHeight()  { return _height; }

    /**
     * Sets the height of marker.
     */
    public void setHeight(double aValue)
    {
        if (aValue == _height) return;
        firePropChange(Height_Prop, _height, _height = aValue);
    }

    /**
     * Sets the bounds.
     */
    public void setBounds(double aX, double aY, double aW, double aH)
    {
        setX(aX);
        setY(aY);
        setWidth(aW);
        setHeight(aH);
    }

    /**
     * Returns the CoordSpace of X/Width properties.
     */
    public CoordSpace getCoordSpaceX()  { return _coordSpaceX; }

    /**
     * Sets the CoordSpace of X/Width properties.
     */
    public void setCoordSpaceX(CoordSpace aCoordSpace)
    {
        if (aCoordSpace == getCoordSpaceX()) return;
        firePropChange(CoordSpaceX_Prop, _coordSpaceX, _coordSpaceX = aCoordSpace);
    }

    /**
     * Returns the CoordSpace of Y/Height properties.
     */
    public CoordSpace getCoordSpaceY()  { return _coordSpaceY; }

    /**
     * Sets the CoordSpace of Y/Height properties.
     */
    public void setCoordSpaceY(CoordSpace aCoordSpace)
    {
        if (aCoordSpace == getCoordSpaceY()) return;
        firePropChange(CoordSpaceY_Prop, _coordSpaceY, _coordSpaceY = aCoordSpace);
    }

    /**
     * Returns whether X/Width coordinates are provided as fractions of CoordSpace.Max - CoordSpace.Min.
     */
    public boolean isFractionalX()  { return _fractionalX; }

    /**
     * Sets whether X/Width coordinates are provided as fractions of CoordSpace.Max - CoordSpace.Min.
     */
    public void setFractionalX(boolean aValue)
    {
        if (aValue == isFractionalX()) return;
        firePropChange(FractionalX_Prop, _fractionalX, _fractionalX = aValue);
    }

    /**
     * Returns whether Y/Height coordinates are provided as fractions of CoordSpace.Max - CoordSpace.Min.
     */
    public boolean isFractionalY()  { return _fractionalY; }

    /**
     * Sets whether Y/Height coordinates are provided as fractions of CoordSpace.Max - CoordSpace.Min.
     */
    public void setFractionalY(boolean aValue)
    {
        if (aValue == isFractionalY()) return;
        firePropChange(FractionalY_Prop, _fractionalY, _fractionalY = aValue);
    }

    /**
     * Returns text to be shown for this marker.
     */
    public String getText()  { return _text; }

    /**
     * Sets text to be shown for this marker.
     */
    public void setText(String aString)
    {
        if (Objects.equals(aString, _text)) return;
        firePropChange(Text_Prop, _text, _text = aString);
    }

    /**
     * Returns whether text is positioned outside bounds X.
     */
    public boolean isTextOutsideX()  { return _textOutsideX; }

    /**
     * Sets whether text is positioned outside bounds X.
     */
    public void setTextOutsideX(boolean aValue)
    {
        if (aValue == _textOutsideX) return;
        firePropChange(TextOutsideX_Prop, _textOutsideX, _textOutsideX = aValue);
    }

    /**
     * Returns whether text is positioned outside bounds Y.
     */
    public boolean isTextOutsideY()  { return _textOutsideY; }

    /**
     * Sets whether text is positioned outside bounds Y.
     */
    public void setTextOutsideY(boolean aValue)
    {
        if (aValue == _textOutsideY) return;
        firePropChange(TextOutsideY_Prop, _textOutsideY, _textOutsideY = aValue);
    }

    /**
     * Returns whether to fit/wrap text inside marker bounds.
     */
    public boolean isFitTextToBounds()  { return _fitTextToBounds; }

    /**
     * Sets whether to fit/wrap text inside marker bounds.
     */
    public void setFitTextToBounds(boolean aValue)
    {
        if (aValue == _fitTextToBounds) return;
        firePropChange(FitTextToBounds_Prop, _fitTextToBounds, _fitTextToBounds = aValue);
    }

    /**
     * Returns whether to show text in axis (between tick labels and axis label).
     */
    public boolean isShowTextInAxis()  { return _showTextInAxis; }

    /**
     * Sets whether to show text in axis (between tick labels and axis label).
     */
    public void setShowTextInAxis(boolean aValue)
    {
        if (aValue == _showTextInAxis) return;
        firePropChange(ShowTextInAxis_Prop, _showTextInAxis, _showTextInAxis = aValue);
    }

    /**
     * Returns image to be shown for this marker.
     */
    public Image getImage()  { return _image; }

    /**
     * Sets text to be shown for this marker.
     */
    public void setImage(Image anImage)
    {
        if (anImage == _image) return;
        firePropChange(Image_Prop, _image, _image = anImage);
    }

    /**
     * Returns SVG to be shown for this marker.
     */
    public String getSVG()  { return _svg; }

    /**
     * Sets SVG to be shown for this marker.
     */
    public void setSVG(String aString)
    {
        if (Objects.equals(aString, _svg)) return;
        firePropChange(SVG_Prop, _svg, _svg = aString);
    }

    /**
     * Override so ChartPart will get/set Marker border from line properties.
     */
    @Override
    public boolean isBorderSupported()
    {
        return false;
    }

    /**
     * Override to register props.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // Add Props
        aPropSet.addProps(X_Prop, Y_Prop, Width_Prop, Height_Prop,
                CoordSpaceX_Prop, CoordSpaceY_Prop, FractionalX_Prop, FractionalY_Prop,
                Text_Prop, TextOutsideX_Prop, TextOutsideY_Prop, FitTextToBounds_Prop, ShowTextInAxis_Prop,
                SVG_Prop);
    }

    /**
     * Override for subclass properties.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        switch (aPropName) {

            // Name
            case Name_Prop: return getName();

            // X, Y, Width, Height
            case X_Prop: return getX();
            case Y_Prop: return getY();
            case Width_Prop: return getWidth();
            case Height_Prop: return getHeight();

            // CoordSpaceX, CoordSpaceY, FractionalX, FractionalY
            case CoordSpaceX_Prop: return getCoordSpaceX();
            case CoordSpaceY_Prop: return getCoordSpaceY();
            case FractionalX_Prop: return isFractionalX();
            case FractionalY_Prop: return isFractionalY();

            // Text, TextOutsideX, TextOutsideY, FitTextToBounds, ShowTextInAxis
            case Text_Prop: return getText();
            case TextOutsideX_Prop: return isTextOutsideX();
            case TextOutsideY_Prop: return isTextOutsideY();
            case FitTextToBounds_Prop: return isFitTextToBounds();
            case ShowTextInAxis_Prop: return isShowTextInAxis();

            // Image, SVG
            case Image_Prop: return getImage();
            case SVG_Prop: return getSVG();

            // Do normal version
            default: return super.getPropValue(aPropName);
        }
    }

    /**
     * Override for subclass properties.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        switch (aPropName) {

            // Name
            case Name_Prop: setName(SnapUtils.stringValue(aValue)); break;

            // X, Y, Width, Height
            case X_Prop: setX(SnapUtils.doubleValue(aValue)); break;
            case Y_Prop: setY(SnapUtils.doubleValue(aValue)); break;
            case Width_Prop: setWidth(SnapUtils.doubleValue(aValue)); break;
            case Height_Prop: setHeight(SnapUtils.doubleValue(aValue)); break;

            // CoordSpaceX, CoordSpaceY, FractionalX, FractionalY
            case CoordSpaceX_Prop: setCoordSpaceX((CoordSpace) aValue); break;
            case CoordSpaceY_Prop: setCoordSpaceY((CoordSpace) aValue); break;
            case FractionalX_Prop: setFractionalX(SnapUtils.boolValue(aValue)); break;
            case FractionalY_Prop: setFractionalY(SnapUtils.boolValue(aValue)); break;

            // Text, TextOutsideX, TextOutsideY, FitTextToBounds, ShowTextInAxis
            case Text_Prop: setText(SnapUtils.stringValue(aValue)); break;
            case TextOutsideX_Prop: setTextOutsideX(SnapUtils.boolValue(aValue)); break;
            case TextOutsideY_Prop: setTextOutsideY(SnapUtils.boolValue(aValue)); break;
            case FitTextToBounds_Prop: setFitTextToBounds(SnapUtils.boolValue(aValue)); break;
            case ShowTextInAxis_Prop: setShowTextInAxis(SnapUtils.boolValue(aValue)); break;

            // Image, SVG
            case Image_Prop: setImage((Image) aValue); break;
            case SVG_Prop: setSVG(SnapUtils.stringValue(aValue)); break;

            // Do normal version
            default: super.setPropValue(aPropName, aValue);
        }
    }

    /**
     * Override for subclass properties.
     */
    @Override
    public Object getPropDefault(String aPropName)
    {
        switch (aPropName) {

            // Align
            case Align_Prop: return DEFAULT_MARKER_ALIGN;

            // X, Y, Width, Height
            case X_Prop: return 0;
            case Y_Prop: return 0;
            case Width_Prop: return 0;
            case Height_Prop: return 0;

            // CoordSpaceX, CoordSpaceY, FractionalX, FractionalY
            case CoordSpaceX_Prop: return DEFAULT_COORD_SPACE;
            case CoordSpaceY_Prop: return DEFAULT_COORD_SPACE;
            case FractionalX_Prop: return false;
            case FractionalY_Prop: return false;

            // Text, TextOutsideX, TextOutsideY, FitTextToBounds, ShowTextInAxis
            case Text_Prop: return null;
            case TextOutsideX_Prop: return false;
            case TextOutsideY_Prop: return false;
            case FitTextToBounds_Prop: return false;
            case ShowTextInAxis_Prop: return false;

            // Image, SVG
            case Image_Prop: return null;
            case SVG_Prop: return null;

            // Do normal version
            default: return super.getPropDefault(aPropName);
        }
    }

    /**
     * Archival.
     */
    @Override
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Archive basic attributes
        XMLElement e = super.toXML(anArchiver);

        // Archive X, Y, Width, Height
        if (!isPropDefault(X_Prop))
            e.add(X_Prop, getX());
        if (!isPropDefault(Y_Prop))
            e.add(Y_Prop, getY());
        if (!isPropDefault(Width_Prop))
            e.add(Width_Prop, getWidth());
        if (!isPropDefault(Height_Prop))
            e.add(Height_Prop, getHeight());

        // Archive CoordSpaceX, CoordSpaceY, FractionalX, FractionalY
        if (!isPropDefault(CoordSpaceX_Prop))
            e.add(CoordSpaceX_Prop, getCoordSpaceX());
        if (!isPropDefault(CoordSpaceY_Prop))
            e.add(CoordSpaceY_Prop, getCoordSpaceY());
        if (!isPropDefault(FractionalX_Prop))
            e.add(FractionalX_Prop, isFractionalX());
        if (!isPropDefault(FractionalY_Prop))
            e.add(FractionalY_Prop, isFractionalY());

        // Archive Text, TextOutsideX, TextOutsideY, FitTextToBounds, ShowTextInAxis
        if (!isPropDefault(Text_Prop))
            e.add(Text_Prop, getText());
        if (!isPropDefault(TextOutsideX_Prop))
            e.add(TextOutsideX_Prop, isTextOutsideX());
        if (!isPropDefault(TextOutsideY_Prop))
            e.add(TextOutsideY_Prop, isTextOutsideY());
        if (!isPropDefault(FitTextToBounds_Prop))
            e.add(FitTextToBounds_Prop, isFitTextToBounds());
        if (!isPropDefault(ShowTextInAxis_Prop))
            e.add(ShowTextInAxis_Prop, isShowTextInAxis());

        // Archive Image, SVG
        if (!isPropDefault(Image_Prop)) {
            Image image = getImage();
            String imageName = image.getName();
            if (imageName == null)
                imageName = "MarkerImage_" + anArchiver.getResources().size();
            byte[] imageBytes = image.getBytes();
            e.add(Image_Prop, imageName);
            anArchiver.addResource(imageBytes, imageName);
        }
        if (!isPropDefault(SVG_Prop))
            e.add(SVG_Prop, getSVG());

        // Return element
        return e;
    }

    /**
     * Unarchival.
     */
    @Override
    public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive basic attributes
        super.fromXML(anArchiver, anElement);

        // Unarchive X, Y, Width, Height
        if (anElement.hasAttribute(X_Prop))
            setX(anElement.getAttributeDoubleValue(X_Prop));
        if (anElement.hasAttribute(Y_Prop))
            setY(anElement.getAttributeDoubleValue(Y_Prop));
        if (anElement.hasAttribute(Width_Prop))
            setWidth(anElement.getAttributeDoubleValue(Width_Prop));
        if (anElement.hasAttribute(Height_Prop))
            setHeight(anElement.getAttributeDoubleValue(Height_Prop));

        // Unarchive CoordSpaceX, CoordSpaceY, FractionalX, FractionalY
        if (anElement.hasAttribute(CoordSpaceX_Prop))
            setCoordSpaceX(anElement.getAttributeEnumValue(CoordSpaceX_Prop, CoordSpace.class, null));
        if (anElement.hasAttribute(CoordSpaceY_Prop))
            setCoordSpaceY(anElement.getAttributeEnumValue(CoordSpaceY_Prop, CoordSpace.class, null));
        if (anElement.hasAttribute(FractionalX_Prop))
            setFractionalX(anElement.getAttributeBoolValue(FractionalX_Prop));
        if (anElement.hasAttribute(FractionalY_Prop))
            setFractionalY(anElement.getAttributeBoolValue(FractionalY_Prop));

        // Unarchive Text, TextOutsideX, TextOutsideY, FitTextToBounds, ShowTextInAxis
        if (anElement.hasAttribute(Text_Prop))
            setText(anElement.getAttributeValue(Text_Prop));
        if (anElement.hasAttribute(TextOutsideX_Prop))
            setTextOutsideX(anElement.getAttributeBoolValue(TextOutsideX_Prop));
        if (anElement.hasAttribute(TextOutsideY_Prop))
            setTextOutsideY(anElement.getAttributeBoolValue(TextOutsideY_Prop));
        if (anElement.hasAttribute(FitTextToBounds_Prop))
            setFitTextToBounds(anElement.getAttributeBoolValue(FitTextToBounds_Prop));
        if (anElement.hasAttribute(ShowTextInAxis_Prop))
            setShowTextInAxis(anElement.getAttributeBoolValue(ShowTextInAxis_Prop));

        // Unarchive Image, SVG
        if (anElement.hasAttribute(Image_Prop)) {
            String imageName = anElement.getAttributeValue(Image_Prop);
            byte[] imageBytes = anArchiver.getResource(imageName);
            if (imageBytes != null) {
                Image image = Image.get(imageBytes);
                if (image != null)
                    setImage(image);
            }
        }
        if (anElement.hasAttribute(SVG_Prop))
            setSVG(anElement.getAttributeValue(SVG_Prop));

        // Return this part
        return this;
    }

    /**
     * Returns an AxisType for CoordSpace.
     */
    private static AxisType getAxisTypeForCoordSpace(CoordSpace aCoordSpace)
    {
        switch (aCoordSpace) {
            case X: return AxisType.X;
            case Y: return AxisType.Y;
            case Y2: return AxisType.Y2;
            case Y3: return AxisType.Y3;
            case Y4: return AxisType.Y4;
            default: return null;
        }
    }
}
