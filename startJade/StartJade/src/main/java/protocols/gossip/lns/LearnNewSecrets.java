package protocols.gossip.lns;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import protocols.IProtocol;
import protocols.Role;
import protocols.auctions.IAuction;
import protocols.auctions.agents.AuctioneerAgent;
import protocols.auctions.agents.BidderAgent;
import protocols.auctions.englishAuction.EnglishAuction;
import protocols.tools.CreateAgentFromJsonFile;
import protocols.tools.ProtocolFromJson;


/** 
 *each agent chooses, at a random moment, an agent among his neighbors whose
 *secret he does not know, and he calls him. During a call the two agents must
 *exchange the secrets they have. So the calling agent, after receiving a message
 *from the called party confirming that he received the call and that he is ready
 *for the exchange, sends the secrets he has. The agent called, after receiving 
 *the message sent by the other, in turn sends back the secrets he knows.
 *When an agent receives all the secrets from his neighbors, he registers in the 
 *yellow pages as an expert. He checks at every moment that there are still agents
 * who are not experts, since they can call him. The protocol therefore ends when 
 * everyone is registered as an expert.
 *
 *@author Roza Amokrane
 */
public class LearnNewSecrets implements IProtocol{

	@Override
	public  List<Behaviour> getBehaviours(Role role, Agent agent, List<String> listagents) {
		final String STATE_CALL = "start a call";
		final String STATE_RECEIVE_CALL = "wait for a call";
		final String STATE_RECEIVE_ACCEPT= "wait for accept call";
		final String STATE_ACCEPT = "answer a call";
		final String STATE_RECEIVE_SECRET = "wait for a secret";
		final String STATE_SEND_SECRET = "send secret";
		final String STATE_END_CALL =  "call end";
		final String STATE_FSM_CALL = " fsm call";
		final String STATE_FSM_ANSWER = "fsm answer";
		final String STATE_END_LNS = "LNS end";

		//The FSM executed by an agent while calling another one
		FSMBehaviour fsmCaller = new FSMBehaviour(agent)  {
			private static final long serialVersionUID = 3566900023610851171L;

			public int onEnd() {
				//System.out.println(this.myAgent.getLocalName() +" FSM behaviour completed.");
				this.reset();
				return super.onEnd();
			}
		};
		//a state to start a call
		fsmCaller.registerFirstState(new StartCallBehaviour(agent), STATE_CALL);
		//the state at the end of a call
		fsmCaller.registerLastState(new EndCallBehaviour(agent), STATE_END_CALL);
		//the state of the agent when he waits to receive message "Agree" from the agents he calles
		fsmCaller.registerState(new ReceiveAcceptBehaviour(agent), STATE_RECEIVE_ACCEPT);
		//a state to send his secret to the agent he called
		fsmCaller.registerState(new SendSecretBehaviour(agent), STATE_SEND_SECRET);
		//a state to wait for the secret to be sent from the other agent
		fsmCaller.registerState(new ReceiveSecretBehaviour(agent), STATE_RECEIVE_SECRET);
		
		//After making a call , the agent waits for the other agent to send Agree
		fsmCaller.registerDefaultTransition(STATE_CALL, STATE_RECEIVE_ACCEPT );
		//if the agent receives agree he send his secret
		fsmCaller.registerTransition(STATE_RECEIVE_ACCEPT, STATE_SEND_SECRET, 1);
		//if the agent doesn't receive agree , he ends the call
		fsmCaller.registerTransition(STATE_RECEIVE_ACCEPT, STATE_END_CALL, 0);
		//after sending his secret the agent waits for the other one secret's
		fsmCaller.registerDefaultTransition(STATE_SEND_SECRET, STATE_RECEIVE_SECRET );
		//when he receives the other agent secret's , the call is ended
		fsmCaller.registerDefaultTransition(STATE_RECEIVE_SECRET, STATE_END_CALL);
		//the FSM executed by an agent who receives a call
		FSMBehaviour fsmReceiver = new FSMBehaviour(agent)  {
			private static final long serialVersionUID = 5450675021941872016L;

			public int onEnd() {
				//System.out.println(this.myAgent.getLocalName() +" FSM behaviour completed.");
				this.reset();
				return super.onEnd();
			}
		};
		
		//A state where the agent sends message "Agree" as an answer for the one he called him
		fsmReceiver.registerFirstState(new AcceptCallBehaviour(agent), STATE_ACCEPT);
		fsmReceiver.registerLastState(new EndCallBehaviour(agent), STATE_END_CALL);
		fsmReceiver.registerState(new SendSecretBehaviour(agent), STATE_SEND_SECRET);
		fsmReceiver.registerState(new ReceiveSecretBehaviour(agent), STATE_RECEIVE_SECRET);

		//After sending message "Agree" , the agent waits for the other one to send his secret
		fsmReceiver.registerDefaultTransition(STATE_ACCEPT, STATE_RECEIVE_SECRET );
	
		fsmReceiver.registerTransition(STATE_RECEIVE_SECRET,STATE_SEND_SECRET, 1);
		fsmReceiver.registerTransition(STATE_RECEIVE_SECRET,STATE_END_CALL, 0);
		//After sending his secret the call is ended
		fsmReceiver.registerDefaultTransition(STATE_SEND_SECRET, STATE_END_CALL);

		// The Agent LNS FSM
		FSMBehaviour fsmLNS = new FSMBehaviour(agent);
		//the agent is waiting for calls, if he doesn't receive for a random period he starts a call
		fsmLNS.registerFirstState(new WaitForCallBehaviour(agent), STATE_RECEIVE_CALL);
		fsmLNS.registerState(fsmCaller, STATE_FSM_CALL);
		fsmLNS.registerState(fsmReceiver, STATE_FSM_ANSWER);
		fsmLNS.registerLastState(new End_LNS_Behaviour(agent), STATE_END_LNS);

		//if the agent receives a call
		fsmLNS.registerTransition(STATE_RECEIVE_CALL, STATE_FSM_ANSWER, 1);
		//if the agent starts a call
		fsmLNS.registerTransition(STATE_RECEIVE_CALL, STATE_FSM_CALL, 0);
		//if the agent doesn't receive a call but knows all the secrets of his neighbors
		fsmLNS.registerTransition(STATE_RECEIVE_CALL, STATE_RECEIVE_CALL, 2);
		//if everybody is expert
		fsmLNS.registerTransition(STATE_RECEIVE_CALL, STATE_END_LNS, 3);
		
		//At the end of a call , the agent return to the waiting state
		fsmLNS.registerDefaultTransition(STATE_FSM_CALL, STATE_RECEIVE_CALL);
		fsmLNS.registerDefaultTransition(STATE_FSM_ANSWER, STATE_RECEIVE_CALL);

		List<Behaviour> lb = new ArrayList<Behaviour>();
		System.out.println(agent.getLocalName()+"  :   "+((LNSAgent) agent).printSecrets()+ " neighbours : "+((LNSAgent) agent).printNeighbours());
		lb.add(fsmLNS);
		return lb;
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static List<CreateAgentFromJsonFile> getProperties(ProtocolFromJson pr) {
		List<CreateAgentFromJsonFile> agents = new ArrayList<CreateAgentFromJsonFile>();
		Object[] objtab = null;
		String agentName;
		String secret;
		List<String> neighbours;
		List<Map> agentsParam =  (List<Map>) pr.get("Agents");
		int nbAgents = agentsParam.size();
		for(Map ag : agentsParam ){
			agentName = (String) ag.get("agentName");
			secret = (String) ag.get("secret");
			neighbours = (List<String>) ag.get("neighbours");
			objtab = new Object[]{secret, neighbours, nbAgents};
			agents.add(new CreateAgentFromJsonFile(agentName, LNSAgent.class.getName(), objtab));
		}
		return agents;
	}
	


	/**
	 *
	 * The agent chooses randomly another one from whose he doesn't know the secret and calls him
	 *
	 */
	public class StartCallBehaviour extends OneShotBehaviour{
		private static final long serialVersionUID = 8788676800655983668L;
		Random rand = new Random();

		public StartCallBehaviour(Agent myAgent) {
			super(myAgent);
		}
		@Override
		public void action() {

			int nbUnknowSecrets = ((LNSAgent) this.myAgent).getUnknownSecrets().size();
			int index = 0;
			if( nbUnknowSecrets > 1){
				index = rand.nextInt(nbUnknowSecrets - 1);
			}
			String receiver = ((LNSAgent) this.myAgent).getUnknownSecrets().get(index);
			((LNSAgent) this.myAgent).setReceiver(receiver);
			System.out.println(this.myAgent.getLocalName()+ "  --  call "+ receiver);
			ACLMessage msgCall= new ACLMessage(ACLMessage.REQUEST);
			msgCall.setProtocol("LNS");
			msgCall.setSender(this.myAgent.getAID());
			msgCall.addReceiver( new AID(receiver, AID.ISLOCALNAME) );
			((LNSAgent) this.myAgent).setIdCall(((LNSAgent) this.myAgent).getIdCall() +1);
			msgCall.setConversationId(String.valueOf(((LNSAgent) this.myAgent).getIdCall()));
			((LNSAgent) this.myAgent).setConversationId(msgCall.getConversationId());
			this.myAgent.send(msgCall);
		}


	}

	/**
	 * The  agent chooses a random waiting time 
	 * at the end of this waiting time he verify if he received a call, in that case he answer the call
	 * if he didn't receive a call , he verifies if he is not an expert yet otherwise he starts a call
	 */
	public class WaitForCallBehaviour extends OneShotBehaviour{

		private int exitValue = 0;
		Random rand = new Random();
		private static final long serialVersionUID = 3268739152264742587L;
		public WaitForCallBehaviour(Agent agent) {
			super(agent);
		}
		@Override
		public void action() {
			// Choose a random waiting time
			int wait = rand.nextInt(1000);
			this.myAgent.doWait(wait);
			final MessageTemplate msgCallTemplate = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST), MessageTemplate.MatchProtocol("LNS"));
			ACLMessage msgCall = this.myAgent.receive(msgCallTemplate);
			
			//A call is received
			if(msgCall != null ) {
				//System.out.println(this.myAgent.getLocalName()+"  received a call");
				((LNSAgent) this.myAgent).setConversationId(msgCall.getConversationId());
				((LNSAgent) this.myAgent).setReceiver(msgCall.getSender().getLocalName());
				exitValue = 1;
			}
			//The agent didn't receive a call
			else {
				//the agent is not an expert yet
				if(! ((LNSAgent) this.myAgent).isExpert()) {
					//if he knows all the secrets of his neighbors 
					if(((LNSAgent) this.myAgent).getUnknownSecrets().size() == 0) {
						System.out.println(this.myAgent.getLocalName()+ " -- I know all the secrets of my neighbours");
						((LNSAgent) this.myAgent).setExpert(true);
						((LNSAgent) this.myAgent).addToYellowPage("Expert");
						this.exitValue = 2;
					}
					//if he doesn't know all his neighbors secret's
					else {
						exitValue = 0;
					}
				}
				//the agent is an expert
				else {
					//if all other agents ar experts
					System.out.println(this.myAgent.getLocalName()+" -- send request to yellow pages to verify if other agents finished");
					if(((LNSAgent) this.myAgent).endLNS()) {
						exitValue = 3;
					}
					else {
						this.exitValue = 2;
					}
				}
			}

		}
		public int onEnd() {
			return exitValue;
		}
	}

	/**
	 * 
	 * After receiving a call the agent informs the caller that he is ready to exchange their secrets
	 *
	 */
	public class AcceptCallBehaviour extends OneShotBehaviour{
		private static final long serialVersionUID = 1L;

		public AcceptCallBehaviour(Agent agent) {
			super(agent);
		}

		@Override
		public void action() {
			// TODO Auto-generated method stub		
			ACLMessage msgAccept= new ACLMessage(ACLMessage.AGREE);
			msgAccept.setSender(this.myAgent.getAID());
			msgAccept.setConversationId(((LNSAgent) this.myAgent).getConversationId());
			msgAccept.addReceiver(new AID(((LNSAgent) this.myAgent).getReceiver(), AID.ISLOCALNAME));
			msgAccept.setProtocol("LNS");
			//System.out.println(this.myAgent.getLocalName()+" -- send Agree -- to "+((LNSAgent) this.myAgent).getReceiver());
			this.myAgent.send(msgAccept);
		}
	}


	/****
	 * 
	 * Used to receive a message agree after calling an agent
	 */
	public class ReceiveAcceptBehaviour extends OneShotBehaviour{

		private static final long serialVersionUID = 4200409910048449786L;
		private int exitValue = 0;
		public ReceiveAcceptBehaviour(Agent agent) {
			super(agent);
		}

		@Override
		public void action() {
			final MessageTemplate msgAcceptTemplate = MessageTemplate.and(MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.AGREE), MessageTemplate.MatchProtocol("LNS")), MessageTemplate.MatchConversationId(((LNSAgent) this.myAgent).getConversationId())), MessageTemplate.MatchSender(new AID(((LNSAgent) this.myAgent).getReceiver(), AID.ISLOCALNAME)));

			try {
				this.myAgent.doWait(300);
			} catch (Exception e) {
				e.printStackTrace();
			}
			ACLMessage msgAccept = this.myAgent.receive(msgAcceptTemplate);
			if(msgAccept != null) {
				//System.out.println(this.myAgent.getLocalName()+" -- received Agree");
				exitValue = 1;
			}
			else {
				exitValue = 0;
				System.out.println(this.myAgent.getLocalName()+" --  "+ ((LNSAgent) this.myAgent).getReceiver()+"  didn't answer the call");
				((LNSAgent) this.myAgent).setCallDone(false);
			}

		}
		public int onEnd() {
			return exitValue;
		}

	}

	/**
	 * 
	 * An agent send it's secret to another one
	 *
	 */
	public class SendSecretBehaviour extends OneShotBehaviour{

		private static final long serialVersionUID = 457723040545774202L;


		public SendSecretBehaviour(Agent agent) {
			super(agent);
		}


		@Override
		public void action() {
			// TODO Auto-generated method stub
			ACLMessage msgSecret= new ACLMessage(ACLMessage.INFORM);
			msgSecret.setSender(this.myAgent.getAID());
			msgSecret.setProtocol("LNS");
			msgSecret.setConversationId( ((LNSAgent) this.myAgent).getConversationId());
			msgSecret.addReceiver(new AID(((LNSAgent) this.myAgent).getReceiver(), AID.ISLOCALNAME));
			try {
				msgSecret.setContentObject((Serializable) ((LNSAgent) this.myAgent).getSecrets());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(this.myAgent.getLocalName()+" -- send secret  to   "+((LNSAgent) this.myAgent).getReceiver());
			this.myAgent.send(msgSecret);
		}
	}

	/***
	 * 
	 * Wait for a secret sent by another agent
	 *
	 */
	public class ReceiveSecretBehaviour extends OneShotBehaviour{

		private static final long serialVersionUID = 172933834196166942L;
		private int exitValue = 0;

		public ReceiveSecretBehaviour(Agent agent) {
			super(agent);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void action() {
			final MessageTemplate msgSecretTemplate =MessageTemplate.and( MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM), MessageTemplate.MatchProtocol("LNS")), MessageTemplate.MatchConversationId(((LNSAgent) this.myAgent).getConversationId())), MessageTemplate.MatchSender(new AID(((LNSAgent) this.myAgent).getReceiver(), AID.ISLOCALNAME)));
			try {
				this.myAgent.doWait(400);
			} catch (Exception e) {
				e.printStackTrace();
			}

			ACLMessage msgSecret = this.myAgent.receive(msgSecretTemplate);
			if(msgSecret != null) {

				try {
					((LNSAgent) this.myAgent).addSecrets((Map<String, String>) msgSecret.getContentObject());
					((LNSAgent) this.myAgent).setCallDone(true);
					//System.out.println(this.myAgent.getLocalName()+" received secret from : "+((LNSAgent) this.myAgent).getReceiver());
				} catch (UnreadableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				this.exitValue = 1;
			}
			else {
				System.out.println(this.myAgent.getLocalName()+" -- didn't receive secret from "+((LNSAgent) this.myAgent).getReceiver() );

				((LNSAgent) this.myAgent).setCallDone(false);
				exitValue = 0;
			}
		}

		public int onEnd() {
			return this.exitValue ;
		}
	}

	//
	public class EndCallBehaviour extends OneShotBehaviour{

		private static final long serialVersionUID = -7939612841686677988L;
		public EndCallBehaviour(Agent agent) {
			super(agent);
		}
		@Override
		public void action() {
			if(((LNSAgent) this.myAgent).callDone())
				System.out.println(this.myAgent.getLocalName()+"  -- At the end of call with "+((LNSAgent) this.myAgent).getReceiver()+": Secrets I have are : "+((LNSAgent) this.myAgent).printSecrets());
			
			//deletes all the messages he received during the call
			final MessageTemplate msgCallTemplate = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST), MessageTemplate.MatchProtocol("LNS"));
			ACLMessage msg = this.myAgent.receive(msgCallTemplate);
			while(msg != null) {
				msg = this.myAgent.receive(msgCallTemplate);
			}
		}
	}
	

	public class End_LNS_Behaviour extends OneShotBehaviour {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4275069603692326032L;


		public End_LNS_Behaviour(Agent a) {
			super(a);
		}


		@Override
		public void action() {
			System.out.println(this.myAgent.getLocalName()+" -- End of LNS : the secrets I know: "+((LNSAgent) this.myAgent).printSecrets());
		}

	}
}
