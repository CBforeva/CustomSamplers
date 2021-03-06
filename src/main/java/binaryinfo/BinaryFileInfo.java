package binaryinfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * This static multiton class holds and handles every information of the test binary data that 
 * was generated with PaGeS (Payload Generator Script). It is always dedicated to a given testing 
 * scenario, as only one instance per path of this class exists during a test run. However, many 
 * Assignment objects may use the same data set on a different (assignment) way.
 * <p>
 * Note, that this class heavily relies on the PaGeS' generated directory structure!
 * Any modification on the generated data set or even on the script may cause wrong behavior in 
 * this class. Please make the appropriate changes here also.
 * <p>
 * Note2: The multiton pattern was introduced in order to create a single static instance
 * of every given test data location, in order to create multiple Assignment configurations 
 * for the same BinaryFileInfo instance.
 * */
public class BinaryFileInfo {

	/** The static private instance. */
	private static final HashMap<String, BinaryFileInfo> instance =
			new HashMap<String, BinaryFileInfo>();

	/** The absolute path to the test data set. */
	private String location;
	/** The number of files in the data set. */
	private int numOfFiles;

	/** A mapping between binary file names, and the appropriate meta information,
	 * that is read from the META-filename.bin.info under the file's chunk folder. */
	private TreeMap<String, HashMap<String, String> > metaInfo;
	/** A mapping between binary file names, and the full path to the appropriate file. */
	private TreeMap<String, String> filePathList;
	/** The array of every binary file names. Directly copied from the filePathList's key-set.
	 * Stored separately for fast threadID - binaryID mapping and lookup.*/
	private String[] fileNameArray;

	/** A mapping between binary file names and chunk file name - chunk file path pairs. */
	private TreeMap<String, TreeMap<String, String> > chunkPathList;
	/** A mapping between binary file names and chunk file name - chunk ID pairs. */
	private TreeMap<String, TreeMap<String, Integer> > chunkIDList;

	public String getInputLocation() {
		return location;
	}

	public int getNumOfFiles() {
		return numOfFiles;
	}

	public TreeMap<String, HashMap<String, String> > getMetaInfo() {
		return metaInfo;
	}

	public TreeMap<String, String> getFilePathList() {
		return filePathList;
	}

	public String[] getFileNameArray() {
		return fileNameArray;
	}

	/** Get the Xth file name from the file name array.
	 * @param  x  the index of the file name to return
	 * @return  String  the Xth file name in the array.
	 * */
	public String getXthFileName(int x) {
		return getFileNameArray()[x-1];
	}

	public TreeMap<String, TreeMap<String, String> > getChunkPathList() {
		return chunkPathList;
	}

	public TreeMap<String, TreeMap<String, Integer> > getChunkIDList() {
		return chunkIDList;
	}

	public String getAbsolutePathFor(String fileName) {
		return filePathList.get(fileName);
	}

	public String getAbsolutePathForChunk(String fileName, String chunkName) {
		return chunkPathList.get(fileName).get(chunkName);
	}

	public Integer getIDForChunk(String fileName, String chunkName) {
		return chunkIDList.get(fileName).get(chunkName);
	}

	public String getPathForStreamerInfo(String fileName) {
		return filePathList.get(fileName) + ".chunks/STREAMER_INFO.bin";
	}

	/**
	 * The standard method for accessing the singleton. 
	 * 
	 * @param  location  the full path to the generated directory by PaGeS
	 * @return  BinaryFileInfo  the only instance of this class
	 * */
	public static BinaryFileInfo getInstance(String location) {
		BinaryFileInfo binInfo = instance.get(location);
		if (binInfo == null) {
			binInfo = new BinaryFileInfo(location);
			instance.put(location, binInfo);
		}
		return binInfo;
	}

