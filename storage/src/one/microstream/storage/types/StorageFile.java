package one.microstream.storage.types;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;

import one.microstream.chars.VarString;
import one.microstream.storage.exceptions.StorageExceptionIo;


/**
 * Type that symbolizes any entity that allows physically persisting and reading a randomly accessible sequence
 * of bytes. This might typically be a file system file entity, but not necessarily.
 * 
 * @author Thomas Münz
 */
public interface StorageFile
{
	/* (13.10.2018 TM)TODO: much better improved file abstraction.
	 * There has to be better file abstractions. Like:
	 * - StorageIoItem defining a unique storage item of I/O handling (directory or file)
	 * - StorageDirectory extends StorageIoItem defining a "space" where StorageFiles can exist
	 * - StorageFile extends StorageIoItem with a parent StorageDirectory that contains a continous block of bytes.
	 * 
	 * A StorageDirectory can be:
	 * - A file system directory
	 * - A qualifing part of a RDBMS table primary key identifying a BLOB (just a crazy example)
	 * - A qualifing part for some mysterious cloud binary storage whathaveyou.
	 * 
	 * A StorageFile can be:
	 * - A file system file
	 * - A row in an RDBMS table containing a BLOB.
	 * - A full qualified mysterious cloud binary storage whathaveyou item
	 * 
	 * Directories can be used not just for channel directories, but also for import/export locations, etc.
	 * 
	 * Including
	 * - StorageInputChannel
	 * - StorageOutputChannel
	 * - StorageIoChannel extends StorageInputChannel, StorageOutputChannel
	 * plus implementations wrapping specific means (e.g. a FileChannel)
	 * because the idiotic nio interfaces and exceptions make me sick.
	 * (seriously: how hard can it be for the "elite" java developers themselves to properly harness the
	 * language's basic typing concept?)
	 * 
	 * Not yet clear:
	 * Should StorageDirectory-s be recursive like file system directories?
	 * Why should they have to be?
	 */
	
	
	/**
	 * Returns a string that gives {@link #name()} a unique identity.
	 * Example: The parent directory path of a {@link File}.
	 * 
	 * @return
	 */
	public String qualifier();
	
	/**
	 * Returns a string uniquely identifying the file represented by this instance.
	 * 
	 * @return this file's unique identifier.
	 * @see #name()
	 */
	public String identifier();
	
	/**
	 * Return a compact string containing a specific, but not necessarily unique
	 * name of the file represented by this instance. Might be the same string
	 * returned by {@link #identifier()}.
	 * 
	 * @return this file's name.
	 * @see #identifier()
	 */
	public String name();
	
	public long length();
	
	public default boolean isEmpty()
	{
		return this.length() == 0;
	}
	
	public boolean delete();
	
	public boolean exists();
	
	public FileChannel fileChannel();

	public default boolean isOpen()
	{
		return this.fileChannel().isOpen();
	}
	
	public default StorageFile flush()
	{
		try
		{
			this.fileChannel().force(false);
			return this;
		}
		catch(final IOException e)
		{
			throw new StorageExceptionIo(e); // damned checked exception
		}
	}

	public default void close()
	{
		try
		{
			this.fileChannel().close();
		}
		catch(final IOException e)
		{
			throw new StorageExceptionIo(e); // damned checked exception
		}
	}
	
	
	public static void closeSilent(final StorageLockedFile file)
	{
		if(file == null)
		{
			return;
		}
		
		try
		{
			file.close();
		}
		catch(final Exception t)
		{
			// sshhh, silence!
		}
	}
	
	
	public static VarString assembleNameAndSize(final VarString vs, final StorageFile file)
	{
		return vs.add(file.identifier() + "[" + file.length() + "]");
	}
	
}