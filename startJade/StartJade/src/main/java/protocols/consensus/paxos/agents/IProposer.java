package protocols.consensus.paxos.agents;

import java.util.List;

import jade.domain.FIPAException;
import protocols.consensus.paxos.RequestValue;
import protocols.consensus.paxos.Paxos.PaxosImplemented;

public interface IProposer {

	/**
	 * set the value for this consensus
	 * @param value : value of the consensus.
	 */
	public void setValue(RequestValue value);
	/**
	 * 
	 * @return value of the consensus.
	 */
	public RequestValue getValue();
	/**
	 * 
	 * @return List of acceptors with whom we want to interact.
	 * @throws FIPAException
	 */
	public List<String> getAcceptors() throws FIPAException;

	/**
	 * 
	 * @return List of learner with whom we want to interact.
	 * @throws FIPAException
	 */
	public List<String> getLearners() throws FIPAException;
	
	/**
	 * 
	 * @param learners : List of learner's name with whom we want to interact.
	 * @return
	 */
	public void setLearners(List<String> learners);
	
	/**
	 * 
	 * @param acceptors : List of acceptor's name with whom we want to interact.
	 * @return
	 */
	public void setAcceptors(List<String> acceptors);
	
	
	/**
	 * set the currently paxos.
	 * @param paxos : the paxos currently.
	 */
	public void setPaxos(PaxosImplemented paxos);
	/**
	 * 
	 * @return : the paxos currently.
	 */
	public PaxosImplemented getPaxos();
	
	/**
	 * use to concate the value of proposer and the value learn by acceptors.
	 * @param majorityvalue : the value give by acceptors.
	 */
	public void concatValue(RequestValue majorityvalue);
	/**
	 * 
	 * @param concensusTopic : the topic of the consensus.
	 * @return : The consensus id after incrementing by 1.
	 */
	public int incrementId(String concensusTopic);
	/**
	 * 
	 * @param concensusTopic : the topic of the consensus.
	 * @return : The consensus id
	 */
	public int getId(String concensusTopic);
	/**
	 * Reset the value to null.
	 */
	public void resetValue();
}
