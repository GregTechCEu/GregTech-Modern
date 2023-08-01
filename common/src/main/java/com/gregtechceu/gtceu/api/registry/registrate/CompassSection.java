package com.gregtechceu.gtceu.api.registry.registrate;

import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.json.SimpleIGuiTextureJsonUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author KilaBash
 * @date 2023/7/31
 * @implNote CompassSectionBuilder
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@Accessors(fluent = true, chain = true)
public class CompassSection {
    @Getter
    private final ResourceLocation sectionID;
    @Setter
    private Supplier<IGuiTexture> icon = () -> IGuiTexture.EMPTY;
    @Setter
    private Supplier<IGuiTexture> background = () -> IGuiTexture.EMPTY;
    @Setter
    private int priority = 99;
    @Setter @Getter
    private String lang;

    private CompassSection(String section) {
        this.sectionID = GTCEu.id(section);
        lang = FormattingUtil.toEnglishName(section);
    }

    public static CompassSection create(String section) {
        return new CompassSection(section);
    }

    public CompassSection register() {
        GTRegistries.COMPASS_SECTIONS.register(sectionID, this);
        return this;
    }

    public String getUnlocalizedKey() {
        return sectionID.toLanguageKey("compass.section");
    }

    public static class CompassSectionProvider implements DataProvider {
        private final DataGenerator generator;
        private final Predicate<ResourceLocation> existingHelper;

        public CompassSectionProvider(DataGenerator generator, Predicate<ResourceLocation> existingHelper) {
            this.generator = generator;
            this.existingHelper = existingHelper;
        }

        @Override
        public void run(CachedOutput cache) {
            generate(generator.getOutputFolder(), cache);
        }

        @Override
        public String getName() {
            return "GTCEU's Compass Sections";
        }

        public void generate(Path path, CachedOutput cache) {
            path = path.resolve("assets/" + GTCEu.MOD_ID);

            try {
                for (var section : GTRegistries.COMPASS_SECTIONS) {
                    var resourcePath = "compass/sections/" + section.sectionID.getPath() + ".json";
                    if (existingHelper.test(GTCEu.id(resourcePath))) {
                        continue;
                    }
                    JsonObject json = new JsonObject();
                    json.add("button_texture",SimpleIGuiTextureJsonUtils.toJson(section.icon.get()));
                    json.add("background_texture",SimpleIGuiTextureJsonUtils.toJson(section.background.get()));
                    json.addProperty("priority", section.priority);
                    DataProvider.saveStable(cache, json, path.resolve(resourcePath));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
