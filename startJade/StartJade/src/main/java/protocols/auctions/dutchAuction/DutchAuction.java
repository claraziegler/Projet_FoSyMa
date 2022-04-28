package protocols.auctions.dutchAuction;
import java.io.IOException;

import java.io.Serializable;
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
 * 
 * @startuml
 * Auctioneer->Bidder1: start(objectId,starting price)[StartDutchAuctionBehaviour]
 * Auctioneer->Bidder2: start(objectId,starting price)[StartDutchAuctionBehaviour]
 * alt if price1 >= reservePrice
 * Auctioneer->Bidder1: announce(objectId,price1)[AnnounceBehaviour]
 * Auctioneer->Bidder2: announce(objectId,price1)[AnnounceBehaviour]
 * end
 * alt if price2 >= reservePrice
 * Auctioneer->Bidder1: announce(objectId,price2)[AnnounceBehaviour]
 * Auctioneer->Bidder2: announce(objectId,price2)[AnnounceBehaviour]
 * end
 * Bidder2->Auctioneer: YES [SendYesBehaviour]
 * note right of Bidder2 : Bidder2 purchase the object
 * Auctioneer->Bidder2 :Confirm(objectId,price2) [AnnounceBehaviour]
 * Auctioneer->Bidder1 :end(objectId,Bidder2,price2)[AnnounceBehaviour]
 * @enduml
 */
/**
 * The auctioneer start the auction by announcing high price , and continue to announce lower price successively,,
 * when buyer accept the price , he signals it to the auctioneer and the auction ends .
 * @see <a href="https://startjade.gitlab.io/protocol-library/auctions.html">Dutch auction's documentation</a>
 * @author Nabila Ould Belkacem 
 * */

public class DutchAuction implements IAuction {
	/**
	 * 
	 * @param reverse true if Reverse Ducth Auction
	 */
	public DutchAuction(boolean reverse) {
		super();
		this.reverse = reverse;
	}
	/**
	 * 
	 *true if Reverse Ducth Auction
	 */
	private boolean reverse = false;
	/**
	 * Return the behaviours needed to perform an auction
	 * @param role   The agent's role in the auction
	 * @param agent   a reference to the agent
	 * @param listagents  The list of agents participating in the auction
	 */ 
	@Override
	public List<Behaviour> getBehaviours(Role role,Agent agent ,List<String> listagents ) {
		List<Behaviour> lb=new ArrayList<Behaviour>();
		String agentPrint = null;
		switch (role) {
		case  Auctioneer:
			String objectId = ((AuctioneerAgent)agent).getObjectId();
			int initPrice = ((AuctioneerAgent) agent).getInitPrice(objectId,getProtocolName(this.reverse));
			int increment = ((AuctioneerAgent) agent).getIncrement(objectId, getProtocolName(this.reverse));
			int reservePrice =   ((AuctioneerAgent) agent).getReservePrice(objectId);
			agentPrint = "-- Auctioneer : "+agent.getLocalName()+" (objectId : "+objectId+", initPrice : "+initPrice;
			if(!this.reverse)
				agentPrint += ",decrement : "+increment +", reserve price : "+reservePrice+")";
			else
				agentPrint += ",increment : "+increment +", budget : "+reservePrice+")";
			lb.add(new StartDutchAuctionBehaviour(listagents,agent,((AuctioneerAgent)agent).getObjectId(), this.reverse));
			break;
		case Bidder :
			agentPrint = "-- Bidder :"+agent.getLocalName();
			if(!this.reverse)
				agentPrint += " (budget : "+((BidderAgent) agent).getBudget("objectId", DutchAuction.getProtocolName(this.reverse))+")";
			else
				agentPrint += " (reserve price : "+((BidderAgent) agent).getBudget("objectId", DutchAuction.getProtocolName(this.reverse))+")";

			lb.add(new ReceiveStartBehaviour(agent, this.reverse));
			break;
		default :
			System.err.println("DutchAuction -- getBehaviours -- unknown role: "+role);
		}
		System.out.println(agentPrint);
		return lb;
	}
	/***
	 * 
	 * @param reverse  is true in case of reverse dutch auction
	 * @return the name of the protocol
	 */
	private static String getProtocolName(boolean reverse) {
		if(reverse)
			return "ReverseDutchAuction";
		else
			return "DutchAuction";
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
		Integer budgetOrReservePrice = null;
		String objectId = (String)auctioneerParams.get("objectId");
		Objects.requireNonNull(objectId);
		Integer waitingPeriod = (Integer)auctioneerParams.get("waitingPeriod");
		Objects.requireNonNull(waitingPeriod);
		switch(pr.getProtocolName()) {
		case DutchAuction:
			auction = new DutchAuction(false);
			budgetOrReservePrice = (Integer)auctioneerParams.get("reservePrice");
			increment = (Integer)auctioneerParams.get("decrement");
			break;
		case ReverseDutchAuction:
			auction = new DutchAuction(true);
			budgetOrReservePrice = (Integer)auctioneerParams.get("budget");
			increment = (Integer)auctioneerParams.get("increment");
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
			increment = 0;
			switch(pr.getProtocolName()) {
			case DutchAuction:
				budgetOrReservePrice = (Integer)bidderParams.get("budget");
				break;
			case ReverseDutchAuction:
				budgetOrReservePrice = (Integer)bidderParams.get("reservePrice");
				break;
			default:
				break;
			}
			Objects.requireNonNull(budgetOrReservePrice);
			objtab = new Object[]{auction, budgetOrReservePrice, increment};
			agents.add(new CreateAgentFromJsonFile(agentName, BidderAgent.class.getName(), objtab));
		}
		return agents;

	}

