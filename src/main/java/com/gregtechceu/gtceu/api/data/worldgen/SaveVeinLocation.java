package com.gregtechceu.gtceu.api.data.worldgen;

import com.gregtechceu.gtceu.GTCEu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.*;

public class SaveVeinLocation extends SavedData
{
    private final Map<BlockPos, ResourceLocation> veinNameMap = new HashMap<>(); // Some other data storage technique may need to be considered to provide faster lookup time when querying for area?

    @Nonnull
    public static SaveVeinLocation get(ServerLevel level){
        if(level.isClientSide){
            throw new RuntimeException("Unable to access this metod from cilent side");
        }
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(SaveVeinLocation::new, SaveVeinLocation::new, "veinmanager");
    }

    public ResourceLocation getVeinsForBlock(BlockPos veinCenter){
        GTCEu.LOGGER.info("Trying a acces a vein in chunk with cords [cp:(%s)]; found vein: [%s] veinMap is: %s".formatted(veinCenter, veinNameMap.get(veinCenter), veinNameMap));
        return veinNameMap.get(veinCenter);
    }

    /**
     *
     * @param position Origin of the searched area
     * @param radius A square area to be searched, Y cord is ignored
     * @return A veins Registry Entry.
     */
    public List<ResourceLocation> getVeinsForArea(BlockPos position, int radius){
        // I don't love this, but I don't have a better solution. -> maybe having the "radius" pre-defined and computing this when saving the veins would be a good solution.
        int minX = position.getX() - radius;
        int maxX = position.getX() + radius;
        int minZ = position.getZ() - radius;
        int maxZ = position.getZ() + radius;

        List<ResourceLocation> matchingVeins = new ArrayList<>();
        veinNameMap.forEach((blockPos, resourceLocation) -> {
            if ((blockPos.getX() >= minX && blockPos.getX() <= maxX) && (blockPos.getZ() >= minZ && blockPos.getZ() <= maxZ)){
                matchingVeins.add(resourceLocation);
            }
        });
        return matchingVeins;
    }

    public void saveVein(BlockPos pos, ResourceLocation veinID){
        veinNameMap.put(pos, veinID);
        setDirty(true);
    }

    public SaveVeinLocation(){
    }

    public SaveVeinLocation(CompoundTag compoundTag){
        ListTag veins = compoundTag.getList("veins", Tag.TAG_COMPOUND);
        veins.forEach(v -> {
            CompoundTag vein = (CompoundTag) v;
            BlockPos veinCenter = new BlockPos(vein.getInt("x"), vein.getInt("y"), vein.getInt("z"));
            ResourceLocation veinID = new ResourceLocation(GTCEu.MOD_ID, vein.getString("veinID"));
            veinNameMap.put(veinCenter, veinID);
        });
        GTCEu.LOGGER.info("Loading data from saved veins: %s".formatted(veinNameMap.toString()));
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag) {
        ListTag veins = new ListTag();
        veinNameMap.forEach((blockPos, generatedVein) ->{
            CompoundTag vein = new CompoundTag();

            vein.putInt("x", blockPos.getX());
            vein.putInt("y", blockPos.getY());
            vein.putInt("z", blockPos.getZ());

            vein.putString("veinID", generatedVein.getPath());

            veins.add(vein);
        });
        tag.put("veins", veins);
        GTCEu.LOGGER.info("Saving Vein Data: %s".formatted(veinNameMap.toString()));
        return tag;
    }


}
