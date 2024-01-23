import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Cuckoo Hashing
 * 
 * I could not get this to work for extremely large text files (such as alice.txt)
 * because it would run out of memory.  
 * 
 * @author Sumneet Brar
 */
public class Cuckoo<K, V> {

  private static final double maxLoadFactor = 2.0; // Max load factor
  private static final double minLoadFactor = 0.1; // Min load factor

  private Node<K, V>[] table1;
  private Node<K, V>[] table2;

  private int tableSize; // Current size of the array

  /**
   * Creates the hash table, using the default initial size of 11.
   */
  public Cuckoo() {
    this(13);
  }

  /**
   * Creates the hash table, with the initial internal array size as given.
   * 
   * @param initialSize
   */
  @SuppressWarnings("unchecked")
  public Cuckoo(int initialSize) {
    table1 = (Node<K, V>[]) new Node[initialSize];
    table2 = (Node<K, V>[]) new Node[initialSize];
    tableSize = 0;
  }

  /**
   * Inserts a new key-value pair.
   * 
   * @param key
   * @param value
   */
  public void put(K key, V value) {
    if (key == null)
      throw new IllegalArgumentException("Key cannot be null.");

    if (containsKey(key)) delete(key);

    if (getLoadFactor() > maxLoadFactor) rehash();
    if (getLoadFactor() < minLoadFactor) rehash();

    Node<K, V> newNode = new Node<>(key, value);

    int position1 = hash1(key);
    int position2 = hash2(key);

    // Try to place the node in table1
    if (table1[position1] == null) {
      table1[position1] = newNode;
      tableSize++;
      return;
    }

    // If table1 is occupied, try to place the node in table2
    if (table2[position2] == null) {
      table2[position2] = newNode;
      tableSize++;
      return;
    }

    // If both positions are occupied, start the cuckoo process
    Node<K, V> displaced = table1[position1];
    table1[position1] = newNode;
    boolean displaceFromTable1 = true;

    for (int i = 0; i < table1.length; i++) {
      // Calculate the locations for the displaced key
      position1 = hash1(displaced.key);
      position2 = hash2(displaced.key);

      if (table1[position1] == null) {
        table1[position1] = displaced;
        tableSize++;
        return;
      }

      if (table2[position2] == null) {
        table2[position2] = displaced;
        tableSize++;
        return;
      }

      // Both of the displaced key's positions are also occupied.
      if (displaceFromTable1) {
        Node<K, V> kicked = table1[position1];
        table1[position1] = displaced;
        displaced = kicked;
      } else {
        Node<K, V> kicked = table2[position2];
        table2[position2] = displaced;
        displaced = kicked;
      }

      // Alternate the table to displace from.
      displaceFromTable1 = !displaceFromTable1;
    }

    // If we reach this point, it means we have a cycle. We need to rehash.
    rehash();
    put(key, value);
  }

  private int hash1(K key) {
    return Math.abs(key.hashCode()) % table1.length;
  }

  private int hash2(K key) {
    int prime = 17;
    return Math.abs((prime * key.hashCode())) % table2.length;
  }

  /**
   * This method takes up way too much memory.
   */
  private void rehash() {
    Node[] oldTable1 = table1;
    Node[] oldTable2 = table2;

    table1 = new Node[oldTable1.length * 2];
    table2 = new Node[oldTable2.length * 2];

    tableSize = 0;

    for (Node<K, V> node : oldTable1) {
      if (node != null) {
        put(node.key, node.value);
      }
    }

    for (Node<K, V> node : oldTable2) {
      if (node != null) {
        put(node.key, node.value);
      }
    }
  }

  // also runs out of memory
  private void altRehash() {
    // Save the existing entries in a temporary list
    List<Node<K, V>> oldEntries = new ArrayList<>();
    for (Node<K, V> node : table1) {
      if (node != null) {
        oldEntries.add(node);
      }
    }
    for (Node<K, V> node : table2) {
      if (node != null) {
        oldEntries.add(node);
      }
    }

    // Clear the tables and resize them
    table1 = Arrays.copyOf(table1, table1.length * 2);
    table2 = Arrays.copyOf(table2, table2.length * 2);

    // Reset the size
    tableSize = 0;

    // Reinsert the entries into the resized tables
    for (Node<K, V> node : oldEntries) {
      put(node.key, node.value);
    }
  }

