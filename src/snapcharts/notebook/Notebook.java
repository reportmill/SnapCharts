/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import javakit.parse.JavaParser;
import javakit.reflect.Resolver;
import javakit.shell.JavaTextDocBlock;
import javakit.shell.JavaText;
import javakit.shell.JavaTextDoc;
import snap.props.PropObject;
import snap.props.PropSet;
import java.util.ArrayList;
import java.util.List;

/**
 * This class manages collections of snippets.
 */
public class Notebook extends PropObject {

    // The JavaTextDoc
    private JavaTextDoc  _javaDoc;

    // The name
    private String  _name;

    // The list of entries
    private List<JavaEntry>  _javaEntries = new ArrayList<>();

    // Whether notebook needs update
    private boolean  _needsUpdate;

    // The Processor
    private Processor  _processor;

    // Constants for properties
    public static final String Entries_Prop = "Entries";
    public static final String NeedsUpdate_Prop = "NeedsUpdate";

    // Constant for Resolver
    private static Resolver  _resolver = Resolver.newResolverForClassLoader(Notebook.class.getClassLoader());

    /**
     * Constructor.
     */
    public Notebook()
    {
        super();

        // Create JavaDoc to hold Java code
        JavaTextDoc javaDoc = new JavaTextDoc();

        // Create JavaParser with Resolver for JavaDoc
        JavaParser javaParser = new JavaParser();
        javaParser.setResolver(_resolver);
        javaDoc.setJavaParser(javaParser);

        // Get template Java text string
        JavaText javaText = new JavaText();
        javaText.setSuperClassName(ChartsREPL.class.getName());
        javaText.addImport("snapcharts.data.*");
        javaText.addImport("snapcharts.notebook.*");
        String javaTextStr = javaText.getText();

        // Create JavaDoc for template Java text
        javaDoc.setString(javaTextStr);

        // Set javaDoc
        setJavaDoc(javaDoc);
    }

    /**
     * Returns the JavaDoc.
     */
    public JavaTextDoc getJavaDoc()  { return _javaDoc; }

    /**
     * Sets the JavaDoc.
     */
    public void setJavaDoc(JavaTextDoc aJavaDoc)
    {
        // If already set, just return
        if (aJavaDoc == getJavaDoc()) return;

        // Set JavaDoc
        _javaDoc = aJavaDoc;

        // Get Java blocks from Java doc and clear entries
        JavaTextDocBlock[] javaBlocks = _javaDoc.getBlocks();
        _javaEntries.clear();

        // Iterate over Java blocks and create/add Java entries
        for (JavaTextDocBlock javaBlock : javaBlocks) {
            JavaEntry javaBlockNB = new JavaEntry(this, javaBlock);
            addEntry(javaBlockNB);
        }
    }

    /**
     * Returns the name.
     */
    public String getName()  { return _name; }

    /**
     * Sets the name.
     */
    public void setName(String aName)
    {
        _name = aName;
    }

    /**
     * Returns the JavaEntries.
     */
    public List<JavaEntry> getEntries()  { return _javaEntries; }

    /**
     * Adds a entry.
     */
    public void addEntry(JavaEntry aJavaEntry)
    {
        addEntry(aJavaEntry, _javaEntries.size());
    }

    /**
     * Adds a entry at given index.
     */
    public void addEntry(JavaEntry aJavaEntry, int anIndex)
    {
        _javaEntries.add(anIndex, aJavaEntry);
        aJavaEntry.setIndex(anIndex + 1);
        firePropChange(Entries_Prop, null, aJavaEntry, anIndex);
    }

    /**
     * Removes a entry at given index.
     */
    public void removeEntry(int anIndex)
    {
        // Remove entry
        JavaEntry javaEntry = _javaEntries.remove(anIndex);

        // Remove block
        JavaTextDoc javaDoc = getJavaDoc();
        JavaTextDocBlock javaBlock = javaEntry.getJavaBlock();
        javaDoc.removeBlock(javaBlock);

        // Fire PropChange
        firePropChange(Entries_Prop, javaEntry, null, anIndex);
    }

    /**
     * Removes given entry.
     */
    public void removeEntry(JavaEntry aJavaEntry)
    {
        int index = _javaEntries.indexOf(aJavaEntry);
        if (index >= 0)
            removeEntry(index);
    }

    /**
     * Returns whether notebook needs update.
     */
    public boolean isNeedsUpdate()  { return _needsUpdate; }

    /**
     * Sets whether notebook needs update.
     */
    protected void setNeedsUpdate(boolean aValue)
    {
        if (aValue == isNeedsUpdate()) return;
        firePropChange(NeedsUpdate_Prop, _needsUpdate, _needsUpdate = aValue);
    }

    /**
     * Submit entry.
     */
    public void submitEntry(JavaEntry aJavaEntry)
    {
        // Set NeedsUpdate
        setNeedsUpdate(true);
    }

    /**
     * Updates the notebook.
     */
    public void updateNotebook()
    {
        // Run processor
        Processor processor = getProcessor();
        processor.resetAll();

        // Get entries
        List<JavaEntry> entries = getEntries();

        // Iterate over entries and get/set response for each
        for (JavaEntry entry : entries) {

            // Get value for entry
            Object value = processor.getValueForJavaEntry(entry);
            if (value == null)
                continue;

            // Create Response, set value
            Response response = new Response(this);
            response.setValue(value);
            response.setIndex(entry.getIndex());
            entry.setResponse(response);
        }

        // Reset NeedsUpdate
        setNeedsUpdate(false);
    }

    /**
     * Returns the processor.
     */
    public Processor getProcessor()
    {
        if (_processor != null) return _processor;
        Processor processor = new Processor(this);
        return _processor = processor;
    }

    /**
     * Returns whether empty entry is at end of notebook.
     */
    public boolean isEmptyEntrySet()
    {
        List<JavaEntry> entries = getEntries();
        JavaEntry lastEntry = entries.size() > 0 ? entries.get(entries.size() - 1) : null;
        return lastEntry != null && lastEntry.isEmpty();
    }

    /**
     * Adds an empty entry to notebook.
     */
    public void addEmptyEntry()
    {
        JavaTextDocBlock javaBlock = _javaDoc.addEmptyBlock();
        JavaEntry javaEntry = new JavaEntry(this, javaBlock);
        addEntry(javaEntry);
    }

    /**
     * Override to register props.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        aPropSet.addPropNamed(Entries_Prop, JavaEntry[].class, EMPTY_OBJECT);
    }

    /**
     * Returns the prop value for given key.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        // Handle properties
        switch (aPropName) {

            // Entries
            case Entries_Prop: return getEntries();

            // Handle super class properties (or unknown)
            default: return super.getPropValue(aPropName);
        }
    }

    /**
     * Sets the prop value for given key.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        // Handle properties
        switch (aPropName) {

            // Entries
            //case Entries_Prop: setEntries(aValue);

            // Handle super class properties (or unknown)
            default: super.setPropValue(aPropName, aValue);
        }
    }
}
