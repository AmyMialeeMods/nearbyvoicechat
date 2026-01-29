package dev.amymialee.nearbyvoicechat;

import de.maxhenkel.voicechat.api.VoicechatPlugin;

public class NearbyVoiceChat implements VoicechatPlugin {
    public static final String MOD_ID = "nearbyvoicechat";

    @Override
    public String getPluginId() {
        return MOD_ID;
    }
}