package com.herobrot.scalingdifficulty.zone;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DifficultyZoneSavedData extends SavedData {

    private static final String DATA_NAME = "scalingdifficulty_zones";
    // Fallback para evitar consumo excesivo al indexar por chunks
    private static final int MAX_INDEXED_CHUNKS = 4096;

    private final List<DifficultyZone> zones = new ArrayList<>();
    private final Long2ObjectMap<List<DifficultyZone>> chunkIndex = new Long2ObjectOpenHashMap<>();
    private final Map<String, List<DifficultyZone>> oversizedZones = new HashMap<>();

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        ListTag list = new ListTag();
        for (DifficultyZone zone : zones) list.add(zone.toNbt());
        tag.put("Zones", list);
        return tag;
    }

    public static DifficultyZoneSavedData load(CompoundTag nbt, HolderLookup.Provider registries) {
        DifficultyZoneSavedData state = new DifficultyZoneSavedData();
        ListTag list = nbt.getList("Zones", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) state.zones.add(DifficultyZone.fromNbt(list.getCompound(i)));
        state.rebuildIndex();
        return state;
    }

    public List<DifficultyZone> getZones() { return zones; }

    public void addZone(DifficultyZone zone) {
        zones.add(zone);
        rebuildIndex();
        setDirty();
    }

    public void removeZone(UUID id) {
        boolean removed = zones.removeIf(zone -> zone.getId().equals(id));
        if (removed) {
            rebuildIndex();
            setDirty();
        }
    }

    public DifficultyZone findZone(String dimensionKey, double x, double y, double z) {
        List<DifficultyZone> bucket = chunkIndex.get(ChunkPos.asLong(Mth.floor(x) >> 4, Mth.floor(z) >> 4));
        if (bucket != null) for (DifficultyZone zone : bucket) if (zone.contains(dimensionKey, x, y, z)) return zone;
        List<DifficultyZone> oversized = oversizedZones.get(dimensionKey);
        if (oversized != null) for (DifficultyZone zone : oversized) if (zone.contains(dimensionKey, x, y, z)) return zone;
        return null;
    }

    private void rebuildIndex() {
        chunkIndex.clear();
        oversizedZones.clear();
        for (DifficultyZone zone : zones) indexZone(zone);
    }

    private void indexZone(DifficultyZone zone) {
        int minChunkX;
        int minChunkZ;
        int maxChunkX;
        int maxChunkZ;

        if (zone.getShape() == DifficultyZone.Shape.BOX) {
            minChunkX = zone.getBoxMin().getX() >> 4;
            minChunkZ = zone.getBoxMin().getZ() >> 4;
            maxChunkX = (zone.getBoxMax().getX() + 1) >> 4;
            maxChunkZ = (zone.getBoxMax().getZ() + 1) >> 4;
        } else {
            int chunkRadius = Mth.ceil(zone.getRadius() / 16.0);
            int centerChunkX = zone.getCenter().getX() >> 4;
            int centerChunkZ = zone.getCenter().getZ() >> 4;
            minChunkX = centerChunkX - chunkRadius;
            minChunkZ = centerChunkZ - chunkRadius;
            maxChunkX = centerChunkX + chunkRadius;
            maxChunkZ = centerChunkZ + chunkRadius;
        }

        long coverage = (long) (maxChunkX - minChunkX + 1) * (maxChunkZ - minChunkZ + 1);
        if (coverage > MAX_INDEXED_CHUNKS) {
            oversizedZones.computeIfAbsent(zone.getDimension(), key -> new ArrayList<>()).add(zone);
            return;
        }

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++)
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++)
                chunkIndex.computeIfAbsent(ChunkPos.asLong(chunkX, chunkZ), key -> new ArrayList<>()).add(zone);
    }

    public static DifficultyZoneSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(DifficultyZoneSavedData::new, DifficultyZoneSavedData::load),
                DATA_NAME);
    }
}