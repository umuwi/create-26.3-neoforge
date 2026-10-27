package net.fabricmc.loader.api;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;

// Часть Fabric Loader API, которой пользуется Create Fly, поверх FML (см. NeoFabricLoader).
public interface FabricLoader {
    static FabricLoader getInstance() {
        return com.zurrtum.create.neoforge.NeoFabricLoader.INSTANCE;
    }

    boolean isModLoaded(String id);

    Optional<ModContainer> getModContainer(String id);

    Collection<ModContainer> getAllMods();

    Path getConfigDir();

    Path getGameDir();

    boolean isDevelopmentEnvironment();

    EnvType getEnvironmentType();

    <T> List<EntrypointContainer<T>> getEntrypointContainers(String key, Class<T> type);

    <T> List<T> getEntrypoints(String key, Class<T> type);
}
