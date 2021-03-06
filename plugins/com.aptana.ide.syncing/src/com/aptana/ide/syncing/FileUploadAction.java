/**
 * This file Copyright (c) 2005-2008 Aptana, Inc. This program is
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
package com.aptana.ide.syncing;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

import com.aptana.ide.core.StringUtils;
import com.aptana.ide.core.io.ConnectionException;
import com.aptana.ide.core.io.IVirtualFile;
import com.aptana.ide.core.io.IVirtualFileManager;
import com.aptana.ide.core.io.VirtualFile;
import com.aptana.ide.core.io.sync.VirtualFileManagerSyncPair;
import com.aptana.ide.core.io.sync.VirtualFileSyncPair;
import com.aptana.ide.core.ui.CoreUIUtils;
import com.aptana.ide.core.ui.DialogUtils;
import com.aptana.ide.core.ui.io.file.LocalFileManager;
import com.aptana.ide.core.ui.views.fileexplorer.FileExplorerView;
import com.aptana.ide.syncing.preferences.IPreferenceConstants;

/**
 * Uploads an item
 * 
 * @author Ingo Muschenetz
 */
public class FileUploadAction extends BaseSyncAction
{
	/**
	 * UploadAction
	 */
	public FileUploadAction()
	{
		gettingMessage = StringUtils.ellipsify(Messages.FileUploadAction_UploadingItems);
		syncingMessage = StringUtils.ellipsify(Messages.FileUploadAction_Uploading);
		confirmMessage = Messages.FileUploadAction_UplaodSelectedItemsToSite;
	}

	public static IVirtualFile[] getUploadFiles(VirtualFileManagerSyncPair conf, IVirtualFile[] files)
			throws ConnectionException, IOException
	{
		IVirtualFileManager sourceManager = conf.getSourceFileManager();
		Set<IVirtualFile> newFiles = new HashSet<IVirtualFile>();

		// show be done via some sort of "import"
		IVirtualFile[] reparentedFiles = VirtualFile.reparentFiles(sourceManager, files);
		IVirtualFile file;
		IVirtualFile[] parents;
		IVirtualFile file2;
		for (int i = 0; i < reparentedFiles.length; i++)
		{
			file = reparentedFiles[i];
			parents = VirtualFile.getParentDirectories(file, sourceManager);

			for (int j = 0; j < parents.length; j++)
			{
				file2 = parents[j];

				if (!newFiles.contains(file2))
				{
					newFiles.add(file2);
				}
			}

			if (file.isDirectory())
			{
				IVirtualFile newFile = sourceManager.createVirtualDirectory(file.getAbsolutePath());

				if (!newFiles.contains(newFile))
				{
					newFiles.add(newFile);
				}

				newFiles.addAll(Arrays.asList(sourceManager.getFiles(newFile, true, false)));
			}
			else
			{
				IVirtualFile newFile = sourceManager.createVirtualFile(file.getAbsolutePath());

				if (!newFiles.contains(newFile))
				{
					newFiles.add(newFile);
				}
			}
		}

		return newFiles.toArray(new IVirtualFile[newFiles.size()]);
	}

	/**
	 * @see com.aptana.ide.syncing.BaseSyncAction#getItems(com.aptana.ide.syncing.Synchronizer,
	 *      com.aptana.ide.core.io.sync.VirtualFileManagerSyncPair, com.aptana.ide.core.io.IVirtualFile[])
	 */
	protected VirtualFileSyncPair[] getItems(Synchronizer sm, VirtualFileManagerSyncPair conf, IVirtualFile[] files)
			throws ConnectionException, IOException
	{
		IVirtualFile[] newFiles = getUploadFiles(conf, files);
		// set upload flag so we get a proper VFM refresh
		conf.setSyncState(VirtualFileManagerSyncPair.Upload);

		sm.setClientFileManager(conf.getSourceFileManager());
		sm.setServerFileManager(conf.getDestinationFileManager());

		return sm.createSyncItems(newFiles, new IVirtualFile[0]);
	}

	/**
	 * @see com.aptana.ide.syncing.BaseSyncAction#syncItems(com.aptana.ide.syncing.Synchronizer,
	 *      com.aptana.ide.core.io.sync.VirtualFileSyncPair[])
	 */
	protected void syncItems(Synchronizer sm, VirtualFileSyncPair[] items) throws ConnectionException, IOException
	{
		sm.upload(items);
	}

	/**
	 * @see BaseSyncAction#displayDone(Synchronizer)
	 */
	protected void displayDone(final Synchronizer sm)
	{
		UIJob job = new UIJob(Messages.FileUploadAction_UIJOB_ShowingUploadFinishedDialog)
		{

			public IStatus runInUIThread(IProgressMonitor monitor)
			{
				IPreferenceStore store = SyncingPlugin.getDefault().getPreferenceStore();
				DialogUtils
						.openIgnoreMessageDialogInformation(Display.getCurrent().getActiveShell(), syncingMessage,
								StringUtils.format(Messages.FileUploadAction_ItemsUploadedConfirmation,
										new Object[] {
											sm.getClientFileTransferedCount(),
											sm.getServerDirectoryCreatedCount()
										}), store,
								IPreferenceConstants.IGNORE_DIALOG_FILE_UPLOAD);
				SyncingPlugin.getDefault().savePluginPreferences();
				return Status.OK_STATUS;
			}

		};
		job.schedule();
	}

	/**
	 * @see com.aptana.ide.syncing.BaseSyncAction#refresh(com.aptana.ide.syncing.Synchronizer)
	 */
	protected void refresh(final Synchronizer sm)
	{
		// finds the parent folders of uploaded files to refresh
		final Set<IVirtualFile> parentFolders = new HashSet<IVirtualFile>();
		IVirtualFile[] newFiles = sm.getNewFilesUploaded();
		IVirtualFile parent;
		for (IVirtualFile file : newFiles)
		{
			parent = file.getParentFile();
			if (parent != null)
			{
				parentFolders.add(parent);
			}
		}
		final IVirtualFileManager fileManager = sm.getServerFileManager();
		final String managerBasePath = (fileManager == null) ? null
				: fileManager.getBaseFile().getAbsolutePath();
		// get display
		IWorkbench workbench = PlatformUI.getWorkbench();
		Display display = workbench.getDisplay();

		// execute callback in the correct thread
		display.asyncExec(new Runnable()
		{

			public void run()
			{
				IWorkbenchPart[] part = CoreUIUtils.getViewsInternal(FileExplorerView.ID);
				FileExplorerView fileExplorer;
				for (int i = 0; i < part.length; i++)
				{
					fileExplorer = (FileExplorerView) part[i];

					for (IVirtualFile folder : parentFolders)
					{
						if (folder.getAbsolutePath().equals(managerBasePath))
						{
							// the parent is the file manager
							refresh(fileExplorer, fileManager);
						}
						else
						{
							refresh(fileExplorer, folder);
						}
					}
				}
			}

			private void refresh(FileExplorerView fileExplorer, IVirtualFileManager fileManager)
			{
				if (fileExplorer == null || fileManager == null)
				{
					return;
				}
				if (fileManager instanceof LocalFileManager)
				{
					fileExplorer.refresh(fileManager.getBaseFile());
				}
				else
				{
					fileExplorer.refresh(fileManager);
				}
			}

			private void refresh(FileExplorerView fileExplorer, IVirtualFile folder)
			{
				if (fileExplorer == null || folder == null)
				{
					return;
				}
				fileExplorer.refresh(folder);
			}
		});
	}
}
