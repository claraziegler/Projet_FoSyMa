package fundamentals.communication;

import java.util.List;

import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;


/**
 * <p>
 * The receiver agent waits for any inform message and prints the content in the console.
 * That's all.
 * </p>
 * In a real application,for genericity and clarity, an agent class and 
 * the behaviour classes should not be in the same file.  
 * 
 * @author hc
 *
 */
public class AgentReceiver extends Agent{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6327128815533680207L;


	protected void setup(){

		super.setup();

		//get the parameters given into the object[] (here no parameters are expected)
		final Object[] args = getArguments();
		if(args.length!=0){
			System.out.println("Malfunction when creating the receiver agent (AgentB in the example)");

		}

		//Add the behaviours
		addBehaviour(new ReceiveMessage(this));

		System.out.println("the receiver agent "+this.getLocalName()+ " is started");

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
	 * This behaviour allows and agent to receive any message sent to 
	 * him with the inform performative, and print it in the console
	 * 
	 * @author hc
	 *
	 */
	public class ReceiveMessage extends SimpleBehaviour{

		private static final long serialVersionUID = 9088209402507795289L;

		private boolean finished=false;

		/**
		 * Receive only one Inform message and print it
		 * @param myagent
		 */
		public ReceiveMessage(final Agent myagent) {
			super(myagent);

		}


		public void action() {
			//1) Create the template to verify
			final MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);

			//Not used here, but shows how to filter more precisely a message
			//MessageTemplate.and(
			//		MessageTemplate.MatchProtocol(MyOntology.PROTOCOL_NAME),
			//		MessageTemplate.and(
			//				MessageTemplate.MatchLanguage(MyOntology.LANGUAGE_NAME),
			//				MessageTemplate.MatchOntology(MyOntology.ONTOLOGY_NAME)
			//		)
			//)

			//2) retrieves the message from the inbox IF the template corresponds
			final ACLMessage msg = this.myAgent.receive(msgTemplate);

			//3) Process the message
			if (msg != null) {		
				String content=msg.getContent();
				
				System.out.println("<---- Message received from "+msg.getSender().getLocalName()+" ,content= "+content);
				System.out.println("\n The above messages content will generate errors if the content is not a String : \n The content of the message will be written : -- ?? -- \n Or the content of the message will generate :\"-- Missing support for base64 conversions \"--  \n if the common-codec dependency is not satisfied");
				List<String> stringList=null;
				try {
					stringList=(List<String>) msg.getContentObject();
				} catch (UnreadableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("\n To manipulate the content properly the designer of the protocol should \n cast the content in the specified object type before printing it:");
				System.out.println("<---- Message received from "+msg.getSender().getLocalName()+" ,contentObject= "+stringList);
				System.out.println("See the ReceivedMessageBehaviour of AgentReceiver.java in the fundamentals>communication package for more details");
				this.finished=true;
			}else{
				System.out.println("Receiver - No message received for now");

				//the behaviours goes to sleep until a new message arrives in the inbox. 
				//Whithout the use of block(), the behaviour is spinning (never a good idea)
				block();
			}
		}

		public boolean done() {
			return finished;
		}

	}

}
