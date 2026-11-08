package io.github.frqnny.darkenchanting.network;

import io.github.frqnny.darkenchanting.DarkEnchanting;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

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
                .consumerMainThread(ApplyEnchantmentsPacket::handle)
                .add();
        CHANNEL.messageBuilder(ApplyRepairPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ApplyRepairPacket::encode)
                .decoder(ApplyRepairPacket::decode)
                .consumerMainThread(ApplyRepairPacket::handle)
                .add();
    }
}
