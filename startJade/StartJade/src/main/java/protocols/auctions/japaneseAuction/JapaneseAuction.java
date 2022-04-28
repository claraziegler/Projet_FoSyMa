package protocols.auctions.japaneseAuction;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import protocols.Role;
import protocols.auctions.Bid;
import protocols.auctions.IAuction;
import protocols.auctions.agents.AuctioneerAgent;
import protocols.auctions.agents.BidderAgent;
import protocols.auctions.dutchAuction.DutchAuction;
import protocols.tools.CreateAgentFromJsonFile;
import protocols.tools.ProtocolFromJson;



/**
 * 
 * @startuml
 * Auctioneer->Bidder1: start(objectId, starting price) [AnnouncePriceAndWinnerBehaviour]
 * Auctioneer->Bidder2: start(objectId, starting price)[AnnouncePriceAndWinnerBehaviour]
 * Bidder1->Auctioneer: in [ReceivePriceBehaviour]
 * Bidder2->Auctioneer: in [ReceivePriceBehaviour]
 * Auctioneer->Bidder1: announce(objectId,price1) [AnnouncePriceAndWinnerBehaviour]
 * Auctioneer->Bidder2: announce(objectId,price1) [AnnouncePriceAndWinnerBehaviour]
 * Bidder1->Auctioneer: in [ReceivePriceBehaviour]
 * Bidder2->Auctioneer: in [ReceivePriceBehaviour]
 * Auctioneer->Bidder1: announce(objectId,price2) [AnnouncePriceAndWinnerBehaviour]
 * Auctioneer->Bidder2: announce(objectId,price2) [AnnouncePriceAndWinnerBehaviour]
 * Bidder2->Auctioneer: in [ReceivePriceBehaviour]
 * Bidder1->Auctioneer: out [ReceivePriceBehaviour]
 * note right of Bidder2 : Bidder2 purchase the object
 * Auctioneer->Bidder2 :Confirm   [AnnouncePriceAndWinnerBehaviour]
 * @enduml
 */

/**Auctioneer announces the starting price,all bidders must say if they are "in" or "out" in the auction.
 * Auctioneer continues to increase  his prices 
 * Each bidder must say if he is still in the auction
 * If the bidder leaves the auction, he cannot re-enter after
 * The auction ends after a fixed time or when a single bidder remains in the auction 
 *
 *@see <a href="https://startjade.gitlab.io/protocol-library/auctions.html">Japanese auction's documentation</a>
 *
 *@author Roza Amokrane
 */
public class JapaneseAuction implements IAuction {
	private boolean reverse = false;

	/**
	 * 
	 * @param reverse  true if Reverse Japanese Auction
	 */
	public JapaneseAuction(boolean reverse) {
		super();
		this.reverse = reverse;
	}

