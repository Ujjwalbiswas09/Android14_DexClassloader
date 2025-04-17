# Android14_DexClassloader
No More Writeable Dex Error
Custom DexClassLoader for Android 14
```java
public void static demo(){
ClassLoader loader = new DexClassloaderV2(new File(out.zip),context.getCacheDir().toString(),Classloader.getSystemClassLoader());
}
```
