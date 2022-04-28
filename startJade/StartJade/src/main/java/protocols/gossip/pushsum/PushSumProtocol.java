package protocols.gossip.pushsum;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import protocols.IProtocol;
import protocols.Role;
import protocols.auctions.IAuction;
import protocols.auctions.agents.AuctioneerAgent;
import protocols.auctions.agents.BidderAgent;
import protocols.auctions.englishAuction.EnglishAuction;
import protocols.gossip.pushsum.agents.PushSumAgent;
import protocols.gossip.pushsum.agents.TickerAgent;
import protocols.tools.CreateAgentFromJsonFile;
import protocols.tools.ProtocolFromJson;

/** 
* @startuml


* note right of Ticker :First Round
* Ticker ->> AgentPush1 : startRound(idRound)
* Ticker ->> AgentPush2 : startRound(idRound)
* Ticker ->> AgentPush3 : startRound(idRound)
* note over AgentPush1 : (S1,1 =v1,w1,1=1,neighbors=[Agent2,Agent3]) 
* AgentPush1->>AgentPush2: Push ((1/2*S1,1),(1/2*w1,1)) 
* AgentPush2 ->>Ticker : Inform()
* note over AgentPush2 : (S1,2=v2,w1,2=1,neighbors=[Agent3,Agent1]) 
* AgentPush2->>AgentPush3: Push((1/2*S1,2),(1/2*w1,2)) 
* AgentPush3 ->>Ticker : Inform()
* note over AgentPush3 :(S1,3=v3,w1,3=1 ,neighbors=[Agent1,Agent2])
* AgentPush3->>AgentPush1: Push((1/2*S1,3),(1/2*w1,3))
* AgentPush1 ->>Ticker : Inform()
* note right of Ticker :Second Round
* Ticker ->> AgentPush1 : startRound(idRound)
* Ticker ->> AgentPush2 : startRound(idRound)
* Ticker ->> AgentPush3 : startRound(idRound)
* note  over AgentPush1 : (S2,1=1/2*S1,1+1/2*S1,3, w2,1=1/2*w1,1+1/2*w1,3)
* AgentPush1->>AgentPush3: Push((1/2*S2,1),(1/2*w2,1)) 
* AgentPush3 ->>Ticker : Inform()
* note over AgentPush2 :(S2,2=1/2*S1,2+1/2*S1,1, w2,1=1/2*w1,2+1/2*w1,1)  
* AgentPush2->>AgentPush3: Push((1/2*S2,2),(1/2*w2,2)) 
* AgentPush3 ->>Ticker : Inform()
* note over AgentPush3 : (S2,3=1/2*S1,3+1/2*S1,2, w2,1=1/2*w1,3+1/2*w1,2)    
* AgentPush3->>AgentPush2: Push((1/2*S2,3),(1/2*w2,3)) 
* AgentPush2->>Ticker : Inform()  
* note right of Ticker :After T Round 
* Ticker ->> AgentPush1 : End
* Ticker ->> AgentPush2 : End
* Ticker ->> AgentPush3 : End 
@enduml
**/
/**
 * <p>Synchronized version of the PushShum protocol</p>
 * we have an "Ticker" agent and a set of n "push" agents: 
 * Each  push agent holds a value generated randomly 
 * Initially, sum := value and weight := 1
 * Agent Ticker sends a "Start round" message to push sum agents at the start of each round.
 * When the pushSum agent receives the "Start Round" message, he must do the push so he selects a random receiver from his list of neighbors to send them half of sum and weight, and he keeps the half 
 * When Push agent receives the message push, he should add the pair (sum,weight) to its own values and inform the Ticker agent that made the pull 
 * When the Ticker agent receives n inform type messages , he starts the next round
 * After nbRounds (nbRounds set by user) Rounds the protocol is ended by agent Ticker 
 * 
 *@see <a href="https://startjade.gitlab.io/protocol-library/gossip.html">Push Sum gossip's documentation</a>
 * @author Nabila Ould Belkacem
 *
 */

public class PushSumProtocol implements IProtocol

{

