package at.pria.osiris.osiris.network;

import android.util.Log;
import api.Robotarm;
import at.pria.osiris.osiris.sensors.SensorRefresher;

/**
 * Created by Samuel on 29.10.2014.
 */
public class DirtyClientMessageProcessor {

    private Robotarm robotarm;
    private SensorRefresher sensorRefresher;


    public DirtyClientMessageProcessor(Robotarm robotarm, SensorRefresher sensorRefresher) {
        this.robotarm = robotarm;
        this.sensorRefresher = sensorRefresher;
    }

    /**
     * Changes Sensor Values in the GUI
     *
     * @param message the message
     */
    public void callMethod(String message) {
        Log.d("OSIRIS_DEBUG_MESSAGES", "Reached callMethod " + message);
        String[] splitted = message.split("/");
        for (int i = 0; i < splitted.length; i++) {
            Log.d("OSIRIS_DEBUG_NETWORK", "Splitted" + i + ": " + splitted[i]);
        }

        /*
         * Info for the sensors
         *
         * sensor0 == sensor on the port0 and so on
         *
         */

        //Sensor 1
        if (splitted[0].equals("sensor1") && splitted.length == 2) {
            try {
                sensorRefresher.refresh(splitted[1], "sensor1");
            } catch (NumberFormatException nfe) {
                // TODO Auto-generated catch block
                nfe.printStackTrace();
            }
        }

        //Sensor 2
        if (splitted[0].equals("sensor2") && splitted.length == 2) {
            try {
                sensorRefresher.refresh(splitted[1], "sensor2");
            } catch (NumberFormatException nfe) {
                // TODO Auto-generated catch block
                nfe.printStackTrace();
            }
        }

    }
}

