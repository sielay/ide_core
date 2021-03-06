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
package com.aptana.ide.core.ui.io.file;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

import com.aptana.ide.core.io.IVirtualFileManager;
import com.aptana.ide.core.io.IVirtualFileManagerDialog;
import com.aptana.ide.core.io.ProtocolManager;
import com.aptana.ide.core.io.sync.SyncManager;
import com.aptana.ide.core.ui.CoreUIPlugin;

/**
 * @author Kevin Lindsey
 */
public class LocalProtocolManager extends ProtocolManager
{
	/**
	 * FileSystemRoots
	 */
	public static String FileSystemRoots = "::"; //$NON-NLS-1$
	
	/**
	 * LocalProtocolManager
	 */
	static LocalProtocolManager _localProtocolManager = new LocalProtocolManager();

	/*
	 * Fields
	 */
	private static Image fFolderIcon;

	/**
	 * static ctor
	 */
	static
	{
		ImageDescriptor imageDescriptor = CoreUIPlugin.getImageDescriptor("icons/folder.gif"); //$NON-NLS-1$

		if (imageDescriptor != null)
		{
			fFolderIcon = imageDescriptor.createImage();
		}
	}

	/**
	 * getManagedType
	 * 
	 * @return String
	 */
	public String getManagedType()
	{
		return LocalFileManager.class.getName();
	}
	
	/**
	 * @see com.aptana.ide.core.io.ProtocolManager#getFileManagers()
	 */
	public IVirtualFileManager[] getFileManagers()
	{
		return (IVirtualFileManager[]) SyncManager.getSyncManager().getItems(LocalFileManager.class);
	}
	
	/**
	 * @see com.aptana.ide.core.io.ProtocolManager#createFileManager()
	 */
	public IVirtualFileManager createFileManager()
	{
		return createFileManager(false);
	}
	
	/**
	 * Creates a file manager, but does not add it to the list
	 *
	 * @param temporary
	 * @return IVirtualFileManager
	 */
	public IVirtualFileManager createFileManager(boolean temporary)
	{
		LocalFileManager lfm = new LocalFileManager(this);
		lfm.setHidden(this.isHidden());
		
		if(!temporary)
		{
			this.addFileManager(lfm);
		}
		
		return lfm;
	}
	
	/**
	 * createProjectFileManager
	 *
	 * @return IVirtualFileManager
	 */
	public IVirtualFileManager createProjectFileManager()
	{
		ProjectFileManager lfm = new ProjectFileManager(this);
		lfm.setHidden(true);
		
		this.addFileManager(lfm);
		
		return lfm;
	}
	
	/**
	 * getInstance
	 * @return LocalProtocolManager
	 */
	public static LocalProtocolManager getInstance()
	{
		return _localProtocolManager;
	}

	/**
	 * @see ProtocolManager#createPropertyDialog(Shell, int)
	 */
	public IVirtualFileManagerDialog createPropertyDialog(Shell parent, int style)
	{
		return null;
	}
	
	/**
	 * @see ProtocolManager#getStaticInstance()
	 */
	public ProtocolManager getStaticInstance()
	{
		return getInstance();
	}


	/**
	 * getImage
	 * 
	 * @return Image
	 */
	public Image getImage()
	{
		return fFolderIcon;
	}
	
	/**
	 * @see com.aptana.ide.core.io.ProtocolManager#fromString(java.lang.String)
	 */
//	public IVirtualFileManager fromString(String hash) 
//	{
//		return LocalFileManager.fromStringStatic(hash);
//	}

}
