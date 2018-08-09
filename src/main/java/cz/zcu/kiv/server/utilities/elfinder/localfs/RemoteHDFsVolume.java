package cz.zcu.kiv.server.utilities.elfinder.localfs;

import cz.zcu.kiv.server.utilities.elfinder.service.FsItem;
import cz.zcu.kiv.server.utilities.elfinder.service.FsVolume;
import cz.zcu.kiv.server.utilities.elfinder.util.MimeTypesUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;

import java.io.*;

import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.RemoteIterator;


public class RemoteHDFsVolume implements FsVolume
{
	Log logger = LogFactory.getLog(RemoteHDFsVolume.class);

    public static final String HDFS_URI = "hdfs://192.168.139.128:8020/";

    public static Configuration HDFS_CONF = new Configuration();

    //Username of hadoop linux user with permission to write to HDFS
    public static final String HADOOP_USER_NAME = "hdfs";

    public static final String HADOOP_USER_NAME_KEY = "HADOOP_USER_NAME";
    FileSystem fs  ;

    public RemoteHDFsVolume() {
        try {
            System.setProperty(HADOOP_USER_NAME_KEY,HADOOP_USER_NAME);
            fs= FileSystem.get(URI.create(HDFS_URI), HDFS_CONF);
        } catch (IOException e) {
            logger.error(e);
        }

    }
	/**
	 * Used to calculate total file size when walking the tree.
	 */
	private static class FileSizeFileVisitor extends SimpleFileVisitor<Path>
	{

		private long totalSize;

		public long getTotalSize()
		{
			return totalSize;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
				throws IOException
		{
			totalSize += file.toFile().length();
			return FileVisitResult.CONTINUE;
		}

	}

	String name;

	File rootDir;

	private File asFile(FsItem fsi)
	{
		return ((RemoteHDFsItem) fsi).getFile();
	}

	@Override
	public void createFile(FsItem fsi) throws IOException
	{
		throw new IOException("Unsupported Operation");
	}

	@Override
	public void createFolder(FsItem fsi) throws IOException
	{
        throw new IOException("Unsupported Operation");
	}

	@Override
	public void deleteFile(FsItem fsi) throws IOException
	{
        throw new IOException("Unsupported Operation");
	}

	@Override
	public void deleteFolder(FsItem fsi) throws IOException
	{
        throw new IOException("Unsupported Operation");
	}

	@Override
	public boolean exists(FsItem newFile)
	{
		return asFile(newFile).exists();
	}

	private RemoteHDFsItem fromFile(File file)
	{
		if (!file.getAbsolutePath().startsWith(rootDir.getAbsolutePath()))
		{
			String message = String.format(
					"Item (%s) can't be outside the root directory (%s)",
					file.getAbsolutePath(), rootDir.getAbsolutePath());
			throw new IllegalArgumentException(message);
		}
		return new RemoteHDFsItem(this, file);
	}

	@Override
	public FsItem fromPath(String relativePath)
	{
		return fromFile(new File(rootDir, relativePath));
	}

	@Override
	public String getDimensions(FsItem fsi)
	{
		return null;
	}

	@Override
	public long getLastModified(FsItem fsi)
	{
		return asFile(fsi).lastModified() / 1000;
	}

	@Override
	public String getMimeType(FsItem fsi)
	{

		if (isFolder(fsi))
			return "directory";
		File file = asFile(fsi);

		String ext = FilenameUtils.getExtension(file.getName());
		if (ext != null && !ext.isEmpty())
		{
			String mimeType = MimeTypesUtils.getMimeType(ext);
			return mimeType == null ? MimeTypesUtils.UNKNOWN_MIME_TYPE
					: mimeType;
		}

		return MimeTypesUtils.UNKNOWN_MIME_TYPE;
	}

	public String getName()
	{
		return name;
	}

	@Override
	public String getName(FsItem fsi)
	{
		return asFile(fsi).getName();
	}

	@Override
	public FsItem getParent(FsItem fsi)
	{
		return fromFile(asFile(fsi).getParentFile());
	}

	@Override
	public String getPath(FsItem fsi) throws IOException
	{
		String fullPath = asFile(fsi).getCanonicalPath();
		String rootPath = rootDir.getCanonicalPath();
		String relativePath = fullPath.substring(rootPath.length());
		return relativePath.replace('\\', '/');
	}

	@Override
	public FsItem getRoot()
	{
		return fromFile(rootDir);
	}

	public File getRootDir()
	{
		return rootDir;
	}

	@Override
	public long getSize(FsItem fsi) throws IOException
	{
		if (isFolder(fsi))
		{
			return 0;
		}
		else
		{
			return asFile(fsi).length();
		}
	}

	@Override
	public String getThumbnailFileName(FsItem fsi)
	{
		return null;
	}

	@Override
	public String getURL(FsItem f)
	{
		// We are just happy to not supply a custom URL.
		return null;
	}

	@Override
	public void filterOptions(FsItem f, Map<String, Object> map)
	{
		// Don't do anything
	}

	@Override
	public boolean hasChildFolder(FsItem fsi)
	{
		return asFile(fsi).isDirectory()
				&& asFile(fsi).listFiles(new FileFilter()
				{

					@Override
					public boolean accept(File arg0)
					{
						return arg0.isDirectory();
					}
				}).length > 0;
	}

	@Override
	public boolean isFolder(FsItem fsi)
	{
		boolean isFile =asFile(fsi).getName().contains(".");
		return !isFile;
	}

	@Override
	public boolean isRoot(FsItem fsi)
	{
		return rootDir.equals(asFile(fsi));
	}

	@Override
	public FsItem[] listChildren(FsItem fsi)  {
		List<FsItem> list = new ArrayList<FsItem>();

        try {

            RemoteIterator<LocatedFileStatus> fileStatusListIterator = fs.listLocatedStatus(
                    new org.apache.hadoop.fs.Path(HDFS_URI+getRelativePath(asFile(fsi).getAbsolutePath())));
            while(fileStatusListIterator.hasNext()){
                LocatedFileStatus fileStatus = fileStatusListIterator.next();
                //do stuff with the file like ...
                String path=fileStatus.getPath().toString();
                path=path.replaceFirst(HDFS_URI,"/");
                File file=new File(getRootDir().getAbsolutePath()+path);
                list.add(fromFile(file));
            }
        }
        catch (IOException e){
		    logger.error(e);
        }
        return list.toArray(new FsItem[0]);
	}

	@Override
	public InputStream openInputStream(FsItem fsi) throws IOException
	{
		return new FileInputStream(asFile(fsi));
	}

	@Override
	public void rename(FsItem src, FsItem dst) throws IOException
	{
        throw new IOException("Unsupported Operation");
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setRootDir(File rootDir)
	{
		if (!rootDir.exists())
		{
			rootDir.mkdirs();
		}

		this.rootDir = rootDir;
	}

	@Override
	public String toString()
	{
		return "RemoteHDFsVolume [" + rootDir + "]";
	}

	@Override
	public void writeStream(FsItem fsi, InputStream is) throws IOException
	{
        throw new IOException("Unsupported Operation");
	}

	public String getRelativePath(String absolutePath){
	    String relativePath= absolutePath.substring(absolutePath.indexOf("/HDFS")+5);
	    return relativePath;
    }
}
