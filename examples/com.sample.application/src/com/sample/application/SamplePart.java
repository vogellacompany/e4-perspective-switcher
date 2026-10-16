/*******************************************************************************
 * Copyright (c) 2012 Joseph Carroll and others.
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
 *     Lars Vogel <lars.vogel@gmail.com> - update to Eclipse 4.3
 ******************************************************************************/ 
package com.sample.application;

import java.util.Objects;

import jakarta.annotation.PostConstruct;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/** Placeholder content that shows which part, and therefore which perspective, is visible. */
public class SamplePart {

	@PostConstruct
	void createControl(Composite parent, MPart part) {
		GridLayoutFactory.swtDefaults().margins(16, 16).applyTo(parent);
		Label label = new Label(parent, SWT.WRAP);
		label.setText(part.getLocalizedLabel() + "\n\n" + Objects.toString(part.getLocalizedTooltip(), "")); //$NON-NLS-1$
	}
}
