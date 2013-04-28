package edu.cit595.qyccy.model;

public enum Respond {

    FWD(100), OK(200), UPDT(210), END(300), BAD(400);
    
    private Respond(int val) {
    }
}
