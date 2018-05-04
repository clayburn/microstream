package net.jadoth.meta;

import static net.jadoth.time.XTime.now;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.jadoth.X;
import net.jadoth.chars.VarString;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.concurrency.XThreads;
import net.jadoth.files.XFiles;
import net.jadoth.typing.KeyValue;


/**
 * This is a helper class merely for debugging purposes. None of its members should be used for productive code.
 * 
 * @author Thomas Muenz
 */
public final class XDebug
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	private static final transient int    LINE_BUFFER_INITIAL_SIZE = 256       ;
	private static final transient int    SOURCE_POSITION_PADDING  = 64        ;
	private static final transient char[] TIME_SEPERATOR           = {'>', ' '};



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	static String formatTimestamp(final Date timestamp)
	{
		/*
		 * JDK people are not capable of programming thread safe utility classes, so a new instance
		 * must be created on every call.
		 */
		return new SimpleDateFormat("HH:mm:ss.SSS").format(timestamp);
	}
	
	static String formatTimestamp(final long timestamp)
	{
		/*
		 * JDK people are not capable of programming thread safe utility classes, so a new instance
		 * must be created on every call.
		 */
		return new DecimalFormat("00,000,000,000").format(timestamp);
	}

	public static final void debugln(final String s)
	{
		debugln(s, 1);
	}

	public static final void debugln(final String s, final int stackTraceCut)
	{
		// index 1 is always safely this method call itself, index 2 is always safely the calling context
		final StackTraceElement e = XThreads.getStackTraceElement(2 + stackTraceCut);

		System.out.println(
			VarString.New(LINE_BUFFER_INITIAL_SIZE)
			.padRight(toMethodLink(e), SOURCE_POSITION_PADDING, ' ')
			.add(formatTimestamp(now()))
			.add(TIME_SEPERATOR)
			.add(s))
		;
	}
	
	private static String toMethodLink(final StackTraceElement e)
	{
		// every StackTraceElement string is guaranteed to be in the pattern [class].[method]([class].java:[line])
		final String s = e.toString();
		return s.substring(s.lastIndexOf('.', s.lastIndexOf('.') - 1));
	}

	public static final void printlnElapsedNanos(final long elapsedTime)
	{
		/*
		 * JDK people are not capable of programming thread safe utility classes, so a new instance
		 * must be created on every call.
		 */
		System.out.println("Elapsed Time: " + formatTimestamp(elapsedTime));
	}

	public static final void printCollection(
		final XGettingCollection<?> collection,
		final String                start     ,
		final String                separator ,
		final String                end       ,
		final Integer               limit
	)
	{
		final char[] sepp = separator != null ? separator.toCharArray() : null;

		final VarString vc = VarString.New();
		if(start != null)
		{
			vc.add(start);
		}
		
		final int vcOldLength = vc.length();
		if(limit == null)
		{
			collection.iterate(e ->
			{
				vc.add(e);
				if(sepp != null)
				{
					vc.add(sepp);
				}
			});
		}
		else
		{
			collection.iterate(new Consumer<Object>()
			{
				private int lim = limit;
				@Override
				public void accept(final Object e)
				{
					if(--this.lim <= 0)
					{
						throw X.BREAK();
					}
					vc.add(e);
					if(sepp != null)
					{
						vc.add(sepp);
					}
				}
			});
		}
		if(sepp != null && vc.length() > vcOldLength)
		{
			vc.deleteLast(sepp.length);
		}

		if(end != null)
		{
			vc.add(end);
		}

		System.out.println(vc.toString());
		System.out.flush();
	}

	public static final VarString assembleTable(
		final VarString           vs        ,
		final XGettingTable<?, ?> collection,
		final String              start     ,
		final String              mapper    ,
		final String              separator ,
		final String              end       ,
		final Integer             limit
	)
	{
		final char[] sepp = separator != null ? separator.toCharArray() : null;
		if(start != null)
		{
			vs.add(start);
		}
		
		final int vcOldLength = vs.length();
		if(limit == null)
		{
			collection.iterate(kv ->
			{
				vs.add(kv.key());
				if(mapper != null)
				{
					vs.add(mapper);
				}
				vs.add(kv.value());
				if(sepp != null)
				{
					vs.add(sepp);
				}
			});
		}
		else
		{
			collection.iterate(new Consumer<KeyValue<?, ?>>()
			{
				private int lim = limit;
				@Override
				public void accept(final KeyValue<?, ?> e)
				{
					if(--this.lim <= 0)
					{
						throw X.BREAK();
					}
					vs.add(e.key());
					if(mapper != null)
					{
						vs.add(mapper);
					}
					vs.add(e.value());
					if(sepp != null)
					{
						vs.add(sepp);
					}
				}
			});
		}
		if(sepp != null && vs.length() > vcOldLength)
		{
			vs.deleteLast(sepp.length);
		}
		if(end != null)
		{
			vs.add(end);
		}
		return vs;
	}

	public static final void printTable(
		final XGettingTable<?, ?> collection,
		final String              start     ,
		final String              mapper    ,
		final String              separator ,
		final String              end       ,
		final Integer             limit
	)
	{
		System.out.println(assembleTable(VarString.New(), collection, start, mapper, separator, end, limit));
	}

	public static final void printArray(
		final Object[] array    ,
		final String   start    ,
		final String   separator,
		final String   end      ,
		final Integer  limit
	)
	{
		final char[] sepp = separator != null ? separator.toCharArray() : null;

		final VarString vc = VarString.New();
		if(start != null)
		{
			vc.add(start);
		}
		final int size = limit == null ? array.length : Math.min(array.length, limit);
		for(int i = 0; i < size; i++)
		{
			vc.add(array[i]);
			if(sepp != null)
			{
				vc.add(sepp);
			}
		}
		if(size > 1 && sepp != null)
		{
			vc.deleteLast(sepp.length);
		}
		if(end != null)
		{
			vc.add(end);
		}

		System.out.println(vc.toString());
		System.out.flush();
	}
	
	public static <T> T printTime(final Supplier<? extends T> logic)
	{
		return internalPrintTime(logic, null, 1, 0, 0);
	}
	
	public static <T> T printTime(final Supplier<? extends T> logic, final String name)
	{
		return internalPrintTime(logic, name, 1, 0, 0);
	}
	
	public static <T> T printTime(
		final Supplier<? extends T> logic          ,
		final int                   stackTraceDepth
	)
	{
		return internalPrintTime(logic, null,  1, 2, stackTraceDepth);
	}
	
	public static <T> T printTime(
		final Supplier<? extends T> logic               ,
		final int                   stackTraceDepthStart,
		final int                   stackTraceDepth
	)
	{
		return internalPrintTime(logic, null, 1, stackTraceDepthStart + 1, stackTraceDepth);
	}
	
	public static <T> T printTime(
		final Supplier<? extends T> logic          ,
		final String                name           ,
		final int                   stackTraceDepth
	)
	{
		return internalPrintTime(logic, name,  1, 2, stackTraceDepth);
	}
	
	public static <T> T printTime(
		final Supplier<? extends T> logic               ,
		final String                name                ,
		final int                   stackTraceDepthStart,
		final int                   stackTraceDepth
	)
	{
		return internalPrintTime(logic, name,  1, stackTraceDepthStart + 1, stackTraceDepth);
	}
	
	public static <T> T internalPrintTime(
		final Supplier<? extends T> logic               ,
		final String                name                ,
		final int                   stackTraceCallLevel ,
		final int                   stackTraceDepthStart,
		final int                   stackTraceDepth
	)
	{
		final long tStart = System.nanoTime();
		final T result = logic.get();
		final long tStop = System.nanoTime();
		
		simplePrint(name, stackTraceCallLevel + 1, stackTraceDepthStart + 1, stackTraceDepth, tStart, tStop);
		
		return result;
	}
	
	public static void printTime(final Runnable logic)
	{
		internalPrintTime(logic, null, 1, 0, 0);
	}
	
	public static void printTime(final Runnable logic, final String name)
	{
		internalPrintTime(logic, name, 1, 0, 0);
	}
	
	public static void printTime(final Runnable logic, final int stackTraceDepth)
	{
		internalPrintTime(logic, null,  1, 2, stackTraceDepth);
	}
	
	public static void printTime(final Runnable logic, final int stackTraceDepthStart, final int stackTraceDepth)
	{
		internalPrintTime(logic, null, 1, stackTraceDepthStart + 1, stackTraceDepth);
	}
	
	public static void printTime(
		final Runnable logic          ,
		final String   name           ,
		final int      stackTraceDepth
	)
	{
		internalPrintTime(logic, name, 1, 2, stackTraceDepth);
	}
	
	public static void printTime(
		final Runnable logic               ,
		final String   name                ,
		final int      stackTraceDepthStart,
		final int      stackTraceDepth
	)
	{
		internalPrintTime(logic, name, 1, stackTraceDepthStart + 1, stackTraceDepth);
	}
	
	private static void internalPrintTime(
		final Runnable logic               ,
		final String   name                ,
		final int      stackTraceCallLevel ,
		final int      stackTraceDepthStart,
		final int      stackTraceDepth
	)
	{
		final long tStart = System.nanoTime();
		logic.run();
		final long tStop = System.nanoTime();
		
		simplePrint(name, stackTraceCallLevel + 1, stackTraceDepthStart + 1, stackTraceDepth, tStart, tStop);
	}
	
	private static void simplePrint(
		final String name                ,
		final int    stackTraceCallLevel ,
		final int    stackTraceDepthStart,
		final int    stackTraceDepth     ,
		final long   tStart              ,
		final long   tStop
	)
	{
		final StackTraceElement[] stacktrace       = new Throwable().getStackTrace();
		final StackTraceElement   callLevelElement = stacktrace[stackTraceCallLevel + 1];
		
		final VarString vs = VarString.New(toMethodLink(callLevelElement)).blank();
		vs.add(new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart)).add(" nanoseconds");
		if(name != null)
		{
			vs.add(" for ").add(name);
		}
		
		// empty stack trace output intentionally not suppressed to indicate faulty stack trace bounds.
		if(stackTraceDepth > 0/* && stackTraceDepthStart + 1 < stacktrace.length*/)
		{
			vs.lf().add("Stacktrace: ");
			final int stackTraceLimit = Math.min(stackTraceDepthStart + stackTraceDepth + 1, stacktrace.length);
			for(int i = stackTraceDepthStart + 1; i < stackTraceLimit; i++)
			{
				vs.lf().add(toMethodLink(stacktrace[i]));
			}
			vs.lf().add("/ Stacktrace");
		}
		
		System.out.println(vs);
	}
	
	public static void resetDirecory(final File target, final File source, final boolean output) throws IOException
	{
		deleteAllFiles(target, output);
		copyFile(source, source, target);
	}

	public static final void deleteAllFiles(final File directory, final boolean output)
	{
		if(!directory.exists())
		{
			return;
		}
		for(final File f : directory.listFiles())
		{
			if(f.isDirectory())
			{
				deleteAllFiles(f, output);
			}
			try
			{
				if(output)
				{
					debugln("Deleting "+f);
				}
				Files.deleteIfExists(f.toPath());
			}
			catch(final Exception e)
			{
				throw new RuntimeException("Cannot delete file: "+f, e);
			}
		}

	}

	public static void copyFile(final File sourceRoot, final File subject, final File targetRoot) throws IOException
	{
		if(subject.isDirectory())
		{
			copyDirectory(sourceRoot, subject, targetRoot);
		}
		else
		{
			copyActualFile(sourceRoot, subject, targetRoot);
		}
	}

	public static void copyDirectory(final File sourceRoot, final File subject, final File targetRoot) throws IOException
	{
		for(final File file : subject.listFiles())
		{
			copyFile(sourceRoot, file, targetRoot);
		}
	}

	public static void copyActualFile(final File sourceRoot, final File subject, final File targetRoot) throws IOException
	{
		final String sourceRootPath = sourceRoot.getAbsolutePath();
		final String subjectPath    = subject.getAbsolutePath();
		final File   targetFile     = new File(targetRoot, subjectPath.substring(sourceRootPath.length()));

		XFiles.ensureDirectoryAndFile(targetFile);

		final Path sourcePath      = subject.toPath();
		final Path destinationPath = targetFile.toPath();

		Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
	}


	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	private XDebug()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}
