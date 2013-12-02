/*******************************************************************************
 * Copyright (c) 2012 - 2013 Joseph Carroll and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Joseph Carroll <jdsalingerjr@gmail.com> - initial API and implementation
 * 	   Lars Vogel <Lars.Vogel@gmail.com> - ongoing maintenance
 ******************************************************************************/ 
package org.eclipse.e4.ui.workbench.perspectiveswitcher;

import org.eclipse.e4.ui.internal.workbench.URIHelper;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class E4PerspectiveSwitcherActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.e4.ui.workbench.perspectiveswitcher"; //$NON-NLS-1$
	
	public static final String RESOURCE_SCHEMA = "bundleclass://"; //$NON-NLS-1$
	public static final String RESOURCE_SEPARATOR = "/"; //$NON-NLS-1$

	// The shared instance
	private static E4PerspectiveSwitcherActivator plugin;
	
	/**
	 * The constructor
	 */
	public E4PerspectiveSwitcherActivator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static E4PerspectiveSwitcherActivator getDefault() {
		return plugin;
	}
	
	/**
	 * Returns the platform URI of the bundle
	 * 
	 * @return the platform URI
	 */
	public String getPlatformURI() {
		return URIHelper.constructPlatformURI(this.getBundle());
	}
	
	/**
	 * Returns the platform resource URI for the provided class
	 * 
	 * @param clazz the class to get the resource URI for
	 * @return the platform resource URI
	 */
	public String getResourceURI(Class<?> clazz) {
		return RESOURCE_SCHEMA + PLUGIN_ID + RESOURCE_SEPARATOR + clazz.getCanonicalName();
	}

}