	/*******************************************
	 * 
	 * 
	 * Auctioneer's Behaviours
	 * 
	 */

	/** 
	 * The behaviour used by the seller to launch an auction
	 */
	public class StartDutchAuctionBehaviour extends SimpleBehaviour {
		private static final long serialVersionUID = -1895215510468551646L;
		private List<String> receivers;
		private Agent myAgent;
		private String objectId;
		private boolean finished=false;
		private boolean reverse;

		/**
		 * @param receivers  the list of bidders participating in the auction 
		 * @param myAgent
		 * @param objectId   the id of the object to be auctioned
		 * @param reverse 
		 */
		public StartDutchAuctionBehaviour (List<String> receivers, Agent myAgent, String objectId, boolean reverse) {
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
			msg.setProtocol(getProtocolName(this.reverse));
			Bid initBid =new Bid(this.objectId,((AuctioneerAgent) this.myAgent).getInitPrice(this.objectId,getProtocolName(this.reverse)),this.myAgent.getLocalName(),0, this.reverse);
			for(String receiver:receivers)
				msg.addReceiver(new AID(receiver, AID.ISLOCALNAME));  
			try {
				msg.setContentObject(initBid);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.myAgent.send(msg);
			System.out.println(this.myAgent.getLocalName()+"("+ ((AuctioneerAgent) this.myAgent).getRole() +") -- I started the auction");
			this.myAgent.addBehaviour(new AnnounceBehaviour(objectId,myAgent, this.receivers,((AuctioneerAgent) this.myAgent).getInitPrice(this.objectId,getProtocolName(this.reverse)), this.reverse));
			this.finished=true;		
		}
		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			return finished;
		}
	}


	/**
	 * Used by the auctioneer to announce prices,confirm to the winner and cancel the auction 
	 */

	public class AnnounceBehaviour extends SimpleBehaviour {
		private static final long serialVersionUID = 9088209402507795289L;
		private boolean finished=false;
		private int currentPrice;
		private String objectId;
		private List<String> listAgent;
		private String winner;
		private boolean reverse;

		/**
		 * 
		 * @param objectId  the id of the object to be auctionned
		 * @param myagent 
		 * @param listAgent  the list of agents participating in the auction 
		 * @param currentPrice  The current price reached on the auction
		 * @param reverse  True if it's a reverse auction
		 */

