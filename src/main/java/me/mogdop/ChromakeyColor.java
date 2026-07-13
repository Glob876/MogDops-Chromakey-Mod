package me.mogdop;

import net.minecraft.util.StringIdentifiable;

public enum ChromakeyColor implements StringIdentifiable {
    GREEN("green", 0xFF00FF33),     // Зеленый хромакей
    BLUE("blue", 0xFF0022FF),       // Синий хромакей
    RED("red", 0xFFFF0000),         // Красный хромакей
    YELLOW("yellow", 0xFFFFFF00),   // Желтый
    CYAN("cyan", 0xFF00FFFF),       // Бирюзовый / Голубой
    MAGENTA("magenta", 0xFFFF00FF), // Пурпурный / Розовый
    WHITE("white", 0xFFFFFFFF),     // Белый
    BLACK("black", 0xFF111111);     // Темно-серый / Черный (0x111111, чтобы сохранялся рельеф)

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