package net.jadoth.com.binary;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import net.jadoth.com.ComChannel;
import net.jadoth.com.ComDefaultIdStrategy;
import net.jadoth.files.XFiles;
import net.jadoth.meta.XDebug;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistenceFoundation;
import net.jadoth.persistence.internal.PersistenceTypeDictionaryFileHandler;
import net.jadoth.persistence.types.BufferSizeProvider;
import net.jadoth.persistence.types.Persistence;
import net.jadoth.persistence.types.PersistenceManager;
import net.jadoth.swizzling.internal.CompositeSwizzleIdProvider;

public class UtilTestCom
{
	public static int defaultPort()
	{
		return 1337;
	}
	
	public static File defaultSystemDirectory()
	{
		return new File("networkpersistencedemo");
	}
	
	public static ServerSocketChannel openServerSocketChannel() throws IOException
	{
		return openServerSocketChannel(defaultPort());
	}
	
	public static ServerSocketChannel openServerSocketChannel(final int port) throws IOException
	{
		final ServerSocketChannel serverChannel = ServerSocketChannel.open();
		serverChannel.socket().bind(new InetSocketAddress(port));
		return serverChannel;
	}
	
	public static SocketChannel openChannelLocalhost() throws IOException
	{
		return openChannel(InetAddress.getLocalHost());
	}
	
	public static SocketChannel openChannel(final InetAddress address) throws IOException
	{
		return openChannel(address, defaultPort());
	}
	
	public static SocketChannel openChannel(final InetAddress address, final int port) throws IOException
	{
		final SocketChannel socketChannel = SocketChannel.open();
		socketChannel.connect(new InetSocketAddress(address, port));
		return socketChannel;
	}
	
	public static SocketChannel accept(final ServerSocketChannel serverSocketChannel)
	{
		final SocketChannel socketChannel;
		try
		{
			socketChannel = serverSocketChannel.accept();
		}
		catch(final Exception e)
		{
			throw new RuntimeException(e);
		}
		
		return socketChannel;
	}
	
	public static void close(final SocketChannel socketChannel)
	{
		try
		{
			socketChannel.close();
		}
		catch(final Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public static ComChannel openComChannel(
		final SocketChannel socketChannel  ,
		final File          systemDirectory,
		final boolean       isClient
	)
	{
		final BinaryPersistenceFoundation<?> foundation = createFoundation(systemDirectory, isClient);
		
		final ComPersistenceChannelBinary channel = ComPersistenceChannelBinary.New(
			socketChannel,
			BufferSizeProvider.New()
		);
		foundation.setPersistenceChannel(channel);
		
		final PersistenceManager<Binary> pm = foundation.createPersistenceManager();
		
		return ComChannel.New(pm);
	}
		
	private static BinaryPersistenceFoundation<?> createFoundation(
		final File    systemDirectory,
		final boolean isClient
	)
	{
		XFiles.ensureDirectory(systemDirectory);
		
		// (13.08.2018 TM)NOTE: copied from EmbeddedStorage#createConnectionFoundation
		
		final PersistenceTypeDictionaryFileHandler dictionaryStorage = PersistenceTypeDictionaryFileHandler.NewInDirecoty(
			systemDirectory
		);

		// (17.08.2018 TM)NOTE: use once to create a TypeDictionary, then switch back to Failing implementation
//		final FileTypeIdProvider fileTypeIdProvider = new FileTypeIdProvider(
//			new File(systemDirectory, Persistence.defaultFilenameTypeId())
//		);

		final ComDefaultIdStrategy idStrategy = ComDefaultIdStrategy.New(
			isClient
			? 9_200_000_000_000_000_000L // temporary id range.
			: 9_100_000_000_000_000_000L // temporary id range
		);
		XDebug.println("Starting OID: " + idStrategy.startingObjectId());
		
		final CompositeSwizzleIdProvider idProvider = idStrategy
			.createIdProvider()
			.initialize()
		;
		
		return BinaryPersistenceFoundation.New()
			.setTypeDictionaryIoHandling   (dictionaryStorage            )
			.setSwizzleIdProvider          (idProvider                   )
			.setTypeEvaluatorPersistable   (Persistence::isPersistable   )
			.setTypeEvaluatorTypeIdMappable(Persistence::isTypeIdMappable)
		;
	}
	
	
}