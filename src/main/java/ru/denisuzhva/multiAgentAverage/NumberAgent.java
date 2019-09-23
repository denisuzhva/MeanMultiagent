package main.java.ru.denisuzhva.multiAgentAverage;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
//import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.Random;


public class NumberAgent extends Agent {
    private Integer[] linkedAgents;
    private String requestMessage = "Requesting the number...";
    private float number;

    @Override
    protected void setup() {
        Object[] args = getArguments();
        linkedAgents = (Integer[])args.clone();

        int agentId = Integer.parseInt(getAID().getLocalName());
        Random rand = new Random();
        number = rand.nextFloat();
        System.out.println("Agent #" + agentId + " is ready; number guessed: " + number);

        addBehaviour(new WaitForNumberRequest());
        addBehaviour(new SendNumberRequest());
    }

    @Override
    protected void takeDown() {
        int agentId = Integer.parseInt(getAID().getLocalName());
        System.out.println("Agent #" + agentId + " is terminating");
    }

    private class SendNumberRequest extends OneShotBehaviour{
        /*
        SendNumberRequest(NumberAgent agent, Integer tick) {
            super(agent, tick);
        }
         */

        @Override
        public void action() {
            int agentId = Integer.parseInt(getAID().getLocalName());
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.setContent(requestMessage);
            for (Integer linkedAgent : linkedAgents) {
                msg.addReceiver(new AID(Integer.toString(linkedAgent), AID.ISLOCALNAME));
                send(msg);
            }
        }
    }

    private class WaitForNumberRequest extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = receive();
            if (msg != null) {
                if (msg.getContent().equals(requestMessage)) {
                    int selfId = Integer.parseInt(getAID().getLocalName());
                    int senderId = Integer.parseInt(msg.getSender().getLocalName());
                    System.out.println("WAITING SERVICE: Agent #" + senderId +
                            " requested the number from Agent #" + selfId +
                            ". The number = " + number);
                }
            } else {
                block();
            }
        }
    }
}
