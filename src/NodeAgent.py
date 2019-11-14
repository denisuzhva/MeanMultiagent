from pade.misc.utility import display_message
from pade.core.agent import Agent
from pade.behaviours.protocols import TimedBehaviour
from pade.acl.aid import AID



class SenderBehav(TimedBehaviour):

    def __init__(self, agent, time, content):
        super(SenderBehav, self).__init__(agent, time)
        self.__agent = agent
        self.__content = content

    def on_time(self):
        super(SenderBehav, self).on_time()
                 


class NodeAgent(Agent):
    
    def __init__(self, aid):

        super(NodeAgent, self).__init__(aid=aid)
        send_delay = 1.0
        sender_behav = SenderBehav(self, send_delay)
        self.behaviours.append(sender_behav)

    
    
