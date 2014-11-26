package linker.model;

import java.util.List;

import api.Axis;
import api.Robotarm;
import linker.model.kinematics.Kinematics;
import linker.model.kinematics.ThreeAxisKinematics2D;
import linkjvm.Botball;
import linkjvm.motors.Motor;
import linkjvm.sensors.analog.AnalogSensor;

/**
 * Implementation of a robotarm
 *
 * @author Adrian Bergler
 * @version 2014-11-20
 */
public class RobotarmImpl implements Robotarm {

    //Constants
    private final double basetoaxisone = 11;
    private final double axisonetoaxistwo = 18;	
	private double[][] padding = {{0.0, 0.0, 0.0},{0.0, 0.0, 0.0},{0.0, 0.0, 0.0}};
	private double[] fragmentlength = {basetoaxisone, axisonetoaxistwo};
    
	//Joints
    private Joint[] joints;
    
    //KinematicStategies
    private Kinematics kinematics;

    public RobotarmImpl() {

    	joints = new Joint[3];
    	
        joints[0] = new Joint(new Motor(0), new AnalogSensor(0), Axis.BASE.getMinimumAngle(), Axis.BASE.getMaximumAngle());
        joints[1] = new Joint(new Motor(1), new AnalogSensor(1), Axis.AXISONE.getMinimumAngle(), Axis.AXISONE.getMaximumAngle());
        joints[2] = new Joint(new Motor(2), new AnalogSensor(2), Axis.AXISTWO.getMinimumAngle(), Axis.AXISTWO.getMaximumAngle());

        kinematics = new ThreeAxisKinematics2D();
    }

    public Joint getAxis(Axis axis) {
        return joints[axis.ordinal()];
    }

    public void setAxis(Axis axis, Joint joint) {
        this.joints[axis.ordinal()] = joint;
    }

    public Joint[] getJoints(){
    	return joints;
    }
    
    public void setJoints(Joint[] joints){
    	this.joints = joints;
    }
    
    @Override
    public void turnAxis(Axis axis, int power) {
        if (power >= -100 && power <= 100) {
            joints[axis.ordinal()].run(power);
        }
    }

    @Override
    public void turnAxis(Axis axis, int power, long timemillis) {
        turnAxis(axis, power);
        Botball.msleep(timemillis);
        stopAxis(axis);
    }

    @Override
    public void stopAxis(Axis axis) {
        System.out.println("Stopping " + axis.name());
        joints[axis.ordinal()].off();
    }

    @Override
    public boolean moveTo(double x, double y, double z) {
    	List<Double> solution = kinematics.moveTo(x, y, z, joints, fragmentlength, padding);
    	
    	if(solution == null) return false;
    	
    	joints[1].moveToAngle((470/4.8) - solution.get(0), 100);
		joints[2].moveToAngle((478/4.8) + (180 - solution.get(1)), 65);
		
    	return true;
    }

    @Override
    public void close() {
    	//Empty for now
    }

    @Override
    public void test() {
        System.out.println("Test successfull");
    }

    @Override
    public void stopAll() {
        for(int i = 0; i < joints.length; i++){
        	joints[i].off();
        }
    }

    @Override
    public void exit() {
        stopAll();
        close();
    }

}
