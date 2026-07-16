package me.mogdop;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ChromakeyControllerItem extends Item {
    public ChromakeyControllerItem(Settings settings) {
        super(settings);
    }

    // Shift + ПКМ по воздуху переключает режим контроллера
    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user.isSneaking()) {
            if (!world.isClient()) {
                int mode = 0; // 0 = Стандартный, 1 = Кастомный цвет
                NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
                NbtCompound nbt = customData != null ? customData.copyNbt() : new NbtCompound();
                mode = nbt.getInt("mode");

                // Переключаем режим по кругу
                mode = mode == 0 ? 1 : 0;
                nbt.putInt("mode", mode);
                stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                if (mode == 1) {
                    user.sendMessage(Text.literal("Controller mode: Custom HEX Color").formatted(Formatting.AQUA), true);
                    // Отправляем пакет клиенту для открытия GUI палитры
                    ServerPlayNetworking.send((ServerPlayerEntity) user, new OpenColorScreenPayload());
                } else {
                    user.sendMessage(Text.literal("Controller mode: Standard Color").formatted(Formatting.GOLD), true);
                }
            }
            return ActionResult.SUCCESS;
        }
        return super.use(world, user, hand);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getStack();

        if (state.getBlock() instanceof ChromakeyBlock chromakeyBlock) {
            if (!world.isClient()) {
                // Считываем текущий режим и сохраненный цвет из NBT предмета через NbtComponent
                int mode = 0;
                int customColor = -1;
                NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
                if (customData != null) {
                    NbtCompound nbt = customData.copyNbt();
                    mode = nbt.getInt("mode");
                    customColor = nbt.contains("custom_color") ? nbt.getInt("custom_color") : -1;
                }

                if (mode == 1) {
                    // Режим 1: Окрашиваем всю структуру в кастомный цвет
                    chromakeyBlock.propagateState(world, pos, state, customColor);
                } else {
                    // Режим 0: Стандартная логика перебора цветов
                    ChromakeyColor currentColor = state.get(ChromakeyBlock.COLOR);
                    ChromakeyColor nextColor;

                    if (player != null && player.isSneaking()) {
                        nextColor = switch (currentColor) {
                            case GREEN -> ChromakeyColor.BLUE;
                            case BLUE -> ChromakeyColor.RED;
                            default -> ChromakeyColor.GREEN;
                        };
                    } else {
                        ChromakeyColor[] colors = ChromakeyColor.values();
                        int nextIndex = (currentColor.ordinal() + 1) % colors.length;
                        nextColor = colors[nextIndex];
                    }

                    BlockState newState = state.with(ChromakeyBlock.COLOR, nextColor);
                    world.setBlockState(pos, newState);
                    // Сбрасываем кастомный цвет блока обратно на -1
                    chromakeyBlock.propagateState(world, pos, newState, -1);

                    if (player != null && ChromakeyConfig.showColorChangeMessage) {
                        player.sendMessage(Text.translatable("message.mogdops-chromakey-mod.color_changed", 
                            Text.translatable("color.mogdops-chromakey-mod." + nextColor.asString())
                        ).formatted(Formatting.GREEN), true);
                    }
                }
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }
}