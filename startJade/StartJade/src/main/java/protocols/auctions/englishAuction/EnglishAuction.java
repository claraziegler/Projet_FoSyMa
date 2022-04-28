package protocols.auctions.englishAuction;
import java.io.IOException;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import protocols.Role;
import protocols.auctions.Bid;
import protocols.auctions.IAuction;
import protocols.auctions.agents.AuctioneerAgent;
import protocols.auctions.agents.BidderAgent;
import protocols.tools.CreateAgentFromJsonFile;
import protocols.tools.ProtocolFromJson;

/** 
 * @startuml
 * Auctioneer->Bidder1:start(objectId, starting price, minimum increment)[StartEnglichAuctionBehaviour]
 * Auctioneer->Bidder2: start(objectId, starting price, minimum increment)[StartEnglichAuctionBehaviour]
 * Bidder1->Auctioneer: propose(objectId,price1)[MakeOfferBehaviour]
 * note right of Bidder1: price1 >= starting price + minimum increment
 * Auctioneer->Bidder2: proposedBy(objectId,price1,Bidder1) [ManageReceivedBidBehaviou]
 * Bidder2->Auctioneer: propose(objectId,price2)[MakeOfferBehaviour]
 * note right of Auctioneer: wait for a fixed period
 * alt if price2 >= reservePrice 
 * Auctioneer->>Bidder2: accept offer [ManageReceivedBidBehaviour]
 * Auctioneer->Bidder1 :end(Bidder2,price2)[ManageReceivedBidBehaviour]
 * else if price2 < reservePrice
 * Auctioneer->Bidder1: Cancel(objectId)[ManageReceivedBidBehaviour]
 * Auctioneer->Bidder2 :Cancel(objectId)[ManageReceivedBidBehaviour]
 * end
 * @enduml
 */
/**
 * In the case of English Auction :
 * The auctioneer begins by announcing a starting price,each buyer announce his bid , each bid must be superior the
 * previous one with a minimum increment fixed by auctioneer .The auction ends after a fixed time fixed by the auctioneer
 * and the winner is the  last agent who made a bid 
 * 
 * For Reverse English the difference is that sellers and buyers switch their roles , so the auctioneer is a buyer 
 * the bidders ar sellers , and prices decrease instead of increasing.
 *@see <a href="https://startjade.gitlab.io/protocol-library/auctions.html">English auction's documentation</a>
 *@author  Roza Amokrane ,Nabila Ould Belkacem
 */

public class EnglishAuction implements IAuction {

	private boolean reverse = false;
	/**
	 * @param reverse  is true in the case of reverse english auction
	 */
	public EnglishAuction(boolean reverse) {
		super();
		this.reverse = reverse;
	}

	public static String getProtocolName(boolean reverse) {
		if(reverse) return "ReverseEnglishAuction";
		else return "EnglishAuction"; 
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
		Integer increment = null;// it's an increment in the case of english auction and a decrement for the reverse one;
		Integer budgetOrReservePrice = null;// Reserve Price in the case of english auction and a budget for the reverse one;
		String objectId = (String)auctioneerParams.get("objectId");
		Objects.requireNonNull(objectId);
		Integer waitingPeriod = (Integer)auctioneerParams.get("waitingPeriod");
		Objects.requireNonNull(waitingPeriod);

		//used to print the agents properties 
		switch(pr.getProtocolName()) {
		case EnglishAuction:
			auction = new EnglishAuction(false);
			budgetOrReservePrice = (Integer)auctioneerParams.get("reservePrice");
			increment= (Integer)auctioneerParams.get("increment");
			break;
		case ReverseEnglishAuction:
			auction = new EnglishAuction(true);
			budgetOrReservePrice = (Integer)auctioneerParams.get("budget");
			increment = (Integer)auctioneerParams.get("decrement");
			break;
		default:
			break;
		}
		Objects.requireNonNull(increment);
		Objects.requireNonNull(budgetOrReservePrice);
		objtab = new Object[]{auction, initPrice, increment, budgetOrReservePrice,objectId, waitingPeriod};
		agents.add(new CreateAgentFromJsonFile(agentName, AuctioneerAgent.class.getName(), objtab));

		for(Map bidderParams : (List<Map>) pr.get("Bidders") ){
			agentName = (String) bidderParams.get("agentName");
			Objects.requireNonNull(agentName);
			switch(pr.getProtocolName()) {
			case EnglishAuction:
				increment = (Integer)bidderParams.get("personalIncrement");
				Objects.requireNonNull(increment);
				budgetOrReservePrice = (Integer)bidderParams.get("budget");
				break;
			case ReverseEnglishAuction:
				increment = (Integer)bidderParams.get("personalDecrement");
				Objects.requireNonNull(increment);
				budgetOrReservePrice = (Integer)bidderParams.get("reservePrice");
				break;
			default:
				break;
			}
			Objects.requireNonNull(budgetOrReservePrice);
			Objects.requireNonNull(increment);
			objtab = new Object[]{auction, budgetOrReservePrice, increment};
			agents.add(new CreateAgentFromJsonFile(agentName, BidderAgent.class.getName(), objtab));

		}
		return agents;

	}

