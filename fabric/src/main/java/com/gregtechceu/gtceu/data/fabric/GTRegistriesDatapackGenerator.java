package com.gregtechceu.gtceu.data.fabric;

import com.google.gson.JsonElement;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.data.registries.RegistriesDatapackGenerator;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class GTRegistriesDatapackGenerator extends RegistriesDatapackGenerator {
	private final String name;
	private final PackOutput output;
	private final CompletableFuture<HolderLookup.Provider> registries;
	private final java.util.function.Predicate<String> namespacePredicate;

	private GTRegistriesDatapackGenerator(String name, PackOutput output, CompletableFuture<Provider> registries, Set<String> modIds) {
		super(output, registries);
		this.name = name;
		this.namespacePredicate = modIds::contains;
		this.registries = registries;
		this.output = output;
	}

	public GTRegistriesDatapackGenerator(PackOutput output, CompletableFuture<Provider> registries, RegistrySetBuilder builder, Set<String> modIds, String name) {
		this(name, output, registries.thenApply(r -> constructRegistries(r, builder)), modIds);
	}

	/**
	 * A method used to construct empty bootstraps for all registries this provider
	 * did not touch such that existing dynamic registry objects do not get inlined.
	 *
	 * @param original a future of a lookup for registries and their objects
	 * @param datapackEntriesBuilder a builder containing the dynamic registry objects added by this provider
	 * @return a new lookup containing the existing and to be generated registries and their objects
	 */
	private static HolderLookup.Provider constructRegistries(HolderLookup.Provider original, RegistrySetBuilder datapackEntriesBuilder) {
		var builderKeys = new HashSet<>(datapackEntriesBuilder.entries.stream().map(RegistrySetBuilder.RegistryStub::key).toList());
		getDataPackRegistriesWithDimensions().filter(data -> !builderKeys.contains(data.key())).forEach(data -> datapackEntriesBuilder.add(data.key(), context -> {}));
		return datapackEntriesBuilder.buildPatch(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY), original);
	}

	@Override
	public CompletableFuture<?> run(CachedOutput output) {
		return this.registries.thenCompose((provider) -> {
			DynamicOps<JsonElement> dynamicops = RegistryOps.create(JsonOps.INSTANCE, provider);
			return CompletableFuture.allOf(getDataPackRegistriesWithDimensions().flatMap((registryData) -> this.dumpRegistryCap(output, provider, dynamicops, registryData).stream()).toArray(CompletableFuture[]::new));
		});
	}

	private <T> Optional<CompletableFuture<?>> dumpRegistryCap(CachedOutput p_256502_, HolderLookup.Provider p_256492_, DynamicOps<JsonElement> p_256000_, RegistryDataLoader.RegistryData<T> p_256449_) {
		ResourceKey<? extends Registry<T>> resourcekey = p_256449_.key();
		return p_256492_.lookup(resourcekey).map((registryLookup) -> {
			PackOutput.PathProvider packoutput$pathprovider = this.output.createPathProvider(PackOutput.Target.DATA_PACK, prefixNamespace(resourcekey.location()));
			return CompletableFuture.allOf(registryLookup.listElements().filter(holder -> this.namespacePredicate.test(holder.key().location().getNamespace())).map((p_256105_) -> dumpValue(packoutput$pathprovider.json(p_256105_.key().location()), p_256502_, p_256000_, p_256449_.elementCodec(), p_256105_.value())).toArray(CompletableFuture[]::new));
		});
	}

	public static Stream<RegistryDataLoader.RegistryData<?>> getDataPackRegistriesWithDimensions() {
		return Stream.concat(RegistryDataLoader.WORLDGEN_REGISTRIES.stream(), RegistryDataLoader.DIMENSION_REGISTRIES.stream());
	}

	public static String prefixNamespace(ResourceLocation registryKey) {
		return registryKey.getNamespace().equals("minecraft") ? registryKey.getPath() : registryKey.getNamespace() +  "/"  + registryKey.getPath();
	}

	@Override
	@NotNull
	public String getName() {
		return name;
	}
}
