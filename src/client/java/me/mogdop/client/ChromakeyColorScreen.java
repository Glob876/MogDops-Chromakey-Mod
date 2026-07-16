package me.mogdop.client;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.ColorPickerComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import me.mogdop.ApplyColorPayload;
import me.mogdop.ChromakeyColor;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.text.Text;
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
        rootComponent
            .surface(Surface.VANILLA_TRANSLUCENT)
            .horizontalAlignment(HorizontalAlignment.CENTER)
            .verticalAlignment(VerticalAlignment.CENTER);

        // Главное окно
        FlowLayout window = UIContainers.verticalFlow(Sizing.content(), Sizing.content());
        window.surface(Surface.DARK_PANEL).padding(Insets.of(12)).horizontalAlignment(HorizontalAlignment.CENTER);

        // Горизонтальный контейнер для разделения Палитры (Слева) и Пресетов (Справа)
        FlowLayout mainRow = UIContainers.horizontalFlow(Sizing.content(), Sizing.content());
        
        // --- ЛЕВАЯ ПАНЕЛЬ (Палитра) ---
        FlowLayout leftPanel = UIContainers.verticalFlow(Sizing.content(), Sizing.content());
        leftPanel.horizontalAlignment(HorizontalAlignment.CENTER);
        leftPanel.child(UIComponents.label(Text.literal("Custom HEX Color")).margins(Insets.bottom(8)));

        ColorPickerComponent colorPicker = new ColorPickerComponent();
        colorPicker.sizing(Sizing.fixed(140), Sizing.fixed(110));

        TextBoxComponent hexInput = UIComponents.textBox(Sizing.fixed(80));
        hexInput.setText("#00FF33");
        hexInput.setMaxLength(7);

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

        leftPanel.child(colorPicker);
        leftPanel.child(hexInput.margins(Insets.top(8)));

        // --- ПРАВАЯ ПАНЕЛЬ (Пресеты) ---
        FlowLayout rightPanel = UIContainers.verticalFlow(Sizing.content(), Sizing.content());
        rightPanel.margins(Insets.left(20)).horizontalAlignment(HorizontalAlignment.CENTER);
        rightPanel.child(UIComponents.label(Text.literal("Presets")).margins(Insets.bottom(8)));

        FlowLayout col1 = UIContainers.verticalFlow(Sizing.content(), Sizing.content());
        FlowLayout col2 = UIContainers.verticalFlow(Sizing.content(), Sizing.content());

        ChromakeyColor[] colors = ChromakeyColor.values();
        for (int i = 0; i < colors.length; i++) {
            ChromakeyColor cc = colors[i];
            ButtonComponent btn = UIComponents.button(Text.translatable("color.mogdops-chromakey-mod." + cc.asString()), b -> {
                selectedColor = cc.getColorHex();
                String hex = String.format("#%06X", (0xFFFFFF & selectedColor));
                hexInput.setText(hex);
                colorPicker.selectedColor(Color.ofRgb(selectedColor));
            });
            btn.sizing(Sizing.fixed(65), Sizing.fixed(20)).margins(Insets.of(2));
            if (i < 4) col1.child(btn); else col2.child(btn);
        }

        FlowLayout presetsGrid = UIContainers.horizontalFlow(Sizing.content(), Sizing.content());
        presetsGrid.child(col1).child(col2);
        rightPanel.child(presetsGrid);

        // --- Сборка ---
        mainRow.child(leftPanel).child(rightPanel);
        window.child(mainRow);
        
        window.child(UIComponents.button(Text.literal("Apply Color"), button -> {
            ClientPlayNetworking.send(new ApplyColorPayload(targetPos, selectedColor));
            this.close();
        }).margins(Insets.top(12)));

        rootComponent.child(window);
    }
}