  /**
   * Returns the value corresponding to the given key, or null if the key is not
   * present.
   * 
   * @param key
   * @return
   */
  public V get(K key) {
    if (key == null)
      throw new IllegalArgumentException("Key cannot be null.");

    int position1 = hash1(key);
    int position2 = hash2(key);

    // check if key is in table 1
    if (table1[position1] != null && table1[position1].key.equals(key)) {
      return table1[position1].value;
    }
    // check table 2
    if (table2[position2] != null && table2[position2].key.equals(key)) {
      return table2[position2].value;
    }
    return null; // key is not present
  }

  /**
   * Removes a key-value pair, returning the deleted value.
   * Returns null if the key wasn’t present.
   * 
   * @param key
   * @return
   */
  public V delete(K key) {
    if (key == null)
      throw new IllegalArgumentException("Key cannot be null.");

    int position1 = hash1(key);
    int position2 = hash2(key);

    if (table1[position1] != null && table1[position1].key.equals(key)) {
      V value = table1[position1].value;
      table1[position1] = null;
      tableSize--;
      return value;
    }
    if (table2[position2] != null && table2[position2].key.equals(key)) {
      V value = table2[position2].value;
      table2[position2] = null;
      tableSize--;
      return value;
    }
    return null; // key wasn't present
  }

  /**
   * Searches for the key, and returns true if it is present.
   * 
   * @param key
   * @return
   */
  public boolean containsKey(K key) {
    if (key == null)
      throw new IllegalArgumentException("Key cannot be null.");

    int position1 = hash1(key);
    int position2 = hash2(key);

    // check if key is in table 1
    if (table1[position1] != null && table1[position1].key.equals(key)) {
      return true;
    }
    // check table 2
    if (table2[position2] != null && table2[position2].key.equals(key)) {
      return true;
    }
    return false;
  }

  /**
   * Searches for the value, and returns true if it is present.
   * 
   * @param value
   * @return
   */
  public boolean containsValue(V value) {
    for (int i = 0; i < table1.length; i++) {
      if (table1[i] != null && table1[i].value.equals(value)) {
        return true;
      }
    }
    for (int i = 0; i < table2.length; i++) {
      if (table2[i] != null && table2[i].value.equals(value)) {
        return true;
      }
    }
    return false; // Value not found
  }

  /**
   * Returns true if both tables are empty.
   * 
   * @return true or false
   */
  public boolean isEmpty() {
    if (tableSize == 0) {
      return true;
    }
    return false;
  }

  /**
   * Returns n, the number of key-value pairs in the table.
   * 
   * @return
   */
  public int size() {
    return tableSize;
  }

  /**
   * Finds a key that maps to the given value, or returns null if there is none.
   * 
   * @param value
   * @return
   */
  public K reverseLookup(V value) {
    for (int i = 0; i < table1.length; i++) {
      if (table1[i] != null && table1[i].value.equals(value)) {
        return table1[i].key;
      }
    }
    for (int i = 0; i < table2.length; i++) {
      if (table2[i] != null && table2[i].value.equals(value)) {
        return table2[i].key;
      }
    }
    return null; // Value not found
  }

  /**
   * Returns m, the size of the primary internal array
   * 
   * @return
   */
  public int getTableSize() {
    return table1.length; // table 1 and 2 should be the same length
  }

  /**
   * Returns α, which is n/m.
   * 
   * @return
   */
  public double getLoadFactor() {
    return (double) tableSize / table1.length;
  }

  /**
   * counts the number of chains with no entries.
   * 
   * @return
   */
  public int countEmptySlots() {
    int count = 0;
    for (Node node : table1) {
      if (node == null) {
        count++;
      }
    }
    for (Node node : table2) {
      if (node == null) {
        count++;
      }
    }
    return count;
  }

  /**
   * returns the length of the longest chain.
   * 
   * @return
   */
  public int findLongestRunLength() {
    int longestRun = 0;
    int currentRun = 0;

    for (Node node : table1) {
      if (node != null) {
        currentRun++;
        if (currentRun > longestRun)
          longestRun = currentRun;
      } else
        currentRun = 0;
    }

    currentRun = 0;
    for (Node node : table2) {
      if (node != null) {
        currentRun++;
        if (currentRun > longestRun)
          longestRun = currentRun;
      } else
        currentRun = 0;
    }

    return longestRun;
  }

  private class Node<K, V> {
    private K key;
    private V value;

    public Node(K key, V value) {
      this.key = key;
      this.value = value;
    }

    public K getKey() {
      return key;
    }

    public V getValue() {
      return value;
    }

    public void setValue(V value) {
      this.value = value;
    }
  }
}
