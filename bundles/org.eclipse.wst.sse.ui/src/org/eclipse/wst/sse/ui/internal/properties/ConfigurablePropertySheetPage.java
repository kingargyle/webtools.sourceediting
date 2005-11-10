/*******************************************************************************
 * Copyright (c) 2001, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jens Lukowski/Innoopract - initial renaming/restructuring
 *     
 *******************************************************************************/
package org.eclipse.wst.sse.ui.internal.properties;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.views.properties.IPropertySheetEntry;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.IPropertySourceProvider;
import org.eclipse.ui.views.properties.PropertySheetPage;
import org.eclipse.wst.sse.ui.views.properties.IPropertySourceExtension;
import org.eclipse.wst.sse.ui.views.properties.PropertySheetConfiguration;


/**
 * A configurable IPropertySheetPage since the standard PropertySheetPage does
 * not expose its viewer field.
 */

public class ConfigurablePropertySheetPage extends PropertySheetPage {
	private class NullPropertySheetConfiguration extends PropertySheetConfiguration {
		public IPropertySourceProvider getPropertySourceProvider(IPropertySheetPage page) {
			return null;
		}
	}

	private static final boolean _DEBUG = "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.wst.sse.ui/propertySheet")); //$NON-NLS-1$  //$NON-NLS-2$;;

	private long _DEBUG_TIME = 0;

	private PropertySheetConfiguration fConfiguration;
	private ISelection fInput = null;
	private IMenuManager fMenuManager;

	private RemoveAction fRemoveAction;

	private IStatusLineManager fStatusLineManager;

	private IToolBarManager fToolBarManager;

	private final PropertySheetConfiguration NULL_CONFIGURATION = new NullPropertySheetConfiguration();

	public ConfigurablePropertySheetPage() {
		super();
	}

	public void createControl(Composite parent) {
		setPropertySourceProvider(getConfiguration().getPropertySourceProvider(this));
		super.createControl(parent);
	}

	public void dispose() {
		setConfiguration(null);
		getSite().getWorkbenchWindow().getSelectionService().removePostSelectionListener(this);
		super.dispose();
	}

	public PropertySheetConfiguration getConfiguration() {
		if (fConfiguration == null)
			fConfiguration = NULL_CONFIGURATION;
		return fConfiguration;
	}

	public void handleEntrySelection(ISelection selection) {
		if (getControl() != null && !getControl().isDisposed() && selection != null) {
			super.handleEntrySelection(selection);
			fRemoveAction.setEnabled(!selection.isEmpty());
		}
	}

	public void init(IPageSite pageSite) {
		super.init(pageSite);
		pageSite.getWorkbenchWindow().getSelectionService().addPostSelectionListener(this);
	}

	public void makeContributions(IMenuManager menuManager, IToolBarManager toolBarManager, IStatusLineManager statusLineManager) {
		super.makeContributions(menuManager, toolBarManager, statusLineManager);
		fMenuManager = menuManager;
		fToolBarManager = toolBarManager;
		fStatusLineManager = statusLineManager;

		fRemoveAction = new RemoveAction(this);
		toolBarManager.add(fRemoveAction);
		menuManager.add(fRemoveAction);
		getConfiguration().addContributions(menuManager, toolBarManager, statusLineManager);

		menuManager.update(true);
	}

	void remove() {
		if (getControl() instanceof Tree) {
			TreeItem[] items = ((Tree) getControl()).getSelection();
			List selectedNodes = new ArrayList(0);
			if (items != null && items.length == 1 && selectedNodes != null) {
				Object data = items[0].getData();
				if (data instanceof IPropertySheetEntry) {
					IPropertySheetEntry entry = (IPropertySheetEntry) data;
					ISelection selection = getConfiguration().getInputSelection(null, new StructuredSelection(selectedNodes));
					if (selection != null && !selection.isEmpty() && selection instanceof IStructuredSelection) {
						IPropertySource source = getConfiguration().getPropertySourceProvider(this).getPropertySource(((IStructuredSelection) selection).getFirstElement());
						if (source != null && source instanceof IPropertySourceExtension) {
							((IPropertySourceExtension) source).removeProperty(entry.getDisplayName());
						}
					}
				}
			}
		}
	}

	/*
	 * Filter the selection through the current Configuration. Not every
	 * selection received is a Structured selection nor are the Structured
	 * selection's elements all to be displayed.
	 * 
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		_DEBUG_TIME = System.currentTimeMillis();
		if (getControl() != null && getControl().isVisible() && !getControl().isFocusControl()) {
			ISelection preferredSelection = getConfiguration().getInputSelection(part, selection);
			/*
			 * Do some minor caching of the selection.
			 * 
			 * As a PropertySheetPage, we've always notified of selection
			 * changes, but we've also subscribed to post selection
			 * notification so we can track caret movements in source viewers.
			 * When selecting a block of text, we're thus notified of the new
			 * selection twice. Remembering what our last *effective*
			 * selection was allows us to cut out most of the
			 * double-notification penalty.
			 */
			if (!preferredSelection.equals(fInput)) {
				fInput = preferredSelection;
				super.selectionChanged(part, preferredSelection);
			}

			if (_DEBUG) {
				System.out.println("(P:service " + (System.currentTimeMillis() - _DEBUG_TIME) + "ms) " + part + " : " + ((IStructuredSelection) preferredSelection).getFirstElement()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}
		else if (_DEBUG) {
			System.out.println("[skipped] (P:" + (System.currentTimeMillis() - _DEBUG_TIME) + "ms) " + part + " : " + selection); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}

	/**
	 * @param configuration
	 *            The configuration to set.
	 */
	public void setConfiguration(PropertySheetConfiguration configuration) {
		if (fConfiguration != null) {
			fConfiguration.removeContributions(fMenuManager, fToolBarManager, fStatusLineManager);
			fConfiguration.unconfigure();
		}

		fConfiguration = configuration;

		if (fConfiguration != null) {
			setPropertySourceProvider(fConfiguration.getPropertySourceProvider(this));
			fConfiguration.addContributions(fMenuManager, fToolBarManager, fStatusLineManager);
		}

	}
}