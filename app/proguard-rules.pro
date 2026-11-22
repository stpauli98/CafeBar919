# Zadržava brojeve linija za čitljive stack trace-ove.
-keepattributes SourceFile,LineNumberTable,*Annotation*,InnerClasses,EnclosingMethod,Signature

# kotlinx-serialization: serializeri generisani za @Serializable klase.
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class **$$serializer { *; }
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

# Room: baza, DAO i entiteti se instanciraju refleksijom (<Baza>_Impl).
-keep class * extends androidx.room.RoomDatabase { <init>(...); }
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *
