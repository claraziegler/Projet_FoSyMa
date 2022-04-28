package protocols.auctions.agents;

import java.util.ArrayList;
import java.util.List;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import protocols.Role;
import protocols.auctions.IAuction;

public class BidderAgent extends Agent{
	/**
	 * An agent that participate in an auction as a buyer
	 */
	private static final long serialVersionUID = -2736761137217916607L;
	private int budget;
	private int increment;

	public  int getBudget(String ObjectId, String protocol) {
		return budget;
	}
	
	
	/**
	 * used for English and Reverse English auction. 
	 * @param ObjectId : the id of the object to be auctioned
	 * @param protocol : the name of the protocol
	 * @param price : the actual price of the auction
	 * @param minIncrement : the minimal increment fixed by the auctioneer
	 * @return the increment to add to the actual price to make an offer
	 * it returns the personal increment of the agent if it's higher than
	 * the minimal increment if it's higher than the minimal increment 
	 * otherwise it returns the minimal increment
	 */
	public int getIncrement(String ObjectId, String protocol, int price, int minIncrement) {
		if (increment < minIncrement) return minIncrement; 
		return increment;
	}

	
	protected void setup(){

		super.setup();
		List<String> agentsNames = getListAgent(this);

		final Object[] args = getArguments();
		try {
		this.budget = (int)args[1];
		this.increment = (int)args[2];
		
		List<Behaviour> behaviours = ((IAuction) args[0]).getBehaviours(Role.Bidder, this,  agentsNames);
		for(Behaviour beh: behaviours) {
			this.addBehaviour(beh);
		}
		}
		catch (ArrayIndexOutOfBoundsException e){
			System.out.println("Please check that you have given all the necessary parameters to the bidder");
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
		}
		catch (Exception e) {
		System.out. println ( "Problem searching AMS: " + e );
		e . printStackTrace () ;
		}
		for ( int i=0; i<agentsDescriptionCatalog.length ; i++){
		AID agentID = agentsDescriptionCatalog[i ]. getName();
		
		agentsNames.add(agentID.getLocalName());
		}
		agentsNames.removeAll( new ArrayList<String>() {
			private static final long serialVersionUID = -608647102730939556L;

		{
			 add("rma");
			 add("sniffeur");
			 add("ams");
			 add("df");
			 add("Other");
			 add(agent.getLocalName());
			}});
		return agentsNames;
		
	}
	public Role getRole() { 
		return Role.Bidder;
	}
}

