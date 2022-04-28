package eu.su.mas.dedaleEtu.mas.agents.dummies.explo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;

import eu.su.mas.dedaleEtu.mas.behaviours.ExploCoopBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.PartageMapBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SayHelloBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ShareMapBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;

/**
 * <pre>
 * ExploreCoop agent. 
 * Basic example of how to "collaboratively" explore the map
 *  - It explore the map using a DFS algorithm and blindly tries to share the topology with the agents within reach.
 *  - The shortestPath computation is not optimized
 *  - Agents do not coordinate themselves on the node(s) to visit, thus progressively creating a single file. It's bad.
 *  - The agent sends all its map, periodically, forever. Its bad x3.
 *   - You should give him the list of agents'name to send its map to in parameter when creating the agent.
 *   Object [] entityParameters={"Name1","Name2};
 *   ag=createNewDedaleAgent(c, agentName, ExploreCoopAgent.class.getName(), entityParameters);
 *  
 * It stops when all nodes have been visited.
 * 
 * 
 *  </pre>
 *  
 * @author hc
 *
 */


public class ExploreCoopAgent extends AbstractDedaleAgent {

	private static final long serialVersionUID = -7969469610241668140L;
	private MapRepresentation myMap;
	private AID agent_communication;
	private List<String> pos_gold;
	private List<String> pos_diamond;
	private List<Integer> qte_gold;
	private List<Integer> qte_diamond;
	

	/**
	 * This method is automatically called when "agent".start() is executed.
	 * Consider that Agent is launched for the first time. 
	 * 			1) set the agent attributes 
	 *	 		2) add the behaviours
	 *          
	 */
	
	public String selectionnerTresorAlea(String type_tresor) {
		Random rand = new Random();
		if (type_tresor.equals("or")) {
			int nombreAleatoire = rand.nextInt(this.pos_gold.size());
			return this.pos_gold.get(nombreAleatoire);
		}
		if (type_tresor.equals("diamant")) {
			int nombreAleatoire = rand.nextInt(this.pos_diamond.size());
			return this.pos_diamond.get(nombreAleatoire);
		}
		return null;
	}
	
	public String selectionnerType() {
		String type = "";
		int qte_or = 0;
		int qte_diam = 0;
		for (int i: this.qte_gold) {
			qte_or = qte_or + i;
		}
		for (int i: this.qte_diamond) {
			qte_diam = qte_diam + i;
		}

		float pourcentage = qte_or/qte_or+qte_diam;
		double rand = Math.random();
		if (rand<pourcentage) {
			type = "or";
		}
		else {
			type = "diamant";
		}
		
		return type;
	}
	
	public String choisirTresor(String type) {
		int place_dispo = getBackPackFreeSpace().get(0).getRight();
		//int place_dispo = ((AbstractDedaleAgent) this).ec.getGoldCapacity();
		String tresor = "";
		int valeur = 0;
		
		if (type.equals("or")) {
			for (int i=0;i<pos_gold.size();i++) {
				int v = qte_gold.get(i);
				if (v>valeur && v<=place_dispo) {
					valeur = v;
					tresor = pos_gold.get(i);
				}
			}
			if (tresor.equals("")) {
				valeur = Integer.MAX_VALUE;
				for (int i=0;i<pos_gold.size();i++) {
					int v = qte_gold.get(i);
					if (v<valeur) {
						valeur = v;
						tresor = pos_gold.get(i);	
					}
				}
			}
		}
		
		if (type.equals("diamant")) {
			for (int i=0;i<pos_diamond.size();i++) {
				int v = qte_diamond.get(i);
				if (v>valeur && v<=place_dispo) {
					valeur = v;
					tresor = pos_diamond.get(i);	
				}
			}
			
			if (tresor.equals("")) {
				valeur = Integer.MAX_VALUE;
				for (int i=0;i<pos_diamond.size();i++) {
					int v = qte_diamond.get(i);
					if (v<valeur) {
						valeur = v;
						tresor = pos_diamond.get(i);	
					}
				}
			}
		}
		System.out.println(this.getName()+" capactité: "+ place_dispo +" tresor: "+tresor+" valeur: "+valeur);
		return tresor;
	}
	
