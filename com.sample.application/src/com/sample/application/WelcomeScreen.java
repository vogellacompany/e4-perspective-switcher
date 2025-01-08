/*******************************************************************************
 * Copyright (c) 2012 Joseph Carroll and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Joseph Carroll <jdsalingerjr@gmail.com> - initial API and implementation
 ******************************************************************************/ 
package com.sample.application;

import jakarta.annotation.PostConstruct;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class WelcomeScreen {
	
	
	@PostConstruct
	void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.None);
		composite.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_BLUE));
	}
	
	
	
	@Focus
	public void onFocus() {
		//TODO Your code here
	}
	
	
}