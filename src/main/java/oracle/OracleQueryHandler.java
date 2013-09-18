package oracle;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;

import customjdbc.CustomJDBCConfigElement;

import utils.CustomSamplersException;
import utils.NotFoundInDBException;
import utils.QueryHandler;

public class OracleQueryHandler implements QueryHandler {

	private static Connection connection;

	public OracleQueryHandler(String databaseName) 
			throws CustomSamplersException, NotFoundInDBException {
		connection = CustomJDBCConfigElement.getJDBCConnection(databaseName);
		if (connection == null) {
			throw new NotFoundInDBException("JDBCConnection instance with name: " + databaseName + " was not found in config!");
		}
	}

	@Deprecated
	@Override
	public void writeBinary(String binaryID, String chunkID, String hash,
			byte[] fileContent, boolean isSpecial)
					throws CustomSamplersException {
		System.out.println("This function is Deprecated! Do not use it!");
	}

	@Deprecated
	@Override
	public byte[] readBinary(String binaryID, String chunkID, String hash, boolean isSpecial)
			throws CustomSamplersException {
		System.out.println("This function is Deprecated! Do not use it!");
		return null;
	}

	@Override
	public void writeChunk(HashMap<String, String> metaInfo, String chunkID, byte[] chunk,
			Boolean isSpecial) throws CustomSamplersException {
		PreparedStatement ps;
		try {
			ps = connection.prepareStatement("INSERT INTO CHUNK"
					+ " (PAYLOAD_HASH, CHUNK_HASH, DATA)"
					+ " VALUES (?, ?, ?)");
			ps.setString(1, metaInfo.get("payload_hash"));
			ps.setString(2, metaInfo.get(chunkID));
			ps.setBinaryStream(3, new ByteArrayInputStream(chunk), chunk.length);
			ps.execute();
			ps.close();
		} catch (SQLException se) {
			throw new CustomSamplersException("SQLException occured during write attempt: " + se.toString());
		}
	}

	@Override
	public byte[] readChunk(String hashKey, String chunkHashKey,
			boolean isSpecial) throws CustomSamplersException {
		PreparedStatement ps;
		byte[] result = null;
		try {
			ps = connection.prepareStatement("SELECT DATA FROM CHUNK WHERE PAYLOAD_HASH=? AND CHUNK_HASH=?");
			ps.setString(1, hashKey);
			ps.setString(2, chunkHashKey);
			ResultSet rs = ps.executeQuery();

			if (rs != null) {
				int counter = 0;
				while(rs.next()) {
					result = rs.getBytes("DATA");
					counter++;
				}
				if (counter > 1) {
					throw new CustomSamplersException("More than one row found with "
							+ "hash=" + hashKey + " and chunk_hash=" + chunkHashKey + " in CHUNK !");
				}
				rs.close();

			} else {

				throw new CustomSamplersException("The row with hash=" + hashKey
						+ " chunk_hash=" +chunkHashKey + " not found in CHUNK!");
			}

			ps.close();
		} catch (SQLException e) {
			throw new CustomSamplersException("SQLException occured during read attempt: " + e.toString());
		}
		return result;
	}
	
