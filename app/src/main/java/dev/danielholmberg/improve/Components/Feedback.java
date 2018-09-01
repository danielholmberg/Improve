package dev.danielholmberg.improve.Components;

public class Feedback {

    private String uid;
    private String feedback_id;
    private String title;
    private String feedback;
    private String timestamp;

    public Feedback() {}

    public Feedback(String uid, String feedback_id, String title, String feedback, String timestamp) {
        this.uid = uid;
        this.feedback_id = feedback_id;
        this.title = title;
        this.feedback = feedback;
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public String getFeedback_id() {
        return feedback_id;
    }

    public String getTitle() {
        return title;
    }

    public String getFeedback() {
        return feedback;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setFeedback_id(String feedback_id) {
        this.feedback_id = feedback_id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
