package com.test.bootanimation;

import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.mediatek.storage.StorageManagerEx;

public final class MountPointManager {
    private static final String TAG = "MountPointManager";

    public static final String SEPARATOR = "/";
    public static final String HOME = "Home";
    public static final String ROOT_PATH = "/storage";

    private String mRootPath = "Root Path";
    private static MountPointManager sInstance = new MountPointManager();

    private StorageManager mStorageManager = null;
    private final CopyOnWriteArrayList <MountPoint> mMountPathList = new CopyOnWriteArrayList<MountPoint>();

    private MountPointManager() {
    }

    /**
     * This method initializes MountPointManager.
     *
     * @param context Context to use
     */
    public void init(Context context) {
        mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        final String defaultPath = getDefaultPath();
        Log.d(TAG, "init,defaultPath = " + defaultPath);
        if (!TextUtils.isEmpty(defaultPath)) {
            mRootPath = ROOT_PATH;
        }
        mMountPathList.clear();
        // check media availability to init mMountPathList
        StorageVolume[] storageVolumeList = mStorageManager.getVolumeList();
        if (storageVolumeList != null) {
            for (StorageVolume volume : storageVolumeList) {
                MountPoint mountPoint = new MountPoint();
                mountPoint.mDescription = volume.getDescription(context);
                mountPoint.mPath = volume.getPath();
                mountPoint.mIsMounted = isMounted(volume.getPath());
                mountPoint.mIsExternal = volume.isRemovable();
                mountPoint.mMaxFileSize = volume.getMaxFileSize();
                Log.d(TAG, "init,description :" + mountPoint.mDescription + ",path : "
                        + mountPoint.mPath + ",isMounted : " + mountPoint.mIsMounted
                        + ",isExternal : " + mountPoint.mIsExternal + ", mMaxFileSize: " + mountPoint.mMaxFileSize);
                mMountPathList.add(mountPoint);
            }
        }
    }

    /**
     * This method gets instance of MountPointManager. Before calling this method, must call init().
     *
     * @return instance of MountPointManager
     */
    public static MountPointManager getInstance() {
        return sInstance;
    }

    private static class MountPoint {
        String mDescription;
        String mPath;
        boolean mIsExternal;
        boolean mIsMounted;
        long mMaxFileSize;
        long mFreeSpace;
        long mTotalSpace;
    }

    /**
     * This method checks weather certain path is root path.
     *
     * @param path certain path to be checked
     * @return true for root path, and false for not root path
     */
    public boolean isRootPath(String path) {
        return mRootPath.equals(path);
    }

    /**
     * This method gets root path
     *
     * @return root path
     */
    public String getRootPath() {
        return mRootPath;
    }

    /**
     * This method gets informations of file of mount point path
     *
     * @return fileInfos of mount point path
     */
    public List<File> getMountPointFile() {
        List<File> files = new ArrayList<File>(0);
        for (MountPoint mp : mMountPathList) {
            if (mp.mIsMounted) {
                files.add(new File(mp.mPath));
            }
        }
        return files;
    }

    /**
     * This method gets count of mount, number of mount point(s)
     *
     * @return number of mount point(s)
     */
    public int getMountCount() {
        int count = 0;
        for (MountPoint mPoint : mMountPathList) {
            if (mPoint.mIsMounted) {
                count++;
            }
        }
        Log.d(TAG, "getMountCount,count = " + count);
        return count;
    }

    /**
     * This method gets default path from StorageManager
     *
     * @return default path from StorageManager
     */
    public String getDefaultPath() {
        Log.d(TAG, "getDefaultPath:" + StorageManagerEx.getDefaultPath());
        return StorageManagerEx.getDefaultPath();

    }

