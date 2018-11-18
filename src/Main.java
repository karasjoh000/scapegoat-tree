/* Author John Karasev Copyright 2018*/

import java.io.File;
import java.util.Scanner;


public class Main {

    /*Main function that reads through the file and executes commands. */
    public static void main(String[] args) {
        Tree tree = null;
        String file;
        /* Determine if file is specified in directory or in args. */
        if (args.length >= 1) file = args[0];
        else file = "./tree.txt";
        Scanner scan;
        try {
            scan = new Scanner(new File(file));
        } catch (Exception e) {
            System.out.println("Error" + e);
            return;
        }
        /* For each line in file, parse them and check what command to execute. */
        for (int line_num = 1; scan.hasNextLine(); line_num++) {
            int key;
            double alpha;
            String[] next = scan.nextLine().replaceAll(",", "").split("\\s+");
            switch (next[0]) {
                case "BuildTree":
                    key = Integer.parseInt(next[2]);
                    alpha = Double.parseDouble(next[1]);
                    tree = new Tree(key, alpha);
                    printResult(line_num, "Success: Tree built with " + key + " key and " + alpha + " as alpha");
                    break;
                case "Insert":
                    key = Integer.parseInt(next[1]);
                    tree.insert(key);
                    printResult(line_num, "Success: " + key + " inserted");
                    break;
                case "Search":
                    key = Integer.parseInt(next[1]);
                    Result res = tree.search(key);
                    if (!res.exists()) printResult(line_num, "Error: " + key + " not found");
                    else printResult(line_num, "Success: " + key + " found");
                    break;
                case "Delete":
                    key = Integer.parseInt(next[1]);
                    boolean r = tree.delete(key);
                    if(r) printResult(line_num, "Success: " + key + " deleted");
                    else printResult(line_num, "Error: " + key + " not found");
                    break;
                case "Print":
                    printResult(line_num, "");
                    tree.print();
                    break;
                case "Done":
                    return;
                default:
                    System.out.println("Error: Incorrect format for tree.txt");

            }
        }

    }

    private static void printResult(int line_num, String mesg) {
        System.out.println("[" + line_num + "]" + " " + mesg);
    }


}

/* Just stores the result and has a flag if result exists. Used in the search method of tree. */
class Result {
    private boolean exists;
    private int value;
    Result(boolean exists, int value) {
        this.value = value;
        this.exists = exists;
    }
    boolean exists() {
        return this.exists;
    }

    int getValue() {
        return this.value;
    }

    void setExistence(boolean exists) {
        this.exists = exists;
    }

    void setValue(int value) {
        this.value = value;
    }
}

/* Represents a single node in the scapegoat tree. */
class Node {

    int key; // The value at the node
    int height; //height of the node.
    int size; // number of nodes of subtree rooted at this
    int halpha; //4.4 Remarks, second bullet point.
    Node left; // left child of node
    Node right; //right child of node
    Node parent; //parent of node


    public Node(int key) {
        this.key = key;
        this.parent = this.left = this.right = null;
    }

    /* Searches for a key. Returns Result that specifies if found or not found. */
    public Result search(int key) {
        if (this.key == key) return new Result(true, key);
        else if (this.key > key && this.left != null) return this.left.search(key);
        else if (this.key < key && this.right != null) return this.right.search(key);
        else return new Result(false, 0);
    }

    /* calculates the height of this. Runs in O(1) time. */
    private void calcHeight() {
        /* if is a leaf, set height to 0. */
        if (this.right == null && this.left == null) this.height = 0;
        else {
            int righth = -1, lefth = -1;
            /* determine the previous heights. */
            if (this.right != null) righth = this.right.height;
            if (this.left != null) lefth = this.left.height;
            /* increment to the maximum height */
            this.height = 1 + Math.max(lefth, righth);
        }
    }

    /* calculates the alpha height of a node. */
    //change of base log_a(b) = log(b) / log(a)
    private void calcHalpha(double alpha) {
        this.halpha = (int) Math.floor(Math.log((double) this.size) / Math.log(1.0 / alpha));
    }

    /* counts the number of nodes in the subtree rooted at this*/
    private void calcSize() {
        this.size = 1;
        if (this.right != null) this.size += this.right.size;
        if (this.left != null) this.size += this.left.size;
    }

    /* Performs the tree operations above */
    public void updateNode(double alpha) {
        this.calcSize();
        this.calcHalpha(alpha);
        this.calcHeight();
    }

    /* Updates node attributes all the way up to parent. Runs in O(log(n)) time, so does not harm the performance. */
    public void update(double alpha) {
        this.updateNode(alpha);
        if (this.parent != null) this.parent.update(alpha);
    }



    /* finds the scapegoat */
    // it can be shown that scapegoat will always be the root if searched from the top.
    // runs in O(log(n)) time
    // starts looking from the root.
    public Node findScapeGoat(int key) {
        if (this.key != key) {
            /* Inequality (4.6) */
            if (this.height > this.halpha) return this;
            if (this.key > key && this.left != null) return this.left.findScapeGoat(key);
            if (this.key < key && this.right != null) return this.right.findScapeGoat(key);
        }
        return null;
    }

