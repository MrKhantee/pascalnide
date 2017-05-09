/*
 *  Copyright 2017 Tran Le Duy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duy.pascal.backend.lib;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.duy.pascal.backend.lib.android.utils.AndroidBatteryLib;
import com.duy.pascal.backend.lib.android.utils.AndroidClipboardLib;
import com.duy.pascal.backend.lib.android.view.AndroidNotifyLib;
import com.duy.pascal.backend.lib.android.hardware.AndroidSensorLib;
import com.duy.pascal.backend.lib.android.voice.AndroidSpeechRecognitionLib;
import com.duy.pascal.backend.lib.android.voice.AndroidTextToSpeechLib;
import com.duy.pascal.backend.lib.android.hardware.AndroidVibrateLib;
import com.duy.pascal.backend.lib.android.barcode.ZXingAPI;
import com.duy.pascal.backend.lib.android.connection.bluetooth.AndroidBluetoothLib;
import com.duy.pascal.backend.lib.android.media.AndroidMediaPlayerLib;
import com.duy.pascal.backend.lib.android.temp.AndroidSettingLib;
import com.duy.pascal.backend.lib.android.media.AndroidToneGeneratorLib;
import com.duy.pascal.backend.lib.android.temp.AndroidUtilsLib;
import com.duy.pascal.backend.lib.android.connection.wifi.AndroidWifiLib;
import com.duy.pascal.backend.lib.android.AndroidLibraryManager;
import com.duy.pascal.backend.lib.annotations.PascalMethod;
import com.duy.pascal.backend.lib.file.FileLib;
import com.duy.pascal.backend.lib.graph.GraphLib;
import com.duy.pascal.backend.lib.io.IOLib;
import com.duy.pascal.backend.lib.io.InOutListener;
import com.duy.pascal.backend.lib.math.MathLib;
import com.duy.pascal.backend.function_declaretion.abstract_class.AbstractMethodDeclaration;
import com.duy.pascal.backend.function_declaretion.ExitFunction;
import com.duy.pascal.backend.function_declaretion.ExitNoneFunction;
import com.duy.pascal.backend.function_declaretion.LengthFunction;
import com.duy.pascal.backend.function_declaretion.HighFunction;
import com.duy.pascal.backend.function_declaretion.LowFunction;
import com.duy.pascal.backend.function_declaretion.NewFunction;
import com.duy.pascal.backend.function_declaretion.SetLengthFunction;
import com.duy.pascal.backend.function_declaretion.SizeOfArrayFunction;
import com.duy.pascal.backend.function_declaretion.SizeOfObjectFunction;
import com.duy.pascal.frontend.activities.ExecHandler;
import com.duy.pascal.frontend.activities.RunnableActivity;
import com.duy.pascal.frontend.program_structure.viewholder.StructureType;
import com.duy.pascal.frontend.view.code_view.SuggestItem;
import com.js.interpreter.ast.MethodDeclaration;
import com.js.interpreter.ast.expressioncontext.ExpressionContextMixin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by Duy on 08-Apr-17.
 */

@SuppressWarnings("DefaultFileTemplate")
public class PascalLibraryManager {
    public static final Map<String, Class<? extends PascalLibrary>> MAP_LIBRARIES = new Hashtable<>();
    @NonNull
    private ExpressionContextMixin program;
    @Nullable
    private RunnableActivity handler;
    private AndroidLibraryManager facadeManager;

    public PascalLibraryManager(@NonNull ExpressionContextMixin program,
                                @Nullable RunnableActivity handler) {
        this.program = program;
        this.handler = handler;
        facadeManager = new AndroidLibraryManager(AndroidLibraryUtils.getSdkVersion(), handler);
        initMapLib();
    }

    public static ArrayList<SuggestItem> getAllMethodDescription(Class<?>... classes) {
        ArrayList<SuggestItem> suggestItems = new ArrayList<>();
        for (Class<?> aClass : classes) {
            Method[] methods = aClass.getDeclaredMethods();
            for (Method method : methods) {
                if (AndroidLibraryUtils.getSdkVersion() >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    if (method.isAnnotationPresent(PascalMethod.class)) {
                        PascalMethod annotation = method.getAnnotation(PascalMethod.class);
                        String description = annotation.description();
                        suggestItems.add(new SuggestItem(StructureType.TYPE_FUNCTION, method.getName(), description));
                    }
                } else {
                    if (Modifier.isPublic(method.getModifiers())) {
                        suggestItems.add(new SuggestItem(StructureType.TYPE_FUNCTION, method.getName()));
                    }
                }
            }
        }
        return suggestItems;
    }

