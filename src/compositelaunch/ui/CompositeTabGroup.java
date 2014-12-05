package compositelaunch.ui;

import compositelaunch.ui.tabs.CompositeLaunchConfigurationTab;
import compositelaunch.ui.tabs.ExecutionNumberTab;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

/**
 * Launch Tab Group composite 
 */
public class CompositeTabGroup extends AbstractLaunchConfigurationTabGroup {
	/**
	 * creates all tabs
	 */
	@Override
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		setTabs(new ILaunchConfigurationTab[] { 
					new CompositeLaunchConfigurationTab(), 
					new ExecutionNumberTab() 
    			});
	}
}