package princ;


import jade.core.AID;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;


import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;
import protocols.auctions.IAuction;
import protocols.auctions.dutchAuction.DutchAuction;
import protocols.auctions.englishAuction.EnglishAuction;
import protocols.auctions.japaneseAuction.JapaneseAuction;
import protocols.consensus.paxos.BasicPaxos;
import protocols.consensus.paxos.MultiPaxos;
import protocols.consensus.paxos.Paxos;
import protocols.consensus.paxos.agents.AcceptorAgent;
import protocols.consensus.paxos.agents.ClientAgent;
import protocols.consensus.paxos.agents.LearnerAgent;
import protocols.consensus.paxos.agents.ProposerAgent;
import protocols.tools.CreateAgentFromJsonFile;
import protocols.tools.ProtocolFromJson;
import protocols.Protocol;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * This class allows you to select the example you want to trigger (line 103 and 109 ).
 * It also illustrates how to launch a JADE platform from source-code.
 * <p>
 * In all examples, the "pause" when launching the platform  is just here to give you the possibility to activate the sniffer agent from its GUI 
 * in order to sniff the agents and to graphically see the message passing process (if any).
 * <p>
 * In any case, I chose here to print the sent/received on the standard output.
 * 
 * For sake of simplicity, the creation of the containers and of the agents is hardcoded, and I do not activate the sniffer by default.  
 * It is bad.
 * 
 * <p>
 * Tested with Jade from 3.7 to 4.5.0
 * 
 * 
 * @author Cédric Herpson
 *
 */

public class Principal {

	private static HashMap<String, ContainerController> containerList=new HashMap<String, ContainerController>();// container's name - container's ref
	private static List<AgentController> agentList;// agents's ref
	private static Runtime rt;	

	/*************************************************************************************
	 * This inner class list all the currently triggerable examples on the platform 
	 *
	 *************************************************************************************/

	public enum EXAMPLE { 
		FUNDAMENTALS_CREATE_PLATFORM("Fundamentals > Create platform"),
		FUNDAMENTALS_COMMUNICATION("Fundamentals > Communication"),
		FUNDAMENTALS_MIGRATION("Fundamentals > Migration"),

		FUNDAMENTALS_BEHAVIOUR_SEQUENTIAL("Fundamentals > Composite behaviour > Sequential"),
		FUNDAMENTALS_BEHAVIOUR_FSM("Fundamentals > Composite behaviour > Finite State Machine (FSM)"),
		FUNDAMENTALS_BEHAVIOUR_PARALLEL("Fundamentals > Composite behaviour > Parallel behaviour"),
		FUNDAMENTALS_BEHAVIOUR_THREADED("Fundamentals > Threaded behaviour"),

		PROTOCOLS_AUCTION_ENGLISH("Protocols>Auction>EnglishAuction"),
		PROTOCOLS_AUCTION_REVERSEENGLISH("Protocols>Auction>EnglishAuction"),
		PROTOCOLS_AUCTION_DUTCH("Protocols>Auction>DutchAuction"),
		PROTOCOLS_AUCTION_JAPANESE("Protocols>Auction>JapaneseAuction"),
		PROTOCOLS_AUCTION_REVERSEJAPANESE("Protocols>Auction>JapaneseAuction"),

		PROTOCOLS_GOSSIP_PUSHSUM("Protocols>Gossip>PushSum"),
		PROTOCOLS_GOSSIP_LNS("Protocols>Gossip>LearnNewSecret"),

		EXERCICES_SUM1_1("Exercices>sum 1-1"),
		EXERCICES_EXO2("Exercices> - not yet available"),
		EXERCICES_EXO3("Exercices> - not yet available"),
		PROTOCOL_FROM_FILE("Protocols>ProtocolFromJson"),
		CONSENSUS_BASICPAXOS("consensus>paxos>BasicPaxos"),
		CONSENSUS_MULTIPAXOS("consensus>paxos>MultiPaxos");

		private String name;

		EXAMPLE(String s){
			name=s;
		}

		public String toString() {
			return name;
		}

	}


