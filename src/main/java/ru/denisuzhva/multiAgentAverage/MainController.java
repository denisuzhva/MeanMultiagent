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
    private static HashMap<Integer, Integer[]> agentConnectivity;

	
	MainController() {
		agentConnectivity = new HashMap<>();
		triangleTen1();
	}


    void initAgents() {
        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();
        p.setParameter(Profile.MAIN_HOST, "localhost");
        p.setParameter(Profile.MAIN_PORT, "10098");
        p.setParameter(Profile.GUI, "false");
        ContainerController cc = rt.createMainContainer(p);

        try {
            for (int i = 0; i < numberOfAgents; i++) {
                AgentController NodeAgent = cc.createNewAgent(Integer.toString(i),
					"main.java.ru.denisuzhva.multiAgentAverage.NodeAgent",
					agentConnectivity.get(i));
                NodeAgent.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

		try {
			AgentController EnvAgent = cc.createNewAgent("env",
				"main.java.ru.denisuzhva.multiAgentAverage.EnvAgent",
				null);
			EnvAgent.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }


	// Graph makers
	private void triangleTen1() {
		numberOfAgents = 10;
        agentConnectivity.put(0, new Integer[] {1});

        agentConnectivity.put(1, new Integer[] {2, 3});
        agentConnectivity.put(2, new Integer[] {0, 4});

        agentConnectivity.put(3, new Integer[] {4, 6});
        agentConnectivity.put(4, new Integer[] {1, 5, 7});
        agentConnectivity.put(5, new Integer[] {2, 8});

        agentConnectivity.put(6, new Integer[] {7});
        agentConnectivity.put(7, new Integer[] {3, 8});
        agentConnectivity.put(8, new Integer[] {4, 9});
        agentConnectivity.put(9, new Integer[] {5});
	}


	private void triangleThree() {
		numberOfAgents = 3;
		agentConnectivity.put(0, new Integer[] {1});
		agentConnectivity.put(1, new Integer[] {2});
		agentConnectivity.put(2, new Integer[] {0});
	}
}