	/**
	 * Return the behaviours needed to perform an auction
	 * @param role   The agent's role in the auction
	 * @param agent   a reference to the agent
	 * @param listagents  The list of agents participating in the auction
	 */ 
	@Override
	public List<Behaviour> getBehaviours( Role role ,Agent agent ,List<String> listagents ) {
		List<Behaviour> lb=new ArrayList<Behaviour>();
		String agentPrint = null; //used to print agent properties
		switch (role) {
		case Auctioneer :
			String objectId = ((AuctioneerAgent)agent).getObjectId();
			int initPrice = ((AuctioneerAgent) agent).getInitPrice(objectId,getProtocolName(this.reverse));
			int increment = ((AuctioneerAgent) agent).getIncrement(objectId, getProtocolName(this.reverse));
			int reservePrice =   ((AuctioneerAgent) agent).getReservePrice(objectId);
			agentPrint = "-- Auctioneer : "+agent.getLocalName()+" (objectId : "+objectId+", initPrice : "+initPrice;
			if(!this.reverse)
				agentPrint += ",minimal increment : "+increment +", reserve price : "+reservePrice+")";
			else
				agentPrint += ",minimal decrement : "+increment +", budget : "+reservePrice+")";
			lb.add(new StartEnglichAuctionBehaviour(listagents,agent,objectId, this.reverse));
			lb.add(new ManageReceivedBidBehaviour(agent, listagents,objectId, this.reverse, ((AuctioneerAgent)agent).getInitPrice(objectId, getProtocolName(this.reverse))));
			if(this.reverse) {

			}
			break;
		case Bidder :
			agentPrint = "-- Bidder :"+agent.getLocalName();
			if(!this.reverse)
				agentPrint += " (budget : "+((BidderAgent) agent).getBudget("objectId", EnglishAuction.getProtocolName(this.reverse))+", personal increment : "+((BidderAgent) agent).getIncrement("objectId", EnglishAuction.getProtocolName(this.reverse), 0,0)+")";
			else
				agentPrint += " (reserve price : "+((BidderAgent) agent).getBudget("objectId", EnglishAuction.getProtocolName(this.reverse))+", personal decrement : "+((BidderAgent) agent).getIncrement("objectId", EnglishAuction.getProtocolName(this.reverse), 0,0)+")";

			EnglishObjectAuction object =new EnglishObjectAuction ("0",0,"",0);
			lb.add(new ReceiveStart(agent,object, this.reverse));
			lb.add(new ReceiveMessagesBehaviour(agent,object,this.reverse)); //TODO end with a new à is okward
			break;
		default :
			System.err.println("English Auction protocol -- getBehaviours -- unknown role: "+role);
		}
		System.out.println(agentPrint);
		return lb;
	}




	/*******************************************************************
	 * ****************************************************************
	 * 
	 * Behaviours used by an Auctioneer
	 *
	 ********************************************************************
	 ********************************************************************/




	/*************************************************************
	 * 
	 * The auctioneer sends a message to bidders in order to start an english auctions
	 * he sends the object Id , the minimal increment and a starting price
	 *
	 */
	public class StartEnglichAuctionBehaviour extends OneShotBehaviour {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1895215510468551646L;
		private List<String> receivers ;
		private Agent myAgent;
		private String objectId;
		private boolean reverse;