	@Override
	public void writePayload(HashMap<String, String> metaInfo,
			byte[] payload, byte[] streamerInfo, boolean isSpecial)
					throws CustomSamplersException {
		PreparedStatement ps;
		try {
			ps = connection.prepareStatement("INSERT INTO PAYLOAD"
					+ " (HASH, OBJECT_TYPE, DATA, STREAMER_INFO, VERSION, CREATION_TIME, CMSSW_RELEASE)"
					+ " VALUES (?, ?, ?, ?, ?, ?, ?)");
			ps.setString(1, metaInfo.get("payload_hash"));
			ps.setString(2, metaInfo.get("object_type"));
			if (payload != null) {
				ps.setBinaryStream(3, new ByteArrayInputStream(payload), payload.length);
			} else {
				ps.setBinaryStream(3, new ByteArrayInputStream(new byte[0]));
			}
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

	@Override
	public byte[] readPayload(String hashKey, boolean isSpecial) 
			throws CustomSamplersException {
		PreparedStatement ps;
		byte[] result = null;
		try {
			ps = connection.prepareStatement("SELECT DATA FROM PAYLOAD WHERE HASH=?");
			ps.setString(1, hashKey);
			ResultSet rs = ps.executeQuery();

			if (rs != null) {
				int counter = 0;
				while(rs.next()) {
					result = rs.getBytes("DATA");
					counter++;
				}
				if (counter > 1) {
					throw new CustomSamplersException("More than one row found with hash="
							+ hashKey + " in PAYLOAD !");
				}
				rs.close();

			} else {

				throw new CustomSamplersException("The row with hash=" + hashKey
						+ " not found in PAYLOAD!");
			}

			ps.close();
		} catch (SQLException e) {
			throw new CustomSamplersException("SQLException occured during read attempt: " + e.toString());
		}
		return result;
	}

	public void writeTag(HashMap<String, String> stringMetaInfo, HashMap<String, Integer> intMetaInfo, boolean isSpecial)
			throws CustomSamplersException {
		PreparedStatement ps;
		try {
			ps = connection.prepareStatement("INSERT INTO TAG"
					+ " (NAME, REVISION, REVISION_TIME, COMMENT, TIME_TYPE, OBJECT_TYPE,"
					+ " LAST_VALIDATED, END_OF_VALIDITY, LAST_SINCE, LAST_SINCE_PID, CREATION_TIME)"
					+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			ps.setString(1, stringMetaInfo.get("name"));
			ps.setInt(2, intMetaInfo.get("revision"));
			ps.setDate(3, new Date(Long.parseLong(stringMetaInfo.get("revision_time"))));
			ps.setString(4, stringMetaInfo.get("comment"));
			ps.setInt(5, intMetaInfo.get("time_type"));
			ps.setString(6, stringMetaInfo.get("object_type"));
			ps.setInt(7, intMetaInfo.get("last_validated_time"));
			ps.setInt(8, intMetaInfo.get("end_of_validity"));
			ps.setInt(9, intMetaInfo.get("last_since"));
			ps.setInt(10, intMetaInfo.get("last_since_pid"));
			ps.setTimestamp(11, new Timestamp(System.currentTimeMillis()));
			ps.execute();
			ps.close();
		} catch (SQLException se) {
			throw new CustomSamplersException("SQLException occured during write attempt: " + se.toString());
		}
	}

	@Override
	public void writeIov(HashMap<String, String> keyAndMetaMap)
			throws CustomSamplersException {
		PreparedStatement ps;
		try {
			ps = connection.prepareStatement("INSERT INTO IOV"
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

	@Override
	public String readIov(HashMap<String, String> keyMap)
			throws CustomSamplersException {
		PreparedStatement ps;
		String result = null;
		try {
			ps = connection.prepareStatement("SELECT PAYLOAD_HASH FROM IOV"
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

	@Override
	public void writeTag(HashMap<String, String> metaMap)
			throws CustomSamplersException {
		PreparedStatement ps;
		try {
			ps = connection.prepareStatement("INSERT INTO TAG"
					+ " (NAME, REVISION, REVISION_TIME, COMMENT, TIME_TYPE, OBJECT_TYPE,"
					+ " LAST_VALIDATED, END_OF_VALIDITY, LAST_SINCE, LAST_SINCE_PID, CREATION_TIME)"
					+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			ps.setString(1, metaMap.get("name"));
			ps.setInt(2, Integer.parseInt(metaMap.get("revision")));
			ps.setDate(3, new Date(Long.parseLong(metaMap.get("revision_time"))));
			ps.setString(4, metaMap.get("comment"));
			ps.setInt(5, Integer.parseInt(metaMap.get("time_type")));
			ps.setString(6, metaMap.get("object_type"));
			ps.setInt(7, Integer.parseInt(metaMap.get("last_validated_time")));
			ps.setInt(8, Integer.parseInt(metaMap.get("end_of_validity")));
			ps.setInt(9, Integer.parseInt(metaMap.get("last_since")));
			ps.setInt(10, Integer.parseInt(metaMap.get("last_since_pid")));
			ps.setTimestamp(11, new Timestamp(System.currentTimeMillis()));
		} catch (SQLException se) {
			throw new CustomSamplersException("SQLException occured during write attempt: " + se.toString());
		}
	}

	@Override
	public HashMap<String, Object> readTag(String tagKey)
			throws CustomSamplersException {
		PreparedStatement ps;
		HashMap<String, Object> result = new HashMap<String, Object>();
		try {
			ps = connection.prepareStatement("SELECT REVISION, REVISION_TIME,"
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

}
