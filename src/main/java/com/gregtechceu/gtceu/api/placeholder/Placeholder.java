package com.gregtechceu.gtceu.api.placeholder;

import com.gregtechceu.gtceu.api.placeholder.exceptions.PlaceholderException;

import lombok.Getter;

import java.util.List;

public abstract class Placeholder {

    @Getter
    private final String name;
    @Getter
    private final int priority;

    public abstract MultiLineComponent apply(PlaceholderContext ctx,
                                             List<MultiLineComponent> args) throws PlaceholderException;

    public Placeholder(String name) {
        this(name, 0);
    }

    public Placeholder(String name, int priority) {
        this.name = name;
        this.priority = priority;
    }
}
