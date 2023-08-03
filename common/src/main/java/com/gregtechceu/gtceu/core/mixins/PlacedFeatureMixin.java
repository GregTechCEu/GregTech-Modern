package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeatureConfiguration;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeatureEntry;
import com.gregtechceu.gtceu.api.data.worldgen.modifier.VeinCountFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.stream.Stream;

@Mixin(PlacedFeature.class)
public abstract class PlacedFeatureMixin {

    @Shadow @Final private Holder<ConfiguredFeature<?, ?>> feature;

    @Shadow @Final @Mutable
    private List<PlacementModifier> placement;

    @Inject(method = "placeWithContext", at = @At(value = "HEAD"))
    public void gtceu$injectPlaceFeature(PlacementContext context, RandomSource source, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (feature.value().config() instanceof GTOreFeatureConfiguration configuration) {
            GTOreFeatureEntry entry = configuration.getEntry(context.getLevel(), context.getLevel().getBiome(pos), source);
            if (entry != null) {
                placement = entry.getModifiers();
                configuration.setEntry(entry);
            }
        }
    }

    @SuppressWarnings({"UnresolvedMixinReference"}) // They do actually work tho
    @Redirect(method = {
                "m_226372_(Lnet/minecraft/world/level/levelgen/placement/PlacementModifier;Lnet/minecraft/world/level/levelgen/placement/PlacementContext;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Ljava/util/stream/Stream;",
                "method_39649(Lnet/minecraft/world/level/levelgen/placement/PlacementModifier;Lnet/minecraft/world/level/levelgen/placement/PlacementContext;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Ljava/util/stream/Stream;",
                "lambda$placeWithContext$3(Lnet/minecraft/world/level/levelgen/placement/PlacementModifier;Lnet/minecraft/world/level/levelgen/placement/PlacementContext;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Ljava/util/stream/Stream;"
            },
            target = @Desc(value = "m_fxgbskhr", owner = PlacedFeature.class, args = {PlacementModifier.class, PlacementContext.class, RandomSource.class, BlockPos.class}, ret = Stream.class),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/placement/PlacementModifier;getPositions(Lnet/minecraft/world/level/levelgen/placement/PlacementContext;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Ljava/util/stream/Stream;", ordinal = 0))
    private static Stream<BlockPos> gtceu$redirectGetPlacementPositions(PlacementModifier original, PlacementContext context, RandomSource source, BlockPos pos) {
        Stream<BlockPos> returnValue = original.getPositions(context, source, pos);
        BlockPos[] positions = returnValue.toArray(BlockPos[]::new);
        if (positions.length == 0) {
            if (context.topFeature().isPresent() && context.topFeature().get().feature().value().config() instanceof GTOreFeatureConfiguration configuration) {
                GTOreFeatureEntry entry = configuration.getEntry(context.getLevel(), context.getLevel().getBiome(pos), source);
                if (entry != null) {
                    VeinCountFilter.didNotPlace(context.getLevel().getLevel(), pos, entry);
                }
            }
            return Stream.empty();
        }
        return Stream.of(positions);
    }
}
