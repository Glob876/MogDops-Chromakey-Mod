package me.mogdop.client;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.ColorPickerComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.GridLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import me.mogdop.ApplyColorPayload;
import me.mogdop.ChromakeyColor;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class ChromakeyColorScreen extends BaseOwoScreen<FlowLayout> {

    private final BlockPos targetPos;
    private int selectedColor = 0xFF00FF33; // Цвет по умолчанию

    public ChromakeyColorScreen(BlockPos pos) {
        this.targetPos = pos;
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, UIContainers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        // Темный полупрозрачный фон на весь экран (фокус на самом окне)
        rootComponent
            .surface(Surface.flat(0x99000000))
            .horizontalAlignment(HorizontalAlignment.CENTER)
            .verticalAlignment(VerticalAlignment.CENTER);

        // Главное окно в стиле Sodium (плоский темный дизайн с тонкой рамкой)
        FlowLayout window = UIContainers.verticalFlow(Sizing.content(), Sizing.content());
        window.surface(Surface.flat(0xFF161616).and(Surface.outline(0xFF333333)));
        window.padding(Insets.of(16));

        // --- ШАПКА ОКНА (Header) ---
        FlowLayout header = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.content());
        header.child(UIComponents.label(Text.literal("Chromakey Configuration").formatted(Formatting.WHITE, Formatting.BOLD)).shadow(true));
        
        // Разделительная линия
        FlowLayout separatorTop = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fixed(1));
        separatorTop.surface(Surface.flat(0xFF333333)).margins(Insets.vertical(10));

        // --- ОСНОВНОЙ КОНТЕНТ (Две колонки) ---
        FlowLayout content = UIContainers.horizontalFlow(Sizing.content(), Sizing.content());
        
        // Левая колонка (Палитра)
        FlowLayout leftPanel = UIContainers.verticalFlow(Sizing.content(), Sizing.content());
        leftPanel.horizontalAlignment(HorizontalAlignment.CENTER);
        leftPanel.child(UIComponents.label(Text.literal("Custom Color").formatted(Formatting.GRAY)).margins(Insets.bottom(8)));

        ColorPickerComponent colorPicker = new ColorPickerComponent();
        colorPicker.sizing(Sizing.fixed(140), Sizing.fixed(120));

        TextBoxComponent hexInput = UIComponents.textBox(Sizing.fixed(140));
        hexInput.setText("#00FF33");
        hexInput.setMaxLength(7);
        hexInput.margins(Insets.top(8));

        colorPicker.onChanged().subscribe(color -> {
            selectedColor = color.rgb();
            String hex = String.format("#%06X", (0xFFFFFF & selectedColor));
            if (!hexInput.getText().equalsIgnoreCase(hex)) {
                hexInput.setText(hex);
            }
        });

        hexInput.onChanged().subscribe(text -> {
            if (text.matches("^#[0-9A-Fa-f]{6}$")) {
                try {
                    int parsedColor = Integer.parseInt(text.substring(1), 16);
                    selectedColor = parsedColor;
                    colorPicker.selectedColor(Color.ofRgb(parsedColor));
                } catch (NumberFormatException ignored) {}
            }
        });

        leftPanel.child(colorPicker).child(hexInput);

        // Правая колонка (Пресеты)
        FlowLayout rightPanel = UIContainers.verticalFlow(Sizing.content(), Sizing.content());
        rightPanel.margins(Insets.left(24));
        rightPanel.child(UIComponents.label(Text.literal("Color Presets").formatted(Formatting.GRAY)).margins(Insets.bottom(8)));

        // Сетка 4x2 для кнопок
        GridLayout presetGrid = UIContainers.grid(Sizing.content(), Sizing.content(), 4, 2);
        ChromakeyColor[] colors = ChromakeyColor.values();
        for (int i = 0; i < colors.length; i++) {
            ChromakeyColor cc = colors[i];
            ButtonComponent btn = UIComponents.button(Text.translatable("color.mogdops-chromakey-mod." + cc.asString()), b -> {
                selectedColor = cc.getColorHex();
                String hex = String.format("#%06X", (0xFFFFFF & selectedColor));
                hexInput.setText(hex);
                colorPicker.selectedColor(Color.ofRgb(selectedColor));
            });
            btn.sizing(Sizing.fixed(70), Sizing.fixed(20)).margins(Insets.of(3));
            presetGrid.child(btn, i / 2, i % 2); // Строка, Колонка
        }
        rightPanel.child(presetGrid);

        content.child(leftPanel).child(rightPanel);

        // --- ПОДВАЛ (Footer с кнопками действия) ---
        FlowLayout separatorBottom = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fixed(1));
        separatorBottom.surface(Surface.flat(0xFF333333)).margins(Insets.vertical(10));

        FlowLayout footer = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.content());
        footer.horizontalAlignment(HorizontalAlignment.RIGHT);
        
        ButtonComponent cancelBtn = UIComponents.button(Text.literal("Cancel"), b -> this.close());
        cancelBtn.sizing(Sizing.fixed(60), Sizing.fixed(20)).margins(Insets.right(8));
        
        ButtonComponent applyBtn = UIComponents.button(Text.literal("Apply").formatted(Formatting.GREEN, Formatting.BOLD), b -> {
            ClientPlayNetworking.send(new ApplyColorPayload(targetPos, selectedColor));
            this.close();
        });
        applyBtn.sizing(Sizing.fixed(80), Sizing.fixed(20));

        footer.child(cancelBtn).child(applyBtn);

        // --- СБОРКА ОКНА ---
        window.child(header);
        window.child(separatorTop);
        window.child(content);
        window.child(separatorBottom);
        window.child(footer);

        rootComponent.child(window);
    }
}