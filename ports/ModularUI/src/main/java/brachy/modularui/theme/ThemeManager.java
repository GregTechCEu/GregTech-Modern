package brachy.modularui.theme;

import brachy.modularui.ModularUI;
import brachy.modularui.api.ITheme;
import brachy.modularui.api.IThemeApi;
import brachy.modularui.utils.serialization.json.JsonBuilder;
import brachy.modularui.utils.serialization.json.JsonHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import com.mojang.serialization.JsonOps;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ApiStatus.Internal
@OnlyIn(Dist.CLIENT)
public class ThemeManager extends SimplePreparableReloadListener<Map<String, List<Identifier>>> {

    public static final ThemeManager INSTANCE = new ThemeManager();

    public static final String THEMES_PATH = "themes.json";
    public static final FileToIdConverter THEME_LISTER = FileToIdConverter.json("themes");
    protected static final WidgetThemeEntry<WidgetTheme> defaultFallbackWidgetTheme = IThemeApi.get()
            .getDefaultTheme().getWidgetTheme(IThemeApi.FALLBACK);

    private static JsonWidgetThemeStorage jsons;

    private ThemeManager() {}

    public static void reload() {
        // hackery to reload themes on this thread
        // usually resources are loaded off-thread to not block the main thread
        // but this should be fine since it is currently not expected to take longer than a second
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        ProfilerFiller profiler = Minecraft.getInstance().getProfiler();
        NeoForge.EVENT_BUS.post(new ReloadThemeEvent());
        INSTANCE.apply(INSTANCE.prepare(resourceManager, profiler), resourceManager, profiler);
    }

    @Override
    protected @NotNull Map<String, List<Identifier>> prepare(ResourceManager resourceManager,
                                                                   ProfilerFiller profiler) {
        ModularUI.LOGGER.info("Reloading Themes...");
        ThemeAPI.INSTANCE.onReload();

        Map<String, List<Identifier>> themes = new Object2ObjectOpenHashMap<>();
        profiler.startTick();
        List<String> themeJsonSources = new ArrayList<>();
        for (String namespace : resourceManager.getNamespaces()) {
            profiler.push(namespace);

            for (Resource resource : resourceManager
                    .getResourceStack(Identifier.fromNamespaceAndPath(namespace, THEMES_PATH))) {
                profiler.push(resource.sourcePackId());
                themeJsonSources.add(resource.sourcePackId());

                JsonElement element;
                try (InputStream stream = resource.open()) {
                    element = JsonHelper.parse(stream);
                } catch (Exception e) {
                    ModularUI.LOGGER.catching(e);
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
                            ModularUI.LOGGER.error("Theme screen definitions must be an object!");
                            continue;
                        }
                        loadScreenThemes(entry.getValue().getAsJsonObject());
                        continue;
                    }
                    if (entry.getValue().isJsonObject() || entry.getValue().isJsonArray() ||
                            entry.getValue().isJsonNull()) {
                        ModularUI.LOGGER.throwing(new JsonParseException("Theme must be a string!"));
                        continue;
                    }
                    themes.computeIfAbsent(entry.getKey(), key -> new ArrayList<>())
                            .add(Identifier.parse(entry.getValue().getAsString()));
                }
                profiler.pop();
            }