	public void ajouter_or(String position, Integer valeur) {
		Boolean ajouter = true;
		for (String i:pos_gold) {
			if (i.equals(position)) {
				ajouter = false;
				break;
			}
		}
		if (ajouter) {
			/*System.out.println(this.getName() + " : ajout or : " + position + " : " + valeur);*/
			pos_gold.add(position);
			qte_gold.add(valeur);
		}
	}
	
	public void ajouter_diamant(String position, Integer valeur) {
		
		Boolean ajouter = true;
		for (String i:pos_diamond) {
			if (i.equals(position)) {
				ajouter = false;
				break;
			}
		}
		if (ajouter) {
			pos_diamond.add(position);
			qte_diamond.add(valeur);
		}
	}
	
	public void print_or() {
		System.out.println(this.getName()+" : Positions et quantite des tresors or:");
		for (int i=0;i<pos_gold.size();i++) {
			System.out.println(pos_gold.get(i)+" : "+qte_gold.get(i));
		}
	}
	
	public void print_diamant() {
		System.out.println("Positions et quantite des tresors diamant:");
		for (int i=0;i<pos_diamond.size();i++) {
			System.out.println(pos_diamond.get(i)+" : "+qte_diamond.get(i));
		}
	}
	
	public String list_serialisees() {
		String chaine = "";
		print_or();
		for (int i=0;i<pos_gold.size();i++) {
			chaine = chaine + "or,"+pos_gold.get(i)+","+qte_gold.get(i)+";";
		}
		for (int i=0;i<pos_diamond.size();i++) {
			chaine = chaine + "dia,"+pos_diamond.get(i)+","+qte_diamond.get(i)+";";
		}
		
		return chaine;
	}
	
	public void deserialize(String chaine) {
		String[] listchaine = chaine.split(";");
		System.out.println(chaine);
		for (int i=0;i<listchaine.length;i++) {
			String[] elem = listchaine[i].split(",");
			//ArrayList<String> elem2 = new ArrayList<String>(Arrays.asList(elem));
			if (elem[0].equals("or")) {
				//System.out.println("Deserialize elem= "+elem2.get(0)+","+elem2.get(1)+","+elem2.get(2));
				ajouter_or(elem[1], Integer.parseInt(elem[2]));
				
			}
			if (elem[0].equals("dia")) {
				ajouter_diamant(elem[1], Integer.parseInt(elem[2]));
			}
				
		}
		
	}
	
	protected void setup(){

		super.setup();
		pos_gold = new ArrayList<String>();
		pos_diamond = new ArrayList<String>();
		qte_gold = new ArrayList<Integer>();
		qte_diamond = new ArrayList<Integer>();
		
		//get the parameters added to the agent at creation (if any)
		final Object[] args = getArguments();
		
		List<String> list_agentNames=new ArrayList<String>();
		
		if(args.length==0){
			System.err.println("Error while creating the agent, names of agent to contact expected");
			System.exit(-1);
		}else{
			int i=2;// WARNING YOU SHOULD ALWAYS START AT 2. This will be corrected in the next release.
			while (i<args.length) {
				list_agentNames.add((String)args[i]);
				i++;
			}
		}

		List<Behaviour> lb=new ArrayList<Behaviour>();
		
		/************************************************
		 * 
		 * ADD the behaviours of the Dummy Moving Agent
		 * 
		 ************************************************/
		String A = "A";
		String B = "B";
		/*
		FSMBehaviour fsm = new FSMBehaviour(this);
		fsm.registerFirstState(new ExploCoopBehaviour(this,this.myMap,list_agentNames),A);
		fsm.registerState(new PartageMapBehaviour(this,this.myMap,this.agent_communication),B);
		//fsm.registerState(new ShareMapBehaviour(this,100,this.myMap,list_agentNames),B);
		
		fsm.registerDefaultTransition(B,A);
		fsm.registerTransition(A,B,1);
		*/
				
		
		lb.add(new ExploCoopBehaviour(this,this.myMap,list_agentNames));
		//lb.add(fsm);
		
		
		/***
		 * MANDATORY TO ALLOW YOUR AGENT TO BE DEPLOYED CORRECTLY
		 */
		
		
		addBehaviour(new startMyBehaviours(this,lb));
		
		System.out.println("the  agent "+this.getLocalName()+ " is started");

	}
	
	
	
}
