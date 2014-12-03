package at.pria.osiris.osiris.Controllers;

import at.pria.osiris.osiris.Controllers.Hedgehog.HedgehogSetup;

/**
 * @author Ari Ayvazyan
 * @version 03.Dec.14
 */
public class ControllerFactory {
    public static ControllerSetup getControllerSetup(ControllerType controllerType){
        switch (controllerType){
            case Hedgehog:
                return new HedgehogSetup();
        }
        throw new RuntimeException("Controller not found: " + controllerType.toString());
    }
}