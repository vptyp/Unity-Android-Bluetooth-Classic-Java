package com.vptyp.unitybluetooth.ubclassic;

import me.aflak.bluetooth.interfaces.BluetoothCallback;
import me.aflak.bluetooth.interfaces.DeviceCallback;
import me.aflak.bluetooth.interfaces.DiscoveryCallback;

import com.vptyp.unitybluetooth.ubclassic.DataBridge;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;

import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;

import com.unity3d.player.UnityPlayer;

import me.aflak.bluetooth.Bluetooth;

import android.content.Context;
import android.app.Activity;
import android.os.Bundle;
import android.Manifest;
import android.os.Build;
import android.provider.ContactsContract;
import java.util.HashMap;
public class BTcb implements BluetoothCallback, DiscoveryCallback, DeviceCallback {

    private static BTcb mInstance = null;
    private Bluetooth bluetooth;
    private BluetoothDevice bluetoothDevice;

    private HashMap<String, BluetoothDevice> foundedDevices;
    private HashMap<String, String> NameToAddress;
    private String deviceAddress = null;
    private String deviceName = null;

    //MonoBehaviour GameObject to catch the Bluetooth Messages
    private static final String mUnityBlueReceiver = "BTAdapter";

    //Command to send Bluetooth data
    private static final String mUnityBlueCommand = "OnBTMessage";
    //Command to send Unity Debug Logs
    private static final String mUnityLogCommand = "LogMessage";

    /**
     * Singleton instance of UnityBluetooth called by Unity
     */
    public static BTcb getInstance() {
        if (mInstance == null) {
            mInstance = new BTcb();
            mInstance.sendToUnity(new DataBridge("OnCreate"));
        }

        return mInstance;
    }

    /**
     * Connects to the device by name
     * @param name the name of the device
     */
    @SuppressLint("MissingPermission")
    public void connect(String name) {
        checkPermissions(UnityPlayer.currentActivity.getApplicationContext(),
                UnityPlayer.currentActivity);
        bluetooth.connectToName(name);
        sendToUnity(new DataBridge("StartingConnectionByName"){
            {
                this.name = name;
            }
        });
    }

    /**
     * Connects to the device by address
     * @param address address in format XX:XX:XX:XX:XX:XX
     */
    @SuppressLint("MissingPermission")
    public void connectAddress(String address) {
        checkPermissions(UnityPlayer.currentActivity.getApplicationContext(),
                UnityPlayer.currentActivity);
        bluetooth.connectToAddress(address);
        sendToUnity(new DataBridge("StartingConnectionByAddress"){
            {
                this.device = address;
            }
        });
    }

    /**
     * StartScanning for devices
     */
    @SuppressLint("MissingPermission")
    public void startScanning() {
        checkPermissions(UnityPlayer.currentActivity.getApplicationContext(),
                UnityPlayer.currentActivity);
        bluetooth.startScanning();
        sendToUnity(new DataBridge("StartingScanning"));
    }

    /**
     * Pair with the device by address
     *      @param address address in format XX:XX:XX:XX:XX:XX.
     *     Linking to the BluetoothDevice from HashMap of Founded Devices.
     *                So needed to use startScanning() before.
     */
    @SuppressLint("MissingPermission")
    public void pair(String address) {
        checkPermissions(UnityPlayer.currentActivity.getApplicationContext(),
                UnityPlayer.currentActivity);

        bluetooth.pair(foundedDevices.get(address));
        sendToUnity(new DataBridge("PairingDevice"){
            {
                this.device = address;
            }
        });
    }

    /**
     * Unpair with the device by address
     * @param address address in format XX:XX:XX:XX:XX:XX.
     * Linking to the BluetoothDevice from HashMap of Founded Devices.
     *                So needed to use startScanning() before.
     *
     */
    @SuppressLint("MissingPermission")
    public void unpair(String address) {
        checkPermissions(UnityPlayer.currentActivity.getApplicationContext(),
                UnityPlayer.currentActivity);

        bluetooth.unpair(foundedDevices.get(address));
        sendToUnity(new DataBridge("UnpairingDevice"){
            {
                this.device = address;
                this.name = NameToAddress.get(address);
            }
        });
    }

