package protocols.consensus.paxos.agents;


import java.util.HashMap;
import java.util.List;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.FIPAException;
import protocols.Role;
import protocols.auctions.IAuction;
import protocols.consensus.paxos.RequestValue;
import protocols.consensus.paxos.Paxos.PaxosImplemented;
import tools.YellowPage;

/**
 * The agent will receive the user's request, and who will try to get the consensus of the acceptors.
 * 
 * @author Axel Foltyn
 *
 */
public class ProposerAgent extends Agent implements IProposer {
	
	private static final long serialVersionUID = -6571665198397883223L;
	// id of each topic
	private HashMap<String, Integer> ids = new HashMap<String, Integer>();
	private RequestValue value;
	private PaxosImplemented paxos;

	// If not initially set, they will be retrievable by yellow page.
	private List<String> acceptors = null;
	private List<String> learners = null;
	



	protected void setup(){
		super.setup();
		
		final Object[] args = getArguments();

		if (args.length > 1) {
			acceptors = (List<String>) args[1];
		}
		if (args.length > 2) {
			learners = (List<String>) args[2];
		}
		List<String> agentsNames = null;
		List<Behaviour> behaviours = ((IAuction) args[0]).getBehaviours(Role.Proposer, this,  agentsNames);
		for(Behaviour beh: behaviours) {
			this.addBehaviour(beh);
		}
	}



	public void setValue(RequestValue value) {
		if (this.value !=null){
			value.setClient(this.value.getClient());
		}
		this.value = value;
	}

	public RequestValue getValue() {
		return value;
	}


	public List<String> getAcceptors() throws FIPAException {
		if (acceptors != null){
			return acceptors;
		}
		System.out.println(this.getLocalName()+ " uses a yellow page to know acceptors.");
		return YellowPage.list_of_agent(this, "Acceptor");
	}
	
	public List<String> getLearners() throws FIPAException {
		if (learners != null){
			return learners;
		}
		System.out.println(this.getLocalName()+ " uses a yellow page to know learners.");
		return YellowPage.list_of_agent(this, "Learner");
	}


	public void setPaxos(PaxosImplemented paxos) {
		this.paxos = paxos;
	}


	public PaxosImplemented getPaxos() {
		return paxos;
	}


	public void concatValue(RequestValue majorityvalue) {
		majorityvalue.addValue(value);
		this.value = majorityvalue;
		
	}


	public int incrementId(String concensusTopic) {
		if (!ids.containsKey(concensusTopic)) {
			ids.put(concensusTopic, 1); // Not 0 to avoid comparison with null.
		}
		else {
			ids.put(concensusTopic, ids.get(concensusTopic)+1);
		}
		return ids.get(concensusTopic);
	}


	public int getId(String concensusTopic) {
		return ids.get(concensusTopic);
	}


	public void resetValue() {
		this.value = null;
	}



	@Override
	public void setLearners(List<String> learners) {
		this.learners = learners;
	}



	@Override
	public void setAcceptors(List<String> acceptors) {
		this.acceptors = acceptors;
	}
	
}
