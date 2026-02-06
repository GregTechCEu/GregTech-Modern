package com.gregtechceu.gtceu.api.mui.theme;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.base.ITheme;
import com.gregtechceu.gtceu.api.mui.base.IThemeApi;
import com.gregtechceu.gtceu.api.mui.utils.*;
import com.gregtechceu.gtceu.utils.serialization.json.JsonBuilder;
import com.gregtechceu.gtceu.utils.serialization.json.JsonHelper;

import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import it.unimi.dsi.fastutil.objects.*;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@ApiStatus.Internal
@OnlyIn(Dist.CLIENT)
public class ThemeManager extends SimplePreparableReloadListener<Map<String, List<ResourceLocation>>> {

    public static final String THEMES_PATH = "themes.json";
    public static final FileToIdConverter THEME_LISTER = new FileToIdConverter("gtceu/themes", ".json");
    protected static final WidgetThemeEntry<WidgetTheme> defaultFallbackWidgetTheme = IThemeApi.get().getDefaultTheme()
            .getWidgetTheme(IThemeApi.FALLBACK);
    private static final JsonObject emptyJson = new JsonObject();

    public ThemeManager() {}

    @Override
    protected @NotNull Map<String, List<ResourceLocation>> prepare(ResourceManager resourceManager,
                                                                   ProfilerFiller profiler) {
        GTCEu.LOGGER.info("Reloading Themes...");
        MinecraftForge.EVENT_BUS.post(new ReloadThemeEvent.Pre());
        ThemeAPI.INSTANCE.onReload();

        Map<String, List<ResourceLocation>> themes = new Object2ObjectOpenHashMap<>();
        profiler.startTick();
        List<String> themeJsonSources = new ArrayList<>();
        for (String namespace : resourceManager.getNamespaces()) {
            profiler.push(namespace);

            for (Resource resource : resourceManager.getResourceStack(new ResourceLocation(namespace, THEMES_PATH))) {
                profiler.push(resource.sourcePackId());
                themeJsonSources.add(resource.sourcePackId());

                JsonElement element;
                try (InputStream stream = resource.open()) {
                    element = JsonHelper.parse(stream);
                } catch (Exception e) {
                    GTCEu.LOGGER.catching(e);
                    continue;
                }
                JsonObject definitions;
                if (!element.isJsonObject()) {
                    continue;
                }
                definitions = element.getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : definitions.entrySet()) {
                    if (entry.getKey().equals("screens")) {
                        if (!entry.getValue().isJsonObject()) {
                            GTCEu.LOGGER.error("Theme screen definitions must be an object!");
                            continue;
                        }
                        loadScreenThemes(entry.getValue().getAsJsonObject());
                        continue;
                    }
                    if (entry.getValue().isJsonObject() || entry.getValue().isJsonArray() ||
                            entry.getValue().isJsonNull()) {
                        GTCEu.LOGGER.throwing(new JsonParseException("Theme must be a string!"));
                        continue;
                    }
                    themes.computeIfAbsent(entry.getKey(), key -> new ArrayList<>())
                            .add(new ResourceLocation(entry.getValue().getAsString()));
                }
                profiler.pop();
            }

            profiler.pop();

        }
        GTCEu.LOGGER.info("Found themes.json's at {}", themeJsonSources);
        return themes;
    }

    @Override
    protected void apply(@NotNull Map<String, List<ResourceLocation>> themes,
                         @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        Map<String, ThemeJson> themeMap = new Object2ObjectOpenHashMap<>();
        profiler.startTick();

        // load json files from the path and parse their parent
        for (Map.Entry<String, List<ResourceLocation>> entry : themes.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            profiler.push(entry.getKey());
            ThemeJson theme = loadThemeJson(entry.getKey(), entry.getValue(), resourceManager, profiler);
            if (theme != null) {
                themeMap.put(entry.getKey(), theme);
            }
            profiler.pop();
        }
        for (Map.Entry<String, List<JsonBuilder>> entry : ThemeAPI.INSTANCE.defaultThemes.entrySet()) {
            if (!themeMap.containsKey(entry.getKey())) {
                themeMap.put(entry.getKey(), new ThemeJson(entry.getKey(),
                        entry.getValue().stream().map(JsonBuilder::getJson).collect(Collectors.toList()), false));
            }
        }
        if (themeMap.isEmpty()) return;
        // yeet any invalid parent declarations
        validateAncestorTree(themeMap);
        if (themeMap.isEmpty()) return;
        // create a sorted list of themes

        Map<String, ThemeJson> sortedThemes = new Object2ObjectLinkedOpenHashMap<>();
        Iterator<Map.Entry<String, ThemeJson>> iterator;
        boolean changed;
        do {
            changed = false;
            iterator = themeMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, ThemeJson> entry = iterator.next();
                if (ThemeAPI.DEFAULT_ID.equals(entry.getValue().parent) ||
                        sortedThemes.containsKey(entry.getValue().parent)) {
                    sortedThemes.put(entry.getKey(), entry.getValue());
                    iterator.remove();
                    changed = true;
                    break;
                }
            }
        } while (changed);

        // finally parse and register themes
        for (ThemeJson themeJson : sortedThemes.values()) {
            Theme theme = themeJson.deserialize();
            ThemeAPI.INSTANCE.registerTheme(theme);
        }

        validateJsonScreenThemes();
        MinecraftForge.EVENT_BUS.post(new ReloadThemeEvent.Post());
    }

    private static void validateAncestorTree(Map<String, ThemeJson> themeMap) {
        Set<ThemeJson> invalidThemes = new ObjectOpenHashSet<>();
        for (ThemeJson theme : themeMap.values()) {
            if (invalidThemes.contains(theme)) {
                continue;
            }
            Set<ThemeJson> parents = new ObjectOpenHashSet<>();
            parents.add(theme);
            ThemeJson parent = theme;
            do {
                if (ThemeAPI.DEFAULT_ID.equals(parent.parent)) {
                    break;
                }
                parent = themeMap.get(parent.parent);
                if (parent == null) {
                    GTCEu.LOGGER.error(
                            "Can't find parent '{}' for theme '{}'! All children for '{}' are therefore invalid!",
                            theme.parent, theme.id, theme.id);
                    invalidThemes.addAll(parents);
                    break;
                }
                if (parents.contains(parent)) {
                    GTCEu.LOGGER.error(
                            "Ancestor tree for themes can't be circular! All of the following make a circle or are children of the circle: {}",
                            parents);
                    invalidThemes.addAll(parents);
                    break;
                }
                if (invalidThemes.contains(parent)) {
                    GTCEu.LOGGER.error(
                            "Parent '{}' was found to be invalid before. All following are children of it and are therefore invalid too: {}",
                            theme.parent, parents);
                    invalidThemes.addAll(parents);
                    break;
                }
                parents.add(parent);
            } while (true);
        }
        for (ThemeJson theme : invalidThemes) {
            themeMap.remove(theme.id);
        }
    }

    private static ThemeJson loadThemeJson(String id, List<ResourceLocation> paths,
                                           @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        List<JsonObject> jsons = new ArrayList<>();
        boolean override = false;
        for (ResourceLocation path : paths) {
            profiler.push(path.toString());
            ResourceLocation rl = THEME_LISTER.idToFile(path);
            Resource resource = resourceManager.getResource(rl).orElse(null);
            if (resource == null) {
                profiler.pop();
                return null;
            }
            JsonElement element;
            try (InputStream stream = resource.open()) {
                element = JsonHelper.parse(stream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (element.isJsonObject()) {
                if (JsonHelper.getBoolean(element.getAsJsonObject(), false, "override")) {
                    jsons.clear();
                    override = true;
                }
                jsons.add(element.getAsJsonObject());
            }
            profiler.pop();
        }
        if (jsons.isEmpty()) {
            GTCEu.LOGGER.throwing(new JsonParseException("Theme must be a JsonObject!"));
            return null;
        }
        return new ThemeJson(id, jsons, override);
    }

    private static void loadScreenThemes(JsonObject json) {
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            if (entry.getValue().isJsonPrimitive()) {
                String theme = entry.getValue().getAsString();
                ThemeAPI.INSTANCE.jsonScreenThemes.put(entry.getKey(), theme);
            } else {
                GTCEu.LOGGER.error("Theme screen definitions must be strings!");
            }
        }
    }

    private static void validateJsonScreenThemes() {
        for (ObjectIterator<Object2ObjectMap.Entry<String, String>> iterator = ThemeAPI.INSTANCE.jsonScreenThemes
                .object2ObjectEntrySet().fastIterator(); iterator.hasNext();) {
            Map.Entry<String, String> entry = iterator.next();
            if (!ThemeAPI.INSTANCE.hasTheme(entry.getValue())) {
                GTCEu.LOGGER.error("Tried to register theme '{}' for screen '{}', but theme does not exist",
                        entry.getValue(), entry.getKey());
                iterator.remove();
            }
        }
    }

    private static class ThemeJson {

        private final String id;
        private final String parent;
        private final List<JsonObject> jsons;
        private final boolean override;

        private ThemeJson(String id, List<JsonObject> jsons, boolean override) {
            this.id = id;
            this.override = override;
            String p = null;
            for (ListIterator<JsonObject> iterator = jsons.listIterator(jsons.size()); iterator.hasPrevious();) {
                JsonObject json = iterator.previous();
                if (json.has(IThemeApi.PARENT)) {
                    p = json.get(IThemeApi.PARENT).getAsString();
                    break;
                }
            }
            this.parent = p == null ? "DEFAULT" : p;
            this.jsons = jsons;
        }

        private Theme deserialize() {
            if (!ThemeAPI.INSTANCE.hasTheme(this.parent)) {
                throw new IllegalStateException(String.format(
                        "Ancestor tree was validated, but parent '%s' was still null during parsing!", this.parent));
            }
            ITheme parent = ThemeAPI.INSTANCE.getTheme(this.parent);
            // merge themes defined in java and via resource pack of the same id into 1 json
            JsonBuilder jsonBuilder = new JsonBuilder();
            if (!this.override) {
                for (JsonBuilder builder : ThemeAPI.INSTANCE.getJavaDefaultThemes(this.id)) {
                    jsonBuilder.addAllOf(builder);
                }
            }
            for (JsonObject json : this.jsons) {
                jsonBuilder.addAllOf(json);
            }

            // parse fallback theme for widget themes
            WidgetThemeMap widgetThemes = new WidgetThemeMap();
            WidgetThemeEntry<?> parentWidgetTheme = parent.getFallback(); // fallback theme of parent
            // fallback theme of new theme
            WidgetTheme fallback = new WidgetTheme(parentWidgetTheme.getTheme(), jsonBuilder.getJson(), null);
            WidgetTheme fallbackHover = fallback;

            JsonObject hoverJson = getJson(jsonBuilder.getJson(), IThemeApi.HOVER_SUFFIX);
            if (hoverJson == null)
                hoverJson = getJson(jsonBuilder.getJson(), IThemeApi.FALLBACK.getFullName() + IThemeApi.HOVER_SUFFIX);
            if (hoverJson != null) {
                fallbackHover = new WidgetTheme(fallback, hoverJson, null);
            }
            widgetThemes.putTheme(IThemeApi.FALLBACK,
                    new WidgetThemeEntry<>(IThemeApi.FALLBACK, fallback, fallbackHover));

            // parse all main widget themes
            for (WidgetThemeKey<?> key : ThemeAPI.INSTANCE.getWidgetThemeKeys()) {
                if (key != IThemeApi.FALLBACK) {
                    parse(widgetThemes, parent, key, jsonBuilder);
                }
            }
            return new Theme(this.id, parent, widgetThemes);
        }

        private <T extends WidgetTheme> void parse(WidgetThemeMap map, ITheme parent, WidgetThemeKey<T> key,
                                                   JsonBuilder json) {
            WidgetThemeParser<T> parser = key.getParser();
            JsonObject widgetThemeJson = getJson(json.getJson(), key.getFullName());
            boolean definedStandard = widgetThemeJson != null;

            JsonObject widgetThemeHoverJson = getJson(json.getJson(), key.getFullName() + IThemeApi.HOVER_SUFFIX);
            boolean definedHover = widgetThemeHoverJson != null;
            if (!definedHover) {
                if (!definedStandard) {
                    // widget theme undefined -> copy from parent
                    if (key.isSubWidgetTheme()) {
                        // if it is a sub widget theme, we use the parent widget theme from this theme
                        WidgetThemeEntry<T> entry = map.getTheme(key.getParent());
                        map.putTheme(key, new WidgetThemeEntry<>(key, entry.getTheme(), entry.getHoverTheme()));
                        return;
                    }
                    // we still need to parse non inherited values (fallback)
                    widgetThemeJson = emptyJson;
                    widgetThemeHoverJson = emptyJson;
                }
            }

            JsonObject fallback = key.isSubWidgetTheme() ? null : json.getJson();
            T widgetTheme;
            if (widgetThemeJson != null) {
                T parentWidgetTheme = key.isSubWidgetTheme() ? map.getTheme(key.getParent()).getTheme() :
                        parent.getWidgetTheme(key).getTheme();
                // sub widget themes strictly only inherit from their parent widget theme and not the parent theme
                widgetTheme = parser.parse(parentWidgetTheme, widgetThemeJson, fallback);
            } else {
                widgetTheme = parent.getWidgetTheme(key).getTheme();
            }

            if (!definedHover && definedStandard) {
                // marker to use the standard theme background on hover
                widgetThemeJson.addProperty(IThemeApi.BACKGROUND, "none");
                widgetThemeHoverJson = widgetThemeJson;
            }

            // only inherit from the widget theme if it was actually defined, otherwise use parent
            T parentWidgetHoverTheme = widgetThemeJson != null ? widgetTheme :
                    parent.getWidgetTheme(key).getHoverTheme();
            T widgetThemeHover = parser.parse(parentWidgetHoverTheme, widgetThemeHoverJson, fallback);

            map.putTheme(key, new WidgetThemeEntry<>(key, widgetTheme, widgetThemeHover));
        }

        private JsonObject getJson(JsonObject json, String key) {
            if (json.has(key)) {
                // theme has widget theme defined
                JsonElement element = json.get(key);
                if (element.isJsonObject()) {
                    // widget theme is a json object
                    return element.getAsJsonObject();
                }
                // incorrect data format
                GTCEu.LOGGER.warn(
                        "WidgetTheme '{}' of theme '{}' with parent '{}' was found to have an incorrect data format.",
                        key, this.id, this.parent);
            }
            // theme doesn't have widget theme defined
            return null;
        }
    }
}
