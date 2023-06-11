package bearmaps.proj2c;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MyTrieSet implements TrieSet61B {

    private Node root;

    private static class Node {
        private boolean isKey;
        private HashMap<Character, Node> children;

        private Node(boolean b) {
            this.isKey = b;
            this.children = new HashMap<>();
        }

    }

    public MyTrieSet() {
        this.root = new Node(false);
    }

    @Override
    /** Clears all items out of Trie */
    public void clear() {
        this.root = new Node(false);
    }

    @Override
    /** Returns true if the Trie contains KEY, false otherwise */
    public boolean contains(String key) {
        Node curr = root;
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            if (!curr.children.containsKey(c)) {
                return false;
            }
            curr = curr.children.get(c);
        }
        return curr.isKey;
    }

    @Override
    /** Inserts string KEY into Trie */
    public void add(String key) {
        if (key == null || key.length() < 1) {
            return;
        }
        Node curr = this.root;
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            if (!curr.children.containsKey(c)) {
                curr.children.put(c, new Node(false));
            }
            curr = curr.children.get(c);
        }
        curr.isKey = true;
    }

    @Override
    /** Returns a list of all words that start with PREFIX */
    public List<String> keysWithPrefix(String prefix) {
        ArrayList<String> keys = new ArrayList<>();
        if (this.contains(prefix)) {
            keys.add(prefix);
        }
        Node curr = root;
        for (int i = 0; i < prefix.length(); i++) {
            char c = prefix.charAt(i);
            curr = curr.children.get(c);
        }
        for (char c : curr.children.keySet()) {
            colHelp(prefix + c, keys, curr.children.get(c));
        }
        return keys;
    }

    private void colHelp(String s, List<String> x, Node n) {
        if (n.isKey) {
            x.add(s);
        }
        for (char c : n.children.keySet()) {
            colHelp(s + c, x, n.children.get(c));
        }
    }

    @Override
    /** Returns the longest prefix of KEY that exists in the Trie
     * Not required for Lab 9. If you don't implement this, throw an
     * UnsupportedOperationException.
     */
    public String longestPrefixOf(String key) {
        throw new UnsupportedOperationException("longestPrefixOf not supported");
    }

}
