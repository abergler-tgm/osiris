package at.pria.osiris.linker.communication.connectors.hedgehog;

import at.pria.osiris.linker.communication.connectors.MessageSender;
import at.pria.osiris.linker.communication.messageProcessors.MessageProcessorDistributor;
import oldLinker.control.DataListener;
import org.andrix.AXCP;
import org.andrix.listeners.ExecutionListener;
import org.andrix.low.*;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author Ari Ayvazyan
 * @version 03.Dec.14
 */
public class HedgehogMessageSender implements MessageSender {
    /**
     * Sends data via the Hedgehog AXCP Controller
     */
    public static void sendData(byte[] data) throws NotConnectedException, RequestTimeoutException {
        AXCP.command(AXCP.EXECUTION_DATA_ACTION, "", 0, data);
    }

    /**
     * Sends data via the Hedgehog AXCP Controller
     */
    public static void sendData(Serializable data) throws NotConnectedException, RequestTimeoutException, IOException {
        sendData((Util.Serializer.serialize(data)));
    }

    /**
     * Sends data via the Hedgehog AXCP Controller
     */
    @Override
    public void sendMessage(Serializable message) {
        try {
            sendData(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets up the communication
     */
    @Override
    public void setupCommunication() {
        AXCPServer.communicationInterface = new HedgehogSerialPortCommunicationInterface(); // The Serial Port Communication Interface for the Pi
        AXCPAccessor.getInstance().connectController(new HardwareController(null,HardwareController.TYPE_V3,"hedgehog-osiris")); // Initialise the AXCPAccessor

        MessageProcessorDistributor mp = new MessageProcessorDistributor();
        //TODO This should not be new MessageProcessr...
        ExecutionListener._l_exec.add(new HedgehogDataListener(new MessageProcessorDistributor()));
    }
}