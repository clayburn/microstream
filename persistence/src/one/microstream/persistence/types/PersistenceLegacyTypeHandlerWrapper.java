package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import java.util.function.Consumer;

import one.microstream.collections.types.XGettingEnum;
import one.microstream.persistence.exceptions.PersistenceExceptionTypeNotPersistable;

public class PersistenceLegacyTypeHandlerWrapper<M, T> extends PersistenceLegacyTypeHandler.Abstract<M, T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static <M, T> PersistenceLegacyTypeHandler<M, T> New(
		final PersistenceTypeDefinition    legacyTypeDefinition,
		final PersistenceTypeHandler<M, T> currentTypeHandler
	)
	{
		return new PersistenceLegacyTypeHandlerWrapper<>(
			notNull(legacyTypeDefinition),
			notNull(currentTypeHandler)
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final PersistenceTypeHandler<M, T> typeHandler;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	PersistenceLegacyTypeHandlerWrapper(
		final PersistenceTypeDefinition    typeDefinition,
		final PersistenceTypeHandler<M, T> typeHandler
	)
	{
		super(typeDefinition);
		this.typeHandler = typeHandler;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public boolean hasInstanceReferences()
	{
		return this.typeHandler.hasInstanceReferences();
	}

	@Override
	public void iterateInstanceReferences(final T instance, final PersistenceFunction iterator)
	{
		this.typeHandler.iterateInstanceReferences(instance, iterator);
	}

	@Override
	public void iterateLoadableReferences(final M medium, final PersistenceObjectIdAcceptor iterator)
	{
		// current type handler perfectly fits the old types structure, so it can be used here.
		this.typeHandler.iterateLoadableReferences(medium, iterator);
	}

	@Override
	public T create(final M medium, final PersistenceObjectIdResolver idResolver)
	{
		return this.typeHandler.create(medium, idResolver);
	}

	@Override
	public void update(final M medium, final T instance, final PersistenceObjectIdResolver idResolver)
	{
		this.typeHandler.update(medium, instance, idResolver);
	}

	@Override
	public void complete(final M medium, final T instance, final PersistenceObjectIdResolver idResolver)
	{
		this.typeHandler.complete(medium, instance, idResolver);
	}
	
	@Override
	public <C extends Consumer<? super Class<?>>> C iterateMemberTypes(final C logic)
	{
		return this.typeHandler.iterateMemberTypes(logic);
	}
	
	@Override
	public final Class<T> type()
	{
		return this.typeHandler.type();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// default method implementations //
	///////////////////////////////////
	
	/*
	 * Tricky:
	 * Must pass through all default methods to be a correct wrapper.
	 * Otherwise, the wrapper changes the behavior in an unwanted fashion.
	 */
	
	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMember> membersInDeclaredOrder()
	{
		// Must pass through all default methods to be a correct wrapper.
		return this.typeHandler.membersInDeclaredOrder();
	}
	
	@Override
	public void guaranteeSpecificInstanceViablity() throws PersistenceExceptionTypeNotPersistable
	{
		// Must pass through all default methods to be a correct wrapper.
		this.typeHandler.guaranteeSpecificInstanceViablity();
	}
	
	@Override
	public boolean isSpecificInstanceViable()
	{
		// Must pass through all default methods to be a correct wrapper.
		return this.typeHandler.isSpecificInstanceViable();
	}
	
	@Override
	public void guaranteeSubTypeInstanceViablity() throws PersistenceExceptionTypeNotPersistable
	{
		// Must pass through all default methods to be a correct wrapper.
		this.typeHandler.guaranteeSubTypeInstanceViablity();
	}
	
	@Override
	public boolean isSubTypeInstanceViable()
	{
		// Must pass through all default methods to be a correct wrapper.
		return this.typeHandler.isSubTypeInstanceViable();
	}
	
	@Override
	public Object[] collectEnumConstants()
	{
		// Must pass through all default methods to be a correct wrapper.
		return this.typeHandler.collectEnumConstants();
	}
	
	@Override
	public int getPersistedEnumOrdinal(final M medium)
	{
		// Must pass through all default methods to be a correct wrapper.
		return this.typeHandler.getPersistedEnumOrdinal(medium);
	}
	
}