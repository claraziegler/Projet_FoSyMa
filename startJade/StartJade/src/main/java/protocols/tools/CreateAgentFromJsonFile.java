package protocols.tools;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * This class stores the information required for an agent to be created. 
 * These information are obtained through Json file processing
 *
 */
public class CreateAgentFromJsonFile {
	private String agentName;
	private String className;
	private Object[] params;

	/**
	 * 
	 * @param agentName : the agent name
	 * @param className : the agent class name
	 * @param objtab   : used to give parameters to the agent
	 */
	public CreateAgentFromJsonFile(String agentName, String className, Object[] objtab) {
		super();
		this.agentName = agentName;
		this.className = className;
		this.params = objtab;
	}
	public CreateAgentFromJsonFile() {
		super();
	}
	public String getAgentName() {
		return agentName;
	}
	public String getClassName() {
		return className;
	}
	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public void setParams(Object[] params) {
		this.params = params;
	}
	public Object[] getParams() {
		return params;
	} 

}
