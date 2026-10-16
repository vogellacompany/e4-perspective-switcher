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
package org.eclipse.e4.ui.workbench.perspectiveswitcher.handlers;

import jakarta.inject.Named;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.perspectiveswitcher.commands.E4WorkbenchParameterConstants;
import org.eclipse.e4.ui.workbench.perspectiveswitcher.internal.dialogs.SelectPerspectiveDialog;

/** Shows the perspective given as parameter, or lets the user pick one if there is none. */
public final class ShowPerspectiveHandler {

	@Execute
	public void execute(IEclipseContext context, MWindow window, EModelService modelService, EPartService partService,
			@Optional @Named(E4WorkbenchParameterConstants.COMMAND_PERSPECTIVE_ID) String perspectiveId) {
		if (perspectiveId == null || perspectiveId.isBlank()) {
			ContextInjectionFactory.make(SelectPerspectiveDialog.class, context).open();
			return;
		}
		modelService.findElements(window, perspectiveId, MPerspective.class).stream()
				.findFirst()
				.ifPresent(perspective -> show(perspective, partService));
	}

	/** Switches to the perspective, bringing it back first if it was closed. */
	public static void show(MPerspective perspective, EPartService partService) {
		perspective.setToBeRendered(true);
		partService.switchPerspective(perspective);
	}
}
