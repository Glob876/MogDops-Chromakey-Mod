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
                // Если блок выключен (LIT = false), возвращаем -1, чтобы
                // частицы разрушения и сам блок оставались нейтрального серого цвета (chromakey_off)
                if (state != null && state.hasProperty(ChromakeyBlock.LIT) && !state.getValue(ChromakeyBlock.LIT)) {
                    return -1;
                }
                
                ChromakeyColor color = state.getValue(ChromakeyBlock.COLOR);
                return color.getColorHex();
            }
            return -1; // Без окрашивания
        }, ChromakeyMod.GREEN_CHROMAKEY_BLOCK);
    }
}