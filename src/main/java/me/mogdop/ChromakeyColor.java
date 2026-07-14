package me.mogdop;

import net.minecraft.util.StringRepresentable;

public enum ChromakeyColor implements StringRepresentable {
    GREEN("green", 0xFF00FF33),
    BLUE("blue", 0xFF0022FF),
    RED("red", 0xFFFF0000),
    YELLOW("yellow", 0xFFFFFF00),
    CYAN("cyan", 0xFF00FFFF),
    MAGENTA("magenta", 0xFFFF00FF),
    WHITE("white", 0xFFFFFFFF),
    BLACK("black", 0xFF111111);

    private final String name;
    private final int colorHex;

    ChromakeyColor(String name, int colorHex) {
        this.name = name;
        this.colorHex = colorHex;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public int getColorHex() {
        return this.colorHex;
    }
}