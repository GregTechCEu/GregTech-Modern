package com.gregtechceu.gtceu.api.mui.theme;

import net.minecraftforge.eventbus.api.Event;

public class ReloadThemeEvent extends Event {

    public static class Pre extends ReloadThemeEvent {}

    public static class Post extends ReloadThemeEvent {}
}
