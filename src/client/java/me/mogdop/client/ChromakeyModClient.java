package me.mogdop.client;

import me.mogdop.ChromakeyBlock;
import me.mogdop.ChromakeyColor;
import me.mogdop.ChromakeyMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;

public class ChromakeyModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Регистрируем окрашивание блока на основе его свойства COLOR
        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            if (tintIndex == 0) {
                ChromakeyColor color = state.get(ChromakeyBlock.COLOR);
                return color.getColorHex();
            }
            return -1; // Без окрашивания
        }, ChromakeyMod.GREEN_CHROMAKEY_BLOCK);
    }
}