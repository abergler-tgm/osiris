package at.pria.osiris.linker.controllers.components.Axes;


import at.pria.osiris.linker.controllers.components.systemDependent.Servo;

/**
 * A class which provides methods that changes the behavior of
 * the Servo.
 *
 * @author Wolfgang Mair
 * @version 11.03.2015
 */
public class ServoHelper {

    public ServoHelper() {
    }

    /**
     * A method which changes the speed of the specified Servo
     * It uses the time it takes the servo to move 1 degree and splits
     * this time into 100 steps.
     * The more power it gets the more steps it actually moves.
     *
     * @param s     The Servo which should move in the end
     * @param power The power it should use
     * @param steps The amount of steps
     */
    public static void pwm(Servo s, int power, int steps) {

        //Defining important Variables
        int maxPower = 100;
        int count = 1;
        boolean pos = true;
        boolean interrupt = false;
        boolean moving = false;
        int startPosition = s.getPositionInDegrees();

        //Checking if the power is negativ of positiv
        if (power < 0) {
            power = power * -1;
            pos = false;
        }

        //Calculating the distance between each stop and go
        double divider = (maxPower - power);
        double mod = maxPower / divider;

        //looping through the given steps with different wait and go times
        for(int i = 0; !interrupt ;i++) {
            if (i == (int) (count * mod)) {
                try {
                    if (moving == true) {
                        s.moveToAngle(s.getPositionInDegrees());
                        moving = false;
                    }
                    Thread.sleep(s.getTimePerDegreeInMilli()*10);
                    //It stops
                    count++;
                }
                //Not useable catch, because it cant see the possibility of an invalid Argument
                //catch (InvalidArgumentException iae) {
                //    iae.printStackTrace();
                //}
                catch (InterruptedException ie) {
                    ie.printStackTrace();
                }

            }
            //Moving either to the maximum angle for the time being or the minimum
            else {
                //It Moves
                if (pos) {
                    //Defining a softwarebased limit for the rotationdegree
                    if (startPosition < s.getMaximumAngle() - 1) {
                        s.moveToAngle(startPosition + 1);
                        startPosition += 1;
                        moving = true;
                    }
                } else {
                    //Defining a softwarebased limit for the rotationdegree
                    if (startPosition > 1) {
                        s.moveToAngle(startPosition - 1);
                        startPosition -= 1;
                        moving = true;
                    }
                }
            }
        }
    }
}
