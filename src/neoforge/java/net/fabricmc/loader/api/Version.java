package net.fabricmc.loader.api;

public interface Version extends Comparable<Version> {
    String getFriendlyString();

    @Override
    default int compareTo(Version other) {
        return getFriendlyString().compareTo(other.getFriendlyString());
    }
}