		public AnnounceBehaviour (String objectId,final Agent myagent,List<String> listAgent,int currentPrice, boolean reverse) {
			super(myagent);
			this.objectId=objectId;
			this.listAgent=listAgent;
			this.currentPrice=currentPrice;
			this.reverse  = reverse;

		}
		public void action() {
			try {
				this.myAgent.doWait(((AuctioneerAgent) this.myAgent).getWaitingPeriod());
			} catch (Exception e) {
				e.printStackTrace();
			}
			// receive YES from a bidder
			MessageTemplate msgAcceptTemplate=MessageTemplate.and(MessageTemplate.MatchConversationId(objectId), MessageTemplate.and(MessageTemplate.MatchProtocol(getProtocolName(this.reverse)), MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL)));		
			final ACLMessage msgAccept = this.myAgent.receive(msgAcceptTemplate);
			if (msgAccept != null) {	
				Bid AcceptedBid=null;
				try {
					AcceptedBid=(Bid)msgAccept.getContentObject();
				} catch (UnreadableException e) {
					e.printStackTrace();
				}
				this.winner=msgAccept.getSender().getLocalName();
				if ((this.currentPrice< AcceptedBid.getCurrentPrice()&&!this.reverse) || (this.currentPrice> AcceptedBid.getCurrentPrice()&&this.reverse)){
					this.currentPrice=AcceptedBid.getCurrentPrice();
				}
				//Send confirm purchase to the winner
				final ACLMessage msgConfirm = new ACLMessage(ACLMessage.CONFIRM);
				msgConfirm.setSender(this.myAgent.getAID());
				msgConfirm.setProtocol(getProtocolName(this.reverse));
				msgConfirm.setConversationId(objectId);
				msgConfirm.addReceiver(new AID(winner, AID.ISLOCALNAME));  
				Bid ConfirmedBid =new Bid(this.objectId,this.currentPrice,this.myAgent.getLocalName());
				try {
					msgConfirm.setContentObject(ConfirmedBid);
				}catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				this.myAgent.send(msgConfirm);
				listAgent.remove(winner);
				//send cancel to other agents
				final ACLMessage msgCancel = new ACLMessage(ACLMessage.CANCEL);
				msgCancel.setSender(this.myAgent.getAID());
				System.out.println("The winner of the auction is"+ winner + " final price is "+ this.currentPrice);				
				Bid CancelBid =new Bid(this.objectId,currentPrice, winner);
				for(String receiver:this.listAgent)
					msgCancel.addReceiver(new AID(receiver, AID.ISLOCALNAME));  
				try {
					msgCancel.setContentObject(CancelBid);
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				this.myAgent.send(msgCancel);
				this.finished=true;
			}

			// if the auctioneer didn't receive any Accept proposol message
			if(!finished) {
				int value = this.currentPrice;	
				if ((!this.reverse && this.currentPrice>=((AuctioneerAgent)this.myAgent).getReservePrice(objectId))||(this.reverse && this.currentPrice<=((AuctioneerAgent)this.myAgent).getReservePrice(objectId))) {
					//1°Create the message Announce 
					final ACLMessage msgPropose = new ACLMessage(ACLMessage.PROPOSE);
					msgPropose.setSender(this.myAgent.getAID());
					msgPropose.setProtocol(getProtocolName(this.reverse));
					msgPropose.setConversationId(objectId);
					Bid myBid= new Bid(this.objectId, value, this.myAgent.getLocalName());
					for(String receiver:this.listAgent)
						msgPropose.addReceiver(new AID(receiver, AID.ISLOCALNAME));  
					try {
						msgPropose.setContentObject((Serializable) myBid);
					}catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					this.myAgent.send(msgPropose);
					System.out.println(this.myAgent.getLocalName()+"("+ ((AuctioneerAgent) this.myAgent).getRole() +") -- Price : " + value);
					if(!reverse)
						this.currentPrice  -= ((AuctioneerAgent) this.myAgent).getIncrement(objectId,getProtocolName(this.reverse));		
					else
						this.currentPrice  += ((AuctioneerAgent) this.myAgent).getIncrement(objectId,getProtocolName(this.reverse));		

				} else{
					System.out.println(this.myAgent.getLocalName()+"("+ ((AuctioneerAgent) this.myAgent).getRole() +") -- I cancel the sale");
					final ACLMessage cancelMsg = new ACLMessage(ACLMessage.CANCEL);
					Bid CanceledBid =null;
					cancelMsg.setSender(this.myAgent.getAID());
					cancelMsg.setProtocol(getProtocolName(this.reverse));
					cancelMsg.setConversationId(objectId);
					try {
						cancelMsg.setContentObject((Serializable) CanceledBid);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					for(String receiver:this.listAgent)
						cancelMsg.addReceiver(new AID(receiver, AID.ISLOCALNAME));  
					this.myAgent.send(cancelMsg);
					this.finished = true; 
				}
			}	
		}
		public boolean done() {
			// TODO Auto-generated method stub
			return finished;
		}

	}



