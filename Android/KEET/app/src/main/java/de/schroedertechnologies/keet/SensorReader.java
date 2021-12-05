package de.schroedertechnologies.keet;


import android.content.Context;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class SensorReader {

    private SensorReaderCallback m_sensorReaderCallback = null;
    private SensorReaderThread m_sensorReaderThread = null;
    private boolean m_isConnected;
    Context m_context;

    public SensorReader(SensorReaderCallback sensorReaderCallback, Context context) {
        this.m_sensorReaderCallback = sensorReaderCallback;
        this.m_isConnected = false;
        this.m_context = context;
    }

    public void disconnect() {
        // stop thread
        if(this.m_isConnected) {
            this.m_sensorReaderThread.cancel();
            this.m_sensorReaderCallback.OnDeviceDisconnected();
        }
    }

    public void connect() {
        if(!this.m_isConnected) {
            tryConnect();
        }
    }

    private void tryConnect() {

        // ToDo: Look for USB device, try to connect and start thread

        // Find all available drivers from attached devices.
        UsbManager manager = (UsbManager) m_context.getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            return;
        }

        // Open a connection to the first available driver.
        UsbSerialDriver driver = availableDrivers.get(0);
        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
        if (connection == null) {
            // add UsbManager.requestPermission(driver.getDevice(), ..) handling here
            return;
        }

        UsbSerialPort port = driver.getPorts().get(0); // Most devices have just one port (port 0)
        try {
            port.open(connection);
            port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            this.m_isConnected = true;
        } catch (IOException e) {
            e.printStackTrace();
        }




        this.m_sensorReaderThread = new SensorReaderThread();

        this.m_sensorReaderThread.start();
        this.m_sensorReaderCallback.OnDeviceConnected();
        this.m_isConnected = true;
    }

    class SensorReaderThread extends Thread {

        private volatile boolean m_cancel;
        private boolean m_isRunning;
        private boolean m_simulateData = true;
        Random m_random = new Random();

        public SensorReaderThread() {
            this.m_cancel = false;
            this.m_isRunning = false;
        }

        public void cancel() {
            this.m_cancel = true;
        }

        public void run() {

            this.m_isRunning = true;

            int val = 10000;

            while(!m_cancel) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(m_simulateData) {
                    int add = 25 - m_random.nextInt(50);

                    val += add;
                    if(val < 0)
                        val = 0;
                    if(val > Math.pow(2, 16))
                        val = (int)Math.pow(2, 16);
                }

                SensorReader.this.m_sensorReaderCallback.OnDeviceDataReceived(val, 0, 16);
            }

            this.m_isRunning = false;
        }

    }

}
