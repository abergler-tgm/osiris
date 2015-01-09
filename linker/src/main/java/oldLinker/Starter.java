package oldLinker;

import oldLinker.control.DataListener;
import oldLinker.control.MessageProcessor.MessageProcessorDistributor;
import oldLinker.control.SensorMessenger;
import oldLinker.model.RobotarmImpl;
import org.andrix.listeners.ExecutionListener;
import org.andrix.low.AXCPAccessor;
import org.andrix.low.AXCPServer;
import org.andrix.low.HardwareController;

/**
 * Starts the link-controller program
 *
 * @author Adrian Bergler
 * @version 0.2
 */
public class Starter {
    public static void main(String[] args) {

        AXCPServer.communicationInterface = new SerialPortCommunicationInterface(); // The Serial Port Communication Interface for the Pi
        AXCPAccessor.getInstance().connectController(new HardwareController(null,HardwareController.TYPE_V3,"hedgehog-osiris")); // Initialise the AXCPAccessor

        Thread thread;
        RobotarmImpl robotarm = new RobotarmImpl();

        MessageProcessorDistributor mp = new MessageProcessorDistributor(robotarm);
        boolean running = true;

        // sends sensordata in ~1s intervall
        SensorMessenger st = new SensorMessenger(robotarm);
        thread = new Thread(st);
        thread.start();

        //A listener to receive Data from the Controller
        ExecutionListener._l_exec.add(new DataListener(mp));
    }
}