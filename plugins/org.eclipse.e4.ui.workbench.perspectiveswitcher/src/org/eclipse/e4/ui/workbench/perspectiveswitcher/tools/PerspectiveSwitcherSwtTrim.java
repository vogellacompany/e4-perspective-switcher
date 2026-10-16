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
package org.eclipse.e4.ui.workbench.perspectiveswitcher.tools;

import java.util.Map;
import java.util.stream.Stream;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.IResourceUtilities;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.perspectiveswitcher.commands.E4WorkbenchCommandConstants;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.osgi.service.event.Event;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Tool control that shows a button per open perspective of its window, plus one
 * that opens the perspective selection dialog. Reference it from an
 * {@code MToolControl} in a trim bar.
 */
public class PerspectiveSwitcherSwtTrim {

	public static final String PREFERENCES_NODE = "org.eclipse.e4.ui.workbench.perspectiveswitcher"; //$NON-NLS-1$
	public static final String SHOW_TEXT = "perspective_switcher_show_text"; //$NON-NLS-1$

	@Inject
	private Logger logger;

	@Inject
	private EModelService modelService;

	@Inject
	private EPartService partService;

	@Inject
	private ECommandService commandService;

	@Inject
	private EHandlerService handlerService;

	@Inject
	private IResourceUtilities<ImageDescriptor> resourceUtilities;

	@Inject
	private MWindow window;

	@Inject
	@Preference(nodePath = PREFERENCES_NODE)
	private IEclipsePreferences preferences;

	private Composite composite;
	private ToolBar toolBar;
	private LocalResourceManager resources;
	private boolean showText;

