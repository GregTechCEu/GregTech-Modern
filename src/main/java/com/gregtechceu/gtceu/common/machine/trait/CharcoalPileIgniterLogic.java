//package com.gregtechceu.gtceu.common.machine.trait;
//
//import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
//import com.gregtechceu.gtceu.common.machine.multiblock.primitive.CharcoalPileIgniterMachine;
//
//public class CharcoalPileIgniterLogic extends RecipeLogic {
//
//    private CharcoalPileIgniterMachine machine;
//
//    public CharcoalPileIgniterLogic(CharcoalPileIgniterMachine machine) {
//        super(machine);
//    }
//
//    @Override
//    public void serverTick() {
//        super.serverTick();
//        if (isWorking() && duration > 0) {
//            if (++progress == duration) {
//                progress = 0;
//                duration = 0;
//                machine.convertLogBlocks();
//                setStatus(Status.IDLE);
//            }
//        }
//    }
//
//    public void setDuration(int max) {
//        this.duration = max;
//    }
//}
