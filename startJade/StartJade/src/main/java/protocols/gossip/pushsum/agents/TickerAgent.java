package protocols.gossip.pushsum.agents;

import java.util.ArrayList;


import java.util.Arrays;
import java.util.List;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import protocols.Role;
import protocols.gossip.pushsum.PushSumProtocol;

/**
 * 
 * Agent used for the PushSum protocol
 *
 */

public class TickerAgent extends Agent {
	private static final long serialVersionUID = 1L;
	/**
	 * @param nbRounds :the number of rounds set by user 
	 */
	private int nbRounds;
	protected void setup(){
		super.setup();
		List<String> agentsNames = getListAgent(this);
		final Object[] args = getArguments();
		this.nbRounds= (int) args[0];
		PushSumProtocol pushSum = new PushSumProtocol ();
		for(Behaviour b: pushSum.getBehaviours(Role.Ticker,this,agentsNames)) {
			addBehaviour(b);
		}
		
	}
 
	private static List<String> getListAgent(Agent agent) {

		AMSAgentDescription [] agentsDescriptionCatalog = null;
		List <String> agentsNames= new ArrayList<String>();

		try {
			SearchConstraints c = new SearchConstraints();
			c.setMaxResults(Long.valueOf(-1) );
			agentsDescriptionCatalog = AMSService.search(agent, new
					AMSAgentDescription (), c );

		}catch (Exception e) {
			System.out. println ( "Problem searching AMS: " + e );
			e.printStackTrace () ;
		}

		for (int i=0; i<agentsDescriptionCatalog.length ; i++){
			AID agentID = agentsDescriptionCatalog[i].getName();
			if (!isIn(agentID,agent.getLocalName()))
				agentsNames.add(agentID.getLocalName());
		}

		return agentsNames;

	}

	private static boolean isIn(AID agentID,String agentLocalName) {
		List<String> doNotSniff= Arrays.asList("rma","sniffeur","ams","df",agentLocalName);
		return doNotSniff.stream().anyMatch(e -> agentID.getLocalName().contains(e));
	}
	
	
	
	public int getNbRounds() {
		return nbRounds;
	}
	public void setNbRounds(int nbRounds) {
		this.nbRounds = nbRounds;
	}

	

	

}

