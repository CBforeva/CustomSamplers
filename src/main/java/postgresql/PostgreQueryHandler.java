package postgresql;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import customjdbc.CustomJDBCConfigElement;

import utils.CustomSamplersException;
import utils.QueryHandler;

public class PostgreQueryHandler implements QueryHandler {

	private static Connection connection;

	public PostgreQueryHandler(String databaseName)
			throws CustomSamplersException {
		connection = CustomJDBCConfigElement.getJDBCConnection(databaseName);
		if (connection == null)
			throw new CustomSamplersException("JDBCConnection instance with name: " + databaseName + " was not found in config!");
	}

	@Override
	public ByteBuffer getData(String tagName, long since)
			throws CustomSamplersException {
		ByteBuffer result = null;
		try {
			PreparedStatement ps = connection.prepareStatement("SELECT data FROM PAYLOAD p, "
					+ "(SELECT payload_hash FROM IOV WHERE tag_name=? AND since=?) iov "
					+ "WHERE p.hash = iov.payload_hash");
			ps.setString(1, tagName);
			ps.setLong(2, since);
			ResultSet rs = ps.executeQuery();

			if (rs != null) {
				int counter = 0;
				while(rs.next()) {
					rs.getBytes("data");
					result = ByteBuffer.wrap(rs.getBytes("data"));
					counter++;
				}
				if (counter > 1) {
					throw new CustomSamplersException("More than one payload found for "
							+ "TAG=" + tagName + " SINCE=" + since +" !");
				}
				rs.close();

			} else {

				throw new CustomSamplersException("Payload not found for "
						+ "TAG=" + tagName + " SINCE=" + since +" !");
			}

			ps.close();
		} catch (SQLException e) {
			throw new CustomSamplersException("SQLException occured during read attempt: " + e.toString());
		}
		return result;
	}

	@Override
	public void putData(HashMap<String, String> metaInfo, ByteArrayOutputStream payload,
			ByteArrayOutputStream streamerInfo) throws CustomSamplersException {
		writePayload(metaInfo, payload.toByteArray(), streamerInfo.toByteArray());
		writeIov(metaInfo);
	}

	@Override
	public TreeMap<Integer, ByteBuffer> getChunks(String tagName, long since)
			throws CustomSamplersException {
		TreeMap<Integer, ByteBuffer> result = new TreeMap<Integer, ByteBuffer>();
		try {
			PreparedStatement ps = connection.prepareStatement("SELECT id, data "
					+ "FROM CHUNK c, (SELECT payload_hash FROM IOV WHERE tag_name=? AND since=?) iov "
					+ "WHERE c.payload_hash = iov.payload_hash");
			ps.setString(1, tagName);
			ps.setLong(2, since);
			ResultSet rs = ps.executeQuery();
			if (rs != null) {
				while(rs.next()) {
					result.put(rs.getInt("id"), ByteBuffer.wrap(rs.getBytes("data")));
				}
				rs.close();
			} else {
				throw new CustomSamplersException("Payload not found for "
						+ "TAG=" + tagName + " SINCE=" + since +" ! (via chunk read)");
			}
			ps.close();
		} catch (SQLException e) {
			throw new CustomSamplersException("SQLException occured during read attempt: " + e.toString());
		}
		return result;
	}

	@Override
	public void putChunks(HashMap<String, String> metaInfo,
			List<ByteArrayOutputStream> chunks) throws CustomSamplersException {
		try {
			writePayload(metaInfo, new byte[0], new byte[0]);
			writeIov(metaInfo);

			for (int i = 0; i < chunks.size(); ++i) {
				PreparedStatement ps = connection.prepareStatement("INSERT INTO CHUNK"
						+ " (PAYLOAD_HASH, CHUNK_HASH, ID, DATA) VALUES (?, ?, ?, ?)");
				ps.setString(1, metaInfo.get("payload_hash"));
				Integer id = i + 1;
				ps.setString(2, metaInfo.get(id.toString()));
				ps.setInt(3, id);
				byte[] chunk = chunks.get(i).toByteArray();
				ps.setBinaryStream(4, new ByteArrayInputStream(chunk), chunk.length);
				ps.execute();
				ps.close();
			}
		} catch (SQLException se) {
			throw new CustomSamplersException("SQLException occured during write attempt: " + se.toString());
		}
	}

	public void writePayload(HashMap<String, String> metaInfo, byte[] payload, byte[] streamerInfo)
			throws CustomSamplersException {
		try {
			PreparedStatement ps = connection.prepareStatement("INSERT INTO PAYLOAD"
					+ " (HASH, OBJECT_TYPE, DATA, STREAMER_INFO, VERSION, CREATION_TIME, CMSSW_RELEASE)"
					+ " VALUES (?, ?, ?, ?, ?, ?, ?)");
			ps.setString(1, metaInfo.get("payload_hash"));
			ps.setString(2, metaInfo.get("object_type"));
			ps.setBinaryStream(3, new ByteArrayInputStream(payload), payload.length);
			ps.setBinaryStream(4, new ByteArrayInputStream(streamerInfo), streamerInfo.length);
			ps.setString(5, metaInfo.get("version"));
			ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
			ps.setString(7, metaInfo.get("cmssw_release"));
			ps.execute();
			ps.close();
		} catch (SQLException se) {
			throw new CustomSamplersException("SQLException occured during write attempt: " + se.toString());
		}
	}

