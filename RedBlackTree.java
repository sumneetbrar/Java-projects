/**
 * RedBlackTree 
 * 
 * @author Sumneet Brar
 */
public class RedBlackTree<K extends Comparable<K>,V>{

  private Node root; // our root node
  private Node deletedNode;

  // node class
  private class Node {

    private boolean isRed;
    private Node left = null;
    private Node right = null;
    private int subtreeSize = 0;
    private K key;
    private V value;

    private Node(K key, V value, int size, boolean isRed) {
      this.isRed = isRed;
      this.key = key;
      this.value = value;
      this.subtreeSize = size;
    }

  }


  /**
   * This constructor creates an empty red black tree:
   * Just creates a null root. 
   */
  public RedBlackTree() {
    root = null;
  }

  /**
   * This method inserts a new key value pair into the tree. 
   * It assumes that neither the given key nor value are null.
   * 
   * 
   * @param key given key to add
   * @param value given value to add
   */
  public void put(K key, V value) {
    root = findAndAdd(root, key, value);
    root.isRed = false; // make sure the root is black
  }

  /**
   * Private method that does the recursive find process
   * 
   * @param top the given node to start the recursion at
   * @param key given key to add
   * @param value given value to add
   * @return the root with the updated links for tha add it just did
   */
  private Node findAndAdd(Node top, K key, V value) {
    // if the current node is null, we've reached the the bottom of the tree without finding the correct value
    if(top == null) return new Node(key, value, 1, true);

    // depending on the current node's key, recurse down in the proper direction
    int compare = key.compareTo(top.key);

    // if the given key is smaller than the current key, recurse left
    if(compare < 0) top.left = findAndAdd(top.left, key, value);
    // if the given key is greater than the current key, recurse right
    else if(compare > 0) top.right = findAndAdd(top.right, key, value);
    // if we find a key with an equal key, replace its value with the given value
    else top.value = value;

    // while recursing up the tree, fix any issues

    // we might end up with a red node that's the right child of a black node, rotate left
    if(isRed(top.right) && !isRed(top.left)) top = rotateLeft(top);
    // we might end up with a red node being the child of another red node, rotate right
    if(isRed(top.left) && isRed(top.left.left)) top = rotateRight(top);
    // we have 2 red children of a black node, colorFlip!
    if(isRed(top.left) && isRed(top.right)) colorFlip(top);

    // recalculate the current node's size from its children's sizes
    top.subtreeSize = size(top.left) + size(top.right) + 1;

    return top;
  }

  
  /**
   * Returns the corresponding value to the given key or null if key
   * is not present.
   * 
   * Does checking if a node is null have the intended effect?
   * Or should I be checking if it's key and value are null?
   * 
   * @param key
   * @return
   */
  public V get(K key) {
    Node currentNode = root;

    // only run the loop while the current node is not null
    while (currentNode != null) {
      // compare the given key with the current node's key
      int compare = key.compareTo(currentNode.key);
      
      // if we enter an if statement, do its commands and then jump to the top of the loop
      if(compare < 0) currentNode = currentNode.left;
      else if(compare > 0) currentNode = currentNode.right;

      // we only reach this line of code if we found the key - neither of the if statements activated
      else return currentNode.value;
    }

    return null;
  }

  /**
   * Removes a key-value pair, returning the deleted value.
   * Returns null if the key wasn’t present.
   * 
   * @param key
   * @return
   */
  public V delete(K key) {
    if (root == null || !containsKey(key)) return null;
    root.isRed = true; // make the root red to take redness with us
    root = findAndDelete(root, key);
    if(root != null) root.isRed = false; // make sure the root is black

    // deleted node should have been updated if we successfully deleted the key, or null if not
    if(deletedNode != null) return deletedNode.value; 
    else return null;
  }

