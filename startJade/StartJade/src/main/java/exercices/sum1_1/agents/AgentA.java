package exercices.sum1_1.agents;

import exercices.sum1_1.behaviours.ReceiveMessageBehaviour;
import exercices.sum1_1.behaviours.SendNbValuesBehaviour;
import jade.core.Agent;


/**
 *  AgentA sends 10 values to AgentSum then wait for AgentSum to send back the result of its computation (the sum).
 *  The number of messages to send as well as the agent receiving them is hardcoded bellow 
 * 
 * @author hc
 *
 */
public class AgentA extends Agent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1410401284578853709L;

	protected void setup(){

		super.setup();

		//get the parameters given into the object[]
		final Object[] args = getArguments();
		if(args.length!=0){
			System.err.println("Malfunction - no parameter expected");
			System.exit(-1);
		}else{
			
		//Add the behaviours
			
		addBehaviour(new SendNbValuesBehaviour(this,10,"AgentSUM"));// Both the name of the agent to contact and the number of values to send are hardcoded, its bad.
		addBehaviour(new ReceiveMessageBehaviour(this));

		System.out.println("The sender agent "+this.getLocalName()+ " is started");
		
		}
	}

	/**
	 * This method is automatically called after doDelete()
	 */
	@Override
	protected void takeDown(){

	}

}
