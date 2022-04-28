package exercices.exercice2.agents;

import exercices.exercice2.behaviours.SendNbValuesBehaviour;
import exercices.exercice2.behaviours.ReceiveMessageBehaviour;
import jade.core.Agent;



/*Agent A sends 2 values to AgentSomme*/

public class AgentA extends Agent{
	
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
			
		addBehaviour(new SendNbValuesBehaviour(this,2,"AgentSomme"));// Both the name of the agent to contact and the number of values to send are hardcoded, its bad.
		addBehaviour(new ReceiveMessageBehaviour(this));

		System.out.println("The sender agent "+this.getLocalName()+ " is started");
		
		}
	
	}
	
}

