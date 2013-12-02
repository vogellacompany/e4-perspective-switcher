/*******************************************************************************
 * Copyright (c) 2012 - 2013 Joseph Carroll and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Joseph Carroll <jdsalingerjr@gmail.com> - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - ongoing maintenance
 ******************************************************************************/ 
package org.eclipse.e4.ui.workbench.perspectiveswitcher.internal.dialogs;

import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.perspectiveswitcher.commands.E4WorkbenchCommandConstants;
import org.eclipse.e4.ui.workbench.perspectiveswitcher.commands.E4WorkbenchParameterConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * A dialog for perspective creation
 */
@Creatable
public class SelectPerspectiveDialog extends Dialog implements
        ISelectionChangedListener {

	@Inject
	private IEclipseContext context;
	
	@Inject
	private ECommandService commandService;
	
	@Inject
	private EHandlerService handlerService;
	
	@Inject
	private MWindow window;
	
    final private static int LIST_HEIGHT = 300;

    final private static int LIST_WIDTH = 300;

    private TableViewer viewer;

    private Button okButton;
    
    Object selection;

    private Button showAllButton;

    /**
     * PerspectiveDialog constructor comment.
     * 
     * @param parentShell the parent shell
     */
    @Inject
    public SelectPerspectiveDialog(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {
        super(shell);
		setShellStyle(getShellStyle() | SWT.SHEET);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
     */
    @Override
    protected void cancelPressed() {
        selection = null;
        super.cancelPressed();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(Shell shell) {
        shell.setText("Open Perspective");
        // TODO: Set help context
        
        setBlockOnOpen(false);
        super.configureShell(shell);
    }

    /**
     * Creates and returns the contents of the upper part of this dialog (above
     * the button bar).
     * 
     * @param parent the parent composite to contain the dialog area
     * @return the dialog area control
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        // Run super.
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setFont(parent.getFont());

        createViewer(composite);
        layoutTopControl(viewer.getControl());
        
        // Not applicable in E4, unless there is a usecase for showing
        // perspective not in the current window.
        // if (needsShowAllButton()) {
        //	createShowAllButton(composite);
        // }

        // Return results.
        return composite;
    }

    /**
     * Create a show all button in the parent.
     * 
     * @param parent the parent <code>Composite</code>.
     */
    private void createShowAllButton(Composite parent) {
        showAllButton = new Button(parent, SWT.CHECK);
        showAllButton.setText("Show All");
        showAllButton.addSelectionListener(new SelectionAdapter() {
        	
        	@Override
            public void widgetSelected(SelectionEvent e) {
                if (showAllButton.getSelection())
                    viewer.resetFilters();
                // else
                // 	viewer.addFilter(activityViewerFilter);
            }
        });
    }

    /**
     * Create a new viewer in the parent.
     * 
     * @param parent the parent <code>Composite</code>.
     */
    private void createViewer(Composite parent) {
        // Add perspective list.
        viewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        viewer.getTable().setFont(parent.getFont());
        
		viewer.setLabelProvider(new DelegatingLabelProviderWithTooltip(
				ContextInjectionFactory.make(PerspectiveLabelProvider.class, context),
				ContextInjectionFactory.make(PerspectiveLabelDecorator.class, context)) {
			
			@Override
			protected Object unwrapElement(Object element) {
				// We do not need to unwrap the element
				return element;
			}
		});
        
		viewer.setContentProvider(
				ContextInjectionFactory.make(PerspectiveContentProvider.class, context));
        // list.addFilter(activityViewerFilter);
        viewer.setComparator(new ViewerComparator());
        viewer.setInput(window);
        viewer.addSelectionChangedListener(this);
        viewer.addDoubleClickListener(new IDoubleClickListener() {
        	
        	@Override
            public void doubleClick(DoubleClickEvent event) {
                handleDoubleClickEvent();
            }
        });
    }

    /**
     * Handle a double click event on the list
     */
    protected void handleDoubleClickEvent() {
        okPressed();
    }

    /**
     * Layout the top control.
     * 
     * @param control the control.
     */
    private void layoutTopControl(Control control) {
        GridData spec = new GridData(GridData.FILL_BOTH);
        spec.widthHint = LIST_WIDTH;
        spec.heightHint = LIST_HEIGHT;
        control.setLayoutData(spec);
    }

    /**
     * Notifies that the selection has changed.
     * 
     * @param event event object describing the change
     */
    @Override
    public void selectionChanged(SelectionChangedEvent event) {
        updateSelection(event);
        updateButtons();
    }

    /**
     * Update the button enablement state.
     */
    protected void updateButtons() {
        okButton.setEnabled(selection != null);
    }

    /**
     * Update the selection object.
     */
    protected void updateSelection(SelectionChangedEvent event) {
        selection = null;
        IStructuredSelection _sel = (IStructuredSelection) event.getSelection();
        if (!_sel.isEmpty()) {
            Object obj = _sel.getFirstElement();
            if (obj instanceof MPerspective)
				selection = obj;
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
    	HashMap<String,String> parameters = new HashMap<String,String>(2);
    	parameters.put(E4WorkbenchParameterConstants.COMMAND_PERSPECTIVE_ID, 
    			((MPerspective) selection).getElementId());
    	parameters.put(E4WorkbenchParameterConstants.COMMAND_PERSPECTIVE_NEW_WINDOW,
    			"false");
    	
		ParameterizedCommand command = commandService
				.createCommand(E4WorkbenchCommandConstants.PERSPECTIVES_SHOW_PERSPECTIVE, parameters);
		handlerService.executeHandler(command);
		super.okPressed();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#isResizable()
     */
    @Override
    protected boolean isResizable() {
    	return true;
    }
}
