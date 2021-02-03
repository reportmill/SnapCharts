package snapcharts.util;
import snap.geom.Path2D;
import snap.geom.Point;
import snap.geom.Shape;
import snapcharts.model.DataSet;

import java.util.*;

/**
 * A class to represent a Triangle mesh.
 */
public class Mesh {

    // The DataSet
    private DataSet  _dset;

    // The triangles
    private Triangle[]  _triangles;

    // A map of edges
    private Map<Long,Edge>  _edges = new HashMap<>();

    /**
     * Constructor to create mesh for given DataSet and array of triangle vertex indexes.
     */
    public Mesh(DataSet aDataSet, int[] triangles)
    {
        _dset = aDataSet;

        // Get number of triangles
        int pointCount = triangles.length;
        int triangleCount = pointCount / 3;

        // Create triangles array
        _triangles = new Triangle[triangleCount];

        // Iterate over triangle index triplets
        for (int i=0; i<triangleCount; i++) {
            int v1 = triangles[i*3];
            int v2 = triangles[i*3+1];
            int v3 = triangles[i*3+2];
            _triangles[i] = new Triangle(v1, v2, v3);
        }
    }

    /**
     * Return a list of contour lines that have a constant elevation near the specified
     * value. Lines will be truncated to the sepcified x/y range, and the elevation
     * function will be sampled at even intervals determined by the spacing parameter.
     */
    public Shape getContourShape(double valZ)
    {
        // Get line segments
        List<Isoline> isolines = getIsolines(valZ);

        // Create path (just return if no segments)
        Path2D path = new Path2D();
        if (isolines.size() == 0)
            return path;

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

            // Expand back
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
            int aboveInd = _dset.getZ(edgeN.v1) >= valZ ? edgeN.v1 : edgeN.v2;

            // Otherwise, go around mesh edges
            while (edge0 != edgeN) {

                // Get edgeN index that is above valZ and get/add point at index
                double aboveX = _dset.getX(aboveInd);
                double aboveY = _dset.getY(aboveInd);
                points.add(new Point(aboveX, aboveY));

                // Get next perimeter edge
                Edge edgeTemp = edgeN;
                edgeN = getNextPerimeterEdge(edgeN, aboveInd);

                // There has to be a next perimeter edge, right?
                if (edgeN==null) {
                    edgeN = getNextPerimeterEdge(edgeTemp, aboveInd);
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
                aboveInd = edgeN.v1==aboveInd ? edgeN.v2 : edgeN.v1;

                // Sanity check: There can't possibly be more path points than there are dataset points
                if (points.size() > _dset.getPointCount()) {
                    System.err.println("Mesh.getPointsForOneSubpathForIsolines: Compute error (too many points)");
                    return points;
                }
            }

            // Sanity check: There can't possibly be more path points than there are dataset points
            if (points.size() > _dset.getPointCount()) {
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
        for (int i=0, iMax=theIsolines.size(); i<iMax; i++) {
            Isoline iso = theIsolines.get(i);
            if (iso.edge1 == anEdge || iso.edge2 == anEdge)
                return theIsolines.remove(i);
        }
        return null;
    }

    /**
     * Returns the egde on the perimeter (is part of only one triangle) that shares given vertex index but isn't given edge.
     */
    private Edge getNextPerimeterEdge(Edge anEdge, int vertInd)
    {
        // Iterate over edges to find
        Collection<Edge> edges = _edges.values();
        for (Edge edge : edges)
            if (edge != anEdge && edge.isPerimeter() && (edge.v1 == vertInd || edge.v2 == vertInd))
                return edge;

        // Return null since not found
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
        for (Triangle triangle : _triangles) {

            // Fill above/below arrays with triangle verteces above/below value
            above.clear(); below.clear();
            for (int v : triangle.vertices) {
                if (_dset.getZ(v) < valZ)
                    below.add(v);
                else above.add(v);
            }

            // All above or all below means no contour line here
            if (below.size == 0 || above.size == 0)
                continue;

            // Recategorize above/below arrays as minority/majority
            IntArray minority = above.size < below.size ? above : below;
            IntArray majority = above.size > below.size ? above : below;

            // Get edges hit by valZ
            Edge edge1 = getEdge(minority.items[0], majority.items[0]);
            Edge edge2 = getEdge(minority.items[0], majority.items[1]);

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
     * Returns an edge for given vertex indexes.
     */
    public Edge getEdge(int index1, int index2)
    {
        int min = Math.min(index1, index2);
        int max = Math.max(index1, index2);
        long minL = ((long) min) << 32;
        Long hash = minL + max;
        Edge edge = _edges.get(hash);
        if (edge==null)
            _edges.put(hash, edge = new Edge(min, max));
        return edge;
    }

    /**
     * A class to represent a triangle in the mesh (as 3 vertex indexes).
     */
    public class Triangle {

        // Triangle vertices
        public int v1, v2, v3;

        // Vertices array
        public int[] vertices;

        // Triangle edges
        public Edge e1, e2, e3;

        /**
         * Constructor.
         */
        public Triangle(int ind1, int ind2, int ind3)
        {
            // Set vertices
            v1 = ind1; v2 = ind2; v3 = ind3;
            vertices = new int[] { v1, v2, v3 };

            // Get/set edges
            e1 = getEdge(v1, v2); e1.bumpUsage();
            e2 = getEdge(v2, v3); e2.bumpUsage();
            e3 = getEdge(v3, v1); e3.bumpUsage();
        }

        /**
         * Standard toString implementation.
         */
        @Override
        public String toString()
        {
            return "Triangle { v1=" + v1 + ", v2=" + v2 + ", v3=" + v3 + ", e1=[" + e1 + "], e2=[" + e2 + "], e3=[" + e3 + "] }";
        }
    }

    /**
     * A class to represent a triangle edge (as two vertex indexes).
     */
    public class Edge {

        // The edge vertexes (indexes into DataSet records)
        public int v1, v2;

        // The number of triangle that contain this edge (1 means it's on the perimeter)
        public int usage;

        /**
         * Constructor.
         */
        public Edge(int ind1, int ind2)
        {
            v1 = ind1; v2 = ind2;
        }

        /**
         * Returns whether edge is on the perimeter.
         */
        public boolean isPerimeter()  { return usage==1; }

        /**
         * Returns an X/Y point along edge for given DataSet Z value.
         */
        public Point getPointAlongEdgeForZ(double valZ)
        {
            // Get Z val for edge vertices and calc ratio
            double e1zval = _dset.getZ(v1);
            double e2zval = _dset.getZ(v2);
            double how_far = ((valZ - e2zval) / (e1zval - e2zval));

            // Get X/Y values for edge vertexes and interpolate X/Y value for valZ
            double e1xval = _dset.getX(v1);
            double e1yval = _dset.getY(v1);
            double e2xval = _dset.getX(v2);
            double e2yval = _dset.getY(v2);
            double dataX = how_far * e1xval + (1 - how_far) * e2xval;
            double dataY = how_far * e1yval + (1 - how_far) * e2yval;
            return new Point(dataX, dataY);
        }

        private void bumpUsage()
        {
            usage++;
            if (v1==0 && v2==1 && usage>1)
                System.out.println("WTF");
        }

        /**
         * Standard toString implementation.
         */
        @Override
        public String toString()
        {
            return "Edge { v1=" + v1 + ", v2=" + v2 + ", usage=" + usage + " }";
        }
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
