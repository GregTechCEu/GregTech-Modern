package com.gregtechceu.gtceu.client.renderer;

import com.lowdragmc.lowdraglib.client.renderer.ATESRRendererProvider;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2022/11/3
 * @implNote TCRendererProvider
 */
@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GTRendererProvider extends ATESRRendererProvider<BlockEntity> {

    private static GTRendererProvider INSTANCE;

    private GTRendererProvider(BlockEntityRendererProvider.Context context) {}

    public static GTRendererProvider getOrCreate(BlockEntityRendererProvider.Context context) {
        if (INSTANCE == null) {
            INSTANCE = new GTRendererProvider(context);
        }
        return INSTANCE;
    }

    @Nullable
    public static GTRendererProvider getInstance() {
        return INSTANCE;
    }
}
