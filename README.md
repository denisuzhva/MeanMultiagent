# MeanMultiagent

## Problem statement

Given a set of numbers, calculate the mean value for this set.
The numbers are stored in separate devices (robots, servers, etc.) called *agents*, that are allowed to communicate to each other using a common language.
We further refer to these numbers as states of the agents.
Meanwhile, the devices may not be connected all to each other, i.e. one agent may not be able to communicate to all the rest ones (the graph of the network is not fully connected).
The devices can also communicate to the Data Center, which is interested in obtaining the mean value.

Each act of sending a message has its cost.
For inter-agent messages, this cost is low: the devices can exchange data between each other quite often.
However, a message from a device to the Data Center (and vice versa) is relatively expensive: let's say, a million times more expensive than a single inter-agent message.

With that in mind, we wish to make the network calculate the mean value using only inter-device communications.

## Solution

The main idea behind the solution is to give an equal task for each agent, which will be described below.
An agent "corrects" its own value based on the values of the agents, which are able to communicate to the former (send him a number).
For example, if the state of one agent is 50 and it receives 70 from a neighbor, then the former one enlarges its state slightly towards 70.
This strategy forms the base of the "local-voting protocol" algorithm.
For more information regarding the local voting, see https://www.researchgate.net/publication/271427886_Local_voting_protocol_in_decentralized_load_balancing_problem_with_switched_topology_noise_and_delays

## Implementation

The multiagent network is implemented using the JADE (Java Agent DEvelopment) framework.
* Build the project: `./gradlew build`.
* Run the project: `./gradlew run`.

