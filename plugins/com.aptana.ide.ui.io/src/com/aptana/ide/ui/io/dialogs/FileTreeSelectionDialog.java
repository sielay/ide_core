/**
 * This file Copyright (c) 2005-2009 Aptana, Inc. This program is
 * dual-licensed under both the Aptana Public License and the GNU General
 * Public license. You may elect to use one or the other of these licenses.
 * 
 * This program is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT. Redistribution, except as permitted by whichever of
 * the GPL or APL you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or modify this
 * program under the terms of the GNU General Public License,
 * Version 3, as published by the Free Software Foundation.  You should
 * have received a copy of the GNU General Public License, Version 3 along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Aptana provides a special exception to allow redistribution of this file
 * with certain other free and open source software ("FOSS") code and certain additional terms
 * pursuant to Section 7 of the GPL. You may view the exception and these
 * terms on the web at http://www.aptana.com/legal/gpl/.
 * 
 * 2. For the Aptana Public License (APL), this program and the
 * accompanying materials are made available under the terms of the APL
 * v1.0 which accompanies this distribution, and is available at
 * http://www.aptana.com/legal/apl/.
 * 
 * You may view the GPL, Aptana's exception and additional terms, and the
 * APL in the file titled license.html at the root of the corresponding
 * plugin containing this source file.
 * 
 * Any modifications to this file must keep this entire header intact.
 */

package com.aptana.ide.ui.io.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.IWorkbenchAdapter;

import com.aptana.ide.core.IdeLog;
import com.aptana.ide.core.StringUtils;
import com.aptana.ide.core.io.IConnectionPoint;
import com.aptana.ide.ui.io.FileSystemUtils;
import com.aptana.ide.ui.io.IOUIPlugin;
import com.aptana.ide.ui.io.navigator.DeferredTreeContentManager;
import com.aptana.ide.ui.io.navigator.DeferredTreeSelectionExpander;
import com.aptana.ide.ui.io.navigator.FileSystemElementComparer;
import com.aptana.ide.ui.io.navigator.FileTreeDeferredContentProvider;
import com.aptana.ide.ui.io.navigator.FileTreeNameSorter;

/**
 * @author Max Stepanov
 *
 */
public class FileTreeSelectionDialog extends ElementTreeSelectionDialog {

	private IFileStore selection;
	
	/**
	 * Label provider
	 */
	private static class FileLabelProvider extends LabelProvider {
		private static final Image IMG_FOLDER = PlatformUI.getWorkbench()
								.getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);

		private static final Image IMG_FILE = PlatformUI.getWorkbench()
								.getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
		
