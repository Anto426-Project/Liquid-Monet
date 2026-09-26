# Only runtime contracts belong here. Public SDK names do not need to survive
# the application's whole-program R8 pass. Dependencies supply their own rules.
-keepattributes Signature,InnerClasses,EnclosingMethod,*Annotation*
-keep class com.anto426.liquidmonet.glass.internal.LiquidNativeWave { *; }
