# Consumer ProGuard rules for MVU Core library
# These rules are automatically included when using the library

# Keep MVU Core interfaces and classes
-keep class com.yavorcool.mvucore.IStore { *; }
-keep class com.yavorcool.mvucore.IEventDispatcher { *; }
-keep class com.yavorcool.mvucore.ICommandHandler { *; }
-keep class com.yavorcool.mvucore.Update { *; }
-keep class com.yavorcool.mvucore.Next { *; }
-keep class com.yavorcool.mvucore.EventObserver { *; }
-keep class com.yavorcool.mvucore.impl.Store { *; }
