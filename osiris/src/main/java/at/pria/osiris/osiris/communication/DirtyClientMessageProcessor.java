package at.pria.osiris.osiris.communication;

import android.util.Log;
import at.pria.osiris.osiris.controllers.RobotArm;
import at.pria.osiris.osiris.sensors.SensorRefreshable;

/**
 * Created by Samuel on 29.10.2014.
 */
public class DirtyClientMessageProcessor {

    private RobotArm robotArm;
    private SensorRefreshable sensorRefresher;


    public DirtyClientMessageProcessor(RobotArm robotArm, SensorRefreshable sensorRefresher) {
        this.robotArm = robotArm;
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
            //Log.d("OSIRIS_DEBUG_NETWORK", "Splitted" + i + ": " + splitted[i]);
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
                sensorRefresher.refresh(Double.parseDouble(splitted[1]), "sensor1");
            } catch (NumberFormatException nfe) {
                // TODO Auto-generated catch block
                nfe.printStackTrace();
            }
        }

        //Sensor 2
        if (splitted[0].equals("sensor2") && splitted.length == 2) {
            try {
                sensorRefresher.refresh(Double.parseDouble(splitted[1]), "sensor2");
            } catch (NumberFormatException nfe) {
                // TODO Auto-generated catch block
                nfe.printStackTrace();
            }
        }

    }
}