	@Override
	public List<Behaviour> getBehaviours(Role role, Agent agent, List<String> listagents) {
		List<Behaviour> lb=new ArrayList<Behaviour>();
		PushSumObject object=new PushSumObject(false);
		switch (role) {
			case Ticker :
				lb.add(new StartRoundBehaviour(agent,listagents,((TickerAgent)agent).getNbRounds(),object));
				break;
			case PushSum :
				float sum= ((PushSumAgent)agent).getPair().getSum();
				float weight= ((PushSumAgent)agent).getPair().getWeight();
				System.out.println(agent.getLocalName()+"(PushSum) ---> my initial sum : "+sum+", my initial weight : "+weight);
				lb.add(new  PushBehaviour(agent,object));
				lb.add(new  PullBehaviour(agent,object));
				break;
			default :
				System.out.println(" protocol -- getBehaviours -- unknown role: "+role);
			}
		    
			return lb;
		}
	

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static List<CreateAgentFromJsonFile> getProperties(ProtocolFromJson pr) {
		List<CreateAgentFromJsonFile> agents = new ArrayList<CreateAgentFromJsonFile>();
		Object[] objtab = null;
		Map tickerParams = (Map) pr.get("Ticker");
		String agentName = (String) tickerParams.get("agentName");
		Objects.requireNonNull(agentName);
		Integer nbRounds = (Integer) tickerParams.get("nbRounds");
		Objects.requireNonNull(nbRounds);
		objtab = new Object[]{nbRounds};
		agents.add(new CreateAgentFromJsonFile(agentName, TickerAgent.class.getName(), objtab));

		for(Map pushsumParams : (List<Map>) pr.get("PushSum")){
			agentName = (String) pushsumParams.get("agentName");
			Objects.requireNonNull(agentName);
			List<String> neighbors =(List<String>)pushsumParams.get("neighbors");
			Objects.requireNonNull(neighbors);
			Integer value = (Integer)pushsumParams.get("value");
			Objects.requireNonNull(value);
			objtab = new Object[]{neighbors,value};
			agents.add(new CreateAgentFromJsonFile(agentName, PushSumAgent.class.getName(), objtab));;
		}
		return agents;

	}


	/*******************************************************************
	 * ****************************************************************
	 * 
	 * Behaviours used by agent Ticker
	 *
	 ********************************************************************
	 ********************************************************************/

	
	/** Agent Ticker asks other agents to start a Round **/
	
	
	public class StartRoundBehaviour extends SimpleBehaviour {
		private static final long serialVersionUID = 9088209402507795289L;
		private boolean finished=false;
		private List<String> listAgent;
		private int idRound;
		private boolean stop=false;
		private int nbPulls;
		private int nbRounds;
		private PushSumObject object;
		/**
		 * 
		 * @param myagent
		 * @param listAgent : list of agents push sum participating in the protocol 
		 * @param nbRounds : the number of rounds 
		 * @param object: object shared between behaviours 
		 */
		public StartRoundBehaviour (final Agent myagent,List<String> listAgent , int nbRounds,PushSumObject object) {
			super(myagent);
			this.listAgent=listAgent;
			this.idRound=0;
			this.nbPulls=0;
			this.nbRounds=nbRounds;
			this.object=object;
		}
		@Override
		public void action() {

			if (!stop) {
				//1°Create the message Start Round
				idRound+=1;
				System.out.println(this.myAgent.getLocalName()+"(Ticker)"+" ----> Start Round"+idRound);
				final ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
				msg.setSender(this.myAgent.getAID());
				msg.setProtocol("PUSHSUM");
				for(String receiver:listAgent)
					msg.addReceiver(new AID(receiver, AID.ISLOCALNAME));  						
				try {
					msg.setContentObject((Serializable)idRound);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				this.myAgent.send(msg);
				this.stop=true;
			}
			//2) receive a message after a pull from participants
			MessageTemplate msgTemplate= MessageTemplate.and(MessageTemplate.MatchProtocol("PUSHSUM"), MessageTemplate.MatchPerformative(ACLMessage.CONFIRM));		
			final ACLMessage msgreceived = this.myAgent.receive(msgTemplate);
			if (msgreceived != null) {
				nbPulls++;
			}else
				block(); 
			//Send end  when all the agents have done "nbRounds" Rounds
			if ((idRound==nbRounds) && (nbPulls==listAgent.size()))
			{ 
				final ACLMessage msgCancel = new ACLMessage(ACLMessage.CANCEL);
				msgCancel.setSender(this.myAgent.getAID());
				msgCancel.setProtocol("PUSHSUM");
				for(String receiver:listAgent)
					msgCancel.addReceiver(new AID(receiver, AID.ISLOCALNAME));  						
				this.myAgent.send(msgCancel);
				this.finished=true;	
			}
			//when all the pushes sent have been pulled by the selected neighbors 
			if (nbPulls==listAgent.size())
			{
				object.setStop(true);
				this.nbPulls=0;
				stop=false;
			}
		}

		public boolean isStop() {
			return stop;
		}
		public void setStop(boolean stop) {
			this.stop = stop;
		}
		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			return this.finished;
		}
		}
	
