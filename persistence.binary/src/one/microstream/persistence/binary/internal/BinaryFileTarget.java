package one.microstream.persistence.binary.internal;

import static one.microstream.X.notNull;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;

import one.microstream.files.FileException;
import one.microstream.files.XFiles;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.exceptions.PersistenceExceptionTransfer;
import one.microstream.persistence.types.PersistenceTarget;

public class BinaryFileTarget implements PersistenceTarget<Binary>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final File file;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryFileTarget(final File file)
	{
		super();
		this.file = notNull(file);
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	protected FileChannel createChannel(final File file) throws FileException, IOException
	{
		return XFiles.createWritingFileChannel(file);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public void write(final Binary chunk) throws PersistenceExceptionTransfer
	{
		try(final FileChannel fch = this.createChannel(this.file))
		{
			fch.write(chunk.buffers());
		}
		catch(final IOException e)
		{
			throw new PersistenceExceptionTransfer(e);
		}
	}

}
