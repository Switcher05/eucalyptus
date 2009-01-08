/*
 * Software License Agreement (BSD License)
 *
 * Copyright (c) 2008, Regents of the University of California
 * All rights reserved.
 *
 * Redistribution and use of this software in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met:
 *
 * * Redistributions of source code must retain the above
 *   copyright notice, this list of conditions and the
 *   following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the
 *   following disclaimer in the documentation and/or other
 *   materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * Author: Sunil Soman sunils@cs.ucsb.edu
 */

package edu.ucsb.eucalyptus.storage.fs;

import edu.ucsb.eucalyptus.storage.StorageManager;
import edu.ucsb.eucalyptus.util.WalrusProperties;

import java.io.*;

public class FileSystemStorageManager implements StorageManager {

    public static final String FILE_SEPARATOR = "/";
    private String rootDirectory;
    public FileSystemStorageManager(String rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    public void setRootDirectory(String rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    public void createBucket(String bucket) throws IOException {
        File bukkit = new File (rootDirectory + FILE_SEPARATOR + bucket);
        if(!bukkit.exists()) {
            if(!bukkit.mkdirs()) {
                throw new IOException(bucket);
            }
        }
    }


    public boolean isEmpty(String bucket) {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void deleteBucket(String bucket) throws IOException {
        File bukkit = new File (rootDirectory + FILE_SEPARATOR + bucket);
        if(!bukkit.delete()) {
            throw new IOException(bucket);
        }
    }

    public void createObject(String bucket, String object) throws IOException {
        File objectFile = new File (rootDirectory + FILE_SEPARATOR + bucket + FILE_SEPARATOR + object);
        if (!objectFile.exists()) {
            if (!objectFile.createNewFile()) {
                throw new IOException(object);
            }
        }
    }

    public int readObject(String bucket, String object, byte[] bytes, long offset) throws IOException {
        return readObject(rootDirectory + FILE_SEPARATOR + bucket + FILE_SEPARATOR + object, bytes, offset);
    }

    public int readObject(String path, byte[] bytes, long offset) throws IOException {
        File objectFile = new File (path);
        if (!objectFile.exists()) {
            throw new IOException(path);
        }
        BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(objectFile));
        if (offset > 0) {
            inputStream.skip(offset);
        }
        int bytesRead = inputStream.read(bytes);
        inputStream.close();
        return bytesRead;
    }

    public void deleteObject(String bucket, String object) throws IOException {
        File objectFile = new File (rootDirectory + FILE_SEPARATOR + bucket + FILE_SEPARATOR + object);
        if (objectFile.exists()) {
            if(!objectFile.delete()) {
                throw new IOException(object);
            }
        }
    }

    public void deleteAbsoluteObject(String object) throws IOException {
        File objectFile = new File (object);
        if (objectFile.exists()) {
            if(!objectFile.delete()) {
                throw new IOException(object);
            }
        }
    }

    public void putObject(String bucket, String object, byte[] base64Data, boolean append) throws IOException {
        File objectFile = new File (rootDirectory + FILE_SEPARATOR + bucket + FILE_SEPARATOR + object);
        if (!objectFile.exists()) {
            objectFile.createNewFile();
        }
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(objectFile, append));
        outputStream.write(base64Data);
        outputStream.close();
    }

    public void renameObject(String bucket, String oldName, String newName) throws IOException {
        File oldObjectFile = new File (rootDirectory + FILE_SEPARATOR + bucket + FILE_SEPARATOR + oldName);
        File newObjectFile = new File (rootDirectory + FILE_SEPARATOR + bucket + FILE_SEPARATOR + newName);
        if (!oldObjectFile.renameTo(newObjectFile)) {
            throw new IOException(oldName);
        }
    }

    public String getObjectPath(String bucket, String object) {
        return rootDirectory + FILE_SEPARATOR + bucket + FILE_SEPARATOR + object;
    }
}