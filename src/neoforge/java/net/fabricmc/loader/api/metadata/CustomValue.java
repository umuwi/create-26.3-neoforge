package net.fabricmc.loader.api.metadata;

import java.util.Map;

// Пользовательские поля fabric.mod.json; у модов NeoForge их нет, ModMetadata.containsCustomValue всегда false.
public interface CustomValue {
    CvType getType();

    CvObject getAsObject();

    String getAsString();

    enum CvType {
        OBJECT, ARRAY, STRING, NUMBER, BOOLEAN, NULL
    }

    interface CvObject extends CustomValue, Iterable<Map.Entry<String, CustomValue>> {
        int size();

        boolean containsKey(String key);

        CustomValue get(String key);
    }
}
