package one.microstream.test.collections;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

import one.microstream.reference.Reference;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.test.corp.logic.Test;
import one.microstream.test.corp.logic.TestImportExport;

public class MainTestStorageJDKCollections
{
	static final Reference<Object[]>    ROOT    = Reference.New(null)        ;
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage.start(ROOT);

	public static void main(final String[] args)
	{
		if(ROOT.get() == null)
		{
			System.out.println("Storing collections ...");
			ROOT.set(createRoot());
			STORAGE.store(ROOT);
			System.out.println("Stored collections.");
		}
		else
		{
			System.out.println("Loaded collections.");
			Test.print("Exporting collection data ..." );
			TestImportExport.testExport(STORAGE, Test.provideTimestampedDirectory("testCollections"));
		}
		
		for(final Object e : ROOT.get())
		{
			System.out.println(e.getClass().getSimpleName() + ":");
			System.out.println(e);
			System.out.println("\n");
		}
		
		System.exit(0);
	}
	
	
	private static Object[] createRoot()
	{
		return new Object[]{
			populate(new ArrayList<>())            ,
			populate(new HashSet<>())              ,
			populate(new ArrayDeque<>())           ,
			populate(new HashMap<>())              ,
			populate(new Hashtable<>())            ,
			populate(new LinkedHashSet<>())        ,
			populate(new LinkedHashMap<>())        ,
			populate(new IdentityHashMap<>())      ,
			populate(new LinkedList<>())           ,
			populate(new PriorityQueue<>())        ,
			populate(new TreeMap<>())              ,
			populate(new TreeSet<>())              ,
			populate(new Vector<>())               ,
			populate(new Stack<>())                ,
			populate(new Properties())             ,
			populate(new ConcurrentHashMap<>())    ,
			populate(new ConcurrentLinkedQueue<>()),
			populate(new ConcurrentLinkedDeque<>()),
			populate(new ConcurrentSkipListSet<>()),
			populate(new ConcurrentSkipListMap<Integer, String>(new IntegerComparator()))
		};
	}
	
	private static Collection<String> populate(final Collection<String> collection)
	{
		for(int i = 0; i < 50; i++)
		{
			collection.add(UUID.randomUUID().toString());
		}
		return collection;
	}
	
	private static Map<Integer, String> populate(final Map<Integer, String> map)
	{
		for(int i = 0; i < 50; i++)
		{
			map.put(i, UUID.randomUUID().toString());
		}
		return map;
	}
	
	private static Properties populate(final Properties properties)
	{
		for(int i = 0; i < 50; i++)
		{
			properties.put("" + i, UUID.randomUUID().toString());
		}
		return properties;
	}
	
	// cannot be a lambda in order to be persistable
	static final class IntegerComparator implements Comparator<Integer>
	{
		@Override
		public int compare(final Integer o1, final Integer o2)
		{
			// nulls ignored for simple example
			return o1.compareTo(o2);
		}
	}
	
}
