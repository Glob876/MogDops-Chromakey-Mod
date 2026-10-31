package me.mogdop.client;

import com.mojang.blaze3d.systems.RenderSystem;
import me.mogdop.ChromakeyColor;
import me.mogdop.ChromakeyMod;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;

public class ChromakeyColorScreen extends Screen {
    private final BlockPos targetPos;
    private int selectedColor = 0x00FF33;
    private float hue = .3667f, saturation = 1, brightness = 1;
    private TextFieldWidget hexInput;
    private ButtonWidget apply;
    private int left, top, dragging;
    private boolean updating;

    public ChromakeyColorScreen(BlockPos pos) {
        super(new LiteralText("Chromakey Configuration"));
        targetPos = pos;
    }

    @Override
    protected void init() {
        left = (width - 310) / 2;
        top = (height - 218) / 2;
        hexInput = new TextFieldWidget(textRenderer, left + 14, top + 159, 130, 20, new LiteralText("Hex color"));
        hexInput.setMaxLength(7);
        hexInput.setText(String.format("#%06X", selectedColor));
        hexInput.setChangedListener(text -> {
            boolean valid = text.matches("^#[0-9A-Fa-f]{6}$");
            apply.active = valid;
            if (valid && !updating) setColor(Integer.parseInt(text.substring(1), 16), false);
        });
        children.add(hexInput);
        ChromakeyColor[] colors = ChromakeyColor.values();
        for (int i = 0; i < colors.length; i++) {
            final ChromakeyColor color = colors[i];
            addButton(new ButtonWidget(left + 164 + (i % 2) * 66, top + 50 + (i / 2) * 24, 64, 20,
                new TranslatableText("color.mogdops-chromakey-mod." + color.asString()), button -> setColor(color.getColorHex(), true)));
        }
        addButton(new ButtonWidget(left + 164, top + 190, 60, 20, new TranslatableText("gui.cancel"), b -> onClose()));
        apply = addButton(new ButtonWidget(left + 230, top + 190, 66, 20, new LiteralText("Apply"), b -> {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBlockPos(targetPos); buf.writeInt(selectedColor);
            ClientPlayNetworking.send(ChromakeyMod.APPLY_COLOR, buf);
            onClose();
        }));
        apply.active = hexInput.getText().matches("^#[0-9A-Fa-f]{6}$");
    }

    private void setColor(int rgb, boolean updateText) {
        selectedColor = rgb & 0xFFFFFF;
        float[] hsv = java.awt.Color.RGBtoHSB((rgb >> 16) & 255, (rgb >> 8) & 255, rgb & 255, null);
        hue = hsv[0]; saturation = hsv[1]; brightness = hsv[2];
        if (updateText) updateText();
    }

    private void updateText() {
        updating = true;
        hexInput.setText(String.format("#%06X", selectedColor));
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
    public boolean mouseClicked(double x, double y, int button) {
        if (button == 0 && x >= left + 14 && x < left + 144) {
            if (y >= top + 50 && y < top + 140) dragging = 1;
            if (y >= top + 144 && y < top + 154) dragging = 2;
            if (pick(x, y)) { setFocused(null); return true; }
        }
        return super.mouseClicked(x, y, button);
    }

    @Override
    public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
        return button == 0 && pick(x, y) || super.mouseDragged(x, y, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        dragging = 0;
        return super.mouseReleased(x, y, button);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        fill(matrices, left, top, left + 310, top + 218, 0xE0151515);
        drawStringWithShadow(matrices, textRenderer, title.getString(), left + 14, top + 14, 0xFFFFFF);
        drawStringWithShadow(matrices, textRenderer, "Custom Color", left + 14, top + 34, 0xAAAAAA);
        drawStringWithShadow(matrices, textRenderer, "Color Presets", left + 164, top + 34, 0xAAAAAA);
        for (int x = 0; x < 130; x += 2) {
            for (int y = 0; y < 90; y += 2)
                fill(matrices, left + 14 + x, top + 50 + y, left + 16 + x, top + 52 + y,
                    java.awt.Color.HSBtoRGB(hue, x / 129f, 1 - y / 89f));
            fill(matrices, left + 14 + x, top + 144, left + 16 + x, top + 154, java.awt.Color.HSBtoRGB(x / 129f, 1, 1));
        }
        int sx = left + 14 + Math.round(saturation * 129), sy = top + 50 + Math.round((1 - brightness) * 89);
        fill(matrices, sx - 2, sy - 2, sx + 3, sy + 3, 0xFF000000);
        fill(matrices, sx - 1, sy - 1, sx + 2, sy + 2, 0xFFFFFFFF);
        int hx = left + 14 + Math.round(hue * 129);
        fill(matrices, hx, top + 143, hx + 1, top + 155, 0xFFFFFFFF);
        fill(matrices, left + 14, top + 190, left + 144, top + 210, 0xFF000000 | selectedColor);
        hexInput.render(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void tick() { hexInput.tick(); }
    @Override
    public boolean isPauseScreen() { return false; }
    @Override
    public void onClose() { client.openScreen(null); }
}
