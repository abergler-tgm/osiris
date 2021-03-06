package at.pria.osiris.linker.implementation.hedgehog.communication;

import Util.Serializer;
import at.pria.osiris.linker.communication.messageProcessors.MessageProcessor;
import org.andrix.deployment.Program;
import org.andrix.listeners.ExecutionListener;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author Ari Ayvazyan
 * @version 03.Dec.14
 */
public class HedgehogDataListener implements ExecutionListener {
    private MessageProcessor messageProcessorDistributor;

    public HedgehogDataListener(MessageProcessor messageProcessor) {
        this.messageProcessorDistributor = messageProcessor;
    }

    @Override
    public void executionStarted(Program program, int i) {

    }

    @Override
    public void executionStopped(Program program, int i) {

    }

    @Override
    public void executionDone(Program program, int i, int i1) {

    }

    @Override
    public void executionOutput(Program program, int i, String s) {

    }

    /*
     * Is received when AXCP.EXECUTION_DATA_ACTION is called.
     */
    @Override
    public void executionDataReceived(Program program, int i, byte[] bytes) {
        System.out.println("received some data!");
        try {
            Logger.getGlobal().info("deserialized message");
            Object receivedMessage = Serializer.deserialize(bytes);
            this.messageProcessorDistributor.processMessage(receivedMessage);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void executionBreaked(Program program, int i, int i1, String[] strings) {

    }
}
