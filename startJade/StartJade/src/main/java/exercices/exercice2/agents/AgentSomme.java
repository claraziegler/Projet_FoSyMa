package exercices.exercice2.agents;

import exercices.exercice2.behaviours.SumNbReceivedValuesBehaviourA;
import jade.core.Agent;


/**
 * This agent possess the SUM capability.
 * It expects to receive K values from agentA, sum them and reply with the result
 * The number of messages to receive as well as the sender's name are hardcoded bellow
 * 
 * @author hc
 *
 */
public class AgentSomme extends Agent{

	private static final long serialVersionUID = 3499482209671348272L;

	@Override
	protected void setup(){

		super.setup();
		
		//get the parameters given into the object[]
		final Object[] args = getArguments();
		if(args.length!=0){
			System.err.println("Error while creating the sum agent");
			System.exit(-1);

		}else{

			//Add the behaviours
			addBehaviour(new SumNbReceivedValuesBehaviourA(this, 2,"AgentA"));// both the number of values and the name of the agent to reply are hardcoded, its bad
			addBehaviour(new SumNbReceivedValuesBehaviourA(this, 3,"AgentC"));
			
			System.out.println("The agent possessing the SUM capability "+this.getLocalName()+ " is started");
		}
	}

	/**
	 * This method is automatically called after doDelete()
	 */
	protected void takeDown(){

	}
}
