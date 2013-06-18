/*******************************************************************************
 * Copyright (c) 2012 Joseph Carroll and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Joseph Carroll <jdsalingerjr@gmail.com> - initial API and implementation
 *     Lars Vogel <lars.vogel@gmail.com> - update to Eclipse 4.3
 ******************************************************************************/ 
package com.sample.application;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class DoWork {
	
	@Inject
	private MApplication application;
	
	@PostConstruct
	void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.None);
		composite.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_GREEN));
	}
	
	
	
	@Focus
	public void onFocus() {
		// TODO: Implement
	}
	
	
}