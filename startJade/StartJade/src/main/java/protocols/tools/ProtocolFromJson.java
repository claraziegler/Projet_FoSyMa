package protocols.tools;

import java.io.File;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.ObjectMapper;

import protocols.Protocol;
import protocols.auctions.dutchAuction.DutchAuction;
import protocols.auctions.englishAuction.EnglishAuction;
import protocols.auctions.japaneseAuction.JapaneseAuction;
import protocols.consensus.paxos.Paxos;
import protocols.gossip.pushsum.PushSumProtocol;
import protocols.gossip.lns.LearnNewSecrets;

/**
 * 
 * This class look for the type of protocol that is described within the json file then delegate the associated loading to the class its related to.
 *
 */
public class ProtocolFromJson {
	private Protocol protocolName;
    private Map<String, Object> properties = new HashMap<>();

    @JsonAnySetter
    public void set(String fieldName, Object value){
        this.properties.put(fieldName, value);
    }

    public Object get(String fieldName){
        return this.properties.get(fieldName);
    }
    
	public Protocol getProtocolName() {
		return protocolName;
	}

	public void setProtocolName(Protocol protocol) {
		this.protocolName = protocol;
	}
	/**
	 * 
	 * @param file the protocol's instantiation description
	 * @return the list of agents' description that need to be created, with their expected parameters
	 */
	public static List<CreateAgentFromJsonFile> readFromJson(File file) {
		ObjectMapper mapper = new ObjectMapper();

		ProtocolFromJson pr = null;
		try {
			 pr = mapper.readValue(file, ProtocolFromJson.class);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("- Protocol Name : "+pr.getProtocolName());
		List<CreateAgentFromJsonFile> obj = new ArrayList<CreateAgentFromJsonFile>();
		switch(pr.getProtocolName()) {
		case EnglishAuction: case ReverseEnglishAuction:
			obj = EnglishAuction.getProperties(pr);
			break;
		case JapaneseAuction: case ReverseJapaneseAuction:
			obj = JapaneseAuction.getProperties(pr);
			break;
		case DutchAuction: case ReverseDutchAuction:
			obj = DutchAuction.getProperties(pr);
			break;
		case BasicPaxos : case MultiPaxos :
			obj = Paxos.getProperties(pr);
			break;
		case PushSum :
			obj = PushSumProtocol.getProperties(pr);
			break;
		case LNS:
			obj = LearnNewSecrets.getProperties(pr);
			break;
		default:
			System.err.println("Unknown protocol");
			System.exit(0);
			break;
			
		}
		return obj;
		
		
	}


}