	/************************************
	 ***********************************
	 *
	 * 1) Network and platform parameters
	 * 
	 ***********************************/

	/**
	 * True if the current computer will possess the main container.  
	 * The node with the main container should be started before launching the child nodes 
	 */
	public static boolean COMPUTERisMAIN= true;

	/**
	 * IP (or host) of the main container
	 */
	private static final String PLATFORM_IP = "127.0.0.1"; 

	/**
	 * Port to use to contact the AMS
	 */
	private static final int PLATFORM_PORT=8888;

	/**
	 * ID (name) of the platform instance
	 */
	private static final String PLATFORM_ID="Ithaq";

	/**
	 * Default name of a distant container
	 **/
	private static final String PLATFORM_DEFAULT_CONTAINER_NAME="DistantContainer";


	/*********************************************
	 *********************************************
	 *
	 * 2) EXAMPLE TO BE TRIGGERED
	 * 
	 **********************************************/

	/************************************
	 * 
	 * 2-a) Select the example you want to trigger 
	 * 
	 ***********************************/

	//private static EXAMPLE EXAMPLE_TO_USE=EXAMPLE.FUNDAMENTALS_CREATE_PLATFORM;
	//private static EXAMPLE EXAMPLE_TO_USE=EXAMPLE.FUNDAMENTALS_COMMUNICATION;
	//private static EXAMPLE EXAMPLE_TO_USE=EXAMPLE.EXERCICES_SUM1_1;
	//private static EXAMPLE EXAMPLE_TO_USE=EXAMPLE.EXERCICES_EXO2;
	private static EXAMPLE EXAMPLE_TO_USE=EXAMPLE.EXERCICES_EXO3;
	//private static EXAMPLE EXAMPLE_TO_USE=EXAMPLE.FUNDAMENTALS_MIGRATION;
	//private static EXAMPLE EXAMPLE_TO_USE=EXAMPLE.PROTOCOLS_GOSSIP_LNS;	
	//private static EXAMPLE EXAMPLE_TO_USE=EXAMPLE.CONSENSUS_BASICPAXOS;
	//private static EXAMPLE EXAMPLE_TO_USE=EXAMPLE.CONSENSUS_MULTIPAXOS;
	//private static EXAMPLE EXAMPLE_TO_USE=EXAMPLE.PROTOCOL_FROM_FILE;

	/************************************
	 * 
	 * 2-b) If EXAMPLE.PROTOCOL_FROM_FILE was selected above (2-a), please select the configuration file you want to use. 
	 * 
	 ***********************************/

	private static String PROTOCOL_FILE = "resources/testEnglishAuction.json";
	//private static String PROTOCOL_FILE = "resources/reverseEnglishAuction.json";
	//private static String PROTOCOL_FILE = "resources/testJapaneseAuction.json";
	//private static String PROTOCOL_FILE = "resources/reverseJapaneseAuction.json";
	//private static String PROTOCOL_FILE = "resources/testDutchAuction.json";
	//private static String PROTOCOL_FILE = "resources/reverseDutchAuction.json";
	//private static String PROTOCOL_FILE = "resources/testBasicPaxos.json";
	//private static String PROTOCOL_FILE = "resources/testMultiPaxos.json";



	/**
	 * Main 
	 * 
	 * @param args
	 */
	public static void main(String[] args){
		List<Object> parameters;

		if(COMPUTERisMAIN){
			//Whe should create the Platform and the GateKeeper, whether the platform is distributed or not 

			//1) Create the platform (Main container (DF+AMS) + containers + monitoring agents : RMA and SNIFFER)
			rt=emptyPlatform(containerList);	
		}else {
			//We only have to create the local container and our agents

			//1') If a distant platform already exist and you want to create and connect your container to it
			containerList.put(PLATFORM_DEFAULT_CONTAINER_NAME,createOneContainer(Runtime.instance(),PLATFORM_DEFAULT_CONTAINER_NAME, PLATFORM_IP, PLATFORM_PORT,PLATFORM_ID));
		}

		//2) Optional set example parameters, if any, and if not loaded from a jsonFile. 
		parameters=new ArrayList<Object>(Arrays.asList(1,5)); //test for PUSHSUM and LNS protocol, when defined outside the jsonFile

		//3) create agents and add them to the platform.
		agentList=createAgents(containerList,EXAMPLE_TO_USE,parameters);

		try {
			System.out.println("The system is paused -- this action is only here to let you activate the sniffer on the agents, if you want (see documentation)");
			System.out.println("Press enter in the console to start the agents");
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}

		//4) launch agents
		startAgents(agentList);

	}


