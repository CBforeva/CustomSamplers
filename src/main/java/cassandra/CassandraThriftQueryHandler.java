package cassandra;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import utils.CustomSamplersException;
import utils.QueryHandler;
/*import me.prettyprint.cassandra.serializers.BytesArraySerializer;
import me.prettyprint.cassandra.serializers.CompositeSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.ColumnSliceIterator;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.Composite;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.exceptions.HectorException;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.ColumnQuery;
import me.prettyprint.hector.api.query.QueryResult;*/

@Deprecated
public class CassandraThriftQueryHandler implements QueryHandler {

	/*private static Cluster cluster;
	private static Keyspace keyspace;
	private final static String iovCFName = "IOV";
	private final static String payloadCFName = "PAYLOAD";*/

	public CassandraThriftQueryHandler(String clusterName) throws CustomSamplersException {
		/*cluster = (Cluster) CassandraConfigElement.getCassandraCluster(clusterName);
		KeyspaceDefinition ksDef = cluster.describeKeyspace("testKS");
		if (ksDef == null) {
			throw new CustomSamplersException("Keyspace testKS not found in the "
					+ cluster.getName() + " cluster");
		}
		keyspace = HFactory.createKeyspace(ksDef.getName(), cluster);
		if (keyspace == null) {
			throw new CustomSamplersException("Keyspace not found based on the definition: "
					+ ksDef.getName());
		}*/
	}

	@Override
	public ByteBuffer getData(String tagName, long since)
			throws CustomSamplersException {
		/*ByteArrayOutputStream result = new ByteArrayOutputStream();
		try {
			StringSerializer ss = StringSerializer.get();

			Composite key = new Composite();
			key.addComponent(tagName, ss);
			key.addComponent(String.valueOf(since), ss);

			ColumnQuery<Composite, String, String> iovQuery =
					HFactory.createColumnQuery(keyspace, new CompositeSerializer(), ss, ss);
			iovQuery.setColumnFamily(iovCFName).setKey(key).setName("hash");
			QueryResult<HColumn<String, String> > iovResult = iovQuery.execute();
			String hash = iovResult.get().getValue();

			ColumnQuery<String, String, byte[]> plQuery = 
					HFactory.createColumnQuery(keyspace, ss, ss, BytesArraySerializer.get());
			plQuery.setColumnFamily(payloadCFName).setKey(hash).setName("data");
			QueryResult<HColumn<String, byte[]> > plResult = plQuery.execute();
			result.write(plResult.get().getValue());
		} catch (HectorException he) {
			throw new CustomSamplersException("HectorException occured during write attempt:" + he.toString());
		} catch (IOException e) {
			throw new CustomSamplersException("IOException occured during write attempt:" + e.toString());
		}*/
		return null;
	}

	@Override
	public void putData(HashMap<String, String> metaInfo, ByteArrayOutputStream payload,
			ByteArrayOutputStream streamerInfo) throws CustomSamplersException {
		try {
			/*StringSerializer ss = StringSerializer.get();
			CompositeSerializer cs = new CompositeSerializer();
			Composite key = new Composite();
			key.addComponent(metaInfo.get("tag_name"), ss);
			key.addComponent(metaInfo.get("since"), ss);

			String hash = metaInfo.get("payload_hash");
			Mutator<Composite> compMutator = HFactory.createMutator(keyspace, cs);
			compMutator.addInsertion(key, iovCFName, HFactory.createColumn("hash", hash, ss, ss));
			compMutator.execute();

			Mutator<String> strMutator = HFactory.createMutator(keyspace, ss);
			strMutator.addInsertion(hash, payloadCFName,
					HFactory.createColumn("data", payload.toByteArray(), ss, BytesArraySerializer.get()));
			strMutator.execute();*/
		} catch (Exception he) {
			throw new CustomSamplersException("Hector exception occured:" + he.toString());
		}
	}

	@Override
	public TreeMap<Integer, ByteBuffer> getChunks(String tagName, long since)
			throws CustomSamplersException {
		TreeMap<Integer, ByteBuffer> result = new TreeMap<Integer, ByteBuffer>();
		try {
			/*StringSerializer ss = StringSerializer.get();

			Composite key = new Composite();
			key.addComponent(tagName, ss);
			key.addComponent(String.valueOf(since), ss);*/

			//ColumnQuery<Composite, String, String> iovQuery =
			//		HFactory.createColumnQuery(keyspace, CompositeSerializer.get(), ss, ss);
			//iovQuery.setColumnFamily(iovCFName).setKey(key).setName("hash");
			//QueryResult<HColumn<String, String> > iovResult = iovQuery.execute();
			//String payloadHash = iovResult.get().getValue();

			//SliceQuery<String, Composite, byte[]> plQuery = HFactory
			//		.createSliceQuery(keyspace, ss, CompositeSerializer.get(), BytesArraySerializer.get())
			//		.setColumnFamily(payloadCFName)
			//		.setKey(payloadHash);

			/*ColumnSliceIterator<String, Composite, byte[]> iterator = 
					new ColumnSliceIterator<String, Composite, byte[]>(
							plQuery, new Composite(), new Composite(), false);*/

			/*while (iterator.hasNext()) {
				HColumn<Composite, byte[]> chunkColumn = iterator.next();
				Composite idAndHash = chunkColumn.getName();
				Integer id = idAndHash.get(0, IntegerSerializer.get());

				ByteArrayOutputStream cBaos = new ByteArrayOutputStream();
				cBaos.write(chunkColumn.getValue());
				result.put(id, cBaos);
			}*/
		} catch (Exception he) {
			throw new CustomSamplersException("HectorException occured during write attempt:" + he.toString());
		} /*catch (IOException e) {
			throw new CustomSamplersException("IOException occured during write attempt:" + e.toString());
		}*/
		return result;
	}

	@Override
	public void putChunks(HashMap<String, String> metaInfo,
			List<ByteArrayOutputStream> chunks) throws CustomSamplersException {
		/*try {
			StringSerializer ss = StringSerializer.get();
			Composite key = new Composite();
			key.addComponent(metaInfo.get("tag_name"), ss);
			key.addComponent(metaInfo.get("since"), ss);

			String payloadHash = metaInfo.get("payload_hash");
			HFactory.createMutator(keyspace, CompositeSerializer.get())
					.addInsertion(key, iovCFName, HFactory.createColumn("payload_hash", payloadHash))
					.execute();

			Mutator<String> strMutator = HFactory.createMutator(keyspace, ss);
			for (int i = 0; i < chunks.size(); ++i) {
				String chunkHash = metaInfo.get(String.valueOf(i+1)); 
				Composite cName = new Composite();
				cName.addComponent(i+1, IntegerSerializer.get());
				cName.addComponent(chunkHash, ss);
				strMutator.addInsertion(payloadHash, payloadCFName,
						HFactory.createColumn(cName, chunks.get(i).toByteArray()));
			}
			strMutator.execute();

		} catch (HectorException he) {
			throw new CustomSamplersException("Hector exception occured:" + he.toString());
		}*/
	}

	@Override
	public void closeResources() throws CustomSamplersException {
		// TODO Auto-generated method stub
		
	}

}
