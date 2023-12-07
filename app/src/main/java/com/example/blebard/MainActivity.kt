package com.example.blebard

package com.example.bleclient

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var device: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Skontroluj, či je povolený Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Log.e("MainActivity", "Bluetooth nie je podporovaný")
            finish()
        } else if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        // Spusti službu BLE na pozadí
        startService(Intent(this, BLEService::class.java))

        // Spusti skenovanie BLE zariadení
        scanForDevices()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            // Bluetooth bol povolený
            scanForDevices()
        } else {
            // Bluetooth nebol povolený
            Log.e("MainActivity", "Bluetooth nebol povolený")
            finish()
        }
    }

    private fun scanForDevices() {
        // Spusti skenovanie BLE zariadení
        bluetoothAdapter.startDiscovery()

        // Zobraz zoznam nájdených zariadení
        val deviceListFragment = DeviceListFragment()
        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction()
            .replace(R.id.fragment_container, deviceListFragment)
            .commit()
    }

    override fun onStart() {
        super.onStart()

        // Skontroluj, či už je vybrané zariadenie
        if (device.isNotEmpty()) {
            // Pripoj sa k zariadeniu
            connectToDevice(device)
        }
    }

    private fun connectToDevice(device: String) {
        // Získaj objekt BluetoothDevice pre dané zariadenie
        val bluetoothDevice = bluetoothAdapter.getRemoteDevice(device)

        // Vytvor objekt BluetoothGatt pre dané zariadenie
        val bluetoothGatt = bluetoothDevice.connectGatt(this, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    // Pripojenie bolo úspešné
                    Log.d("MainActivity", "Pripojenie k zariadeniu bolo úspešné")
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    // Pripojenie bolo prerušené
                    Log.d("MainActivity", "Pripojenie k zariadeniu bolo prerušené")
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    // Ziskaj všetky charakteristiky zariadenia
                    val characteristics = gatt.services[0].characteristics

                    // Pre každú charakteristiku zariadenia
                    for (characteristic in characteristics) {
// Skontroluj, či sa charakteristika volá "value"
