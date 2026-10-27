package net.fabricmc.api;

// См. ModInitializer: на NeoForge вызывается из CreateNeoForge только на клиенте.
public interface ClientModInitializer {
    void onInitializeClient();
}
