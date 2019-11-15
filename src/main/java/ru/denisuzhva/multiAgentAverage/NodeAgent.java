package main.java.ru.denisuzhva.multiAgentAverage;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.*;



public class NodeAgent extends Agent {
    private String sumRequestConvId = "average-request";
    private float guessedNumber;
	private float uBuffer;
	private agentId = Integer.parseInt(getAID().getLocalName());

    @Override
    protected void setup() {
        Random rand = new Random();
        guessedNumber = rand.nextInt(100);
        System.out.println("Agent #" + agentId + " is ready; number guessed: " + guessedNumber);
    }

    @Override
    protected void takeDown() {
        System.out.println("Agent #" + agentId + " is terminating");
    }
}
