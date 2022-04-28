package protocols.consensus.paxos;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.text.StyledEditorKit.ForegroundAction;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.ThreadedBehaviourFactory;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import protocols.Role;
import protocols.auctions.IAuction;
import protocols.consensus.paxos.agents.AcceptorAgent;
import protocols.consensus.paxos.agents.ClientAgent;
import protocols.consensus.paxos.agents.IAcceptor;
import protocols.consensus.paxos.agents.ILearner;
import protocols.consensus.paxos.agents.IProposer;
import protocols.consensus.paxos.agents.LearnerAgent;
import protocols.consensus.paxos.agents.MultiAgent;
import protocols.consensus.paxos.agents.ProposerAgent;
import protocols.tools.CreateAgentFromJsonFile;
import protocols.tools.ProtocolFromJson;


/**
 * This class is used to gather the behaviors of the Paxos consensus.
 * the concrete classes are BasicPaxos and MultiPaxos. 
 * 
 * @author Axel Foltyn
 *
 */
public abstract class Paxos implements IAuction {

	// The currently implemented paxos.
	public enum PaxosImplemented {
		BasicPaxos("Basic Paxos"), MultiPaxos("Multi Paxos");

		private String name;

		PaxosImplemented(String s){
			name=s;
		}

		public String getName() {
			return name;
		}
	}


	// Used to find out which type of paxos is used.
	private PaxosImplemented paxos;

	// State names use for Proposer behavior
	private static final String STATE_LISTEN_REQUEST = "Listen request";
	private static final String STATE_PROPOSE = "Propose";
	private static final String STATE_WAIT_PROMISE = "wait promise";
	private static final String STATE_ACCEPT = "accept";
	private static final String STATE_WAIT_ACCEPT = "wait accept";
	private static final String STATE_DECIDE = "decide";




	public Paxos(PaxosImplemented paxos) {
		this.paxos = paxos;
	}

	@Override
	public List<Behaviour> getBehaviours(Role role, Agent agent, List<String> listagents) {
		boolean isMultiPaxos = paxos == PaxosImplemented.MultiPaxos;
		List<Behaviour> lb = new ArrayList<Behaviour>();
		switch (role) {
		case Proposer :

			((IProposer)agent).setPaxos(paxos);

			FSMBehaviour fsm = new FSMBehaviour(agent);
			// Register STATE_LISTEN (first state)
			fsm.registerFirstState(new ListenResquestBehaviour((IProposer) agent), STATE_LISTEN_REQUEST);
			// Register STATE_PROPOSE
			fsm.registerState(new ProposeBehaviour((IProposer) agent), STATE_PROPOSE);
			// Register STATE_WAIT_PROMISE
			fsm.registerState(new WaitPromiseBehaviour((IProposer) agent), STATE_WAIT_PROMISE);
			// Register STATE_ACCEPT
			fsm.registerState(new AcceptBehaviour((IProposer) agent), STATE_ACCEPT);
			// Register STATE_ACCEPT
			fsm.registerState(new WaitAcceptBehaviour((IProposer) agent), STATE_WAIT_ACCEPT);
			// Register STATE_DECIDE
			fsm.registerState(new DecideBehaviour((IProposer) agent), STATE_DECIDE);


			fsm.registerDefaultTransition(STATE_LISTEN_REQUEST, STATE_PROPOSE);
			fsm.registerDefaultTransition(STATE_PROPOSE, STATE_WAIT_PROMISE);
			fsm.registerTransition(STATE_WAIT_PROMISE, STATE_PROPOSE, 0);
			fsm.registerTransition(STATE_WAIT_PROMISE, STATE_ACCEPT, 1);
			fsm.registerDefaultTransition(STATE_ACCEPT, STATE_WAIT_ACCEPT);
			fsm.registerTransition(STATE_WAIT_ACCEPT, STATE_PROPOSE, 0);
			fsm.registerTransition(STATE_WAIT_ACCEPT, STATE_DECIDE, 1);
			fsm.registerDefaultTransition(STATE_DECIDE, STATE_LISTEN_REQUEST);


			lb.add(fsm);

			break;
		case Acceptor :
			lb.add(new AcceptorBehaviour((IAcceptor) agent));
			break;
		case Learner :
			((ILearner)agent).setPaxos(paxos);
			lb.add(new LearnerBehaviour((ILearner) agent, isMultiPaxos));
			break;
		case Client :
			Object[] args = agent.getArguments();
			String proposer = (String)args[1];
			String topicValue = (String) args[2];
			String valueRequest = (String) args[3];
			lb.add(new ClientBehaviour(agent,topicValue, valueRequest, proposer));
			break;
		case Multi_Role:
			args = agent.getArguments();
			HashMap<String, Object> params = (HashMap<String, Object>) args[1];
			List<String> roles = (List<String>)params.get("roles");

			//ParallelBehaviour pb = new ParallelBehaviour(ParallelBehaviour.WHEN_ALL);

			for (String role2 : roles) {
				switch(role2) {
				case "Acceptor" :
					addBehavior(lb, Role.Acceptor, (MultiAgent) agent, listagents);
					break;
				case "Learner" :
					addBehavior(lb, Role.Learner, (MultiAgent) agent, listagents);
					break;
				case "Client" :
					agent.setArguments(new Object[]{this, params.get("nameProposer"), params.get("topic"), params.get("value")});
					addBehavior(lb, Role.Client, (MultiAgent) agent, listagents);
					break;
				case "Proposer" :
					agent.setArguments(new Object[]{this});
					((IProposer) agent).setAcceptors(getName((List<String>)params.get("namesAcceptors")));
					((IProposer) agent).setLearners(getName((List<String>)params.get("namesLearners")));
					addBehavior(lb, Role.Proposer, (MultiAgent) agent, listagents);
					break;
				default:
					System.err.println(paxos.getName() + " protocol -- getBehaviours -- unknown role: "+role+">"+role2);
				}
				//lb.add(pb);
			}
			break;
		default :
			System.err.println(paxos.getName() + " protocol -- getBehaviours -- unknown role: "+role);
		}
		return lb;
	}

