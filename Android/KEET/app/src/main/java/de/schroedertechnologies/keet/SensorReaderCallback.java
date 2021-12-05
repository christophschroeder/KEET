package de.schroedertechnologies.keet;

public interface SensorReaderCallback {
    void OnDeviceConnected();
    void OnDeviceDisconnected();
    void OnDeviceDataReceived(int sensVal, int avgSensVal, int bits);
}
