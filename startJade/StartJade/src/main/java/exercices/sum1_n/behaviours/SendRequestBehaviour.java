package exercices.sum1_n.behaviours;

import java.util.ArrayList;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * This behaviour request values from other agents, sum them in another behaviour and then send the result back to the agents whose sent values.
	 * 
	 * In order to stay relatively simple for now, this behaviour is not fully generic (it is not declared as being part of a protocol) 
	 * and do both the receiving and the sending processes
	 *	 
 * @author hc
 *
 */
public class SendRequestBehaviour extends SimpleBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4061707264077722453L;

	private boolean finished=false;
	
	/**
	 * Number of int to receive
	 */
	private int nbValues;
	
	/**
	 * Number of int received until now
	 */
	private int nbMessagesReceived;
	
	/**
	 * Name of the agent to send the result back
	 */
	private ArrayList<String>  senderNames= new ArrayList<>();
	
	/**
	 * sum
	 */
	private int sum;
	
	
	/**
	 * 
	 * This behaviour request values from other agents, sum them in another behaviour and then send the result back to the agents whose sent values.
	 * 
	 * In order to stay relatively simple for now, this behaviour is not fully generic (it is not declared as being part of a protocol) 
	 * and do both the receiving and the sending processes
	 *	 
	 * @param myagent agent the behaviour belongs to
	 * @param nbvalues number of values the agent will expect to receive
	 * @param resultReceivers list of (local) agent names that might answer the call 
	 */
	public SendRequestBehaviour(final Agent myagent,int nbvalues,ArrayList<String> resultReceivers) {
		super(myagent);
		this.senderNames=resultReceivers;
		this.nbValues = nbvalues;

	}


	@Override
	public void action() {
		//1°Create the message
		final ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.setSender(this.myAgent.getAID());
		for(String sender: this.senderNames) {
			msg.addReceiver(new AID(sender, AID.ISLOCALNAME));  
		}
			
		//2° compute the random value		
		this.myAgent.send(msg);
		this.myAgent.addBehaviour(new SumValuesBehaviour(myAgent, this.nbValues, senderNames));
		this.finished = true;
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return finished;
	}

}
