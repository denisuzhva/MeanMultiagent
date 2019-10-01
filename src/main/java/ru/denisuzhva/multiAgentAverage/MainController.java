package main.java.ru.denisuzhva.multiAgentAverage;

import com.sun.tools.javac.Main;
import jade.wrapper.AgentContainer;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

import java.util.*;


class MainController {
    private static int numberOfAgents = 3;
    private static HashMap<Integer, Integer[]> agentConnectivity;

    MainController() {
        agentConnectivity = new HashMap<Integer, Integer[]>();
        triangleGraphThree();
    }

    void initAgents() {
        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();
        p.setParameter(Profile.MAIN_HOST, "localhost");
        p.setParameter(Profile.MAIN_PORT, "10098");
        //p.setParameter(Profile.GUI, "true");
        ContainerController cc = rt.createMainContainer(p);

        try {
            for (int i = 0; i < numberOfAgents; i++) {
                AgentController agent = cc.createNewAgent(Integer.toString(i),
                        "main.java.ru.denisuzhva.multiAgentAverage.NumberAgent",
                        agentConnectivity.get(i));
                agent.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Graph makers

    private void simpleDuo() {
        numberOfAgents = 2;
        agentConnectivity.put(0, new Integer[] {1});
        agentConnectivity.put(1, new Integer[] {0});
    }

    private void triangleGraphSix() {
        // 6 nodes only!
        numberOfAgents = 6;
        agentConnectivity.put(0, new Integer[] {1, 2});
        agentConnectivity.put(1, new Integer[] {0, 2, 3, 4});
        agentConnectivity.put(2, new Integer[] {0, 1, 4, 5});
        agentConnectivity.put(3, new Integer[] {1, 4});
        agentConnectivity.put(4, new Integer[] {1, 2, 3, 5});
        agentConnectivity.put(5, new Integer[] {2, 4});
    }

    private void triangleGraphThree() {
        numberOfAgents = 3;
        agentConnectivity.put(0, new Integer[] {1, 2});
        agentConnectivity.put(1, new Integer[] {0, 2});
        agentConnectivity.put(2, new Integer[] {0, 1});
    }

    private void allConnected() {
        for (int agentIter = 0; agentIter < numberOfAgents; agentIter++) {
            Integer[] linkedAgents = new Integer[numberOfAgents-1];
            for (int i = 0; i < numberOfAgents; i++) {
                if (i < agentIter) {
                    linkedAgents[i] = i;
                } else if (i > agentIter) {
                    linkedAgents[i-1] = i;
                }
            }
            agentConnectivity.put(agentIter, linkedAgents);
        }
    }

    private void straightConnection() {
        numberOfAgents = 4;
        Integer[] temp = new Integer[numberOfAgents-1];
        for (int i = 1; i < numberOfAgents; i++) {
            temp[i-1] = i;
        }
        agentConnectivity.put(0, temp);
        for (int agentIter = 1; agentIter < numberOfAgents; agentIter++) {
            agentConnectivity.put(agentIter, new Integer[] {0});
        }
    }

    private void diamondGraph() {
        numberOfAgents = 4;
        agentConnectivity.put(0, new Integer[] {1, 2});
        agentConnectivity.put(1, new Integer[] {0, 3});
        agentConnectivity.put(2, new Integer[] {0, 3});
        agentConnectivity.put(3, new Integer[] {1, 2});
    }

    private void diamondGraphBar() {
        numberOfAgents = 4;
        agentConnectivity.put(0, new Integer[] {1, 2});
        agentConnectivity.put(1, new Integer[] {0, 2, 3});
        agentConnectivity.put(2, new Integer[] {0, 1, 3});
        agentConnectivity.put(3, new Integer[] {1, 2});
    }

    private void lineGraph() {
        numberOfAgents = 3;
        agentConnectivity.put(0, new Integer[] {1});
        for (int i = 1; i < numberOfAgents-1; i++) {
            agentConnectivity.put(i, new Integer[] {i-1, i+1});
        }
        agentConnectivity.put(numberOfAgents-1, new Integer[] {numberOfAgents-2});
    }
}


