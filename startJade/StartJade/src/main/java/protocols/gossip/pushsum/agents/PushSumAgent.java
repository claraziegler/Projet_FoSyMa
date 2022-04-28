package protocols.gossip.pushsum.agents;
import java.util.List;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import protocols.Role;
import protocols.gossip.pushsum.PairPushSum;
import protocols.gossip.pushsum.PushSumProtocol;

public class PushSumAgent extends Agent {
	private static final long serialVersionUID = -4088190833611481047L;
	/**
	 * neighbors of agent
	 */
	private List<String> neighbors;
	/**
	 * the initial value of the agent 
	 */
	  
	private int value ;
	/**
	 * object pair initially  equal to (value,1)
	 */
	private PairPushSum pair;
	
	
	
	@SuppressWarnings("unchecked")
	protected void setup(){
		super.setup();
		final Object[] args = getArguments();
		this.neighbors = (List<String>) args[0];
		this.value = (int) args[1];
		this.pair =new PairPushSum(this.value,1,"");
		PushSumProtocol pushSum = new PushSumProtocol ();
		for(Behaviour b: pushSum.getBehaviours(Role.PushSum,this, neighbors)) {
			addBehaviour(b);
		}
		
	}
	public List<String> getNeighbors() {
		return neighbors;
	}
	public void setNeighbours(List<String> neighbors) {
		this.neighbors = neighbors;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}

	public PairPushSum getPair() {
		return pair;
	}

	public void setPair(PairPushSum pair) {
		this.pair = pair;
	}
		
	

}
