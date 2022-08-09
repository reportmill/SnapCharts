package snapcharts.util;
import snap.geom.Path2D;
import snap.geom.Point;
import snap.geom.Shape;
import snapcharts.data.DataSet;
import snapcharts.data.IntArray;
import snapcharts.util.Mesh.Edge;
import snapcharts.util.Mesh.Triangle;
import java.util.*;

/**
 * A class to generate contour paths from a DataSet.
 */
public class ContourMaker {

    // The Mesh
    private Mesh  _mesh;

    // The number of points in dataset
    private int  _pointCount;

    /**
     * Constructor to create mesh for given DataSet and array of triangle vertex indexes.
     */
    public ContourMaker(DataSet aDataSet)
    {
        _mesh = new Mesh(aDataSet);
        _pointCount = _mesh.getPointCount();
    }

    /**
     * Returns the mesh this contour maker uses to make contours.
     */
    public Mesh getMesh()  { return _mesh; }

    /**
     * Return a list of contour lines that have a constant elevation near the specified
     * value. Lines will be truncated to the sepcified x/y range, and the elevation
     * function will be sampled at even intervals determined by the spacing parameter.
     */
    public Shape getContourShape(double valZ)
    {
        // Get line segments
        List<Isoline> isolines = getIsolines(valZ);

        // If no isolones, return path around mesh
        if (isolines.size()==0)
            return getMesh().getHullPath();

        // Create path
        Path2D path = new Path2D();

        // Add subpaths from isolines until done
        while (isolines.size() != 0) {

            // Find next set of subpath points from isolines list (trims isolines as they are used)
            List<Point> points = getPointsForOneSubpathForIsolines(isolines, valZ);

            // Iterate over points and add to path
            for (int i=0, iMax=points.size(); i<iMax; i++) {
                Point point = points.get(i);
                if (i==0)
                    path.moveTo(point.x, point.y);
                else path.lineTo(point.x, point.y);
            }

            // Close path
            path.close();
        }

        // Return path
        return path;
    }

    /**
     * Returns the points for one subpath for given list of Isolines
     */
    private List<Point> getPointsForOneSubpathForIsolines(List<Isoline> theIsolines, double valZ)
    {
        // Get info
        List<Point> points = new ArrayList<>();
        Isoline firstIso = theIsolines.remove(0);
        Edge edge0 = firstIso.edge1;
        Edge edgeN = firstIso.edge2;
        points.add(firstIso.point1);
        points.add(firstIso.point2);

        // Iterate until closed
        while (edge0 != edgeN) {

            // Expand forward
            Isoline nextIso = getIsolineForEdge(theIsolines, edgeN);
            while (nextIso != null) {
                points.add(edgeN == nextIso.edge1 ? nextIso.point2 : nextIso.point1);
                edgeN = edgeN == nextIso.edge1 ? nextIso.edge2 : nextIso.edge1;
                nextIso = getIsolineForEdge(theIsolines, edgeN);
            }

            // Expand back - do I need this?
            Isoline prevIso = getIsolineForEdge(theIsolines, edge0);
            while (prevIso != null) {
                points.add(0, edge0 == prevIso.edge1 ? prevIso.point2 : prevIso.point1);
                edge0 = edge0 == prevIso.edge1 ? prevIso.edge2 : prevIso.edge1;
                prevIso = getIsolineForEdge(theIsolines, edge0);
            }

            // If closed loop of points, just return
            if (edge0 == edgeN)
                return points;

            // Sanity check: Both edges should be on perimeter
            if (!edge0.isPerimeter() || !edgeN.isPerimeter()) {
                System.err.println("Mesh.getPointsForOneSubpathForIsolines: unconnected edge not on perimeter");
                if (!edge0.isPerimeter())
                    getIsolineForEdge(theIsolines, edge0);
                if (!edgeN.isPerimeter())
                    getIsolineForEdge(theIsolines, edgeN);
            }

            // Get next index to add
            int aboveInd = getZ(edgeN.v1) >= valZ ? edgeN.v1 : edgeN.v2;

            // Otherwise, go around mesh edges
            while (edge0 != edgeN) {

                // Get edgeN index that is above valZ and get/add point at index
                double aboveX = getX(aboveInd);
                double aboveY = getY(aboveInd);
                points.add(new Point(aboveX, aboveY));

                // Get next perimeter edge
                Edge edgeTemp = edgeN;
                edgeN = _mesh.getNextPerimeterEdge(edgeN, aboveInd);

                // There has to be a next perimeter edge, right?
                if (edgeN == null) {
                    edgeN = _mesh.getNextPerimeterEdge(edgeTemp, aboveInd);
                    System.err.println("Mesh.getPointsForOneSubpathForIsolines: Can't find next perimeter edge");
                    return points;
                }

                // If has isoline, add that point and break
                Isoline edgeIso = getIsolineForEdge(theIsolines, edgeN);
                if (edgeIso != null) {
                    Point point1 = edgeIso.edge1 == edgeN ? edgeIso.point1 : edgeIso.point2;
                    Point point2 = edgeIso.edge1 == edgeN ? edgeIso.point2 : edgeIso.point1;
                    points.add(point1);
                    points.add(point2);
                    edgeN = edgeIso.edge1 == edgeN ? edgeIso.edge2 : edgeIso.edge1;
                    break;
                }

                // Otherwise update nextIndex
                aboveInd = edgeN.v1 == aboveInd ? edgeN.v2 : edgeN.v1;

                // Sanity check: There can't possibly be more path points than there are dataset points
                if (points.size() > getPointCount()*2) {
                    System.err.println("Mesh.getPointsForOneSubpathForIsolines: Compute error (too many points)");
                    return points;
                }
            }

            // Sanity check: There can't possibly be more path points than there are dataset points
            if (points.size() > getPointCount()*2) {
                System.err.println("Mesh.getPointsForOneSubpathForIsolines: Compute error (too many points)");
                return points;
            }
        }

        // Return points
        return points;
    }

