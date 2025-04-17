# Android14_DexClassloader
No More Writeable Dex Error
<h>Custom DexClassLoader for Android 14</h>
```java
public static void demo(){
ClassLoader loader = new DexClassloaderV2(new File(out.zip),context.getCacheDir().toString(),Classloader.getSystemClassLoader());
}
```