    /**
     * Pair with the device by name
     * @param name name of the device.
     *             Linking to the BluetoothDevice from HashMap of NameToAddress and Founded Devices.
     *             So needed to use startScanning() before.
     */
    public void pairByName(String name) {
        checkPermissions(UnityPlayer.currentActivity.getApplicationContext(),
                UnityPlayer.currentActivity);

        bluetooth.pair(foundedDevices.get(NameToAddress.get(name)));
        sendToUnity(new DataBridge("PairingDeviceByName"){
            {
                this.name = name;
                this.device = NameToAddress.get(name);
            }
        });
    }

    /**
     * Stop scanning for devices
     */
    @SuppressLint("MissingPermission")
    public void stopScanning() {
        checkPermissions(UnityPlayer.currentActivity.getApplicationContext(),
                UnityPlayer.currentActivity);
        bluetooth.stopScanning();
        sendToUnity(new DataBridge("StoppingScanning"));
    }



    /**
     * Constructor for UnityBluetooth
     * Enables bluetooth and creates instances
     */
    BTcb() {
        checkPermissions(UnityPlayer.currentActivity.getApplicationContext(),
                UnityPlayer.currentActivity);
        bluetooth = new Bluetooth(UnityPlayer.currentActivity);
        bluetooth.setBluetoothCallback(this);
        bluetooth.setDiscoveryCallback(this);
        bluetooth.setDeviceCallback(this);
    }

    /**
     * Sends data to the unity receiver object
     *
     * @param dataBridge the data to be sent
     */
    public void sendToUnity(DataBridge dataBridge) {
        UnityPlayer.UnitySendMessage(mUnityBlueReceiver, mUnityBlueCommand, dataBridge.toJson());
    }

    /**
     * Sends the given message to Unity BLE Adapter to log inside the Unity stack trace
     *
     * @param message the message to be logged
     */
    public static void unityLog(String message) {
        UnityPlayer.UnitySendMessage(mUnityBlueReceiver, mUnityLogCommand, message);
    }

    public static Boolean checkPermissions(Context context, Activity activity) {
        if (context.checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                || context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || context.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                || context.checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                || context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(new String[]{Manifest.permission.BLUETOOTH,
                                                    Manifest.permission.BLUETOOTH_ADMIN,
                                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                                    Manifest.permission.BLUETOOTH_SCAN,
                                                    Manifest.permission.BLUETOOTH_CONNECT}, 1);
        }
        return Boolean.TRUE;
    }


    // region BluetoothCallback
    @Override
    public void onBluetoothTurningOn() {
        DataBridge output = new DataBridge("OnBluetoothTurningOn");
        UnityPlayer.UnitySendMessage(mUnityBlueReceiver, mUnityBlueCommand, output.toJson());
    }

    @Override
    public void onBluetoothOn() {
        DataBridge output = new DataBridge("OnBluetoothOn");
        UnityPlayer.UnitySendMessage(mUnityBlueReceiver, mUnityBlueCommand, output.toJson());
    }

    @Override
    public void onBluetoothTurningOff() {
        DataBridge output = new DataBridge("OnBluetoothTurningOff");
        UnityPlayer.UnitySendMessage(mUnityBlueReceiver, mUnityBlueCommand, output.toJson());
    }

    @Override
    public void onBluetoothOff() {
        DataBridge output = new DataBridge("OnBluetoothOff");
        UnityPlayer.UnitySendMessage(mUnityBlueReceiver, mUnityBlueCommand, output.toJson());
    }

    @Override
    public void onUserDeniedActivation() {
        DataBridge output = new DataBridge("OnUserDeniedActivation");
        UnityPlayer.UnitySendMessage(mUnityBlueReceiver, mUnityBlueCommand, output.toJson());
    }

    @Override
    @SuppressLint("MissingPermission")
    public void onDeviceConnected(BluetoothDevice device) {
        checkPermissions(UnityPlayer.currentActivity.getApplicationContext(),
                UnityPlayer.currentActivity);
        bluetoothDevice = device;
        DataBridge output = new DataBridge("OnDeviceConnected");

        output.name = bluetoothDevice.getName();
        output.device = bluetoothDevice.getAddress();

        deviceAddress = bluetoothDevice.getAddress();
        deviceName = bluetoothDevice.getName();

        UnityPlayer.UnitySendMessage(mUnityBlueReceiver, mUnityBlueCommand, output.toJson());
    }

