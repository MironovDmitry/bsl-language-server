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
package org.github._1c_syntax.bsl.languageserver;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.github._1c_syntax.bsl.languageserver.configuration.LanguageServerConfiguration;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class BSLLanguageServer implements LanguageServer, LanguageClientAware {

  private final LanguageServerConfiguration configuration;
  private BSLTextDocumentService textDocumentService;
  private BSLWorkspaceService workspaceService;
  private boolean shutdownWasCalled;

  public BSLLanguageServer(LanguageServerConfiguration configuration) {
    this.configuration = configuration;

    Locale currentLocale = Locale.forLanguageTag(this.configuration.getDiagnosticLanguage().getLanguageCode());
    Locale.setDefault(currentLocale);

    workspaceService = new BSLWorkspaceService(configuration);
    textDocumentService = new BSLTextDocumentService(configuration);
  }

  public BSLLanguageServer() {
    this(LanguageServerConfiguration.create());
  }

  @Override
  public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
    ServerCapabilities capabilities = new ServerCapabilities();
    capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
    //capabilities.setCompletionProvider(new CompletionOptions(false, Lists.newArrayList(".")));
    //capabilities.setHoverProvider(Boolean.TRUE);
    capabilities.setDocumentRangeFormattingProvider(Boolean.TRUE);
    capabilities.setDocumentFormattingProvider(Boolean.TRUE);
    capabilities.setFoldingRangeProvider(Boolean.TRUE);
    capabilities.setDocumentSymbolProvider(Boolean.TRUE);
    capabilities.setCodeActionProvider(Boolean.TRUE);

    InitializeResult result = new InitializeResult(capabilities);

    return CompletableFuture.completedFuture(result);
  }

  @Override
  public CompletableFuture<Object> shutdown() {
    shutdownWasCalled = true;
    textDocumentService.reset();
    return CompletableFuture.completedFuture(Boolean.TRUE);
  }

  @Override
  public void exit() {
    int status = shutdownWasCalled ? 0 : 1;
    System.exit(status);
  }

  @Override
  public TextDocumentService getTextDocumentService() {
    return textDocumentService;
  }

  @Override
  public WorkspaceService getWorkspaceService() {
    return workspaceService;
  }

  @Override
  public void connect(LanguageClient client) {
    textDocumentService.connect(client);
  }
}