	private void addBehavior(ParallelBehaviour pb, Role role, MultiAgent agent, List<String> listagents) {
		agent.create(role);
		List<Behaviour> lbTmp = getBehaviours(role, agent, listagents);
		for (Behaviour behaviour : lbTmp) {
			pb.addSubBehaviour(behaviour);
		}
	}

	private void addBehavior(List<Behaviour> lb, Role role, MultiAgent agent, List<String> listagents) {
		agent.create(role);
		List<Behaviour> lbTmp = getBehaviours(role, agent, listagents);
		for (Behaviour behaviour : lbTmp) {
			ThreadedBehaviourFactory factoryBehaviour = new ThreadedBehaviourFactory();
			lb.add(factoryBehaviour.wrap(behaviour));
		}
	}

	/**
	 * 
	 * @param pr : information read in json.
	 * @return : information for each agent to be created
	 */
	public static List<CreateAgentFromJsonFile> getProperties(ProtocolFromJson pr) {
		List<CreateAgentFromJsonFile> agents = new ArrayList<CreateAgentFromJsonFile>();
		Object[] objtab = null;


		Paxos paxos = null;
		switch (pr.getProtocolName()) {
		case BasicPaxos:
			paxos = new BasicPaxos();
			break;
		case MultiPaxos:
			paxos = new MultiPaxos();
			break;
		default:
			System.err.println(pr.getProtocolName() + " doesn't exist for paxos.");
			System.exit(-1);
			break;
		}


		//acceptor
		List<Map<String, Object>> acceptorParams = (List<Map<String, Object>>) pr.get("Acceptor");
		if (acceptorParams != null){
			objtab=new Object[]{paxos};
			for(Map<String, Object> param : acceptorParams) {
				agents.add(new CreateAgentFromJsonFile((String) param.get("agentName"), AcceptorAgent.class.getName(), objtab));
			}
		}
		//learner
		List<Map<String, Object>> learnerParams = (List<Map<String, Object>>) pr.get("Learner");
		if (learnerParams != null){
			objtab=new Object[]{paxos};
			for(Map<String, Object> param : learnerParams) {
				agents.add(new CreateAgentFromJsonFile((String) param.get("agentName"), LearnerAgent.class.getName(), objtab));
			}
		}
		//proposer
		List<Map<String, Object>> proposerParams = (List<Map<String, Object>>) pr.get("Proposer");
		if (proposerParams != null) {
			objtab=new Object[]{paxos};
			for(Map<String, Object> param : proposerParams) {
				objtab=new Object[]{paxos, 
						getName((List<String>) param.get("namesAcceptors")), 
						getName((List<String>) param.get("namesLearners")) };
				agents.add(new CreateAgentFromJsonFile((String) param.get("agentName"), ProposerAgent.class.getName(), objtab));
			}
		}
		// Client
		List<Map<String, Object>> clientParams = (List<Map<String, Object>>) pr.get("Client");
		if (clientParams != null){
			for(Map<String, Object> param : clientParams) {
				String topicRequest = (String) param.get("topic");
				String valueRequest = (String) param.get("value");
				String globalNameProposer = new AID((String)param.get("nameProposer"), AID.ISLOCALNAME).getName();
				objtab=new Object[]{paxos, globalNameProposer, topicRequest, valueRequest};
				agents.add(new CreateAgentFromJsonFile((String) param.get("agentName"), ClientAgent.class.getName(), objtab));
			}
		}
		//multiRole
		List<Map<String, Object>> multiRoleParams = (List<Map<String, Object>>) pr.get("MultiRole");
		if (multiRoleParams != null){
			for(Map<String, Object> param : multiRoleParams) {
				objtab=new Object[]{paxos, param};
				if (param.containsKey("nameProposer")){
					String globalNameProposer = new AID((String) param.get("nameProposer"), AID.ISLOCALNAME).getName();
					param.put("nameProposer", globalNameProposer);
				}
				agents.add(new CreateAgentFromJsonFile((String) param.get("agentName"), MultiAgent.class.getName(), objtab));
			}
		}

		return agents;
	}

