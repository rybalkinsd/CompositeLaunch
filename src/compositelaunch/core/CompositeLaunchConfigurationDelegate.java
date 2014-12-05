package compositelaunch.core;

import java.util.Map;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;

/**
 * Delegate class provides running each slave configuration 
 * exec number times.
 *
 */
public class CompositeLaunchConfigurationDelegate implements ILaunchConfigurationDelegate {

	@Override
	public void launch(ILaunchConfiguration configuration, 
					   String mode, 
					   ILaunch launch, 
					   IProgressMonitor monitor) throws CoreException {
		
		Map<ILaunchConfiguration, Integer> slaves 
				= CompositeLaunchUtils.getSlavesWithExecNumbers(configuration);
		
		for (Map.Entry<ILaunchConfiguration, Integer> slave : slaves.entrySet()) {
			for (int index = 0; index < slave.getValue(); index++) {
				slave.getKey().launch(mode, monitor);
			}
			
		}
	}
	
}