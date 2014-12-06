
package compositelaunch.ui.tabs;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.MultipleInputDialog;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.internal.ui.launchConfigurations.EnvironmentVariable;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsMessages;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;

import compositelaunch.core.CompositeLaunchUtils;

/**
 * Tab with Table of pairs:  slave configuration -> exec number
 *
 */
public class ExecutionNumberTab extends AbstractLaunchConfigurationTab {
	
	private static final String VALUE_LABEL = LaunchConfigurationsMessages.EnvironmentTab_9;
	private static final String[] columnHeaders = { 
	    "Sub Launches", "Execution number" };
  
	private static final String P_VARIABLE = "variable";
	private static final String P_VALUE = "value";
	
	private TableViewer launchConfigurationsTable;
	private Button editButton;
	private Map<String, Integer> modifyActions = new HashMap<>();
	private ILaunchConfiguration config;
	
	@Override
  	public void createControl(Composite parent) {
  		Composite mainComposite = SWTFactory.createComposite(parent, 2, 1, 768);
  		setControl(mainComposite);
  		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), getHelpContextId());

  		createExecturionNumberTable(mainComposite);
  		createEditButtons(mainComposite);
  		Dialog.applyDialogFont(mainComposite);
  	}
  	
  	@Override
  	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
  		// do nothing
  	}

  	@Override
  	public void initializeFrom(ILaunchConfiguration configuration) {
  		this.config = configuration;
  		updateExecNumbers(configuration);
  	}

  	@Override
  	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
  		// do nothing (everything is done before)
  	}
  	
  	@Override
  	public String getName() {
  		return "Execution Number";
  	}

  	@Override
  	public String getId() {
  		return "Execution Number";
  	}

  	@Override
  	public Image getImage() {
  		return DebugPluginImages.getImage("IMG_OBJS_ENVIRONMENT");
  	}

  	/**
  	 * Updates tab on activate action
  	 */
  	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
  		updateExecNumbers(workingCopy);
  	}

  	public void deactivated(ILaunchConfigurationWorkingCopy workingCopy) { 
  		//do nothing
  	}
  	
  	/**
  	 * 
  	 * @param parent
  	 * All magic constants describes style properties of table
  	 */
  	private void createExecturionNumberTable(Composite parent) {
  		Font font = parent.getFont();
 		
  		Composite tableComposite = SWTFactory.createComposite(parent, font, 1, 1, 1808, 0, 0);

  		launchConfigurationsTable = new TableViewer(tableComposite, 68354);

	    Table table = launchConfigurationsTable.getTable();
	    table.setLayout(new GridLayout());
	    table.setLayoutData(new GridData(1808));
	    table.setHeaderVisible(true);
	    table.setLinesVisible(true);
	    table.setFont(font);
	    launchConfigurationsTable.setContentProvider(new ExecNumberContentProvider());
	    launchConfigurationsTable.setLabelProvider(new LaunchNumberLabelProvider());
	    launchConfigurationsTable.setColumnProperties(new String[] { "variable", "value" });
	    launchConfigurationsTable.addSelectionChangedListener(new ISelectionChangedListener() {
	    	@Override
	    	public void selectionChanged(SelectionChangedEvent event) {
	    		handleTableSelectionChanged(event);
	    	}
	    });
    
	    launchConfigurationsTable.addDoubleClickListener(new IDoubleClickListener() {
	    	@Override
	    	public void doubleClick(DoubleClickEvent event) {
	    		if (!launchConfigurationsTable.getSelection().isEmpty())
	    			handleExecNumberEditButtonSelected();
	    	}
	    });
	    
	    costomizeTable(table, tableComposite);
  	}
  	
  	/**
  	 * Table customization method
  	 * @param table
  	 * @param tableComposite
  	 */
  	private void costomizeTable(Table table, Composite tableComposite) {
  		final TableColumn tc1 = new TableColumn(table, 0, 0);
	    tc1.setText(columnHeaders[0]);
	    final TableColumn tc2 = new TableColumn(table, 0, 1);
	    tc2.setText(columnHeaders[1]);
	    final Table tref = table;
	    final Composite comp = tableComposite;
	    tableComposite.addControlListener(new ControlAdapter() {
	    	public void controlResized(ControlEvent e) {
	    		Rectangle area = comp.getClientArea();
	    		Point size = tref.computeSize(-1, -1);
	    		ScrollBar vBar = tref.getVerticalBar();
	    		int width = area.width - tref.computeTrim(0, 0, 0, 0).width - 2;
	    		if (size.y > area.height + tref.getHeaderHeight()) {
	    			Point vBarSize = vBar.getSize();
	    			width -= vBarSize.x;
	    		}
	    		Point oldSize = tref.getSize();
	    		
	    		if (oldSize.x > area.width) {
	    			tc1.setWidth(width / 2 - 1);
	    			tc2.setWidth(width - tc1.getWidth());
	    			tref.setSize(area.width, area.height);
	    		} else {
	    			tref.setSize(area.width, area.height);
	    			tc1.setWidth(width / 2 - 1);
	    			tc2.setWidth(width - tc1.getWidth());
	    		}
	    	}
	    });
		
	}

	private void handleTableSelectionChanged(SelectionChangedEvent event) {
  		int size = ((IStructuredSelection)event.getSelection()).size();
  		editButton.setEnabled(size == 1);
  	}

	/**
	 * Creates edit button 
	 * @param parent
	 * 
	 * All magic constants describes style properties of button
	 */
  	private void createEditButtons(Composite parent) {
  		Composite buttonComposite 
  			= SWTFactory.createComposite(parent, parent.getFont(), 1, 1, 130, 0, 0);

  		editButton = createPushButton(buttonComposite, 
  				LaunchConfigurationsMessages.EnvironmentTab_Edit_5, null);
  		
  		editButton.addSelectionListener(new SelectionAdapter() {
  			@Override
  			public void widgetSelected(SelectionEvent event) {
  				handleExecNumberEditButtonSelected();
  			}
  		});
  		
  		editButton.setEnabled(false);
  	}

  	private void updateExecNumbers(ILaunchConfiguration configuration) {
  		modifyExecNumbers(configuration);
  		launchConfigurationsTable.setInput(configuration);
  	}

  	private void handleExecNumberEditButtonSelected() {
  		IStructuredSelection sel = (IStructuredSelection) launchConfigurationsTable.getSelection();
  		ConfigExecNumber configurationLaunchNumber = (ConfigExecNumber) sel.getFirstElement();

  		if (configurationLaunchNumber == null) {
  			return;
  		}
  		String launchName = configurationLaunchNumber.getName();
  		MultipleInputDialog dialog = new MultipleInputDialog(getShell(), 
  				launchName + " executions");

  		String value = configurationLaunchNumber.getNumber().toString();
  		dialog.addTextField(VALUE_LABEL, value, true);

  		if (dialog.open() != 0) {
  			return;
  		}

  		value = dialog.getStringValue(VALUE_LABEL);
  		try {
  			Integer intValue = Integer.valueOf(Integer.parseInt(value));
  			if (intValue.intValue() > 0) {
  				configurationLaunchNumber.setNumber(intValue);
  				modifyActions.put(launchName, intValue);
  				updateExecNumbers(config);
  			} else {
  				showFailureMessage();
  			}
  		} catch (NumberFormatException e) {
  			showFailureMessage();
  		}
  }

  	private void showFailureMessage() {
  		MessageBox dialog = new MessageBox(getShell(), 292);
  		dialog.setText("Info");
  		dialog.setMessage("Value can not be interpret as execution number.");
  		dialog.open();
  	}

  	private void modifyExecNumbers(ILaunchConfiguration configuration) {
  		ILaunchConfigurationWorkingCopy configCopy = null;
  		try {
  			configCopy = configuration.getWorkingCopy();
  		} catch (CoreException e) {
  			throw new RuntimeException(e);
  		}

  		for (Map.Entry<String, Integer> action : modifyActions.entrySet()) {
  			CompositeLaunchUtils.storeSlaveExecNumber(configCopy, 
  					action.getKey(), action.getValue());
  		}

  		this.modifyActions = new HashMap<>();
  	}

  	
  	private static class LaunchNumberLabelProvider extends LabelProvider implements ITableLabelProvider {
  		
  		@Override
  		public String getColumnText(Object element, int columnIndex) {
  			String result = null;
  			ConfigExecNumber var = (ConfigExecNumber) element;
  			if (element != null) {
  				switch (columnIndex) {
  					case 0:
  						result = var.getName();
  						break;
  					case 1:
  						result = var.getNumber().toString();
  						break;
  				}
  			}
	
  			return result;
  		}
  		
  		@Override
	    public Image getColumnImage(Object element, int columnIndex) {
	    	if (columnIndex == 0) {
	    		return DebugPluginImages.getImage("IMG_OBJS_ENV_VAR");
	    	}
	
	    	return null;
	    }
  	}
  	
  
	private static class ExecNumberContentProvider implements IStructuredContentProvider {
		@Override
		public Object[] getElements(Object inputElement) {
			ILaunchConfiguration config = (ILaunchConfiguration) inputElement;
			Map<String, Integer> execNumbers = CompositeLaunchUtils.getSlaveExecNumbers(config);
			
			// map to ConfigurationLaunchNumbers and return as Array
			return execNumbers.entrySet().stream()
				.map(x -> new ConfigExecNumber(x.getKey(), x.getValue()))
				.collect(Collectors.toList())
				.toArray();
		}
		
		@Override
		public void dispose() { }
		
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) { }
		
	}
  
}