/*
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package com.codenvy.report;

/** @author Dmytro Nochevnov */
public class ReportParameters {
  private String title;
  private String sender;
  private String receiver;

  public ReportParameters() {}

  public ReportParameters(String title, String sender, String receivers) {
    this.title = title;
    this.sender = sender;
    this.receiver = receivers;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ReportParameters that = (ReportParameters) o;

    if (title != null ? !title.equals(that.title) : that.title != null) return false;
    if (sender != null ? !sender.equals(that.sender) : that.sender != null) return false;
    return !(receiver != null ? !receiver.equals(that.receiver) : that.receiver != null);
  }

  @Override
  public int hashCode() {
    int result = title != null ? title.hashCode() : 0;
    result = 31 * result + (sender != null ? sender.hashCode() : 0);
    result = 31 * result + (receiver != null ? receiver.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ReportParameters{"
        + "title='"
        + title
        + '\''
        + ", sender='"
        + sender
        + '\''
        + ", receiver='"
        + receiver
        + '\''
        + '}';
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getSender() {
    return sender;
  }

  public void setSender(String sender) {
    this.sender = sender;
  }

  public String getReceiver() {
    return receiver;
  }

  public void setReceiver(String receiver) {
    this.receiver = receiver;
  }
}