	private static List<String> getName(List<String> localsnames) {
		if (localsnames != null) {
			List<String> res = new ArrayList<String>();
			for(String name : localsnames) {
				res.add(new AID(name, AID.ISLOCALNAME).getName());
			}
			if (res.size() > 0) {
				return res;
			}
		}
		return null;
	}





	/*******************************************************************
	 * ****************************************************************
	 * 
	 *                    Client
	 *
	 ********************************************************************
	 ********************************************************************/





	/**
	 * The behaviour used by the Client.
	 * The behaviour is to send a request to a Proposer and wait the answer
	 * 
	 * @author Axel Foltyn
	 *
	 */
	public class ClientBehaviour extends Behaviour {
		private static final long serialVersionUID = -4939093938296884595L;
		private String proposer;
		private String topicRequest;
		private boolean finished;
		private String value;


		/**
		 * Return the behaviour of Client to Paxos consensus.
		 * @param agent : A reference to the agent.
		 * @param topicRequest : The topic of the request.
		 * @param value : the value choose for this topic
		 * @param proposer : Proposer's global name to which the request is sent.
		 */ 
		public ClientBehaviour(Agent myagent, String topicRequest, String value, String proposer) {
			super(myagent);
			this.proposer = proposer;
			this.topicRequest = topicRequest;
			this.value = value;
		}

		@Override
		public void action() {
			try {
				// Create the message
				final ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
				msg.setSender(this.myAgent.getAID());
				msg.setProtocol("requestProtocol");
				AID proposerAid = new AID(proposer, AID.ISGUID);
				msg.addReceiver(proposerAid);
				msg.setContentObject(new RequestValue(this.myAgent.getAID().getName(), this.topicRequest, this.value));
				this.myAgent.send(msg);
				System.out.println(this.myAgent.getLocalName() + " sends request(" + this.topicRequest + ", " + this.value + ") to "+ proposerAid.getLocalName() + ".");

				final MessageTemplate msgTemplate = MessageTemplate.and(
						MessageTemplate.MatchPerformative(ACLMessage.INFORM),
						MessageTemplate.MatchProtocol("AnswerProtocol"));
				ACLMessage request = this.myAgent.receive(msgTemplate);

				// passive waiting for a request.
				if (request!=null) {
					System.out.println(this.myAgent.getLocalName()+" received "+request.getContent() + ".");
					finished = true;
				}else {
					block();
				}

			} catch (IOException e) {
				System.err.println("Error when sending the message to the Proposer.");
			}
		}

		@Override
		public boolean done() {
			return finished;
		}

	}



	/*******************************************************************
	 * ****************************************************************
	 * 
	 *                    Acceptor
	 *
	 ********************************************************************
	 ********************************************************************/


	// Protocol accepted by the class Acceptor.
	public enum ProtocolAcceptor {
		Propose("ProposeProtocol"), Accept("AcceptProtocol");

		private String name;

		ProtocolAcceptor(String s){
			name=s;
		}

		public String getName() {
			return name;
		}
	}

