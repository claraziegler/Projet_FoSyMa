package protocols.consensus.paxos;


/**
 * @startuml
 * Client->>Proposer1: request(val1)
 *   Client->>Proposer2: request(val2)
 *   Proposer1->>Acceptor1:Propose(1)
 *   note over Acceptor1: The first Proposer is always accepted.
 *   Acceptor1->>Proposer1:Promise(1, null)
 *   Proposer2->>Acceptor1:Propose(1)
 *   Proposer2->>Acceptor2:Propose(1)
 *   note over Acceptor2: The first Proposer is always accepted.
 *   Acceptor2->>Proposer2:Promise(1, null)
 *   Acceptor1->>Proposer2:Nak(1)
 *   Proposer2->>Acceptor1:Propose(2)
 *   Proposer1->>Acceptor2:Propose(1)
 *  Acceptor2->>Proposer1:Nak(1)
 *   Proposer2->>Acceptor2:Propose(2)
 *   Acceptor2->>Proposer2:Promise(2, null)
 *   Proposer2->>Acceptor1:Accept(2, val2)
 *   Proposer2->>Acceptor2:Accept(2, val2)
 *   Acceptor2->>Proposer2:Accepted(2, val2)    
 *   Acceptor1->>Proposer2:Accepted(2, val2)
 *   Proposer2->>Leaner:Accept(2, val2)
 *   Leaner->>Client:res2
 *   note over Client: res2 is the answer of the request(val2).
 *   note over Proposer1: Continue for the consensus of the request(val1).
 *   Proposer1->>Acceptor1:Propose(3)
 *   Proposer1->>Acceptor2:Propose(3)
 *   Acceptor2->>Proposer1:Promise(3, val2)    
 *   Acceptor1->>Proposer1:Promise(3, val2)
 *   Proposer1->>Acceptor1:Accept(3, val2)
 *   Proposer1->>Acceptor2:Accept(3, val2)
 *   Acceptor2->>Proposer1:Accepted(3, val2)    
 *   Acceptor1->>Proposer1:Accepted(3, val2)
 *   Proposer1->>Leaner:Accept(2, val2)
 *   Leaner->>Client:res2
 *   note over Client: res2 is the answer of the request(val2).
 * @enduml
 */
/**
 * MultiPaxos has the same roles as BasicPaxos, 
 * what changes is that instead of having a consensus 
 * for a single value, it is done for a series of values.
 * 
 * @author axel foltyn
 *
 */
public class MultiPaxos extends Paxos {

	public MultiPaxos() {
		super(PaxosImplemented.MultiPaxos);
	}

}