  // need to set deleted node to deleted node in somewhere
  private Node findAndDelete(Node currentNode, K key) {
    // every node we enter has to be red - bring redness down
    // we need to recalculate compare each time so value is up to date

    // we need to go left
    if(key.compareTo(currentNode.key) < 0) {
      // if the left node and left's child is not red, color flip to bring redness with us
      if(!isRed(currentNode.left) && !isRed(currentNode.left.left)) {
        colorFlip(currentNode);
        // if the right node's left child is red, rotate right then left to bring the red node up so we can enter it
        if(isRed(currentNode.right.left)) {
          currentNode.right = rotateRight(currentNode.right);
          currentNode = rotateLeft(currentNode);
          colorFlip(currentNode); // color flip to make the children red instead of the parent
        }
      }
      currentNode.left = findAndDelete(currentNode.left, key); // recurse left
    }

    // key to delete is in the right subtree
    else {
      // move redness right if it exists in left
      if(isRed(currentNode.left)) currentNode = rotateRight(currentNode);
      
      // the current node has the proper key and it has a left red leaf, just return null
      // could've found key
      if(key.compareTo(currentNode.key) == 0 && currentNode.right == null) {
        deletedNode = currentNode;
        return null;
      }
      
      // right and its left child need to be red
      if(!isRed(currentNode.right) && !isRed(currentNode.right.left)) {
        colorFlip(currentNode);
        if(isRed(currentNode.left.left)) {
          currentNode = rotateRight(currentNode);
          colorFlip(currentNode);
        }
      }

      // we found the key here or either above
      if(key.compareTo(currentNode.key) == 0) {
        deletedNode = currentNode;
        Node min = findMinNode(currentNode.right);
        currentNode.key = min.key;
        currentNode.value = min.value;

        // call private function th delete the smallest node
        currentNode.right = deleteMinNode(currentNode.right);
      }
      else currentNode.right = findAndDelete(currentNode.right, key); // recurse right
    }

    // recalculate the current node's size from its children's sizes
    currentNode.subtreeSize = size(currentNode.left) + size(currentNode.right) + 1;
    return fixForDelete(currentNode);
  }

  private Node deleteMinNode(Node node) {
    if (node.left == null) return null;

    if (!isRed(node.left) && !isRed(node.left.left)) {
      colorFlip(node);
      if(isRed(node.left.left)) {
      node = rotateRight(node);
      colorFlip(node);
      }
    }
        
    node.left = deleteMinNode(node.left);
    // recalculate the current node's size from its children's sizes
    node.subtreeSize = size(node.left) + size(node.right) + 1;
    return fixForDelete(node);
  }
  

  /**
   * Returns true if the key is present.
   * 
   * Does checking if a node is null have the intended effect?
   * Or should I be checking if it's key and value are null?
   * 
   * 
   * @param key
   * @return
   */
  public boolean containsKey(K key) {
    Node currentNode = root;
    boolean containsKey = false; // boolean value that will update if we find the key

    // only run the loop if we haven't found the key and if the current node is not null
    while (containsKey == false && currentNode != null) {
      // compare the key values
      int compare = key.compareTo(currentNode.key);
      
      // if we enter an if statement, do that specfic command and jump up to top to verify
      // that the new node is still not null.
      if(compare < 0) currentNode = currentNode.left;
      else if(compare > 0) currentNode = currentNode.right;
      else containsKey = true; // we only reach this line of code if we've found the key
    }

    return containsKey;
  }

  /**
   * Returns true if the value is present.
   * 
   * Search every node comparing the value to the given value?
   * 
   * @param value
   * @return
   */
  public boolean containsValue(V value) {
    return containsV(root, value);
  }

  public boolean containsV(Node currentNode, V value) {
    if (currentNode == null) return false;

    // comapare the value of the currentNode with the given value
    if(currentNode.value.equals(value)) return true;

    // recurse down the tree comparing every node's value
    boolean left = containsV(currentNode.left, value);
    boolean right = containsV(currentNode.right, value);

    if (left != false) return true; // if left subtree found it, return true
    else if (right != false) return true; // if right subtree found it, return true
    else return false; // we didn't find it
  }

