package com.gregtechceu.gtceu.api.registry.registrate.provider;

import com.gregtechceu.gtceu.core.mixins.registrate.LanguageProviderAccessor;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.data.event.GatherDataEvent;

import com.google.gson.JsonObject;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import com.tterrag.registrate.providers.RegistrateProvider;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class GTLangProvider extends RegistrateLangProvider implements RegistrateProvider {

    protected final AbstractRegistrate<?> owner;
    protected final PackOutput output;

    public GTLangProvider(AbstractRegistrate<?> owner, GatherDataEvent event,
                          Map<ProviderType<?>, RegistrateProvider> existing) {
        super(owner, event.getGenerator().getPackOutput());
        this.owner = owner;
        this.output = event.getGenerator().getPackOutput();
    }

    @Override
    public @NotNull String getName() {
        return "Lang (en_us)";
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cache) {
        addTranslations();

        var translations = ((LanguageProviderAccessor) this).gtceu$getData();
        if (translations.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        JsonObject json = new JsonObject();
        translations.forEach(json::addProperty);

        Path target = this.output.getOutputFolder(PackOutput.Target.RESOURCE_PACK)
                .resolve(this.owner.getModid()).resolve("lang").resolve("en_us.json");
        return DataProvider.saveStable(cache, json, target);
    }

    /**
     * Registers multiple values under the same key.
     * <p>
     * For example, a cumbersome way to add translations would be the following:
     * <pre> {@code
     * provider.add("terminal.fluid_prospector.tier.0", "radius size 1");
     * provider.add("terminal.fluid_prospector.tier.1", "radius size 2");
     * provider.add("terminal.fluid_prospector.tier.2", "radius size 3");
     * } </pre>
     * </p>
     *
     * Instead, {@link #addMultiLang} can be used for the same result:
     * <pre> {@code
     * provider.addMultiLang("terminal.fluid_prospector.tier", "radius size 1", "radius size 2", "radius size 3");
     * } </pre>
     *
     * In situations requiring a large number of generated translations, the
     * following could be used instead, which
     * generates translations for 100 tiers:
     * <pre> {@code
     * provider.addMultiLang("terminal.fluid_prospector.tier", IntStream.of(100)
     *                 .map(i -> i + 1)
     *                 .mapToObj(Integer::toString)
     *                 .map(i -> "radius size " + i)
     *                 .toArray(String[]::new));
     * } </pre>
     *
     * @param key    Base key of the translation. The real key for each translation will be appended by ".0" for
     *               the first, ".1" for the second, etc. This ensures that the keys are unique.
     * @param lines  All translation lines.
     */
    public void addMultiLang(String key, String... lines) {
        for (var i = 0; i < lines.length; i++) {
            add(key + "." + i, lines[i]);
        }
    }

    /**
     * Adds one translation to the given lang provider per line in the given
     * multiline (a multiline is a String containing newline characters). For example,
     * <pre> {@code
     * provider.addMultiline("gtceu.gui.overclock.enabled", "Overclocking Enabled.\nClick to Disable");
     * } </pre>
     *
     * Results in the following translations:
     * <pre> {@code
     * "gtceu.gui.overclock.enabled.0": "Overclocking Enabled.",
     * "gtceu.gui.overclock.enabled.1": "Click to Disable",
     * } </pre>
     * </p>
     *
     * @param key       Base key of the translation. The real key for each line will be appended by ".0" for the
     *                  first line, ".1" for the second, etc. This ensures that the keys are unique.
     * @param multiline The multiline string
     */
    public void addMultiline(String key, String multiline) {
        var lines = multiline.split("\n");
        addMultiLang(key, lines);
    }

    @Override
    public void addTooltip(NonNullSupplier<? extends ItemLike> item, @NotNull String tooltip) {
        add(item.get().asItem().getDescriptionId() + ".tooltip", tooltip);
    }

    public void addTooltip(@NotNull NonNullSupplier<? extends ItemLike> item, String... tooltip) {
        addMultiLang(item.get().asItem().getDescriptionId() + ".tooltip", tooltip);
    }

    @Override
    public void addTooltip(@NotNull NonNullSupplier<? extends ItemLike> item, List<@NotNull String> tooltip) {
        addTooltip(item, tooltip.toArray(String[]::new));
    }

    @Override
    public void add(@NotNull String key, @NotNull String value) {
        ((LanguageProviderAccessor) this).gtceu$getData().put(key, value);
    }
}
