package com.aptana.ide.filesystem.s3;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileTree;
import org.eclipse.core.filesystem.provider.FileSystem;
import org.eclipse.core.runtime.IProgressMonitor;

public class S3FileSystem extends FileSystem
{

	@Override
	public IFileStore getStore(URI uri)
	{
		return new S3FileStore(uri);
	}

	@Override
	public boolean canDelete()
	{
		return true;
	}

	@Override
	public boolean canWrite()
	{
		return true;
	}

	@Override
	public IFileTree fetchFileTree(IFileStore root, IProgressMonitor monitor)
	{
		if (!(root instanceof S3FileStore))
			return null;
		try
		{
			S3FileStore s3Store = (S3FileStore) root;
			if (monitor != null && monitor.isCanceled())
				return null;
			return new S3FileTree(root, s3Store.listEntries());
		}
		catch (MalformedURLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
