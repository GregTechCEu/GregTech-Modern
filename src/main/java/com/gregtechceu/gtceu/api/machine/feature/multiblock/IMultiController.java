// package com.gregtechceu.gtceu.api.machine.feature.multiblock;
//
// import com.gregtechceu.gtceu.api.capability.IParallelHatch;
// import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
// import com.gregtechceu.gtceu.api.machine.feature.IMachineFeature;
// import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
// import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
// import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
// import com.gregtechceu.gtceu.api.multiblock.pattern.PatternState;
// import com.gregtechceu.gtceu.client.renderer.MultiblockInWorldPreviewRenderer;
// import com.gregtechceu.gtceu.config.ConfigHolder;
//
// import net.minecraft.core.BlockPos;
// import net.minecraft.core.Direction;
// import net.minecraft.world.InteractionHand;
// import net.minecraft.world.InteractionResult;
// import net.minecraft.world.entity.player.Player;
// import net.minecraft.world.level.Level;
// import net.minecraft.world.level.block.state.BlockState;
// import net.minecraft.world.level.block.state.properties.BooleanProperty;
// import net.minecraft.world.phys.BlockHitResult;
//
// import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
// import org.jetbrains.annotations.NotNull;
// import org.jetbrains.annotations.Nullable;
//
// import java.util.Comparator;
// import java.util.List;
// import java.util.Optional;
// import java.util.Set;
// import java.util.concurrent.locks.Lock;
//
// public interface IMultiController extends IMachineFeature {
//
// BooleanProperty IS_FORMED_PROPERTY = GTMachineModelProperties.IS_FORMED;
//
// @Override
// default MultiblockControllerMachine self() {
// return (MultiblockControllerMachine) this;
// }
//
// void createStructurePatterns();
//
// Reference2ObjectMap<String, IBlockPattern> getStructurePatterns();
//
// /**
// * Check MultiBlock Pattern. Just checking pattern without any other logic.
// * You can override it, but it's unsafe for calling. because it will also be called in an async thread.
// * <br>
// * you should always use {@link IMultiController#checkPatternWithLock()} and
// * {@link IMultiController#checkPatternWithTryLock()} instead.
// *
// * @return whether it can be formed.
// */
// void checkAndFormStructurePatterns();
//
// Set<String> getStructureNames();
//
// /**
// * This method will check if a multiblock aisles predicates are valid and WILL update the patternState each time it
// * is called.
// *
// * @return the new state
// */
// PatternState checkStructurePattern();
//
// PatternState checkStructurePattern(String structureName);
//
// PatternState getDefaultPatternState();
//
// PatternState getPatternState(String name);
//
// /**
// * Check pattern with a lock.
// */
// default boolean checkPatternWithLock(String name) {
// var lock = getPatternLock();
// lock.lock();
// try {
// var patternCheckState = getPatternState(name).getState();
// if (patternCheckState == null || !patternCheckState.isValid()) {
// checkStructurePattern(name);
// }
// return getPatternState(name).getState().isValid();
// } finally {
// lock.unlock();
// }
// }
//
// /**
// * Check pattern with a try lock
// *
// * @return false - checking failed or cant get the lock.
// */
// default boolean checkPatternWithTryLock(String name) {
// var lock = getPatternLock();
// if (lock.tryLock()) {
// try {
// var patternCheckState = getPatternState(name).getState();
// if (patternCheckState == null || !patternCheckState.isValid()) {
// checkStructurePattern(name);
// }
// return getPatternState(name).getState() != PatternState.CheckState.UNINITIALIZED;
// } finally {
// lock.unlock();
// }
// } else {
// return false;
// }
// }
//
// /**
// * Get structure pattern.
// * You can override it to create dynamic patterns.
// */
// default IBlockPattern createStructurePattern() {
// return self().getDefinition().getPatternFactory().get();
// }
//
// /**
// * Call to form a multiblock
// *
// * @param name the structure with which to form
// */
// void formStructure(@NotNull String name);
//
// void invalidateStructure(String name);
//
// // /**
// // * Called when structure is formed, have to be called after {@link #formStructure(String)}. (server-side / fake
// // scene only)
// // * <br>
// // * Trigger points:
// // * <br>
// // * 1 - Blocks in structure changed but still formed.
// // * <br>
// // * 2 - Literally, structure formed.
// // */
// // void onStructureFormed(String name);
// //
// // /**
// // * Called when structure is invalid. (server-side / fake scene only)
// // * <br>
// // * Trigger points:
// // * <br>
// // * 1 - Blocks in structure changed.
// // * <br>
// // * 2 - Before controller machine removed.
// // */
// // void onStructureInvalid(String name);
//
// /**
// * Whether multiblock is formed.
// * <br>
// * NOTE: even if machine is formed, it doesn't mean the machine will be workable
// * Its parts maybe invalid due to unloaded chunks.
// */
// boolean isFormed();
//
// /**
// * Called in an async thread. It's unsafe, Don't modify anything of world but checking information.
// * It will be called per 5 tick.
// *
// * @param periodID period Tick
// */
// void asyncCheckPattern(long periodID);
//
// /**
// * Whether it has front face.
// * false means structure of all sides are available.
// */
// boolean hasFrontFacing();
//
// /**
// * Get all parts
// */
// List<IMultiPart> getParts();
//
// /**
// * The instance of {@link IParallelHatch} attached to this Controller.
// * <p>
// * Note that this will return a singular instance, and will not account for multiple attached IParallelHatches
// *
// * @return an {@link Optional} of the attached IParallelHatch, empty if one is not attached
// */
// Optional<IParallelHatch> getParallelHatch();
//
// /**
// *
// * @return Whether batching is enabled on this multiblock
// */
// default boolean isBatchEnabled() {
// return false;
// }
//
// default void setBatchEnabled(boolean batch) {}
//
// /**
// * Called from part, when part is invalid due to chunk unload or broken.
// */
// void onPartUnload();
//
// /**
// * Get lock for pattern checking.
// */
// Lock getPatternLock();
//
// /**
// * should add part to the part list.
// */
// default boolean shouldAddPartToController(IMultiPart part) {
// return true;
// }
//
// /**
// * get parts' Appearance. same as IForgeBlock.getAppearance() / IFabricBlock.getAppearance()
// */
// @Nullable
// default BlockState getPartAppearance(IMultiPart part, Direction side, BlockState sourceState, BlockPos sourcePos) {
// if (isFormed()) {
// return self().getDefinition().getPartAppearance().apply(this, part, side);
// }
// return null;
// }
//
// default Comparator<IMultiPart> getPartSorter() {
// return self().getDefinition().getPartSorter().apply(self());
// }
//
// /**
// * Show the preview of structure.
// */
// @Override
// default InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
// BlockHitResult hit) {
// if (!self().isFormed() && player.isShiftKeyDown() && player.getItemInHand(hand).isEmpty()) {
// if (world.isClientSide()) {
// MultiblockInWorldPreviewRenderer.showPreview(pos, self(),
// ConfigHolder.INSTANCE.client.inWorldPreviewDuration * 20);
// }
// return InteractionResult.SUCCESS;
// }
// return IInteractedMachine.super.onUse(state, world, pos, player, hand, hit);
// }
//
// default boolean allowCircuitSlots() {
// return true;
// }
// }
