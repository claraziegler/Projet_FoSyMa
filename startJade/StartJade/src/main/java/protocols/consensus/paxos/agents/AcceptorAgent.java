package protocols.consensus.paxos.agents;

import java.util.HashMap;
import java.util.List;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import protocols.Role;
import protocols.auctions.IAuction;
import protocols.consensus.paxos.RequestValue;
import tools.YellowPage;

/**
 * The agent will accept values, where a value is chosen if a majority accept.
 * It send a promise for a propose and an accept for an accept request.
 * 
 * @author Axel Foltyn
 *
 */
public class AcceptorAgent extends Agent implements IAcceptor {

	private static final long serialVersionUID = -6427842134364937916L;

	private HashMap<String, RequestValue> valueLearns = new HashMap<String, RequestValue>();
	private HashMap<String, Integer> promiseIds  = new HashMap<String, Integer>();

	@Override
	protected void setup(){
		super.setup();

		YellowPage.add_yellow_page(this, "Acceptor");

		final Object[] args = getArguments();

		List<String> agentsNames = null;
		List<Behaviour> behaviours = ((IAuction) args[0]).getBehaviours(Role.Acceptor, this,  agentsNames);
		for(Behaviour beh: behaviours) {
			this.addBehaviour(beh);
		}
	}

	@Override
	protected  void takeDown(){ 
		super.takeDown();
		YellowPage.remove_yellow_page(this, "Acceptor");
	}

	@Override
	public int getPromiseId(String topic) {
		if (promiseIds.containsKey(topic)){
			return promiseIds.get(topic);
		}
		return 0;
	}

	@Override
	public void setPromiseId(String topic, int promise_id) {
		this.promiseIds.put(topic, promise_id);
	}

	@Override
	public HashMap<String, RequestValue> getValueLearns() {
		return valueLearns;
	}

	@Override
	public void setValueLearns(String topic, RequestValue valueLearn) {
		this.valueLearns.put(topic, valueLearn);

	}


}
