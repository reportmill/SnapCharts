/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.data;
import snap.props.StringCodec;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents data units.
 */
public class DataUnit implements StringCodec.Codeable {

    // The name
    private String  _name;

    // All known units
    private static DataUnit[]  _allUnits;

    // Constants for units
    public static final DataUnit Degrees = new DataUnit("Degrees");
    public static final DataUnit Radians = new DataUnit("Radians");

    // Constant for Default
    public static final DataUnit DEFAULT_THETA_UNIT = Degrees;

    /**
     * Constructor.
     */
    public DataUnit()
    {
        _name = "Unknown";
    }

    /**
     * Constructor.
     */
    public DataUnit(String aName)
    {
        _name = aName;
    }

    /**
     * Returns the name.
     */
    public String getName()  { return _name; }

    /**
     * Returns a String representation of this object.
     */
    @Override
    public String codeString()
    {
        return _name;
    }

    /**
     * Configures this object from a String representation.
     */
    @Override
    public StringCodec.Codeable decodeString(String aString)
    {
        return getUnitForName(aString, null);
    }

    /**
     * Standard toString.
     */
    @Override
    public String toString()
    {
        return getName();
    }

    /**
     * Returns all units.
     */
    public static DataUnit[] getAllUnits()
    {
        // If already set, just return
        if (_allUnits != null) return _allUnits;

        // Create/set
        List<DataUnit> allUnits = new ArrayList<>();
        allUnits.add(Degrees);
        allUnits.add(Radians);

        // Set/return
        return _allUnits = allUnits.toArray(new DataUnit[0]);
    }

    /**
     * Returns unit for name.
     */
    public static DataUnit getUnitForName(String aName, DataUnit aDefault)
    {
        // Iterate over AllUnits and return if unit matches
        DataUnit[] allUnits = getAllUnits();
        for (DataUnit dataUnit : allUnits)
            if (dataUnit.getName().equals(aName))
                return dataUnit;

        // Iterate over AllUnits and return if unit matches
        for (DataUnit dataUnit : allUnits)
            if (dataUnit.getName().equalsIgnoreCase(aName))
                return dataUnit;

        // Return null since not found
        return aDefault;
    }
}
