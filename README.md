# Android14_DexClassloader
No More Writeable Dex Error
<p>
Custom DexClassLoader for Android 14
  </p>
```java
public static void demo(){
ClassLoader loader = new DexClassloaderV2(new File(out.zip),context.getCacheDir().toString(),Classloader.getSystemClassLoader());
}
```