		private ResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
		 */
		public Image getImage(Object element) {
			IFileInfo fileInfo = FileSystemUtils.getFileInfo(element);
			if (fileInfo != null) {
				return fileInfo.isDirectory() ? IMG_FOLDER : IMG_FILE;
			}
			IFileStore fileStore = FileSystemUtils.getFileStore(element);
			if (fileStore != null && Path.ROOT.toPortableString().equals(fileStore.getName())) {
				return IMG_FOLDER;
			}
			if (element instanceof IAdaptable) {
				IWorkbenchAdapter workbenchAdapter = (IWorkbenchAdapter) ((IAdaptable) element).getAdapter(IWorkbenchAdapter.class);
				if (workbenchAdapter != null) {
					ImageDescriptor imageDescriptor = workbenchAdapter.getImageDescriptor(element);
					if (imageDescriptor != null) {
						return (Image) resourceManager.get(imageDescriptor);
					}
				}
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
		 */
		public String getText(Object element) {
			IFileStore fileStore = FileSystemUtils.getFileStore(element);
			if (fileStore != null) {
				return fileStore.getName();
			}
			return super.getText(element);
		}
	}

	/**
	 * Content provider
	 */
	private static class FileContentProvider implements ITreeContentProvider {
		protected static final String SELECTION_EXPANDER_KEY = "selection_expander";
		private boolean allowFiles;
		
		/**
		 * @param allowFiles
		 */
		public FileContentProvider(boolean allowFiles) {
			super();
			this.allowFiles = allowFiles;
		}

		public Object[] getChildren(Object parentElement) {
			return new Object[0];
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			return false;
		}

		public Object[] getElements(Object inputElement) {
			return new Object[1];
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			AbstractTreeViewer treeViewer = (AbstractTreeViewer) viewer;
			if (treeViewer.getContentProvider() != this) {
				return;
			}
			DeferredTreeContentManager deferredTreeContentManager = new DeferredTreeContentManager(treeViewer) {
				/* (non-Javadoc)
				 * @see org.eclipse.ui.progress.DeferredTreeContentManager#addChildren(java.lang.Object, java.lang.Object[], org.eclipse.core.runtime.IProgressMonitor)
				 */
				@Override
				protected void addChildren(Object parent, Object[] children, IProgressMonitor monitor) {
					if (!allowFiles) {
						List<Object> filtered = new ArrayList<Object>();
						for (Object i : children) {
							if (i instanceof IAdaptable) {
								IFileInfo fileInfo = (IFileInfo) ((IAdaptable) i).getAdapter(IFileInfo.class);
								if (fileInfo != null) {
									if (fileInfo.isDirectory()) {
										filtered.add(i);
									}
									continue;
								}
							}
							filtered.add(i);
						}
						children = filtered.toArray();
					}
					super.addChildren(parent, children, monitor);
				}
			};
			treeViewer.setContentProvider(new FileTreeDeferredContentProvider(deferredTreeContentManager) {
				@Override
				public Object[] getElements(Object element) {
					if (element instanceof IConnectionPoint) {
						try {
							return new Object[] { ((IConnectionPoint) element).getRoot() };
						} catch (CoreException e) {
							IdeLog.logImportant(IOUIPlugin.getDefault(), StringUtils.EMPTY, e);
						}
					}
					return super.getElements(element);
				}
				
			});
			treeViewer.setComparer(new FileSystemElementComparer());
			DeferredTreeSelectionExpander selectionExpander = new DeferredTreeSelectionExpander(deferredTreeContentManager, treeViewer);
			treeViewer.setData(SELECTION_EXPANDER_KEY, selectionExpander);
		}
	}
		
	/**
	 * @param parent
	 * @param allowFiles
	 */
	public FileTreeSelectionDialog(Shell parent, boolean allowFiles) {
		super(parent,
			new FileLabelProvider(),
			new FileContentProvider(allowFiles));
		setTitle("Browse");
		setComparator(new FileTreeNameSorter());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.ElementTreeSelectionDialog#setInput(java.lang.Object)
	 */
	@Override
	public void setInput(Object input) {
		Assert.isLegal(input instanceof IConnectionPoint || input instanceof IFileStore);
		super.setInput(input);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.ElementTreeSelectionDialog#setInitialSelection(java.lang.Object)
	 */
	@Override
	public void setInitialSelection(Object selection) {
		if (selection instanceof IFileStore) {
			this.selection = (IFileStore) selection;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.ElementTreeSelectionDialog#create()
	 */
	@Override
	public void create() {
		super.create();
		if (selection == null) {
			return;
		}
        BusyIndicator.showWhile(null, new Runnable() {
            public void run() {
            	TreeViewer treeViewer = getTreeViewer();
            	Object input = treeViewer.getInput();
            	try {
            		IFileStore fileStore = null;
					if (input instanceof IConnectionPoint) {
						fileStore = ((IConnectionPoint) input).getRoot();
						if (!fileStore.isParentOf(selection)) {
							return;
						}
					} else if (input instanceof IFileStore) {
						fileStore = (IFileStore) input;
						if (fileStore.equals(selection) || !fileStore.isParentOf(selection)) {
							return;
						}
					} else {
						return;
					}
					
					List<IFileStore> list = new ArrayList<IFileStore>();
					IFileStore i = selection;
					while (i != null) {
						list.add(0, i);
						if (i.equals(fileStore)) {
							break;
						}
						i = i.getParent();
					}
					TreePath treePath = new TreePath(list.toArray());
					DeferredTreeSelectionExpander selectionExpander = (DeferredTreeSelectionExpander) treeViewer.getData(FileContentProvider.SELECTION_EXPANDER_KEY);
					if (selectionExpander != null) {
						selectionExpander.setSelection(treePath);
					}
				} catch (CoreException e) {
					IdeLog.logImportant(IOUIPlugin.getDefault(), StringUtils.EMPTY, e);
				}
            }
        });
	}

}
