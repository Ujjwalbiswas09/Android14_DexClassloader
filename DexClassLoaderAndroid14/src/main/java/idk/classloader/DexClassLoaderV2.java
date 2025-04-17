package idk.classloader;

import android.os.Build;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import dalvik.system.DexClassLoader;
import dalvik.system.InMemoryDexClassLoader;

public class DexClassLoaderV2 extends ClassLoader {
    private boolean needResourceLoader = false;
    private ZipFile workingZipFile;
    private static final String[] archieve_formats = {
            ".apk",
            ".jar",
            ".zip"
    };

     public DexClassLoaderV2(File file,String cache, ClassLoader parent){
         super(createClassLoader(file.getAbsolutePath(),cache,parent));
         if(Build.VERSION.SDK_INT >= 26 ) {
             if(file.getName().toLowerCase().endsWith(".dex")){
                 return;
             }
             needResourceLoader = isArchive(file.getName());
             try {
                 workingZipFile = new ZipFile(file);
             } catch (IOException e) {
                 throw new IllegalArgumentException("Unable to open file: " + file.getAbsolutePath());
             }
         }
     }
     private static boolean isArchive(String name){
         for (String format : archieve_formats) {
             if(name.toLowerCase().endsWith(format)){
                 return true;
             }
         }
         return false;
     }

     private static ClassLoader getArchieveClassLoader(String path,ClassLoader parent){
         try {
             ZipFile zipFile = new ZipFile(path);
             Enumeration<? extends ZipEntry> entries = zipFile.entries();
             List<ByteBuffer> byteBuffers = new ArrayList<>();
             while (entries.hasMoreElements()){
                 ZipEntry entry = entries.nextElement();
                 String name = entry.getName().toLowerCase();
                 if(!name.endsWith(".dex")){
                     continue;
                 }
                 try {
                     InputStream ins = zipFile.getInputStream(entry);
                     ByteArrayOutputStream bos = new ByteArrayOutputStream();
                     byte[] buffer = new byte[1024];
                     int len;
                     while ((len = ins.read(buffer)) != -1){
                         bos.write(buffer,0,len);
                     }
                     ins.close();
                     bos.flush();
                     byte[] bytes= bos.toByteArray();
                     ByteBuffer currentBuffer = ByteBuffer.allocate(bytes.length);
                     currentBuffer.position(0);
                     currentBuffer.put(buffer);
                     currentBuffer.position(0);
                     byteBuffers.add(currentBuffer);
                     bos.close();
                 }catch (Exception e){
                     e.printStackTrace();
                 }
             }
             zipFile.close();
             ByteBuffer[] array = new ByteBuffer[byteBuffers.size()];
             for (int i = 0; i < array.length; i++) {
                 array[i] = byteBuffers.get(i);
             }
             byteBuffers.clear();
             ClassLoader loader = parent;
             if (Build.VERSION.SDK_INT >= 26) {
                 for (ByteBuffer byteBuffer : array) {
                     loader = new InMemoryDexClassLoader(byteBuffer, loader);
                 }
             }
         }catch (Exception e){
             e.printStackTrace();
         }
         return null;
     }
     public static ClassLoader createClassLoader(String path,String cache,ClassLoader parent){
         if(Build.VERSION.SDK_INT >= 26) {
             if(path.toLowerCase().endsWith(".dex")){
                 try {
                     InputStream ins = new FileInputStream(path);
                     ByteArrayOutputStream bos = new ByteArrayOutputStream();
                     byte[] buffer = new byte[1024];
                     int len;
                     while ((len = ins.read(buffer)) != -1) {
                         bos.write(buffer, 0, len);
                     }
                     ins.close();
                     bos.flush();
                     byte[] bytes = bos.toByteArray();
                     ByteBuffer currentBuffer = ByteBuffer.allocate(bytes.length);
                     currentBuffer.position(0);
                     currentBuffer.put(buffer);
                     currentBuffer.position(0);
                     return new InMemoryDexClassLoader(currentBuffer, parent);
                 }catch (Exception e){
                     e.printStackTrace();
                 }
             }else if(isArchive(path)){
                 return getArchieveClassLoader(path,parent);
             }else {
                 throw new IllegalArgumentException("File is not a dex file or archive");
             }
         }else {
             return new DexClassLoader(path,cache,null,parent);
         }
         return null;
     }

    @Override
    public URL findResource(String name) {
        if(needResourceLoader && workingZipFile !=null && workingZipFile.getEntry(name) !=null){
             try {
                 return new URL("jar:"+workingZipFile.getName()+"!/"+name);
             } catch (MalformedURLException e) {
                 e.printStackTrace();
             }
         }
        return super.getResource(name);
    }

    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
         URL url;
         if((url = getResource(name)) !=null){
             return Collections.enumeration(List.of(url));
         }
        return super.getResources(name);
    }

}