    /* Inserts a new node into the tree. Ignore duplicates. count keeps track of the depth. */
    public int insert(Node newkey, double alpha, int count) {
        if (this.key < newkey.key) {
            if (this.right == null) {
                this.right = newkey;
                newkey.parent = this;
                newkey.left = newkey.right = null;
                newkey.update(alpha);
                return count + 1;
            }
            else return this.right.insert(newkey, alpha, count + 1);
        } else {
            if (this.left == null) {
                this.left = newkey;
                newkey.parent = this;
                newkey.left = newkey.right = null;
                newkey.update(alpha);
                return count + 1;
            }
            else return this.left.insert(newkey, alpha, count + 1);
        }
    }

    //CHECKED.
    /* Support function for delete(int key). Finds the successor of key (key of current node this).
     * return - Returns the successor node of key. If none found, null is returned.
     */
    private Node extract_successor(double alpha) {
        /* indicator if current node is left or right child for its parent. */
        boolean isRight = true;
        /* if no successor, return false */
        if (this.right == null) return null;
        /* determine if successor will be right of left from its parent */
        if (this.right.left != null) isRight = false;
        /* traverse along the left children to find the successor */
        Node iter = this.right;
        while (iter.left != null) iter = iter.left;
        /* Determine whether successor is left or right child of parent, then prepare fields to extract
         * the successor. */
        if (isRight) iter.parent.right = iter.right;
        else iter.parent.left = iter.right;
        /* check if right is null or not to setup parent and update fields */
        if (iter.right != null) iter.right.parent = iter.parent;
        iter.parent.update(alpha);
        return iter;
    }

    //CHECKED.
    /* removes key from the tree.
     * int key - key to be removed
     * return - false if key is not found. true if found and deleted. */
    public boolean delete(int key, double alpha) {
        /* if key is found, remove it */
        if (this.key == key) {
            /* determine if parent exists */
            boolean parentExists = this.parent != null;
            /* determine if child is right or left of parent */
            boolean isRight = (parentExists && this.parent.right == this);
            /* find the successor */
            Node successor = this.extract_successor(alpha);
            /* if no successor then just replace with left child */
            if (successor == null) {
                /* when parent doe not exist, set parent to null */
                if (!parentExists) {
                    /* When parent is null, set left to tree root. Just change this attributes so Tree.root will be
                     * pointing to the correct position. */
                    if (this.left != null) {
                        /* Make a temporary variable to keep it independent from pointer manipulations */
                        Node left = this.left;
                        this.key = left.key;
                        this.left = left.left;
                        this.right = left.right;
                        this.size = left.size;
                        this.height = left.height;
                        this.halpha = left.halpha;
                    }
                }
                /* determine where to put the left (right or left of parent) */
                else if (isRight) {
                    this.parent.right = this.left;
                    if (this.left != null) this.left.parent = this.parent;
                }
                else {
                    this.parent.left = this.left;
                    if (this.left != null) this.left.parent = this.parent;
                }
                /* recalculate sizes, h_alphas and heights. */
                if (this.parent != null) this.parent.update(alpha);
            }
            /* if successor is found, replace with the successor */
            else {
                //size, h_alpha or height is not changed.
                this.key = successor.key;
            }
            return true;
        }
        /* look in the right subtree if key is larger */
        else if (this.key < key && this.right != null) return this.right.delete(key, alpha);
        /* look in the left subtree if key is smaller */
        else if (this.key > key && this.left != null) return this.left.delete(key, alpha);
        /* no node is found. */
        else return false;
    }

    /* Provided in the assignment description */
    public void print(int level) {
        if (this.right != null) this.right.print(level + 1);
        System.out.println();
        for(int i = level; i >= 0; i--) System.out.print("\t");
        System.out.print(this.key);
        System.out.flush();
        if (this.left != null) this.left.print(level + 1);
    }
}

/* points to the root of the subtree. */
class Tree {
    private Node root; // root of the tree
    private double alpha; // see scapegoat spec
    private int maxSize; // see scapegoat spec

    Tree(int key, double alpha) {
        this.alpha = alpha;
        this.root = new Node(key);
        this.root.parent = this.root.left = this.root.right = null;
        this.root.update(this.alpha);
        this.maxSize = 1;
    }