            profiler.pop();

        }
        ModularUI.LOGGER.info("Found themes.json's at {}", themeJsonSources);
        return themes;
    }

    @Override
    protected void apply(@NotNull Map<String, List<Identifier>> themes,
                         @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        Map<String, ThemeJson> themeMap = new Object2ObjectOpenHashMap<>();
        profiler.startTick();

        // load json files from the path and parse their parent
        for (Map.Entry<String, List<Identifier>> entry : themes.entrySet()) {
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
        jsons = new JsonWidgetThemeStorage();
        for (ThemeJson themeJson : sortedThemes.values()) {
            Theme theme = themeJson.deserialize();
            ThemeAPI.INSTANCE.registerTheme(theme);
        }
        jsons = null;

        validateJsonScreenThemes();
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
                    ModularUI.LOGGER.error(
                            "Can't find parent '{}' for theme '{}'! All children for '{}' are therefore invalid!",
                            theme.parent, theme.id, theme.id);
                    invalidThemes.addAll(parents);
                    break;
                }
                if (parents.contains(parent)) {
                    ModularUI.LOGGER.error(
                            "Ancestor tree for themes can't be circular! All of the following make a circle or are children of the circle: {}",
                            parents);
                    invalidThemes.addAll(parents);
                    break;
                }
                if (invalidThemes.contains(parent)) {
                    ModularUI.LOGGER.error(
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

    private static ThemeJson loadThemeJson(String id, List<Identifier> paths,
                                           @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        List<JsonObject> jsons = new ArrayList<>();
        boolean override = false;
        for (Identifier path : paths) {
            profiler.push(path.toString());
            Identifier rl = THEME_LISTER.idToFile(path);
            Resource resource = resourceManager.getResource(rl).orElse(null);
            if (resource == null) {
                profiler.pop();
                ModularUI.LOGGER.warn("Theme '{}' was not found at path '{}'", id, rl);
                continue;
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
            ModularUI.LOGGER.throwing(new JsonParseException("Theme must be a JsonObject!"));
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
                ModularUI.LOGGER.error("Theme screen definitions must be strings!");
            }
        }
    }

    private static void validateJsonScreenThemes() {
        for (ObjectIterator<Object2ObjectMap.Entry<String, String>> iterator = ThemeAPI.INSTANCE.jsonScreenThemes
                .object2ObjectEntrySet().fastIterator(); iterator.hasNext(); ) {
            Map.Entry<String, String> entry = iterator.next();
            if (!ThemeAPI.INSTANCE.hasTheme(entry.getValue())) {
                ModularUI.LOGGER.error("Tried to register theme '{}' for screen '{}', but theme does not exist",
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
            for (ListIterator<JsonObject> iterator = jsons.listIterator(jsons.size()); iterator.hasPrevious(); ) {
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
            var incompleteFallbackJson = ImmutableJson.of(jsonBuilder.getJson()); // themes should only inherit values which are declared here
            var fallbackJson = IThemeApi.FALLBACK.getMerger().merge(
                    jsonBuilder.getJson(),
                    ThemeManager.jsons.get(IThemeApi.FALLBACK, parentWidgetTheme.theme()),
                    ImmutableJson.EMPTY);
            WidgetTheme fallback = IThemeApi.FALLBACK.parseJson(fallbackJson);
            WidgetTheme fallbackHover = fallback;
            var immutableFallbackJson = ThemeManager.jsons.get(IThemeApi.FALLBACK, fallback);

            JsonObject hoverJson = getJson(jsonBuilder.getJson(), IThemeApi.HOVER_SUFFIX);
            if (hoverJson == null) {
                hoverJson = getJson(jsonBuilder.getJson(), IThemeApi.FALLBACK.getFullName() + IThemeApi.HOVER_SUFFIX);
            }
            if (hoverJson != null) {
                hoverJson = IThemeApi.FALLBACK.getMerger().merge(hoverJson, immutableFallbackJson, ImmutableJson.EMPTY);
                fallbackHover = IThemeApi.FALLBACK.parseJson(hoverJson);
            }

            widgetThemes.register(IThemeApi.FALLBACK, fallback, fallbackHover);
            // parse all main widget themes
            for (WidgetThemeKey<?> key : ThemeAPI.INSTANCE.getWidgetThemeKeys()) {
                if (key != IThemeApi.FALLBACK) {
                    parse(widgetThemes, parent, key, jsonBuilder, incompleteFallbackJson);
                }
            }
            return new Theme(this.id, parent, widgetThemes);
        }

        private <T extends WidgetTheme> void parse(WidgetThemeMap map, ITheme parent, WidgetThemeKey<T> key,
                                                   JsonBuilder json, ImmutableJson fallback) {
            WidgetThemeMerger<T> merger = key.getMerger();
            JsonObject rawWidgetThemeJson = getJson(json.getJson(), key.getFullName());
            boolean definedStandard = rawWidgetThemeJson != null;

            JsonObject widgetThemeHoverJson = getJson(json.getJson(), key.getFullName() + IThemeApi.HOVER_SUFFIX);
            boolean definedHover = widgetThemeHoverJson != null;
            if (!definedHover) {
                if (!definedStandard) {
                    // widget theme undefined -> copy from parent
                    if (key.isSubWidgetTheme()) {
                        // if it is a sub widget theme, we use the parent widget theme from this theme
                        WidgetThemeEntry<T> entry = map.getTheme(key.getParent());
                        map.putTheme(key, new WidgetThemeEntry<>(key, entry.theme(), entry.hoverTheme()));
                        return;
                    }
                    // we still need to parse non-inherited values (fallback)
                    rawWidgetThemeJson = new JsonObject();
                    widgetThemeHoverJson = new JsonObject();
                }
            }

            if (key.isSubWidgetTheme()) fallback = ImmutableJson.EMPTY;
            ImmutableJson widgetThemeJson;
            if (rawWidgetThemeJson != null) {
                T parentWidgetTheme = key.isSubWidgetTheme() ? map.getTheme(key.getParent()).theme() : parent.getWidgetTheme(key).theme();
                // sub widget themes strictly only inherit from their parent widget theme and not the parent theme
                widgetThemeJson = ImmutableJson.of(merger.merge(rawWidgetThemeJson, ThemeManager.jsons.get(key, parentWidgetTheme), fallback));
            } else {
                widgetThemeJson = ThemeManager.jsons.get(key, parent.getWidgetTheme(key).theme());
            }

            if (!definedHover && definedStandard) {
                // marker to use the standard theme background on hover
                rawWidgetThemeJson.addProperty(IThemeApi.BACKGROUND, "none");
                widgetThemeHoverJson = rawWidgetThemeJson;
            }

            // only inherit from the widget theme if it was actually defined, otherwise use parent
            ImmutableJson parentWidgetHoverTheme = definedStandard ? widgetThemeJson : ThemeManager.jsons.get(key, parent.getWidgetTheme(key).hoverTheme());
            JsonObject widgetThemeHover = merger.merge(widgetThemeHoverJson, parentWidgetHoverTheme, fallback);
            var immutableHoverWidgetTheme = ImmutableJson.of(widgetThemeHover);

            T widgetThemeInstance = key.getCodec().codec().parse(JsonOps.INSTANCE, widgetThemeJson.toJson()).getOrThrow();
            T widgetThemeHoverInstance = key.getCodec().codec().parse(JsonOps.INSTANCE, immutableHoverWidgetTheme.toJson()).getOrThrow();

            map.register(key, widgetThemeInstance, widgetThemeHoverInstance);
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
                ModularUI.LOGGER.warn(
                        "WidgetTheme '{}' of theme '{}' with parent '{}' was found to have an incorrect data format.",
                        key, this.id, this.parent);
            }
            // theme doesn't have widget theme defined
            return null;
        }
    }
}
