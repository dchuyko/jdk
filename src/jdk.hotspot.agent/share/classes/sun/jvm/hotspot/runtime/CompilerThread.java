/*
 * Copyright (c) 2000, 2025, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 *
 */

package sun.jvm.hotspot.runtime;

import java.io.*;
import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.types.*;
import sun.jvm.hotspot.utilities.Observable;
import sun.jvm.hotspot.utilities.Observer;

public class CompilerThread extends JavaThread {
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }

  private static AddressField envField;

  private static synchronized void initialize(TypeDataBase db) throws WrongTypeException { }

  public CompilerThread(Address addr) {
    super(addr);
  }

  @Override
  public boolean isHiddenFromExternalView() {
      /*
       * See JDK-8348317. CompilerThreads are sometimes hidden and sometimes not. They
       * are not when JVMCI is enabled and a compiler implemented in java is running
       * on the CompilerThread. This is hard for SA to determine, and not something a customer
       * is likely to ever run across or care about, so by default all CompilerThreads
       * are considered to be hidden. However, we allow this behaviour to be overridden
       * in case the user has a need to make the CompilerThreads visible.
       */
      return !Boolean.getBoolean("sun.jvm.hotspot.runtime.CompilerThread.visible");
  }

}
