package one.microstream.storage.restclient.jersey;

import static one.microstream.X.notNull;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import one.microstream.persistence.binary.types.BinaryFieldLengthResolver;
import one.microstream.persistence.types.PersistenceTypeDescription;
import one.microstream.persistence.types.PersistenceTypeDictionaryParser;
import one.microstream.persistence.types.PersistenceTypeNameMapper;
import one.microstream.persistence.types.PersistenceTypeResolver;
import one.microstream.reflect.ClassLoaderProvider;
import one.microstream.storage.restadapter.ViewerObjectDescription;
import one.microstream.storage.restadapter.ViewerRootDescription;
import one.microstream.storage.restadapter.ViewerStorageFileStatistics;
import one.microstream.storage.restclient.ObjectRequest;
import one.microstream.storage.restclient.StorageRestClient;

public interface StorageRestClientJersey extends StorageRestClient
{
	public static StorageRestClientJersey New(
		final String baseUrl
	)
	{
		return new Default(
			notNull(baseUrl),
			Routes.Default()
		);
	}
	
	public static StorageRestClientJersey New(
		final String baseUrl,
		final Routes routes
	)
	{
		return new Default(
			notNull(baseUrl),
			notNull(routes)
		);
	}
	
	
	public static class Default implements StorageRestClientJersey
	{
		private final String baseUrl;
		private final Routes routes;
		private Client       client;
		private WebTarget    storageRestService;
		
		Default(
			final String baseUrl,
			final Routes routes
		)
		{
			super();
			this.baseUrl = baseUrl;
			this.routes  = routes;
		}
		
		private WebTarget storageRestService()
		{
			if(this.storageRestService == null)
			{
				this.client = ClientBuilder.newBuilder()
					.register(GsonReader.class)
					.build();
				
				this.storageRestService = this.client.target(this.baseUrl);
			}
			
			return this.storageRestService;
		}
	
		@Override
		public Map<Long, PersistenceTypeDescription> requestTypeDictionary()
		{
			final String data = this.storageRestService()
				.path(this.routes.dictionary())
				.request(MediaType.APPLICATION_JSON)
				.get(String.class);
			
			final PersistenceTypeDictionaryParser parser = PersistenceTypeDictionaryParser.New(
				PersistenceTypeResolver.New(ClassLoaderProvider.System()),
				new BinaryFieldLengthResolver.Default(),
				PersistenceTypeNameMapper.New()
			);
			
			final Map<Long, PersistenceTypeDescription> dictionary = new HashMap<>();
			parser.parseTypeDictionaryEntries(data).forEach(entry -> dictionary.put(entry.typeId(), entry));
			return dictionary;
		}
	
		@Override
		public ViewerRootDescription requestRoot()
		{
			return this.storageRestService()
				.path(this.routes.root())
				.request(MediaType.APPLICATION_JSON)
				.get(ViewerRootDescription.class);
		}
		
		@Override
		public ViewerObjectDescription requestObject(
			final ObjectRequest objectRequest
		)
		{
			WebTarget target = this.storageRestService()
				.path(this.routes.object())
				.path(Long.toString(objectRequest.objectId()));
			
			target = this.optAddParam(target, "valueLength",     objectRequest.valueLength    ());
			target = this.optAddParam(target, "fixedOffset",     objectRequest.fixedOffset    ());
			target = this.optAddParam(target, "fixedLength",     objectRequest.fixedLength    ());
			target = this.optAddParam(target, "variableOffset",  objectRequest.variableOffset ());
			target = this.optAddParam(target, "variableLength",  objectRequest.variableLength ());
			target = this.optAddParam(target, "references",      objectRequest.references     ());
							
			return target
				.request(MediaType.APPLICATION_JSON)
				.get(ViewerObjectDescription.class);
		}
		
		private WebTarget optAddParam(final WebTarget target, final String name, final Object value)
		{
			return value != null
				? target.queryParam(name, value)
				: target;
		}
	
		@Override
		public ViewerStorageFileStatistics requestFileStatistics()
		{
			return this.storageRestService()
				.path(this.routes.filesStatistics())
				.request(MediaType.APPLICATION_JSON)
				.get(ViewerStorageFileStatistics.class);
		}
	
		@Override
		public void close()
		{
			if(this.client != null)
			{
				this.client.close();
				this.client = null;
			}
			
			this.storageRestService = null;
		}
	
	}
	
}