  /**
   * Returns true if the tree is empty.
   * 
   * @return
   */
  public boolean isEmpty() {
    if (root == null) return true;
    else return false;
  }

  /**
   * Returns n, the number of key-value pairs in the tree.
   * 
   * @return
   */
  public int size() {return size(root);}

  private int size(Node node) {
    // if the given node is not null, return the node's subtree size
    if (node == null) return 0;
    else return node.subtreeSize;
  }

  /**
   * Finds a key that maps to the given value, or returns null
   * if there is none.
   * 
   * @param value
   * @return
   */
  public K reverseLookup(V value) {
    return reverseLookup(root, value).key;
  }

  private Node reverseLookup(Node currentNode, V value) {
    if(currentNode == null) return null;
    // comapare the value of the currentNode with the given value
    if(currentNode.value == value) return currentNode;

    // recurse down the tree comparing every node's value
    Node left = reverseLookup(currentNode.left, value);
    Node right = reverseLookup(currentNode.right, value);

    if (left != null) {return left;} // if left is not null, we found it in the left subtree - return the node stored in left
    else if (right != null) {return right;} // if right is not null - return the node stored in right
    else return null; // we didn't find the value :( - return null
  }

  /**
   * Returns the key that is less than all the others.
   * Or null if none.
   * 
   * @return
   */
  public K findFirstKey() {
    return findMinNode(root).key;
  }

  /**
   * Find the smallest node
   * 
   * @param node
   * @return
   */
  private Node findMinNode(Node node) {
    // the smallest node will be the leftmost - keep traveling left as far as possible
    while(node != null) {
      if(node.left == null) return node; // we hit the end, return the current node
      else node = node.left;
    }
    return null; // the root was null - return null
  }

  /**
   * Returns the key that is greater than all the others.
   * Or null if none.
   * 
   * @return
   */
  public K findLastKey() {
    Node currentNode = root;

    // greatest key will be rightmost - travel right as far as possible
    while(currentNode != null) {
      if(currentNode.right == null) return currentNode.key;
      else currentNode = currentNode.right;
    }

    return null; // the root is null - return null
  }


  /**
   * Returns the key contained in the root.
   * Or null if none.
   * 
   * @return
   */
  public K getRootKey() {
    if(root != null) return root.key;
    else return null;
  }

  /**
   * Returns the predecessor of the given key, or null
   * if the key is not present or has no predecssor.
   * 
   * If a node has a left child, its predecessor is obtained by traveling left once, 
   * and then right as much as possible.
   * Otherwise, it is the most recent ancestor from which we traveled right.
   * 
   * @param key
   * @return
   */
  public K findPredecessor(K key) {

    // first find the node
    if (!containsKey(key)) return null; // see if the key is in the tree

    Node currentNode = root;
    Node predecessor = null; 

    while (currentNode != null) {
      int compare = key.compareTo(currentNode.key);
      
      // if we enter an if statement, do its commands and then jump to the top of the loop
      if(compare < 0) {
        currentNode = currentNode.left;
      }
      else if(compare > 0) {
        predecessor = currentNode; // keep track of the most recent ancestor from which we traveled right
        currentNode = currentNode.right;
      }

      // we only reach this segment of code if we found the key - neither of the if statements activated
      else {
        // travel left once
        if(currentNode.left != null) {
          currentNode = currentNode.left;
          // then right as much as possible
          while(currentNode.right != null) {
          currentNode = currentNode.right;
          }
          return currentNode.key;
        }
        else {
          // the node does not have a left subtree - it's predecessor is the most recent from which we traveled right
          if(predecessor != null) {
            return predecessor.key;
          }
          else return null;
        }
      }
    }
    return null; // the key doesn't exist
  }

