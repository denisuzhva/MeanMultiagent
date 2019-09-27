package main.java.ru.denisuzhva.multiAgentAverage;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
//import jade.core.event.MessageAdapter;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
//import jade.tools.sniffer.Message;
//import jade.tools.sniffer.Message;
//import java.nio.channels.InterruptedByTimeoutException;

import java.util.*;



public class NumberAgent extends Agent {
    private Integer[] linkedAgents;
    private HashSet<Integer> rootAgentSet;
    private String sumRequestConvId = "average-request";
    private float guessedNumber;


    @Override
    protected void setup() {
        Object[] args = getArguments();
        linkedAgents = (Integer[])args.clone();
        //Collection<Integer> linkedAgentsCollection = Arrays.asList(linkedAgents);

        rootAgentSet = new HashSet<>();

        int agentId = Integer.parseInt(getAID().getLocalName());
        Random rand = new Random();
        guessedNumber = rand.nextInt(100);
        System.out.println("Agent #" + agentId + " is ready; number guessed: " + guessedNumber);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (agentId == 0) {
            addBehaviour(new RequestNumAver());
        }
        if (agentId != 0) {
            addBehaviour(new NumberRequestListener(this, 100));
        }
    }


    @Override
    protected void takeDown() {
        int agentId = Integer.parseInt(getAID().getLocalName());
        System.out.println("Agent #" + agentId + " is terminating");
    }


    private class RequestNumAver extends Behaviour {
        private float globalSum = guessedNumber;
        private int globalAgentCount = 1;
        private int propagateCount = 0;
        private int informCount = 0;
        private int refuseCount = 0;
        private String selfIdString;
        private MessageTemplate mt;
        private int step = 0;

        @Override
        public void action() {
            switch (step) {
                case 0:
                    selfIdString = getAID().getLocalName();
                    rootAgentSet.add(Integer.parseInt(selfIdString));
                    ACLMessage sumRequestMsg = new ACLMessage(ACLMessage.REQUEST);
                    sumRequestMsg.setContent(selfIdString);
                    sumRequestMsg.setConversationId(sumRequestConvId);
                    sumRequestMsg.setReplyWith("root-request" + System.currentTimeMillis());
                    for (Integer linkedAgent : linkedAgents) {
                        System.out.println("Root Agent #" +
                                selfIdString +
                                " sent a request message to its linked Agent #" + linkedAgent);
                        sumRequestMsg.addReceiver(new AID(Integer.toString(linkedAgent), AID.ISLOCALNAME));
                    }
                    myAgent.send(sumRequestMsg);
                    for (jade.util.leap.Iterator it = sumRequestMsg.getAllReceiver(); it.hasNext(); ) {
                        System.out.println(it.next());
                    }
                    mt = MessageTemplate.and(MessageTemplate.MatchInReplyTo(sumRequestMsg.getReplyWith()),
                            MessageTemplate.and(MessageTemplate.MatchConversationId(sumRequestConvId),
                            MessageTemplate.not(MessageTemplate.MatchPerformative(ACLMessage.REQUEST))));
                    step = 1;
                    break;

                case 1:
                    //System.out.println("Root is on STEP 1");
                    ACLMessage replyMsg = myAgent.receive(mt);
                    if (replyMsg != null) {
                        if (replyMsg.getPerformative() == ACLMessage.PROPAGATE) {
                            float prevSum = Float.parseFloat(replyMsg.getContent());
                            globalSum += prevSum;
                            propagateCount++;
                        }
                        else if (replyMsg.getPerformative() == ACLMessage.INFORM) {
                            int prevCount = Integer.parseInt(replyMsg.getContent());
                            globalAgentCount += prevCount;
                            informCount++;
                        }
                        else if (replyMsg.getPerformative() == ACLMessage.REFUSE) {
                            refuseCount++;
                        }
                        int totalReplyCount = refuseCount + (propagateCount + informCount) / 2;
                        if (totalReplyCount >= linkedAgents.length) {
                            System.out.println("Refuses: " + refuseCount);
                            System.out.println("Propagates: " + propagateCount);
                            System.out.println("Informs: " + informCount);
                            step = 2;
                        }
                    }
                    else {
                        block();
                    }
                    break;

                case 2:
                    float averageValue = globalSum / globalAgentCount;
                    System.out.println("Global agent count: " + globalAgentCount);
                    System.out.println("Root Agent #" + selfIdString + " displaying average value: " + averageValue);
                    step = 3;
                    break;
            }
        }

        @Override
        public boolean done() {
            return step == 3;
        }
    }


    private class NumberRequestListener extends TickerBehaviour {
        NumberRequestListener(Agent agent, Integer tickLen) {
            super(agent, tickLen);
        }

        private float localSum = guessedNumber;
        private int localAgentCount = 1;
        private int propagateCount = 0;
        private int informCount = 0;
        private int refuseCount = 0;
        private AID prevRequester;
        private ACLMessage incomingMessage;
        private String rootAgent;
        private Integer rootAgentNumber;
        private MessageTemplate mt;
        private int step = 0;

