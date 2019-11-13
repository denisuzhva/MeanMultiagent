package main.java.ru.denisuzhva.multiAgentAverage;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
//import jade.tools.sniffer.Message;

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

        rootAgentSet = new HashSet<>();

        int agentId = Integer.parseInt(getAID().getLocalName());
        Random rand = new Random();
        guessedNumber = rand.nextInt(100);
        System.out.println("Agent #" + agentId + " is ready; number guessed: " + guessedNumber);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (agentId == 0) {
            addBehaviour(new RequestNumAver());
        }
        addBehaviour(new NumberRequestListener());
    }


    @Override
    protected void takeDown() {
        int agentId = Integer.parseInt(getAID().getLocalName());
        System.out.println("Agent #" + agentId + " is terminating");
    }


    private class RequestNumAver extends CyclicBehaviour {
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

                /*
                Send request
                 */
                case 0:
                    selfIdString = getAID().getLocalName();
                    rootAgentSet.add(Integer.parseInt(selfIdString));
                    ACLMessage sumRequestMsg = new ACLMessage(ACLMessage.REQUEST);
                    sumRequestMsg.setContent(selfIdString);
                    sumRequestMsg.setConversationId(sumRequestConvId);
                    sumRequestMsg.setReplyWith("root-request" + System.currentTimeMillis());

                    for (Integer linkedAgent : linkedAgents) {
                        /*
                        System.out.println("Root Agent #" +
                                selfIdString +
                                " sent a request message to its linked Agent #" + linkedAgent);
                        */
                        sumRequestMsg.addReceiver(new AID(Integer.toString(linkedAgent), AID.ISLOCALNAME));
                    }
                    myAgent.send(sumRequestMsg);
                    /*
                    for (jade.util.leap.Iterator it = sumRequestMsg.getAllReceiver(); it.hasNext(); ) {
                        System.out.println(it.next());
                    }
                    */
                    mt = MessageTemplate.and(MessageTemplate.MatchInReplyTo(sumRequestMsg.getReplyWith()),
                                             MessageTemplate.and(MessageTemplate.MatchConversationId(sumRequestConvId),
                                                                 MessageTemplate.not(MessageTemplate.MatchPerformative(ACLMessage.REQUEST))));
                    step = 1;
                    break;

                /*
                Receive all replies
                 */
                case 1:
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
                            /*
                            System.out.println("Refuses: " + refuseCount);
                            System.out.println("Propagates: " + propagateCount);
                            System.out.println("Informs: " + informCount);
                            */
                            step = 2;
                            propagateCount = 0;
                            informCount = 0;
                            refuseCount = 0;
                        }
                    }
                    else {
                        block();
                    }
                    break;

                /*
                Calculate average
                 */
                case 2:
                    float averageValue = globalSum / globalAgentCount;
                    System.out.println("Global agent count: " + globalAgentCount);
                    System.out.println("Root Agent #" + selfIdString + " displaying average value: " + averageValue);
                    step = 3;
                    break;

                /*
                Send termination message
                 */
                case 3:
                    ACLMessage terminatorMsg = new ACLMessage(ACLMessage.CONFIRM);
                    terminatorMsg.setConversationId(sumRequestConvId);
                    for (Integer linkedAgent : linkedAgents) {
                        terminatorMsg.addReceiver(new AID(Integer.toString(linkedAgent), AID.ISLOCALNAME));
                    }
                    myAgent.send(terminatorMsg);
                    myAgent.doDelete();
                    step = 4;
                    break;
            }
        }
    }


    private class NumberRequestListener extends CyclicBehaviour {

        private float localSum = guessedNumber;
        private int localAgentCount = 1;
        private int propagateCount = 0;
        private int informCount = 0;
        private int refuseCount = 0;
        private Integer prevRequester;
        private ACLMessage incomingMessage;
        private String rootAgent;
        private MessageTemplate mt;
        private int step = 0;

        @Override
        public void action() {
            String selfIdString = getAID().getLocalName();
            switch (step) {

                /*
                Read incoming request or confirm
                 */
                case 0:
                    mt = MessageTemplate.and(MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                                                                MessageTemplate.MatchPerformative(ACLMessage.CONFIRM)),
                                             MessageTemplate.MatchConversationId(sumRequestConvId));
                    incomingMessage = myAgent.receive(mt);
                    if (incomingMessage != null) {
                        if (incomingMessage.getPerformative() == ACLMessage.REQUEST) {
                            step = 1;
                        }
                        else if (incomingMessage.getPerformative() == ACLMessage.CONFIRM) {
                            step = 5;
                        }
                    }
                    else {
                        block();
                    }
                    break;

                /*
                Analyze request
                 */
                case 1:
                    prevRequester = Integer.parseInt(incomingMessage.getSender().getLocalName());
                    //System.out.println("A #" + selfIdString + " GOT a request FROM A #" + prevRequester);
                    rootAgent = incomingMessage.getContent();
                    Integer rootAgentNumber = Integer.parseInt(rootAgent);
                    if (rootAgentSet.contains(rootAgentNumber)) {
                        ACLMessage reply = incomingMessage.createReply();
                        reply.setPerformative(ACLMessage.REFUSE);
                        reply.setConversationId(sumRequestConvId);
                        myAgent.send(reply);
                        step = 0;
                    }
                    else {
                        rootAgentSet.add(rootAgentNumber);
                        step = 2;
                    }
                    break;

                /*
                Send requests to linked agents
                 */
                case 2:
                    ACLMessage localRequestMsg = new ACLMessage(ACLMessage.REQUEST);
                    localRequestMsg.setContent(rootAgent);
                    localRequestMsg.setConversationId(sumRequestConvId);
                    //localRequestMsg.setReplyWith("local-request" + System.currentTimeMillis());

                    boolean sendPerformed = false;
                    for (Integer linkedAgent : linkedAgents) {
                        if (!linkedAgent.equals(prevRequester)) {
                            localRequestMsg.addReceiver(new AID(Integer.toString(linkedAgent), AID.ISLOCALNAME));
                            sendPerformed = true;
                        }
                    }

                    if (sendPerformed) {
                        myAgent.send(localRequestMsg);
                        //mt = MessageTemplate.and(MessageTemplate.MatchInReplyTo(localRequestMsg.getReplyWith()),
                        //        MessageTemplate.MatchConversationId(sumRequestConvId));
                        mt = MessageTemplate.MatchConversationId(sumRequestConvId);
                        step = 3;
                    }
                    else {
                        step = 4;
                    }
                    break;

                /*
                Read replies from linked agents
                 */
                case 3:
                    ACLMessage replyMsg = myAgent.receive(mt);
                    if (replyMsg != null) {
                        /*
                        System.out.println("Agent #" +
                                selfIdString +
                                " received performative " +
                                replyMsg.getPerformative() +
                                " from A #" +
                                replyMsg.getSender().getLocalName());
                        */
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
                        else if (replyMsg.getPerformative() == ACLMessage.REQUEST) {
                            ACLMessage refuseReply = replyMsg.createReply();
                            refuseReply.setPerformative(ACLMessage.REFUSE);
                            refuseReply.setConversationId(sumRequestConvId);
                            myAgent.send(refuseReply);
                            /*
                            System.out.println("A #" + selfIdString +
                                    " refused to A #" +
                                    replyMsg.getSender().getLocalName() +
                                    " from STEP 2");
                            */
                        }
                        int totalReplyCount = refuseCount + (propagateCount + informCount) / 2;
                        //System.out.println("A #" + selfIdString + " totalReplies: " + totalReplyCount);
                        if (totalReplyCount >= linkedAgents.length-1) {
                            step = 4;
                            propagateCount = 0;
                            informCount = 0;
                            refuseCount = 0;
                        }
                    }
                    else {
                        block();
                    }
                    break;

                /*
                Propagate data
                 */
                case 4:
                    ACLMessage localSumMsg = incomingMessage.createReply();
                    ACLMessage localAgentCountMsg = incomingMessage.createReply();

                    localSumMsg.setPerformative(ACLMessage.PROPAGATE);
                    localAgentCountMsg.setPerformative(ACLMessage.INFORM);

                    localSumMsg.setConversationId(sumRequestConvId);
                    localAgentCountMsg.setConversationId(sumRequestConvId);

                    localSumMsg.setContent(String.valueOf(localSum));
                    localAgentCountMsg.setContent(String.valueOf(localAgentCount));

                    //System.out.println("A #" + selfIdString + " sent PROPAGATE and INFORM to A #" + prevRequester.getLocalName());

                    myAgent.send(localSumMsg);
                    myAgent.send(localAgentCountMsg);
                    step = 0;
                    break;

                /*
                Terminate agent
                 */
                case 5:
                    ACLMessage confirmReply = incomingMessage.createReply();
                    confirmReply.setPerformative(ACLMessage.CONFIRM);
                    confirmReply.setConversationId(sumRequestConvId);
                    sendPerformed = false;
                    for (Integer linkedAgent : linkedAgents) {
                        if (!linkedAgent.equals(prevRequester)) {
                            confirmReply.addReceiver(new AID(Integer.toString(linkedAgent), AID.ISLOCALNAME));
                            sendPerformed = true;
                        }
                    }
                    if (sendPerformed) {
                        myAgent.send(confirmReply);
                    }
                    myAgent.doDelete();
                    break;
            }
        }
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
