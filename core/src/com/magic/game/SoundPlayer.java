package com.magic.game;

import com.badlogic.gdx.math.Vector2;

class SoundPlayer {

    private static Entity player;
    private static float GLOBAL_SFX_LEVEL = 0.5f;
    private static float GLOBAL_MUSIC_LEVEL = 0.5f;
    private static float HEARING_RANGE = 100.0f;

    static void setPlayer(Entity entity) {
        player = entity;
    }

    static float getSfxVolume(Vector2 pos) {
        if (player != null) {
            float distance = pos.dst(player.getPos());
            if (distance > HEARING_RANGE) {
                return 0.05f;
            }
            float volume = (1.0f - distance / (HEARING_RANGE )) * GLOBAL_SFX_LEVEL;
            return volume;
        }
        return 0;
    }

    static float getMusicVolume() {
        return GLOBAL_MUSIC_LEVEL;
    }
}
