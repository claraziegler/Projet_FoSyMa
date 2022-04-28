package fundamentals.migration;

import jade.core.Agent;

/**
 * This agent oscillates between two intra-platform containers whose coordinate and name are harcoded (its bad).
 * 
 * @author hc
 *
 */
public class DummyMovingAgent extends Agent {
	private static final long serialVersionUID = -5686331366676803589L;
	
	protected void setup(){//Automatically called at agent’s creation
		super.setup();
		addBehaviour(new OscillationBehaviour(this,"127.0.0.1","8888",2000));
	}
	
	protected void beforeMove(){//Automatically called before doMove()
		super.beforeMove();
		System.out.println(this.getLocalName()+": I migrate ");
	}
	
	protected void afterMove(){//Automatically called after doMove()
		super.afterMove();
		System.out.println("I migrated");
	}
}