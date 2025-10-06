package com.gregtechceu.gtceu.integration.xei;

import com.gregtechceu.gtceu.client.mui.screen.ModularScreen;

import java.util.function.Predicate;

public enum XeiState implements Predicate<ModularScreen> {

    ENABLED {

        @Override
        public boolean test(ModularScreen screen) {
            return true;
        }
    },
    DISABLED {

        @Override
        public boolean test(ModularScreen screen) {
            return false;
        }
    },
    DEFAULT {

        @Override
        public boolean test(ModularScreen screen) {
            return !screen.isClientOnly();
        }
    }
}
