package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Set;

import dataStructures.serializableGraph.SerializableNode;
import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;


public class CollectBehaviour extends SimpleBehaviour {
	
	private MapRepresentation myMap;
	//private AID receivers;
	private int cpt;
	private List<String> agentNames;
	private String objectif;
	private long date;
	private boolean finished = false;
	

	
	public CollectBehaviour(ExploreCoopAgent myAgent,MapRepresentation mymap,List<String> agentNames,String objectif,long debut) {
		super();
		this.myMap=mymap;
		//this.receivers=receivers;	
		this.cpt=0;
		this.agentNames = agentNames;
		this.objectif = objectif;
		System.out.println("Debut collecte behaviour : "+myAgent.getName());
		this.date = debut;
		
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -568863390879327961L;
	
	
	

	@Override
	public void action() {
		if (this.objectif == null) {
			
			if (((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace().get(0).getRight()==0) {
				SerializableSimpleGraph<String, MapAttribute> sg=this.myMap.getSerializableGraph();
				Set<SerializableNode<String, MapAttribute>> nodes = sg.getAllNodes();
				
				Random r= new Random();
				int moveId=r.nextInt(nodes.size());
				int i = 0;
				for (SerializableNode<String, MapAttribute> n: nodes) {
					if (i==moveId) {
						System.out.println("Sac à dos plein, noeud aleatoire choisi par"+this.myAgent.getName() +" : "+n.getNodeId());
						this.objectif = n.getNodeId();
						break;
					}
					i++;
				}
			}
			
			else {
				if (((AbstractDedaleAgent) myAgent).getMyTreasureType()==Observation.GOLD) {
					this.objectif = ((ExploreCoopAgent) myAgent).choisirTresor("or");
				}
				else if (((AbstractDedaleAgent) myAgent).getMyTreasureType()==Observation.DIAMOND){
					this.objectif = ((ExploreCoopAgent) myAgent).choisirTresor("diamant");
				}
				else {
					if (this.myMap.hasOpenNode()) {
						Random r = new Random();
						int nb = r.nextInt(9);
						if (nb<5) {
							this.objectif = ((ExploreCoopAgent) myAgent).choisirTresor("or");
						}
						else {
							this.objectif = ((ExploreCoopAgent) myAgent).choisirTresor("diamant");
						}
					}
					else {
						String type = ((ExploreCoopAgent) myAgent).selectionnerType();
						this.objectif = ((ExploreCoopAgent) myAgent).choisirTresor(type);
					}
					
				}
			}
			
			if (this.objectif==null) {
				SerializableSimpleGraph<String, MapAttribute> sg=this.myMap.getSerializableGraph();
				Set<SerializableNode<String, MapAttribute>> nodes = sg.getAllNodes();
				
				Random r= new Random();
				int moveId=r.nextInt(nodes.size());
				int i = 0;
				for (SerializableNode<String, MapAttribute> n: nodes) {
					if (i==moveId) {
						System.out.println("Interblocage,noeud aleatoire choisi par "+this.myAgent.getName()+": "+n.getNodeId());
						this.objectif = n.getNodeId();
						break;
					}
					i++;
				}
			}
			
			
		}
		
		
		System.out.println(this.myAgent.getName()+" objectif : "+this.objectif);
		
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		
		List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
		List<Couple<Observation,Integer>> lObservations= lobs.get(0).getRight();
		boolean stench = false;
		int tresor = -1;
		int j=0;
		for(Couple<Observation,Integer> o:lObservations){
		//if (lObservations.isEmpty()==false) {
			switch (o.getLeft()) {
			case DIAMOND:
				((ExploreCoopAgent) myAgent).mise_a_jour(o.getLeft(),myPosition,o.getRight(),System.currentTimeMillis());
				tresor = j;
				break;
			case GOLD:
				((ExploreCoopAgent) myAgent).mise_a_jour(o.getLeft(),myPosition,o.getRight(),System.currentTimeMillis());
				tresor = j;
				break;
			}
			j++;
		}
		
		if (tresor==-1) {
			for(Couple<Observation,Integer> o:lObservations){
				switch (o.getLeft()) {
				case STENCH:
					stench = true;
					break;
				case LOCKSTATUS:
					if (((ExploreCoopAgent) this.myAgent).quel_tresor(myPosition).equals("or")) {
						((ExploreCoopAgent) myAgent).mise_a_jour(Observation.GOLD,myPosition,0,System.currentTimeMillis());
					}
					if (((ExploreCoopAgent) this.myAgent).quel_tresor(myPosition).equals("diamant")) {
						((ExploreCoopAgent) myAgent).mise_a_jour(Observation.DIAMOND,myPosition,0,System.currentTimeMillis());
					}
					break;
				default:
				break;
				}
			}
		}
		
		
		
		if(myPosition.equals(this.objectif)) {
			//System.out.println(lObservations);
			
			if (lObservations.isEmpty()==false && tresor!=-1) {
				if (stench==false) {
					((AbstractDedaleAgent) this.myAgent).openLock(lObservations.get(tresor).getLeft());
				
					int picked = ((AbstractDedaleAgent) this.myAgent).pick();
					System.out.println(this.myAgent.getName()+" a ramassé "+picked+" "+lObservations.get(tresor).getLeft()+" sur la case "+ myPosition);
					((ExploreCoopAgent) this.myAgent).mise_a_jour(lObservations.get(tresor).getLeft(),myPosition,lObservations.get(tresor).getRight()-picked,System.currentTimeMillis());
			
				}
			}
			this.objectif = null;
		}
		else {
			String nextNode=null;
			nextNode=this.myMap.getShortestPath(myPosition,objectif).get(0);
			int cpt = 0;
			while (((AbstractDedaleAgent)this.myAgent).moveTo(nextNode)==false && cpt<2) {
				this.myAgent.doWait(500);
				cpt++;
			}
			if (cpt==2) {
				SerializableSimpleGraph<String, MapAttribute> sg=this.myMap.getSerializableGraph();
				Set<SerializableNode<String, MapAttribute>> nodes = sg.getAllNodes();
				
				Random r= new Random();
				int moveId=r.nextInt(nodes.size());
				int i = 0;
				for (SerializableNode<String, MapAttribute> n: nodes) {
					if (i==moveId) {
						System.out.println(this.myAgent.getName()+" Interblocage, noeud aleatoire choisi : "+n.getNodeId());
						this.objectif = n.getNodeId();
						break;
					}
					i++;
				}
			}
		}
		//System.out.println(this.myAgent.getName()+" objectif : "+this.objectif);
		ACLMessage msg1 = new ACLMessage(ACLMessage.INFORM);
		msg1.setProtocol("REPERAGE");
		msg1.setSender(this.myAgent.getAID());
	
		for (String agentName : this.agentNames) {
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
			
			for (String agentName : this.agentNames) {
				if (agentName!=this.myAgent.getLocalName()) {
					msg2.addReceiver(new AID(agentName,AID.ISLOCALNAME));
				}
			}
			msg2.setContent("Il y a quelqu'un ?");
			((AbstractDedaleAgent)this.myAgent).sendMessage(msg2);
			
			System.out.println(this.myAgent.getLocalName() + ": " + msgReceived1.getContent() + " par " + msgReceived1.getSender().getLocalName());
			this.myAgent.addBehaviour(new PartageMapCollectBehaviour((ExploreCoopAgent) this.myAgent,this.myMap,msgReceived1.getSender(),this.agentNames,this.objectif,this.date));
			
			finished = true;
			
		}
		
		if (System.currentTimeMillis()-this.date>45000) {
			finished =true;
			this.myAgent.addBehaviour(new ExploCoopBehaviour((ExploreCoopAgent) this.myAgent,this.myMap,this.agentNames,null,System.currentTimeMillis()));
			System.out.println(this.myAgent.getLocalName()+" - Collecte arrêtée temporairement");
		}
	}

	@Override
	public boolean done() {
		return finished;
	}
}
