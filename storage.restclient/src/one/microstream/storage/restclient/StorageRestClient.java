
package one.microstream.storage.restclient;

import java.util.Map;

import one.microstream.persistence.types.PersistenceTypeDescription;
import one.microstream.storage.restadapter.ViewerObjectDescription;
import one.microstream.storage.restadapter.ViewerRootDescription;
import one.microstream.storage.restadapter.ViewerStorageFileStatistics;


public interface StorageRestClient extends AutoCloseable
{
	public Map<Long, PersistenceTypeDescription> requestTypeDictionary();
	
	public ViewerRootDescription requestRoot();
	
	public ViewerObjectDescription requestObject(
		long oid
	);
	
	public ViewerObjectDescription requestObjectWithReferences(
		long oid
	);
	
	public ViewerObjectDescription requestObjectWithReferences(
		long oid,
		long referenceOffset,
		long referenceLength
	);
	
	public ViewerStorageFileStatistics requestFileStatistics();
	
	@Override
	public void close();
	
}