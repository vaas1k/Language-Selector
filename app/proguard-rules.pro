-dontobfuscate
# Shizuku starts UserService by name via reflection; AIDL stubs cross the binder
-keep class vegabobo.languageselector.service.UserService { *; }
-keep class vegabobo.languageselector.service.RootUserService { *; }
-keep class vegabobo.languageselector.IUserService** { *; }
