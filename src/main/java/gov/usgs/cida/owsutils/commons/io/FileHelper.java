package gov.usgs.cida.owsutils.commons.io;

import gov.usgs.cida.owsutils.commons.io.exception.ShapefileFormatException;
import gov.usgs.cida.owsutils.commons.shapefile.utils.IterableShapefileReader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.LoggerFactory;

/**
 * Utility class that helps with FileIO operations
 *
 * @author isuftin
 *
 */
public class FileHelper extends FileUtils {

	private static org.slf4j.Logger log = LoggerFactory.getLogger(FileHelper.class);
	private static final String SUFFIX_SHP = ".shp";
	private static final String SUFFIX_SHX = ".shx";
	private static final String SUFFIX_PRJ = ".prj";
	private static final String SUFFIX_DBF = ".dbf";

	/**
	 * @see FileHelper#base64Encode(byte[])
	 * @param input
	 * @return
	 * @throws IOException
	 */
	public static byte[] base64Encode(final File input) throws IOException {
		return FileHelper.base64Encode(FileHelper.getByteArrayFromFile(input));
	}

	/**
	 * Provides Base64 encoding and decoding as defined by <a
	 * href="http://tools.ietf.org/html/rfc2045">RFC 2045</a>.
	 *
	 * @param input
	 * @return Byte array representing the base 64 encoding of the incoming File
	 * or byte array
	 */
	public static byte[] base64Encode(final byte[] input) {
		if (input == null) {
			return (byte[]) Array.newInstance(byte.class, 0);
		}

		log.trace(new StringBuilder("Attempting to base64 encode a byte array of ").append(input.length).append(" bytes.").toString());

		byte[] result;

		Base64 encoder = new Base64();

		result = encoder.encode(input);

		return result;
	}

	/**
	 * Reads a file into a byte array
	 *
	 * @param file
	 * @return a byte array representation of the incoming file
	 * @throws IOException
	 */
	public static byte[] getByteArrayFromFile(File file) throws IOException {
		if (file == null) {
			return (byte[]) Array.newInstance(byte.class, 0);
		}

		log.debug(new StringBuilder("Attempting to get a byte array from file: ").append(file.getPath()).toString());

		// Get the size of the file
		long length = file.length();

		// Maximum size of file cannot be larger than the Integer.MAX_VALUE
		if (length > Integer.MAX_VALUE) {
			throw new IOException("File is too large: File length: " + file.length() + " bytes. Maximum length: " + Integer.MAX_VALUE + " bytes.");
		}

		// Create the byte array to hold the data
		byte[] bytes = new byte[(int) length];

		// Read in the bytes
		int offset = 0, numRead = 0;

		InputStream is = null;
		try {
			is = new FileInputStream(file);
			while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
				offset += numRead;
			}
		} finally {
			if (is != null) {
				is.close();
			}
		}

