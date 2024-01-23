import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

/**
 * Create a graph that uses A* to find the distance between
 * vertices.
 * 
 * @author Sumneet Brar
 */
public class AStarGraph {

  public class City {

    private String name;
    private double latitude;
    private double longitude;

    private double weight;
    private double cost;

    public City(String name, double latitude, double longitude) {
      this.name = name;
      this.latitude = latitude;
      this.longitude = longitude;
    }

    public String getName() {
      return name;
    }

    public double getLatitude() {
      return latitude;
    }

    public double getLongitude() {
      return longitude;
    }

    private double getWeight() {
      return weight;
    }
    
    private double getCost() {
      return cost;
    }

    private void setWeight(double weight) {
      this.weight = weight;
      this.cost = weight;
    }

    private void setCost(double cost) {
      this.cost = cost;
    }
  }

  private final int vertices = 50;
  // private int edges = 0;
  private int index = 0;

  private ArrayList<City>[] adj; // arraylist of adjacency lists
  private HashMap<String, Integer> states; // map to store the indicies
  // private HashMap<String, Pair> edges;

  /**
   * Create an empty graph
   */
  public AStarGraph() {
    adj = new ArrayList[50];
    states = new HashMap<String, Integer>();
    // edges = new HashMap<String, Pair>();

    // Initialize each ArrayList in the adj array
    for (int i = 0; i < vertices; i++) {
      adj[i] = new ArrayList<>();
    }
  }

  /**
   * Inserts a city into the graph. Throws an IllegalArgumentException if a city
   * by that name already exists. Note that city names often contain spaces.
   * 
   * @param name
   * @param latitude
   * @param longitude
   */
  public void addCity(String name, double latitude, double longitude) {
    // check if the city already exists
    if (states.containsKey(name)) {
      throw new IllegalArgumentException("That city already exists!");
    }

    // create a new city node
    City city = new City(name, latitude, longitude);

    adj[index].add(city); // add it to the next index in the adjacency list
    states.put(name, index); // also add it to the map
    index++;
  }

  /**
   * Adds a new road between two cities. Roads are assumed to be two-way.
   * It should throw an IllegalArgumentException if the cities don’t exist,
   * if the road is shorter than the shortest distance between the cities,
   * or if the cities are already connected by a road.
   * 
   * @param city1
   * @param city2
   * @param length
   */
  public void addRoad(String city1, String city2, double length) {
    // confirm that both cities exist
    if (!states.containsKey(city1) || !states.containsKey(city2)) {
      throw new IllegalArgumentException("That city does not exist!");
    }

    if (roadExists(city1, city2) == true) {
      throw new IllegalArgumentException("That road already exists!");
    }

    // get the index of the given cities in the adjacency list
    int indexOfCity1 = states.get(city1);
    int indexOfCity2 = states.get(city2);

    // create new city objects to add to appropriate adjacency list
    City neighbor = new City(adj[indexOfCity2].get(0).getName(), adj[indexOfCity2].get(0).getLatitude(), adj[indexOfCity2].get(0).getLongitude());
    neighbor.setWeight(length);;
    adj[indexOfCity1].add(neighbor);

    City viceVersa = new City(adj[indexOfCity1].get(0).getName(), adj[indexOfCity1].get(0).getLatitude(), adj[indexOfCity1].get(0).getLongitude());
    viceVersa.setWeight(length);
    adj[indexOfCity2].add(viceVersa);
  }
  
  /**
   * Removes a road between two cities. Returns true if successful,
   * or false if there was no road. Throws an IllegalArgumentException
   * if the vertices don’t exist.
   * 
   * @param city1
   * @param city2
   * @return
   */
  public boolean deleteRoad(String city1, String city2) {
    if (!states.containsKey(city1) || !states.containsKey(city2)) {
      throw new IllegalArgumentException("One or both of the cities don't exist!");
    } 
    
    // confirm road exists
    if (roadExists(city1, city2) == false) return false;
    boolean removed = false;

    int indexOfCity1 = states.get(city1);
    int indexOfCity2 = states.get(city2);

    // remove the other from own adjacency list
    removed = adj[indexOfCity1].remove(adj[indexOfCity2].get(0));
    removed = adj[indexOfCity2].remove(adj[indexOfCity1].get(0));

    return removed;
  }

