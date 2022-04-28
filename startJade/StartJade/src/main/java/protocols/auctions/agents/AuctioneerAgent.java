package protocols.auctions.agents;
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
import protocols.auctions.IAuction;


public class AuctioneerAgent extends Agent{
	
	/**
	 * An agent which participate in an auction as an Auctioneer
	 */
	
	private static final long serialVersionUID = -4163058176033718845L;
	private int initPrice;
	private int increment;
	private int reservePrice;
	private String objectId;
	private int waitingPeriod;
	protected void setup(){

		super.setup();
		List<String> agentsNames = getListAgent(this);

		final Object[] args = getArguments();

		try{
		this.initPrice = (int)args[1];
		this.increment = (int)args[2];
		this.reservePrice = (int)args[3];
		this.objectId = (String)args[4];
		this.waitingPeriod = (int)args[5];
		
		List<Behaviour> behaviours = ((IAuction) args[0]).getBehaviours(Role.Auctioneer, this,  agentsNames);
		for(Behaviour beh: behaviours) {
			this.addBehaviour(beh);
		}
		}
		catch (ArrayIndexOutOfBoundsException e){
			System.out.println("Please check that you have given all the necessary parameters to the auctioneer");
		}
	}
	
	public int getInitPrice(String ObjectId, String protocol) {
		return this.initPrice;
	}
	
	/**
	 * 
	 * @param ObjectId : the object to be auctioned
	 * @param protocol : the auction protocol
	 * @return returns the increment of the auction (or decrement if it's a decreasing auction)
	 */
	public int getIncrement(String ObjectId, String protocol) {
		return this.increment;
	}

	public String getObjectId() {
		return this.objectId;
	}
	
	/**
	 * 
	 * @param ObjectId : the object to be auctioned
	 * @return the reserve price in the case of standard auctions and the budget in the case of the reverse ones
	 */
	public int getReservePrice(String ObjectId) {
		return this.reservePrice;
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

	public int getWaitingPeriod() {
		return waitingPeriod;
	}

	private static boolean isIn(AID agentID,String agentLocalName) {
		List<String> doNotSniff= Arrays.asList("rma","sniffeur","ams","df",agentLocalName);
		return doNotSniff.stream().anyMatch(e -> agentID.getLocalName().contains(e));
	}
	
	public Role getRole() { 
		return Role.Auctioneer;
	}
}


