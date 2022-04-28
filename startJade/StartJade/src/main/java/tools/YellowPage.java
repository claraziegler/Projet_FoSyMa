package tools;


import java.util.ArrayList;
import java.util.List;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;


public class YellowPage {

	/**
	 * This function allows to register a service in the Yellow Pages
	 * @param agent:a reference to the agent
	 * @param service: the name of the service
	 */

	public static void add_yellow_page(Agent agent, String service){
		DFAgentDescription dfd = new DFAgentDescription();

		dfd.setName(agent.getAID()); // The agent AID
		ServiceDescription sd = new
				ServiceDescription();
		sd.setType(service); // You have to give a name to each service your agent offers
		sd.setName(service); // (local)name of the agent

		dfd.addServices(sd) ;
		//Register the service

		try {
			DFService.register(agent, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace(); 
		}
	}


	public static void add_yellow_page(Agent agent, List<String> services){
		DFAgentDescription dfd = new DFAgentDescription();

		dfd.setName(agent.getAID()); // The agent AID
		for (String service : services) {
			ServiceDescription sd = new
					ServiceDescription();
			sd.setType(service); // You have to give a name to each service your agent offers
			sd.setName(agent.getName()); // (local)name of the agent
			dfd.addServices(sd) ;
		}
		//Register the service

		try {
			DFService.register(agent, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace(); 
		}
	}


	/**
	 * This function allows to deregister a service in the Yellow Pages
	 * @param agent :a reference to the agent
	 * @param service : the name of the service 
	 */

	public static void remove_yellow_page(Agent agent, String service){
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(agent.getAID()); // The agent AID
		ServiceDescription sd = new
				ServiceDescription();
		sd.setType(service); // You have to give a name to each service your agent offers
		sd.setName(agent.getLocalName()); // (local)name of the agent
		dfd.addServices(sd) ;
		try { DFService.deregister(agent, dfd); }
		catch (Exception e) {}
	}

	/***
	 * 
	 * @param service : the name of the service 
	 * @param agent : a reference to the agent
	 * @return the list of agents who offers the service "service"
	 */
	public static List<String> list_of_agent(Agent agent, String service) throws FIPAException {
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription () ;
		sd .setType(service); // name of the service
		dfd.addServices(sd) ;
		DFAgentDescription[] result = DFService.search(agent, dfd) ;
		List<String> res = (List<String>) new ArrayList();
		for (DFAgentDescription ag : result) {
			res.add(ag.getName().getName());
		}
		return res;
	}



}