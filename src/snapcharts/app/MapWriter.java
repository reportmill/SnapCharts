package snapcharts.app;
import java.text.DecimalFormat;
import java.util.*;
import snap.gfx.Color;

/**
 * A class to write a Map to string.
 */
public class MapWriter {
    
    // The StringBuffer
    StringBuffer    _sb = new StringBuffer();

    // The current level of indent
    int             _indent = 0;
    
    // The indent string
    String          _istr = "    ";
    
    // Whether writing compact
    boolean         _compact;
    
    // A formatter
    static DecimalFormat _fmt = new DecimalFormat("#.###");

/**
 * Returns a string for given map.
 */
public String getString(Map aMap)
{
    _sb.setLength(0);
    writeMapTop(aMap);
    return _sb.toString();
}

/**
 * Writes a key.
 */
protected void writeKey(String aKey)  { _sb.append(aKey); }

/**
 * Writes an object to StringBuffer.
 */
protected void writeValue(Object anObj)
{
    if(anObj instanceof String) _sb.append('\"').append(anObj).append('\"');
    else if(anObj instanceof Number) _sb.append(_fmt.format((Number)anObj));
    else if(anObj instanceof Color) writeColor((Color)anObj);
    else if(anObj instanceof Boolean) _sb.append((Boolean)anObj);
    else if(anObj==null) _sb.append("null");
    else if(anObj instanceof Map) writeMap((Map)anObj);
    else if(anObj instanceof List) writeList((List)anObj);
    else System.err.println("MapWriter.writeVaue: Unsupported class: " + anObj.getClass().getSimpleName());
}

/**
 * Writes a map to StringBuffer.
 */
protected void writeMapTop(Map aMap)
{
    String keys[] = { "title", "subtitle", "yaxis", "xaxis", "legend", "plotOptions", "series" };
    Map map = new HashMap(aMap);
    
    _sb.append('{'); newline(); indent();
    for(String key : keys) { Object val = aMap.get(key); map.remove(key); newline();
        writeIndent(); _sb.append(key).append(": "); writeValue(val); _sb.append(","); newline(); }
    for(String key : (Set<String>)map.keySet()) { Object val = map.get(key); newline();
        writeIndent(); _sb.append(key).append(": "); writeValue(val); _sb.append(","); newline(); }
    if(aMap.size()>0) { delete(2); newline(); }
    outdent(); writeIndent(); _sb.append('}');
}

/**
 * Writes a map to StringBuffer.
 */
protected void writeMap(Map aMap)
{
    int len = _sb.length(); boolean simple = true;
    _sb.append('{'); newline(); indent();
    for(String key : (Set<String>)aMap.keySet()) { Object val = aMap.get(key);
        writeIndent(); _sb.append(key).append(": "); writeValue(val); _sb.append(","); newline();
        if(simple && (val instanceof Map || val instanceof List)) simple = false;
    }
    if(aMap.size()>0) { delete(2); newline(); }
    outdent(); writeIndent(); _sb.append('}');
    
    // If map less than 100 chars, write as single line
    int size = _sb.length() - len, size2 = size - getSpaceCount(len);
    if(simple && size2<100 && !_compact) { delete(size); _compact = true; writeMap(aMap); _compact = false; }
}

/**
 * Writes a list to StringBuffer.
 */
protected void writeList(List aList)
{
    int len = _sb.length(); boolean simple = true;
    _sb.append('['); newline(); indent();
    for(Object val : aList) {
        writeIndent(); writeValue(val); _sb.append(','); newline();
        if(simple && (val instanceof Map || val instanceof List)) simple = false;
    }
    if(aList.size()>0) { delete(2); newline(); }
    outdent(); writeIndent(); _sb.append(']');
    
    // If map less than 100 chars, write as single line
    int size = _sb.length() - len, size2 = size - getSpaceCount(len);
    if(simple && size2<100 && !_compact) { delete(size); _compact = true; writeList(aList); _compact = false; }
}

/**
 * Writes a map to Color.
 */
protected void writeColor(Color aColor)
{
    writeValue(aColor.toHexString());
}

/**
 * Indents.
 */
void indent()  { _indent++; }

/**
 * Outdent.
 */
void outdent()  { _indent--; }

/**
 * Writes the indent.
 */
protected void writeIndent()  { if(!_compact) for(int i=0;i<_indent;i++) _sb.append(_istr); }

/**
 * Writes newline.
 */
void newline()  { if(_compact) _sb.append(' '); else _sb.append('\n'); }

/**
 * Deletes the given number of last chars.
 */
protected void delete(int aCount)
{
    _sb.delete(_sb.length() - aCount, _sb.length());
}

/**
 * Returns the space count after given index.
 */
int getSpaceCount(int anIndex)
{
    int count = 0; boolean lastWasWhiteSpace = false;
    for(int i=anIndex,iMax=_sb.length(); i<iMax; i++) {
        boolean ws = Character.isWhitespace(_sb.charAt(i));
        if(ws && lastWasWhiteSpace) count++; lastWasWhiteSpace = ws;
    }
    return count;
}

}