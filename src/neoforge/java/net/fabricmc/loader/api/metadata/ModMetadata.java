package net.fabricmc.loader.api.metadata;

import net.fabricmc.loader.api.Version;

public interface ModMetadata {
    String getId();

    String getName();

    Version getVersion();

    default boolean containsCustomValue(String key) {
        return false;
    }

    default CustomValue getCustomValue(String key) {
        return null;
    }
}
