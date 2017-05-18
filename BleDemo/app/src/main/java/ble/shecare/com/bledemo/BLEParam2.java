package ble.shecare.com.bledemo;

import java.util.UUID;

public class BLEParam2
{
  public static final String BLE_THERMOMETER_SERVICE_UUID = UUIDUtil.UUID_16bit_128bit("1809", true);
  public static final String FirmwareVersion_Char_UUID = UUIDUtil.UUID_16bit_128bit("2A26", true);
  public static final String Temp_Char_UUID = UUIDUtil.UUID_16bit_128bit("2A1C", true); // 温度 Characteristic UUID
  public static final String Ack_Char_UUID = UUIDUtil.UUID_16bit_128bit("3033", true); // 发送温度指令 Characteristic UUID，同时也是电量 Characteristic UUID
  public static final String TIME_SYNC_UUID = UUIDUtil.UUID_16bit_128bit("3031", true);

  public static final UUID[] TemperServiceUuids = {UUID.fromString(BLE_THERMOMETER_SERVICE_UUID)};
  public static final UUID DESC_CCC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
}