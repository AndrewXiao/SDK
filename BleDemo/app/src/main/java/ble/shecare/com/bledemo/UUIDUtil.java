package ble.shecare.com.bledemo;

public class UUIDUtil
{
  private static final String base_uuid_regex = "0000([0-9a-f][0-9a-f][0-9a-f][0-9a-f])-0000-1000-8000-00805f9b34fb";

  public static boolean isBaseUUID(String uuid)
  {
    return uuid.toLowerCase().matches("0000([0-9a-f][0-9a-f][0-9a-f][0-9a-f])-0000-1000-8000-00805f9b34fb");
  }

  public static String UUID_128_to_16bit(String uuid, boolean lower_case)
  {
    if (uuid.length() == 36) {
      if (lower_case) {
        return uuid.substring(4, 8).toLowerCase();
      }
      return uuid.substring(4, 8).toUpperCase();
    }

    return null;
  }

  public static String UUID_16bit_128bit(String uuid, boolean lower_case)
  {
    if (lower_case) {
      return ("0000([0-9a-f][0-9a-f][0-9a-f][0-9a-f])-0000-1000-8000-00805f9b34fb".substring(0, 4) + uuid + "0000([0-9a-f][0-9a-f][0-9a-f][0-9a-f])-0000-1000-8000-00805f9b34fb".substring(38)).toLowerCase();
    }
    return ("0000([0-9a-f][0-9a-f][0-9a-f][0-9a-f])-0000-1000-8000-00805f9b34fb".substring(0, 4) + uuid + "0000([0-9a-f][0-9a-f][0-9a-f][0-9a-f])-0000-1000-8000-00805f9b34fb".substring(38)).toUpperCase();
  }
}