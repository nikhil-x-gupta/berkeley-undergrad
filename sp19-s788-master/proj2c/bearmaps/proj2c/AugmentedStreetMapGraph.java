package bearmaps.proj2c;

import bearmaps.hw4.streetmap.Node;
import bearmaps.hw4.streetmap.StreetMapGraph;
import bearmaps.proj2ab.Point;
import bearmaps.proj2ab.WeirdPointSet;

import java.util.*;

/**
 * An augmented graph that is more powerful that a standard StreetMapGraph.
 * Specifically, it supports the following additional operations:
 *
 *
 * @author Alan Yao, Josh Hug, ________
 */
public class AugmentedStreetMapGraph extends StreetMapGraph {

    private Map<Point, Node> pointToNode = new HashMap<>();
    private WeirdPointSet pointTree;
    private MyTrieSet cleanStringTree = new MyTrieSet();
    private Map<String, ArrayList<Node>> cleanToNode = new HashMap<>();

    public AugmentedStreetMapGraph(String dbPath) {
        super(dbPath);
        // You might find it helpful to uncomment the line below:
        List<Node> nodes = this.getNodes();
        List<Point> points = new ArrayList<>();
        for (Node node: nodes) {
            Point point = new Point(node.lon(), node.lat());
            this.pointToNode.put(point, node);
            if (this.neighbors(node.id()).size() != 0) {
                points.add(point);
            }
            if (node.name() != null) {
                String cleanName = cleanString(node.name());
                this.cleanStringTree.add(cleanName);
                ArrayList<Node> dupes;
                if (this.cleanToNode.containsKey(cleanName)) {
                    dupes = this.cleanToNode.get(cleanName);
                } else {
                    dupes = new ArrayList<>();
                }
                dupes.add(node);
                this.cleanToNode.put(cleanName, dupes);
            }
        }
        this.pointTree = new WeirdPointSet(points);
    }


    /**
     * For Project Part II
     * Returns the vertex closest to the given longitude and latitude.
     * @param lon The target longitude.
     * @param lat The target latitude.
     * @return The id of the node in the graph closest to the target.
     */
    public long closest(double lon, double lat) {
        Point nearestPoint = this.pointTree.nearest(lon, lat);
        return this.pointToNode.get(nearestPoint).id();
    }


    /**
     * For Project Part III (gold points)
     * In linear time, collect all the names of OSM locations that prefix-match the query string.
     * @param prefix Prefix string to be searched for. Could be any case, with our without
     *               punctuation.
     * @return A <code>List</code> of the full names of locations whose cleaned name matches the
     * cleaned <code>prefix</code>.
     */
    public List<String> getLocationsByPrefix(String prefix) {
        prefix = cleanString(prefix);
        ArrayList<String> locMatches = (ArrayList<String>) this.cleanStringTree.keysWithPrefix(prefix);
        ArrayList<String> realLocs = new ArrayList<>(locMatches.size());
        for (String loc : locMatches) {
            ArrayList<Node> dupes = this.cleanToNode.get(loc);
            for (Node dupe : dupes) {
                if (!realLocs.contains(dupe.name())) {
                    realLocs.add(dupe.name());
                }
            }
        }
        return realLocs;
    }

    /**
     * For Project Part III (gold points)
     * Collect all locations that match a cleaned <code>locationName</code>, and return
     * information about each node that matches.
     * @param locationName A full name of a location searched for.
     * @return A list of locations whose cleaned name matches the
     * cleaned <code>locationName</code>, and each location is a map of parameters for the Json
     * response as specified: <br>
     * "lat" -> Number, The latitude of the node. <br>
     * "lon" -> Number, The longitude of the node. <br>
     * "name" -> String, The actual name of the node. <br>
     * "id" -> Number, The id of the node. <br>
     */
    public List<Map<String, Object>> getLocations(String locationName) {
        locationName = cleanString(locationName);
        ArrayList<Map<String, Object>> matchLocs = new ArrayList<>();
        ArrayList<Node> dupes = this.cleanToNode.get(locationName);
        for (Node dupe : dupes) {
            Map<String, Object> nodeData = new HashMap<>();
            nodeData.put("lat", dupe.lat());
            nodeData.put("lon", dupe.lon());
            nodeData.put("name", dupe.name());
            nodeData.put("id", dupe.id());
            matchLocs.add(nodeData);
        }
        return matchLocs;
    }


    /**
     * Useful for Part III. Do not modify.
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     * @param s Input string.
     * @return Cleaned string.
     */
    private static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

}