	@PostConstruct
	void createControl(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		RowLayout layout = new RowLayout(SWT.HORIZONTAL);
		layout.marginLeft = 6;
		layout.marginRight = 4;
		layout.marginTop = layout.marginBottom = 0;
		layout.center = true;
		composite.setLayout(layout);
		composite.addPaintListener(this::paintBorder);
		resources = new LocalResourceManager(JFaceResources.getResources(), composite);

		toolBar = new ToolBar(composite, SWT.FLAT | SWT.WRAP | SWT.RIGHT);
		toolBar.addMenuDetectListener(this::showContextMenu);

		ToolItem openDialog = new ToolItem(toolBar, SWT.PUSH);
		ResourceLocator.imageDescriptorFromBundle(getClass(), "icons/open_perspective.svg") //$NON-NLS-1$
				.ifPresent(descriptor -> openDialog.setImage(resources.create(descriptor)));
		openDialog.setToolTipText("Open Perspective..."); //$NON-NLS-1$
		openDialog.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> openPerspectiveDialog()));

		new ToolItem(toolBar, SWT.SEPARATOR);

		refreshShortcuts();
	}

	@Inject
	void setShowText(@Preference(nodePath = PREFERENCES_NODE, value = SHOW_TEXT) boolean showText) {
		this.showText = showText;
		if (isAlive()) {
			shortcuts().forEach(this::updateShortcut);
			composite.getParent().layout(true, true);
		}
	}

	@Inject
	@Optional
	void onPerspectivesChanged(@UIEventTopic(UIEvents.ElementContainer.TOPIC_CHILDREN) Event event) {
		if (event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MPerspectiveStack stack && isInWindow(stack)) {
			refreshShortcuts();
		}
	}

	@Inject
	@Optional
	void onToBeRenderedChanged(@UIEventTopic(UIEvents.UIElement.TOPIC_TOBERENDERED) Event event) {
		if (event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MPerspective perspective && isInWindow(perspective)) {
			refreshShortcuts();
		}
	}

	@Inject
	@Optional
	void onSelectionChanged(@UIEventTopic(UIEvents.ElementContainer.TOPIC_SELECTEDELEMENT) Event event) {
		if (event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MPerspectiveStack stack && isInWindow(stack)) {
			shortcuts().forEach(item -> item.setSelection(item.getData() == stack.getSelectedElement()));
		}
	}

	@Inject
	@Optional
	void onLabelChanged(@UIEventTopic(UIEvents.UILabel.TOPIC_ALL) Event event) {
		if (event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MPerspective perspective && isInWindow(perspective)) {
			shortcuts().filter(item -> item.getData() == perspective).forEach(this::updateShortcut);
		}
	}

	private boolean isAlive() {
		return toolBar != null && !toolBar.isDisposed();
	}

	private boolean isInWindow(MUIElement element) {
		return isAlive() && modelService.getTopLevelWindowFor(element) == window;
	}

	private Stream<ToolItem> shortcuts() {
		return Stream.of(toolBar.getItems()).filter(item -> item.getData() instanceof MPerspective);
	}

	/** Recreates the perspective buttons from the model, so that their order follows the model. */
	private void refreshShortcuts() {
		shortcuts().forEach(ToolItem::dispose);
		for (MPerspective perspective : modelService.findElements(window, null, MPerspective.class)) {
			if (perspective.isToBeRendered()) {
				ToolItem item = new ToolItem(toolBar, SWT.RADIO);
				item.setData(perspective);
				item.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
					// a radio item also reports being deselected
					if (item.getSelection()) {
						partService.switchPerspective(perspective);
					}
				}));
				updateShortcut(item);
			}
		}
		composite.getParent().layout(true, true);
	}

	private void updateShortcut(ToolItem item) {
		MPerspective perspective = (MPerspective) item.getData();
		item.setImage(getIcon(perspective));
		String label = perspective.getLocalizedLabel() != null ? perspective.getLocalizedLabel() : ""; //$NON-NLS-1$
		if (showText || item.getImage() == null) {
			item.setText(label);
			item.setToolTipText(perspective.getLocalizedTooltip());
		} else {
			item.setText(""); //$NON-NLS-1$
			item.setToolTipText(label);
		}
		item.setSelection(perspective.getParent() != null && perspective.getParent().getSelectedElement() == perspective);
	}

	private Image getIcon(MPerspective perspective) {
		String iconURI = perspective.getIconURI();
		if (iconURI == null || iconURI.isBlank()) {
			return null;
		}
		try {
			ImageDescriptor descriptor = resourceUtilities.imageDescriptorFromURI(URI.createURI(iconURI));
			return descriptor != null ? resources.create(descriptor) : null;
		} catch (RuntimeException e) {
			logger.debug(e, "Cannot load perspective icon " + iconURI); //$NON-NLS-1$
			return null;
		}
	}

	/** Draws a small rounded edge on the left, separating the switcher from the trim before it. */
	private void paintBorder(PaintEvent event) {
		int height = composite.getSize().y;
		event.gc.setAntialias(SWT.ON);
		event.gc.setForeground(event.display.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
		event.gc.drawPolyline(new int[] { 0, height - 1, 1, height - 1, 2, height - 2, 2, 1, 3, 0 });
	}

	private void showContextMenu(MenuDetectEvent event) {
		ToolItem item = toolBar.getItem(toolBar.toControl(event.x, event.y));
		if (item == null || !(item.getData() instanceof MPerspective perspective)) {
			return;
		}
		Menu menu = new Menu(toolBar);

		MenuItem close = new MenuItem(menu, SWT.PUSH);
		close.setText("&Close"); //$NON-NLS-1$
		close.setEnabled(shortcuts().count() > 1);
		close.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> closePerspective(perspective)));

		new MenuItem(menu, SWT.SEPARATOR);

		MenuItem showTextItem = new MenuItem(menu, SWT.CHECK);
		showTextItem.setText("Show &Text"); //$NON-NLS-1$
		showTextItem.setSelection(showText);
		showTextItem.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> storeShowText(!showText)));

		Rectangle bounds = item.getBounds();
		Point location = toolBar.toDisplay(bounds.x, bounds.y + bounds.height);
		menu.setLocation(location);
		menu.addListener(SWT.Hide, e -> toolBar.getDisplay().asyncExec(menu::dispose));
		menu.setVisible(true);
	}

	private void closePerspective(MPerspective perspective) {
		if (perspective.getParent().getSelectedElement() == perspective) {
			perspective.getParent().getChildren().stream()
					.filter(other -> other != perspective && other.isToBeRendered())
					.filter(MPerspective.class::isInstance)
					.map(MPerspective.class::cast)
					.findFirst()
					.ifPresent(partService::switchPerspective);
		}
		perspective.setToBeRendered(false);
	}

	private void storeShowText(boolean value) {
		preferences.putBoolean(SHOW_TEXT, value);
		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			logger.error(e, "Cannot store the perspective switcher preferences"); //$NON-NLS-1$
		}
	}

	private void openPerspectiveDialog() {
		handlerService.executeHandler(
				commandService.createCommand(E4WorkbenchCommandConstants.PERSPECTIVES_SHOW_PERSPECTIVE, Map.of()));
	}
}
