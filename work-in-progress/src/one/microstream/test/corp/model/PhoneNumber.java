package one.microstream.test.corp.model;

public final class PhoneNumber
{
	////////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final String number     ;
	final String description;



	////////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PhoneNumber(final String number, final String description)
	{
		super();
		this.number      = number     ;
		this.description = description;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	public final String number()
	{
		return this.number;
	}

	public final String description()
	{
		return this.description;
	}



	////////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final String toString()
	{
		return this.number();
	}

}