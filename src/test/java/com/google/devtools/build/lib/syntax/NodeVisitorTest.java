// Copyright 2015 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.devtools.build.lib.syntax;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for @{code NodeVisitor} */
@RunWith(JUnit4.class)
public final class NodeVisitorTest {

  private BuildFileAST parse(String... lines) throws IOException {
    ParserInput input = ParserInput.fromLines(lines);
    return BuildFileAST.parse(input, StarlarkThread.FAIL_FAST_HANDLER);
  }

  @Test
  public void everyIdentifierAndParameterIsVisitedInOrder() throws IOException {
    final List<String> idents = new ArrayList<>();
    final List<String> params = new ArrayList<>();

    class IdentVisitor extends NodeVisitor {
      @Override
      public void visit(Identifier node) {
        idents.add(node.getName());
      }

      @Override
      public void visit(Parameter<Expression, Expression> node) {
        params.add(node.toString());
      }
    }

    BuildFileAST file =
        parse(
            "a = b", //
            "def c(p1, p2=4, **p3):",
            "  for d in e: f(g)",
            "  return h + i.j()");
    IdentVisitor visitor = new IdentVisitor();
    file.accept(visitor);
    assertThat(idents).containsExactly("b", "a", "c", "e", "d", "f", "g", "h", "i", "j").inOrder();
    assertThat(params).containsExactly("p1", "p2=4", "**p3").inOrder();
  }
}
