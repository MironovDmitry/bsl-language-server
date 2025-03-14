/*
 * This file is a part of BSL Language Server.
 *
 * Copyright © 2018-2019
 * Alexey Sosnoviy <labotamy@gmail.com>, Nikita Gryzlov <nixel2007@gmail.com> and contributors
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * BSL Language Server is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * BSL Language Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with BSL Language Server.
 */
package org.github._1c_syntax.bsl.languageserver.diagnostics;

import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.lsp4j.DiagnosticRelatedInformation;
import org.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticMetadata;
import org.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticParameter;
import org.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticScope;
import org.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticSeverity;
import org.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticType;
import org.github._1c_syntax.bsl.languageserver.utils.RangeHelper;
import org.github._1c_syntax.bsl.parser.BSLParser;
import org.github._1c_syntax.bsl.parser.BSLParserRuleContext;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;


@DiagnosticMetadata(
  type = DiagnosticType.CODE_SMELL,
  severity = DiagnosticSeverity.CRITICAL,
  scope = DiagnosticScope.ALL,
  minutesToFix = 30
)
public class NestedStatementsDiagnostic extends AbstractListenerDiagnostic {

  private final String relatedMessage = getResourceString("parentStatementRelatedMessage");
  private static final int MAX_ALLOWED_LEVEL = 4;

  @DiagnosticParameter(
    type = Integer.class,
    defaultValue = "" + MAX_ALLOWED_LEVEL,
    description = "Максимальный уровень вложенности конструкций"
  )
  private int maxAllowedLevel = MAX_ALLOWED_LEVEL;

  private ParseTree lastCtx;
  private Deque<ParseTree> nestedParents = new ArrayDeque<>();


  @Override
  public void enterIfStatement(BSLParser.IfStatementContext ctx) {
    enterNode(ctx);
  }

  @Override
  public void exitIfStatement(BSLParser.IfStatementContext ctx) {
    exitNode(ctx);
  }

  @Override
  public void enterWhileStatement(BSLParser.WhileStatementContext ctx) {
    enterNode(ctx);
  }

  @Override
  public void exitWhileStatement(BSLParser.WhileStatementContext ctx) {
    exitNode(ctx);
  }

  @Override
  public void enterForStatement(BSLParser.ForStatementContext ctx) {
    enterNode(ctx);
  }

  @Override
  public void exitForStatement(BSLParser.ForStatementContext ctx) {
    exitNode(ctx);
  }

  @Override
  public void enterForEachStatement(BSLParser.ForEachStatementContext ctx) {
    enterNode(ctx);
  }

  @Override
  public void exitForEachStatement(BSLParser.ForEachStatementContext ctx) {
    exitNode(ctx);
  }

  @Override
  public void enterTryStatement(BSLParser.TryStatementContext ctx) {
    enterNode(ctx);
  }

  @Override
  public void exitTryStatement(BSLParser.TryStatementContext ctx) {
    exitNode(ctx);
  }

  private void enterNode(BSLParserRuleContext ctx) {
    lastCtx = ctx;
    nestedParents.push(ctx);
  }

  private void exitNode(BSLParserRuleContext ctx) {

    if (ctx == lastCtx && nestedParents.size() > maxAllowedLevel) {
      addRelatedInformationDiagnostic(ctx);
    }
    nestedParents.pop();
  }

  private void addRelatedInformationDiagnostic(BSLParserRuleContext ctx) {
    List<DiagnosticRelatedInformation> relatedInformation = new ArrayList<>();
    relatedInformation.add(
      RangeHelper.createRelatedInformation(
        documentContext.getUri(),
        RangeHelper.newRange(ctx.getStart()),
        relatedMessage
      )
    );

    nestedParents.stream()
      .filter(node -> node != ctx)
      .map(expressionContext ->
        RangeHelper.createRelatedInformation(
          documentContext.getUri(),
          RangeHelper.newRange(((BSLParserRuleContext) expressionContext).getStart()),
          relatedMessage
        )
      )
      .collect(Collectors.toCollection(() -> relatedInformation));

    diagnosticStorage.addDiagnostic(ctx.getStart(), relatedInformation);
  }
}
