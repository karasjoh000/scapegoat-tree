public class Main {

    public static void main(String[] args) {
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

    public Node(Node left, Node right, Node parent, Node sibling) {
        this.left = left;
        this.right = right;
        this.parent = parent;
        this.sibling = sibling;
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

    public void calcSize(boolean isBottom) {
        if (isBottom) this.size = 1;
        else this.size = this.size += 1;
        if (this.parent != null) this.parent.calcSize(false);
    }
}

class Tree {
    Node root;
    double alpha;


}
