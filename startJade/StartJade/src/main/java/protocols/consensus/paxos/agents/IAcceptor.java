package protocols.consensus.paxos.agents;

import java.util.HashMap;

import protocols.consensus.paxos.RequestValue;

public interface IAcceptor{

	/**
	 * @param topic : the topic of consensus. 
	 * @return : the id promise for this topic.
	 */
	public int getPromiseId(String topic);

	/**
	 * set the id promise of the this topic
	 * 
	 * @param topic : the topic of consensus.
	 * @param promise_id : the id promise for this topic.
	 */
	public void setPromiseId(String topic, int promise_id);

	/**
	 * @return : the map<topic, value>
	 */
	public HashMap<String, RequestValue> getValueLearns();

	/**
	 * @param topic : the topic of consensus.
	 * @param valueLearn : value learn for this topic
	 */
	public void setValueLearns(String topic, RequestValue valueLearn);
}