		// Ensure all the bytes have been read in
		if (offset < bytes.length) {
			throw new IOException("Could not completely read file " + file.getName());
		}
		log.debug(new StringBuilder("Successfully attained a byte array from file: ").append(file.getPath()).toString());
		return bytes;
	}

	/**
	 * Performs a safe renaming of a file. First copies old file to new file,
	 * then if new file exists, removes old file.
	 *
	 * @param fromFile
	 * @param toFileName
	 * @return true if succeeded, false if not
	 * @throws IOException
	 */
	public static boolean renameFile(final File fromFile, final String toFileName) throws IOException {
		File toFile = new File(fromFile.getParent() + File.separator + toFileName);

		copyFile(fromFile, toFile);

		if (!toFile.exists()) {
			return false;
		}

		return fromFile.delete();
	}

	/**
	 * Performs a filecopy without deleting the original file
	 *
	 * @see FileHelper#copyFileToPath(java.io.File, java.lang.String, boolean)
	 * @param inFile
	 * @param outFilePath
	 * @return
	 * @throws IOException
	 */
	public static boolean copyFileToPath(final File inFile, final String outFilePath) throws IOException {
		return FileHelper.copyFileToPath(inFile, outFilePath, false);
	}

	/**
	 * Copies a File object (directory or file) to a given location Is able to
	 * handle
	 *
	 * @param inFile File to be copied
	 * @param outPath Destination where to copy to
	 * @param deleteOriginalFile - effectively makes this function as a MOVE
	 * command instead of a COPY command
	 * @return true if file properly copied, otherwise false
	 * @throws IOException
	 */
	public static boolean copyFileToPath(final File inFile, final String outPath, boolean deleteOriginalFile) throws IOException {
		if (inFile.isDirectory()) {
			copyDirectory(inFile, (new File(outPath + File.separator + inFile.getName())));
		} else {
			copyFile(inFile, (new File(outPath + File.separator + inFile.getName())));
		}

		if (deleteOriginalFile) {
			deleteQuietly(inFile);
		}

		return true;
	}

	public static void copyInputStreamToFile(InputStream is, File destinationFile) throws IOException {
		try (FileOutputStream os = new FileOutputStream(destinationFile)) {
			IOUtils.copyLarge(is, os);
		}
	}

	/**
	 * Delete files older than a given Long instance
	 *
	 * @param directory Directory within which to search.
	 * @param cutoffTime
	 * @param deleteDirectory Also delete the directory given in the directory
	 * param
	 * @return files that were deleted
	 */
	public static Collection<File> wipeOldFiles(File directory, Long cutoffTime, boolean deleteDirectory) {
		if (directory == null || !directory.exists()) {
			return new ArrayList<File>();
		}

		Collection<File> result = new ArrayList<File>();
		Collection<File> oldFiles = FileHelper.getFilesOlderThan(directory, cutoffTime, Boolean.TRUE);
		for (File file : oldFiles) {
			String logString = "Deleting File: \"" + file.toString() + "\" ... ";

			if (file.canWrite() && file.delete()) {
				logString += "done. ";
				result.add(file);
				if (file.getParentFile().isDirectory()) {
					if (file.getParentFile() != directory && file.getParentFile().delete()) {
						log.info("Deleting Directory: \"" + file.getParent() + "\" ...  done");
					} else if (file.getParentFile() == directory && deleteDirectory) {
						log.info("Deleting Directory: \"" + file.getParent() + "\" ...  done");
					}
				}
			} else {
				logString += "FAILED!";
			}
			log.info(logString);
		}

		return result;
	}

	/**
	 * Creates a directory according to the passed in File object
	 *
	 * @see FileHelper#createDir(java.lang.String)
	 * @param directory
	 * @return true if directory has been created, false if not
	 */
	public static boolean createDir(File directory) {
		return FileHelper.createDir(directory.toString());
	}

	/**
	 * Creates a directory in the filesystem according to the passed in String
	 * object
	 *
	 * @param directory
	 * @param removeAtSysExit
	 * @return boolean true if already exists or created, false if directory
	 * could not be created
	 */
	public static boolean createDir(String directory) {
		boolean result;
		if (FileHelper.doesDirectoryOrFileExist(directory)) {
			return true;
		}
		result = new File(directory).mkdirs();
		return result;
	}

	/**
	 * Recursively deletes a directory from the filesystem.
	 *
	 * @param directory
	 * @return
	 */
	public static boolean deleteDirRecursively(File directory) throws IOException {
		if (!directory.exists()) {
			return false;
		}
		deleteDirectory(directory);
		return true;
	}

	/**
	 * Recursively deletes a directory from the filesystem.
	 *
	 * @param directory
	 * @return
	 */
	public static boolean deleteDirRecursively(String directory) throws IOException {
		boolean result;
		File dir = new File(directory);
		if (!dir.exists()) {
			return false;
		}
		result = FileHelper.deleteDirRecursively(dir);
		return result;
	}

	/**
	 * Deletes a file at the location of the passed in String object.
	 *
	 * @param filePath
	 * @return true if file has been deleted, false otherwise
	 */
	public static boolean deleteFileQuietly(String filePath) {
		return deleteQuietly(new File(filePath));
	}

	/**
	 * Deletes a file at the location of the passed in File object.
	 *
	 * @see FileHelper#deleteFileQuietly(java.lang.String)
	 * @param filePath
	 * @return true if file has been deleted, false otherwise
	 */
	public static boolean deleteFileQuietly(File file) {
		return deleteQuietly(file);
	}

	/**
	 * @see FileHelper.deleteFile
	 *
	 * @param filePath
	 * @return
	 * @throws SecurityException
	 */
	public static boolean deleteFile(String filePath) throws SecurityException {
		if ("".equals(filePath)) {
			return false;
		}
		return deleteFile(new File(filePath));
	}

	/**
	 * Deletes a file from the file system
	 *
	 * @param file - method returns false if File object passed in was null
	 * @return true if file was deleted, false if not
	 * @throws SecurityException
	 */
	public static boolean deleteFile(File file) throws SecurityException {
		if (file == null) {
			return false;
		}
		return file.delete();
	}

	/**
	 * Tests whether or not a directory or file exists given the passed String
	 * representing a file/directory location
	 *
	 * @param filePath
	 * @return
	 */
	public static boolean doesDirectoryOrFileExist(String filePath) {
		return new File(filePath).exists();
	}

	/**
	 * Attempts to find a file by recursively going through a given directory
	 *
	 * @param file The file that is being searched for
	 * @param rootPath The path to begin looking from
	 * @return the first file that was found
	 */
	public static File findFile(String file, String rootPath) {
		if (rootPath == null || "".equals(rootPath)) {
			return null;
		}
		File result = null;
		Collection<File> fileCollection = listFiles(new File(rootPath), new String[]{file.substring(file.lastIndexOf('.') + 1)}, true);
		if (fileCollection.isEmpty()) {
			return result;
		}
		Iterator<File> fileCollectionIterator = fileCollection.iterator();
		while (fileCollectionIterator.hasNext()) {
			File testFile = fileCollectionIterator.next();
			if (file.equals(testFile.getName())) {
				result = testFile;
			}
		}
		return result;
	}

	/**
	 * Get recursive directory listing
	 *
	 * @see FileHelper#getFileCollection(java.lang.String, java.lang.String[],
	 * boolean)
	 * @param filePath the path to begin looking through
	 * @param recursive whether or not the function should look only at base
	 * level or recursively
	 * @return a list of strings that represent the path to the files found
	 * @throws IllegalArgumentException
	 */
	public static List<String> getFileList(String filePath, boolean recursive) throws IllegalArgumentException {
		return FileHelper.getFileList(filePath, null, recursive);
	}

	/**
	 * Get recursive directory listing
	 *
	 * @param filePath the path to begin looking through
	 * @param extensions a list of extensions to match on
	 * @param recursive whether or not the function should look only at base
	 * level or recursively
	 * @return a list of strings that represent the path to the files found
	 * @throws IllegalArgumentException
	 */
	public static List<String> getFileList(String filePath, String[] extensions, boolean recursive) throws IllegalArgumentException {
		if (filePath == null) {
			return null;
		}
		List<String> result = new ArrayList<String>();
		Collection<File> fileList = listFiles((new File(filePath)), extensions, recursive);

		for (File file : fileList) {
			result.add(file.getName());
		}

		return result;
	}

	/**
	 * Returns a Collection of type File
	 *
	 * @see FileHelper#getFileCollection(java.lang.String, java.lang.String[],
	 * boolean)
	 * @param filePath the path to begin looking through
	 * @param recursive whether or not the function should look only at base
	 * level or recursively
	 * @return a collection of type File of files found at the directory point
	 * given
	 */
	public static Collection<File> getFileCollection(String filePath, boolean recursive) throws IllegalArgumentException {
		return (Collection<File>) getFileCollection(filePath, null, recursive);
	}

	/**
	 * Returns a Collection of type File
	 *
	 * @see FileHelper#getFileCollection(java.lang.String, java.lang.String[],
	 * boolean)
	 * @param filePath the path to begin looking through
	 * @param extensions a list of extensions to match on
	 * @param recursive whether or not the function should look only at base
	 * level or recursively
	 * @return a collection of type File of files found at the directory point
	 * given
	 */
	public static Collection<?> getFileCollection(String filePath, String[] extensions, boolean recursive) throws IllegalArgumentException {
		if (filePath == null) {
			return null;
		}

		Collection<File> result = null;
		Object interimResult = listFiles((new File(filePath)), extensions, recursive);
		if (interimResult instanceof Collection<?>) {
			result = (Collection<File>) interimResult;
		}
		return result;
	}

	/**
	 * Returns the temp directory specific to the operating system
	 *
	 * @see System.getProperty("java.io.tmpdir")
	 * @return
	 */
	public static String getSystemTemp() {
		return System.getProperty("java.io.tmpdir");
	}

	public static void flattenZipFile(String zipFileLocation) throws IOException {
		File zipFile = new File(zipFileLocation);
		if (!zipFile.exists()) {
			throw new IOException("File at location " + zipFileLocation + " does not exist");
		}

		if (zipFile.isDirectory()) {
			throw new IOException("File at location " + zipFileLocation + " is a directory. File needs to be a zip file");
		}

		if (!zipFile.canRead() || !zipFile.canWrite()) {
			throw new IOException("File at location " + zipFileLocation + " must be readable and writable");
		}

		File temporaryDirectory = new File(getTempDirectory(), UUID.randomUUID().toString() + "-deleteme");
		try {
			if (!temporaryDirectory.mkdirs()) {
				throw new IOException("Could not create temporary directory (" + temporaryDirectory.getCanonicalPath() + ") for processing");
			}

			unzipFile(temporaryDirectory.getPath(), zipFile);
			FileUtils.deleteQuietly(zipFile);
			zipFilesInDirectory(temporaryDirectory, zipFile);
		} finally {
			forceDelete(temporaryDirectory);
		}
	}

	public static void zipFilesInDirectory(File sourceDirectory, File file) throws IOException {
		if (!sourceDirectory.exists()) {
			throw new IOException("Directory at location " + sourceDirectory.getPath() + " does not exist");
		}

		if (!sourceDirectory.isDirectory()) {
			throw new IOException("Directory at location " + sourceDirectory.getPath() + " is not a directory.");
		}

		if (file.exists()) {
			throw new IOException("File at location " + file.getPath() + " already exists.");
		}

		FileOutputStream fos = new FileOutputStream(file);
		ZipOutputStream zos = new ZipOutputStream(fos);
		try {
			File[] fileList = sourceDirectory.listFiles();
			for (File fileItem : fileList) {
				ZipEntry zipEntry = new ZipEntry(fileItem.getName());
				zos.putNextEntry(zipEntry);
				FileInputStream fis = new FileInputStream(fileItem);
				IOUtils.copyLarge(fis, zos);
				zos.closeEntry();
				IOUtils.closeQuietly(fis);
			}
		} finally {
			IOUtils.closeQuietly(zos);
			IOUtils.closeQuietly(fos);
		}
	}

	/**
	 * Takes a zip file and unzips it to a outputDirectory
	 *
	 * @param outputDirectory
	 * @param zipFile
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static boolean unzipFile(String outputDirectory, File zipFile) throws FileNotFoundException, IOException {
		FileInputStream fis = new FileInputStream(zipFile);
		ZipInputStream zis = null;
		FileOutputStream fos = null;
		try {
			zis = new ZipInputStream(fis);
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				// Get the final filename (even if it's within directories in the ZIP file)
				if (!entry.isDirectory() && !entry.getName().startsWith(".") && !entry.getName().contains(File.separator + ".") && !entry.getName().toLowerCase(Locale.getDefault()).contains("macosx")) {
					String destinationFileName = entry.getName().contains(File.separator) ? entry.getName().substring(entry.getName().lastIndexOf(File.separator) + 1) : entry.getName();
					String destinationPath = outputDirectory + java.io.File.separator + destinationFileName;
					fos = new FileOutputStream(destinationPath);
					IOUtils.copyLarge(zis, fos);
					IOUtils.closeQuietly(fos);
				}
			}
		} finally {
			IOUtils.closeQuietly(zis);
			IOUtils.closeQuietly(fos);
		}
		return true;
	}

	/**
	 * Creates a unique user directory
	 *
	 * @param applicationUserSpaceDir User directory created
	 * @return
	 */
	public static String createUserDirectory(String applicationUserSpaceDir) {
		String userSubDir = Long.toString(new Date().getTime());

		//String applicationUserSpaceDir = System.getProperty("applicationUserSpaceDir");
		String seperator = File.separator;
		String userTempDir = applicationUserSpaceDir + seperator + userSubDir;
		if (FileHelper.createDir(userTempDir)) {
			log.debug("User subdirectory created at: " + userTempDir);
			return userSubDir;
		}
		log.warn(new StringBuilder("User subdirectory could not be created at: " + userSubDir).toString());
		log.debug("User will be unable to upload files for this session.");
		return "";
	}

	/**
	 * Updates the time stamp on a file or a list of files within a given
	 * directory
	 *
	 * @param path Path to file or directory
	 * @param recursive If path parameter is a directory and this param is true,
	 * will attempt to update the timestamp on all files within the directory to
	 * current time
	 * @return true if updating succeeded, false if not
	 * @throws IOException
	 */
	public static boolean updateTimestamp(final String path, final boolean recursive) throws IOException {
		if (path == null || "".equals(path)) {
			return false;
		}
		if (!FileHelper.doesDirectoryOrFileExist(path)) {
			return false;
		}

		if (recursive) {
			Iterator<File> files = iterateFiles(new File(path), null, true);
			while (files.hasNext()) {
				File file = files.next();
				touch(file); // update date on file
				log.debug(new StringBuilder("Updated timestamp on file: ").append(file.getPath()).toString());
			}
		} else {
			touch(new File(path));
			log.debug(new StringBuilder("Updated timestamp on file: ").append(new File(path).getPath()).toString());
		}
		return true;
	}

	/**
	 * Returns files and directories older that a specified date
	 *
	 * @param filePath System path to the directory
	 * @param age
	 * @param msPerDay
	 * @param recursive
	 * @return
	 */
	public static Collection<File> getFilesOlderThan(File filePath, Long age, Boolean recursive) {
		if (filePath == null || !filePath.exists()) {
			return new ArrayList<File>();
		}
		Iterator<File> files;

		if (recursive.booleanValue()) {
			files = iterateFiles(filePath, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		} else {
			files = iterateFiles(filePath, TrueFileFilter.INSTANCE, null);
		}

		Collection<File> result = new ArrayList<File>();
		Date date = new Date();
		while (files.hasNext()) {
			File file = files.next();

			if (file.lastModified() < date.getTime() - age.longValue()) {
				result.add(file);
				log.trace(new StringBuilder("Added ").append(file.getPath()).append(" to \"old files list\".").toString());
			}
		}

		return result;
	}

	public static Boolean validateShapeFile(final File shapeFile) {
		String shapefileName = shapeFile.getName();
		String shapefileNamePrefix = shapefileName.substring(0, shapefileName.lastIndexOf("."));
		File shapefileDir = shapeFile.getParentFile();

		// Find all files with filename with any extension
		String pattern = shapefileNamePrefix + "\\..*";
		FileFilter filter = new RegexFileFilter(pattern);

		String[] filenames = shapefileDir.list((FilenameFilter) filter);
		List<String> filenamesList = Arrays.asList(filenames);

		// Make sure required files are present
		String[] requiredFiles = {SUFFIX_SHP, SUFFIX_SHX, SUFFIX_PRJ, SUFFIX_DBF};
		for (String requiredFile : requiredFiles) {
			if (!filenamesList.contains(shapefileNamePrefix + requiredFile)) {
				return false;
			}
		}

		// Ensure we only have one shapefile inside this zip (extra project specific dbf files are allowed)
		int shpCount = listFiles(shapefileDir, (new String[]{"shp"}), false).size();
		int shxCount = listFiles(shapefileDir, (new String[]{"shx"}), false).size();
		int prjCount = listFiles(shapefileDir, (new String[]{"prj"}), false).size();

		if (shpCount + shxCount + prjCount > 3) {
			return false;
		}

		return true;
	}

	public static void validateShapefileZip(final File shapeZip) throws IOException, ShapefileFormatException {
		File temporaryDirectory = new File(getTempDirectory(), UUID.randomUUID().toString() + "-deleteme");
		try {
			if (!temporaryDirectory.mkdirs()) {
				throw new IOException("Could not create temporary directory (" + temporaryDirectory.getCanonicalPath() + ") for processing");
			}

			ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(new FileInputStream(shapeZip)));
			ZipEntry entry;
			while ((entry = zipInputStream.getNextEntry()) != null) {
				String currentExtension = entry.getName();
				// We want to skip past directories, hidden files and metadata files (MACOSX ZIPPING FIX)
				if (!entry.isDirectory()
						&& !currentExtension.startsWith(".")
						&& !currentExtension.contains(File.separator + ".")) {
					File currentFile = new File(temporaryDirectory, currentExtension);

					FileOutputStream fos = null;
					try {
						currentFile.createNewFile();
						fos = new FileOutputStream(currentFile);
						IOUtils.copy(zipInputStream, fos);
					} catch (IOException ioe) {
						// This usually occurs because this file is inside of another dir
						// so skip this file. Shapefiles inside with arbitrary directory 
						// depth should first be preprocessed to be single-depth since 
						// GS will not accept it otherwise
					} finally {
						IOUtils.closeQuietly(fos);
					}
				}
				System.gc();
			}
			IOUtils.closeQuietly(zipInputStream);

			File[] shapefiles = listFiles(temporaryDirectory, (new String[]{"shp"}), false).toArray(new File[0]);
			if (shapefiles.length == 0) {
				throw new ShapefileFormatException("Shapefile archive needs to contain at least one shapefile");
			} else if (shapefiles.length > 1) {
				throw new ShapefileFormatException("Shapefile archive may only contain one shapefile");
			} else if (!validateShapeFile(shapefiles[0])) {
				throw new ShapefileFormatException("Shapefile archive is not valid");
			}
			File[] prjfiles = FileHelper.listFiles(temporaryDirectory, (new String[]{"prj"}), false).toArray(new File[0]);
			if (prjfiles.length == 0 || prjfiles.length > 1) {
				throw new ShapefileFormatException("Shapefile archive needs to contain one prj file");
			}
		} finally {
			forceDelete(temporaryDirectory);
		}
	}

	public static File createTemporaryDirectory() throws IOException {
		File temporaryDirectory = new File(getTempDirectory(), UUID.randomUUID().toString() + "-deleteme");
		forceMkdir(temporaryDirectory);
		return temporaryDirectory;
	}

	public static IterableShapefileReader loadShapefileFromDirectoryIntoReader(File shapefileDirectory) throws IOException {
		IterableShapefileReader reader = null;
		Collection<File> shapefiles = FileUtils.listFiles(shapefileDirectory, new String[]{"shp"}, false);
		if (shapefiles.isEmpty()) {
			throw new IOException("No shapefiles at location");
		} else if (shapefiles.size() > 1) {
			throw new IOException("Multiple shapefiles at location");
		} else {
			reader = new IterableShapefileReader(shapefiles.iterator().next());
		}
		return reader;
	}
}
