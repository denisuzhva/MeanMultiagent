package main.java.ru.denisuzhva.multiAgentAverage;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.*;



public class NodeAgent extends Agent {

	private Integer[] linkedAgents;
    private String consensusStateConvId;
    private float selfState;
	private float statePool;
	private float conformity;
	private String selfId;
	private MessageTemplate mt;


    @Override
    protected void setup() {
        Object[] args = getArguments();
        linkedAgents = (Integer[])args.clone();

		selfId = getAID().getLocalName();

		consensusStateConvId = "consensus-state";

        Random rand = new Random();
        selfState = 100 * rand.nextFloat();
		selfState = rand.nextInt(100);
		/*
		if (selfId.equals("0")) {
			selfState = 91.0f;
		} else if (selfId.equals("1")) {
			selfState = 38.0f;
		} else if (selfId.equals("2")) {
			selfState = 79.0f;
		}
		*/

		statePool = 0.0f;
		conformity = 0.1f;

        System.out.println("Agent " + selfId + " is ready; current state: " + selfState);
		addBehaviour(new StateSender(this, 5000));
		addBehaviour(new PoolPusher(this, 100));
		addBehaviour(new StateListener());
		addBehaviour(new PoolFiller());
    }


    @Override
    protected void takeDown() {
        System.out.println("Agent " + selfId + " is terminating");
    }


	private class StateSender extends TickerBehaviour {
		
		StateSender(Agent a, long period) {
			super(a, period);
		}
		
		@Override
		protected void onTick() {
			System.out.println(selfId + " state " + selfState);

			ACLMessage stateOutMsg = new ACLMessage(ACLMessage.PROPAGATE);
			String stateContent = String.valueOf(selfState); 
			for (Integer linkedAgent : linkedAgents) {
				stateContent += " " + String.valueOf(linkedAgent);
			}
			stateOutMsg.setContent(stateContent);
			stateOutMsg.setConversationId(consensusStateConvId);
			stateOutMsg.setReplyWith("consensusJobStatePpg" + System.currentTimeMillis());
			stateOutMsg.addReceiver(new AID("env", AID.ISLOCALNAME));
			myAgent.send(stateOutMsg);
		}
	}


	private class StateListener extends CyclicBehaviour {

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE),
					MessageTemplate.MatchConversationId(consensusStateConvId));
			ACLMessage inStateMsg = myAgent.receive(mt);
			if (inStateMsg != null) {
				Float inState = Float.parseFloat(inStateMsg.getContent());
				statePool += conformity * (selfState - inState);
				//String sender = inStateMsg.getSender().getLocalName();
				//System.out.println(statePool + " " + sender + " " + selfId);
				if (statePool > 0.0f) {
					selfState -= statePool;
					//statePool = 0.0f;
				}
			}
		}
	}


	private class PoolPusher extends TickerBehaviour {

		PoolPusher(Agent a, long period) {
			super(a, period);
		}

		@Override
		protected void onTick() {
			if (statePool > 0.0f) {
				ACLMessage pushMsg = new ACLMessage(ACLMessage.PROPOSE);
				String poolContent = String.valueOf(statePool); 
				for (Integer linkedAgent : linkedAgents) {
					poolContent += " " + String.valueOf(linkedAgent);
				}
				pushMsg.setContent(poolContent);
				pushMsg.setConversationId(consensusStateConvId);
				pushMsg.setReplyWith("consensusJobPoolPps" + System.currentTimeMillis());
				pushMsg.addReceiver(new AID("env", AID.ISLOCALNAME));

				//selfState -= statePool;
				statePool = 0.0f;
				myAgent.send(pushMsg);
			}
		}
	}


	private class PoolFiller extends CyclicBehaviour {

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
					MessageTemplate.MatchConversationId(consensusStateConvId));
			ACLMessage poolIn = myAgent.receive(mt);
			if (poolIn != null) {
				Float inPool = Float.parseFloat(poolIn.getContent());	
				Float newStatePool = statePool + inPool;

				if (statePool <= 0.0f) {
					selfState += Math.min(-statePool, inPool);
					statePool = newStatePool;
				} else {
					statePool = newStatePool;
				}
			}
		}
	}
}
