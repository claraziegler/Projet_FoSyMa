package protocols.consensus.paxos.agents;


import java.util.List;


import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import protocols.Role;
import protocols.auctions.IAuction;


/**
 * The agent will send a request and block until it receives a response.
 * 
 * @author Axel Foltyn
 * 
 */
public class ClientAgent extends Agent {
	private static final long serialVersionUID = -6585527442706244880L;
	
	protected void setup(){
		super.setup();
		
		final Object[] args = getArguments();
		List<String> agentsNames = null;
		List<Behaviour> behaviours = ((IAuction) args[0]).getBehaviours(Role.Client, this,  agentsNames);
		for(Behaviour beh: behaviours) {
			this.addBehaviour(beh);
		}
	}

}