	/**********************************************
	 * 
	 * Methods used to create an empty platform
	 * 
	 **********************************************/

	/**
	 * Create an empty platform composed of 1 main container and several containers.
	 * 
	 * @param containerList the HashMap of (container's name,container's ref)
	 * @return a ref to the platform and update the containerList
	 */
	private static Runtime emptyPlatform(HashMap<String, ContainerController> containerList){

		Runtime rt = Runtime.instance();

		// 1) create a platform (main container+DF+AMS)
		Profile pMain = new ProfileImpl(PLATFORM_IP, PLATFORM_PORT, PLATFORM_ID);
		System.out.println("Launching a main-container..."+pMain);
		AgentContainer mainContainerRef = rt.createMainContainer(pMain); //DF and AMS are include

		// 2) create the containers
		containerList.putAll(createContainers(rt));

		// 3) create monitoring agents : rma agent, used to debug and monitor the platform; sniffer agent, to monitor communications; 
		createMonitoringAgents(mainContainerRef);

		System.out.println("Plaform ok");
		return rt;

	}

	/**
	 * Create the containers used to hold the agents 
	 * @param rt The reference to the main container
	 * @return an Hmap associating the name of a container and its object reference.
	 * <p>
	 * note: there is a smarter way to find a container with its name, but we go straight to the goal here. Cf jade's doc.
	 */
	private static HashMap<String,ContainerController> createContainers(Runtime rt) {
		String containerName;
		ProfileImpl pContainer;
		ContainerController containerRef;

		HashMap<String, ContainerController> containerList=new HashMap<String, ContainerController>();//bad to do it here.
		System.out.println("Launching containers ...");

		//create the container1	
		containerName="Mycontainer1";
		containerList.put(containerName, createOneContainer(rt, containerName, PLATFORM_IP, PLATFORM_PORT, PLATFORM_ID));

		//create the container2	
		containerName="Mycontainer2";
		containerList.put(containerName, createOneContainer(rt, containerName, PLATFORM_IP, PLATFORM_PORT, PLATFORM_ID));

		//create the container3	
		containerName="Mycontainer3";
		containerList.put(containerName, createOneContainer(rt, containerName, PLATFORM_IP, PLATFORM_PORT, PLATFORM_ID));

		System.out.println("Launching containers done");
		return containerList;
	}

	/**
	 * 
	 * @param rt Link to the platform ref we want to deploy the container on (null if the platform is distant)
	 * @param containerName name of the container, should be unique
	 * @param platformIp  A null value means use the default (i.e. localhost)
	 * @param platformPort  A negative value should be used for using the default port number.
	 * @param platformId The symbolic name of the platform, if different from default. A null value means use the default (i.e. localhost)
	 * @return A reference to the newly created container.
	 */
	private static ContainerController createOneContainer(Runtime rt,String containerName,String platformIp,int platformPort, String platformId) {
		ProfileImpl pContainer;
		pContainer = new ProfileImpl(platformIp,platformPort,platformId);
		pContainer.setParameter(Profile.CONTAINER_NAME,containerName);

		System.out.println("Launching container "+pContainer);

		return rt.createAgentContainer(pContainer); //ContainerController replace AgentContainer in the new versions of Jade.
	}



