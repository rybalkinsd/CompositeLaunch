package compositelaunch.ui.tabs;

public class ConfigExecNumber {
	
	/**
	 * configuration name
	 */
	private String name;
	
	/**
	 * number of executions
	 */
	private Integer number;

	
	public ConfigExecNumber(String name, Integer number) {
		this.name = name;
		this.number = number;
	}

	public String getName() {
		return name;
	}

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}
  
}
