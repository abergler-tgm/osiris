package at.pria.osiris.linker.implementation.hedgehog.axes;

import at.pria.osiris.linker.controllers.components.Axis;
import at.pria.osiris.linker.controllers.components.systemDependent.Motor;
import at.pria.osiris.linker.implementation.hedgehog.components.HedgehogMotor;
import org.andrix.low.NotConnectedException;

/**
 * @author Ari Michael Ayvazyan
 * @version 16.02.2015
 */
public class HorizontalAxis extends Axis{
    private Motor motor;
    private int position =-1; //-1 is a undefined state
    public HorizontalAxis() throws NotConnectedException {
        super("HorizontalAxis");
        this.motor=new HedgehogMotor(2);
    }

    /**
     *
     * @see at.pria.osiris.linker.controllers.components.Axis
     */
    public void moveToPosition(int position){
        //TODO work with sensor values
    }

    /**
     * @see at.pria.osiris.linker.controllers.components.Axis
     */
    public void moveAtPower(int power){
        motor.moveAtPower(power);
    }

    /**
     * @see at.pria.osiris.linker.controllers.components.Axis
     */
    @Override
    public int getSensorValue() {
        return position;
    }
}
