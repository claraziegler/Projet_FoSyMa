package exercices.sum1_n.behaviours;

import java.util.Random;


import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Used to generate and send values to requesting agent(s)
 * @author hc
 *
 */
public class SendValuesBehaviour extends SimpleBehaviour {
	private static final long serialVersionUID = 9088209402507795289L;

	private boolean finished=false;
	/**
	 * number of values to send
	 */
	private int nbValues;
	
	/**
	 * number of values already sent
	 */
	private int nbMessagesSent=0;
	
	/**
	 * Name of the agent that should receive the values
	 */
	private String receiverName;
	
	Random r;

	/**
	 * 
	 * @param myagent the Agent this behaviour is linked to
	 * @param receiverName The local name of the receiver agent
	 */
	public SendValuesBehaviour(final Agent myagent, String receiverName) {
		super(myagent);
		this.receiverName=receiverName;
		this.r= new Random();
		

	}


	public void action() {
		
		
		//1°Create the message
		final ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setSender(this.myAgent.getAID());
		msg.addReceiver(new AID(this.receiverName, AID.ISLOCALNAME));  
			
		//2° compute the random value		
		msg.setContent(((Integer)r.nextInt(100)).toString());
		this.myAgent.send(msg);
		nbMessagesSent++;

		System.out.println(this.myAgent.getLocalName()+" ----> Message number "+this.nbMessagesSent+" sent to "+this.receiverName+" ,content= "+msg.getContent());

	}

	public boolean done() {
		return finished;
	}
	
	/**
	 * Set the behaviour to finished so that it can automatically call the done() methods
	 */
	public void setfinished() {
		this.finished = true;
		System.out.println(this.myAgent.getLocalName()+"cancel received");
	}
}
