package io.github.frqnny.darkenchanting.network;

import io.github.frqnny.darkenchanting.DarkEnchanting;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class ModPackets {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            DarkEnchanting.id("main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void init() {
        int id = 0;
        CHANNEL.messageBuilder(ApplyEnchantmentsPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ApplyEnchantmentsPacket::encode)
                .decoder(ApplyEnchantmentsPacket::decode)
                .consumer(onMainThread(ApplyEnchantmentsPacket::handle))
                .add();
        CHANNEL.messageBuilder(ApplyRepairPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ApplyRepairPacket::encode)
                .decoder(ApplyRepairPacket::decode)
                .consumer(onMainThread(ApplyRepairPacket::handle))
                .add();
    }

    /**
     * 1.18.2 has no MessageBuilder#consumerMainThread: run the handler on the main thread and mark the packet handled.
     */
    private static <MSG> BiConsumer<MSG, Supplier<NetworkEvent.Context>> onMainThread(BiConsumer<MSG, Supplier<NetworkEvent.Context>> handler) {
        return (message, ctx) -> {
            ctx.get().enqueueWork(() -> handler.accept(message, ctx));
            ctx.get().setPacketHandled(true);
        };
    }
}
