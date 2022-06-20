package codes.ztereohype.mchue.devices.interfaces;

import org.jetbrains.annotations.Nullable;

public record BridgeConnectionUpdate(BridgeResponse response, @Nullable Integer timeLeft, @Nullable String errorMessage) { }
