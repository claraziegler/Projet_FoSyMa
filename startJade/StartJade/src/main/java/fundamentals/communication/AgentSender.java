package fundamentals.communication;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * <p>
 * This agent is very simple. 
 * It only possesses a behaviour that allows him to send a single 
 * message to another agent.
 * </p>
 * In a real application,for genericity and clarity, an agent class and 
 * the behaviour classes should not be in the same file.  
 * 
 * @author hc
 *
 */
public class AgentSender extends Agent{

	private static final long serialVersionUID = 1968856210218561967L;
	
	/**
	 * data given to the agent at creation
	 */
	protected List<String> data;
	
	/**
	 * <p>
	 * This method is automatically called when "agent".start() is executed.
	 * Consider that AgentA is launched for the first time. 
	 * </p>
	 * <ul>
	 * 		<li>	1) set the agent attributes 
	 *	 	<li>	2) add the behaviour(s)
	 * </ul>
	 * 
	 */
	protected void setup(){

		super.setup();

		//get the parameters given into the object[] when creating the agent (see the 
		final Object[] args = getArguments();
		if(args[0]!=null){
			data = (List<String>) args[0];
			//these data are currently not used by the agent, its just to show you how to get them if you need it 
		}else{
			System.err.println("Error during parameter transfer");
			System.exit(0);
		}

		//Add the behaviours
		addBehaviour(new SendMessage(this,"Agent1"));

		System.out.println("the sender agent "+this.getLocalName()+ " is started");
		System.err.println("This example will generate errors in the console, its normal. Its to show you the way message content is shared and used.");
		
		
	}

	/**
	 * This method is automatically called after doDelete()
	 */
	protected void takeDown(){

	}

	
	/**************************************
	 * 
	 * 
	 * 				BEHAVIOURS
	 * 
	 * 
	 **************************************/

/**
 * Simple behaviour that only send one message
 * @author hc
 *
 */
	public class SendMessage extends SimpleBehaviour{
		/**
		 * When an agent choose to communicate with others agents in order to reach a precise decision, 
		 * it tries to form a coalition. This behaviour is the first step of the paxos algorithm
		 *  
		 */
		private static final long serialVersionUID = 9088209402507795289L;

		private boolean finished=false;
		private String targetName;
		
		/**
		 * 
		 * @param myagent
		 * @param targetLocalName local name of the agent to send the message to
		 */
		public SendMessage(final Agent myagent,String targetLocalName) {
			super(myagent);
			targetName=targetLocalName;
		}


		public void action() {
			//1) Create a message in order to send it to the choosen agent
			final ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				
			//Not mandatory here, but very useful for real applications
			//msg.setLanguage(MyOntology.LANGUAGE);
			//msg.setOntology(MyOntology.ONTOLOGY_NAME);
			//msg.setProtocol(MyOntology.PROTOCOL_NAME);
			//msg.setConversationId(X);
			
			//2) Set the sender and the receiver
			msg.setSender(this.myAgent.getAID());
			msg.addReceiver(new AID(targetName, AID.ISLOCALNAME)); // In a real application, the agent must receive this knowledge during the initialisation, or through communication with other agents/the DF. 
				
			//3) Set the content of the message.
			//Here I can chose to send some data String OR an object 
			//YOU CANNOT use setContent AND setContObject at the same time.
			//msg.setContent(data.get(0));
			
			//3') If I want to send an object, I can, if it is serializable
			// Here I send the data the agent received during its initialisation. 
			//SEE the console output to understand
			try {
				msg.setContentObject((Serializable) ((AgentSender)this.myAgent).data);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//4) send the message
			this.myAgent.send(msg);
			
			// After the execution of the action() method, this behaviour will be erased from the agent's list of triggerable behaviours.
			this.finished=true; 
			
			System.out.println("----> Message sent to "+msg.getAllReceiver().next()+" ,content= "+msg.getContent());

		}

		public boolean done() {
			return finished;
		}

	}
}
