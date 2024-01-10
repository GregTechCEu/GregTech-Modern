package com.gregtechceu.gtceu.api.registry.registrate.forge;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.IGTFluidBuilder;
import com.tterrag.registrate.builders.NoConfigBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

/**
 * @author KilaBash
 * @date 2023/2/14
 * @implNote GTRegistrateImpl
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GTRegistrateImpl extends GTRegistrate {
    protected GTRegistrateImpl(String modId) {
        super(modId);
    }

    public static IGTFluidBuilder fluid(GTRegistrate parent, Material material, String name, String langKey, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        return parent.entry(name, callback -> new GTFluidBuilder<>(parent, parent, material, name, langKey, callback, stillTexture, flowingTexture, GTFluidBuilder::defaultFluidType).defaultLang().defaultSource().setData(ProviderType.LANG, NonNullBiConsumer.noop()));
    }

    @Nonnull
    public static GTRegistrate create(String modId) {
        return new GTRegistrateImpl(modId);
    }

    @Override
    public void registerRegistrate() {
        registerEventListeners(FMLJavaModLoadingContext.get().getModEventBus());
    }

    @Override
    protected <P> NoConfigBuilder<CreativeModeTab, CreativeModeTab, P> createCreativeModeTab(P parent, String name, Consumer<CreativeModeTab.Builder> config) {
        var tab = ResourceKey.create(Registries.CREATIVE_MODE_TAB, new ResourceLocation(getModid(), name));
        return this.generic(parent, name, Registries.CREATIVE_MODE_TAB, () -> {
            var builder = CreativeModeTab.builder()
                    .icon(() -> getAll(Registries.ITEM).stream().findFirst().map(ItemEntry::cast).map(ItemEntry::asStack).orElse(new ItemStack(Items.AIR)))
                    .title(this.addLang("itemGroup", tab.location(), RegistrateLangProvider.toEnglishName(name)));
            config.accept(builder);
            return builder.build();
        });
    }
}
