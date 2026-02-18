package com.gregtechceu.gtceu.integration.recipeviewer.entry;

import java.util.List;

public interface EntryList<T> {

    List<T> getStacks();

    boolean isEmpty();
}
