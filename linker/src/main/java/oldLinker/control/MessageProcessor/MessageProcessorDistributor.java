package oldLinker.control.MessageProcessor;

import Util.Serializer;
import api.Robotarm;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Sends a message via socket
 *
 * @author Adrian Bergler
 * @version 2014-10-17
 */
public class MessageProcessorDistributor {

    private Robotarm robotarm;
    private List<MessageProcessor> messageProcessors;

    public MessageProcessorDistributor(Robotarm robotarm) {
        this.robotarm = robotarm;
        this.messageProcessors = new LinkedList<MessageProcessor>();
        initProcessors();
    }

    private void initProcessors() {
        this.addMessageProcessor(new StringProcessor(robotarm));
    }

    /**
     * Combined with a message-"receiver" this calls a method of the robotarm
     *
     * @param message the message
     */

    public void processMessage(byte[] message) {
        try {
            Object receivedMessage = Serializer.deserialize(message);
            for (MessageProcessor messageProcessor : messageProcessors) {
                messageProcessor.processMessage(receivedMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void addMessageProcessor(MessageProcessor messageProcessor) {
        this.messageProcessors.add(messageProcessor);
    }
}
