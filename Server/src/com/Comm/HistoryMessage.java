package com.Comm;

import java.io.Serializable;
import java.sql.Timestamp;

public class HistoryMessage implements Serializable {
    private Integer SenderId;
    private Integer ReceiveId;
    private Timestamp dates;
    private String content;
    private String contentType;

    public HistoryMessage() {
    }

    public Integer getSenderId() {
        return SenderId;
    }

    public void setSenderId(Integer senderId) {
        SenderId = senderId;
    }

    public Integer getReceiveId() {
        return ReceiveId;
    }

    public void setReceiveId(Integer receiveId) {
        ReceiveId = receiveId;
    }

    public Timestamp getDates() {
        return dates;
    }

    public void setDates(Timestamp dates) {
        this.dates = dates;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
