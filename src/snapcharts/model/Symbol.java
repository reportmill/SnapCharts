package snapcharts.model;
import snap.geom.*;

/**
 * A class to represent a Symbol.
 */
public class Symbol {

    // The id
    private int  _id;

    // The name
    private String  _name;

    // The size
    private int  _size = 8;

    // The shape
    private Shape  _shape;

    // The shared symbols
    private static Symbol[] _symbols;

    // Total number of symbols
    public static final int SYMBOL_COUNT = 10;

    // Named symbols
    public static final String CIRCLE = "Circle";
    public static final String SQUARE = "Square";
    public static final String DIAMOND = "Diamond";
    public static final String TRIANGLE_UP = "Triangle-Up";
    public static final String TRIANGLE_DOWN = "Triangle-Down";
    public static final String TRIANGLE_LEFT = "Triangle-Left";
    public static final String TRIANGLE_RIGHT = "Triangle-Right";
    public static final String CROSS = "Cross";
    public static final String X = "X";
    public static final String STAR = "Star";
    public static final String UNKNOWN = "Unknown";

    /**
     * Creates a new Symbol.
     */
    private Symbol(int anId)
    {
        _id = anId;
        _name = getNameForId(anId);
    }

    /**
     * Returns the Id.
     */
    public int getId()  { return _id; }

    /**
     * Returns the name.
     */
    public String getName()  { return _name; }

    /**
     * Returns the size.
     */
    public int getSize()  { return _size; }

    /**
     * Returns the shape.
     */
    public Shape getShape()
    {
        // If already set, just return
        if (_shape != null) return _shape;

        // Set/return
        Shape shape = getShapeForId(_id);
        shape = shape.copyForBounds(0, 0, _size, _size);

        // Set/return
        return _shape = shape;
    }

    /**
     * Returns the symbol for given size.
     */
    public Symbol copyForSize(int aSize)
    {
        Symbol copy = new Symbol(_id);
        copy._size = aSize;
        return copy;
    }

    /**
     * Returns the shared symbol for given id.
     */
    public static Symbol getSymbolForId(int anId)
    {
        Symbol[] symbols = getSymbols();
        return symbols[anId];
    }

    /**
     * Returns the shared symbol for given id.
     */
    public static Symbol getSymbolForIndex(int anIndex)
    {
        int id = anIndex % SYMBOL_COUNT;
        return getSymbolForId(id);
    }

    /**
     * Returns the shared symbols.
     */
    public static Symbol[] getSymbols()
    {
        // If already set, just return
        if (_symbols != null) return _symbols;

        // Create, configure
        Symbol[] symbols = new Symbol[SYMBOL_COUNT];
        for (int i=0; i<SYMBOL_COUNT; i++)
            symbols[i] = new Symbol(i);

        // Set/return
        return _symbols = symbols;
    }

    /**
     * Returns the name.
     */
    public static String getNameForId(int anId)
    {
        switch (anId) {
            case 0: return CIRCLE;
            case 1: return SQUARE;
            case 2: return DIAMOND;
            case 3: return TRIANGLE_UP;
            case 4: return TRIANGLE_DOWN;
            case 5: return TRIANGLE_LEFT;
            case 6: return TRIANGLE_RIGHT;
            case 7: return CROSS;
            case 8: return X;
            case 9: return STAR;
            default: System.err.println("Symbol.getNameForId: Invalid Id: " + anId); return UNKNOWN;
        }
    }

    /**
     * Returns the symbol shape at index.
     */
    public static Shape getShapeForId(int anId)
    {
        // Create, set, return
        switch (anId) {

            // CIRCLE
            case 0: return new Ellipse(0, 0, 8, 8);

            // SQUARE
            case 1: return new Rect(0, 0, 8, 8);

            // DIAMOND
            case 2: return new Polygon(4, 0, 8, 4, 4, 8, 0, 4);

            // TRIANGLE_UP
            case 3: return new Polygon(4, 0, 8, 8, 0, 8);

            // TRIANGLE_DOWN
            case 4: return new Polygon(0, 0, 8, 0, 4, 8);

            // TRIANGLE_LEFT
            case 5: return new Polygon(0, 4, 8, 0, 8, 8);

            // TRIANGLE_RIGHT
            case 6: return new Polygon(0, 0, 8, 4, 0, 8);

            // CROSS
            case 7: return Path.getPathFromSVG("M 40 0 L 80 0 L 80 40 L 120 40 L 120 80 L 80 80 L 80 120 L 40 120 L 40 80 L 0 80 L 0 40 L 40 40 Z");

            // X
            case 8: return Path.getPathFromSVG("M 0 40 L 40 0 L 80 40 L 120 0 L 160 40 L 120 80 L 160 120 L 120 160 L 80 120 L 40 160 L 0 120 L 40 80 Z");

            // Star
            case 9: return Path.getPathFromSVG("M 0 120 L 100 120 L 140 0 L 180 120 L 280 120 L 200 180 L 220 280 L 140 220 L 60 280 L 80 180 Z");

            // Default
            default:
                System.err.println("Symbol.getShapeForId: Invalid Id: " + anId);
                return getShapeForId(0);
        }
    }
}