	/**
	 * The behaviour used by the Acceptor.
	 * The behaviour is to wait for a request, 
	 * check the id value of the request 
	 * and then send the appropriate response.
	 * 
	 * @author Axel Foltyn
	 *
	 */
	public class AcceptorBehaviour extends Behaviour {

		private static final long serialVersionUID = 2283849506266559094L;




		private IAcceptor myagent;

		// template accepted for message.
		private final MessageTemplate msgTemplate = MessageTemplate.and(
				MessageTemplate.or(
						MessageTemplate.MatchProtocol(ProtocolAcceptor.Propose.getName()), 
						MessageTemplate.MatchProtocol(ProtocolAcceptor.Accept.getName())), 
				MessageTemplate.MatchPerformative(ACLMessage.REQUEST));




		/**
		 * Return the behaviour of Acceptor to Paxos consensus.
		 * @param agent : Agent with all IAcceptor method. 
		 */
		public AcceptorBehaviour(IAcceptor agent) {
			super((Agent) agent);
			this.myagent = agent;
		}

		@Override
		public void action() {
			int id;
			String topic;


			ACLMessage request = this.myAgent.receive(msgTemplate);
			// Passive waiting for a request.
			while (request == null){
				this.myAgent.doWait();
				request = this.myAgent.receive(msgTemplate);
			}
			while (request != null) {
				System.out.println(this.myAgent.getLocalName() + " receives a request " + request.getProtocol() + " from " + request.getSender().getLocalName() + ".");
				if  (request.getProtocol() == ProtocolAcceptor.Propose.getName()) {
					MessageObject propose;
					try {
						propose = (MessageObject) request.getContentObject();
						id = propose.getId();
						topic = propose.getTopic();
						if (this.myagent.getPromiseId(topic) < id){
							accepted(id, request, ProtocolWait.Promise.getName(), topic);
						}
						else {
							refuse(request, topic, id);
						}
					} catch (UnreadableException e) {
						System.err.println("Error when get proposObject send by" + request.getSender().getLocalName() + ".");
					}
				}
				else if  (request.getProtocol() == ProtocolAcceptor.Accept.getName()) {
					MessageObject valueReceived = null;
					id = -1;
					topic = "";
					try {
						valueReceived = (MessageObject) request.getContentObject();
						id = valueReceived.getId();
						topic = valueReceived.getTopic();
						if (valueReceived != null && this.myagent.getPromiseId(topic) <= id){
							this.myagent.setValueLearns(topic, valueReceived.getValue());
							accepted(id, request, ProtocolWait.Accept.getName(), topic);
						}
						else {
							refuse(request, topic, id);
						}
					} catch (UnreadableException e) {
						System.err.println("Error when get MessageObject send by" + request.getSender().getLocalName() + ".");
					}

				}
				request = this.myAgent.receive(msgTemplate);

			}
		}

		private void accepted(int id, ACLMessage request, String protocol, String topic) {
			this.myagent.setPromiseId(topic, id);
			ACLMessage reply = request.createReply();
			reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
			reply.setProtocol(protocol);
			try {
				reply.setContentObject(new MessageObject(topic, this.myagent.getPromiseId(topic), this.myagent.getValueLearns().get(topic)));
				System.out.println(this.myAgent.getLocalName() 
						+ " sends " + protocol + "(" + topic + ", " + id + ") to " + request.getSender().getLocalName() + ".");
				this.myAgent.send(reply);
			} catch (IOException e) {
				System.err.println("Error when send " + protocol + "(" + topic + ", " + id + ") to " + request.getSender().getLocalName() + ".");
			}
		}


		private void refuse(ACLMessage request, String topic, int id) {
			ACLMessage reply = request.createReply();
			reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
			reply.setProtocol("Nak");
			try {
				reply.setContentObject(new MessageObject(topic, id));
				System.out.println(this.myAgent.getLocalName() 
						+ " sends a Nak(" + topic + ", " + id + ") to " + request.getSender().getLocalName() + ".");
				this.myAgent.send(reply);
			} catch (IOException e) {
				System.err.println("Error when send Nak(" + topic + ", " + id + ") to " + request.getSender().getLocalName() + ".");
			}
		}

		@Override
		public boolean done() {
			return false;
		}

	}


	/*******************************************************************
	 * ****************************************************************
	 * 
	 *                    Learner
	 *
	 ********************************************************************
	 ********************************************************************/


