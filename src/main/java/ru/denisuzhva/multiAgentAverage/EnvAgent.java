package main.java.ru.denisuzhva.multiAgentAverage;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
//import jade.tools.sniffer.Message;

import java.util.*;



public class EnvAgent extends Agent {
	private static int numberOfAgents = 10;
    private static HashMap<Integer, Integer[]> agentConnectivity;

	@Override
	protected void setup() {
        agentConnectivity = new HashMap<>();
	}

	private void traingleTen1() {
		numberOfAgents = 10;


	}
}
