/*
 * Copyright (c) 2008, 2025, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.hotspot.igv.data;

import java.util.*;

/**
 *
 * @author Thomas Wuerthinger
 */
public class InputBlock {

    private List<InputNode> nodes;
    private final String name;
    private final InputGraph graph;
    private final Set<InputBlock> successors;
    private Set<Integer> liveOut;
    private boolean artificial;

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object o) {

        if (o == this) {
            return true;
        }

        if ((!(o instanceof InputBlock))) {
            return false;
        }

        final InputBlock b = (InputBlock)o;
        final boolean result = b.nodes.equals(nodes) && b.name.equals(name) && b.successors.size() == successors.size();
        if (!result) {
            return false;
        }

        final HashSet<String> s = new HashSet<>();
        for (InputBlock succ : successors) {
            s.add(succ.name);
        }

        for (InputBlock succ : b.successors) {
            if (!s.contains(succ.name)) {
                return false;
            }
        }

        if (this.liveOut.size() != b.liveOut.size()) {
            return false;
        }
        for (int liveRangeId : this.liveOut) {
            if (!b.liveOut.contains(liveRangeId)) {
                return false;
            }
        }

        return true;
    }

    InputBlock(InputGraph graph, String name) {
        this.graph = graph;
        this.name = name;
        nodes = new ArrayList<>();
        successors = new LinkedHashSet<>(2);
        liveOut = new HashSet<Integer>(0);
        artificial = false;
    }

    public String getName() {
        return name;
    }

    public List<InputNode> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    public void addNode(int id) {
        InputNode node = graph.getNode(id);
        assert node != null;
        // nodes.contains(node) is too expensive for large graphs so
        // just make sure the Graph doesn't know it yet.
        assert graph.getBlock(id) == null : "duplicate : " + node;
        graph.setBlock(node, this);
        nodes.add(node);
    }

    public void addLiveOut(int liveRangeId) {
        liveOut.add(liveRangeId);
    }

    public Set<Integer> getLiveOut() {
        return Collections.unmodifiableSet(liveOut);
    }

    public Set<InputBlock> getSuccessors() {
        return Collections.unmodifiableSet(successors);
    }

    public void setNodes(List<InputNode> nodes) {
        this.nodes = nodes;
    }

    @Override
    public String toString() {
        return "Block " + this.getName();
    }

    void addSuccessor(InputBlock b) {
        successors.add(b);
    }

    void setArtificial() {
        this.artificial = true;
    }

    public boolean isArtificial() {
        return artificial;
    }
}