	/**
	 * The behaviour used by the learner.
	 * The behaviour is to wait for a request, 
	 * and execute the action associate.
	 * 
	 * @author Axel Foltyn
	 *
	 */
	public class LearnerBehaviour extends CyclicBehaviour {

		private static final long serialVersionUID = 8620116542495436271L;


		final MessageTemplate msgTemplate = MessageTemplate.and(
				MessageTemplate.MatchProtocol("DecideProtocol"), 
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));


		private ILearner myagent;


		/**
		 * 
		 * Return the behaviour of Learner to Paxos consensus.
		 * @param agent : Agent with all ILearner method.
		 * @param isMultiPaxos : boolean to know if we are in basic or multi Paxos.
		 */
		public LearnerBehaviour(ILearner agent, boolean isMultiPaxos) {
			super((Agent) agent);
			this.myagent = (ILearner) agent;
		}

		@Override
		public void action() {
			ACLMessage request = null;
			request = this.myAgent.receive(msgTemplate);

			if (request != null) {
				System.out.println(this.myAgent.getLocalName()+" received a request.");
				try {
					RequestValue value = (RequestValue) request.getContentObject();
					this.myagent.trigger(value);
				} catch (UnreadableException e) {
					System.err.println("Error when get RequestValue from " + request.getSender().getLocalName() + ".");
				}
			}else {
				block();
			}


		}

	}



	/*******************************************************************
	 * ****************************************************************
	 * 
	 *                    Proposer
	 *
	 ********************************************************************
	 ********************************************************************/


	/**
	 * The behaviour used by the proposer.
	 * It's the first step before both phases. The Proposer wait a request.
	 * 
	 * @author Axel Foltyn
	 *
	 */
	public class ListenResquestBehaviour extends Behaviour {
		private static final long serialVersionUID = -3342067813416640026L;
		private IProposer myagent;
		private boolean finished;

		final MessageTemplate msgTemplate = MessageTemplate.and(MessageTemplate.MatchProtocol("requestProtocol"),
				MessageTemplate.MatchPerformative(ACLMessage.REQUEST));


		public ListenResquestBehaviour(IProposer agent) {
			super((Agent) agent);
			this.myagent = agent;
			this.finished= false;
		}

		@Override
		public void action() {
			this.finished= false;
			ACLMessage request = this.myAgent.receive(msgTemplate);
			
			//Also possible to create a tickerBehaviour with a 5s cycle, and if not null, instead of false call stop()
			if (request != null){
				System.out.println(this.myAgent.getLocalName() + " received a request from " + request.getSender().getLocalName() + ".");
				try {
					this.myagent.setValue((RequestValue)request.getContentObject());
				} catch (UnreadableException e) {
					System.err.println("Error when set value send by" + request.getSender().getLocalName() + ".");
				}
				finished = true;
			}else {
				block();
			}	
		}

		@Override
		public boolean done() {
			return finished ;
		}

	}





	/**
	 * The behaviour used by the proposer.
	 * It's the first step before both phases. The Proposer sends a propose request.
	 * 
	 * @author Axel Foltyn
	 *
	 */
	public class ProposeBehaviour extends OneShotBehaviour {
		private static final long serialVersionUID = 6595099943604699395L;
		private IProposer mySpecificAgent;

		public ProposeBehaviour(IProposer agent) {
			super((Agent) agent);
			this.mySpecificAgent = agent;
		}

		@Override
		public void action() {
			String topic = this.mySpecificAgent.getValue().getConcensusTopic();
			System.out.println(this.myAgent.getLocalName()+" starts the Propose part.");
			//increase id
			this.mySpecificAgent.incrementId(topic);
			// Create the message
			final ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			msg.setSender(this.myAgent.getAID());
			msg.setProtocol(ProtocolAcceptor.Propose.getName());
			try {
				List<String> acceptors = this.mySpecificAgent.getAcceptors();
				for(String receiver : acceptors)
					msg.addReceiver(new AID(receiver, AID.ISGUID));
			} catch (FIPAException e) {
				System.err.println(this.myAgent.getLocalName() + " Error when send Propose(" + topic + ", " + this.mySpecificAgent.getId(topic) +") to all acceptors.");
			}  
			try {
				msg.setContentObject(new MessageObject(topic, this.mySpecificAgent.getId(topic)));
				System.out.println(this.myAgent.getLocalName() + " sends Propose(" + topic + ", " + this.mySpecificAgent.getId(topic) +") to all acceptors.");
				this.myAgent.send(msg);
			} catch (IOException e) {
				System.err.println(this.myAgent.getLocalName() + " Error when send Propose(" + topic + ", " + this.mySpecificAgent.getId(topic) +") to all acceptors.");
			}
		}
	}



	// Protocol accepted by the behavior wait of class Protocol.
	public static enum ProtocolWait {
		Promise("PromiseProtocol"), Accept("AcceptProtocol");

		private String name;

		private ProtocolWait(String s){
			name=s;
		}

		public String getName() {
			return name;
		}
	}

	/**
	 * The behaviour used by proposer's behaviour.
	 * The class allows the grouping of acceptor wait behaviours.
	 * 
	 * @author Axel Foltyn
	 *
	 */
	public abstract class WaitAcceptorBehaviour extends OneShotBehaviour {
		private static final long serialVersionUID = 1181097438195597568L;


		private int exitValue = 0; // By default, we return to Propose.


		protected IProposer myagent;


		private ProtocolWait protocol;


		public WaitAcceptorBehaviour(IProposer agent, ProtocolWait protocol) {
			super((Agent) agent);
			this.myagent = agent;
			this.protocol = protocol;
		}

		public RequestValue waitReply() {
			RequestValue majorityValue = null;

			final MessageTemplate msgTemplateOK = MessageTemplate.and(MessageTemplate.MatchProtocol(this.protocol.getName()), MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL));
			final MessageTemplate msgTemplateNak = MessageTemplate.and(MessageTemplate.MatchProtocol("Nak"), MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL));

			int nbAcceptor;
			try {
				nbAcceptor = this.myagent.getAcceptors().size();
			} catch (FIPAException e) {
				exitValue = 0;
				return majorityValue;
			}
			int nbNak = 0;
			int nbOK = 0;

			ACLMessage requestOk;
			ACLMessage requestNak;
			// Semi-passive waiting for a request. Stops when the majority has responded or a lot of timeout.
			while ((nbNak == 0 && nbOK == 0) || (nbNak < nbAcceptor/2 && nbOK < nbAcceptor/2)){
				this.myAgent.doWait(5000);
				requestOk = this.myAgent.receive(msgTemplateOK);
				requestNak = this.myAgent.receive(msgTemplateNak);

				//case of timeout
				if (requestOk == null && requestNak == null) {
					System.out.println(this.myAgent.getLocalName() + " wait 5s without answer.");
					nbNak++;
				}

				// We read all the answers.
				while(requestOk !=null || requestNak != null) {
					if (requestOk !=null){
						switch (protocol) {
						case Accept:
						case Promise:
							try {

								MessageObject reply = (MessageObject) requestOk.getContentObject();
								if (this.myagent.getId(reply.getTopic()) == reply.getId()){
									System.out.println(this.myAgent.getLocalName() + " receives a positive answer from " + requestOk.getSender().getLocalName() + ".");
									if (reply.getValue() != null){
										majorityValue = reply.getValue();
									}
									nbOK++;
								}
							} catch (UnreadableException e) {
								System.err.println(this.myAgent.getLocalName() + " Error when get reply from " + requestOk.getSender().getLocalName() + ".");
							}
							break;

						default:
							System.err.println(this.myAgent.getLocalName() + " not have protocol " + protocol.getName());
							break;
						}


					}
					if (requestNak != null){
						try {
							MessageObject nakObject = (MessageObject) requestNak.getContentObject();
							if (this.myagent.getId(nakObject.getTopic()) == nakObject.getId()) {
								System.out.println(this.myAgent.getLocalName() + " receives a negative answer.");
								nbNak++;
							}
						} catch (UnreadableException e) {
							System.err.println("Error when get reply from " + requestOk.getSender().getLocalName() + ".");
						}

					}
					requestOk = this.myAgent.receive(msgTemplateOK);
					requestNak = this.myAgent.receive(msgTemplateNak);
				}		
			}
			if (nbOK > 0 && nbOK > nbAcceptor/2) {
				exitValue = 1; // We can move on to the next step.
				System.out.println(this.myAgent.getLocalName()+" have enough positive reply.");
			}
			else {
				System.out.println(this.myAgent.getLocalName()+" have enough negative reply.");
				exitValue = 0;
			}
			return majorityValue;
		}

		public int onEnd() {
			return exitValue;
		}

	}


	/**
	 * The behaviour used by the proposer.
	 * It's the last step of first phase.
	 * 
	 * @author Axel Foltyn
	 *
	 */
	public class WaitPromiseBehaviour extends WaitAcceptorBehaviour {
		private static final long serialVersionUID = -7589759402372519627L;


		public WaitPromiseBehaviour(IProposer agent) {
			super(agent, ProtocolWait.Promise);
		}

		@Override
		public void action() {
			RequestValue majorityvalue= waitReply();
			if (majorityvalue != null){
				switch (this.myagent.getPaxos()) {
				case BasicPaxos:
					this.myagent.setValue(majorityvalue);
					break;
				case MultiPaxos:
					this.myagent.concatValue(majorityvalue);
					break;

				default:
					break;
				}

			}
		}

	}


	/**
	 * The behaviour used by the proposer.
	 * It's the second step of last phase. The Proposer wait a accept answer from acceptors.
	 * 
	 * @author Axel Foltyn
	 *
	 */
	public class WaitAcceptBehaviour extends WaitAcceptorBehaviour {

		private static final long serialVersionUID = -5212689597801027997L;


		public WaitAcceptBehaviour(IProposer agent) {
			super(agent, ProtocolWait.Accept);
		}

		@Override
		public void action() {
			RequestValue majorityvalue= waitReply();
			if (majorityvalue != null){
				this.myagent.setValue(majorityvalue);
			}
		}


	}


	/**
	 * The behaviour used by the proposer.
	 * It's the first state of the second phase (The acceptance phase).
	 * 
	 * @author Axel Foltyn
	 *
	 */
	public class AcceptBehaviour extends OneShotBehaviour {

		private static final long serialVersionUID = 1829114996427981143L;
		private IProposer myagent;

		public AcceptBehaviour(IProposer agent) {
			super((Agent) agent);
			this.myagent = agent;
		}

		@Override
		public void action() {
			System.out.println(this.myAgent.getLocalName()+" starts the Accept part.");
			//1°Create the message
			final ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			msg.setSender(this.myAgent.getAID());
			msg.setProtocol(ProtocolAcceptor.Accept.getName());
			List<String> acceptors = new ArrayList<String>();
			try {
				acceptors = this.myagent.getAcceptors();
				for(String receiver : acceptors) {
					msg.addReceiver(new AID(receiver, AID.ISGUID));
				}
			} catch (FIPAException e) {
				System.err.println("Error when send a request to have Acceptor Name.");
			}  
			String topic = this.myagent.getValue().getConcensusTopic();
			int id = this.myagent.getId(topic);
			try {
				msg.setContentObject(new MessageObject(topic, id, this.myagent.getValue()));
				System.out.println(this.myAgent.getLocalName() + " sends Accepte(" + 
						topic + ", "+ id +", "+ this.myagent.getValue().getValue() +") to all acceptor. : ");
				for (String name : acceptors) {
					System.out.println("  - "+name);
				}

				this.myAgent.send(msg);
			} catch (IOException e) {
				System.err.println("Error when send Accepte(" + topic + ", "+ id +", "+ this.myagent.getValue().getValue() +") to all acceptor.");
			}


		}

	}


	/**
	 * The behaviour used by the proposer.
	 * It's the last step of the second phase (The acceptance phase).
	 * 
	 * @author Axel Foltyn
	 *
	 */
	public class DecideBehaviour extends OneShotBehaviour {
		private static final long serialVersionUID = 386554475917136819L;
		private IProposer myagent;

		public DecideBehaviour(IProposer agent) {
			super((Agent) agent);
			this.myagent = agent;
		}

		@Override
		public void action() {
			//1°Create the message
			final ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setSender(this.myAgent.getAID());
			msg.setProtocol("DecideProtocol");
			List<String> learners = new ArrayList<String>();
			try {
				learners = this.myagent.getLearners();
				for(String receiver : learners)
					msg.addReceiver(new AID(receiver, AID.ISGUID));
			} catch (FIPAException e) {
				System.err.println("Error when send a request to have Learner name.");
			}  

			try {
				msg.setContentObject(this.myagent.getValue());
				System.out.println(this.myAgent.getLocalName()+" sends the decision to all learner. : ");
				for (String name : learners) {
					System.out.println("  - "+name);
				}
				this.myAgent.send(msg);
				this.myagent.resetValue();
			} catch (IOException e) {
				System.err.println("Error when send decision to all learner");
			}
		}

	}

}