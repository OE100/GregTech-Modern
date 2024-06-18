package com.gregtechceu.gtceu.common.network.packets.hazard;

import com.gregtechceu.gtceu.client.EnvironmentalHazardClientHandler;
import com.gregtechceu.gtceu.common.capability.EnvironmentalHazardSavedData;

import com.lowdragmc.lowdraglib.networking.IHandlerContext;
import com.lowdragmc.lowdraglib.networking.IPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class SPacketAddHazardZone implements IPacket {

    private ChunkPos pos;
    private EnvironmentalHazardSavedData.HazardZone zone;

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarLong(pos.toLong());
        zone.toNetwork(buf);
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        long packed = buf.readVarLong();
        int x = ChunkPos.getX(packed);
        int z = ChunkPos.getZ(packed);
        pos = new ChunkPos(x, z);
        zone = EnvironmentalHazardSavedData.HazardZone.fromNetwork(buf);
    }

    @Override
    public void execute(IHandlerContext handler) {
        if (handler.isClient()) {
            EnvironmentalHazardClientHandler.INSTANCE.addHazardZone(pos, zone);
        }
    }
}