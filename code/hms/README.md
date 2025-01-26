1.配置HMS Core SDK的Maven仓地址

根目录build.gradle
```groovy
buildscript {
    repositories {
        google()
        jcenter()
        // 配置HMS Core SDK的Maven仓地址。
        maven {url 'https://developer.huawei.com/repo/'}
    }
    dependencies {
        ...
        // 增加AGC插件配置，请您参见AGC插件依赖关系选择合适的AGC插件版本。
        classpath 'com.huawei.agconnect:agcp:1.6.0.300'
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        // 配置HMS Core SDK的Maven仓地址。
        maven {url 'https://developer.huawei.com/repo/'}
    }
} 
```
(有的博客或者官网第一步要 添加当前应用的AppGallery Connect配置文件  , 如果我们的应用不需要上传到各大商店的话,可以暂时不考虑这个)

2.添加编译依赖

应用级的“build.gradle”文件

Scan Kit提供两种SDK，您可以根据需求选择合适的SDK
```groovy
dependencies{
  //implementation 'com.huawei.hms:scan:1.3.2.300'
  implementation 'com.huawei.hms:scanplus:1.3.2.300'
 }
```
文件头部声明下一行添加如下配置
```groovy
apply plugin: 'com.huawei.agconnect'
或者
plugins {
    id 'com.android.application'
    //添加如下配置
    id 'com.huawei.agconnect'
}
```
注:
如果在非华为手机使用多码能力接口，请使用Scan SDK-Plus，否则会影响识别。
com.huawei.hms:scanplus:{version}

3.配置混淆脚本
在应用级根目录下打开混淆配置文件“proguard-rules.pro”，加入排除HMS Core SDK的混淆配置脚本。

(我原有的项目中也开启了混淆,但是加入HMS Core SDK后,一直混淆不通过,会提示很多的Warnings,提示android studio混淆打包:transformClassesAndResourcesWithProguardForRelease‘,,,记录一下我的方法,因为看AS编译错误,有个显示是java heap oom,我就把gradle.properties  中的 org.gradle.jvmargs=-Xmx4096m)
```groovy
-ignorewarnings
-keepattributes *Annotation*
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
-keep class com.huawei.hianalytics.**{*;}
-keep class com.huawei.updatesdk.**{*;}
-keep class com.huawei.hms.**{*;}
```
4.指定权限
AndroidManifest.xml中指定相应的权限
代码中动态申请一下权限
校验是否开启相应的权限，决定是否继续扫码
```groovy
<!--permission for WiFI post processing,not for scankit itself-->
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE"></uses-permission>
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE"></uses-permission>
<!--camera permission-->
<uses-permission android:name="android.permission.CAMERA" />
<!--read permission for Bitmap Mode-->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<!--write permission for save QRCODE Bitmap,not for scankit itself-->
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-feature android:name="android.hardware.camera" />
<uses-feature android:name="android.hardware.camera.autofocus" />
<uses-permission android:name="android.permission.VIBRATE" />

<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" />
```
