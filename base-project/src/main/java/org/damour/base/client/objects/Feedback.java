package org.damour.base.client.objects;

import java.util.Date;

public class Feedback {

  private Date date;
  private String subject;
  private String contactName;
  private String email;
  private String phone;
  private String message;

  public Feedback() {
  }

  public String getContactName() {
    return contactName;
  }

  public void setContactName(String contactName) {
    this.contactName = contactName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String comments) {
    this.message = comments;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

}
