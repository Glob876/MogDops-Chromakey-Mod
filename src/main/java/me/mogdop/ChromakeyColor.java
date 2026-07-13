package me.mogdop;

import net.minecraft.util.StringIdentifiable;

public enum ChromakeyColor implements StringIdentifiable {
    GREEN("green", 0xFF00FF33), // Добавили FF в начало (непрозрачный зеленый)
    BLUE("blue", 0xFF0022FF),   // Добавили FF в начало (непрозрачный синий)
    RED("red", 0xFFFF0000);     // Добавили FF в начало (непрозрачный красный)

    private final String name;
    private final int colorHex;

    ChromakeyColor(String name, int colorHex) {
        this.name = name;
        this.colorHex = colorHex;
    }

    @Override
    public String asString() {
        return this.name;
    }

    public int getColorHex() {
        return this.colorHex;
    }
}