	public void writeIov(HashMap<String, String> keyAndMetaMap)
			throws CustomSamplersException {
		try {
			PreparedStatement ps = connection.prepareStatement("INSERT INTO IOV"
					+ " (TAG_NAME, SINCE, PAYLOAD_HASH, INSERT_TIME) VALUES (?, ?, ?, ?)");
			ps.setString(1, keyAndMetaMap.get("tag_name"));
			ps.setLong(2, Long.parseLong(keyAndMetaMap.get("since")));
			ps.setString(3, keyAndMetaMap.get("payload_hash"));
			ps.setDate(4, new Date(System.currentTimeMillis()));
			ps.execute();
			ps.close();
		} catch (SQLException se) {
			throw new CustomSamplersException("SQLException occured during write attempt: " + se.toString());
		}
	}

	public String readIov(HashMap<String, String> keyMap)
			throws CustomSamplersException {
		String result = null;
		try {
			PreparedStatement ps = connection.prepareStatement("SELECT PAYLOAD_HASH FROM IOV"
					+ " WHERE TAG_NAME=? AND SINCE=?");
			ps.setString(1, keyMap.get("tag_name"));
			ps.setLong(2, Long.parseLong(keyMap.get("since")));
			ResultSet rs = ps.executeQuery();

			if (rs != null) {
				int counter = 0;
				while(rs.next()) {
					result = rs.getString("PAYLOAD_HASH");
					counter++;
				}
				if (counter > 1) {
					throw new CustomSamplersException("More than one row found with "
							+ "tag_name=" + keyMap.get("tag_name")
							+ " and since=" + keyMap.get("since") + " in IOV !");
				}
				rs.close();

			} else {

				throw new CustomSamplersException("The row with"
						+ " tag_name=" + keyMap.get("tag_name")
						+ " and since=" + keyMap.get("since")
						+ " is not found in IOV!");
			}

			ps.close();
		} catch (SQLException e) {
			throw new CustomSamplersException("SQLException occured during read attempt: " + e.toString());
		}
		return result;
	}

	public void writeTag(HashMap<String, String> metaMap)
			throws CustomSamplersException {
		try {
			PreparedStatement ps = connection.prepareStatement("INSERT INTO TAG"
					+ " (NAME, REVISION, REVISION_TIME, COMMENT, TIME_TYPE, OBJECT_TYPE,"
					+ " LAST_VALIDATED, END_OF_VALIDITY, LAST_SINCE, LAST_SINCE_PID, CREATION_TIME)"
					+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			ps.setString(1, metaMap.get("name"));
			ps.setInt(2, Integer.parseInt(metaMap.get("revision")));
			ps.setDate(3, new Date(Long.parseLong(metaMap.get("revision_time"))));
			ps.setString(4, metaMap.get("comment"));
			ps.setInt(5, Integer.parseInt(metaMap.get("time_type")));
			ps.setString(6, metaMap.get("object_type"));
			ps.setInt(7, Integer.parseInt(metaMap.get("last_validated")));
			ps.setInt(8, Integer.parseInt(metaMap.get("end_of_validity")));
			ps.setInt(9, Integer.parseInt(metaMap.get("last_since")));
			ps.setInt(10, Integer.parseInt(metaMap.get("last_since_pid")));
			ps.setTimestamp(11, new Timestamp(System.currentTimeMillis()));
		} catch (SQLException se) {
			throw new CustomSamplersException("SQLException occured during write attempt: " + se.toString());
		}
	}

	public HashMap<String, Object> readTag(String tagKey)
			throws CustomSamplersException {
		HashMap<String, Object> result = new HashMap<String, Object>();
		try {
			PreparedStatement ps = connection.prepareStatement("SELECT REVISION, REVISION_TIME,"
					+ " COMMENT, TIME_TYPE, OBJECT_TYPE, LAST_VALIDATED, END_OF_VALIDITY,"
					+ " LAST_SINCE, LAST_SINCE_PID, CREATION_TIME"
					+ " FROM `TAG` WHERE NAME=?");
			ps.setString(1, tagKey);
			ResultSet rs = ps.executeQuery();

			if (rs != null) {
				int counter = 0;
				while(rs.next()) {
					result.put("revision", rs.getObject("REVISION"));
					result.put("revision_time", rs.getObject("REVISION_TIME"));
					result.put("comment", rs.getObject("COMMENT"));
					result.put("time_type", rs.getObject("TIME_TYPE"));
					result.put("object_type", rs.getObject("OBJECT_TYPE"));
					result.put("last_validated", rs.getObject("LAST_VALIDATED"));
					result.put("end_of_validity", rs.getObject("END_OF_VALIDITY"));
					result.put("last_since", rs.getObject("LAST_SINCE"));
					result.put("last_since_pid", rs.getObject("LAST_SINCE_PID"));
					result.put("creation_time", rs.getObject("CREATION_TIME"));
					counter++;
				}
				if (counter > 1) {
					throw new CustomSamplersException("More than one row found with"
							+ " name=" + tagKey + " in TAG !");
				}
				rs.close();

			} else {

				throw new CustomSamplersException("The row with"
						+ " name=" + tagKey + " is not found in TAG!");
			}

			ps.close();
		} catch (SQLException e) {
			throw new CustomSamplersException("SQLException occured during read attempt: " + e.toString());
		}
		return result;
	}

	@Override
	public void closeResources() throws CustomSamplersException {
		// TODO Auto-generated method stub
		
	}

}