	/**
	 * create the monitoring agents (rma+sniffer) on the main-container given in parameter and launch them.
	 * <ul>
	 * <li> RMA agent's is used to control, debug and monitor the platform;
	 * <li> Sniffer agent is used to monitor communications
	 * </ul>
	 * @param mc the main-container's reference
	 */
	private static void createMonitoringAgents(ContainerController mc) {

		System.out.println("Launching the rma agent on the main container ...");
		AgentController rma;

		try {
			rma = mc.createNewAgent("rma", "jade.tools.rma.rma", new Object[0]);
			rma.start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
			System.out.println("Launching of rma agent failed");
		}

		System.out.println("Launching  Sniffer agent on the main container...");
		AgentController snif=null;

		try {
			snif= mc.createNewAgent("sniffeur", "jade.tools.sniffer.Sniffer",new Object[0]);
			snif.start();

		} catch (StaleProxyException e) {
			e.printStackTrace();
			System.out.println("launching of sniffer agent failed");

		}		
	}



	/**********************************************
	 * 
	 * Methods used to create the agents and to start them
	 * 
	 **********************************************/


	/**
	 *  Creates the agents and add them to the agentList. The agents are NOT started.
	 *@param containerList Name and container's ref
	 *@param exampleToExecute Example to use
	 *@param exampleParameters parameters to be given for the given example, if any
	 *@return the agentList
	 */

