package com.securevault.gui.displayable.keys;

public record Pair(String websiteName, String id) implements Comparable<Pair> {

    @Override
    public int compareTo(Pair pair) {
        int v = websiteName.compareTo(pair.websiteName());
        return v != 0 ? v : id.compareTo(pair.id());
    }
}
