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
package org.eclipse.e4.ui.workbench.perspectiveswitcher.commands;

/** Ids, names and descriptions of the commands contributed by the perspective switcher. */
public final class E4WorkbenchCommandConstants {

	public static final String CATEGORY_PERSPECTIVES = "org.eclipse.e4.ui.perspectives"; //$NON-NLS-1$
	public static final String CATEGORY_PERSPECTIVES$_NAME = "Perspectives"; //$NON-NLS-1$
	public static final String CATEGORY_PERSPECTIVES$_DESCRIPTION = "Manipulation of workbench perspectives"; //$NON-NLS-1$

	public static final String PERSPECTIVES_SHOW_PERSPECTIVE = "org.eclipse.e4.ui.perspectives.showPerspective"; //$NON-NLS-1$
	public static final String PERSPECTIVES_SHOW_PERSPECTIVE$_NAME = "Show Perspective"; //$NON-NLS-1$
	public static final String PERSPECTIVES_SHOW_PERSPECTIVE$_DESCRIPTION = "Opens a perspective"; //$NON-NLS-1$

	public static final String PERSPECTIVES_SHOW_PERSPECTIVE_HANDLER = "org.eclipse.e4.ui.perspectives.handlers.showPerspective"; //$NON-NLS-1$

	private E4WorkbenchCommandConstants() {
	}
}