    private void initMapLib() {
        MAP_LIBRARIES.put(CrtLib.NAME, CrtLib.class);
        MAP_LIBRARIES.put(DosLib.NAME, DosLib.class);
        MAP_LIBRARIES.put(MathLib.NAME, MathLib.class);
        MAP_LIBRARIES.put(GraphLib.NAME, GraphLib.class);
        MAP_LIBRARIES.put(StrUtilsLibrary.NAME, StrUtilsLibrary.class);
        MAP_LIBRARIES.put(SysUtilsLibrary.NAME, SysUtilsLibrary.class);

        MAP_LIBRARIES.put(AndroidMediaPlayerLib.NAME, AndroidMediaPlayerLib.class);
        MAP_LIBRARIES.put(AndroidUtilsLib.NAME, AndroidUtilsLib.class);
        MAP_LIBRARIES.put(AndroidToneGeneratorLib.NAME, AndroidToneGeneratorLib.class);
        MAP_LIBRARIES.put(AndroidWifiLib.NAME, AndroidWifiLib.class);
        MAP_LIBRARIES.put(AndroidSettingLib.NAME, AndroidSettingLib.class);
        MAP_LIBRARIES.put(AndroidBluetoothLib.NAME, AndroidBluetoothLib.class);

        MAP_LIBRARIES.put(AndroidBatteryLib.NAME, AndroidBatteryLib.class);
        MAP_LIBRARIES.put(AndroidTextToSpeechLib.NAME, AndroidTextToSpeechLib.class);
        MAP_LIBRARIES.put(AndroidSensorLib.NAME, AndroidSensorLib.class);
        MAP_LIBRARIES.put(AndroidClipboardLib.NAME, AndroidClipboardLib.class);
        MAP_LIBRARIES.put(AndroidNotifyLib.NAME, AndroidNotifyLib.class);
        MAP_LIBRARIES.put(AndroidVibrateLib.NAME, AndroidVibrateLib.class);
        MAP_LIBRARIES.put(AndroidSpeechRecognitionLib.NAME, AndroidSpeechRecognitionLib.class);
        MAP_LIBRARIES.put(ZXingAPI.NAME, ZXingAPI.class);
        MAP_LIBRARIES.put(AndroidMediaPlayerLib.NAME, AndroidMediaPlayerLib.class);
    }

    /**
     * get method of class, call by java reflect
     *
     * @param classes  - list class
     * @param modifier - allow method modifier
     */
    public void addMethodFromClasses(ArrayList<Class<? extends PascalLibrary>> classes,
                                     int modifier) {

        for (Class<? extends PascalLibrary> t : classes) {
            addMethodFromClass(t);
        }
    }

    /**
     * load method from a class
     */

    public void addMethodFromClass(Class<? extends PascalLibrary> t) {
        Object parent = null;
        Constructor constructor;
        try {
            constructor = t.getConstructor(InOutListener.class);
            parent = constructor.newInstance(handler);
        } catch (Exception ignored) {
        }
        if (parent == null) {
            try {
                constructor = t.getConstructor(ExecHandler.class);
                parent = constructor.newInstance(handler);
            } catch (Exception ignored) {
            }
        }
        if (parent == null) {
            try {
                constructor = t.getConstructor(AndroidLibraryManager.class);
                parent = constructor.newInstance(facadeManager);
            } catch (Exception ignored) {
            }
        }
        if (parent == null) {
            try {
                constructor = t.getConstructor();
                parent = constructor.newInstance();
            } catch (Exception ignored) {
            }
        }

        if (parent != null) {
            for (Method method : t.getDeclaredMethods()) {
                if (AndroidLibraryUtils.getSdkVersion() >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    if (method.isAnnotationPresent(PascalMethod.class)) {
                        PascalMethod annotation = method.getAnnotation(PascalMethod.class);
                        String description = annotation.description();
                        MethodDeclaration methodDeclaration = new MethodDeclaration(parent, method, description);
                        program.declareFunction(methodDeclaration);
                    }
                } else {
                    if (Modifier.isPublic(method.getModifiers())) {
                        MethodDeclaration methodDeclaration = new MethodDeclaration(parent, method);
                        program.declareFunction(methodDeclaration);
                    }
                }
            }
        }

    }

    /**
     * load all method of the list classes
     *
     * @param source       - current library
     * @param newLibraries - list name of libraries to import
     * @param handler-     - handler for handle output, input, graph, ...
     * @param program      - main program for declare function
     */
    public void loadLibrary(ArrayList<String> source, ArrayList<String> newLibraries,
                            RunnableActivity handler,
                            ExpressionContextMixin program) {
        source.addAll(newLibraries);
        ArrayList<Class<? extends PascalLibrary>> classes = new ArrayList<>();
        for (String name : newLibraries) {
            classes.add(MAP_LIBRARIES.get(name.toLowerCase()));
        }
        addMethodFromClasses(classes, Modifier.PUBLIC);
    }

    /**
     * load system method
     */
    public void loadSystemLibrary() {
        program.declareFunction(new AbstractMethodDeclaration(new SetLengthFunction()));
        program.declareFunction(new AbstractMethodDeclaration(new LengthFunction()));
        program.declareFunction(new AbstractMethodDeclaration(new SizeOfObjectFunction()));
        program.declareFunction(new AbstractMethodDeclaration(new SizeOfArrayFunction()));
        program.declareFunction(new AbstractMethodDeclaration(new ExitFunction()));
        program.declareFunction(new AbstractMethodDeclaration(new ExitNoneFunction()));
        program.declareFunction(new AbstractMethodDeclaration(new HighFunction()));
        program.declareFunction(new AbstractMethodDeclaration(new LowFunction()));
        program.declareFunction(new AbstractMethodDeclaration(new NewFunction()));

        //Important: load file library before io lib. Because  method readln(file, ...)
        //in {@link FileLib} will be override method readln(object...) in {@link IOLib}

        addMethodFromClass(FileLib.class);
        addMethodFromClass(IOLib.class);
        addMethodFromClass(SystemLib.class);


    }

}