		/**
		 * This behaviour is used by the seller. It needs to know :
		 * @param receivers The agents that will be participating in the auction
		 * @param myAgent
		 * @param objectId   the id of the object to be auctioned
		 * @param reverse 
		 */
		public StartEnglichAuctionBehaviour(List<String> receivers, Agent myAgent, String objectId, boolean reverse) {
			super();
			this.receivers = receivers;
			this.myAgent = myAgent; 
			this.objectId = objectId;
			this.reverse = reverse;
		}
		@Override
		public void action() {
			//1°Create the message
			final ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			msg.setSender(this.myAgent.getAID());
			String protocolName = "EnglishAuction";
			if(reverse) {
				protocolName = "ReverseEnglishAuction";
			}
			msg.setProtocol(protocolName);

			Bid initBid= new Bid(objectId,((AuctioneerAgent)this.myAgent).getInitPrice(objectId, protocolName) ,this.myAgent.getLocalName(), ((AuctioneerAgent) this.myAgent).getIncrement(objectId, protocolName), this.reverse);
			for(String receiver:receivers)
				msg.addReceiver(new AID(receiver, AID.ISLOCALNAME));  						

			try {
				msg.setContentObject(initBid);
			} catch (IOException e) {
				// TODO Auto-generated catch bloc
				e.printStackTrace();
			}
			this.myAgent.send(msg);
			System.out.println(this.myAgent.getLocalName()+"("+ ((AuctioneerAgent) this.myAgent).getRole() +") -- I started the auction");
			//System.out.println(initBid);
		}
	}

	/**
	 *The auctioneer receives bids and informs bidders about the higher bid received 
	 *If the auctioneer doesn't receive price proposals for a defined period of time 
	 *he sends an accept proposal to the winner , and informs other participants that the auction is closed 
	 */
	public class ManageReceivedBidBehaviour extends SimpleBehaviour {

		private static final long serialVersionUID = 9088209402507795289L;
		private boolean finished=false;
		private int currentPrice;
		private List<String> listAgent;
		private long start = System.currentTimeMillis();
		private String winner = null;
		private String objectId;
		private boolean reverse;

