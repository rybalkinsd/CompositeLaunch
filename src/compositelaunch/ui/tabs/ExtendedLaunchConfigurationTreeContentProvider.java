package compositelaunch.ui.tabs;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationTreeContentProvider;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsMessages;
import org.eclipse.swt.widgets.Shell;

import compositelaunch.core.CompositeLaunchUtils;

/**
 * Specific Content provider.
 * Provides only allowed content.
 * 
 * @see getChildren method
 *
 */
public class ExtendedLaunchConfigurationTreeContentProvider extends LaunchConfigurationTreeContentProvider {

	/**
	 * Empty Object array
	 */
	private static final Object[] EMPTY_ARRAY = new Object[0];	
	
	private ILaunchConfiguration masterConfig;
	
	public ExtendedLaunchConfigurationTreeContentProvider(String mode, ILaunchConfiguration masterConfig) {
		super(mode, null);
		this.masterConfig = masterConfig;
		
	}
	
	/**
	 * @return only allowed slave configs to be
	 */
	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof ILaunchConfiguration) {
			return EMPTY_ARRAY;
		} else if (parentElement instanceof ILaunchConfigurationType) {
			try {
				ILaunchConfigurationType type = (ILaunchConfigurationType) parentElement;
				List<ILaunchConfiguration> configs = Arrays.asList(getLaunchManager().getLaunchConfigurations(type));
				if (masterConfig == null) {
					return configs.toArray(); 
				}
				
				// retain only possible slaves
				return configs.stream()
						.filter(conf -> CompositeLaunchUtils.isRelationAvaliable(masterConfig, conf))
						.collect(Collectors.toList())
						.toArray();
				
			} catch (CoreException e) {
				// exception handler pull from base class
				DebugUIPlugin.errorDialog(null, LaunchConfigurationsMessages.LaunchConfigurationDialog_Error_19, 
						LaunchConfigurationsMessages.LaunchConfigurationDialog_An_exception_occurred_while_retrieving_launch_configurations_20, e);
			}
		} else {
			return getLaunchManager().getLaunchConfigurationTypes();
		}
		return EMPTY_ARRAY;
	}

	
	/**
	 * Convenience method to get the singleton launch manager.
	 */
	private ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}
}
