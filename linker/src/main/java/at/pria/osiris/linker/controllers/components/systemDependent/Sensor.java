package at.pria.osiris.linker.controllers.components.systemDependent;

import org.andrix.low.NotConnectedException;
import org.andrix.low.RequestTimeoutException;

public interface Sensor {

	double getCurentValue() throws NotConnectedException, RequestTimeoutException;

}
