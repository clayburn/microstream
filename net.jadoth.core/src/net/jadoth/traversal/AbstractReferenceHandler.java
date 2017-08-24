package net.jadoth.traversal;

import java.util.function.Predicate;

import net.jadoth.collections.types.XSet;

public abstract class AbstractReferenceHandler implements TraversalReferenceHandler
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////
	
	static final int SEGMENT_SIZE = 50;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	static final Object[] createIterationSegment()
	{
		// one trailing slot as a pointer to the next segment array. Both hacky and elegant.
		return new Object[SEGMENT_SIZE + 1];
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	final TypeTraverserProvider  traverserProvider;
	final XSet<Object>           alreadyHandled   ;
	final TraversalPredicateSkip predicateSkip    ;
	final TraversalPredicateNode predicateNode    ;
	final TraversalPredicateLeaf predicateLeaf    ;
	final TraversalPredicateFull predicateFull    ;
	final Predicate<Object>      predicateHandle  ; // more used for logging stuff than for filtering, see skipping.

	Object[] head          ;
	Object[] lastHead      ;
	Object[] enqueueSegment;
	int      enqueueIndex  ;
	int      dequeueIndex  ;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	AbstractReferenceHandler(
		final TypeTraverserProvider  traverserProvider,
		final XSet<Object>           alreadyHandled   ,
		final TraversalPredicateSkip predicateSkip    ,
		final TraversalPredicateNode predicateNode    ,
		final TraversalPredicateLeaf predicateLeaf    ,
		final TraversalPredicateFull predicateFull    ,
		final Predicate<Object>      predicateHandle
	)
	{
		super();
		this.traverserProvider = traverserProvider;
		this.alreadyHandled    = alreadyHandled   ;
		this.predicateSkip     = predicateSkip    ;
		this.predicateNode     = predicateNode    ;
		this.predicateLeaf     = predicateLeaf    ;
		this.predicateFull     = predicateFull    ;
		this.predicateHandle   = predicateHandle  ;
		
		
		this.enqueueSegment = this.head = createIterationSegment();
		this.lastHead = createIterationSegment();
		setNextSegment(this.enqueueSegment, this.lastHead);
//		JadothConsole.debugln("terminating segment is " + Jadoth.systemString(this.lastHead));
//		JadothConsole.debugln("first enqueue segment is " + Jadoth.systemString(this.enqueueSegment));
		this.enqueueIndex = -1;
		this.dequeueIndex = SEGMENT_SIZE;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public final boolean skip(final Object instance)
	{
		return this.alreadyHandled.add(instance);
	}
	
	private static void setNextSegment(final Object[] previousSegment, final Object[] nextSegment)
	{
		previousSegment[SEGMENT_SIZE] = nextSegment;
	}
	
	private static Object[] getNextSegment(final Object[] previousSegment)
	{
		return (Object[])previousSegment[SEGMENT_SIZE];
	}
	
	@Override
	public final void enqueue(final Object instance)
	{
		// must check for null as there is no control over what custom handler implementations might pass.
		if(instance == null)
		{
			return;
		}
		if(!this.alreadyHandled.add(instance))
		{
			return;
		}
		if(this.predicateSkip != null && this.predicateSkip.skip(instance))
		{
			return;
		}
		
		if(++this.enqueueIndex >= SEGMENT_SIZE)
		{
			this.addEnqueuingSegment();
		}
//		JadothConsole.debugln("enqueuing to " + Jadoth.systemString(this.enqueueSegment) + "[" + this.enqueueIndex + "] = " + instance);
		this.enqueueSegment[this.enqueueIndex] = instance;
	}
	
	final void addEnqueuingSegment()
	{
		final Object[] newSegment = createIterationSegment();
		if(this.lastHead == null)
		{
			// switch to enqueuing mode and set the hint for the dequeuing logic
			this.lastHead     = this.head   ;
			this.head         = newSegment  ;
			this.dequeueIndex = SEGMENT_SIZE; // must be SIZE because of the pre-check preincrement!
//			JadothConsole.debugln("switch to enqueue mode. enqueueIndex = " + 0);
		}
		else
		{
			setNextSegment(this.enqueueSegment, newSegment);
		}
		setNextSegment(newSegment, this.lastHead);
//		JadothConsole.debugln(
//			"(" + Integer.toHexString(System.identityHashCode(this.enqueueSegment)) + ") - " +
//			"(" + Integer.toHexString(System.identityHashCode(newSegment)) + ") - " +
//			"(" + Integer.toHexString(System.identityHashCode(this.lastHead)) + ")"
//		);
		this.enqueueSegment = newSegment;
		this.enqueueIndex   = 0;
	}
	
	private void scrollToNextDequeueItem()
	{
		final Object[] seg = this.head;
		int i = this.dequeueIndex;
		while(++i < SEGMENT_SIZE)
		{
			if(seg[i] != null)
			{
				this.dequeueIndex = i;
				return;
			}
		}
		this.advanceHeadSegment(getNextSegment(this.head));
	}
					
	private Object dequeue()
	{
		// this indicates either a completely processed segment or a segment change via enqueue
		if(++this.dequeueIndex >= SEGMENT_SIZE)
		{
			this.updateDequeueSegment();
		}
		if(this.head[this.dequeueIndex] == null)
		{
			this.scrollToNextDequeueItem();
		}
		
		final Object next = this.head[this.dequeueIndex];
		this.head[this.dequeueIndex] = null;
		
//		JadothConsole.debugln(
//			"dequeuing from " + Jadoth.systemString(this.head) + "[" + this.dequeueIndex + "] = " + Jadoth.systemString(next)
//		);
		return next;
	}
	
	private void updateDequeueSegment()
	{
		if(this.lastHead != null)
		{
			// switch to dequeue mode
			this.dequeueIndex   =            0; // reset dequeueIndex for iterating the current head segment
			this.lastHead       =         null; // reset mode helper reference (effectively dequeue mode)
			this.enqueueIndex   = SEGMENT_SIZE; // hint to enqueuing logic. SIZE because of the pre-check preincrement!
			this.enqueueSegment =         null;
//			JadothConsole.debugln("switch to dequeue mode. dequeueIndex = " + this.dequeueIndex);
		}
		else
		{
			// already in dequeuing mode, hence simply advance to the next linked segment
			this.advanceHeadSegment(getNextSegment(this.head));
//			JadothConsole.debugln("next dequeue segment. dequeueIndex = " + this.dequeueIndex);
		}
	}
	
	final void advanceHeadSegment(final Object[] passed)
	{
		Object[] seg = passed;
		
		outer:
		while(true)
		{
			// if there is no more segment, the traversal is complete
			if(seg == null)
			{
				ObjectGraphTraverser.signalAbortTraversal();
				return; // effectively unreachable return, just to satisfy the compiler
			}
			else if(seg[0] != null)
			{
				// quick-check for the common case
				this.dequeueIndex = 0;
				break outer;
			}

			// scan current segument for next item
			int i = 0;
			while(++i < SEGMENT_SIZE)
			{
				if(seg[i] != null)
				{
					this.dequeueIndex = i;
					break outer;
				}
			}

			// current segment was completely empty, so move to the next
			seg = getNextSegment(seg);
		}

		this.head = seg;
	}

	private void enqueueAll(final Object[] instances)
	{
		for(final Object instance : instances)
		{
			this.enqueue(instance);
		}
	}
	
	@Override
	public final void handleAsFull(final Object[] instances)
	{
		this.enqueueAll(instances);

		try
		{
			while(true)
			{
				final Object instance = this.dequeue();
				if(this.predicateHandle != null && !this.predicateHandle.test(instance))
				{
					continue;
				}
				
				final TypeTraverser<Object> traverser = this.traverserProvider.provide(instance);
				
				if(this.predicateLeaf != null && this.predicateLeaf.isLeaf(instance))
				{
					this.handleLeaf(instance, traverser);
				}
				else if(this.predicateNode != null && this.predicateNode.isNode(instance))
				{
					this.handleNode(instance, traverser);
				}
				else
				{
					this.handleFull(instance, traverser);
				}
			}
		}
		catch(final TraversalSignalAbort s)
		{
			// some logic signaled to abort the traversal. So abort and return. (This is a signal, NOT a problem!)
			return;
		}
	}
	
	@Override
	public final void handleAsNode(final Object[] instances)
	{
		this.enqueueAll(instances);

		try
		{
			while(true)
			{
				final Object instance = this.dequeue();
				if(this.predicateHandle != null && !this.predicateHandle.test(instance))
				{
					continue;
				}
				
				final TypeTraverser<Object> traverser = this.traverserProvider.provide(instance);
				
				if(this.predicateFull != null && this.predicateFull.isFull(instance))
				{
					this.handleFull(instance, traverser);
				}
				else if(this.predicateLeaf != null && this.predicateLeaf.isLeaf(instance))
				{
					this.handleLeaf(instance, traverser);
				}
				else
				{
					this.handleNode(instance, traverser);
				}
			}
		}
		catch(final TraversalSignalAbort s)
		{
			// some logic signaled to abort the traversal. So abort and return. (This is a signal, NOT a problem!)
			return;
		}
	}
	
	@Override
	public final void handleAsLeaf(final Object[] instances)
	{
		this.enqueueAll(instances);

		try
		{
			while(true)
			{
				final Object instance = this.dequeue();
				if(this.predicateHandle != null && !this.predicateHandle.test(instance))
				{
					continue;
				}
				
				final TypeTraverser<Object> traverser = this.traverserProvider.provide(instance);
				
				if(this.predicateFull != null && this.predicateFull.isFull(instance))
				{
					this.handleFull(instance, traverser);
				}
				else if(this.predicateNode != null && this.predicateNode.isNode(instance))
				{
					this.handleNode(instance, traverser);
				}
				else
				{
					this.handleLeaf(instance, traverser);
				}
			}
		}
		catch(final TraversalSignalAbort s)
		{
			// some logic signaled to abort the traversal. So abort and return. (This is a signal, NOT a problem!)
			return;
		}
	}
	
	

	abstract <T> void handleFull(T instance, final TypeTraverser<T> traverser);
	
	abstract <T> void handleLeaf(T instance, final TypeTraverser<T> traverser);
	
	final <T> void handleNode(final T instance, final TypeTraverser<T> traverser)
	{
//		JadothConsole.debugln("Traversing NODE " + Jadoth.systemString(instance) + " via " + Jadoth.systemString(traverser));
		traverser.traverseReferences(instance, this);
	}
	
}
