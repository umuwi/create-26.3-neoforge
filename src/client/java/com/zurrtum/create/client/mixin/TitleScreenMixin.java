package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.blauncher.BLauncherNotice;
import com.zurrtum.create.client.blauncher.BLauncherNoticeWidget;
import com.zurrtum.create.client.infrastructure.gui.OpenCreateMenuButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Плашка «порт от BLauncher» в главном меню — слева внизу, над строками версии (см. BLauncherNotice).
@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Component title) {
        super(title);
    }

    // Кнопка-очки Create слева от «Сетевая игра», как в оригинале (см. OpenCreateMenuButton).
    @Inject(method = "init()V", at = @At("TAIL"))
    private void create$addMenuButton(CallbackInfo ci) {
        OpenCreateMenuButton button = OpenCreateMenuButton.forScreen(this, children());
        if (button != null) {
            addRenderableWidget(button);
        }
    }

    @Inject(method = "init()V", at = @At("TAIL"))
    private void create$addBLauncherNotice(CallbackInfo ci) {
        if (!BLauncherNotice.shouldShow()) {
            return;
        }
        // Над верхней строкой версии (их считает BLauncherNotice.brandingLines).
        int bottom = height - (10 + (BLauncherNotice.brandingLines() - 1) * (font.lineHeight + 1)) - 2;
        BLauncherNoticeWidget[] notice = new BLauncherNoticeWidget[1];
        notice[0] = addRenderableWidget(new BLauncherNoticeWidget(this, font, 2, bottom, () -> removeWidget(notice[0])));
    }
}