        @Override
        protected void onTick() {
            String selfIdString = getAID().getLocalName();
            switch (step) {
                case 0:
                    //System.out.println("Agent #" + selfIdString + " is on STEP 0");
                    mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                            MessageTemplate.MatchConversationId(sumRequestConvId));
                    incomingMessage = myAgent.receive(mt);
                    if (incomingMessage != null) {
                        prevRequester = incomingMessage.getSender();
                        System.out.println("A #" + selfIdString + " GOT a request FROM A #" + prevRequester.getLocalName());
                        rootAgent = incomingMessage.getContent();
                        rootAgentNumber = Integer.parseInt(rootAgent);
                        if (rootAgentSet.contains(rootAgentNumber)) {
                            //System.out.println("REFUSE: " + incomingMessage.getPerformative());
                            ACLMessage reply = incomingMessage.createReply();
                            reply.setPerformative(ACLMessage.REFUSE);
                            reply.setConversationId(sumRequestConvId);
                            myAgent.send(reply);
                        }
                        else {
                            //System.out.println("CONFIRM");
                            rootAgentSet.add(rootAgentNumber);
                            step = 1;
                        }
                    }
                    else {
                        block();
                    }
                    break;

                case 1:
                    ACLMessage localRequestMsg = new ACLMessage(ACLMessage.REQUEST);
                    localRequestMsg.setContent(rootAgent);
                    localRequestMsg.setConversationId(sumRequestConvId);
                    //localRequestMsg.setReplyWith("local-request" + System.currentTimeMillis());

                    boolean sendPerformed = false;
                    for (Integer linkedAgent : linkedAgents) {
                        if (!linkedAgent.equals(Integer.parseInt(prevRequester.getLocalName()))) {
                            System.out.println("A #" + selfIdString + " SENT a request TO A #" + linkedAgent);
                            localRequestMsg.addReceiver(new AID(Integer.toString(linkedAgent), AID.ISLOCALNAME));
                            sendPerformed = true;
                        }
                    }

                    if (sendPerformed) {
                        myAgent.send(localRequestMsg);
                        //mt = MessageTemplate.and(MessageTemplate.MatchInReplyTo(localRequestMsg.getReplyWith()),
                        //        MessageTemplate.MatchConversationId(sumRequestConvId));
                        mt = MessageTemplate.MatchConversationId(sumRequestConvId);
                        step = 2;
                    }
                    else {
                        step = 3;
                    }
                    break;

                case 2:
                    ACLMessage replyMsg = myAgent.receive(mt);
                    if (replyMsg != null) {
                        System.out.println("Agent #" +
                                selfIdString +
                                " received performative " +
                                replyMsg.getPerformative() +
                                " from A #" +
                                replyMsg.getSender().getLocalName());
                        if (replyMsg.getPerformative() == ACLMessage.PROPAGATE) {
                            float prevSum = Float.parseFloat(replyMsg.getContent());
                            localSum += prevSum;
                            propagateCount++;
                        }
                        else if (replyMsg.getPerformative() == ACLMessage.INFORM) {
                            int prevCount = Integer.parseInt(replyMsg.getContent());
                            localAgentCount += prevCount;
                            informCount++;
                        }
                        else if (replyMsg.getPerformative() == ACLMessage.REFUSE) {
                            refuseCount++;
                        }
                        else if (replyMsg.getPerformative() == ACLMessage.REQUEST &&
                                !replyMsg.getSender().getLocalName().equals(rootAgent)) {
                            ACLMessage refuseReply = replyMsg.createReply();
                            refuseReply.setPerformative(ACLMessage.REFUSE);
                            refuseReply.setConversationId(sumRequestConvId);
                            myAgent.send(refuseReply);
                            System.out.println("A #" + selfIdString +
                                    " refused to A #" +
                                    replyMsg.getSender().getLocalName() +
                                    " from STEP 2");
                        }
                        int totalReplyCount = refuseCount + (propagateCount + informCount) / 2;
                        //System.out.println("A #" + selfIdString + " totalReplies: " + totalReplyCount);
                        if (totalReplyCount >= linkedAgents.length-1) {
                            step = 3;
                            propagateCount = 0;
                            informCount = 0;
                            refuseCount = 0;
                        }
                    }
                    else {
                        block();
                    }
                    break;

                case 3:
                    ACLMessage localSumMsg = incomingMessage.createReply();
                    ACLMessage localAgentCountMsg = incomingMessage.createReply();

                    localSumMsg.setPerformative(ACLMessage.PROPAGATE);
                    localAgentCountMsg.setPerformative(ACLMessage.INFORM);

                    localSumMsg.setConversationId(sumRequestConvId);
                    localAgentCountMsg.setConversationId(sumRequestConvId);

                    localSumMsg.setContent(String.valueOf(localSum));
                    localAgentCountMsg.setContent(String.valueOf(localAgentCount));

                    System.out.println("A #" + selfIdString + " sent PROPAGATE and INFORM to A #" + prevRequester.getLocalName());
                    myAgent.send(localSumMsg);
                    myAgent.send(localAgentCountMsg);
                    step = 0;
                    break;
            }
        }

        /*
        @Override
        public boolean done() {
            return (step == 4);
        }
        */
    }


    // Misc
    private boolean isNumeric(String strNum) {
        try {
            double d = Float.parseFloat(strNum);
        } catch (NumberFormatException | NullPointerException nfe) {
            return false;
        }
        return true;
    }
}