	public static String getProtocolName(boolean reverse) {
		if(reverse) return "ReverseJapaneseAuction";
		else return "JapaneseAuction";
	}
	/**
	 * Return the behaviours needed to perform an auction
	 * @param role   The agent's role in the auction
	 * @param agent   a reference to the agent
	 * @param listagents  The list of agents participating in the auction
	 */ 
	@Override
	public List<Behaviour> getBehaviours(Role role, Agent agent, List<String> listagents) {
		List<Behaviour> lb=new ArrayList<Behaviour>();
		String agentPrint = null;
		switch (role) {
		case Auctioneer :
			String objectId = ((AuctioneerAgent)agent).getObjectId();
			int initPrice = ((AuctioneerAgent) agent).getInitPrice(objectId,getProtocolName(this.reverse));
			int increment = ((AuctioneerAgent) agent).getIncrement(objectId, getProtocolName(this.reverse));
			int reservePrice =   ((AuctioneerAgent) agent).getReservePrice(objectId);
			agentPrint = "-- Auctioneer : "+agent.getLocalName()+" (objectId : "+objectId+", initPrice : "+initPrice;
			if(!this.reverse)
				agentPrint += ",increment : "+increment +")";
			else
				agentPrint += ",decrement : "+increment +")";
			JapaneseObjectAuction japaneseObject= new JapaneseObjectAuction (false);
			lb.add(new AnnouncePriceAndWinnerBehaviour(listagents, agent,((AuctioneerAgent) agent).getObjectId(), ((AuctioneerAgent)agent).getInitPrice("0", "JapaneseAuction"),japaneseObject, this.reverse));
			break;
		case Bidder :
			agentPrint = "-- Bidder :"+agent.getLocalName();
			if(!this.reverse)
				agentPrint += " (budget : "+((BidderAgent) agent).getBudget("objectId", getProtocolName(this.reverse))+")";
			else
				agentPrint += " (reserve price : "+((BidderAgent) agent).getBudget("objectId", getProtocolName(this.reverse))+")";

			lb.add(new ReceivePriceBehaviour(agent, this.reverse));
			break;
		default :
			System.err.println("Observer protocol -- getBehaviours -- unknown role: "+role);
		}
		System.out.println(agentPrint);
		return lb;
	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static List<CreateAgentFromJsonFile> getProperties(ProtocolFromJson pr) {
		List<CreateAgentFromJsonFile> agents = new ArrayList<CreateAgentFromJsonFile>();
		Object[] objtab = null;
		IAuction auction = null;
		Map auctioneerParams = (Map) pr.get("Auctioneer");
		String agentName = (String) auctioneerParams.get("agentName");
		Objects.requireNonNull(agentName);
		Integer initPrice = (Integer) auctioneerParams.get("initPrice");
		Objects.requireNonNull(initPrice);
		Integer increment = null;

		Integer budgetOrReservePrice = 0;
		String objectId = (String)auctioneerParams.get("objectId");
		Objects.requireNonNull(objectId);
		Integer waitingPeriod = (Integer)auctioneerParams.get("waitingTime");
		Objects.requireNonNull(waitingPeriod);
		switch(pr.getProtocolName()) {
		case JapaneseAuction:
			auction = new JapaneseAuction(false);
			increment = (Integer)auctioneerParams.get("increment");
			break;
		case ReverseJapaneseAuction:
			auction = new JapaneseAuction(true);
			increment = (Integer)auctioneerParams.get("decrement");
			break;
		default:
			break;
		}
		Objects.requireNonNull(increment);
		Objects.requireNonNull(budgetOrReservePrice);
		objtab = new Object[]{auction, initPrice, increment, budgetOrReservePrice,objectId,  waitingPeriod};
		agents.add(new CreateAgentFromJsonFile(agentName, AuctioneerAgent.class.getName(), objtab));

		for(Map bidderParams : (List<Map>) pr.get("Bidders") ){
			agentName = (String) bidderParams.get("agentName");
			Objects.requireNonNull(agentName);
			switch(pr.getProtocolName()) {
			case JapaneseAuction:
				budgetOrReservePrice = (Integer)bidderParams.get("budget");
				break;
			case ReverseJapaneseAuction:
				budgetOrReservePrice = (Integer)bidderParams.get("reservePrice");
				break;
			default:
				break;
			}
			increment = 0;
			Objects.requireNonNull(budgetOrReservePrice);
			objtab = new Object[]{auction, budgetOrReservePrice, increment};
			agents.add(new CreateAgentFromJsonFile(agentName, BidderAgent.class.getName(), objtab));

		}
		return agents;

	}



	/**=======================================================
	 * 
	 * Behaviours of the Auctioneer
	 * 
	 * 
	 *=====================================================*/




	/***
	 * 
	 * The Bidder verifies the list of remaining participants
	 * if only one remains , he sends a confirm purchase message to him
	 * if there is still more than one , he announces a higher price  
	 */
	public class AnnouncePriceAndWinnerBehaviour extends SimpleBehaviour{

		private static final long serialVersionUID = 4111562276804955536L;

		private List<String> participants ;
		private String objectId;
		private int currentPrice;
		private JapaneseObjectAuction japaneseObject;
		private boolean finished = false;
		private String protocolName;

		private boolean reverse;
		/**
		 * 
		 * @param participants  the list of agents remaining in the auction 
		 * @param myAgent
		 * @param objectId  the id of the object to be auctioned 
		 * @param startingPrice  the initial price proposed by auctioneer 
		 * @param japaneseObject  Object shared between behaviors 
		 * @param reverse 
		 */
		public AnnouncePriceAndWinnerBehaviour(List<String> participants, Agent myAgent, String objectId, int startingPrice,JapaneseObjectAuction japaneseObject, boolean reverse) {
			super(myAgent);
			this.participants = participants;
			this.objectId = objectId;
			this.currentPrice = startingPrice;
			this.japaneseObject=japaneseObject;
			this.reverse = reverse;
			protocolName = "JapaneseAuction";
			if(reverse)
				protocolName = "ReverseJapaneseAuction";
		}

		@Override
		public void action() {
			if(!this.japaneseObject.isStop()) {
				if(this.participants.size()> 1) {
					final ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
					msg.setSender(this.myAgent.getAID());
					msg.setProtocol(protocolName);
					Bid ProposedBid =new Bid(objectId,this.currentPrice,this.myAgent.getLocalName());
					for(String receiver:participants) {
						msg.addReceiver(new AID(receiver, AID.ISLOCALNAME));  
					}
					try {
						msg.setContentObject(ProposedBid);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					this.myAgent.send(msg);
					System.out.println(this.myAgent.getLocalName()+"("+ ((AuctioneerAgent) this.myAgent).getRole() +") -- Price of " +this.currentPrice);
					int increm = ((AuctioneerAgent) this.myAgent).getIncrement(objectId,protocolName);
					if(reverse)
						this.currentPrice -= increm;
					else
						this.currentPrice += increm;
					this.japaneseObject.setStop(true);
					this.myAgent.addBehaviour(new ReceiveAnswersBehaviour(participants, myAgent,this.japaneseObject, this.objectId, this.reverse));
				}
				else {
					if(this.participants.size() == 1) {
						System.out.println("The winner is "+this.participants.get(0));
						final ACLMessage confirmMsg = new ACLMessage(ACLMessage.CONFIRM);
						confirmMsg.setSender(this.myAgent.getAID());
						confirmMsg.setProtocol(protocolName);
						for(String receiver:participants) {
							confirmMsg.addReceiver(new AID(receiver, AID.ISLOCALNAME));  
						}
						this.myAgent.send(confirmMsg);
					}
					else {
						System.out.println(this.myAgent.getLocalName()+"("+ ((AuctioneerAgent) this.myAgent).getRole() +")They are all out");
					}
					this.finished = true;
				}
			}


		}
		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			return this.finished;
		}

	}
	/**
	 * The auctioneer receives the answer made by the agents, 
	 * he removes from participants list those who are 'OUT'
	 * if he doesn't answer in a fixed period on time he is removed from the list
	 */
	public class ReceiveAnswersBehaviour extends SimpleBehaviour {
		private int nbParticipants;
		private List<String> participants;
		private List<String> hasAnswered = new ArrayList<>();
		private boolean finished = false;
		private long start;
		private JapaneseObjectAuction japaneseObject;
		private String objectId;
		private String protocolName;

		/**
		 * 
		 * @param participants  the list of participating remaining  in the auction 
		 * @param myAgent
		 * @param japaneseObject  object shared between behaviours 
		 */
		public ReceiveAnswersBehaviour( List<String> participants, Agent myAgent, JapaneseObjectAuction japaneseObject, String objectId, boolean reverse) {
			super(myAgent);
			this.nbParticipants = participants.size();
			this.participants = participants;
			this.start =  System.currentTimeMillis();
			this.japaneseObject=japaneseObject;
			this.objectId = objectId;
			protocolName = "JapaneseAuction";
			if(reverse) {
				protocolName = "ReverseJapaneseAuction";
			}

		}


		private static final long serialVersionUID = 114537912248266213L;

		@Override
		public void action() {
			MessageTemplate msgTemplate= MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchProtocol(protocolName), MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL)), MessageTemplate.MatchConversationId(this.objectId));		
			final ACLMessage msgOut = this.myAgent.receive(msgTemplate);
			if (msgOut != null) {
				if(participants.contains(msgOut.getSender().getLocalName())) {
					hasAnswered.add(msgOut.getSender().getLocalName());
					participants.remove(msgOut.getSender().getLocalName());
				}
			}
			MessageTemplate msgInTemplate= MessageTemplate.and(MessageTemplate.MatchProtocol(protocolName), MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL));		
			final ACLMessage msgIn = this.myAgent.receive(msgInTemplate);
			if (msgIn != null) {
				if(participants.contains(msgIn.getSender().getLocalName())) {
					hasAnswered.add(msgIn.getSender().getLocalName());
				}

			}
			if(hasAnswered.size() == nbParticipants) {
				this.finished = true;
				japaneseObject.setStop(false);
			}
			else {
				//If some bidders didn't answer after a fixed period of time 
				if(System.currentTimeMillis() - start > ((AuctioneerAgent) this.myAgent).getWaitingPeriod()) {
					List<String> toRemove = new ArrayList<String>();
					for(String bidder: participants) {
						if(!hasAnswered.contains(bidder))
							toRemove.add(bidder);
					}
					participants.removeAll(toRemove);
					japaneseObject.setStop(false);
					this.finished = true;
				}
			}

		}

		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			return finished;
		}

	}







	/*=======================================================
	 * 
	 * Behaviours of the bidder
	 * 
	 * 
	 *====================================================*/


	/**
	 * The bidder receives the price announcement , then , says whether he is 'IN' or 'OUT'
	 */
	public class ReceivePriceBehaviour extends SimpleBehaviour{
		/**
		 * 
		 */
		private boolean finished = false;
		private boolean reverse;
		private String protocolName;

		public ReceivePriceBehaviour(Agent myAgent, boolean reverse) {
			super(myAgent);
			this.reverse = reverse;
			protocolName = "JapaneseAuction";
			if(reverse) {
				protocolName = "ReverseJapaneseAuction";
			}

		}
		private static final long serialVersionUID = 959816028401037334L;
		@Override
		public void action() {
			MessageTemplate msgConfirmTemplate= MessageTemplate.and(MessageTemplate.MatchProtocol(protocolName), MessageTemplate.MatchPerformative(ACLMessage.CONFIRM));		
			final ACLMessage msgConfirm = this.myAgent.receive(msgConfirmTemplate);
			if (msgConfirm != null) {		

				//System.out.println(this.myAgent.getLocalName() +"("+ ((BidderAgent) this.myAgent).getRole() +") -- The object is for me");
				this.finished = true;
			}
			else {
				MessageTemplate msgTemplate= MessageTemplate.and(MessageTemplate.MatchProtocol(protocolName), MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));		
				final ACLMessage msg = this.myAgent.receive(msgTemplate);
				if (msg != null) {		
					Bid receivedBid=null;
					try {
						receivedBid = (Bid)msg.getContentObject();
					} catch (UnreadableException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}		
					final ACLMessage msgAnswer;
					int budget = ((BidderAgent) this.myAgent).getBudget(receivedBid.getobjectId(), protocolName);
					if((budget >= receivedBid.getCurrentPrice() && !this.reverse) ||(budget <= receivedBid.getCurrentPrice() && this.reverse)){
						msgAnswer = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
						msgAnswer.setSender(this.myAgent.getAID());
						msgAnswer.setProtocol(protocolName);
						msgAnswer.setConversationId(receivedBid.getobjectId());
						System.out.println(this.myAgent.getLocalName() +"("+ ((BidderAgent) this.myAgent).getRole() +") -- Im in");
					}
					else {
						msgAnswer = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
						msgAnswer.setSender(this.myAgent.getAID());
						msgAnswer.setProtocol(protocolName);
						msgAnswer.setConversationId(receivedBid.getobjectId());

						System.out.println(this.myAgent.getLocalName() +"("+ ((BidderAgent) this.myAgent).getRole() +") -- Im out");
					}
					msgAnswer.addReceiver(new AID(msg.getSender().getLocalName(), AID.ISLOCALNAME));  
					this.myAgent.send(msgAnswer);
				}
				else {
					block();
				}
			}

		}
		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			return finished;
		}
	}
}
