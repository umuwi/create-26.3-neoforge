package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.infrastructure.gui.OpenCreateMenuButton;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Кнопка-очки Create в меню паузы (см. OpenCreateMenuButton).
@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin extends Screen {
    protected PauseScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init()V", at = @At("TAIL"))
    private void create$addMenuButton(CallbackInfo ci) {
        OpenCreateMenuButton button = OpenCreateMenuButton.forScreen(this, children());
        if (button != null) {
            addRenderableWidget(button);
        }
    }
}
