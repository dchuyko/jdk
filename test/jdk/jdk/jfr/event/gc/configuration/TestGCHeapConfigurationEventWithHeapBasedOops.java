/*
 * Copyright (c) 2013, 2025, Oracle and/or its affiliates. All rights reserved.
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
 */

package jdk.jfr.event.gc.configuration;

import jdk.jfr.consumer.RecordedEvent;
import jdk.test.lib.jfr.EventVerifier;

/*
 * @test TestGCHeapConfigurationEventWith32BitOops
 * @requires vm.flagless
 * @requires vm.hasJFR
 * @requires vm.gc == "Parallel" | vm.gc == null
 * @requires os.family == "linux" | os.family == "windows"
 * @requires sun.arch.data.model == "64"
 * @library /test/lib /test/jdk
 * @build jdk.test.whitebox.WhiteBox
 * @run driver jdk.test.lib.helpers.ClassFileInstaller jdk.test.whitebox.WhiteBox
 * @run main/othervm -XX:+UnlockExperimentalVMOptions -XX:-UseFastUnorderedTimeStamps -XX:+UseParallelGC -XX:+UseCompressedOops -Xmx31g jdk.jfr.event.gc.configuration.TestGCHeapConfigurationEventWithHeapBasedOops
 */

/* See the shell script wrapper for the flags used when invoking the JVM */
public class TestGCHeapConfigurationEventWithHeapBasedOops extends GCHeapConfigurationEventTester {
    public static void main(String[] args) throws Exception {
        GCHeapConfigurationEventTester t = new TestGCHeapConfigurationEventWithHeapBasedOops();
        t.run();
    }

    @Override
    protected EventVerifier createVerifier(RecordedEvent e) {
        return new HeapBasedOopsVerifier(e);
    }
}

class HeapBasedOopsVerifier extends GCHeapConfigurationEventVerifier {
    public HeapBasedOopsVerifier(RecordedEvent e) {
        super(e);
    }

    @Override
    public void verify() throws Exception {
        // Can't verify min and initial heap size due to constraints on
        // physical memory on tests machines
        verifyMaxHeapSizeIs(gigabytes(31));
        verifyUsesCompressedOopsIs(true);
        verifyObjectAlignmentInBytesIs(8);
        verifyHeapAddressBitsIs(32);
        verifyCompressedOopModeContains("Non-zero");
    }
}
