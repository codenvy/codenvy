/*
 *  [2012] - [2017] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.report;

/**
 * @author Dmytro Nochevnov
 */
public class ReportParameters {
    private String  title;
    private String  sender;
    private String  receiver;

    public ReportParameters() {
    }

    public ReportParameters(String title, String sender, String receivers) {
        this.title = title;
        this.sender = sender;
        this.receiver = receivers;
    }

    @Override 
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ReportParameters that = (ReportParameters)o;

        if (title != null ? !title.equals(that.title) : that.title != null)
            return false;
        if (sender != null ? !sender.equals(that.sender) : that.sender != null)
            return false;
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
        return "ReportParameters{" +
               "title='" + title + '\'' +
               ", sender='" + sender + '\'' +
               ", receiver='" + receiver + '\'' +
               '}';
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
