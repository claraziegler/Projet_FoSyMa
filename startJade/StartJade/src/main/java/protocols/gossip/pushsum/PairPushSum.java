package protocols.gossip.pushsum;

import java.io.Serializable;

/**  a structure used to send  the pair (sum,weight) **/
public class PairPushSum implements Serializable{
	
	private static final long serialVersionUID = -5054403761935204553L;
	private float sum;
	private float weight;
	private String agentName; 
	/**
	 * 
	 * @param sum :the sum specific to each agent 
	 * @param weight:the weight specific to each agent 
	 * @param agentName : an optional parameter 
	 */
	public PairPushSum(float sum, float weight,String agentName) {
		super();
		this.sum = sum;
		this.weight = weight;
		this.agentName=agentName;
	}

	public float getSum() {
		return sum;
	}

	public void setSum(float sum) {
		this.sum = sum;
	}

	public float getWeight() {
		return weight;
	}

	public void setWeight(float weight) {
		this.weight = weight;
	}

	public String getAgentName() {
		return agentName;
	}

	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}
	
	
	

}
