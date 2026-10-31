package me.mogdop.client;

import me.mogdop.ChromakeyBlock;
import me.mogdop.ChromakeyColor;
import me.mogdop.ChromakeyMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class ChromakeyModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(me.mogdop.OpenColorScreenPayload.ID,
            (payload, context) -> context.client().gui.setScreen(new ChromakeyColorScreen(payload.pos())));
        // Регистрируем окрашивание блока через BlockColorRegistry для 26.2
        BlockColorRegistry.register(List.of(new BlockTintSource() {
            @Override
            public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
                // Если блок выключен (LIT = false), возвращаем -1, чтобы
                // частицы разрушения и сам блок оставались нейтрального серого цвета (chromakey_off)
                if (state != null && state.hasProperty(ChromakeyBlock.LIT) && !state.getValue(ChromakeyBlock.LIT)) {
                    return -1;
                }
                
                if (level != null && pos != null && level.getBlockEntity(pos) instanceof me.mogdop.ChromakeyBlockEntity entity
                    && entity.getCustomColor() != -1) return 0xFF000000 | entity.getCustomColor();
                ChromakeyColor color = state.getValue(ChromakeyBlock.COLOR);
                return color.getColorHex();
            }

            @Override
            public int color(BlockState state) {
                ChromakeyColor color = state.getValue(ChromakeyBlock.COLOR);
                return color.getColorHex();
            }
        }), ChromakeyMod.GREEN_CHROMAKEY_BLOCK);
    }
}