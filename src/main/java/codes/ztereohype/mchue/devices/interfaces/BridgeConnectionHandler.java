package codes.ztereohype.mchue.devices.interfaces;

public interface BridgeConnectionHandler {
    void success(String username);
    void pressButton(int timeLeft);
    void timeUp();
    void failure(String errorMessage);
}
