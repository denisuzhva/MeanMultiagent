from pade.misc.utility import start_loop 
from pade.acl.aid import AID
from NodeAgent import NodeAgent
from sys import argv



class MainController:
    
    def __init__(self):
        self.__num_of_agents = 2
        self.__agent_list = list()


    def initAgents(self):
        c = 0
        for agent_iter in range(self.__num_of_agents):
            port_num = int(argv[1]) + c
            agent_name = 'agent_test_{}@localhost:{}'.format(port_num, port_num)
            agent_test = NodeAgent(AID(name=agent_name))
            self.__agent_list.append(agent_test)
            c += 1000

        start_loop(self.__agent_list)
