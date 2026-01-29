package dev.amymialee.nearbyvoicechat.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.plugins.PluginManager;
import de.maxhenkel.voicechat.voice.common.*;
import de.maxhenkel.voicechat.voice.server.*;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(Server.class)
public abstract class ServerMixin {
    @Shadow public abstract double getBroadcastRange(float minRange);

    @Inject(method = "processGroupPacket", at = @At(value = "HEAD"))
    private void nearby$init(
            PlayerState senderState, ServerPlayerEntity sender, @NonNull MicPacket packet, CallbackInfo ci,
            @Share("distance") LocalFloatRef distance, @Share("nearby") LocalRef<Collection<ServerPlayerEntity>> nearby) {
        if (packet.isWhispering()) {
            distance.set(Voicechat.SERVER_CONFIG.whisperDistance.get().floatValue());
        } else {
            distance.set(Utils.getDefaultDistanceServer());
        }
        distance.set(PluginManager.instance().getDistance(sender, senderState, packet, distance.get()) - 2);
        nearby.set(ServerWorldUtils.getPlayersInRange(sender.getEntityWorld(), sender.getEntityPos(), this.getBroadcastRange(distance.get()), (p) -> !p.getUuid().equals(sender.getUuid())));
    }

    @WrapOperation(method = "processGroupPacket", at = @At(value = "INVOKE", target = "Lde/maxhenkel/voicechat/voice/server/Server;sendSoundPacket(Lnet/minecraft/server/network/ServerPlayerEntity;Lde/maxhenkel/voicechat/voice/common/PlayerState;Lnet/minecraft/server/network/ServerPlayerEntity;Lde/maxhenkel/voicechat/voice/common/PlayerState;Lde/maxhenkel/voicechat/voice/server/ClientConnection;Lde/maxhenkel/voicechat/voice/common/SoundPacket;Ljava/lang/String;)V"))
    private void nearby$pivot(
            Server instance, @NonNull ServerPlayerEntity sender, @NonNull PlayerState senderState,
            ServerPlayerEntity receiver, PlayerState receiverState, ClientConnection connection,
            SoundPacket<?> soundPacket, String source, Operation<Void> original,
            PlayerState senderState2, ServerPlayerEntity sender2, @NonNull MicPacket packet,
            @Share("distance") LocalFloatRef distance, @Share("nearby") LocalRef<Collection<ServerPlayerEntity>> nearby) {
        if (!sender.isSpectator() && nearby.get().contains(receiver)) {
            soundPacket = new PlayerSoundPacket(sender.getUuid(), sender.getUuid(), packet.getData(), packet.getSequenceNumber(), packet.isWhispering(), distance.get(), null);
            source = "proximity";
        }
        original.call(instance, sender, senderState, receiver, receiverState, connection, soundPacket, source);
    }
}