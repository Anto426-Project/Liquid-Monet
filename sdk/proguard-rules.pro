# Build-time rules: a published library must retain its callable ABI.
# The final application R8 pass can shrink/rename unused public APIs.
-keepattributes Signature,InnerClasses,EnclosingMethod,Exceptions,*Annotation*
-keep class kotlin.Metadata { *; }
-allowaccessmodification
-keep,includedescriptorclasses,allowoptimization public class com.anto426.liquidmonet.** { public protected *; }
-keep,includedescriptorclasses,allowoptimization public class com.kyant.** { public protected *; }
-keep,includedescriptorclasses,allowoptimization @kotlin.PublishedApi class * { *; }
-keepclassmembers,includedescriptorclasses,allowoptimization class * { @kotlin.PublishedApi *; }
-keep class com.anto426.liquidmonet.glass.internal.LiquidNativeWave { *; }

# Independently minified SDKs must not produce colliding names such as a.a.
-repackageclasses com.anto426.liquidmonet.obfuscated
