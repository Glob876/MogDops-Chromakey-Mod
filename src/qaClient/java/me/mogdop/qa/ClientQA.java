package me.mogdop.qa;

import me.mogdop.*;
import me.mogdop.client.ChromakeyColorScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.LevelSettings;

import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.item.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import java.nio.file.*;

/** Runs only in a disposable development world; never shipped in the release. */
public class ClientQA implements ClientModInitializer {
    static final BlockPos TARGET = new BlockPos(0, 81, 0);
    static final BlockPos ISOLATED = new BlockPos(4, 81, 0);
    static final int RGB = 0x1234AB;
    int ticks, phase, phaseTicks;
    boolean started;
    volatile boolean prepared, applied;
    Path dir;
    static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    @Override public void onInitializeClient() {
        check(Boolean.getBoolean("chromakey.qa"), "Isolated QA only");
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            try {
                dir = mc.gameDirectory.toPath(); ticks++;
                if (ticks == 1) { Files.deleteIfExists(dir.resolve("PASSED.txt")); Files.deleteIfExists(dir.resolve("FAILED.txt")); }
                mc.options.pauseOnLostFocus = false;
                if (ticks > 6000) throw new AssertionError("QA timeout");
                if (!started && ticks > 60 && mc.gui.overlay() == null) {
                    started = true;
                    mc.options.renderDistance().set(3);
                    var info = new LevelSettings("Chromakey QA", GameType.CREATIVE,
                        new LevelSettings.DifficultySettings(Difficulty.PEACEFUL, false, false), true, WorldDataConfiguration.DEFAULT);
                    mc.options.onboardAccessibility = false;
                    mc.createWorldOpenFlows().createFreshLevel("Chromakey-QA-" + System.currentTimeMillis(), info,
                        new WorldOptions(123L, false, false), r -> r.lookupOrThrow(net.minecraft.core.registries.Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT).value().createWorldDimensions(), new TitleScreen());
                    return;
                }
                if (mc.player == null || mc.level == null) return;
                if (phase == 0) {
                    phase = 1;
                    var server = mc.getSingleplayerServer();
                    server.execute(() -> {
                        try {
                            var world = server.overworld();
                            var block = (ChromakeyBlock) ChromakeyMod.GREEN_CHROMAKEY_BLOCK;
                            for (BlockPos pos : new BlockPos[]{TARGET, TARGET.east(), TARGET.above(), ISOLATED}) world.setBlock(pos, block.defaultBlockState(), 3);
                            block.propagateState(world, TARGET, true, 0xAA5511);
                            check(world.getBlockState(TARGET.east()).getValue(ChromakeyBlock.LIT), "connected lighting");
                            check(!world.getBlockState(ISOLATED).getValue(ChromakeyBlock.LIT), "isolated lighting");
                            var be = (ChromakeyBlockEntity) world.getBlockEntity(TARGET.east());
                            check(be.getCustomColor() == 0xAA5511, "connected color");
                            var copy = (ChromakeyBlockEntity) net.minecraft.world.level.block.entity.BlockEntity.loadStatic(TARGET, block.defaultBlockState(),
                                be.saveWithFullMetadata(world.registryAccess()), world.registryAccess());
                            check(copy.getCustomColor() == 0xAA5511, "NBT color persistence");
                            var player = server.getPlayerList().getPlayer(mc.player.getUUID());
                            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ChromakeyMod.CHROMAKEY_CONTROLLER));
                            player.connection.teleport(.5, 80, -4, 0, 0);
                            world.setBlock(new BlockPos(0,79,-4), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(), 3);
                            prepared = true;
                        } catch (Throwable e) { fail(e); mc.execute(mc::stop); }
                    });
                }
                if (!prepared) return;
                phaseTicks++;
                if (phase == 1 && phaseTicks > 40) {
                    phase = 2; phaseTicks = 0;
                    mc.getSingleplayerServer().execute(() -> {
                        var player = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                        ChromakeyMod.CHROMAKEY_CONTROLLER.useOn(new net.minecraft.world.item.context.UseOnContext(player, InteractionHand.MAIN_HAND,
                            new BlockHitResult(Vec3.atCenterOf(TARGET), Direction.NORTH, TARGET, false)));
                    });
                } else if (phase == 2 && phaseTicks > 30) {
                    check(mc.gui.screen() instanceof ChromakeyColorScreen, "controller opens screen through S2C packet");
                    Screenshot.grab(mc, false);
                    mc.gui.setScreen(null);
                    ClientPlayNetworking.send(new ApplyColorPayload(TARGET, RGB));
                    phase = 3; phaseTicks = 0;
                } else if (phase == 3 && phaseTicks > 40) {
                    var be = (ChromakeyBlockEntity) mc.level.getBlockEntity(TARGET.east());
                    check(be.getCustomColor() == RGB, "C2S color edit and S2C block entity sync");
                    check(((ChromakeyBlockEntity) mc.level.getBlockEntity(ISOLATED)).getCustomColor() == -1, "disconnected color unchanged");
                    Screenshot.grab(mc, false);
                    Files.writeString(dir.resolve("PASSED.txt"), "PASS: startup, registries, resources, recipes, connected color/light, isolated blocks, NBT persistence, controller menu, bidirectional networking, client color sync\n");
                    mc.stop(); phase = 4;
                }
            } catch (Throwable e) { fail(e); mc.stop(); }
        });
    }
    void fail(Throwable e) {
        e.printStackTrace();
        try { Files.writeString(dir.resolve("FAILED.txt"), e.toString()); } catch (Exception ignored) {}
    }
}
