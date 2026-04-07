package com.transylvania.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long feedbackId;

    @Column(nullable = false)
    private int rating;

    @Column(length = 500)
    private String comment;

    @Column(name = "sentiment_tag", nullable = false)
    private String sentimentTag;

    @Column(name = "issue_tag", nullable = false)
    private String issueTag;

    @Column(name = "visit_again", nullable = false)
    private boolean visitAgain;

    @Column(name = "submitted_date", nullable = false)
    private LocalDate submittedDate;

    @ManyToOne
    @JoinColumn(name = "reservation_id", nullable = false, unique = true)
    private Reservation reservation;

    @ManyToOne
    @JoinColumn(name = "guest_id", nullable = false)
    private Guest guest;

    public Feedback() {
    }

    public Feedback(int rating,
                    String comment,
                    String sentimentTag,
                    String issueTag,
                    boolean visitAgain,
                    LocalDate submittedDate,
                    Reservation reservation,
                    Guest guest) {
        this.rating = rating;
        this.comment = comment;
        this.sentimentTag = sentimentTag;
        this.issueTag = issueTag;
        this.visitAgain = visitAgain;
        this.submittedDate = submittedDate;
        this.reservation = reservation;
        this.guest = guest;
    }

    public Long getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(Long feedbackId) {
        this.feedbackId = feedbackId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getSentimentTag() {
        return sentimentTag;
    }

    public void setSentimentTag(String sentimentTag) {
        this.sentimentTag = sentimentTag;
    }

    public String getIssueTag() {
        return issueTag;
    }

    public void setIssueTag(String issueTag) {
        this.issueTag = issueTag;
    }

    public boolean isVisitAgain() {
        return visitAgain;
    }

    public void setVisitAgain(boolean visitAgain) {
        this.visitAgain = visitAgain;
    }

    public LocalDate getSubmittedDate() {
        return submittedDate;
    }

    public void setSubmittedDate(LocalDate submittedDate) {
        this.submittedDate = submittedDate;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public Guest getGuest() {
        return guest;
    }

    public void setGuest(Guest guest) {
        this.guest = guest;
    }
}
