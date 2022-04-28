package exercices.sum1_n.behaviours;

import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveMessageBehaviour extends SimpleBehaviour{


	private boolean finished=false;
	private SendValuesBehaviour b ;
	
	/**
	 * 
	 * This behaviour is a one Shot.
	 * It receives a message tagged with an inform performative, print the content in the console and destroy itlself
	 * @param myagent agent the behaviour belongs to
	 */
	public ReceiveMessageBehaviour(final Agent myagent) {
		super(myagent);
	}


	public void action() {
		//1) receive the message
		final MessageTemplate msgTemplate1 = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);			
		final MessageTemplate msgTemplate2 = MessageTemplate.MatchPerformative(ACLMessage.CANCEL);			

		final ACLMessage msg1 = this.myAgent.receive(msgTemplate1);
		final ACLMessage msg2 = this.myAgent.receive(msgTemplate2);

		if (msg2 != null) {		
			this.b.setfinished();
			this.finished=true;
		}else{
			if(msg1 != null) {
				b = new SendValuesBehaviour(myAgent, msg1.getSender().getLocalName());
				this.myAgent.addBehaviour(b);
			}
			block();// the behaviour goes to sleep until the arrival of a new message in the agent's Inbox.
		}
	}

	public boolean done() {
		return finished;
	}

}