    @Override
    @SuppressLint("MissingPermission")
    public void onDeviceDisconnected(BluetoothDevice device, String message) {
        checkPermissions(UnityPlayer.currentActivity.getApplicationContext(),
                UnityPlayer.currentActivity);

        DataBridge output = new DataBridge("OnDeviceDisconnected");
        output.name = device.getName();
        output.device = device.getAddress();
        output.data = message;

        bluetoothDevice = null;
        deviceAddress = null;
        deviceName = null;

        UnityPlayer.UnitySendMessage(mUnityBlueReceiver, mUnityBlueCommand, output.toJson());
    }

    @Override
    public void onMessage(byte[] message) {
        DataBridge output = new DataBridge("OnMessage");
        output.data = new String(message);
        output.name = deviceName;
        output.device = deviceAddress;

        UnityPlayer.UnitySendMessage(mUnityBlueReceiver, mUnityBlueCommand, output.toJson());
    }
    @Override
    @SuppressLint("MissingPermission")
    public void onConnectError(BluetoothDevice device, String message) {
        checkPermissions(UnityPlayer.currentActivity.getApplicationContext(),
                UnityPlayer.currentActivity);

        DataBridge output = new DataBridge("OnConnectError");
        output.name = device.getName();
        output.device = device.getAddress();
        output.setError(message);

        UnityPlayer.UnitySendMessage(mUnityBlueReceiver, mUnityBlueCommand, output.toJson());
    }

    @Override
    public void onDiscoveryStarted() {
        DataBridge output = new DataBridge("OnDiscoveryStarted");
        UnityPlayer.UnitySendMessage(mUnityBlueReceiver, mUnityBlueCommand, output.toJson());
    }

    @Override
    public void onDiscoveryFinished() {
        DataBridge output = new DataBridge("OnDiscoveryFinished");
        UnityPlayer.UnitySendMessage(mUnityBlueReceiver, mUnityBlueCommand, output.toJson());
    }

    @Override
    @SuppressLint("MissingPermission")
    public void onDeviceFound(BluetoothDevice device) {
        checkPermissions(UnityPlayer.currentActivity.getApplicationContext(),
                UnityPlayer.currentActivity);

        DataBridge output = new DataBridge("OnDeviceFound");
        output.name = device.getName();
        output.device = device.getAddress();

        foundedDevices.put(device.getAddress(), device);
        NameToAddress.put(device.getName(), device.getAddress());

        UnityPlayer.UnitySendMessage(mUnityBlueReceiver, mUnityBlueCommand, output.toJson());
    }

    @Override
    @SuppressLint("MissingPermission")
    public void onDevicePaired(BluetoothDevice device) {
        checkPermissions(UnityPlayer.currentActivity.getApplicationContext(),
                UnityPlayer.currentActivity);

        DataBridge output = new DataBridge("OnDevicePaired");
        output.name = device.getName();
        output.device = device.getAddress();

        UnityPlayer.UnitySendMessage(mUnityBlueReceiver, mUnityBlueCommand, output.toJson());
    }

    @Override
    @SuppressLint("MissingPermission")
    public void onDeviceUnpaired(BluetoothDevice device) {
        checkPermissions(UnityPlayer.currentActivity.getApplicationContext(),
                UnityPlayer.currentActivity);

        DataBridge output = new DataBridge("OnDeviceUnpaired");
        output.name = device.getName();
        output.device = device.getAddress();

        UnityPlayer.UnitySendMessage(mUnityBlueReceiver, mUnityBlueCommand, output.toJson());
    }

    @Override
    public void onError(int errorCode) {
        DataBridge output = new DataBridge("OnError");
        output.setError("Bluetooth callback error: " + String.valueOf(errorCode));
        UnityPlayer.UnitySendMessage(mUnityBlueReceiver, mUnityBlueCommand, output.toJson());
    }
    // endregion
}
