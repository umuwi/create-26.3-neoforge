package com.zurrtum.create.neoforge.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// NeoForge создаёт моды раньше, чем Minecraft (Minecraft.getInstance() ещё null), а клиентская
// инициализация Create рассчитана на момент, когда Fabric зовёт ClientModInitializer: внутри
// Minecraft.<init> сразу после записи gameDirectory. Повторяем ту же точку.
@Mixin(Minecraft.class)
public class MinecraftInitMixin {
    @Inject(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;gameDirectory:Ljava/io/File;", opcode = org.objectweb.asm.Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    private void create$initClient(GameConfig gameConfig, CallbackInfo ci) {
        new com.zurrtum.create.client.Create().onInitializeClient();
    }
}
