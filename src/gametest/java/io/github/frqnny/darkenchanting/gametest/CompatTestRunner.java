package io.github.frqnny.darkenchanting.gametest;

import io.github.frqnny.darkenchanting.DarkEnchanting;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Runs the compatibility tests on a dedicated dev server (`./gradlew runServer -PcompatTests`) once the world is
 * loaded, logs the results and stops the server. 1.16.5 has no GameTest framework, so this replaces gameTestServer.
 * A failed run exits with status 1 so Gradle reports the failure.
 */
@Mod.EventBusSubscriber(modid = DarkEnchanting.MODID)
public final class CompatTestRunner {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String PROPERTY = "dark_enchanting.compatTests";
    private static boolean failed;

    private CompatTestRunner() {
    }

    @SubscribeEvent
    public static void onServerStarted(FMLServerStartedEvent event) {
        if (!Boolean.getBoolean(PROPERTY)) {
            return;
        }

        MinecraftServer server = event.getServer();
        ServerWorld level = server.overworld();
        BlockPos base = level.getSharedSpawnPos().above(100);

        List<Method> tests = Arrays.stream(MajruszCompatTests.class.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()) && Modifier.isStatic(m.getModifiers()))
                .filter(m -> m.getParameterCount() == 1 && m.getParameterTypes()[0] == TestHelper.class)
                .sorted(Comparator.comparing(Method::getName))
                .collect(Collectors.toList());

        List<String> failures = new ArrayList<>();
        for (int i = 0; i < tests.size(); i++) {
            Method test = tests.get(i);
            // Each test gets its own spot so placed blocks never overlap.
            TestHelper helper = new TestHelper(level, base.offset(i * 16, 0, 0));
            try {
                test.invoke(null, helper);
            } catch (InvocationTargetException e) {
                LOGGER.error("Compat test {} threw", test.getName(), e.getCause());
                helper.fail("threw " + e.getCause());
            } catch (IllegalAccessException e) {
                helper.fail("could not run: " + e);
            }

            if (helper.failure() == null) {
                LOGGER.info("[compat] PASS {}", test.getName());
            } else {
                failures.add(test.getName() + ": " + helper.failure());
            }
        }

        LOGGER.info("========= {} COMPAT TESTS COMPLETE ======================", tests.size());
        if (failures.isEmpty()) {
            LOGGER.info("All {} compat tests passed :)", tests.size());
        } else {
            LOGGER.error("{} compat tests failed :(", failures.size());
            failures.forEach(f -> LOGGER.error("   - {}", f));
        }
        failed = !failures.isEmpty() || tests.isEmpty();
        server.halt(false);
    }

    @SubscribeEvent
    public static void onServerStopped(FMLServerStoppedEvent event) {
        if (Boolean.getBoolean(PROPERTY) && failed) {
            LOGGER.error("Compat tests failed; exiting with status 1");
            Runtime.getRuntime().halt(1);
        }
    }
}