    /**
     * This method checks whether SDcard is mounted or not
     *
     * @param mountPoint the mount point that should be checked
     * @return true if SDcard is mounted, false otherwise
     */
    protected boolean isMounted(String mountPoint) {
        Log.d(TAG, "isMounted, mountPoint = " + mountPoint);
        if (TextUtils.isEmpty(mountPoint)) {
            return false;
        }
        String state = null;

        state = mStorageManager.getVolumeState(mountPoint);
        Log.d(TAG, "state = " + state);
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * This method checks whether SDcard is mounted or not
     *
     * @param path the path that should be checked
     * @return true if SDcard is mounted, false otherwise
     */
    protected boolean isRootPathMount(String path) {
        Log.d(TAG, "isRootPathMount,  path = " +  path);
        boolean ret = false;
        if (path == null) {
            return ret;
        }
        ret = isMounted(getRealMountPointPath(path));
        Log.d(TAG, "isRootPathMount,  ret = " + ret);
        return ret;
    }

    /**
     * This method gets real mount point path for certain path.
     *
     * @param path certain path to be checked
     * @return real mount point path for certain path, "" for path is not mounted
     */
    public String getRealMountPointPath(String path) {
        Log.d(TAG, "getRealMountPointPath ,path =" + path);
        for (MountPoint mountPoint : mMountPathList) {
            if ((path + SEPARATOR).startsWith(mountPoint.mPath + SEPARATOR)) {
                Log.d(TAG, "getRealMountPointPath = " + mountPoint.mPath);
                return mountPoint.mPath;
            }
        }
        Log.d(TAG, "getRealMountPointPath = \"\" ");
        return "";
    }

    /**
     * This method checks weather certain path is a FAT32 disk.
     *
     * @param path certain path to be checked
     * @return true for FAT32, and false for not.
     */
    public boolean isFat32Disk(String path) {
        Log.d(TAG, "isFat32Disk ,path =" + path);
        for (MountPoint mountPoint : mMountPathList) {
            if ((path + SEPARATOR).startsWith(mountPoint.mPath + SEPARATOR)) {
                Log.d(TAG, "isFat32Disk = " + mountPoint.mPath);
                if (mountPoint.mMaxFileSize > 0) {
                     Log.d(TAG, "isFat32Disk = true.");
                    return true;
                }
                Log.d(TAG, "isFat32Disk = false.");
                return false;
            }
        }

        Log.d(TAG, "isFat32Disk = false.");
        return false;
    }

    /**
     * This method changes mount state of mount point, if parameter path is mount point.
     *
     * @param path certain path to be checked
     * @param isMounted flag to mark weather certain mount point is under mounted state
     * @return true for change success, and false for fail
     */
    public boolean changeMountState(String path, Boolean isMounted) {
        boolean ret = false;
        for (MountPoint mountPoint : mMountPathList) {
            if (mountPoint.mPath.equals(path)) {
                if (mountPoint.mIsMounted == isMounted) {
                    break;
                } else {
                    mountPoint.mIsMounted = isMounted;
                    ret = true;
                    break;
                }
            }
        }
        Log.d(TAG, "changeMountState ,path =" + path + ",ret = " + ret);

        return ret;
    }

    /**
     * This method checks weather certain path is mount point.
     *
     * @param path certain path, which needs to be checked
     * @return true for mount point, and false for not mount piont
     */
    public boolean isMountPoint(String path) {
        boolean ret = false;
        Log.d(TAG, "isMountPoint ,path =" + path);
        if (path == null) {
            return ret;
        }
        for (MountPoint mountPoint : mMountPathList) {
            if (path.equals(mountPoint.mPath)) {
                ret = true;
                break;
            }
        }
        Log.d(TAG, "isMountPoint ,ret =" + ret);
        return ret;
    }

    /**
     * This method checks weather certain path is internal mount path.
     *
     * @param path path which needs to be checked
     * @return true for internal mount path, and false for not internal mount path
     */
    public boolean isInternalMountPath(String path) {
        Log.d(TAG, "isInternalMountPath ,path =" + path);
        if (path == null) {
            return false;
        }
        for (MountPoint mountPoint : mMountPathList) {
            if (!mountPoint.mIsExternal && mountPoint.mPath.equals(path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method checks weather certain path is external mount path.
     *
     * @param path path which needs to be checked
     * @return true for external mount path, and false for not external mount path
     */
    public boolean isExternalMountPath(String path) {
        Log.d(TAG, "isExternalMountPath ,path =" + path);
        if (path == null) {
            return false;
        }
        for (MountPoint mountPoint : mMountPathList) {
            if (mountPoint.mIsExternal && mountPoint.mPath.equals(path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method checks weather certain file is External File.
     *
     * @param fileInfo certain file needs to be checked
     * @return true for external file, and false for not external file
     */
    public boolean isExternalFile(File file) {
        boolean ret = false;
        if (file != null) {
            String mountPath = getRealMountPointPath(file.getAbsolutePath());
            if (mountPath.equals(file.getAbsolutePath())) {
                Log.d(TAG, "isExternalFile,return false .mountPath = " + mountPath);
                ret = false;
            }
            if (isExternalMountPath(mountPath)) {
                ret = true;
            }
        }

        Log.d(TAG, "isExternalFile,ret = " + ret);

        return ret;
    }

    /**
     * This method gets description of certain path
     *
     * @param path certain path
     * @return description of the path
     */
    public String getDescriptionPath(String path) {
        Log.d(TAG, "getDescriptionPath ,path =" + path);
        if (mMountPathList != null) {
            for (MountPoint mountPoint : mMountPathList) {
                if ((path + SEPARATOR).startsWith(mountPoint.mPath + SEPARATOR)) {
                    return path.length() > mountPoint.mPath.length() + 1 ? mountPoint.mDescription
                            + SEPARATOR + path.substring(mountPoint.mPath.length() + 1)
                            : mountPoint.mDescription;
                }
            }
        }
        return path;
    }

    /**
     * This method judge whether one path indicates primary volume.
     *
     * @param path certain path
     * @return true for primary path, false for other path
     */
    public boolean isPrimaryVolume(String path) {
        Log.d(TAG, "isPrimaryVolume ,path =" + path);
        if (mMountPathList.size() > 0) {
            return mMountPathList.get(0).mPath.equals(path);
        } else {
            Log.w(TAG, "mMountPathList null!");
            return false;
        }
    }

    /**
     * This method update mount point space infomation(free space & total space)
     *
     */
    public void updateMountPointSpaceInfo() {
        Log.d(TAG, "updateMountPointSpaceInfo...");
        for (MountPoint mp : mMountPathList) {
            if (mp.mIsMounted) {
                File file = new File(mp.mPath);
                mp.mFreeSpace = file.getUsableSpace();
                mp.mTotalSpace = file.getTotalSpace();
            }
        }
    }

    /**
     * This method gets free space of some path, if this path indicates mount point.
     *
     * @param path certain path
     * @return free space of volume
     */
    public long getMountPointFreeSpace(String path) {
        Log.d(TAG, "getMountPointFreeSpace " + path);
        long freeSpace = 0;
        for (MountPoint mp : mMountPathList) {
            if (mp.mPath.equalsIgnoreCase(path)) {
                freeSpace = mp.mFreeSpace;
            }
        }
        return freeSpace;
    }

    /**
     * This method gets total space of some path, if this path indicates mount point.
     *
     * @param path certain path
     * @return total space of volume
     */
    public long getMountPointTotalSpace(String path) {
       Log.d(TAG, "getMountPointTotalSpace " + path);
        long totalSpace = 0;
        for (MountPoint mp : mMountPathList) {
            if (mp.mPath.equalsIgnoreCase(path)) {
                totalSpace = mp.mTotalSpace;
                if (totalSpace == 0) {
                    mp.mFreeSpace = new File(mp.mPath).getUsableSpace();
                    mp.mTotalSpace = new File(mp.mPath).getTotalSpace();
                }
            }
        }
        return totalSpace;
    }
        
    public boolean isExistExternalStorage() {
		boolean result = false;
		if (mMountPathList != null && mMountPathList.size() > 0) {
			for (MountPoint mp : mMountPathList) {
				if (mp.mIsExternal && mp.mIsMounted) {
					result = true;
					break;
				}
			}
		}
		
		return result;
	}
	
	public File getExternalStorageFile() {
		File file = null;
		if (mMountPathList != null && mMountPathList.size() > 0) {
			for (MountPoint mp : mMountPathList) {
				if (mp.mIsExternal && mp.mIsMounted) {
					file = new File(mp.mPath);
					break;
				}
			}
		}
		
		return file;
	}
}
