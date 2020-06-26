Scape-goat binary search tree 
===============================
### This is a scape-goat binary search tree. See the pdf for details on how it works. 


Future improvments 
===================
By lemma 5.1, If a node at depth greather then T.root.halpha then there is an alpha-weight-unbalanced ancestor of x. 

scapegoat is found by x.height > x.halpha (inequality 4.6). This implies: T.root.height >= d[x] > T.root.halpha, and T.root.height > T.root.halpha. 
This means that the scapegoat will always be the root if searched from the top. 

Usage 
==================

This is a scape-goat tree, see spec or assignment sheet for more details.

To build: javac Main.java
To run: java Main <absolute path to tree.txt OPTIONAL>


`src/Main.java` - source code of the scapegoat tree along with the main function that parses the tree.txt and executes the commands  
`src/tree.txt` - sample tree.txt file. 