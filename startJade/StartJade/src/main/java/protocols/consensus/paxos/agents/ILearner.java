package protocols.consensus.paxos.agents;


import protocols.consensus.paxos.RequestValue;
import protocols.consensus.paxos.Paxos.PaxosImplemented;

public interface ILearner {

	/**
	 * Action to be taken when a request is received.
	 * 
	 * @param value : the value of the request
	 */
	public void trigger(RequestValue value);

	/**
	 * 
	 * @param paxos : paxos use currently.
	 */
	public void setPaxos(PaxosImplemented paxos);

}
