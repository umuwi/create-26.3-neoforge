package com.zurrtum.create.neoforge.mixin.client;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.neoforged.neoforge.client.gui.PictureInPictureRendererPool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Пул NeoForge держит по рендереру на элемент GUI и закрывает лишние каждый кадр. Рендереры Create —
// одиночки с общими статическими текстурами (как в ванили), CreateNeoForgeClient отдаёт пулу один и тот же
// экземпляр; закрытие одного «лишнего» закрывало текстуру, которой рисует остальное (27.10.2026: вылет
// «Texture view Sampler0 (UI Item Transform texture) has been closed» при открытии Ponder).
@Mixin(PictureInPictureRendererPool.class)
public class PictureInPictureRendererPoolMixin {
    @Shadow
    private Object2ObjectMap<?, PictureInPictureRenderer<?>> renderersLastFrame;

    @Shadow
    private Object2ObjectMap<?, PictureInPictureRenderer<?>> renderersThisFrame;

    @Inject(method = "clearUnusedRenderers", at = @At("HEAD"))
    private void create$keepShared(CallbackInfo ci) {
        renderersLastFrame.values().removeIf(PictureInPictureRendererPoolMixin::create$isShared);
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void create$keepSharedOnClose(CallbackInfo ci) {
        renderersLastFrame.values().removeIf(PictureInPictureRendererPoolMixin::create$isShared);
        renderersThisFrame.values().removeIf(PictureInPictureRendererPoolMixin::create$isShared);
    }

    @Unique
    private static boolean create$isShared(PictureInPictureRenderer<?> renderer) {
        return renderer.getClass().getName().startsWith("com.zurrtum.create.");
    }
}
