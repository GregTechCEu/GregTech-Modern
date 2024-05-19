package com.gregtechceu.gtceu.api.data.worldgen;

import com.gregtechceu.gtceu.GTCEu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.*;

public class SaveVeinLocation extends SavedData
{
    private final Map<ChunkPos, Vein> veinMap = new HashMap<>();

    @Nonnull
    public static SaveVeinLocation get(ServerLevel level){
//        if(level.isClientSide){
//            throw new RuntimeException("Unable to access this metod from cilent side");
//        }
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(SaveVeinLocation::new, SaveVeinLocation::new, "veinmanager");
    }

    public Vein GetVeinsForChunk(BlockPos bpos){
        ChunkPos cords = new ChunkPos(bpos);

        GTCEu.LOGGER.info("Trying a acces a vein in chunk with cords [%s, %s cp:(%s)]; found vein: [%s] veinMap is: %s".formatted(cords.x, cords.z, cords, veinMap.get(bpos), veinMap));

        return veinMap.get(cords);
    }

    public void saveVein(ChunkPos pos, Vein vein){
        veinMap.put(pos, vein);
        String name = ForgeRegistries.BLOCKS.getKey(vein.containingBlocks.get(0)).toString();
        GTCEu.LOGGER.info("Adding a vein to be saved. Ore vein at %s, with blocks: %s".formatted(pos, name)); // Move this so all vein types are affected
        setDirty(true);
    }

    public SaveVeinLocation(){

    }

    public SaveVeinLocation(CompoundTag compoundTag){
        ListTag veins = compoundTag.getList("veins", Tag.TAG_COMPOUND);
        veins.forEach(v -> {
            CompoundTag vein = (CompoundTag) v;
            ChunkPos pos = new ChunkPos(vein.getInt("x"), vein.getInt("z"));
            List<String> oreStrings = new ArrayList<>();
            List<Block> oreBlocks = new ArrayList<>();
            ListTag ores = (ListTag) vein.get("ores");
            assert ores != null;
            ores.forEach(o -> {
                CompoundTag ore = (CompoundTag) o;
                oreStrings.add(ore.getString("ore"));
            });

            oreStrings.forEach(oreString -> oreBlocks.add(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(oreString.replace("+", ":")))));
            veinMap.put(pos, new Vein(oreBlocks));
        });
        GTCEu.LOGGER.info("Loading data from saved veins: %s".formatted(veinMap.toString()));
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag) {
        ListTag veins = new ListTag();
        veinMap.forEach((chunkPos, generatedVein) ->{
            CompoundTag vein = new CompoundTag();

            vein.putInt("x", chunkPos.x);
            vein.putInt("z", chunkPos.z);

            ListTag ores = new ListTag();
            generatedVein.containingBlocks.forEach(block -> {
                CompoundTag ore = new CompoundTag();
                ore.putString("ore", Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block)).toString().replace(":", "+"));
                ores.add(ore);
            });
            vein.put("ores", ores);

            veins.add(vein);
        });
        tag.put("veins", veins);
        GTCEu.LOGGER.info("Saving Vein Data: %s".formatted(veinMap.toString()));
        return tag;
    }


}