		/**
		 * @param myagent  a reference to the agent
		 * @param listAgent  The list of agents participating in the auction
		 * @param objectId  the object to be auctioned
		 * @param reverse  is true in the case of Reverse English Auction
		 */
		public ManageReceivedBidBehaviour (final Agent myagent,List<String> listAgent ,String objectId, boolean reverse, int initPrice ) {
			super(myagent);
			this.listAgent=listAgent;
			this.objectId=  objectId;
			this.reverse = reverse;
			this.currentPrice = initPrice;

		}
		@Override
		public void action() {
			MessageTemplate msgProtocol = MessageTemplate.MatchProtocol(EnglishAuction.getProtocolName(this.reverse));
			MessageTemplate msgProposeTemplate= MessageTemplate.and(MessageTemplate.and(msgProtocol, MessageTemplate.MatchPerformative(ACLMessage.PROPOSE)), MessageTemplate.MatchConversationId(objectId));	
			ACLMessage msgPropose = this.myAgent.receive(msgProposeTemplate); 
			if (msgPropose != null) {
				start = System.currentTimeMillis();
				Bid receivedBid=null;
				try {
					receivedBid=(Bid)msgPropose.getContentObject();
				} catch (UnreadableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if ( this.currentPrice<receivedBid.getCurrentPrice()&&! this.reverse || this.currentPrice>receivedBid.getCurrentPrice()&&this.reverse ){
					//we could replace these variable by the currentAcceptedBid variable
					this.currentPrice=receivedBid.getCurrentPrice();
					this.winner = receivedBid.getAuctioneer();
					//1°Create the message
					ACLMessage msgInform = new ACLMessage(ACLMessage.INFORM);
					msgInform.setSender(this.myAgent.getAID());
					msgInform.setProtocol(EnglishAuction.getProtocolName(this.reverse));
					msgInform.setConversationId(this.objectId);
					for (String receiverName : this.listAgent)
						msgInform.addReceiver(new AID(receiverName, AID.ISLOCALNAME)); 
					Bid currentAcceptedBid=new Bid(receivedBid.getobjectId(), currentPrice, winner);
					try {
						msgInform.setContentObject(currentAcceptedBid);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println(this.myAgent.getLocalName()+"("+ ((AuctioneerAgent) this.myAgent).getRole() +") -- Actually the winner is "+ currentAcceptedBid.getAuctioneer() +" for a price of "+currentAcceptedBid.getCurrentPrice());
					this.myAgent.send(msgInform);
				}

			}else{

				if(System.currentTimeMillis() - start> ((AuctioneerAgent) this.myAgent).getWaitingPeriod()) {
					//if no higher bid is received under the defined time-intervall
					final ACLMessage msgCancel = new ACLMessage(ACLMessage.CANCEL);
					msgCancel.setSender(this.myAgent.getAID());
					if(!reverse) {
						msgCancel.setProtocol("EnglishAuction");
					}
					else {
						msgCancel.setProtocol("ReverseEnglishAuction");
					}
					msgCancel.setConversationId(this.objectId);
					if(winner != null) {
						if( (this.currentPrice >=((AuctioneerAgent) this.myAgent).getReservePrice(this.objectId) && !this.reverse) || (this.currentPrice <=((AuctioneerAgent) this.myAgent).getReservePrice(this.objectId)) && this.reverse){
							final ACLMessage msgWin= new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
							msgWin.setSender(this.myAgent.getAID());
							if(!reverse) {
								msgWin.setProtocol("EnglishAuction");
							}
							else {
								msgWin.setProtocol("ReverseEnglishAuction");
							}
							msgWin.setConversationId(this.objectId);

							try {
								msgWin.setContentObject(this.currentPrice);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							System.out.println(this.myAgent.getLocalName()+"("+((AuctioneerAgent) this.myAgent).getRole()+") -- The wainting period has expired, I didn't receive a new offer.\n The winner of the auction is: "+ winner + " the final price is "+ currentPrice);
							msgWin.addReceiver(new AID(winner, AID.ISLOCALNAME));
							listAgent.remove(winner);
							this.myAgent.send(msgWin);
						}

						// the price proposed is lower than the auctioneer reserve price 
						else {			
							if(!reverse)
								System.out.println(this.myAgent.getLocalName() +"("+((AuctioneerAgent) this.myAgent).getRole()+") -- the price is lower than my ReservePrice");
							else
								System.out.println(this.myAgent.getLocalName() +"("+((AuctioneerAgent) this.myAgent).getRole()+") -- the price is higher than my budget");

							winner = null;
						}

					}
					else {
						// No proposal was received from the starting of the auction
						System.out.println(this.myAgent.getLocalName() +"("+((AuctioneerAgent) this.myAgent).getRole()+")Nobody wants this object");
					}

					//Send cancel message
					for (String receiverName : this.listAgent)
						msgCancel.addReceiver(new AID(receiverName, AID.ISLOCALNAME)); 

					Bid lastAcceptedBid= new Bid(this.objectId,this.currentPrice,winner);

					try {
						msgCancel.setContentObject(lastAcceptedBid);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					this.myAgent.send(msgCancel);
					this.finished = true;
				}
				else {
					block(((AuctioneerAgent) this.myAgent).getWaitingPeriod());
				}
			}
		}
		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			return finished ;
		}
	}





	/*******************************************************************
	 * ****************************************************************
	 * 
	 * Behaviours used by Bidders
	 *
	 ********************************************************************
	 ********************************************************************/



	/**The Bidder receives the start message sent by the auctioneer and starts MakeOfferBehaviour**/
	public class ReceiveStart extends SimpleBehaviour {
		private static final long serialVersionUID = 9088209402507795289L;
		private boolean finished=false;
		private EnglishObjectAuction objectAuction;
		private boolean reverse;
		/**
		 * @param myagent  the reference to the agent
		 * @param objectAuction Object shared between behaviors 
		 * @param reverse  is true in the case of the Reverse English Auction
		 */
		public ReceiveStart ( Agent myagent, EnglishObjectAuction objectAuction, boolean reverse) {
			super(myagent);
			this.objectAuction=objectAuction;
			this.reverse = reverse;
		}
		@Override
		public void action() {
			//1) receive the message
			MessageTemplate msgProtocol = MessageTemplate.MatchProtocol(EnglishAuction.getProtocolName(this.reverse));
			MessageTemplate msgTemplate= MessageTemplate.and(msgProtocol, MessageTemplate.MatchPerformative(ACLMessage.REQUEST));		
			final ACLMessage msg = this.myAgent.receive(msgTemplate);
			if (msg != null) {		
				Bid initBid=null;
				try {
					initBid=(Bid)msg.getContentObject();
				} catch (UnreadableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String Idobject=initBid.getobjectId();
				int startingPrice=initBid.getCurrentPrice();
				int minimalIncrement=initBid.getminIncrement();
				this.objectAuction.setObjectId(Idobject);
				this.objectAuction.setCurrentPrice(startingPrice);
				this.objectAuction.setMinIncrement(minimalIncrement);
				this.objectAuction.setAuctioneer( msg.getSender().getLocalName());
				this.myAgent.addBehaviour(new MakeOfferBehaviour(this.myAgent,objectAuction, this.reverse));
				this.finished=true;
			}else{
				block();// the behaviour goes to sleep until the arrival of a new message in the agent's Inbox.
			}
		}
		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			return finished ;
		}
	}



	/**
	 *A bidder's behaviour that allows him to bid , he sends his price proposals to the auctioneer
	 *
	 */
	public class MakeOfferBehaviour extends SimpleBehaviour {
		private static final long serialVersionUID = 5702027812919298552L;
		private Agent myAgent;
		private boolean finished;
		private EnglishObjectAuction objectAuction;
		private boolean reverse;
		String protocolName;

		/**
		 * @param myAgent  a reference to the agent
		 * @param objectAuction  Object shared between behaviors 
		 * @param reverse  true if it's a reverse auction
		 */
		public MakeOfferBehaviour(Agent myAgent,EnglishObjectAuction objectAuction, boolean reverse) {
			this.myAgent=myAgent;
			this.objectAuction=objectAuction;
			this.reverse = reverse;
			protocolName = EnglishAuction.getProtocolName(this.reverse);
		}
		@Override
		public void action() {
			try {
				this.myAgent.doWait(250);
			} catch (Exception e) {
				e.printStackTrace();
			}
			int value;
			if(!reverse)
				value = this.objectAuction.getCurrentPrice()  + ((BidderAgent) this.myAgent).getIncrement(this.objectAuction.getObjectId(),protocolName, this.objectAuction.getCurrentPrice() , this.objectAuction.getMinIncrement());
			else
				value = this.objectAuction.getCurrentPrice()  - ((BidderAgent) this.myAgent).getIncrement(this.objectAuction.getObjectId(),protocolName, this.objectAuction.getCurrentPrice() , this.objectAuction.getMinIncrement());

			int budget = ((BidderAgent) this.myAgent).getBudget(this.objectAuction.getObjectId(),protocolName);
			int currentPrice = this.objectAuction.getCurrentPrice();
			
			// if the difference between the current price and the budget/reserve price is lower than the personal increment/decrement
			// and higher than the minimal increment , the agent bid with his budget/ reserve price
			if(value < budget && reverse &&  currentPrice - budget >= this.objectAuction.getMinIncrement() &&  currentPrice - budget > 0)
				value = budget;
			if(value > budget && !reverse && budget - currentPrice > 0 && budget - currentPrice >= this.objectAuction.getMinIncrement())
				value = budget;
			
			//if the value after adding the increment is within the budget of the agent (or within his reserve price in the case of reverse auction)
			if(( (value >= budget  && value< currentPrice && this.reverse ) || (value <= budget  && value> currentPrice && !this.reverse )) && !objectAuction.isStop()) {
				//1°Create the message
				final ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
				msg.setSender(this.myAgent.getAID());
				msg.setProtocol(protocolName);
				msg.setConversationId(this.objectAuction.getObjectId());


				Bid myBid= new Bid(this.objectAuction.getObjectId(), value, this.myAgent.getLocalName());
				try {
					msg.setContentObject(myBid);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				msg.addReceiver(new AID(this.objectAuction.getAuctioneer(), AID.ISLOCALNAME));  

				this.myAgent.send(msg);
				this.objectAuction.setStop(true);
				System.out.println(this.myAgent.getLocalName()+"("+ ((BidderAgent) this.myAgent).getRole() +") -- I made an offer of " + value);
				this.objectAuction.setCurrentPrice(value);
			}
			else {
				if(this.objectAuction.isStop()) {
					//System.out.println(this.myAgent.getLocalName()+" ----> I have the object " );
				}
				else {
					if(!this.reverse)
						System.out.println(this.myAgent.getLocalName()+"("+ ((BidderAgent) this.myAgent).getRole() +") -- I'm out cause my budget is reached");
					else
						System.out.println(this.myAgent.getLocalName()+"("+ ((BidderAgent) this.myAgent).getRole() +") -- I'm out cause my reserve price is reached");

					this.finished = true;
				}
			}
			if (this.objectAuction.getFinished()==true)
				this.finished=true;
		}

		public void setfinished(boolean finished ) {
			this.finished=true;
		}
		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			return this.finished;
		}
	}



	/**
	 *used by buyers to track the auction , allows them to receive the last bid and the name of the agent who made it.
	 */
	public class ReceiveMessagesBehaviour extends SimpleBehaviour {

		private static final long serialVersionUID = 9088209402507795289L;
		private boolean finished=false;
		private EnglishObjectAuction objectAuction;
		private boolean reverse;
		/**
		 * 
		 * @param myagent the reference to the agent
		 * @param objectAuction  Object shared between behaviors 
		 * @param reverse  indicates whether it's a reverse auction or not
		 */

		public ReceiveMessagesBehaviour  (final Agent myagent,EnglishObjectAuction objectAuction, boolean reverse) {
			super(myagent);
			this.objectAuction=objectAuction;
			this.reverse = reverse;
		}
		@Override
		public void action() {
			MessageTemplate msgProtocol = MessageTemplate.MatchProtocol(EnglishAuction.getProtocolName(this.reverse));

			//1) receive INFORM Message
			MessageTemplate msgInformTemplate=  MessageTemplate.and(msgProtocol, MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),MessageTemplate.MatchConversationId(this.objectAuction.getObjectId())));
			final ACLMessage msgInform = this.myAgent.receive(msgInformTemplate);
			//2) receive Cancel message
			MessageTemplate msgCancelTemplate= MessageTemplate.and(msgProtocol, MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.CANCEL),MessageTemplate.MatchConversationId(this.objectAuction.getObjectId())));	


