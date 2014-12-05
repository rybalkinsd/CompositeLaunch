package compositelaunch.ui.tabs;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationTreeContentProvider;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.osgi.internal.messages.Msg;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;

import compositelaunch.core.CompositeLaunchUtils;

/**
 * Tab with checkboxTree of all (allowed) slave configs to be.
 *
 */
public class CompositeLaunchConfigurationTab extends AbstractLaunchConfigurationTab {	

	private CheckboxTreeViewer configurationsViewer;
	

	@Override
	public void createControl(Composite parent) {
		ILabelDecorator labelDecorator = PlatformUI.getWorkbench().
										getDecoratorManager().
										getLabelDecorator();
		IBaseLabelProvider labelProvider = new DecoratingLabelProvider(
										DebugUITools.newDebugModelPresentation(), 
										labelDecorator);
		
		ITreeContentProvider contentProvider 
			= new ExtendedLaunchConfigurationTreeContentProvider(
					getLaunchConfigurationDialog().getMode(), null);
		
		configurationsViewer = new ContainerCheckedTreeViewer(parent);
		configurationsViewer.setContentProvider(contentProvider);
		configurationsViewer.setLabelProvider(labelProvider);
		configurationsViewer.setInput(ResourcesPlugin.getWorkspace());
		configurationsViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				updateLaunchConfigurationDialog();
			}
		});
		
		setControl(configurationsViewer.getTree());
	}
	
	
	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		updateContentProvider(configuration);
		Object[] slaves = CompositeLaunchUtils.getSlaves(configuration).toArray();
		configurationsViewer.setCheckedElements(slaves);
	}
	
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy config) {
		Object[] checkedElements = configurationsViewer.getCheckedElements();
		Set<String> slaveNames = retainSlaveNames(checkedElements);
		CompositeLaunchUtils.setSlaves(config, slaveNames);
	}
	
	@Override
	public String getName() {
		return "Sub launches";
	}
	
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		//do nothing
	}	
	
	/**
	 * Retains only slaves names from
	 * @param items 
	 * @return slave configs names
	 */
	private Set<String> retainSlaveNames(Object[] items) {
		return Arrays.stream(items)
					.filter(x -> x instanceof ILaunchConfiguration)
					.map(x -> ((ILaunchConfiguration) x).getName())
					.collect(Collectors.toSet());
	}
	
	private void updateContentProvider(ILaunchConfiguration configuration) {
		ITreeContentProvider contentProvider 
			= new ExtendedLaunchConfigurationTreeContentProvider(
				getLaunchConfigurationDialog().getMode(), configuration);
		
		configurationsViewer.setContentProvider(contentProvider);
	}
}
