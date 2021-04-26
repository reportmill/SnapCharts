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

    // The shape
    private Shape  _shape;

    // The shared symbols
    private static Symbol[] _symbols;

    // Total number of symbols
    public static final int SYMBOL_COUNT = 5;

    // Named symbols
    public static final String CIRCLE = "Circle";
    public static final String DIAMOND = "Diamond";
    public static final String SQUARE = "Square";
    public static final String TRIANGLE_UP = "Triangle-Up";
    public static final String TRIANGLE_DOWN = "Triangle-DOWN";
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
     * Returns the shape.
     */
    public Shape getShape()
    {
        // If already set, just return
        if (_shape != null) return _shape;

        // Set/return
        return _shape = getShapeForId(_id);
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
            case 1: return DIAMOND;
            case 2: return SQUARE;
            case 3: return TRIANGLE_UP;
            case 4: return TRIANGLE_DOWN;
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

            // DIAMOND
            case 1: return new Polygon(4, 0, 8, 4, 4, 8, 0, 4);

            // SQUARE
            case 2: return new Rect(0, 0, 8, 8);

            // TRIANGLE_UP
            case 3: return new Polygon(4, 0, 8, 8, 0, 8);

            // TRIANGLE_DOWN
            case 4: return new Polygon(0, 0, 8, 0, 4, 8);

            // Default
            default:
                System.err.println("Symbol.getShapeForId: Invalid Id: " + anId);
                return getShapeForId(0);
        }
    }
}
