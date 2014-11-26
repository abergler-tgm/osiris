package at.pria.osiris.osiris.network;

import android.app.Activity;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import api.Axis;
import api.Robotarm;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.channels.NotYetConnectedException;

import javax.net.SocketFactory;

/**
 * An object that allows remote-controlling a roboterarm
 *
 * @author Adrian Bergler
 * @version 2014-10-17
 */
public class RemoteRobotarm extends Thread implements Robotarm {

    public static final int MAX_POWER = 100;
    private static RemoteRobotarm INSTANCE;
    private final String linkip = "192.168.43.241";        //IP of the JVM-Link-Controller
    private final int linkport = 8889;                    //Port of the Server-program running on the Controller
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    private RemoteRobotarm() throws IOException {
        //Strict mode ... dirty dirty

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        this.start();

    }

    public static RemoteRobotarm getInstance() throws IOException {
        if (INSTANCE == null) {
            INSTANCE = new RemoteRobotarm();
        }
        return INSTANCE;
    }

    @Override
    public void turnAxis(Axis axis, int power) {
        sendMessage("turnaxis/" + axis.ordinal() + "/" + power);
    }

    @Override
    public void turnAxis(Axis axis, int power, long timemillis) {
        sendMessage("turnaxis/" + axis.ordinal() + "/" + power + "/" + timemillis);

    }

    @Override
    public void stopAxis(Axis axis) {
        sendMessage("stopaxis/" + axis.ordinal());
    }

    @Override
    public boolean moveTo(double x, double y, double z) {
        sendMessage("moveto/" + x + "/" + y + "/" + z);
        return true;
    }

    @Override
    public void stopAll() {
        sendMessage("stopall");
    }

    /**
     * Close sockets, kill watchdogs
     */
    @Override
    public void close() {
        sendMessage("close");
    }

    @Override
    public void test() {
        sendMessage("test");
    }

    public void exit() {
        sendMessage("exit");
    }

    private void sendMessage(String message) {
        try {
            //Log.d("osiris", "Sending message to Socket Server: " + message);

            if(oos!=null) {
                oos.writeObject(message);
                //Log.d("osiris", "Message sent");
            } else {
                //Log.d("Osiris-Verbose", "Not connected");
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        Log.e("MyOsiris", "In run RemoteRobotarm");
        try {
            socket = SocketFactory.getDefault().createSocket(this.linkip, this.linkport);
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            if(socket==null || !socket.isConnected()) {
                //TODO make a toast, Ari
            }
        }
    }

    public ObjectInputStream getOis() {
        return ois;
    }
}
