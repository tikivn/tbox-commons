package com.tboxcommons

import java.util.Arrays

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager
import com.tboxcontacts.TboxContactsModule

class TboxCommonsPackage : ReactPackage {
  override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
    val modules = arrayOf(TboxCommonsModule(reactContext),TboxContactsModule(reactContext) );
    return Arrays.asList<NativeModule>(* modules);
  }

  override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
    return emptyList<ViewManager<*, *>>()
  }
}
