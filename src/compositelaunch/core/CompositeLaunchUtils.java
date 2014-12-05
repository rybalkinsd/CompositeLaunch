package compositelaunch.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

/**
 * Util class provides API to get/store slave launch configurations
 * and theirs executions number.
 *
 */
public class CompositeLaunchUtils {
	
	private static final String SLAVE_ATTR = "Slave";
	private static final String SLAVE_EXEC_NUMBER_ATTR = "SlaveExecNumber";
	
	
	private CompositeLaunchUtils() {
	}
	
	/**
	 * Maps slaves to theirs executions numbers
	 * @param config
	 */
	public static Map<ILaunchConfiguration, Integer> getSlavesWithExecNumbers(ILaunchConfiguration config) {
		if (config == null) {
			return Collections.emptyMap();
		}
		List<ILaunchConfiguration> slaves = getSlaves(config);
		Map<String, Integer> execNumbers = getSlaveExecNumbers(config);
		
		// combine two instances into one map
		return slaves.stream()
				.collect(Collectors.toMap(
						x -> x, 
						x -> execNumbers.get(x.getName())));
	}
	
	
	/**
	 * This method skips renamed and removed configs in
	 * returning list.
	 * @param masterConfig
	 * @return slaves of masterConfig 
	 */
	public static List<ILaunchConfiguration> getSlaves(ILaunchConfiguration masterConfig)  {
		if (masterConfig == null) {
			return Collections.emptyList();
		}
		
		List<ILaunchConfiguration> allConfigs = null;
		final Set<String> storedSlaveNames = new HashSet<>();
		
		try {
			allConfigs = Arrays.asList(DebugPlugin.getDefault()
					.getLaunchManager().getLaunchConfigurations());
			
			storedSlaveNames.addAll(masterConfig.getAttribute(SLAVE_ATTR, Collections.emptySet()));
		} catch (CoreException e) {
			// remove stackTrace
			e.printStackTrace();
			return Collections.emptyList();
		}
		
		// 
		List<ILaunchConfiguration> actualSlaves = allConfigs.stream()
			.filter(conf -> storedSlaveNames.contains(conf.getName()))
			.collect(Collectors.toList());
				
		return actualSlaves;
	}
	
	/** 
	 * @param config
	 * @return mapping from slaveName to theirs executions number
	 */
	public static Map<String, Integer> getSlaveExecNumbers(ILaunchConfiguration config) {
		if (config == null) {
			return Collections.emptyMap();
		}
		
		final Map<String, String> storedSlaveExecNumbers = new HashMap<>();		
		try {
			storedSlaveExecNumbers.putAll(config.getAttribute(SLAVE_EXEC_NUMBER_ATTR, 
					Collections.emptyMap()));
			
		} catch (CoreException e) {
			// remove stackTrace
			e.printStackTrace();
			return Collections.emptyMap();
		}
		
		// collect actual slaves
		List<ILaunchConfiguration> actualSlaves = getSlaves(config);
		Set<String> actualSlavesNames = actualSlaves.stream()
				.map(x -> x.getName())
				.collect(Collectors.toSet());
		
		// filter out-of-date configs
		Map<String, Integer> actualExecNumbers = storedSlaveExecNumbers.entrySet().stream()
			.filter(x -> actualSlavesNames.contains(x.getKey()))
			.collect(Collectors.toMap(
					x -> x.getKey(), 
					x -> Integer.parseInt(x.getValue())));
		
		return actualExecNumbers;
	}
	
	
	/**
	 * Store slaves(slaveNames) in
	 * @param config
	 * @param slaveNames
	 * 
	 */
	public static void setSlaves(ILaunchConfigurationWorkingCopy config, Set<String> slaveNames) {
		if (config == null || slaveNames == null) {
			return;
		}
		
		config.setAttribute(SLAVE_ATTR, slaveNames);	
		acltualizeSlaveExecNumbers(config, slaveNames);
	}

	/**
	 * Stores single slave(slaveName) configs execution number (execNumber) 
	 * in
	 * @param masterConfig
	 * @param slaveName
	 * @param execNumber
	 */
	public static void storeSlaveExecNumber(ILaunchConfigurationWorkingCopy masterConfig, 
											String slaveName, 
											Integer execNumber) {
		
		if (masterConfig == null || slaveName == null || execNumber == null) {
			return;
		}
		
		Map<String, String> slaveExecNumbers;
		try {	
			slaveExecNumbers = masterConfig.getAttribute(SLAVE_EXEC_NUMBER_ATTR, Collections.emptyMap());
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
		
		if (slaveExecNumbers.isEmpty()) {
			Map<String, String> singleExecNumber = new HashMap<>();
			singleExecNumber.put(slaveName, execNumber.toString());
			masterConfig.setAttribute(SLAVE_EXEC_NUMBER_ATTR, singleExecNumber);
			
		} else {
			slaveExecNumbers.put(slaveName, execNumber.toString());
		}
		
	}
	
	/**
	 * 
	 * @param master
	 * @param slave
	 * @return true if relation master -> slave will not cause loops in configs
	 */
	public static boolean isRelationAvaliable(ILaunchConfiguration master, ILaunchConfiguration slave) {
		if (master == null || slave == null) {
			return false;
		}
		return !existsPath(slave, master);
	}
	
	/**
	 * 
	 * @param from
	 * @param to
	 * @return true if path from -> a -> b -> ... -> to exists
	 *  -> is a master -> slave relation 
	 */
	private static boolean existsPath(ILaunchConfiguration from, ILaunchConfiguration to) {
		
		return from.getName().equals(to.getName()) 
				|| getSlaves(from).stream().anyMatch(slave -> existsPath(slave, to));
	}
	
	/**
	 * Method actualizes slaves execution numbers of 
	 * @param config
	 * with
	 * @param slaveNames
	 */
	private static void acltualizeSlaveExecNumbers(ILaunchConfigurationWorkingCopy config, 
												   Set<String> slaveNames) {
		
		Map<String, Integer> execNumbers = getSlaveExecNumbers(config);
		
		// filter out-of-date configs
		execNumbers.keySet().retainAll(slaveNames);
		
		// add new slave configs with initial execution number
		slaveNames.forEach(slave -> {
			if (!execNumbers.containsKey(slave)) { 
				storeSlaveExecNumber(config, slave, 1);
			}
		});		
	}
}
