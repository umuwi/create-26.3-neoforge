package com.zurrtum.create.neoforge;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.impl.util.version.StringVersion;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.neoforgespi.language.IModInfo;

// Fabric Loader API поверх FML. Данные берём из LoadingModList, а не ModList: Create спрашивает
// isModLoaded уже из миксин-плагинов, когда ModList ещё не собран.
public final class NeoFabricLoader implements FabricLoader {
    public static final NeoFabricLoader INSTANCE = new NeoFabricLoader();

    private NeoFabricLoader() {
    }

    @Override
    public boolean isModLoaded(String id) {
        // Fabric API и Fabric Loader на NeoForge не бывает: Create Fly тогда работает в «ванильном режиме».
        if (id.equals("fabricloader") || id.startsWith("fabric-") || id.equals("fabric")) {
            return false;
        }
        if (id.equals("minecraft") || id.equals("java")) {
            return true;
        }
        LoadingModList list = LoadingModList.get();
        return list != null && list.getModFileById(id) != null;
    }

    @Override
    public Optional<ModContainer> getModContainer(String id) {
        LoadingModList list = LoadingModList.get();
        if (list == null) {
            return Optional.empty();
        }
        for (IModInfo info : list.getMods()) {
            if (info.getModId().equals(id)) {
                return Optional.of(new Container(info));
            }
        }
        return Optional.empty();
    }

    @Override
    public Collection<ModContainer> getAllMods() {
        List<ModContainer> mods = new ArrayList<>();
        LoadingModList list = LoadingModList.get();
        if (list != null) {
            for (IModInfo info : list.getMods()) {
                mods.add(new Container(info));
            }
        }
        return mods;
    }

    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public Path getGameDir() {
        return FMLPaths.GAMEDIR.get();
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLEnvironment.isProduction();
    }

    @Override
    public EnvType getEnvironmentType() {
        return FMLEnvironment.getDist().isClient() ? EnvType.CLIENT : EnvType.SERVER;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<EntrypointContainer<T>> getEntrypointContainers(String key, Class<T> type) {
        // Точки входа из fabric.mod.json Create; у других модов на NeoForge их нет.
        String className = switch (key) {
            case "create_plugin" -> "com.zurrtum.create.AllEarlyRegistries";
            default -> null;
        };
        if (className == null) {
            return List.of();
        }
        try {
            T entry = (T) Class.forName(className).getDeclaredConstructor().newInstance();
            ModContainer provider = getModContainer("create").orElseThrow();
            return List.of(new EntrypointContainer<>() {
                @Override
                public T getEntrypoint() {
                    return entry;
                }

                @Override
                public ModContainer getProvider() {
                    return provider;
                }
            });
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Не удалось создать точку входа " + className, e);
        }
    }

    @Override
    public <T> List<T> getEntrypoints(String key, Class<T> type) {
        return getEntrypointContainers(key, type).stream().map(EntrypointContainer::getEntrypoint).toList();
    }

    private record Container(IModInfo info) implements ModContainer, ModMetadata {
        @Override
        public ModMetadata getMetadata() {
            return this;
        }

        @Override
        public List<Path> getRootPaths() {
            return List.copyOf(info.getOwningFile().getFile().getContents().getContentRoots());
        }

        @Override
        public String getId() {
            return info.getModId();
        }

        @Override
        public String getName() {
            return info.getDisplayName();
        }

        @Override
        public Version getVersion() {
            return new StringVersion(info.getVersion().toString());
        }
    }
}
