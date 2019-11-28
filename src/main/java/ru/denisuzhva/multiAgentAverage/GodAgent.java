package main.java.ru.denisuzhva.multiAgentAverage;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
//import jade.tools.sniffer.Message;

import java.util.*;



public class GodAgent extends Agent {

	private String selfId;
    private String consensusStateConvId;
	private int numberOfAgents;
	private int iterrr;
	private Float stateVal;
	private Float stateMean;


	
	@Override
	protected void setup() {
		consensusStateConvId = "consensus-state";
		selfId = getAID().getLocalName();
        System.out.println("Agent " + selfId + " is ready");

        Object[] args = getArguments();
		Integer[] argsInts = (Integer[])args.clone();
        numberOfAgents = argsInts[0];
		iterrr = 0;
		stateVal = 0.0f;
		stateMean = 0.0f;

		addBehaviour(new CalculatorServer());
	}


	@Override
    protected void takeDown() {
        System.out.println("Agent " + selfId + " is terminating");
    }


	private class CalculatorServer extends Behaviour {

		public void action() {
			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE),
					MessageTemplate.MatchConversationId(consensusStateConvId));
			ACLMessage stateMsg = myAgent.receive(mt);
			if (stateMsg != null) {
				stateVal = Float.parseFloat(stateMsg.getContent());
				stateMean += stateVal;
				iterrr++;
			}
		}

		public boolean done() {
			if (iterrr >= numberOfAgents) {
				stateMean /= numberOfAgents;
				System.out.println("True mean: " + stateMean);
				return true;
			} else {
				return false;
			}
		}
	}
}
