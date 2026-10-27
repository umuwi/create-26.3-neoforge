package com.zurrtum.create.client.blauncher;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import java.nio.file.Files;
import java.nio.file.Path;

// Плашка «порт от BLauncher» в главном меню. Порт Create на 26.3 сделан для BLauncher; в чужих лаунчерах
// показываем ненавязчивую подпись со ссылкой — без блокировок и без сбора данных.
// BLauncher при запуске игры передаёт -Dblauncher.launcher=true (GameLaunchService) — тогда плашки нет.
// Игрок может скрыть её крестиком: отметка хранится в config/create/blauncher_notice_hidden.
public final class BLauncherNotice {
    public static final String SITE = "https://blauncher.ru";
    private static final String LAUNCHER_PROPERTY = "blauncher.launcher";

    private BLauncherNotice() {
    }

    public static boolean shouldShow() {
        return !launchedByBLauncher() && !Files.exists(hiddenMarker());
    }

    public static boolean launchedByBLauncher() {
        return Boolean.parseBoolean(System.getProperty(LAUNCHER_PROPERTY));
    }

    public static void hideForever() {
        Path marker = hiddenMarker();
        try {
            Files.createDirectories(marker.getParent());
            Files.writeString(marker, "Плашка BLauncher скрыта игроком. Удалите файл, чтобы вернуть.\n");
        } catch (IOException ignored) {
            // Не записали — плашка просто появится снова при следующем запуске.
        }
    }

    private static Path hiddenMarker() {
        return FabricLoader.getInstance().getConfigDir().resolve("create").resolve("blauncher_notice_hidden");
    }

    // Сколько строк версии внизу слева: у ванили (Fabric) одна, NeoForge выводит свои через BrandingControl
    // (версия NeoForge, число модов, иногда напоминание об обновлении) — плашку ставим над ними.
    // Класс NeoForge внутренний и есть только там, поэтому через отражение; при любой ошибке — одна строка.
    public static int brandingLines() {
        try {
            Method forEachLine = Class.forName("net.neoforged.neoforge.internal.BrandingControl")
                .getMethod("forEachLine", boolean.class, boolean.class, BiConsumer.class);
            int[] count = {0};
            BiConsumer<Integer, String> counter = (line, text) -> count[0]++;
            forEachLine.invoke(null, true, true, counter);
            return Math.max(1, count[0]);
        } catch (ReflectiveOperationException | LinkageError e) {
            return 1;
        }
    }
}