  /**
   * Returns the successor of the given key, or null
   * if the key is not present or has no successor.
   * 
   * Same logic as predecessor but in reverse
   * 
   * @param key
   * @return
   */
  public K findSuccessor(K key) {

    // first find the node
    if (!containsKey(key)) return null; // see if the node is in the tree

    Node currentNode = root;
    Node successor = null; 

    while (currentNode != null) {
      int compare = key.compareTo(currentNode.key);
      
      // if we enter an if statement, do its commands and then jump to the top of the loop
      if(compare < 0) {
        successor = currentNode; // keep track of the most recent ancestor from which we traveled left
        currentNode = currentNode.left;
      }
      else if(compare > 0) {
        currentNode = currentNode.right;
      }

      // we only reach this segment of code if we found the key - neither of the if statements activated
      else {
        // if there is a right subtree, travel right once
        if(currentNode.right != null) {
          currentNode = currentNode.right; 
          // and then left as far as possible
          while(currentNode.left != null) {
          currentNode = currentNode.left;
          }
          return currentNode.key;
        }
        else {
          // the node does not have a right subtree - return the most recent ancestor from which we traveled right
          if(successor != null) {
            return successor.key;
          }
          else return null;
        }
      }
    }
  
    return null; // the key doesn't exist
  }

  /**
   * Returns the rank of the given key, or -1 if the key is
   * not present. 
   * 
   * @param key
   * @return
   */
  public int findRank(K key) {
    if(!containsKey(key)) return -1; // is the key present
    Node currentNode = root;
    int rank = 0; // keep track of the rank

    while (currentNode != null) {
      int compare = key.compareTo(currentNode.key);
      
      // going left - do nothing
      if(compare < 0) currentNode = currentNode.left;

      // going right - add the size of the subtree + 1
      else if(compare > 0) {
        rank += size(currentNode.left) + 1;
        currentNode = currentNode.right;
      }

      // found the key - add the size of the node's left subtree
      else {
        return rank + size(currentNode.left);
      }
    }
    return 0;
  }

  /**
   * Returns the word with the given rank or null when the 
   * rank is invalid. 
   * 
   * this could be a loop maybe? instead of recursion
   * 
   * @param rank
   * @return
   */
  public K select(int rank) {
    // make sure the rank is valid
    if (rank < 0 || rank >= size()) {
      throw new IllegalArgumentException("Rank is out of bounds!");
    }
    return select(root, rank);
    }

  private K select(Node node, int rank) {
    while(node != null) {
      int size = size(node.left);

      if(rank < size) node = node.left;
      else if(size == rank) return node.key;
      else {
        rank = rank - (size + 1);
        node = node.right;
      } 
    }
    return null;
  }

  /**
   * Returns the number of red nodes in the tree
   * 
   * @return
   */
  public int countRedNodes() {return countRedNodes(root);}

  private int countRedNodes(Node node) {
    if(node == null) return 0;

    int redNodes = 0;
    if(isRed(node)) redNodes = 1;

    redNodes += countRedNodes(node.left);
    redNodes += countRedNodes(node.right);

    return redNodes;
  }

  /**
   * Returns the height of the tree, where an empty tree
   * has height 0.
   * 
   * Recursive function: https://stackoverflow.com/questions/20037137/how-to-calculate-the-height-of-a-red-black-tree
   * 
   * @return
   */
  public int calcHeight() {
    return calcHeight(root);
  }

  // has to be recursive 
  private int calcHeight(Node node) {
    if(node == null) return 0;

    int left = calcHeight(node.left);
    int right = calcHeight(node.right);

    return Math.max(left, right) + 1;
  }

  /**
   * Returns the black height of the tree, or 0 for an
   * empty tree.
   * 
   * @return
   */
  public int calcBlackHeight() {
    if(isEmpty()) return 0;
    
    Node node = root;
    int blackHeight = 0;

    // every path should have the same black length - so pick one
    while(node != null) {
      if(!isRed(node)) blackHeight++;
      node = node.left;
    }

    return blackHeight;
  }