			final ACLMessage msgCancel = this.myAgent.receive(msgCancelTemplate);
			// 3)receive Accept Proposal message
			MessageTemplate msgAccceptTemplate= MessageTemplate.and(msgProtocol, MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),MessageTemplate.MatchConversationId(this.objectAuction.getObjectId())));
			final ACLMessage msgAcccept = this.myAgent.receive(msgAccceptTemplate);
			if (msgInform != null) {		
				Bid receivedBidInformation=null;
				try {
					receivedBidInformation = (Bid) msgInform.getContentObject();

				}catch (UnreadableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Objects.requireNonNull(receivedBidInformation);
				if ( (this.objectAuction.getCurrentPrice()<= receivedBidInformation.getCurrentPrice() && !this.reverse) || (this.objectAuction.getCurrentPrice()>= receivedBidInformation.getCurrentPrice()&&this.reverse))
				{
					this.objectAuction.setCurrentPrice(receivedBidInformation.getCurrentPrice());
					if(!(receivedBidInformation.getAuctioneer()).equals(this.myAgent.getLocalName()))
						this.objectAuction.setStop(false);
				}
			}else {
				if (msgCancel != null) {		
					int price =0;
					Bid receivedBid=null;
					try {
						receivedBid=(Bid)msgCancel.getContentObject();
					} catch (UnreadableException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Objects.nonNull(receivedBid);
					String winner =receivedBid.getAuctioneer();
					price  =receivedBid.getCurrentPrice();
					if(winner != null)	{
						//System.out.println(this.myAgent.getLocalName()+"("+ ((BidderAgent) this.myAgent).getRole() +") -- "+winner +" won the auction for a final price of "+price);
					}
					this.finished=true;
					objectAuction.setFinished(true);
				}
				else {
					if (msgAcccept != null) {
						int price=0;
						try {
							price= (int) msgAcccept.getContentObject();
						} catch (UnreadableException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						//System.out.println(this.myAgent.getLocalName()+" -- I won the auction for a final price of "+price);
						this.finished=true;
						objectAuction.setFinished(true);
					}
					else 
						block();
				}
			}
		}

		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			return finished ;
		}

	}
}
