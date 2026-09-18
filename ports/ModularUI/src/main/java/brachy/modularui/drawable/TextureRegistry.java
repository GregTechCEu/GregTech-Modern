package brachy.modularui.drawable;

import brachy.modularui.ModularUI;
import brachy.modularui.ModularUIConfig;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class TextureRegistry {

    private static final Map<String, UITexture> TEXTURES = new Object2ObjectOpenHashMap<>();
    private static final Map<UITexture, String> REVERSE_TEXTURES = new Object2ObjectOpenHashMap<>();

    private static synchronized void registerTextureInternal(String name, UITexture texture) {
        if (texture == null) return;
        UITexture current = TEXTURES.put(name, texture);
        REVERSE_TEXTURES.put(texture, name);
        if (current != null && (ModularUI.isDev() || ModularUIConfig.DEBUG_UI.get())) {
            ModularUI.LOGGER.warn("[DEBUG] Replacing texture with name '{}' and location '{}' with texture with location '{}'", name, current.location, texture.location);
        }
    }

    public static synchronized void registerTexture(String name, UITexture texture) {
        String current = REVERSE_TEXTURES.get(texture);
        if (current != null) {
            if (name != null && !current.equals(name)) {
                TEXTURES.put(name, texture);
                REVERSE_TEXTURES.put(texture, name);
                TEXTURES.remove(current);
            }
            return;
        }
        if (name == null) {
            registerTextureAutoName(texture);
        } else {
            registerTextureInternal(name, texture);
        }
    }

    public static void registerTextureAutoName(UITexture texture) {
        if (texture == null) return;
        String[] p = texture.location.getPath().split("/");
        p = p[p.length - 1].split("\\.");
        String baseName = texture.location.getNamespace() + ":" + p[0];
        String name = baseName;
        int number = 0;
        UITexture current;
        while ((current = TEXTURES.get(name)) != null) {
            if (current.equals(texture)) return;
            number++;
            name = baseName + "_" + number;
            if (number == 20 && (ModularUI.isDev() || ModularUIConfig.DEBUG_UI.get())) {
                ModularUI.LOGGER.warn("[DEBUG] Trying to register a UITexture with location '{}' for at least 20 times. This is likely a bug and should be fixed.", texture.location);
            } else if (number == 10000) {
                throw new IllegalStateException("Trying to register a UITexture with location '" + texture.location + "' 10000 times.");
            }
        }
        registerTexture(name, texture);
    }

    @Nullable
    public static UITexture getTexture(String s) {
        return TEXTURES.get(s);
    }

    @Nullable
    public static String getTextureId(UITexture texture) {
        return REVERSE_TEXTURES.get(texture);
    }
}
