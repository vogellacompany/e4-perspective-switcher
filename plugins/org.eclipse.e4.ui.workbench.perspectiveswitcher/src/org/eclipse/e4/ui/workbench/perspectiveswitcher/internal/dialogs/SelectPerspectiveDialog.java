/*******************************************************************************
 * Copyright (c) 2012, 2026 Joseph Carroll and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Joseph Carroll <jdsalingerjr@gmail.com> - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - ongoing maintenance
 ******************************************************************************/
package org.eclipse.e4.ui.workbench.perspectiveswitcher.internal.dialogs;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.IResourceUtilities;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.perspectiveswitcher.handlers.ShowPerspectiveHandler;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/** Lets the user pick one of the perspectives of the active window and switches to it. */
public class SelectPerspectiveDialog extends Dialog {

	@Inject
	private Logger logger;

	@Inject
	private EModelService modelService;

	@Inject
	private EPartService partService;

	@Inject
	private IResourceUtilities<ImageDescriptor> resourceUtilities;

	@Inject
	private MWindow window;

	private TableViewer viewer;

	@Inject
	public SelectPerspectiveDialog(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {
		super(shell);
		setShellStyle(getShellStyle() | SWT.SHEET);
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Open Perspective"); //$NON-NLS-1$
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		LocalResourceManager resources = new LocalResourceManager(JFaceResources.getResources(), area);

		viewer = new TableViewer(area, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, true).hint(300, 300).applyTo(viewer.getControl());
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				MPerspective perspective = (MPerspective) element;
				String label = perspective.getLocalizedLabel();
				if (label == null || label.isBlank()) {
					label = perspective.getElementId();
				}
				boolean active = perspective.getParent() != null && perspective.getParent().getSelectedElement() == perspective;
				return active ? label + " (active)" : label; //$NON-NLS-1$
			}

			@Override
			public Image getImage(Object element) {
				ImageDescriptor descriptor = getIconDescriptor((MPerspective) element);
				return descriptor != null ? resources.create(descriptor) : null;
			}
		});
		viewer.setComparator(new ViewerComparator());
		viewer.setInput(modelService.findElements(window, null, MPerspective.class));
		viewer.addSelectionChangedListener(e -> getButton(IDialogConstants.OK_ID).setEnabled(!e.getSelection().isEmpty()));
		viewer.addDoubleClickListener(e -> okPressed());
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		getButton(IDialogConstants.OK_ID).setEnabled(false);
	}

	@Override
	protected void okPressed() {
		if (viewer.getStructuredSelection().getFirstElement() instanceof MPerspective perspective) {
			ShowPerspectiveHandler.show(perspective, partService);
			super.okPressed();
		}
	}

	private ImageDescriptor getIconDescriptor(MPerspective perspective) {
		String iconURI = perspective.getIconURI();
		if (iconURI == null || iconURI.isBlank()) {
			return null;
		}
		try {
			return resourceUtilities.imageDescriptorFromURI(URI.createURI(iconURI));
		} catch (RuntimeException e) {
			logger.debug(e, "Cannot load perspective icon " + iconURI); //$NON-NLS-1$
			return null;
		}
	}
}