	/*******************************************************************
	 * ****************************************************************
	 * 
	 * Behaviours used by an PushSum Agent
	 *
	 ********************************************************************
	 ********************************************************************/

	
	/**Push Agent after receiving a Start Round message from the Agent Ticker, he sends a push to his neighbor  **/
	public class PushBehaviour extends SimpleBehaviour {
		private static final long serialVersionUID = 9088209402507795289L;
		private boolean finished=false;
		Random rand = new Random();
		private int idRound;
		private boolean stop;
		private PushSumObject object;
		/**
		 * 
		 * @param myagent : the reference to the agent
		 */
		public PushBehaviour ( Agent myagent,PushSumObject object) {
			super(myagent);
			this.object=object;
			
		}
		@SuppressWarnings("unchecked")
		@Override
		public void action() {
			//1) receive the message Start Round
			MessageTemplate msgTemplate= MessageTemplate.and(MessageTemplate.MatchProtocol("PUSHSUM"), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));		
			final ACLMessage msg = this.myAgent.receive(msgTemplate);
			if (msg != null) {	
				try {
					this.idRound=(int)msg.getContentObject();
				} catch (UnreadableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//2 Create  message Push to send to a random neighbor 
				final ACLMessage msgPush = new ACLMessage(ACLMessage.INFORM);
				msgPush.setSender(this.myAgent.getAID());
				msgPush.setProtocol("PUSHSUM");
				String receiver=((PushSumAgent)(this.myAgent)).getNeighbors().get(rand.nextInt(((PushSumAgent)(this.myAgent)).getNeighbors().size()));
				msgPush.addReceiver(new AID(receiver, AID.ISLOCALNAME)); 
				System.out.println(this.myAgent.getLocalName()+"(PushSum)"+" ----> I  Pushed  in Round "+idRound+" to my neighbour "+receiver);
				PairPushSum pairPushSum = new PairPushSum(0.5f*((PushSumAgent)this.myAgent).getPair().getSum(),0.5f*((PushSumAgent)this.myAgent).getPair().getWeight(),msg.getSender().getLocalName());
				((PushSumAgent)this.myAgent).getPair().setSum(0.5f*((PushSumAgent)this.myAgent).getPair().getSum());
				((PushSumAgent)this.myAgent).getPair().setWeight(0.5f*((PushSumAgent)this.myAgent).getPair().getWeight());
				object.setStop(false);
				try {
					msgPush.setContentObject(pairPushSum);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				this.myAgent.send(msgPush);
				
			}else {
				//Receive message End
				MessageTemplate msgTemplate1= MessageTemplate.and(MessageTemplate.MatchProtocol("PUSHSUM"), MessageTemplate.MatchPerformative(ACLMessage.CANCEL));		
				final ACLMessage msgCancel = this.myAgent.receive(msgTemplate1);
				if (msgCancel != null)
				{
					System.out.println(this.myAgent.getLocalName()+"(PushSum) ----> my final sum : "+((PushSumAgent)this.myAgent).getPair().getSum()+", my final weight : "+((PushSumAgent)this.myAgent).getPair().getWeight());
					this.finished=true;
					
				}
					
				else{
					block();// the behaviour goes to sleep until the arrival of a new message in the agent's Inbox.
				}
			}
    
		}
		
		public boolean isStop() {
			return stop;
		}
		public void setStop(boolean stop) {
			this.stop = stop;
		}
		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			return finished ;
		}
	}
	
	/** Agent Push performs the pull  after receiving a message Push from his neighbor **/ 
	public class PullBehaviour extends SimpleBehaviour {
		private static final long serialVersionUID = 9088209402507795289L;
		private boolean finished=false;
		private PushSumObject object;
		/**
		 * @param myagent : the reference to the agent
		 * @param object : Object shared between behaviors 
		 */
		public PullBehaviour (Agent myagent,PushSumObject object) {
			super(myagent);
			this.object=object;
			
		}
		@SuppressWarnings("unchecked")
		@Override
		public void action() {
			if (!object.isStop())
			{	
				//1) receive the message
				MessageTemplate msgTemplate= MessageTemplate.and(MessageTemplate.MatchProtocol("PUSHSUM"), MessageTemplate.MatchPerformative(ACLMessage.INFORM));		
				final ACLMessage msg = this.myAgent.receive(msgTemplate);
				PairPushSum pairReceived=null;
				if (msg != null) {		
					try {
						pairReceived=(PairPushSum)msg.getContentObject();
					} catch (UnreadableException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					((PushSumAgent)this.myAgent).getPair().setSum(((PushSumAgent)this.myAgent).getPair().getSum()+pairReceived.getSum());
					((PushSumAgent)this.myAgent).getPair().setWeight(((PushSumAgent)this.myAgent).getPair().getWeight()+pairReceived.getWeight());
				    System.out.println(this.myAgent.getLocalName()+"(PushSum)"+"----> I received a pull : my sum is "+((PushSumAgent)this.myAgent).getPair().getSum()+" my weight is "+((PushSumAgent)this.myAgent).getPair().getWeight());
					 //2 Create message to send to the agent Ticker
					 final ACLMessage msgComfirm = new ACLMessage(ACLMessage.CONFIRM);
					 msgComfirm.setSender(this.myAgent.getAID());
					 msgComfirm.setProtocol("PUSHSUM");
					 msgComfirm.addReceiver(new AID(pairReceived.getAgentName(), AID.ISLOCALNAME)); 
					  this.myAgent.send(msgComfirm);
				}else 
				{
					//3 Receive message Cancel from agent Ticker
					MessageTemplate msgTemplate1= MessageTemplate.and(MessageTemplate.MatchProtocol("PUSHSUM"), MessageTemplate.MatchPerformative(ACLMessage.CANCEL));		
					final ACLMessage msgCancel = this.myAgent.receive(msgTemplate1);
					if (msgCancel != null)
						this.finished=true;
				else{
					block();// the behaviour goes to sleep until the arrival of a new message in the agent's Inbox.
				}
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






