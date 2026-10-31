package me.mogdop.client;


import me.mogdop.ChromakeyColor;
import me.mogdop.ChromakeyMod;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import me.mogdop.ApplyColorPayload;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;

import net.minecraft.network.chat.Component;

import net.minecraft.core.BlockPos;

public class ChromakeyColorScreen extends Screen {
    private final BlockPos targetPos;
    private int selectedColor = 0x00FF33;
    private float hue = .3667f, saturation = 1, brightness = 1;
    private EditBox hexInput;
    private Button apply;
    private int left, top, dragging;
    private boolean updating;

    public ChromakeyColorScreen(BlockPos pos) {
        super(Component.literal("Chromakey Configuration"));
        targetPos = pos;
    }

    @Override
    protected void init() {
        left = (width - 310) / 2;
        top = (height - 218) / 2;
        hexInput = new EditBox(font, left + 14, top + 159, 130, 20, Component.literal("Hex color"));
        hexInput.setMaxLength(7);
        hexInput.setValue(String.format("#%06X", selectedColor));
        hexInput.setResponder(text -> {
            boolean valid = text.matches("^#[0-9A-Fa-f]{6}$");
            apply.active = valid;
            if (valid && !updating) setColor(Integer.parseInt(text.substring(1), 16), false);
        });
        addRenderableWidget(hexInput);
        ChromakeyColor[] colors = ChromakeyColor.values();
        for (int i = 0; i < colors.length; i++) {
            final ChromakeyColor color = colors[i];
            addRenderableWidget(Button.builder(Component.translatable("color.mogdops-chromakey-mod." + color.getSerializedName()), button -> setColor(color.getColorHex(), true))
                .bounds(left + 164 + (i % 2) * 66, top + 50 + (i / 2) * 24, 64, 20).build());
        }
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), b -> onClose()).bounds(left + 164, top + 190, 60, 20).build());
        apply = addRenderableWidget(Button.builder(Component.literal("Apply"), b -> {
            ClientPlayNetworking.send(new ApplyColorPayload(targetPos, selectedColor));
            onClose();
        }).bounds(left + 230, top + 190, 66, 20).build());
        apply.active = hexInput.getValue().matches("^#[0-9A-Fa-f]{6}$");
    }

    private void setColor(int rgb, boolean updateText) {
        selectedColor = rgb & 0xFFFFFF;
        float[] hsv = java.awt.Color.RGBtoHSB((rgb >> 16) & 255, (rgb >> 8) & 255, rgb & 255, null);
        hue = hsv[0]; saturation = hsv[1]; brightness = hsv[2];
        if (updateText) updateText();
    }

    private void updateText() {
        updating = true;
        hexInput.setValue(String.format("#%06X", selectedColor));
        updating = false;
    }

    private boolean pick(double x, double y) {
        if (dragging == 1) {
            saturation = (float) Math.max(0, Math.min(1, (x - left - 14) / 129));
            brightness = 1 - (float) Math.max(0, Math.min(1, (y - top - 50) / 89));
        } else if (dragging == 2) {
            hue = (float) Math.max(0, Math.min(1, (x - left - 14) / 129));
        } else return false;
        selectedColor = java.awt.Color.HSBtoRGB(hue, saturation, brightness) & 0xFFFFFF;
        updateText();
        return true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double x = event.x(), y = event.y(); int button = event.button();
        if (button == 0 && x >= left + 14 && x < left + 144) {
            if (y >= top + 50 && y < top + 140) dragging = 1;
            if (y >= top + 144 && y < top + 154) dragging = 2;
            if (pick(x, y)) { clearFocus(); return true; }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        double x = event.x(), y = event.y(); int button = event.button();
        return button == 0 && pick(x, y) || super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        dragging = 0;
        return super.mouseReleased(event);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        extractTransparentBackground(graphics);
        graphics.fill(left, top, left + 310, top + 218, 0xE0151515);
        graphics.text(font, title, left + 14, top + 14, 0xFFFFFFFF);
        graphics.text(font, "Custom Color", left + 14, top + 34, 0xFFAAAAAA);
        graphics.text(font, "Color Presets", left + 164, top + 34, 0xFFAAAAAA);
        for (int x = 0; x < 130; x += 2) {
            for (int y = 0; y < 90; y += 2)
                graphics.fill(left + 14 + x, top + 50 + y, left + 16 + x, top + 52 + y,
                    java.awt.Color.HSBtoRGB(hue, x / 129f, 1 - y / 89f));
            graphics.fill(left + 14 + x, top + 144, left + 16 + x, top + 154, java.awt.Color.HSBtoRGB(x / 129f, 1, 1));
        }
        int sx = left + 14 + Math.round(saturation * 129), sy = top + 50 + Math.round((1 - brightness) * 89);
        graphics.fill(sx - 2, sy - 2, sx + 3, sy + 3, 0xFF000000);
        graphics.fill(sx - 1, sy - 1, sx + 2, sy + 2, 0xFFFFFFFF);
        int hx = left + 14 + Math.round(hue * 129);
        graphics.fill(hx, top + 143, hx + 1, top + 155, 0xFFFFFFFF);
        graphics.fill(left + 14, top + 190, left + 144, top + 210, 0xFF000000 | selectedColor);
        
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override
    public void tick() {}
    @Override
    public boolean isPauseScreen() { return false; }
    @Override
    public void onClose() { minecraft.gui.setScreen(null); }
}
