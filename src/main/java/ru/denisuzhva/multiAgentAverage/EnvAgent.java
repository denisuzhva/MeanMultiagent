package main.java.ru.denisuzhva.multiAgentAverage;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
//import jade.tools.sniffer.Message;

import java.util.*;



public class EnvAgent extends Agent {

	private String selfId;
    private String consensusStateConvId;
	

	@Override
	protected void setup() {
		consensusStateConvId = "consensus-state";
		selfId = getAID().getLocalName();
        System.out.println("Agent " + selfId + " is ready");

		addBehaviour(new StateTranslator());
		addBehaviour(new PoolTranslator());
		addBehaviour(new StateRefillTranslator());
	}


	@Override
    protected void takeDown() {
        System.out.println("Agent " + selfId + " is terminating");
    }


	private class StateTranslator extends CyclicBehaviour {

		private float inState;
		private float outState;

		public void action() {
			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE), 
					MessageTemplate.MatchConversationId(consensusStateConvId));
			ACLMessage stateMsg = myAgent.receive(mt);
			if (stateMsg != null) {
				String msgContent = stateMsg.getContent();
				String[] contentList = msgContent.split(" ");
				inState = Float.parseFloat(contentList[0]);

				// do noizy stuff here
				outState = inState;

				ACLMessage stateFurther = new ACLMessage(ACLMessage.PROPAGATE);
				stateFurther.setContent(String.valueOf(outState));
				stateFurther.setConversationId(consensusStateConvId);
				stateFurther.setReplyWith(stateMsg.getReplyWith());
				stateFurther.setSender(stateMsg.getSender());
				for (int i = 1; i < contentList.length; i++) {
					String contentEnt = contentList[i];
					stateFurther.addReceiver(new AID(contentEnt, AID.ISLOCALNAME));
				}
				myAgent.send(stateFurther);
			}
		}
	}


	private class PoolTranslator extends CyclicBehaviour {

		private float inPool;
		private float outPoolDist;
		private int numNeigh;
		private HashMap<String, HashMap<String, ArrayList<Float>>> poolHolder = new HashMap<String, HashMap<String, ArrayList<Float>>>();

		public void action() {
			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.PROPOSE), 
					MessageTemplate.MatchConversationId(consensusStateConvId));
			ACLMessage poolMsg = myAgent.receive(mt);
			if (poolMsg != null) {
				String msgContent = poolMsg.getContent();
				String[] contentList = msgContent.split(" ");
				String senderId = poolMsg.getSender().getLocalName();
				inPool = Float.parseFloat(contentList[0]);
				numNeigh = contentList.length - 1;

				// do noizy stuff here

				/*
				outPoolDist = inPool / numNeigh;

				ACLMessage poolFurther = new ACLMessage(ACLMessage.PROPOSE);
				poolFurther.setContent(String.valueOf(outPoolDist));
				poolFurther.setConversationId(consensusStateConvId);
				poolFurther.setReplyWith(poolMsg.getReplyWith());
				poolFurther.setSender(poolMsg.getSender());
				for (int i = 1; i < contentList.length; i++) {
					String recipientId = contentList[i];
					poolFurther.addReceiver(new AID(recipientId, AID.ISLOCALNAME));
				}
				myAgent.send(poolFurther);
				*/

				Random rand = new Random();
				float nxtNormal = 0.01f * (float)rand.nextGaussian();
				outPoolDist = inPool / numNeigh + nxtNormal;

				float nxtUni = rand.nextFloat();
				float nxtUniLose = rand.nextFloat();
				for (int i = 1; i < contentList.length; i++) {
					String recipientId = contentList[i];
					if (nxtUni < 0.99f) {
						if (nxtUniLose < 0.99f) {
							ACLMessage poolFurtherMsg = new ACLMessage(ACLMessage.PROPOSE);
							poolFurtherMsg.setContent(String.valueOf(outPoolDist));
							poolFurtherMsg.setConversationId(consensusStateConvId);
							poolFurtherMsg.setReplyWith(poolMsg.getReplyWith());
							poolFurtherMsg.setSender(poolMsg.getSender());
							poolFurtherMsg.addReceiver(new AID(recipientId, AID.ISLOCALNAME));
							myAgent.send(poolFurtherMsg);

							if (poolHolder.get(senderId) != null) {
								HashMap<String, ArrayList<Float>> poolHolderMap = poolHolder.get(senderId);
								if (poolHolderMap.get(recipientId) != null) {
									List<Float> poolHolderMapList = poolHolderMap.get(recipientId);
									for (int iterr = 0; iterr < poolHolderMapList.size(); iterr++) {
										Float valGet = poolHolderMapList.get(iterr);
										poolHolder.get(senderId).get(recipientId).remove(valGet);

										poolFurtherMsg = new ACLMessage(ACLMessage.PROPOSE);
										poolFurtherMsg.setContent(String.valueOf(valGet));
										poolFurtherMsg.setConversationId(consensusStateConvId);
										poolFurtherMsg.setReplyWith(poolMsg.getReplyWith());
										poolFurtherMsg.setSender(poolMsg.getSender());
										poolFurtherMsg.addReceiver(new AID(recipientId, AID.ISLOCALNAME));
										myAgent.send(poolFurtherMsg);
										//System.out.println("shit happens");
									}
								}
							}
						}
							
					} else {
						if (poolHolder.get(senderId) != null) {
							if (poolHolder.get(senderId).get(recipientId) != null) {
								poolHolder.get(senderId).get(recipientId).add(outPoolDist);
							} else {
								poolHolder.get(senderId).put(recipientId, new ArrayList<>());
								poolHolder.get(senderId).get(recipientId).add(outPoolDist);
							}
						} else {
							poolHolder.put(senderId, new HashMap<>());
							poolHolder.get(senderId).put(recipientId, new ArrayList<>());
							poolHolder.get(senderId).get(recipientId).add(outPoolDist);
						}
					}
				}

			}
		}
	}


	private class StateRefillTranslator extends CyclicBehaviour {

		private float inRefill;
		private float outRefill;
		private String msgContent;
		private String[] contentList;
		private String recipient;


		public void action() {
			MessageTemplate mt = MessageTemplate.and(MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
						MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL)),
					MessageTemplate.MatchConversationId(consensusStateConvId));
			ACLMessage refillMsg = myAgent.receive(mt);
			if (refillMsg != null) {
				msgContent = refillMsg.getContent();
				contentList = msgContent.split(" ");
				inRefill = Float.parseFloat(contentList[0]);
				recipient = contentList[1];

				// do noizy stuff here
				outRefill = inRefill; 

				ACLMessage refillFurther = new ACLMessage(ACLMessage.INFORM);
				refillFurther.setContent(String.valueOf(outRefill));
				refillFurther.setConversationId(consensusStateConvId);
				refillFurther.setReplyWith(refillMsg.getReplyWith());
				refillFurther.setSender(refillMsg.getSender());
				refillFurther.addReceiver(new AID(recipient, AID.ISLOCALNAME));
				myAgent.send(refillFurther);
			}
		}
	}
}