	private static List<AgentController> createAgents(HashMap<String, ContainerController> containerList,EXAMPLE exampleToExecute,List<Object>exampleParameters) {
		System.out.println("\n --- \n Launching agents for example "+exampleToExecute+" ... \n --- \n");
		ContainerController c;
		String agentName;
		String containerName;
		List<AgentController> agentList=new ArrayList<AgentController>();

		switch (exampleToExecute) {
		case FUNDAMENTALS_CREATE_PLATFORM:
			if (COMPUTERisMAIN) {
				// Only one empty agent created with the platform components.
				containerName="Mycontainer1";
				c = containerList.get(containerName);
				agentName="Agent-DoNothing";
				createOneAgent(c, agentName, fundamentals.createPlatform.AgentEmpty.class.getName(),agentList,null);
			}
			break;

		case FUNDAMENTALS_COMMUNICATION:
			//Agent0 on container1
			containerName="Mycontainer1";
			c = containerList.get(containerName);

			agentName="Agent0";// the sender
			List<String> data=new ArrayList<String>();
			data.add("This");data.add("is");data.add("a");data.add("test");
			Object[] objtab=new Object[]{data};// Example regarding how to give information to an agent at creation. These "data" will be processed in the setup() method of agent0 
			createOneAgent(c, agentName, fundamentals.communication.AgentSender.class.getName(),agentList, objtab);

			//Agent1 and Agent3 on Mycontainer2
			containerName="Mycontainer2";
			c = containerList.get(containerName);

			agentName="Agent1";// the receiver
			objtab=new Object[]{};//used to give informations to the agent (in that case, nothing)
			createOneAgent(c, agentName, fundamentals.communication.AgentReceiver.class.getName(), agentList, objtab);

			agentName="Agent3";
			objtab=new Object[]{};//used to give informations to the agent (in that case, nothing)
			createOneAgent(c, agentName, fundamentals.createPlatform.AgentEmpty.class.getName(),agentList, objtab);

			//Agent2 on Mycontainer3
			containerName="Mycontainer3";
			c = containerList.get(containerName);
			agentName="Agent2";
			objtab=new Object[]{};//used to give informations to the agent (in that case, nothing)
			createOneAgent(c, agentName, fundamentals.createPlatform.AgentEmpty.class.getName(), agentList, objtab);
			break;

		case FUNDAMENTALS_MIGRATION: //Intra-platform migration example
			//AgentOscillator on Mycontainer2 before moving somewhere else
			containerName="Mycontainer2";
			c = containerList.get(containerName);
			agentName="Oscillator";
			objtab=new Object[]{};//used to give informations to the agent (in that case, nothing)
			createOneAgent(c, agentName, fundamentals.migration.DummyMovingAgent.class.getName(), agentList, objtab);
			break;

		case FUNDAMENTALS_BEHAVIOUR_FSM:
			containerName="Mycontainer1";
			c = containerList.get(containerName);
			agentName="AgentFSM";
			objtab=new Object[]{};//used to give informations to the agent (in that case, nothing)
			createOneAgent(c, agentName, fundamentals.jadeBehaviours.composite.fsm.agent.AgentFsm.class.getName(), agentList, objtab);
			break;

		case FUNDAMENTALS_BEHAVIOUR_SEQUENTIAL:	
			containerName="Mycontainer1";
			c = containerList.get(containerName);
			agentName="AgentSequential";
			objtab=new Object[]{};//used to give informations to the agent (in that case, nothing)
			createOneAgent(c, agentName, fundamentals.jadeBehaviours.composite.sequential.AgentSequential.class.getName(), agentList, objtab);
			break;

		case FUNDAMENTALS_BEHAVIOUR_PARALLEL:	
			containerName="Mycontainer1";
			c = containerList.get(containerName);
			agentName="AgentPara";
			objtab=new Object[]{};//used to give informations to the agent (in that case, nothing)
			createOneAgent(c, agentName, fundamentals.jadeBehaviours.composite.parallel.AgentParallel.class.getName(), agentList, objtab);
			break;

		case FUNDAMENTALS_BEHAVIOUR_THREADED:	
			containerName="Mycontainer1";
			c = containerList.get(containerName);
			agentName="AgentMultiThread";
			objtab=new Object[]{};//used to give informations to the agent (in that case, nothing)
			createOneAgent(c, agentName, fundamentals.jadeBehaviours.threaded.AgentMultithread.class.getName(), agentList, objtab);
			break;

		case EXERCICES_SUM1_1: //AgentA and AgentSUM on container2
			containerName="Mycontainer2";
			c = containerList.get(containerName);
			agentName="AgentA";
			objtab=new Object[]{};//used to give informations to the agent (in that case, nothing)
			createOneAgent(c, agentName, exercices.sum1_1.agents.AgentA.class.getName(), agentList, objtab);

			agentName="AgentSUM";
			objtab=new Object[]{};//used to give informations to the agent (in that case, nothing)
			createOneAgent(c, agentName, exercices.sum1_1.agents.AgentSum.class.getName(), agentList, objtab);
			break;
		case EXERCICES_EXO2:
			containerName="Mycontainer2";
			c = containerList.get(containerName);
			agentName="AgentA";
			objtab=new Object[]{};
			createOneAgent(c,agentName,exercices.exercice2.agents.AgentA.class.getName(), agentList, objtab);
			
			agentName="AgentC";
			objtab=new Object[]{};
			createOneAgent(c,agentName,exercices.exercice2.agents.AgentC.class.getName(), agentList, objtab);
			
			agentName="AgentSomme";
			objtab=new Object[]{};
			createOneAgent(c,agentName,exercices.exercice2.agents.AgentSomme.class.getName(), agentList, objtab);
			break;
		case EXERCICES_EXO3:
			containerName="Mycontainer2";
			c = containerList.get(containerName);
			agentName="Agent1";
			objtab=new Object[]{};
			createOneAgent(c,agentName,exercices.sum1_n.agents.Agent1.class.getName(), agentList, objtab);
			
			agentName="Agent2";
			objtab=new Object[]{};
			createOneAgent(c,agentName,exercices.sum1_n.agents.Agent2.class.getName(), agentList, objtab);
			
			agentName="AgentSomme";
			objtab=new Object[]{};
			createOneAgent(c,agentName,exercices.sum1_n.agents.AgentSomme.class.getName(), agentList, objtab);
			break;	
		case PROTOCOLS_AUCTION_ENGLISH :
			agentList=startAuction((Map<String,Object[]>)exampleParameters.get(0),(Map<String,Object[]>)exampleParameters.get(1),Protocol.EnglishAuction);
			break;
		case PROTOCOLS_AUCTION_REVERSEENGLISH :
			agentList=startAuction((Map<String,Object[]>)exampleParameters.get(0),(Map<String,Object[]>)exampleParameters.get(1),Protocol.ReverseEnglishAuction);
			break;
		case PROTOCOLS_AUCTION_DUTCH :
			agentList=startAuction((Map<String,Object[]>)exampleParameters.get(0),(Map<String,Object[]>)exampleParameters.get(1),Protocol.DutchAuction);
			break;
		case PROTOCOLS_AUCTION_JAPANESE:
			agentList=startAuction((Map<String,Object[]>)exampleParameters.get(0),(Map<String,Object[]>)exampleParameters.get(1),Protocol.JapaneseAuction);
			break;
		case PROTOCOLS_AUCTION_REVERSEJAPANESE:
			agentList=startAuction((Map<String,Object[]>)exampleParameters.get(0),(Map<String,Object[]>)exampleParameters.get(1),Protocol.ReverseJapaneseAuction);
			break;
		case PROTOCOL_FROM_FILE:
			//In that case, all required information are given within a Json File
			containerName="Mycontainer2";
			c = containerList.get(containerName);
			List<CreateAgentFromJsonFile> agents = ProtocolFromJson.readFromJson(new File(PROTOCOL_FILE));
			for(CreateAgentFromJsonFile agent:agents)
				createOneAgent(c, agent.getAgentName(), agent.getClassName(), agentList, agent.getParams());
			break;
		case CONSENSUS_BASICPAXOS:
			int nbClient = 3;
			int nbProposer = 2;
			int nbAcceptor = 3;
			int nbLearner = 2;
			containerName="Mycontainer2";
			BasicPaxos basicpaxos = new BasicPaxos();
			objtab=new Object[]{basicpaxos};//used to give informations to the agent (in that case, nothing)
			List<AgentController> listProposer = new ArrayList<AgentController>();
			List<AgentController> listAcceptor = new ArrayList<AgentController>();
			List<AgentController> listLearner = new ArrayList<AgentController>();

			for(int i=0;i<nbAcceptor;i++) {
				c = containerList.get(containerName);
				createOneAgent(c, "Acceptor"+i, protocols.consensus.paxos.agents.AcceptorAgent.class.getName(), listAcceptor, objtab);

			}
			agentList.addAll(listAcceptor);
			for(int i=0;i<nbLearner;i++) {
				c = containerList.get(containerName);
				createOneAgent(c, "Learner"+i, protocols.consensus.paxos.agents.LearnerAgent.class.getName(), listLearner, objtab);
			}
			agentList.addAll(listLearner);
			for(int i=0;i<nbProposer;i++) {
				c = containerList.get(containerName);
				List<String> listNameAcceptor = new ArrayList<String>();
				List<String> listNameLearner = new ArrayList<String>();
				for (AgentController ag : listAcceptor) {
					try {
						listNameAcceptor.add(ag.getName());
					} catch (StaleProxyException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				for (AgentController ag : listLearner) {
					try {
						listNameLearner.add(ag.getName());
					} catch (StaleProxyException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				//objtab=new Object[]{basicpaxos, listNameAcceptor, listNameLearner};
				objtab=new Object[]{basicpaxos};
				createOneAgent(c, "Proposer"+i, protocols.consensus.paxos.agents.ProposerAgent.class.getName(), listProposer, objtab);

			}
			agentList.addAll(listProposer);
			for(int i=0;i<nbClient;i++) {
				c = containerList.get(containerName);
				try {
					String topicRequest = "Exemple"+ i%nbProposer;
					String valueRequest = Integer.toString(i);
					objtab=new Object[]{basicpaxos, listProposer.get(i%nbProposer).getName(), topicRequest, valueRequest};
					createOneAgent(c, "Client"+i, protocols.consensus.paxos.agents.ClientAgent.class.getName(), agentList, objtab);
				} catch (StaleProxyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			break;
		case CONSENSUS_MULTIPAXOS:
			nbClient = 10;
			nbProposer = 2;
			nbAcceptor = 3;
			nbLearner = 2;
			containerName="Mycontainer2";
			MultiPaxos multipaxos = new MultiPaxos();
			objtab=new Object[]{multipaxos};//used to give informations to the agent (in that case, nothing)
			listProposer = new ArrayList<AgentController>();
			listAcceptor = new ArrayList<AgentController>();
			listLearner = new ArrayList<AgentController>();

			for(int i=0;i<nbAcceptor;i++) {
				c = containerList.get(containerName);
				createOneAgent(c, "Acceptor"+i, protocols.consensus.paxos.agents.AcceptorAgent.class.getName(), listAcceptor, objtab);

			}
			agentList.addAll(listAcceptor);
			for(int i=0;i<nbLearner;i++) {
				c = containerList.get(containerName);
				createOneAgent(c, "Learner"+i, protocols.consensus.paxos.agents.LearnerAgent.class.getName(), listLearner, objtab);
			}
			agentList.addAll(listLearner);
			for(int i=0;i<nbProposer;i++) {
				c = containerList.get(containerName);
				List<String> listNameAcceptor = new ArrayList<String>();
				List<String> listNameLearner = new ArrayList<String>();
				for (AgentController ag : listAcceptor) {
					try {
						listNameAcceptor.add(ag.getName());
					} catch (StaleProxyException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				for (AgentController ag : listLearner) {
					try {
						listNameLearner.add(ag.getName());
					} catch (StaleProxyException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}objtab=new Object[]{multipaxos, listNameAcceptor, listNameLearner};
				createOneAgent(c, "Proposer"+i, protocols.consensus.paxos.agents.ProposerAgent.class.getName(), listProposer, objtab);

			}
			agentList.addAll(listProposer);
			for(int i=0;i<nbClient;i++) {
				c = containerList.get(containerName);
				try {
					String topicRequest = "Exemple"+ i%nbProposer;
					String valueRequest = Integer.toString(i);
					objtab=new Object[]{multipaxos, listProposer.get(i%nbProposer).getName(), topicRequest, valueRequest};
					createOneAgent(c, "Client"+i, protocols.consensus.paxos.agents.ClientAgent.class.getName(), agentList, objtab);
				} catch (StaleProxyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			break;
		case PROTOCOLS_GOSSIP_PUSHSUM: 
			Random rand = new Random();
			containerName="Mycontainer2";
			c = containerList.get(containerName);
			int nbRounds=3;
			agentName="1stAgent";
			objtab=new Object[]{nbRounds};
			createOneAgent(c,agentName, protocols.gossip.pushsum.agents.TickerAgent.class.getName(), agentList, objtab);
			agentName="2ndAgent";
			objtab=new Object[]{new ArrayList<String>(Arrays.asList("3rdAgent","4thAgent")),rand.nextInt(30)};
			createOneAgent(c, agentName, protocols.gossip.pushsum.agents.PushSumAgent.class.getName(), agentList, objtab);
			agentName="3rdAgent";
			objtab=new Object[]{new ArrayList<String>(Arrays.asList("2ndAgent","4thAgent")),rand.nextInt(30) };
			createOneAgent(c, agentName, protocols.gossip.pushsum.agents.PushSumAgent.class.getName(), agentList, objtab);
			agentName="4thAgent";
			objtab=new Object[]{new ArrayList<String>(Arrays.asList("2ndAgent","3rdAgent")),rand.nextInt(30)};
			createOneAgent(c, agentName, protocols.gossip.pushsum.agents.PushSumAgent.class.getName(), agentList, objtab);
			break;
		case PROTOCOLS_GOSSIP_LNS: 
			containerName="Mycontainer2";
			c = containerList.get(containerName);
			int nbAgent = 3;
			agentName="AgentLNS1";
			objtab=new Object[]{"secret1", new ArrayList<String>(Arrays.asList("AgentLNS2", "AgentLNS3")), 3};
			createOneAgent(c, agentName, protocols.gossip.lns.LNSAgent.class.getName(), agentList, objtab);

			agentName="AgentLNS2";
			objtab=new Object[]{"secret2", new ArrayList<String>(Arrays.asList("AgentLNS1", "AgentLNS3")), 3};
			createOneAgent(c, agentName, protocols.gossip.lns.LNSAgent.class.getName(), agentList, objtab);

			agentName="AgentLNS3";
			objtab=new Object[]{"secret3", new ArrayList<String>(Arrays.asList("AgentLNS1", "AgentLNS2")), 3};
			createOneAgent(c, agentName, protocols.gossip.lns.LNSAgent.class.getName(), agentList, objtab);
			break;

		default:
			System.err.println("This example does currently not exist: "+exampleToExecute);
			System.exit(-1);
			break;
		}

		System.out.println("Agents launched...");
		return agentList;
	}

	/**
	 * Create one agent agentName of class className wit parameters  agentOptionnalParameters on container c
	 * @param container containerObject
	 * @param agentName name of the agent
	 * @param className class of the agent
	 * @param agentList list that store the agents'references 
	 * @param agentOptionnalParameters agent's initial parameters that can be retrieved through the getArgument() method in the agent's setup(). Should be null if no params
	 */
	private static void createOneAgent(ContainerController container, String agentName, String className,List<AgentController> agentList, Object[] agentOptionnalParameters) {
		try {						
			AgentController	ag=container.createNewAgent(agentName,className,agentOptionnalParameters);
			agentList.add(ag);
			try {
				System.out.println(agentName+" launched on "+container.getContainerName());
			} catch (ControllerException e) {
				e.printStackTrace();
			}
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
	}



	/**
	 * Start the agents
	 * @param agentList
	 */
	private static void startAgents(List<AgentController> agentList){

		System.out.println("Starting agents...");


		for(final AgentController ac: agentList){
			try {
				ac.start();
			} catch (StaleProxyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		System.out.println("Agents started...");
	}


	/**

	 * Start an auction protocol
	 * @param auctioneers A map<String , Object[]> , which contains the properties of the auctioneers
	 * the key is the name , the value is their properties ("initPrice, increment(decrement if decreasing auction)
	 * , reservePrice (or budget if reverse auction ) ,objectId , waitingTime")
	 * @param bidders A map<String , Object[]> , which contains the properties of the bidders
	 * the properties are ("budget , increment") 
	 * if an auction type doesn't need one of those properties just set it to 0
	 * @param protocol  The type of the protocol
	 * @return the list of the agent controllers
	 * 
	 */
	private static List<AgentController> startAuction(Map<String, Object[]> auctioneers, Map<String,Object[]> bidders, 	Protocol protocol ){
		IAuction auction = null;
		switch(protocol) {
		case EnglishAuction:
			auction = new 	EnglishAuction(false);
			break;
		case ReverseEnglishAuction:
			auction = new 	EnglishAuction(true);
			break;
		case JapaneseAuction:
			auction = new 	JapaneseAuction(false);
			break;
		case ReverseJapaneseAuction:
			auction = new 	JapaneseAuction(true);
			break;
		case DutchAuction:
			auction = new 	DutchAuction(false);
			break;
		case ReverseDutchAuction:
			auction = new 	DutchAuction(true);
			break;
		default:
			System.err.println("This protocol does not exist");
			System.exit(-1);

		}
		System.out.println("Launching agents for Auction example ...");
		ContainerController c;
		String containerName;
		List<AgentController> agentList=new ArrayList<AgentController>();
		containerName="Mycontainer2";
		c = containerList.get(containerName);
		List<Object> list;
		for (Map.Entry<String,Object[]> auctioneer : auctioneers.entrySet()) {
			list = new ArrayList<Object>(Arrays.asList(auction));  
			list.addAll(Arrays.asList(auctioneer.getValue())); 
			createOneAgent(c,auctioneer.getKey(), protocols.auctions.agents.AuctioneerAgent.class.getName(), agentList, list.toArray());
		}
		for (Map.Entry<String,Object[]> bidder : bidders.entrySet()) {
			list = new ArrayList<Object>(Arrays.asList(auction));  
			list.addAll(Arrays.asList(bidder.getValue())); 
			createOneAgent(c, bidder.getKey(), protocols.auctions.agents.BidderAgent.class.getName(), agentList, list.toArray());		
		}
		return agentList;
	}

}







