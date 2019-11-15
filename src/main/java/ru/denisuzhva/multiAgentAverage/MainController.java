package main.java.ru.denisuzhva.multiAgentAverage;

//import com.sun.tools.javac.Main;
//import jade.wrapper.AgentContainer;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

import java.util.*;


class MainController {
    private static int numberOfAgents = 10;

    MainController() {
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
                        "main.java.ru.denisuzhva.multiAgentAverage.NodeAgent",
                        null);
                agent.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


