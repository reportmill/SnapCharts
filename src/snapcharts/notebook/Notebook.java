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

    // The list of Request entries
    private List<JavaEntry>  _javaEntries = new ArrayList<>();

    // The Processor
    private Processor  _processor;

    // Constants for properties
    public static final String Request_Prop = "Request";

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

        // Rebuild requests
        _javaEntries.clear();
        JavaTextDocBlock[] javaBlocks = _javaDoc.getBlocks();
        for (JavaTextDocBlock javaBlock : javaBlocks) {
            JavaEntry javaBlockNB = new JavaEntry(javaBlock);
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
        firePropChange(Request_Prop, null, aJavaEntry, anIndex);
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
        firePropChange(Request_Prop, javaEntry, null, anIndex);
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
     * Returns the Response for a given entry.
     */
    public Response getResponseForEntry(JavaEntry aJavaEntry)
    {
        // Get response from map - just return if found
        Response response = aJavaEntry.getResponse();
        if (response != null)
            return response;

        // Create Response for Request
        response = createResponseForEntry(aJavaEntry);

        // Set in request and return
        aJavaEntry.setResponse(response);
        return response;
    }

    /**
     * Returns the response for a given entry.
     */
    protected Response createResponseForEntry(JavaEntry aJavaEntry)
    {
        Processor processor = getProcessor();
        Response response = processor.createResponseForRequest(aJavaEntry);
        response.setIndex(aJavaEntry.getIndex());
        return response;
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
        JavaEntry javaEntry = new JavaEntry(javaBlock);
        addEntry(javaEntry);
    }

    /**
     * Returns the last response of given class.
     */
    public Response getLastResponseForValueClass(Class aClass)
    {
        // Get list of Requests
        List<JavaEntry> javaEntries = getEntries();

        // Iterate over requests (backwards) and return first Response.Value for given class
        for (int i = javaEntries.size() - 1; i >= 0; i--) {

            // Get request/response
            JavaEntry javaEntry = javaEntries.get(i);
            Response response = javaEntry.getResponse();
            if (response == null)
                continue;

            // If Response.Value is of given class, return it
            Object responseValue = response.getValue();
            if (aClass.isInstance(responseValue))
                return response;
        }

        // Return not found
        return null;
    }

    /**
     * Returns the last response of given class.
     */
    public <T> T getLastResponseValueForClass(Class<T> aClass)
    {
        Response response = getLastResponseForValueClass(aClass);
        T responseValue = response != null ? (T) response.getValue() : null;
        return responseValue;
    }

    /**
     * Override to register props.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        aPropSet.addPropNamed(Request_Prop, JavaEntry[].class, EMPTY_OBJECT);
    }

    /**
     * Returns the prop value for given key.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        // Handle properties
        switch (aPropName) {

            // Request
            case Request_Prop: return getEntries();

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

            // Request
            //case Request_Prop: setRequests();

            // Handle super class properties (or unknown)
            default: super.setPropValue(aPropName, aValue);
        }
    }
}
