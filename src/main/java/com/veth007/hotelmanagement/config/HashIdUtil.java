package com.veth007.hotelmanagement.config;

import org.hashids.Hashids;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class HashIdUtil {
    private final Hashids hashids;

    // Static bridge so static mappers (e.g. FloorResponse.mapToResponse) can use it.
    private static HashIdUtil instance;

    public HashIdUtil(@Value("${app.hashids.salt}") String salt){
        this.hashids = new Hashids(salt, 8); // min length 8 so short ids still look random
    }

    @PostConstruct
    void init() {
        instance = this;
    }

    // Deterministic: the same id must produce the same hash on every
    // response, otherwise clients can't match ids across endpoints
    // (e.g. room_id from /api/favorites/me vs id from /api/room/all).
    public String encode(Long id){
        return hashids.encode(id);
    }

    public long decode(String hash){
        long[] decoded = hashids.decode(hash);
        if(decoded.length == 0){
            throw new IllegalArgumentException("Invalid hash: " + hash);
        }

        // decoded[0] = the real id; decoded[1] (if present) = the random
        // nonce added by encode(). Old single-number hashes still decode.
        return decoded[0];
    }

    // ─── static helpers for use inside static mapper methods ───
    public static String encodeId(Long id){
        return id != null ? instance.encode(id) : null;
    }

    public static long decodeId(String hash){
        return instance.decode(hash);
    }

    // for @PathVariable ids: responses expose hashids, but plain numeric
    // ids are still accepted for backward compatibility
    public static long resolveId(String idOrHash){
        try {
            return Long.parseLong(idOrHash);
        } catch (NumberFormatException e) {
            return instance.decode(idOrHash);
        }
    }
}
