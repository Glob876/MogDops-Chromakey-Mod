package me.mogdop.qa;

import me.mogdop.*;
import me.mogdop.client.ChromakeyColorScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
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
                dir = mc.runDirectory.toPath(); ticks++;
                if (ticks == 1) { Files.deleteIfExists(dir.resolve("PASSED.txt")); Files.deleteIfExists(dir.resolve("FAILED.txt")); }
                mc.options.pauseOnLostFocus = false;
                if (ticks > 6000) throw new AssertionError("QA timeout");
                if (!started && ticks > 60 && mc.getOverlay() == null) {
                    started = true;
                    mc.options.getViewDistance().setValue(3);
                    var info = new LevelInfo("Chromakey QA", GameMode.CREATIVE, false, Difficulty.PEACEFUL, true, new GameRules(DataConfiguration.SAFE_MODE.enabledFeatures()), DataConfiguration.SAFE_MODE);
                    mc.createIntegratedServerLoader().createAndStart("Chromakey-QA-" + System.currentTimeMillis(), info,
                        new GeneratorOptions(123L, false, false), WorldPresets::createTestOptions, new TitleScreen());
                    return;
                }
                if (mc.player == null || mc.world == null) return;
                if (phase == 0) {
                    phase = 1;
                    var server = mc.getServer();
                    server.execute(() -> {
                        try {
                            var world = server.getOverworld();
                            var block = (ChromakeyBlock) ChromakeyMod.GREEN_CHROMAKEY_BLOCK;
                            for (BlockPos pos : new BlockPos[]{TARGET, TARGET.east(), TARGET.up(), ISOLATED}) world.setBlockState(pos, block.getDefaultState());
                            block.propagateState(world, TARGET, true, 0xAA5511);
                            check(world.getBlockState(TARGET.east()).get(ChromakeyBlock.LIT), "connected lighting");
                            check(!world.getBlockState(ISOLATED).get(ChromakeyBlock.LIT), "isolated lighting");
                            var be = (ChromakeyBlockEntity) world.getBlockEntity(TARGET.east());
                            check(be.getCustomColor() == 0xAA5511, "connected color");
                            var copy = new ChromakeyBlockEntity(TARGET, block.getDefaultState());
                            copy.read(be.createNbt(world.getRegistryManager()), world.getRegistryManager());
                            check(copy.getCustomColor() == 0xAA5511, "NBT color persistence");
                            var player = server.getPlayerManager().getPlayer(mc.player.getUuid());
                            player.setStackInHand(Hand.MAIN_HAND, new ItemStack(ChromakeyMod.CHROMAKEY_CONTROLLER));
                            player.networkHandler.requestTeleport(.5, 80, -4, 0, 0);
                            world.setBlockState(new BlockPos(0,79,-4), net.minecraft.block.Blocks.STONE.getDefaultState());
                            prepared = true;
                        } catch (Throwable e) { fail(e); mc.execute(mc::scheduleStop); }
                    });
                }
                if (!prepared) return;
                phaseTicks++;
                if (phase == 1 && phaseTicks > 40) {
                    phase = 2; phaseTicks = 0;
                    mc.getServer().execute(() -> {
                        var player = mc.getServer().getPlayerManager().getPlayer(mc.player.getUuid());
                        ChromakeyMod.CHROMAKEY_CONTROLLER.useOnBlock(new ItemUsageContext(player, Hand.MAIN_HAND,
                            new BlockHitResult(Vec3d.ofCenter(TARGET), Direction.NORTH, TARGET, false)));
                    });
                } else if (phase == 2 && phaseTicks > 30) {
                    check(mc.currentScreen instanceof ChromakeyColorScreen, "controller opens screen through S2C packet");
                    ScreenshotRecorder.saveScreenshot(mc.runDirectory, "chromakey-menu.png", mc.getFramebuffer(), t -> {});
                    mc.setScreen(null);
                    ClientPlayNetworking.send(new ApplyColorPayload(TARGET, RGB));
                    phase = 3; phaseTicks = 0;
                } else if (phase == 3 && phaseTicks > 40) {
                    var be = (ChromakeyBlockEntity) mc.world.getBlockEntity(TARGET.east());
                    check(be.getCustomColor() == RGB, "C2S color edit and S2C block entity sync");
                    check(((ChromakeyBlockEntity) mc.world.getBlockEntity(ISOLATED)).getCustomColor() == -1, "disconnected color unchanged");
                    ScreenshotRecorder.saveScreenshot(mc.runDirectory, "chromakey-wall.png", mc.getFramebuffer(), t -> {});
                    Files.writeString(dir.resolve("PASSED.txt"), "PASS: startup, registries, resources, recipes, connected color/light, isolated blocks, NBT persistence, controller menu, bidirectional networking, client color sync\n");
                    mc.scheduleStop(); phase = 4;
                }
            } catch (Throwable e) { fail(e); mc.scheduleStop(); }
        });
    }
    void fail(Throwable e) {
        e.printStackTrace();
        try { Files.writeString(dir.resolve("FAILED.txt"), e.toString()); } catch (Exception ignored) {}
    }
}