  private boolean roadExists(String city1, String city2) {
    int indexOfCity1 = states.get(city1);

    // iterate through adjacency list to see if city2 is in city1's list
    for (City temp : adj[indexOfCity1]) {
      if (temp.getName().equals(city2)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Uses A* to find the best path between two cities. It should throw an
   * IllegalArgumentException if the cities don’t exist. The return value should
   * be an array of strings where city1 is the first element, city2 is the last element,
   * and the rest of the cities indicate the step-by-step path between them. It will return
   * null if there exists no such path.
   * 
   * @param city1
   * @param city2
   * @return
   */
  public String[] findPath(String city1, String city2) {
    if (!states.containsKey(city1) || !states.containsKey(city2)) {
      throw new IllegalArgumentException("One or both cities don't exist.");
    }

    Comparator<City> cityComparator = Comparator.comparingDouble(city -> city.getCost());
    PriorityQueue<City> openList = new PriorityQueue<>(cityComparator);
    HashMap<String, Double> gOfX = new HashMap<>(); // store already traveled
    HashMap<String, String> hOfX = new HashMap<>(); // estimate of distance (our heuristic)

    int indexOfCity1 = states.get(city1);
    int indexOfCity2 = states.get(city2);

    // add the current city's adjacency list into the dist travled map
    for (int i = 0; i < index; i++) {
      for (City city : adj[i]) {
        // initialize with infinity
        gOfX.put(city.getName(), Double.POSITIVE_INFINITY);
      }
    }

    gOfX.put(city1, 0.0);
    openList.offer(adj[indexOfCity1].get(0));

    while (!openList.isEmpty()) {
      City current = openList.poll();

      // found destination
      if (current.getName().equals(city2)) {
        // construct path and return it
        ArrayList<String> path = new ArrayList<>();
        path.add(current.getName());
        String prev = hOfX.get(current.getName());
        while (prev != null) {
          path.add(prev);
          prev = hOfX.get(prev);
        }
        Collections.reverse(path);
        return path.toArray(new String[0]);
      }

      for (City neighbor : adj[states.get(current.getName())]) {
        // possible distance we have traveled
        double tempGOfX = gOfX.get(current.getName()) + neighbor.getCost();
        // if this already traveled distance is smaller than the neighbor's distance traveled 
        if (tempGOfX < gOfX.get(neighbor.getName())) {
          // add the neighbor to our maps with the new traveled distance
          hOfX.put(neighbor.getName(), current.getName());
          gOfX.put(neighbor.getName(), tempGOfX);
          double crowFliesDistance = calculateCrowFliesDistance(neighbor, adj[indexOfCity2].get(0));
          double fOfX = tempGOfX + crowFliesDistance;
          neighbor.setCost(fOfX); // set new cost
          if(!openList.contains(neighbor)) openList.offer(neighbor);
        }
      }
    }

    return null; // did not find a path
  }

  private double calculateCrowFliesDistance(City city1, City city2) {
    double latitude1 = Math.toRadians(city1.getLatitude());
    double longitude1 = Math.toRadians(city1.getLongitude());
    double latitude2 = Math.toRadians(city2.getLatitude());
    double longitude2 = Math.toRadians(city2.getLongitude());
    double earthRadius = 6371; 

    // calculate distance
    double distance = Math.acos(Math.sin(latitude1) * Math.sin(latitude2) 
      + Math.cos(latitude1) * Math.cos(latitude2) * Math.cos(longitude1 - longitude2)) * earthRadius;

    return distance;
  }

  /**
   * Sum up the length of the path given. It should throw an
   * IllegalArgumentException if two cities adjacent in the list do not share a
   * road.
   * 
   * @param path
   * @return
   */
  public double measurePath(String[] path) {
    // confirm path validity
    if (path == null || path.length <= 1) {
      throw new IllegalArgumentException("Invalid path provided.");
    }

    double totalLength = 0;
    // iterate through path, obtaining road length of each given token
    for (int i = 0; i < path.length - 1; i++) {
      double roadLength = getRoadLength(path[i], path[i + 1]);
      if (roadLength == -1) {
        throw new IllegalArgumentException("No road between " + path[i] + " and " + path[i + 1]);
      }
      totalLength += roadLength; // else, += roadlength
    }

    return totalLength;
  }

  /**
   * @return the number of cities in the graph.
   */
  public int size() {
    return index;
  }

  /**
   * Returns true if the city exists, or false otherwise.
   * 
   * @param city
   * @return
   */
  public boolean isValidCity(String city) {
    if (states.containsKey(city)) return true;
    return false;
  }

  /**
   * Returns the location of a city, or null if no such city exists.
   */
  public double[] getCityLocation(String city) {
    if (states.containsKey(city)) {
      int indexOfCity = states.get(city);

      City town = adj[indexOfCity].get(0);
      double longitude = town.getLongitude();
      double latitude = town.getLatitude();

      return new double[] { latitude, longitude };
    }

    return null;
  }

  /**
   * Returns the length of a direct road between the cities,
   * or -1 if there is no such road.
   * 
   * @param city1
   * @param city2
   * @return
   */
  public double getRoadLength(String city1, String city2) {
    if (!states.containsKey(city1) || !states.containsKey(city2)) return -1;
    if (roadExists(city1, city2) != true) return -1;

    // since roads are bidirectional, only need to check one
    int indexOfCity = states.get(city1);
    for (City temp : adj[indexOfCity]) {
      if (temp.getName().equals(city2)) {
        return temp.getWeight();
      }
    }

    return -1;
  }

  /**
   * Gets a list of all adjacent cities to the given one. It may return an array
   * of length 0
   * if the city is isolated.
   */
  public String[] getNeighboringCities(String city) {
    String[] adjCities = new String[49];

    int indexOfCity = states.get(city);
    int currentIndex = 0;

    // iterate through adjacency list
    for (City temp : adj[indexOfCity]) {
      adjCities[currentIndex] = temp.getName();
      currentIndex++;
    }

    // return copy without extra null values
    return Arrays.copyOf(adjCities, currentIndex);
  }
}