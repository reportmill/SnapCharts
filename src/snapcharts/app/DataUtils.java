package snapcharts.app;

import snap.view.Clipboard;

import java.util.Arrays;

/**
 * Helper class for SheetView copy paste.
 */
class DataUtils {

    /**
     * Paste.
     */
    public static String[][] getClipboardCellData()
    {
        Clipboard cb = Clipboard.get();

        if (cb.hasString()) {
            String str = cb.getString();

            String cells[][] = getCellData(str);

            System.out.println("Read lines of data: " + cells.length);
            for (int i=0; i<cells.length; i++) {
                System.out.println(i + ". " + Arrays.toString(cells[i]));
            }

            return cells;
        }

        return null;
    }

    /**
     * Returns cell data for string.
     */
    public static String[][] getCellData(String aString)
    {
        String lines[] = aString.split("\\s*(\n|\r\n)\\s*");
        String cells[][] = new String[lines.length][];
        int lineCount = 0;

        for (String line : lines) {
            String fields[] = line.split("\\s*\t\\s*");
            cells[lineCount++] = fields;
        }

        if (lineCount!=lines.length)
            cells = Arrays.copyOf(cells, lineCount);
        return cells;
    }
}
