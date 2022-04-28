package exercices.sum1_n.behaviours;

import java.util.ArrayList;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * Realise the sum
 * @author hc
 *
 */
public class SumValuesBehaviour extends SimpleBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8805558491286629677L;

	private boolean finished=false; 
	
	/**
	 * Number of int to receive
	 */
	private final int nbValues;
	
	/**
	 * Number of int received until now
	 */
	private int nbMessagesReceived;
	
	/**
	 * Name of the agent to send the result back
	 */
	private ArrayList <String> senders= new ArrayList<>();
	
	/**
	 * sum
	 */
	private int sum;
	
	
	/**
	 * 
	 * This behaviour receives nb integer values, sum them and then send the result back to the agent whose name is given in parameters.
	 * 
	 * In order to stay relatively simple for now, this behaviour is not fully generic (it is not declared as being part of a protocol) 
	 * and do both the receiving and the sending processes
	 *	 
	 * @param myagent agent the behaviour belongs to
	 * @param nbValues number of values the agent will expect to receive
	 * @param receivers list of (local) agent names that might answer the call 
	 */
	public SumValuesBehaviour(final Agent myagent,int nbValues,ArrayList<String>  receivers) {
		super(myagent);
		this.nbValues=nbValues;
		this.nbMessagesReceived=0;
		this.senders=receivers;
		this.sum=0;

	}


	public void action() { 
		
		//1) create the reception template (inform + name of the sender)
		final MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);

		//2) get the message
		final ACLMessage msg = this.myAgent.receive(msgTemplate);
		
		if (msg != null) {		
			
			System.out.println(this.myAgent.getLocalName()+ "<----Message received from "+msg.getSender().getLocalName()+" ,content= "+msg.getContent());
			
			//add the value to the counter
			this.sum=this.sum+(Integer.parseInt(msg.getContent()));
			this.nbMessagesReceived++;
			System.out.println("================" +this.nbMessagesReceived +"======================");
			if(this.nbMessagesReceived>=this.nbValues){
				//nb values to process reached
				
				//send the result back
				final ACLMessage msgResult = new ACLMessage(ACLMessage.CANCEL);
				msgResult.setSender(this.myAgent.getAID());
				for(String senderName:this.senders) {
					msgResult.addReceiver(new AID(senderName, AID.ISLOCALNAME));  	
				}
				this.myAgent.send(msgResult);
				
				System.out.println("SUM == "+ this.sum);
				this.finished=true;
			}
			
			
		}else{
			//block the behaviour until the next message
			System.out.println("No message received, the behaviour "+this.getBehaviourName()+ "goes to sleep");
			block();
		}
	}

	public boolean done() { 
		return finished;
	}

}
