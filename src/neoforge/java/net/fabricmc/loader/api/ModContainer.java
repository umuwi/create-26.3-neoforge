package net.fabricmc.loader.api;

import java.nio.file.Path;
import java.util.List;
import net.fabricmc.loader.api.metadata.ModMetadata;

public interface ModContainer {
    ModMetadata getMetadata();

    List<Path> getRootPaths();
}
