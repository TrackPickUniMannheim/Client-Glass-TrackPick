package de.unima.ar.collector.shared;

public class Settings
{
    // This file is modified on startup by Android! Check xml.preferences.xml!

    public static double  SENSOR_DEFAULT_FREQUENCY = 50.0d;    // Hz
    public static long    GPS_DEFAULT_FREQUENCY    = 10000l; // Milliseconds 600000l = 10min
    public static long    MICRO_DEFAULT_FREQUENCY  = 20l; // Milliseconds 20l = 50hertz
    public static boolean ACCLOWPASS               = false;

    public static boolean DATABASE_DIRECT_INSERT = false;
    public static int     DATABASE_CACHE_SIZE    = 1000;   // number of entries
    public static String  DATABASE_DELIMITER     = "-#~o~#-";

    public static boolean STREAMING              = true;
    public static String  SERVER_IP              = "10.0.2.2";
    public static Integer SERVER_PORT            = 9999;

    public static String ACTIVITY_DELIMITER = " - ";

    public static int EXPORT_ATONCE = 10000;

    public static boolean LIVE_PLOTTER_ENABLED = true;

    public static boolean WEARSENSOR           = true;
    public static boolean WEARTRANSFERDIRECT   = false;
    public static int     WEARTRANSFERIDLETIME = 120000;   // milliseconds
    public static int     WEARTRANSFERTIMEOUT  = 5000;    // milliseconds
    public static int     WEARTRANSFERSIZE     = 5000;  // 1000
}