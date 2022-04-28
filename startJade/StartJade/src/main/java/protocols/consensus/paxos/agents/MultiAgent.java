package protocols.consensus.paxos.agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import protocols.Role;
import protocols.auctions.IAuction;
import protocols.consensus.paxos.RequestValue;
import protocols.consensus.paxos.Paxos.PaxosImplemented;
import tools.YellowPage;

public class MultiAgent extends Agent implements IAcceptor, IProposer, ILearner{

	private static final long serialVersionUID = 1651352445382728071L;



	private AcceptorAgent acceptorAgent;
	private ProposerAgent proposerAgent;
	private ClientAgent clientAgent;
	private LearnerAgent learnerAgent;
	private List<String> services = new ArrayList<String>();


	private PaxosImplemented paxos;



	private List<String> learners;



	private List<String> acceptors;
	
	@Override
	protected void setup(){
		final Object[] args = getArguments();
		
		List<String> agentsNames = null;
		List<Behaviour> behaviours = ((IAuction) args[0]).getBehaviours(Role.Multi_Role, this,  agentsNames);
		for(Behaviour beh: behaviours) {
			this.addBehaviour(beh);
		}
		YellowPage.add_yellow_page(this, services);
	}

	public void create(Role role) {
		if (role == Role.Acceptor && acceptorAgent == null) {
			acceptorAgent = new AcceptorAgent();
			this.services.add("Acceptor");
			System.out.println("Acceptor in " + getLocalName() + " created.");
		}
		if (role == Role.Learner && learnerAgent == null) {
			learnerAgent = new LearnerAgent();
			this.services.add("Learner");
			System.out.println("Learner in " + getLocalName() + " created.");
		}
		if (role == Role.Client && clientAgent == null) {
			clientAgent = new ClientAgent();
			System.out.println("Client in " + getLocalName() + " created.");
		}
		if (role == Role.Proposer && proposerAgent == null) {
			proposerAgent = new ProposerAgent();
			System.out.println("Proposer in " + getLocalName() + " created.");
		}
	}

	@Override
	protected  void takeDown(){ 
		super.takeDown();
		if (acceptorAgent != null) {
			YellowPage.remove_yellow_page(this, "Acceptor");
		}
		if (learnerAgent != null) {
			YellowPage.remove_yellow_page(this, "Learner");
		}
		
	}


	@Override
	public int getPromiseId(String topic) {
		return acceptorAgent.getPromiseId(topic);
	}


	@Override
	public void setPromiseId(String topic, int promise_id) {
		acceptorAgent.setPromiseId(topic, promise_id);
	}


	@Override
	public HashMap<String, RequestValue> getValueLearns() {
		return acceptorAgent.getValueLearns();
	}


	@Override
	public void setValueLearns(String concensusThem, RequestValue valueLearn) {
		acceptorAgent.setValueLearns(concensusThem, valueLearn);
	}

	@Override
	public void setValue(RequestValue value) {
		proposerAgent.setValue(value);	
	}


	@Override
	public RequestValue getValue() {
		return proposerAgent.getValue();
	}

	@Override
	public List<String> getAcceptors() throws FIPAException {
		if (acceptors != null){
			return acceptors;
		}
		System.out.println(this.getLocalName()+ " uses a yellow page to know acceptors.");
		return YellowPage.list_of_agent(this, "Acceptor");
	}


	@Override
	public List<String> getLearners() throws FIPAException {
		if (learners != null){
			return learners;
		}
		System.out.println(this.getLocalName()+ " uses a yellow page to know learners.");
		return YellowPage.list_of_agent(this, "Learner");
	}
	
	@Override
	public void setLearners(List<String> learners) {
		this.learners = learners;
	}

	@Override
	public void setAcceptors(List<String> acceptors) {
		this.acceptors = acceptors;
	}

	@Override
	public void setPaxos(PaxosImplemented paxos) {
		this.paxos = paxos;
		if (proposerAgent !=null){
			proposerAgent.setPaxos(paxos);
		}
	}


	@Override
	public PaxosImplemented getPaxos() {
		return proposerAgent.getPaxos();
	}


	@Override
	public void concatValue(RequestValue majorityvalue) {
		proposerAgent.concatValue(majorityvalue);
	}


	@Override
	public int incrementId(String concensusTopic) {
		return proposerAgent.incrementId(concensusTopic);
	}


	@Override
	public int getId(String concensusTopic) {
		return proposerAgent.getId(concensusTopic);
	}


	@Override
	public void resetValue() {
		proposerAgent.resetValue();

	}


	@Override
	public void trigger(RequestValue value) {
		final ACLMessage answer = new ACLMessage(ACLMessage.INFORM);
		answer.setSender(this.getAID());
		answer.setProtocol("AnswerProtocol");
		if (paxos == PaxosImplemented.MultiPaxos) {
			List<String> clients = value.getClients();
			List<String> values = value.getValues();
			System.out.println("List of action by client");
			AID client = null;
			for (int i = 0; i < clients.size(); i++) {
				client = new AID(clients.get(i), AID.ISGUID);
				System.out.println("client : " + 
						client.getLocalName() + " val : " + values.get(i) + ".");
			}
			answer.addReceiver(client);
			answer.setContent(values.get(values.size() - 1));
			System.out.println(this.getLocalName()+" sends the an answer to "+ client.getLocalName() + ".");
		}
		else {
			AID client = new AID(value.getClient(), AID.ISGUID);
			answer.addReceiver(client);
			answer.setContent(value.getValue());
			System.out.println(this.getLocalName()+" sends an answer to "+ client.getLocalName() + ".");
		}
		
		this.send(answer);
	}
}
