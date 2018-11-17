import java.io.File;
import java.util.Scanner;


public class Main {

    public static void main(String[] args) {
        Tree tree = null;
        String file;
        if (args.length >= 1) file = args[0];
        else file = "/Users/johnkarasev/IdeaProjects/scapegoat-tree/src/tree.txt";
        Scanner scan;
        try {
            scan = new Scanner(new File(file));
        } catch (Exception e) {
            System.out.println("Error" + e);
            return;
        }
        for (int line_num = 1; scan.hasNextLine(); line_num++) {
            String[] next = scan.nextLine().replaceAll(",", "").split("\\s+");
            switch (next[0]) { //TODO print the keys in success.
                case "BuildTree":
                    tree = new Tree(Integer.parseInt(next[2]), Double.parseDouble(next[1]));
                    printResult(line_num, "Success: Tree built");
                    break;
                case "Insert":
                    tree.insert(Integer.parseInt(next[1]));
                    printResult(line_num, "Success: Key inserted");
                    break;
                case "Search":
                    Result res = tree.search(Integer.parseInt(next[1]));
                    if (!res.exists()) printResult(line_num, "Error: key not found");
                    else printResult(line_num, "Success: key found");
                    break;
                case "Delete":
                    boolean r = tree.delete(Integer.parseInt(next[1]));
                    if(r) printResult(line_num, "Success: key deleted");
                    else printResult(line_num, "Error: Key not found");
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

class Node {

    int key;
    int height;
    int size;
    int halpha;
    Node left;
    Node right;
    Node parent;

    public Node(int key) {
        this.key = key;
        this.parent = this.left = this.right = null;
    }

    public Result search(int key) {
        if (this.key == key) return new Result(true, key);
        else if (this.key > key && this.left != null) return this.left.search(key);
        else if (this.key < key && this.right != null) return this.right.search(key);
        else return new Result(false, 0);
    }


    private void calcHeight() {
        /* if is a leaf, set height to 0. */
        if (this.right == null && this.left == null) this.height = 0;
        else {
            int righth = -1, lefth = -1;
            if (this.right != null) righth = this.right.height;
            if (this.left != null) lefth = this.left.height;
            /* Compare if new height is more than previous one. */
            if (lefth < righth) this.height = 1 + righth;
            else this.height = 1 + lefth;
        }
    }

    private void calcHalpha(double alpha) {
        this.halpha = (int) (Math.floor(this.size) / Math.floor(1.0 / alpha));
    }

    private void calcSize() {
        this.size = 1;
        if (this.right != null) this.size += this.right.size;
        if (this.left != null) this.size += this.left.size;
    }

    public void updateNode(double alpha) {
        this.calcSize();
        this.calcHalpha(alpha);
        this.calcHeight();
    }

    public void update(double alpha) {
        this.updateNode(alpha);
        if (this.parent != null) this.parent.update(alpha);
    }




    //runs in O(log(n)) time
    public Node findScapeGoat(int key) {
        if (this.key != key) {
            if (this.height > this.halpha) return this;
            if (this.key > key && this.left != null) return this.left.findScapeGoat(key);
            if (this.key < key && this.right != null) return this.right.findScapeGoat(key);
        }
        return null;
    }

    // This function assumes that at least one node is in the tree.
    // assume also that there are no duplicate keys.
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

    /*
    Support function for delete(int key). Finds the successor of key (key of current node this).
    return - Returns the successor node of key. If none found, null is returned.
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
        if (iter.right != null) {
            iter.right.parent = iter.parent;
            iter.right.update(alpha);
        } else iter.parent.update(alpha);

        return iter;
    }

    /*
    removes key from the tree.
    int key - key to be removed
    return - false if key is not found. true if found and deleted.
     */
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
                    if (this.left != null) this.left.parent = null;
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
                //recalculate sizes, h_alphas and heights.
                if (this.left != null) this.left.update(alpha);
                else if (this.parent != null) this.parent.update(alpha);
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

    public void print(int level) {
        if (this.right != null) this.right.print(level + 1);
        System.out.println();
        for(int i = level; i >= 0; i--) System.out.print("\t");
        System.out.print(this.key);
        System.out.flush();
        if (this.left != null) this.left.print(level + 1);
    }
}

class Tree {
    private Node root;
    private double alpha;
    private int maxSize;

    Tree(int key, double alpha) {
        this.alpha = alpha;
        this.root = new Node(key);
        this.root.parent = this.root.left = this.root.right = null;
        this.root.update(this.alpha);
        this.maxSize = 1;
    }

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
            this.maxSize = 1;
            this.root.update(this.alpha);
        }
        /* check if the new node is a deep node. If a deep node, rebuild the tree. */
        if(depth > this.root.halpha) {
            /* Find a scapegoat that is closest to root or is root (root is prioritized over all other nodes) */
            Node scapegoat = this.root.findScapeGoat(newkey.key);
            /* Record the parent of scapegoat to know where to insert the rebuilt tree */
            Node scapegoat_parent = null;
            /* Check if parent exists, if doesn't keep it null otherwise set to parent */
            if (scapegoat.parent != null)  scapegoat_parent = scapegoat.parent;
            /* Determine if scapegoat child is left or right of parent */
            boolean isRight = (scapegoat_parent != null && scapegoat_parent.right == scapegoat);
            /* Rebuild the tree using procedure in the scapegoat paper (Scapegoat Trees Chapter 19 Galperin and
             * Rivest) */
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
            /* reset the maxsize to tree size */
            this.maxSize = this.root.size;
        }
        /* update maxsize */
        else this.maxSize = Math.max(this.root.size, this.maxSize);
    }

    /* Node flatten(Node x, Node y)
     * Converts a subtree into a linked list, ordered from least to greatest with the right attribute pointing to the
     * next node. The dummy variable is attached to the head of the list (greatest node points to dummy variable) */
    private static Node flatten(Node x, Node y) {
        if (x == null) return y;
        x.right = flatten(x.right, y);
        return flatten(x.left, x);
    }

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
        Node r = this.buildTree((int) (Math.floor(((double) (n - 1)) / 2.0)), x);
        Node s = this.buildTree((int) (Math.ceil(((double) (n - 1)) / 2.0)), r.right);
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

    public boolean delete(int key) {
        /* if tree is empty return false */
        if (this.root == null) return false;
        /* if key is not found, return false */
        if (!this.root.delete(key, this.alpha)) return false;
        /* if the deleted key is the root, set root to null (only is true when the last key is deleted. */
        if (this.root.key == key) this.root = null;
        /* check for the rebuild condition. */
        if (this.root != null && this.root.size < (this.maxSize * this.alpha)) {
            this.root = rebuildTree(this.root.size, this.root);
            /* Get rid of the dummy pointer */
            this.root.parent = null;
            /* reset maxsize */
            this.maxSize = this.root.size;
        }
        return true;
    }

    public Result search(int key) {
        if (this.root != null) return this.root.search(key);
        else return new Result(false, -1);
    }

    public void print() {
        if (this.root != null) this.root.print(0);
        System.out.println();
    }

}
