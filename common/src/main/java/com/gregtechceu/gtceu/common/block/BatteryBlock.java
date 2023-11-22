package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.AppearanceBlock;
import com.gregtechceu.gtceu.api.machine.multiblock.IBatteryData;
import com.lowdragmc.lowdraglib.client.renderer.IBlockRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import lombok.Getter;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BatteryBlock extends AppearanceBlock implements IBlockRendererProvider {
    private final IRenderer renderer;

    @Getter
    private final IBatteryData data;

    public BatteryBlock(Properties properties, IBatteryData data, IRenderer renderer) {
        super(properties);
        this.data = data;
        this.renderer = renderer;
    }

    @Nullable
    @Override
    public IRenderer getRenderer(BlockState state) {
        return renderer;
    }

    public enum BatteryPartType implements StringRepresentable, IBatteryData {
        EMPTY_TIER_I,
        EV_LAPOTRONIC(GTValues.EV, 25_000_000L * 6),      // Lapotron Crystal * 6
        IV_LAPOTRONIC(GTValues.IV, 250_000_000L * 6),     // Lapotronic Orb * 6

        EMPTY_TIER_II,
        LuV_LAPOTRONIC(GTValues.LuV, 1_000_000_000L * 6), // Lapotronic Orb Cluster * 6
        ZPM_LAPOTRONIC(GTValues.ZPM, 4_000_000_000L * 6), // Energy Orb * 6

        EMPTY_TIER_III,
        UV_LAPOTRONIC(GTValues.UV, 16_000_000_000L * 6),  // Energy Cluster * 6
        UHV_ULTIMATE(GTValues.UHV, Long.MAX_VALUE),       // Ultimate Battery
        ;

        private final int tier;
        private final long capacity;

        BatteryPartType() {
            this.tier = -1;
            this.capacity = 0;
        }

        BatteryPartType(int tier, long capacity) {
            this.tier = tier;
            this.capacity = capacity;
        }

        @Override
        public int getTier() {
            return tier;
        }

        @Override
        public long getCapacity() {
            return capacity;
        }

        // must be separately named because of reobf issue
        @NotNull
        @Override
        public String getBatteryName() {
            return name().toLowerCase();
        }

        @NotNull
        @Override
        public String getSerializedName() {
            return getBatteryName();
        }
    }
}