    /**
     * Searches the given Isolines list for one that includes given edge. If found, removes and returns (otherwise null).
     */
    private Isoline getIsolineForEdge(List<Isoline> theIsolines, Edge anEdge)
    {
        for (int i = 0, iMax = theIsolines.size(); i < iMax; i++) {
            Isoline iso = theIsolines.get(i);
            if (iso.edge1 == anEdge || iso.edge2 == anEdge)
                return theIsolines.remove(i);
        }
        return null;
    }

    /**
     * Returns an array of isolines for given Z alue.
     */
    public List<Isoline> getIsolines(double valZ)
    {
        // A couple of int arrays to hold indexes above/below given threshold Z value for each triangle
        IntArray above = new IntArray();
        IntArray below = new IntArray();

        // Create array of Segments to hold
        List<Isoline> isolines = new ArrayList<>();

        // Iterate over triangles and find segments for valZ
        Triangle[] triangles = _mesh.getTriangles();
        for (Triangle triangle : triangles) {

            // Fill above/below arrays with triangle verteces above/below value
            above.clear(); below.clear();
            for (int v : triangle.vertices) {
                if (getZ(v) <= valZ)
                    below.add(v);
                else above.add(v);
            }

            // All above or all below means no contour line here
            int belowLen = below.length();
            int aboveLen = above.length();
            if (belowLen == 0 || aboveLen == 0)
                continue;

            // Recategorize above/below arrays as minority/majority
            IntArray minority = aboveLen < belowLen ? above : below;
            IntArray majority = aboveLen > belowLen ? above : below;

            // Get edges hit by valZ
            Edge edge1 = getEdge(minority.getInt(0), majority.getInt(0));
            Edge edge2 = getEdge(minority.getInt(0), majority.getInt(1));

            // Get points at either end of contour lines
            Point point1 = edge1.getPointAlongEdgeForZ(valZ);
            Point point2 = edge2.getPointAlongEdgeForZ(valZ);

            // Create/add line
            Isoline isoline = new Isoline(triangle, edge1, point1, edge2, point2);
            isolines.add(isoline);
        }

        // Return segments
        return isolines;
    }

    /**
     * Returns the number of points.
     */
    private int getPointCount()  { return _pointCount; }

    /**
     * Returns X value at given dataset point index (accounts for 'super-triangle' points).
     */
    private double getX(int anIndex)
    {
        return _mesh.getX(anIndex);
    }

    /**
     * Returns Y at given dataset point index (accounts for 'super-triangle' points).
     */
    private double getY(int anIndex)
    {
        return _mesh.getY(anIndex);
    }

    /**
     * Returns Z at given dataset point index (accounts for 'super-triangle' points).
     */
    private double getZ(int anIndex)
    {
        return _mesh.getZ(anIndex);
    }

    /**
     * Returns an edge for given vertex indexes.
     */
    public Edge getEdge(int index1, int index2)
    {
        return _mesh.getEdge(index1, index2);
    }

    /**
     * A class to represent a line through two triangle edges for a contour value.
     */
    public class Isoline {

        // The Triangle
        public Triangle  triangle;

        // The Edges
        public Edge  edge1, edge2;

        // The points
        public Point point1, point2;

        /**
         * Constructor.
         */
        public Isoline(Triangle aTriangle, Edge anEdge1, Point aPoint1, Edge anEdge2, Point aPoint2)
        {
            triangle = aTriangle;
            edge1 = anEdge1; point1 = aPoint1;
            edge2 = anEdge2; point2 = aPoint2;
        }

        /**
         * Standard toString implementation.
         */
        @Override
        public String toString()
        {
            return "Isoline { edge1=" + edge1 + ", edge2=" + edge2 + ", point1=" + point1 + ", point2=" + point2 + " } ";
        }
    }
}
