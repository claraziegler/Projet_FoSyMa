package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;

import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import eu.su.mas.dedaleEtu.mas.behaviours.ShareMapBehaviour;


import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;


/**
 * <pre>
 * This behaviour allows an agent to explore the environment and learn the associated topological map.
 * The algorithm is a pseudo - DFS computationally consuming because its not optimised at all.
 * 
 * When all the nodes around him are visited, the agent randomly select an open node and go there to restart its dfs. 
 * This (non optimal) behaviour is done until all nodes are explored. 
 * 
 * Warning, this behaviour does not save the content of visited nodes, only the topology.
 * Warning, the sub-behaviour ShareMap periodically share the whole map
 * </pre>
 * @author hc
 *
 */
public class ExploCoopBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	private boolean finished = false;

	/**
	 * Current knowledge of the agent regarding the environment
	 */
	private MapRepresentation myMap;

	private List<String> list_agentNames;
	
	
	private String objectif;
	
	private long date;

/**
 * 
 * @param myagent
 * @param myMap known map of the world the agent is living in
 * @param agentNames name of the agents to share the map with
 */
	public ExploCoopBehaviour(ExploreCoopAgent myagent, MapRepresentation myMap,List<String> agentNames,String objectif, long debut) {
		super(myagent);
		this.myMap=myMap;
		this.list_agentNames=agentNames;
		this.objectif = objectif;
		this.date = debut;
		System.out.println("Début de l'exploration de "+myagent.getName());
	}

	@Override
	public void action() {
		
		if(this.myMap==null) {
			this.myMap= new MapRepresentation();
			//this.myAgent.addBehaviour(new ShareMapBehaviour(this.myAgent,500,this.myMap,this.list_agentNames));
		}

		//0) Retrieve the current position
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		if (myPosition!=null){
			if (myPosition.equals(this.objectif)) {
				this.objectif=null;
			}
			//List of observable from the agent's current position
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition

			/**
			 * Just added here to let you see what the agent is doing, otherwise he will be too quick
			 */
			/*try {
				this.myAgent.doWait(500);
			} catch (Exception e) {
				e.printStackTrace();
			}*/
			
			List<Couple<Observation,Integer>> lObservations= lobs.get(0).getRight();
			
			for(Couple<Observation,Integer> o:lObservations){
				switch (o.getLeft()) {
				case DIAMOND:
					 ((ExploreCoopAgent) myAgent).ajouter_diamant(myPosition,o.getRight(),System.currentTimeMillis());
					break;
				case GOLD:
					((ExploreCoopAgent) myAgent).ajouter_or(myPosition,o.getRight(),System.currentTimeMillis());
					break;
				default:
					break;
				}
			}

			//1) remove the current node from openlist and add it to closedNodes.
			this.myMap.addNode(myPosition, MapAttribute.closed);

			//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
			String nextNode=null;
			Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
			while(iter.hasNext()){
				String nodeId=iter.next().getLeft();
				boolean isNewNode=this.myMap.addNewNode(nodeId);
				//the node may exist, but not necessarily the edge
				if (myPosition!=nodeId) {
					this.myMap.addEdge(myPosition, nodeId);
					if (nextNode==null && isNewNode) nextNode=nodeId;
				}
			}
			
			if (this.objectif!=null) {
				//System.out.println(this.myAgent.getName()+" Objectif : "+this.objectif);
				nextNode = this.myMap.getShortestPath(myPosition,this.objectif).get(0);
			}

			//3) while openNodes is not empty, continues.
			if (!this.myMap.hasOpenNode()){
				//Explo finished
				finished=true;
				((ExploreCoopAgent) myAgent).print_or();
				((ExploreCoopAgent) myAgent).print_diamant();
				this.myAgent.addBehaviour(new CollectBehaviour((ExploreCoopAgent) this.myAgent,this.myMap,this.list_agentNames,null,System.currentTimeMillis()));
				System.out.println(this.myAgent.getLocalName()+" - Exploration successufully done, behaviour removed.");
			}else{
				//4) select next move.
				//4.1 If there exist one open node directly reachable, go for it,
				//	 otherwise choose one from the openNode list, compute the shortestPath and go for it
				if (nextNode==null){
					//no directly accessible openNode
					//chose one, compute the path and take the first step.
					nextNode=this.myMap.getShortestPathToClosestOpenNode(myPosition).get(0);//getShortestPath(myPosition,this.openNodes.get(0)).get(0);
					//System.out.println(this.myAgent.getLocalName()+"-- list= "+this.myMap.getOpenNodes()+"| nextNode: "+nextNode);
				}else {
					//System.out.println("nextNode notNUll - "+this.myAgent.getLocalName()+"-- list= "+this.myMap.getOpenNodes()+"\n -- nextNode: "+nextNode);
				}
				
				if (!finished) {
					int cpt=0;
					while(((AbstractDedaleAgent)this.myAgent).moveTo(nextNode)==false && cpt<2) {
						this.myAgent.doWait(500);
						cpt++;
					}
					if (cpt==2) {
						List<String> open_nodes=  this.myMap.getOpenNodes();
						Random r= new Random();
						int moveId=r.nextInt(open_nodes.size());
						String node = open_nodes.get(moveId);
						this.objectif = node;
						System.out.println(this.myAgent.getName()+" Noeud inaccessible, choix d'un nouvel objectif: "+this.objectif);
					}
					
				}
				
				ACLMessage msg1 = new ACLMessage(ACLMessage.INFORM);
				msg1.setProtocol("REPERAGE");
				msg1.setSender(this.myAgent.getAID());
				
				for (String agentName : this.list_agentNames) {
					if (agentName!=this.myAgent.getLocalName()) {
						msg1.addReceiver(new AID(agentName,AID.ISLOCALNAME));
					}
				}
				msg1.setContent("Il y a quelqu'un ?");
				((AbstractDedaleAgent)this.myAgent).sendMessage(msg1);
				this.myAgent.doWait(500);
				
				MessageTemplate msgTemplate1=MessageTemplate.and(
						MessageTemplate.MatchProtocol("REPERAGE"),
						MessageTemplate.MatchPerformative(ACLMessage.INFORM));
				ACLMessage msgReceived1=this.myAgent.receive(msgTemplate1);
				if(msgReceived1 != null) {
					ACLMessage msg2 = new ACLMessage(ACLMessage.INFORM);
					msg2.setProtocol("REPERAGE");
					msg2.setSender(this.myAgent.getAID());
					
					for (String agentName : this.list_agentNames) {
						if (agentName!=this.myAgent.getLocalName()) {
							msg2.addReceiver(new AID(agentName,AID.ISLOCALNAME));
						}
					}
					msg2.setContent("Il y a quelqu'un ?");
					((AbstractDedaleAgent)this.myAgent).sendMessage(msg2);
					
					System.out.println(this.myAgent.getLocalName() + ": " + msgReceived1.getContent() + " par " + msgReceived1.getSender().getLocalName());
					this.myAgent.addBehaviour(new PartageMapBehaviour((ExploreCoopAgent) this.myAgent,this.myMap,msgReceived1.getSender(),this.list_agentNames,this.objectif,this.date));
					System.out.println("Behaviour ajouté");
					finished = true;
					
				}
			
				//4) At each time step, the agent blindly send all its graph to its surrounding to illustrate how to share its knowledge (the topology currently) with the the others agents. 	
				// If it was written properly, this sharing action should be in a dedicated behaviour set, the receivers be automatically computed, and only a subgraph would be shared.
				/*
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setProtocol("SHARE-TOPO");
				msg.setSender(this.myAgent.getAID());
				if (this.myAgent.getLocalName().equals("Explo1")) {
					msg.addReceiver(new AID("Explo2",false));
				}else {
					msg.addReceiver(new AID("Explo1",false));
				}
				SerializableSimpleGraph<String, MapAttribute> sg=this.myMap.getSerializableGraph();
				try {					
					msg.setContentObject(sg);
				} catch (IOException e) {
					e.printStackTrace();
				}
				((AbstractDedaleAgent)this.myAgent).sendMessage(msg);*/

				//5) At each time step, the agent check if he received a graph from a teammate. 	
				// If it was written properly, this sharing action should be in a dedicated behaviour set.
				/*MessageTemplate msgTemplate=MessageTemplate.and(
						MessageTemplate.MatchProtocol("SHARE-TOPO"),
						MessageTemplate.MatchPerformative(ACLMessage.INFORM));
				ACLMessage msgReceived=this.myAgent.receive(msgTemplate);
				if (msgReceived!=null) {
					SerializableSimpleGraph<String, MapAttribute> sgreceived=null;
					try {
						sgreceived = (SerializableSimpleGraph<String, MapAttribute>)msgReceived.getContentObject();
					} catch (UnreadableException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					this.myMap.mergeMap(sgreceived);
				}*/
				/*
				if (!finished) {
					((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
				}*/
				if (System.currentTimeMillis()-this.date>60000) {
					finished =true;
					this.myAgent.addBehaviour(new CollectBehaviour((ExploreCoopAgent) this.myAgent,this.myMap,this.list_agentNames,null,System.currentTimeMillis()));
					System.out.println(this.myAgent.getLocalName()+" - Exploration temporairement arrêtée.");
				}
			}

		}
	}
	
	@Override
	public boolean done() {
		return finished;
	}

}
