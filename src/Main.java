public class Main {

    public static void main(String[] args) {
        Tree t = new Tree();
        t.insert(2);
        System.out.println("Hello World!");
    }
}

class Result {
    private boolean exists;
    private int value;
    public Result(boolean exists, int value) {
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
    int balance;
    int size;
    int halpha;
    int depth;
    Node left;
    Node right;
    Node parent;
    Node sibling;

    public Node(int key) {
        this.key = key;
    }

    public Result search(int key) {
        if (this.key == key) return new Result(true, key);
        else if (this.key > key && this.left != null) return this.left.search(key);
        else if (this.key < key && this.right != null) return this.right.search(key);
        else return new Result(false, 0);
    }

    //runs in O(1) time
    public void calcDepth() {
        if (this.parent == null) this.depth = 0;
        else this.depth = this.parent.depth + 1;
    }

    //runs in O(lg(n)) time
    public void calcHeight(Result prev) {
        if (!prev.exists()) this.height = 0;

        else if (this.height < 1 + prev.getValue()) this.height = 1 + prev.getValue();

        if (this.parent != null) {
            prev.setExistence(true);
            prev.setValue(this.height);
            this.parent.calcHeight(prev);
        }
    }

    public void calcSizeandHalpha(boolean isBottom, double alpha) {
        if (isBottom) this.size = 1;
        else this.size = this.size += 1;
        this.calcHalpha(alpha);
        if (this.parent != null) calcSizeandHalpha(false, alpha);
    }

    public void calcHalpha(double alpha) {
        this.halpha = (int) (Math.floor(this.size) / Math.floor(1.0 / alpha));
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

    public void insert(Node newkey, double alpha) {
        if (this.key < newkey.key) {
            if (this.right == null) {
                this.right = newkey;
                newkey.parent = this;
                newkey.sibling = this.left;
                newkey.left = newkey.right = null;
                newkey.calcDepth();
                newkey.calcHeight(new Result(false, -1));
                newkey.calcSizeandHalpha(true, alpha);
            }
            else this.right.insert(newkey, alpha);
        } else {
            if (this.left == null) {
                this.left = newkey;
                newkey.parent = this;
                newkey.sibling = this.right;
                newkey.left = newkey.right = null;
                newkey.calcDepth();
                newkey.calcHeight(new Result(false, -1));
                newkey.calcSizeandHalpha(true, alpha);
            }
            else this.left.insert(newkey, alpha);
        }
    }

    /*
    Support function for delete(int key). Finds the successor of key (key of current node this).
    return - Returns the successor node of key. If none found, null is returned.
     */
    private Node extract_successor() {
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
        iter.right.sibling = iter.sibling;
        iter.right.parent = iter.parent;
        //TODO recalculate halpha, depths, and heights.
        return iter;
    }

    /*
    removes key from the tree.
    int key - key to be removed
    return - false if key is not found. true if found and deleted.
     */
    public boolean delete(int key) {
        /* if key is found, remove it */
        if (this.key == key) { //TODO recalculate halpha, depths, and heights.
            /* determine if parent exists */
            boolean parentExists = this.parent != null;
            /* determine if child is right or left of parent */
            boolean isRight = (parentExists && this.parent.right == this);
            /* find the successor */
            Node successor = this.extract_successor();
            /* if no successor then just replace with left child */
            if (successor == null) {
                /* when parent doe not exist, set parent to null */
                if (!parentExists) this.left.parent = null;
                /* determine where to put the left (right or left of parent) */
                else if (isRight) {
                    this.parent.right = this.left;
                    this.left.parent = this.parent;
                }
                else {
                    this.parent.left = this.left;
                    this.left.parent = this.parent;
                }
            }
            /* if successor is found, replace with the successor */
            else this.key = successor.key;

            return true;
        }
        /* look in the right subtree if key is larger */
        else if (this.key < key && this.right != null) return this.right.delete(key);
        /* look in the left subtree if key is smaller */
        else if (this.key > key && this.left != null) return this.left.delete(key);
        /* no node is found. */
        else return false;
    }
}

class Tree {
    private Node root;
    private double alpha;
    private int maxSize;

    public void insert(int key) {
        Node newkey = new Node(key);
        root.insert(newkey, this.alpha);
        if(newkey.height > this.root.halpha) {
            Node scapegoat = this.root.findScapeGoat(newkey.key);
            RebuildTree(newkey.size, scapegoat);
        }
    }

    public void RebuildTree(int nsize, Node scapegoat) {

    }

    public void delete(int key) {
        if (!this.root.delete(key)) return;
        if (this.root.size < (this.maxSize * this.root.halpha));
    }

}
