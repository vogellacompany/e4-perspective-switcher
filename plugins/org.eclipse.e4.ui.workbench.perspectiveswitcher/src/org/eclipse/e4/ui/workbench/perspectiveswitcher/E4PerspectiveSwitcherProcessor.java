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
package org.eclipse.e4.ui.workbench.perspectiveswitcher;

import static org.eclipse.e4.ui.workbench.perspectiveswitcher.commands.E4WorkbenchCommandConstants.*;
import static org.eclipse.e4.ui.workbench.perspectiveswitcher.commands.E4WorkbenchParameterConstants.*;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MCategory;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandParameter;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.workbench.perspectiveswitcher.handlers.ShowPerspectiveHandler;
import org.osgi.framework.FrameworkUtil;

/** Adds the "Show Perspective" command and its handler to the application model. */
public class E4PerspectiveSwitcherProcessor {

	@Execute
	void addCommands(MApplication application) {
		// the model is persisted between sessions, so it may already contain the command
		if (application.getCommand(PERSPECTIVES_SHOW_PERSPECTIVE) != null) {
			return;
		}
		String bundleId = FrameworkUtil.getBundle(getClass()).getSymbolicName();
		String contributorURI = "platform:/plugin/" + bundleId; //$NON-NLS-1$
		MCommandsFactory factory = MCommandsFactory.INSTANCE;

		MCategory category = application.getCategories().stream()
				.filter(c -> CATEGORY_PERSPECTIVES.equals(c.getElementId()))
				.findFirst()
				.orElseGet(() -> {
					MCategory created = factory.createCategory();
					created.setContributorURI(contributorURI);
					created.setElementId(CATEGORY_PERSPECTIVES);
					created.setName(CATEGORY_PERSPECTIVES$_NAME);
					created.setDescription(CATEGORY_PERSPECTIVES$_DESCRIPTION);
					application.getCategories().add(created);
					return created;
				});

		MCommand command = factory.createCommand();
		command.setContributorURI(contributorURI);
		command.setElementId(PERSPECTIVES_SHOW_PERSPECTIVE);
		command.setCategory(category);
		command.setCommandName(PERSPECTIVES_SHOW_PERSPECTIVE$_NAME);
		command.setDescription(PERSPECTIVES_SHOW_PERSPECTIVE$_DESCRIPTION);

		MCommandParameter perspectiveId = factory.createCommandParameter();
		perspectiveId.setContributorURI(contributorURI);
		perspectiveId.setElementId(COMMAND_PERSPECTIVE_ID);
		perspectiveId.setName(COMMAND_PERSPECTIVE_ID$_NAME);
		perspectiveId.setOptional(true);
		command.getParameters().add(perspectiveId);
		application.getCommands().add(command);

		MHandler handler = factory.createHandler();
		handler.setContributorURI(contributorURI);
		handler.setElementId(PERSPECTIVES_SHOW_PERSPECTIVE_HANDLER);
		handler.setCommand(command);
		handler.setContributionURI("bundleclass://" + bundleId + "/" + ShowPerspectiveHandler.class.getName()); //$NON-NLS-1$ //$NON-NLS-2$
		application.getHandlers().add(handler);
	}
}
