/*
 * Copyright (c) 1997, 2025, Oracle and/or its affiliates. All rights reserved.
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

#ifndef SHARE_OPTO_PHASE_HPP
#define SHARE_OPTO_PHASE_HPP

#include "runtime/timer.hpp"

class IfNode;
class MergeMemNode;
class Node;
class PhaseGVN;
class Compile;
class ciMethod;

//------------------------------Phase------------------------------------------
// Most optimizations are done in Phases.  Creating a phase does any long
// running analysis required, and caches the analysis in internal data
// structures.  Later the analysis is queried using transform() calls to
// guide transforming the program.  When the Phase is deleted, so is any
// cached analysis info.  This basic Phase class mostly contains timing and
// memory management code.
class Phase : public StackObj {
public:
  enum PhaseNumber {
    Compiler,                         // Top-level compiler phase
    Parser,                           // Parse bytecodes
    Remove_Useless,                   // Remove useless nodes
    Remove_Useless_And_Renumber_Live, // First, remove useless nodes from the graph. Then, renumber live nodes.
    Optimistic,                       // Optimistic analysis phase
    GVN,                              // Pessimistic global value numbering phase
    Ins_Select,                       // Instruction selection phase
    CFG,                              // Build a CFG
    BlockLayout,                      // Linear ordering of blocks
    Register_Allocation,              // Register allocation, duh
    LIVE,                             // Dragon-book LIVE range problem
    StringOpts,                       // StringBuilder related optimizations
    Interference_Graph,               // Building the IFG
    Coalesce,                         // Coalescing copies
    Ideal_Loop,                       // Find idealized trip-counted loops
    Macro_Expand,                     // Expand macro nodes
    Peephole,                         // Apply peephole optimizations
    Vector,
    Output,
    last_phase
  };

#define ALL_PHASE_TRACE_IDS(f)                                   \
    f(   _t_none,                    "none")                     \
    f(   _t_parser,                  "parse")                    \
    f(   _t_optimizer,               "optimizer")                \
    f(     _t_escapeAnalysis,        "escapeAnalysis")           \
    f(       _t_connectionGraph,     "connectionGraph")          \
    f(       _t_macroEliminate,      "macroEliminate")           \
    f(     _t_iterGVN,               "iterGVN")                  \
    f(     _t_incrInline,            "incrementalInline")        \
    f(       _t_incrInline_ideal,    "incrementalInline_ideal")  \
    f(       _t_incrInline_igvn,     "incrementalInline_igvn")   \
    f(       _t_incrInline_pru,      "incrementalInline_pru")    \
    f(       _t_incrInline_inline,   "incrementalInline_inline") \
    f(     _t_vector,                "")                         \
    f(       _t_vector_elimination,  "vector_elimination")       \
    f(         _t_vector_igvn,       "incrementalInline_igvn")   \
    f(         _t_vector_pru,        "vector_pru")               \
    f(     _t_renumberLive,          "")                         \
    f(     _t_idealLoop,             "idealLoop")                \
    f(       _t_autoVectorize,       "autoVectorize")            \
    f(     _t_idealLoopVerify,       "idealLoopVerify")          \
    f(     _t_ccp,                   "ccp")                      \
    f(     _t_iterGVN2,              "iterGVN2")                 \
    f(     _t_macroExpand,           "macroExpand")              \
    f(     _t_barrierExpand,         "barrierExpand")            \
    f(     _t_graphReshaping,        "graphReshape")             \
    f(   _t_matcher,                 "matcher")                  \
    f(     _t_postselect_cleanup,    "postselect_cleanup")       \
    f(   _t_scheduler,               "scheduler")                \
    f(   _t_registerAllocation,      "regalloc")                 \
    f(     _t_ctorChaitin,           "ctorChaitin")              \
    f(     _t_buildIFGvirtual,       "buildIFG_virt")            \
    f(     _t_buildIFGphysical,      "buildIFG")                 \
    f(     _t_computeLive,           "computeLive")              \
    f(     _t_regAllocSplit,         "regAllocSplit")            \
    f(     _t_postAllocCopyRemoval,  "postAllocCopyRemoval")     \
    f(     _t_mergeMultidefs,        "mergeMultidefs")           \
    f(     _t_fixupSpills,           "fixupSpills")              \
    f(     _t_chaitinCompact,        "chaitinCompact")           \
    f(     _t_chaitinCoalesce1,      "chaitinCoalesce1")         \
    f(     _t_chaitinCoalesce2,      "chaitinCoalesce2")         \
    f(     _t_chaitinCoalesce3,      "chaitinCoalesce3")         \
    f(     _t_chaitinCacheLRG,       "chaitinCacheLRG")          \
    f(     _t_chaitinSimplify,       "chaitinSimplify")          \
    f(     _t_chaitinSelect,         "chaitinSelect")            \
    f(   _t_blockOrdering,           "blockOrdering")            \
    f(   _t_peephole,                "peephole")                 \
    f(   _t_postalloc_expand,        "postalloc_expand")         \
    f(   _t_output,                  "output")                   \
    f(     _t_instrSched,            "isched")                   \
    f(     _t_shortenBranches,       "shorten branches")         \
    f(     _t_buildOopMaps,          "bldOopMaps")               \
    f(     _t_fillBuffer,            "fill buffer")              \
    f(     _t_registerMethod,        "install_code")             \
    f(   _t_temporaryTimer1,         "tempTimer1")               \
    f(   _t_temporaryTimer2,         "tempTimer2")               \
    f(   _t_testPhase1,              "testPhase1")               \
    f(   _t_testPhase2,              "testPhase2")

  enum PhaseTraceId {
#define DEFID(name, text) name,
    ALL_PHASE_TRACE_IDS(DEFID)
#undef DEFID
    max_phase_timers
   };

  static const char* get_phase_trace_id_text(PhaseTraceId id);

  static elapsedTimer timers[max_phase_timers];

protected:
  enum PhaseNumber _pnum;       // Phase number (for stat gathering)

  static int _total_bytes_compiled;

  // accumulated timers
  static elapsedTimer _t_totalCompilation;
  static elapsedTimer _t_methodCompilation;
  static elapsedTimer _t_stubCompilation;

  // Generate a subtyping check.  Takes as input the subtype and supertype.
  // Returns 2 values: sets the default control() to the true path and
  // returns the false path.  Only reads from constant memory taken from the
  // default memory; does not write anything.  It also doesn't take in an
  // Object; if you wish to check an Object you need to load the Object's
  // class prior to coming here.
  // Used in GraphKit and PhaseMacroExpand
  static Node* gen_subtype_check(Node* subklass, Node* superklass, Node** ctrl, Node* mem, PhaseGVN& gvn, ciMethod* method, int bci);

public:
  Compile * C;
  Phase( PhaseNumber pnum );
  NONCOPYABLE(Phase);

  static void print_timers();
};

#endif // SHARE_OPTO_PHASE_HPP
