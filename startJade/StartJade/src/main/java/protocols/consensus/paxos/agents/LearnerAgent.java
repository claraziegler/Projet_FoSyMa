package protocols.consensus.paxos.agents;


import java.util.List;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import protocols.Role;
import protocols.auctions.IAuction;
import protocols.consensus.paxos.RequestValue;
import protocols.consensus.paxos.Paxos.PaxosImplemented;
import tools.YellowPage;



/**
 * The agent will save the action log, 
 * execute it and transmit the response of the request to the user.
 * 
 * @author Axel Foltyn
 * 
 */
public class LearnerAgent extends Agent implements ILearner{
	
	private static final long serialVersionUID = -1793608002691443344L;
	
	
	private PaxosImplemented paxos;
	
	
	protected void setup(){
		super.setup();
		
		YellowPage.add_yellow_page(this, "Learner");
		
		final Object[] args = getArguments();

		List<String> agentsNames = null;
		List<Behaviour> behaviours = ((IAuction) args[0]).getBehaviours(Role.Learner, this,  agentsNames);
		for(Behaviour beh: behaviours) {
			this.addBehaviour(beh);
		}
	}
	
	protected  void takeDown(){ 
		YellowPage.remove_yellow_page(this, "Learner");
	}
	
	/**
	 * Action to be taken when a request is received.
	 * 
	 * @param value : the value of the request
	 */
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

	public void setPaxos(PaxosImplemented paxos) {
		this.paxos = paxos;
	}

}
