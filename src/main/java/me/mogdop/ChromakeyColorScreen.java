package me.mogdop.client;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ColorPickerComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import me.mogdop.ApplyColorPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class ChromakeyColorScreen extends BaseOwoScreen<FlowLayout> {

    private int selectedColor = 0xFF00FF33; // Цвет по умолчанию (Зеленый)

    @Override
    protected @NotNull Class<FlowLayout> rootLayoutType() {
        return FlowLayout.class;
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent
            .surface(Surface.VANILLA_TRANSLUCENT)
            .horizontalAlignment(HorizontalAlignment.CENTER)
            .verticalAlignment(VerticalAlignment.CENTER);

        // Основной вертикальный контейнер для элементов интерфейса
        FlowLayout container = Containers.verticalFlow(Sizing.content(), Sizing.content());
        container.surface(Surface.DARK_PANEL)
            .padding(Insets.all(12))
            .horizontalAlignment(HorizontalAlignment.CENTER);

        // Заголовок окна
        container.child(Components.label(Text.literal("Select Custom HEX Color")).margins(Insets.vertical(5)));

        // Компонент встроенной HSV палитры
        ColorPickerComponent colorPicker = new ColorPickerComponent();
        colorPicker.sizing(Sizing.fixed(150), Sizing.fixed(120));
        colorPicker.margins(Insets.vertical(8));

        // Поле ввода HEX-кода
        TextBoxComponent hexInput = Components.textBox(Sizing.fixed(100));
        hexInput.setText("#00FF33");
        hexInput.setMaxLength(7);

        // 1. При перетаскивании ползунка на палитре обновляем текст в поле ввода
        colorPicker.onColorSelected().subscribe(color -> {
            selectedColor = color.rgb();
            String hex = String.format("#%06X", (0xFFFFFF & selectedColor));
            hexInput.setText(hex);
        });

        // 2. При вводе валидного HEX-кода в поле автоматически перестраиваем палитру
        hexInput.onChanged().subscribe(text -> {
            if (text.matches("^#[0-9A-Fa-f]{6}$")) {
                try {
                    int parsedColor = Integer.parseInt(text.substring(1), 16);
                    selectedColor = parsedColor;
                    colorPicker.selectedColor(Color.ofRgb(parsedColor));
                } catch (NumberFormatException ignored) {}
            }
        });

        container.child(colorPicker);
        container.child(hexInput.margins(Insets.vertical(6)));
        
        // Кнопка подтверждения
        container.child(Components.button(Text.literal("Apply"), button -> {
            applyColor(selectedColor);
            this.close();
        }).margins(Insets.vertical(5)));

        rootComponent.child(container);
    }

    private void applyColor(int rgb) {
        // Отправляем пакет на сервер для сохранения цвета в предмет
        ClientPlayNetworking.send(new ApplyColorPayload(rgb));
    }
}