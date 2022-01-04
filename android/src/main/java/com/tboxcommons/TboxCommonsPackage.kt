package com.tboxcommons

import java.util.Arrays

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager
import com.tboxcontacts.TboxContactsModule


class TboxCommonsPackage : ReactPackage {
    override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
      val modules = Arrays.asList<NativeModule>()
      modules.add(TboxCommonsModule(reactContext))
      modules.add(TboxContactsModule(reactContext))
      return modules;
    }

    override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
        return emptyList<ViewManager<*, *>>()
    }
}
