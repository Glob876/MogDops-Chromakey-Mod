package me.mogdop;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class ChromakeyControllerItem extends Item {
    public ChromakeyControllerItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = world.getBlockState(pos);
        Player player = context.getPlayer();

        if (state.getBlock() instanceof ChromakeyBlock chromakeyBlock) {
            if (!world.isClientSide()) {
                ChromakeyColor currentColor = state.getValue(ChromakeyBlock.COLOR);
                ChromakeyColor nextColor;

                // Проверяем, зажат ли у игрока SHIFT (Sneaking)
                if (player != null && player.isShiftKeyDown()) {
                    // Переключаем ТОЛЬКО основные цвета хромакея в порядке: GREEN -> BLUE -> RED -> GREEN
                    nextColor = switch (currentColor) {
                        case GREEN -> ChromakeyColor.BLUE;
                        case BLUE -> ChromakeyColor.RED;
                        default -> ChromakeyColor.GREEN; // Сбрасывает любые другие декоративные цвета на GREEN
                    };
                } else {
                    // Обычное нажатие ПКМ: перебираем все доступные цвета по полному кругу
                    ChromakeyColor[] colors = ChromakeyColor.values();
                    int nextIndex = (currentColor.ordinal() + 1) % colors.length;
                    ChromakeyColor nextColorResult = colors[nextIndex];
                    nextColor = nextColorResult;
                }

                BlockState newState = state.setValue(ChromakeyBlock.COLOR, nextColor);
                world.setBlock(pos, newState, 3);
                chromakeyBlock.propagateState(world, pos, newState); // Перекрашиваем всю стену!

                // Отправляем сообщение только если в конфиге включен параметр showColorChangeMessage
                if (player != null && ChromakeyConfig.showColorChangeMessage) {
                    player.sendSystemMessage(Component.translatable("message.mogdops-chromakey-mod.color_changed", 
                        Component.translatable("color.mogdops-chromakey-mod." + nextColor.getSerializedName())
                    ).withStyle(ChatFormatting.GREEN));
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}