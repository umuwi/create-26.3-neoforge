package com.zurrtum.create.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

// Вход мода на NeoForge: вызывает те же инициализаторы, что Fabric по точкам входа main/client.
@Mod("create")
public final class CreateNeoForge {
    public CreateNeoForge(IEventBus modBus, ModContainer container, Dist dist) {
        new com.zurrtum.create.Create().onInitialize();
        if (dist.isClient()) {
            new com.zurrtum.create.client.Create().onInitializeClient();
        }
    }
}
