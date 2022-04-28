package protocols;
import java.util.List;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
public interface IProtocol {

		public abstract List<Behaviour> getBehaviours(Role role,Agent agent ,List<String> listagents );
		
	

}
