package main.java.ru.denisuzhva.multiAgentAverage;

import jade.core.Agent;


public class NumberAgent extends Agent {
    private String[] linkedAgents;
    private float number;

    @Override
    protected void setup() {
        int id = Integer.parseInt(getAID().getLocalName());
        System.out.println("Agent #" + id);

        addBehaviour(new FindAverage(this, 6000));
    }
}
