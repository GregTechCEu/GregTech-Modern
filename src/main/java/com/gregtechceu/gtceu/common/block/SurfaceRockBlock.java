package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.worldgen.SaveVeinLocation;
import com.gregtechceu.gtceu.api.data.worldgen.ores.OreVeinUtil;
import com.gregtechceu.gtceu.client.renderer.block.SurfaceRockRenderer;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.xaeros.XaerosWorldMapPlugin;
import com.lowdragmc.lowdraglib.Platform;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import xaero.common.XaeroMinimapSession;
import xaero.common.core.IXaeroMinimapClientPlayNetHandler;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.common.minimap.waypoints.WaypointsManager;
import xaero.minimap.XaeroMinimap;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SurfaceRockBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    private static final VoxelShape AABB_NORTH = Block.box(2, 2, 0, 14, 14, 3);
    private static final VoxelShape AABB_SOUTH = Block.box(2, 2,13, 14, 14 ,16);
    private static final VoxelShape AABB_WEST = Block.box(0, 2, 2, 3, 14, 14);
    private static final VoxelShape AABB_EAST = Block.box(13, 2, 2, 16, 14, 14);
    private static final VoxelShape AABB_UP = Block.box(2, 13, 2, 14, 16, 14);
    private static final VoxelShape AABB_DOWN = Block.box(2, 0, 2, 14, 3, 14);

    @Getter
    private final Material material;


    public SurfaceRockBlock(Properties properties, Material material) {
        super(properties);
        this.material = material;

        registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.DOWN));

        if (Platform.isClient()) {
            SurfaceRockRenderer.create(this);
        }
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        // Adding waypoints to xaero's map if the player destroyed an indicator

        if(XaerosWorldMapPlugin.isActive && ConfigHolder.INSTANCE.client.oreIndicatorWaypointOnDestroy) {
            assert Minecraft.getInstance().player != null;
            IXaeroMinimapClientPlayNetHandler clientLevel = (IXaeroMinimapClientPlayNetHandler) (Minecraft.getInstance().player.connection);
            XaeroMinimapSession session = clientLevel.getXaero_minimapSession();
            WaypointsManager waypointsManager = session.getWaypointsManager();
            String oreName = I18n.get(this.getName().getString());
            //Get vein info for a specified chunk

            if(level instanceof ServerLevel serverLevel){
                List<ResourceLocation> vein = SaveVeinLocation.get(serverLevel).getVeinsForArea(pos, 20); // Move the radius to config.
                if (vein == null){
                    return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
                }

//                Check if a waypoint for this chunk is already present.

                for (Waypoint waypoint : waypointsManager.getWaypoints().getList()) {
                    BlockPos waypointPos = new BlockPos(waypoint.getX(), waypoint.getY(), waypoint.getZ());
                    // Waypoint in the same chunk, we return, don't need to duplicate
                    if (new ChunkPos(waypointPos).equals(new ChunkPos(pos))) {
                        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
                    }
                }
                ChunkPos chunkPos = new ChunkPos(pos);
                var random = new XoroshiroRandomSource(serverLevel.getSeed() ^ chunkPos.toLong());

                Optional<BlockPos> veinCenter = OreVeinUtil.getVeinCenter(chunkPos, random);

                StringBuilder comboVeinsName = new StringBuilder();

                //voodoo magic <- to allow using predefined jei ore veins locals
                for (ResourceLocation veinResourceLocation : vein) {
                    comboVeinsName.append(I18n.get(veinResourceLocation.toLanguageKey().replace("gtceu.", "gtceu.jei.ore_vein.")));
                    comboVeinsName.append("/");
                }
                final String combinedTrimedVeinName = comboVeinsName.substring(0, comboVeinsName.length()-2);
                final var instant = new Waypoint(pos.getX(), pos.getY(), pos.getZ(), "%s: %s".formatted(I18n.get("veins.possible.vein.location"), combinedTrimedVeinName), combinedTrimedVeinName.substring(0, 1), 0);
                waypointsManager.getWaypoints().getList().add(instant);
            }

            try {
                XaeroMinimap.instance.getSettings().saveWaypoints(waypointsManager.getCurrentWorld());
            } catch (IOException error) {
                error.printStackTrace();
            }
        }

        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.destroyBlock(pos, true, player)) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case DOWN -> AABB_DOWN;
            case UP -> AABB_UP;
            case NORTH -> AABB_NORTH;
            case SOUTH -> AABB_SOUTH;
            case WEST -> AABB_WEST;
            case EAST -> AABB_EAST;
        };
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isCollisionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isOcclusionShapeFullBlock(BlockState state, BlockGetter view, BlockPos pos) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        var facing = state.getValue(FACING);
        var attachedBlock = pos.relative(facing);

        return level.getBlockState(attachedBlock).isFaceSturdy(level, attachedBlock, facing.getOpposite());
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);

        if (!canSurvive(state, level, pos)) {
            Block.updateOrDestroy(state, Blocks.AIR.defaultBlockState(), level, pos, Block.UPDATE_ALL);
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return getStateForDirection(context.getNearestLookingVerticalDirection());
    }

    public BlockState getStateForDirection(Direction direction) {
        return defaultBlockState().setValue(FACING, direction);
    }

    @OnlyIn(Dist.CLIENT)
    public static BlockColor tintedColor() {
        return (state, reader, pos, tintIndex) -> {
            if (state.getBlock() instanceof SurfaceRockBlock block) {
                return block.material.getMaterialRGB();
            }
            return -1;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public String getDescriptionId() {
        return "block.surface_rock";
    }

    @Override
    public MutableComponent getName() {
        return Component.translatable("block.surface_rock", material.getLocalizedName());
    }
}
