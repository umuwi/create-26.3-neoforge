package net.fabricmc.loader.impl.util.version;

import net.fabricmc.loader.api.Version;

public class StringVersion implements Version {
    private final String version;

    public StringVersion(String version) {
        this.version = version;
    }

    @Override
    public String getFriendlyString() {
        return version;
    }

    @Override
    public String toString() {
        return version;
    }
}
