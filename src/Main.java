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
            switch (next[0]) {
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
                    if(r) printResult(line_num, "Error: key not found");
                    else printResult(line_num, "Success: Key deleted");
                    break;
                case "Print":
                    printResult(line_num, "");
                    tree.print();
                    break;
                case "Done":
                    return;
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
    int balance;
    int size;
    int halpha;
    int depth;
    Node left;
    Node right;
    Node parent;
    Node brother; //might not need this node. //TODO remove if not used.

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
            if (this.height > this.halpha) return this; //TODO height or depth?
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
                newkey.brother = this.left;
                newkey.left = newkey.right = null;
                newkey.update(alpha);
                return count + 1;
            }
            else return this.right.insert(newkey, alpha, count + 1);
        } else {
            if (this.left == null) {
                this.left = newkey;
                newkey.parent = this;
                newkey.brother = this.right;
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
        iter.right.brother = iter.brother;
        iter.right.parent = iter.parent;

        iter.right.update(alpha);

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
                //recalculate sizes, h_alphas and heights.
                this.left.update(alpha);
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
    }

    public void insert(int key) {
        Node newkey = new Node(key);
        int depth = root.insert(newkey, this.alpha, 0);
        if(depth > this.root.halpha) {
            Node scapegoat = this.root.findScapeGoat(newkey.key);
            Node scapegoat_parent = null;
            if (scapegoat.parent != null)  scapegoat_parent = scapegoat.parent;
            boolean isRight = (scapegoat_parent != null && scapegoat_parent.right == scapegoat);
            Node balanced = rebuildTree(newkey.size, scapegoat);
            if (scapegoat_parent != null) {
                //TODO add brother if necessary.
                if (isRight) scapegoat_parent.right = balanced;
                else scapegoat_parent.left = balanced;
                balanced.parent = scapegoat_parent;
                balanced.parent.update(this.alpha);
            } else this.root = balanced;
            /* reset the maxsize to tree size */
            this.maxSize = this.root.size;
        }
        /* update maxsize */
        else this.maxSize = Math.max(this.root.size, this.maxSize);
    }

    private static Node flatten(Node x, Node y) {
        if (x == null) return y;
        x.right = flatten(x.right, y);
        return flatten(x.left, x);
    }

    //TODO check if brother needs to be set.
    private Node buildTree(int n, Node x) {
        if (n == 0) {
            x.left = null;
            return x;
        }
        Node r = this.buildTree((int) (Math.floor(((double) (n - 1)) / 2.0)), x);
        Node s = this.buildTree((int) (Math.ceil(((double) (n - 1)) / 2.0)), r.right);
        r.right = s.left;
        s.left = r;
        /* update parents */
        if(r.right != null) r.right.parent = r;
        if(r.left != null) r.left.parent = r;
        /* update the size, halpha, and height */
        r.updateNode(this.alpha);
        return s;
    }

    public Node rebuildTree(int n, Node scapegoat) {
        Node w = new Node(-1);
        Node z = Tree.flatten(scapegoat, w);
        this.buildTree(n, z);
        return w.left;
    }

    public boolean delete(int key) {
        if (!this.root.delete(key, this.alpha)) return false;
        if (this.root.size < (this.maxSize * this.root.halpha)) {
            this.root = rebuildTree(this.root.size, this.root);
            this.maxSize = this.root.size;
        }
        return true;
    }

    public Result search(int key) {
        return this.root.search(key);
    }

    public void print() {
        this.root.print(0);
        System.out.println();
    }



}