	/** The constructor of the class that reads every information about the test data set.
	 * 
	 * @param  loc  the full path to the PaGeS generated directory
	 * */
	protected BinaryFileInfo(String loc) {
		location = loc;
		numOfFiles = 0;
		filePathList = new TreeMap<String, String>();
		chunkPathList = new TreeMap<String, TreeMap<String, String> >();
		chunkIDList = new TreeMap<String, TreeMap<String, Integer> >();
		metaInfo = new TreeMap<String, HashMap<String, String> >();

		File[] locFolder = new File(location).listFiles();
		for (File sub : locFolder) {
			if (sub.isFile()) {
				numOfFiles++;
				// Store the path in filePathList
				String binaryName = sub.getName();
				filePathList.put(sub.getName(), sub.getAbsolutePath());
				//There must be a sub-dir for the file with .chunks suffix:
				File dir = new File(sub.getAbsolutePath() + ".chunks");
				if (dir.isDirectory()) {
					try {
						readMetaForFile(binaryName, dir);
						readChunkPathList(binaryName, dir);
					} catch (Exception ex) {
						System.out.println(ex.toString());
					}
				}
			} else {
				// IGNORE ANYTHING ELSE, dirs already processed.
			}
		}
		fileNameArray = filePathList.keySet().toArray(new String[0]);
		System.out.println("New BinaryFileInfo Object created with the following parameters: \n"
				+ " -> Location of files: " + loc + "\n"
				+ " -> Number of files found: " + numOfFiles + "\n"
				+ " -> Object is: " + this.toString() + "\n"
		);
	}

	/**
	 * This utility function processes the chunk directory of a binary file name.
	 * 
	 * @param  binaryName  the binary file name
	 * @param  dir  the chunk directory of the first parameter, binary file name
	 * */
	private void readMetaForFile(String binaryName, File dir)
			throws FileNotFoundException, IOException {
		File metaFile = new File(dir.getAbsolutePath() + "/META-" + binaryName + ".info");
		BufferedReader in = new BufferedReader(new FileReader(metaFile));
		String line = "";
		HashMap<String, String> metaMapForBinary = new HashMap<String, String>();
		while ((line = in.readLine()) != null) {
			String cols[] = line.split(" ");
			metaMapForBinary.put(cols[0], cols[1]);
		}
		metaMapForBinary.put("id", binaryName);
		metaInfo.put(binaryName, metaMapForBinary);
		in.close();
	}

	/** In a given folder, this function filters out the binary chunk files. This is
	 * called only for the chunk folders, so the pattern matches only for the files
	 * that have the ".bin" extension, and their name don't start with "STREAMER_INFO".
	 * 
	 * @param  folder  the folder to filter
	 * @return  File[]  the array of chunk files in the folder parameter
	 * */
	private File[] getChunkFiles(File folder) {
		File[] chunkFiles = folder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".bin") && !name.startsWith("STREAMER_INFO");
			}
		});
		return chunkFiles;
	}

	/**
	 * This method stores the path of every chunk file that were found with the getChunkFiles
	 * utility function.
	 * 
	 * @param  binaryName  which binary file is that we process the chunks for
	 * @param  dir  the directory of the chunks
	 * */
	private void readChunkPathList(String binaryName, File dir) {
		TreeMap<String, String> namelist = new TreeMap<String, String>();
		TreeMap<String, Integer> idlist = new TreeMap<String, Integer>();
		File[] chunks = getChunkFiles(dir);
		for (int i = 0; i < chunks.length; ++i) {
			String chunkName = chunks[i].getName();
			namelist.put(chunkName, chunks[i].getAbsolutePath());
			idlist.put(chunkName, Integer.parseInt(chunkName.replaceAll("[\\D]", "")));
		}
		chunkPathList.put(binaryName, namelist);
		chunkIDList.put(binaryName, idlist);
	}

	/**
	 * This function reads every chunk of a given file, into a list of ByteArrayOutputStreams.
	 * 
	 * @param  binaryName  the binary file name to get in chunks
	 * @return  List{@literal}<}ByteArrayOutputStream{@literal}>}  the list of chunks
	 * */
	/*public List<ByteArrayOutputStream> readChunksFor(String binaryName) {
		TreeMap<String, String> cPathList = chunkPathList.get(binaryName);
		int size = cPathList.entrySet().size();

		List<ByteArrayOutputStream> res = new ArrayList<ByteArrayOutputStream>(size);
		for (int i = 0; i < size; ++i) {
			res.add(i, null);
		}
		for (Map.Entry<String, String> it : cPathList.entrySet()) {
			int id = Integer.parseInt(it.getKey().replaceAll("[\\D]", ""));
			res.set(id-1, Readers.BinaryReader.read(it.getValue()));
		}
		return res;
	}*/

}
