package com.gregtechceu.gtceu.common.machine.trait;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockore.BedrockOreVeinSavedData;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockore.OreVeinWorldEntry;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.BedrockOreMinerMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.utils.GTUtil;
import lombok.Getter;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * @author KilaBash
 * @date 2023/7/12
 * @implNote FluidDrillLogic
 */
public class BedrockOreMinerLogic extends RecipeLogic {
    public static final int MAX_PROGRESS = 20;

    @Getter @Nullable
    private List<Map.Entry<Integer, Material>> veinMaterials;

    public BedrockOreMinerLogic(BedrockOreMinerMachine machine) {
        super(machine);
    }

    @Override
    public BedrockOreMinerMachine getMachine() {
        return (BedrockOreMinerMachine)super.getMachine();
    }

    @Override
    public void findAndHandleRecipe() {
        if (getMachine().getLevel() instanceof ServerLevel serverLevel) {
            lastRecipe = null;
            var data = BedrockOreVeinSavedData.getOrCreate(serverLevel);
            if (veinMaterials == null) {
                this.veinMaterials = data.getOreInChunk(getChunkX(), getChunkZ());
                if (this.veinMaterials == null) {
                    if (subscription != null) {
                        subscription.unsubscribe();
                        subscription = null;
                    }
                    return;
                }
            }
            var match = getOreMinerRecipe();
            if (match != null) {
                var copied = match.copy(new ContentModifier(match.duration, 0));
                if (match.matchRecipe(this.machine).isSuccess() && copied.matchTickRecipe(this.machine).isSuccess()) {
                    setupRecipe(match);
                }
            }
        }
    }

    @Nullable
    private GTRecipe getOreMinerRecipe() {
        if (getMachine().getLevel() instanceof ServerLevel serverLevel && veinMaterials != null) {
            var data = BedrockOreVeinSavedData.getOrCreate(serverLevel);
            Material material = veinMaterials.get(GTUtil.getRandomItem(veinMaterials, veinMaterials.size())).getValue();
            ItemStack stack = ChemicalHelper.get(TagPrefix.get(ConfigHolder.INSTANCE.machines.bedrockOreDropTagPrefix), material, getOreToProduce(data.getOreVeinWorldEntry(getChunkX(), getChunkZ())));
            if (stack.isEmpty()) stack = ChemicalHelper.get(TagPrefix.crushed, material, getOreToProduce(data.getOreVeinWorldEntry(getChunkX(), getChunkZ()))); // backup 1: crushed; if raw ore doesn't exist
            if (stack.isEmpty()) stack = ChemicalHelper.get(TagPrefix.gem, material, getOreToProduce(data.getOreVeinWorldEntry(getChunkX(), getChunkZ()))); // backup 2: gem; if crushed ore doesn't exist
            if (stack.isEmpty()) stack = ChemicalHelper.get(TagPrefix.ore, material, getOreToProduce(data.getOreVeinWorldEntry(getChunkX(), getChunkZ()))); // backup 3: just fallback to normal ore...
            var recipe = GTRecipeBuilder.ofRaw()
                    .duration(MAX_PROGRESS)
                    .EUt(GTValues.VA[getMachine().getEnergyTier()])
                    .outputItems(stack)
                    .buildRawRecipe();
            if (recipe.matchRecipe(getMachine()).isSuccess() && recipe.matchTickRecipe(getMachine()).isSuccess()) {
                return recipe;
            }
        }
        return null;
    }

    private int getOreToProduce(OreVeinWorldEntry entry) {
        var definition = entry.getDefinition();
        if (definition != null) {
            int depletedYield = definition.depletedYield();
            int regularYield = entry.getOreYield();
            int remainingOperations = entry.getOperationsRemaining();

            int produced = Math.max(depletedYield, regularYield * remainingOperations / BedrockOreVeinSavedData.MAXIMUM_VEIN_OPERATIONS);
            produced *= BedrockOreMinerMachine.getRigMultiplier(getMachine().getTier());

            // Overclocks produce 50% more fluid
            if (isOverclocked()) {
                produced = produced * 3 / 2;
            }
            return produced;
        }
        return 0;
    }

    @Override
    public void onRecipeFinish() {
        machine.afterWorking();
        if (lastRecipe != null) {
            lastRecipe.postWorking(this.machine);
            lastRecipe.handleRecipeIO(IO.OUT, this.machine);
        }
        depleteVein();
        // try it again
        var match = getOreMinerRecipe();
        if (match != null) {
            var copied = match.copy(new ContentModifier(match.duration, 0));
            if (match.matchRecipe(this.machine).isSuccess() && copied.matchTickRecipe(this.machine).isSuccess()) {
                setupRecipe(match);
                return;
            }
        }
        setStatus(Status.IDLE);
        progress = 0;
        duration = 0;
    }

    protected void depleteVein() {
        if (getMachine().getLevel() instanceof ServerLevel serverLevel) {
            int chance = BedrockOreMinerMachine.getDepletionChance(getMachine().getTier());
            var data = BedrockOreVeinSavedData.getOrCreate(serverLevel);
            // chance to deplete based on the rig
            if (chance == 1 || GTValues.RNG.nextInt(chance) == 0) {
                data.depleteVein(getChunkX(), getChunkZ(), 0, false);
            }
        }
    }

    protected boolean isOverclocked() {
        return getMachine().getEnergyTier() > getMachine().getTier();
    }

    private int getChunkX() {
        return SectionPos.blockToSectionCoord(getMachine().getPos().getX());
    }

    private int getChunkZ() {
        return SectionPos.blockToSectionCoord(getMachine().getPos().getZ());
    }

}
