package me.mogdop;

import net.minecraft.util.StringIdentifiable;

public enum ChromakeyColor implements StringIdentifiable {
    GREEN("green"),
    BLUE("blue"),
    RED("red");

    private final String name;

    ChromakeyColor(String name) {
        this.name = name;
    }

    @Override
    public String asString() {
        return this.name;
    }
}