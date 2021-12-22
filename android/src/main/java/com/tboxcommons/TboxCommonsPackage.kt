package com.tboxcommons

import java.util.Arrays
import java.util.Collections

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager
import com.facebook.react.bridge.JavaScriptModule
import com.facebook.react.bridge.JavaScriptModule

import com.tboxcontacts
import java.util.Arrays




class TboxCommonsPackage : ReactPackage {
    override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
      val modules: List<NativeModule> = Arrays.asList<NativeModule>(modules)
      modules.add(new TboxCommonsModule(reactContext))
      modules.add(new TboxContactsModule(reactContext))
      return modules;
    }

    override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
        return emptyList<ViewManager<*, *>>()
    }
}
