package com.uca.juangarcia.ifit.modules.notification.model;

/**
 * This class represents the details of an email to be sent by the application.
 * It includes the recipient's email address, the body of the message, the subject,
 * and an optional attachment.
 */
public class AppEmailDetails {

    private String recipient;
    private String msgBody;
    private String subject;
    private String attachment;

    public AppEmailDetails() { }
    
    public AppEmailDetails(String recipient, String msgBody, String subject, String attachment) {
        this.recipient = recipient;
        this.msgBody = msgBody;
        this.subject = subject;
        this.attachment = attachment;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getMsgBody() {
        return msgBody;
    }

    public void setMsgBody(String msgBody) {
        this.msgBody = msgBody;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }
}