	/**=======================================
	 * 
	 * Bidder's Behaviours
	 * 
	 **=======================================
	 */


	/**
	 * Allows to a bidder to receive start auction message and join the auction
	 */
	public class ReceiveStartBehaviour extends SimpleBehaviour {
		private static final long serialVersionUID = 9088209402507795289L;
		private boolean finished=false;
		private boolean reverse;
		/**
		 * 
		 * @param myagent
		 * @param reverse 
		 */
		public ReceiveStartBehaviour (Agent myagent, boolean reverse) {
			super(myagent);
			this.reverse = reverse;
		}
		@Override
		public void action() {
			//1) receive the message
			MessageTemplate msgRequestTemplate= MessageTemplate.and(MessageTemplate.MatchProtocol(getProtocolName(this.reverse)), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));		
			final ACLMessage msgRequest = this.myAgent.receive(msgRequestTemplate);
			if (msgRequest != null) {		
				Bid initBid=null;
				try {
					initBid=(Bid)msgRequest.getContentObject();
				} catch (UnreadableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
				//System.out.println(initBid);
				String idObject=initBid.getobjectId();
				int startingPrice=initBid.getCurrentPrice();
				if ((!this.reverse && startingPrice <= ((BidderAgent)this.myAgent).getBudget(idObject,getProtocolName(this.reverse)))||(this.reverse && startingPrice >= ((BidderAgent)this.myAgent).getBudget(idObject,getProtocolName(this.reverse))))
				{
					this.myAgent.addBehaviour(new SendYesBehaviour(idObject,this.myAgent,startingPrice,msgRequest.getSender().getLocalName(), this.reverse));
				}
				this.myAgent.addBehaviour(new ReceiveMessagesBehaviour(this.myAgent, idObject, this.reverse));
				this.finished=true;
			}else{
				block();// the behaviour goes to sleep until the arrival of a new message in the agent's Inbox.
			}
		}
		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			return finished;
		}
	}

	/**
	 * Used by bidders to receive the price announcement , the message confirm and cancel  made by the seller 
	 */
	public class ReceiveMessagesBehaviour extends SimpleBehaviour {
		private static final long serialVersionUID = 5541600887638359547L;
		private String idObject;
		private boolean finished=false;
		private boolean reverse;

