package snapcharts.util;

import java.util.Arrays;

/**
 * IntArray.
 */
public class IntArray {

    // The items
    public int[]  items;

    // The number of items
    public int  size;

    /**
     * Constructor.
     */
    public IntArray()
    {
        int aSize = 16;
        items = new int[aSize];
        size = aSize;
    }

    /**
     * Add.
     */
    public void add(int aValue)
    {
        ensureCapacity(size + 1);
        items[size++] = aValue;
    }

    /**
     * Remove.
     */
    public void removeIndex(int anIndex)
    {
        System.arraycopy(items, anIndex+1, items, anIndex, size - anIndex - 1);
        size--;
    }

    /**
     * Ensure capacity.
     */
    public void ensureCapacity(int aSize)
    {
        if (aSize > items.length)
            items = Arrays.copyOf(items, aSize);
    }

    /**
     * Clear.
     */
    public void clear()  { size = 0; }
}
