package at.pria.osiris.osiris.controllers;

/**
 * @author Ari Ayvazyan
 * @version 05.Dec.14
 */
public class ConnectionNotEstablishedException extends Exception {
    public ConnectionNotEstablishedException(Exception e){
        super(e);
    }
}