		public  ReceiveMessagesBehaviour (final Agent myAgent, String idObject, boolean reverse) {
			super(myAgent);
			this.idObject = idObject;
			this.reverse = reverse;

		}
		public void action() {
			//1) receive price proposal from the auctioneer 
			MessageTemplate msgProposeTemplate= MessageTemplate.and(MessageTemplate.and( MessageTemplate.MatchProtocol(getProtocolName(this.reverse)), MessageTemplate.MatchPerformative(ACLMessage.PROPOSE)), MessageTemplate.MatchConversationId(idObject));		
			final ACLMessage msgPropose = this.myAgent.receive(msgProposeTemplate);
			if (msgPropose != null) {		
				Bid ReceivedBid=null; 
				try {
					ReceivedBid =(Bid)msgPropose.getContentObject();
				} catch (UnreadableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
				if ((!this.reverse && ReceivedBid.getCurrentPrice() <=((BidderAgent) this.myAgent).getBudget(this.idObject, getProtocolName(this.reverse)))
						||(this.reverse && ReceivedBid.getCurrentPrice() >=((BidderAgent) this.myAgent).getBudget(this.idObject, getProtocolName(this.reverse))))
				{

					this.myAgent.addBehaviour(new SendYesBehaviour(idObject,this.myAgent,ReceivedBid.getCurrentPrice(),msgPropose.getSender().getLocalName(), this.reverse));
					this.finished=true;
				}

			}
			else {
				//1) receive cancel Message
				MessageTemplate msgCancelTemplate= MessageTemplate.and(MessageTemplate.and( MessageTemplate.MatchProtocol(getProtocolName(this.reverse)), MessageTemplate.MatchPerformative(ACLMessage.CANCEL)), MessageTemplate.MatchConversationId(idObject));				
				final ACLMessage msgCancel = this.myAgent.receive(msgCancelTemplate);
				if (msgCancel != null) {		
					this.finished=true;
					Bid CanceledBid =null;
					try {
						CanceledBid = (Bid) msgCancel.getContentObject();
					} catch (UnreadableException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(CanceledBid != null) {
						//System.out.println(this.myAgent.getLocalName() +" -- The winner is "+CanceledBid.getAuctioneer());
					}
					else {
						//System.out.println(this.myAgent.getLocalName()+" --  auction canceled");
					}
				}
				else {
					//receive Confirm purchase
					MessageTemplate msgConfirmTemplate= MessageTemplate.and(MessageTemplate.and( MessageTemplate.MatchProtocol(getProtocolName(this.reverse)), MessageTemplate.MatchPerformative(ACLMessage.CONFIRM)), MessageTemplate.MatchConversationId(idObject));				
					final ACLMessage msgConfirm = this.myAgent.receive(msgConfirmTemplate);
					if (msgConfirm != null) {
						Bid ConfirmedBid =null;
						try {
							ConfirmedBid = (Bid) msgConfirm.getContentObject();
						} catch (UnreadableException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						//System.out.println(this.myAgent.getLocalName()+"("+ ((BidderAgent) this.myAgent).getRole() +") --  I won the auction for a price of "+ConfirmedBid.getCurrentPrice());

						this.finished=true;
					}
					else {
						block();
					}
				}
			}

		}

		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			return finished;
		}

	}

	/**
	 *A OneShotBehaviour that allows to a bidder to accept a price proposition
	 */
	public class SendYesBehaviour extends OneShotBehaviour {
		private static final long serialVersionUID = 9088209402507795289L;
		private int CurrentPrice;
		private String receiver;
		private String idObject;
		private boolean reverse;
		/**
		 * 
		 * @param idObject  the id of the object to be auctioned 
		 * @param myagent
		 * @param CurrentPrice  the price accepted by bidder 
		 * @param receiver  the name of the proposer (auctioneer)
		 * @param reverse 
		 */
		public SendYesBehaviour (String idObject,final Agent myagent,int CurrentPrice,String  receiver, boolean reverse) {
			super(myagent);
			this.CurrentPrice= CurrentPrice;
			this.receiver= receiver;
			this.idObject=idObject;
			this.reverse = reverse;
		}
		public void action() {
			//1°Create the message
			final ACLMessage msg = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
			msg.setSender(this.myAgent.getAID());
			msg.setProtocol(getProtocolName(this.reverse));
			msg.setConversationId(idObject);
			msg.addReceiver(new AID(receiver, AID.ISLOCALNAME));  
			Bid acceptedBid= new Bid(this.idObject,this.CurrentPrice,this.myAgent.getLocalName());
			try {
				msg.setContentObject(acceptedBid);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.myAgent.send(msg);
			System.out.println(this.myAgent.getLocalName()+"("+ ((BidderAgent) this.myAgent).getRole() +") -- I send YES to "+ this.receiver+" with price "+CurrentPrice);
		}	

	}

}