  /**
   * Returns the average distance of the nodes from the root. 
   * Empty trees should return NaN.
   * 
   * @return
   */
  public double calcAverageDepth() {
    if(isEmpty()) return Double.NaN; // empty tree, return NaN

    int sumOfDepth = calcTotalDepth(root, 0);
    int numOfNodes = size(root); // calculate the number of nodes in the tree

    return (double) sumOfDepth / numOfNodes; // divide sum of depth and the number of nodes to get average
  }

  private int calcTotalDepth(Node node, int currentDepth) {
    if(node == null) return 0;

    // recurse on left and right subtrees, keeping track of the depth
    int left = calcTotalDepth(node.left, currentDepth + 1);
    int right = calcTotalDepth(node.right, currentDepth + 1);

    return currentDepth + left + right; // add the two numbers and return the sum
  }



  /**
   * Fix any issues in the tree
   * 
   * @param currentNode
   * @return
   */
  private Node fixForDelete(Node currentNode) {
    // we might end up with 2 red children of a black node, color flip
    if(isRed(currentNode.left) && isRed(currentNode.right)) colorFlip(currentNode);
    // we might end up with a red node that's the right child of a black node, rotate left
    if(isRed(currentNode.right) && !isRed(currentNode.left)) currentNode = rotateLeft(currentNode);
    // we might end up with a red node being the child of another red node, rotate right
    if(isRed(currentNode.left) && isRed(currentNode.left.left)) currentNode = rotateRight(currentNode);
    // we have 2 red children of a black node, colorFlip!
    if(isRed(currentNode.left) && isRed(currentNode.right)) colorFlip(currentNode);

    return currentNode;
  }

  /**
   * Assuming that we have a right-leaning red node so we rotate it to
   * lean to the left
   * 
   * inspired from the book's code
   * 
   * @param oldRoot the given root of the (sub)tree
   * @return the new root of this (sub)tree
   */
  private Node rotateLeft(Node oldRoot) {
    // the newRoot is the problematic right red child
    Node newRoot = oldRoot.right; 

    // the oldRoot's right link should be the newRoot's previous left link 
    oldRoot.right = newRoot.left; 

    // the newRoot's left link is the oldRoot bc we did a rotateLeft
    newRoot.left = oldRoot; 

    // newRoot has same color value as oldRoot
    newRoot.isRed = oldRoot.isRed; 

    // oldRoot being the left child should be red
    oldRoot.isRed = true; 

    // since newRoot is in the same position, its subtree size will be the same as oldRoot's
    newRoot.subtreeSize = oldRoot.subtreeSize; 

    // calculate new subtree size of oldRoot
    oldRoot.subtreeSize = size(oldRoot.left) + size(oldRoot.right) + 1; 

    // return the node now at the root position of this (sub)tree
    return newRoot;
  }

  /**
   * In the case a red left child also has a red left child, we rotate the subtree 
   * right to get rid of the violation
   * 
   * @param oldRoot
   * @return the new root of this (sub)tree
   */
  private Node rotateRight(Node oldRoot) {
    // same logic as rotateLeft
    Node newRoot = oldRoot.left;
    oldRoot.left = newRoot.right;
    newRoot.right = oldRoot;

    newRoot.isRed = oldRoot.isRed;
    oldRoot.isRed = true;  // color flip would get called right after this

    newRoot.subtreeSize = oldRoot.subtreeSize;
    oldRoot.subtreeSize = size(oldRoot.left) + size(oldRoot.right) + 1;

    return newRoot;
  }

  /**
   * Give the node and its children the opposite of their original colors
   * 
   * @param top
   */
  private void colorFlip(Node parent) {
    parent.isRed = !parent.isRed;
    parent.left.isRed = !parent.left.isRed;
    parent.right.isRed = !parent.right.isRed;
  }

  /**
   * Need this method to prevent exceptions when node doesn't exist
   * 
   * @param node
   * @return
   */
  private boolean isRed(Node node) {
    if(node == null) return false;
    return node.isRed == true;
  }
}