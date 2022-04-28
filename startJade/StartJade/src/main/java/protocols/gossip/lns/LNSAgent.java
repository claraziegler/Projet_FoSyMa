package protocols.gossip.lns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class LNSAgent extends Agent{
	private static final long serialVersionUID = -4088190833611481047L;
	private List<String> neighbours;
	private Map<String, String> secrets;
	private String receiver;
	private int idCall = -1;
	private boolean busy = false; 
	private String conversationId;
	private boolean expert = false;
	private int nbAgents;
	private boolean callDone;
	protected void setup(){
		super.setup();
		
		final Object[] args = getArguments();

		secrets = new HashMap<String, String>();
		secrets.put(this.getLocalName(), (String) args[0]);
		this.neighbours = (List<String>) args[1];
		this.nbAgents = (int) args[2];
		LearnNewSecrets lns = new LearnNewSecrets();
		for(Behaviour b: lns.getBehaviours(null, this, neighbours)) {
			addBehaviour(b);
		}
		
	}
	
	

	
	public boolean isBusy() {
		return busy;
	}


	public void setBusy(boolean busy) {
		this.busy = busy;
	}


	public Map<String, String> getSecrets() {
		return secrets;
	}
	
	public void addSecrets(Map<String, String> secrets) {
		for(String agent: secrets.keySet()) {
			if(!this.secrets.containsKey(agent))
				this.secrets.put(agent, secrets.get(agent));
		}
	}
	public int getIdCall() {
		return idCall;
	}
	public void setIdCall(int idCall) {
		this.idCall = idCall;
	}
	public List<String> getNeighbours() {
		return neighbours;
	}
	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}
	public String getReceiver() {
		// TODO Auto-generated method stub
		return receiver;
	}


	public List<String> getUnknownSecrets() {
		List<String> unknownSecrets = new ArrayList<String>();
		// TODO Auto-generated method stub
		for(String neighbour: neighbours) {
			if(!secrets.containsKey(neighbour)) {
				unknownSecrets.add(neighbour);
			}
		}
		return unknownSecrets;
	}




	public String getConversationId() {
		// TODO Auto-generated method stub
		return this.conversationId;
	}




	public void setConversationId(String conversationId) {
		this.conversationId = conversationId;
	}



	public void addToYellowPage(String service) {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(this.getAID()); 
		ServiceDescription sd = new
				ServiceDescription();
		sd.setType(service); 
		sd.setName(this.getLocalName());
		dfd.addServices(sd) ;
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace(); 
		}
	}
	
	public boolean endLNS() {
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription () ;
		sd .setType("Expert"); // name of the service
		dfd.addServices(sd) ;
		try {
			DFAgentDescription[] result = DFService.search(this, dfd) ;
			return result.length == this.nbAgents;
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		return false;

	}
	
	public void setExpert(boolean exp) {
		this.expert = exp;
	}
	public boolean isExpert() {
		// TODO Auto-generated method stub
		return this.expert;
	} 

	public String printSecrets() {
		StringBuilder secretsString = new StringBuilder("\t");
		for (Map.Entry<String, String> s: secrets.entrySet()) {
			secretsString.append(s.getValue()+", ");
		}
		return secretsString.toString();
	}

	public String printNeighbours() {
		StringBuilder neighString = new StringBuilder("\t");
		for(String neighbour: neighbours) {
			neighString.append(neighbour+", ");
		}
		return neighString.toString();

	}



	public boolean callDone() {
		return callDone;
	}




	public void setCallDone(boolean callDone) {
		this.callDone = callDone;
	}
}