    //Checked.
    public void insert(int key) {
        /* create a new node that will store the new key */
        Node newkey = new Node(key);
        /* insert the new node using regular BST insertion, and record the depth of the newly inserted node.
         * Check the case for root being null. */
        int depth = 0;
        if (this.root != null) depth = this.root.insert(newkey, this.alpha, 0);
        else {
            this.root = newkey;
            this.root.parent = this.root.left = this.root.right = null;
            this.root.update(this.alpha);
        }
        /* check if the new node is a deep node. If a deep node, rebuild the tree. */
        if(depth > this.root.halpha) { // Lemma 5.1
            /* Find a scapegoat that is closest to root or is root (root is prioritized over all other nodes) */
            Node scapegoat = this.root.findScapeGoat(newkey.key);
            /* Record the parent of scapegoat to know where to insert the rebuilt tree */
            Node scapegoat_parent = scapegoat.parent;
            /* Determine if scapegoat child is left or right of parent */
            boolean isRight = (scapegoat_parent != null && scapegoat_parent.right == scapegoat);
            /* Rebuild the tree using procedure in the scapegoat paper (Scapegoat Trees Chapter 19 Galperin and
             * Rivest). This will rebuild the subtree rooted at the scapegoat (4.2 3rd paragraph). */
            Node balanced = rebuildTree(scapegoat.size, scapegoat);
            /* determine how to merge the balanced subtree (is it root or someones child) */
            if (scapegoat_parent != null) {
                /* set up parameters and update sizes, heights, and halphas (which takes O(log(n)) time.) */
                if (isRight) scapegoat_parent.right = balanced;
                else scapegoat_parent.left = balanced;
                balanced.parent = scapegoat_parent;
                balanced.parent.update(this.alpha);
            } else  {
                /* no scapegoat parent implies that the balanced node (indicating the subtree that was just balanced)
                 * is the root node. */
                this.root = balanced;
                /* Make sure the roots parent is null and not pointing to the dummy variable w in rebuildTree */
                this.root.parent = null;
            }
            /* reset the maxsize to tree size. Remark 4.4 first bullet point */
            this.maxSize = this.root.size;
        }
        /* update maxsize. 4.2 first paragraph. */
        else this.maxSize = Math.max(this.root.size, this.maxSize);
    }

    //CHECKED.
    /* Node flatten(Node x, Node y)
     * Converts a subtree into a linked list, ordered from least to greatest with the right attribute pointing to the
     * next node. The dummy variable is attached to the head of the list (greatest node points to dummy variable) */
    private static Node flatten(Node x, Node y) {
        if (x == null) return y;
        x.right = flatten(x.right, y);
        return flatten(x.left, x);
    }

    //CHECKED
    /* Node buildTree(int n, Node x)
     * recursive function that builds a perfectly balanced subtree.
     * int n - size of the scapegoat subtree
     * Node x - linked list from the flatten method. */
    private Node buildTree(int n, Node x) {
        /* see Scapegoat Trees Chapter 19 Galperin and Rivest to understand what is happening here. */
        if (n == 0) {
            x.left = null;
            return x;
        }
        /* create subtrees */
        Node r = this.buildTree((int) (Math.ceil(((double) (n - 1)) / 2.0)), x);
        Node s = this.buildTree((int) (Math.floor(((double) (n - 1)) / 2.0)), r.right);
        /* Merge r and s. r will not be touched anymore so it will be safe to set fields on it as will as
         * calculating height, size, and halphas. */
        r.right = s.left;
        s.left = r;
        /* update parents */
        r.parent = s;
        if(r.right != null) r.right.parent = r;
        if(r.left != null) r.left.parent = r;
        /* update the size, halpha, and height */
        r.updateNode(this.alpha);
        return s;
    }


    /* Node rebuildTree(int n, Node scapegoat)
     * int n - size of the scapegoat tree.
     * Node scapegoat - root of the subtree to be rebuilt.
     * This function is an auxiliary to the buildTree method, by setting up a linked list and passing it into the
     * buildTree method. Returns the left child of the dummy variable where the balanced tree is stored. */
    public Node rebuildTree(int n, Node scapegoat) {
        /* create the dummy variable */
        Node w = new Node(-1);
        /* create a linked list from the subtree */
        Node z = Tree.flatten(scapegoat, w);
        /* rebuild the tree so it will be perfectly balanced. */
        this.buildTree(n, z);
        /* return the left child of dummy variable where the balanced tree is stored. */
        return w.left;
    }

    /* deletes node from tree, rebuilds if necessary */
    public boolean delete(int key) {
        /* if tree is empty return false */
        if (this.root == null) return false;
        /* if key is not found, return false */
        if (!this.root.delete(key, this.alpha)) return false;
        /* if the deleted key is the root, set root to null (only is true when the last key is deleted. */
        if (this.root.key == key) {
            this.root = null;
            this.maxSize = 0;
        }
        /* check for the rebuild condition. Inequality 4.7 */
        if (this.root != null && this.root.size < (this.maxSize * this.alpha)) {
            this.root = rebuildTree(this.root.size, this.root);
            /* Get rid of the dummy pointer */
            this.root.parent = null;
            /* reset maxsize. Remarks 4.4 second bullet point. */
            this.maxSize = this.root.size;
        }
        return true;
    }

    /* Searches for a node, if found returns Result with the key and exist flag set to true. Otherwise exists flag is
    * set to false. */
    public Result search(int key) {
        if (this.root != null) return this.root.search(key);
        else return new Result(false, -1);
    }

    /* Prints the tree. */
    public void print() {
        if (this.root != null) this.root.print(0);
        System.out.println();
    }

}
