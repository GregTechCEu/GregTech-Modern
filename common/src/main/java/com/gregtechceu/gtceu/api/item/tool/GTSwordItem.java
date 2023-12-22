package com.gregtechceu.gtceu.api.item.tool;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class GTSwordItem extends SwordItem implements IGTTool {

    @Getter
    private final GTToolType toolType;
    @Getter
    private final Material material;
    @Getter
    private final int electricTier;
    @Getter
    private final IGTToolDefinition toolStats;

    public GTSwordItem(GTToolType toolType, MaterialToolTier tier, Material material, int electricTier, IGTToolDefinition toolStats, Properties properties) {
        super(tier, 0, 0, properties);
        this.toolType = toolType;
        this.material = material;
        this.electricTier = electricTier;
        this.toolStats = toolStats;
    }

    @Override
    public boolean isElectric() {
        return electricTier > -1;
    }

    @Nullable
    @Override
    public SoundEntry getSound() {
        return toolType.soundEntry;
    }

    @Override
    public boolean playSoundOnBlockDestroy() {
        return toolType.playSoundOnBlockDestroy;
    }

    @Override
    public Set<GTToolType> getToolClasses(ItemStack stack) {
        return null;
    }
}
