package main.java.ru.denisuzhva.multiAgentAverage;

import com.sun.tools.javac.Main;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

import java.util.HashMap;


class MainController {
    private static final int numberOfAgents = 5;
    private static HashMap<Integer, Integer[]> agentConnectivity;

    MainController() {
        agentConnectivity = new HashMap<Integer, Integer[]>();
        agentConnectivity.put(0, new Integer[] {1, 2, 3, 4, 5});
        agentConnectivity.put(1, new Integer[]{0, 2, 3, 4, 5});
        agentConnectivity.put(2, new Integer[]{0, 1, 3, 4, 5});
        agentConnectivity.put(3, new Integer[]{0, 1, 2, 4, 5});
        agentConnectivity.put(4, new Integer[]{0, 1, 2, 3, 5});
        agentConnectivity.put(5, new Integer[]{0, 1, 2, 3, 4});
    }

    void initAgents() {
        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();
        p.setParameter(Profile.MAIN_HOST, "localhost");
        p.setParameter(Profile.MAIN_PORT, "10098");
        p.setParameter(Profile.GUI, "true");
        ContainerController cc = rt.createMainContainer(p);

        try {
            for (int i = 0; i < MainController.numberOfAgents; i++) {
                AgentController agent = cc.createNewAgent(Integer.toString(i),
                        "main.java.ru.denisuzhva.multiAgentAverage.NumberAgent",
                        null);
                agent.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


