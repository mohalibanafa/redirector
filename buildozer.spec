[app]

# (str) Title of your application
title = RedirectMaster

# (str) Package name
package.name = redirectmaster

# (str) Package domain (needed for android/ios packaging)
package.domain = org.antigravity

# (str) Application version
version = 0.1

# (str) Source code where the main.py live
source.dir = .

# (list) Source files to include (let empty to include all the files)
source.include_exts = py,png,jpg,kv,atlas

# (list) Application requirements
# comma separated e.g. requirements = sqlite3,kivy
requirements = python3,flet,openssl,sqlite3,hostpython3

# (str) Custom source folders for requirements
# packagename == path_to_parent_dir_of_package
# requirements.source.kivy = ../../kivy

# (str) Presplash of the application
#presplash.filename = %(source.dir)s/data/presplash.png

# (str) Icon of the application
#icon.filename = %(source.dir)s/data/icon.png

# (list) Supported orientations
# Valid values are: landscape, portrait, portrait-upside-down, landscape-left, landscape-right
orientation = portrait

# (list) Permissions
android.permissions = INTERNET, ACCESS_NETWORK_STATE

# (int) Android API to use
android.api = 33

# (bool) If True, then skip the acknowledgment of the Android SDK license
# agreement. This should only be used if you have itself accepted the
# agreement.
android.accept_sdk_license = True

# (int) Minimum API your APK will support.
android.minapi = 21

# (int) Android SDK version to use
#android.sdk = 33

# (str) Android NDK version to use
#android.ndk = 25b

# (bool) Use --private data storage (True) or --dir public storage (False)
#android.private_storage = True

# (str) Android NDK directory (if empty, it will be automatically downloaded.)
#android.ndk_path =

# (str) Android SDK directory (if empty, it will be automatically downloaded.)
#android.sdk_path =

# (str) ANT directory (if empty, it will be automatically downloaded.)
#android.ant_path =

# (list) Android white list
#android.whitelist =

# (str) Android entry point, default is to use start.py
#android.entrypoint = main.py

# (list) Pattern to exclude for the search
#android.exclude_src =

# (list) List of Java files to add to the android project (can be java or a directory containing the files)
#android.add_src =

# (list) Android AAR archives to add
#android.add_aars =

# (list) Gradle dependencies
#android.add_dependencies =

# (list) Java classes to add as activities to the manifest.
#android.add_activities = com.facebook.AdsConfig

# (list) External libraries to copy into the lib/armeabi directory of the project.
#android.add_libs_armeabi = lib/armeabi/libtest.so
#android.add_libs_armeabi_v7a = lib/armeabi-v7a/libtest.so
#android.add_libs_arm64_v8a = lib/arm64-v8a/libtest.so
#android.add_libs_x86 = lib/x86/libtest.so
#android.add_libs_x86_64 = lib/x86_64/libtest.so

# (list) Android static libraries to copy into the project
#android.add_static_libs =

# (list) Android extra manifest attributes
#android.manifest_attributes =

# (str) Android logcat filters to use
#android.logcat_filters = *:S python:D

# (bool) Copy library instead of making a lib dir and adding it to the project (default: False)
#android.copy_libs = 1

# (list) The Android archs to build for, choices: armeabi-v7a, arm64-v8a, x86, x86_64
android.archs = arm64-v8a, armeabi-v7a

# (bool) enables Android auto backup feature (on Android API >= 23)
android.allow_backup = True

# (str) The format used to package the app for release mode (aab or apk or default).
android.release_artifact = apk

# (str) The format used to package the app for debug mode (apk or default).
android.debug_artifact = apk

[buildozer]

# (int) Log level (0 = error only, 1 = info, 2 = debug (with command output))
log_level = 2

# (int) Display warning if buildozer is run as root (0 = off, 1 = on)
warn_on_root = 1
