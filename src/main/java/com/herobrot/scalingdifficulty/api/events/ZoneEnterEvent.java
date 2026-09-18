package com.herobrot.scalingdifficulty.api.events;

import com.herobrot.scalingdifficulty.network.payload.ZoneSyncPayload;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

import javax.annotation.Nullable;

/**
 * A client event is triggered on the NeoForge bus every time the player
 * enters or exits a difficulty zone. getZone() returns null when the player is outside any zone.
 * Canceling the event only stops it from propagating to the remaining listeners;
 * the tracker's internal state is not reverted.
 */
public class ZoneEnterEvent extends LivingEvent implements ICancellableEvent {

    @Nullable
    private final ZoneSyncPayload.ZoneEntry zone;

    public ZoneEnterEvent(LivingEntity entity, @Nullable ZoneSyncPayload.ZoneEntry zone) {
        super(entity);
        this.zone = zone;
    }

    @Nullable
    public ZoneSyncPayload.ZoneEntry getZone() { return zone; }
}