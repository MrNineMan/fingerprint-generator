package dev.draft.fingerprintgenerator;

import jakarta.persistence.*;

@Entity
@Table(name ="entries")
public class Entry {
    @Id
    @Column(name="visitor_id")
    private String visitorId;

    @Column(name = "first_name" )
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @Column(name = "email_address")
    private String emailAddress;

    /*There is no column in the table for requestId.
     *These annotations ensure that the requestId isn't inserted or fetched from the database.
     */
    @Column(insertable = false, updatable = false)
    @Transient
    private String requestId;

   // private String requestId;
    public Entry() {

    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstname) {
        this.firstName = firstname;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getVisitorId() {
        return visitorId;
    }

    public void setVisitorId(String visitorId) {
        this.visitorId = visitorId;
    }

    @Transient
    public String getRequestId() {
        return requestId;
    }
    @Transient
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
