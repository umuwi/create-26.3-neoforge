package net.fabricmc.api;

// Замена Fabric API для NeoForge-сборки порта: код Create Fly остаётся общим для обоих загрузчиков,
// а вызывает эти методы вход @Mod (com.zurrtum.create.neoforge.CreateNeoForge).
public interface ModInitializer {
    void onInitialize();
}
