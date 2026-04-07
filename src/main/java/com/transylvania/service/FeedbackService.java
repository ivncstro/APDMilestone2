package com.transylvania.service;

import com.transylvania.config.LoggerUtil;
import com.transylvania.model.Feedback;
import com.transylvania.model.Reservation;
import com.transylvania.repository.FeedbackRepository;
import com.transylvania.repository.ReservationRepository;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class FeedbackService {

    private static final int MAX_COMMENT_LENGTH = 500;

    private final FeedbackRepository feedbackRepository;
    private final ReservationRepository reservationRepository;

    public FeedbackService() {
        this(new FeedbackRepository(), new ReservationRepository());
    }

    public FeedbackService(FeedbackRepository feedbackRepository, ReservationRepository reservationRepository) {
        this.feedbackRepository = feedbackRepository;
        this.reservationRepository = reservationRepository;
    }

    public Feedback submitFeedback(String phone, int rating, boolean visitAgain, String comment) {
        String normalizedPhone = phone == null ? "" : phone.trim();
        String normalizedComment = comment == null ? "" : comment.trim();

        validateSubmission(normalizedPhone, rating, normalizedComment);

        Reservation reservation = reservationRepository.findLatestCheckedOutByPhoneWithoutFeedback(normalizedPhone)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Feedback can only be submitted for a checked-out reservation that has not already been reviewed."
                ));

        Feedback feedback = new Feedback(
                rating,
                normalizedComment,
                "N/A",
                "N/A",
                visitAgain,
                LocalDate.now(),
                reservation,
                reservation.getGuest()
        );

        Feedback saved = feedbackRepository.save(feedback);
        LoggerUtil.logInfo(
                "guest",
                "submit_feedback",
                "feedback",
                String.valueOf(saved.getFeedbackId()),
                "Feedback submitted for reservation " + reservation.getReservationId()
        );
        return saved;
    }

    public List<Feedback> searchFeedback(String guestName, Integer rating, String sentimentTag, LocalDate fromDate, LocalDate toDate) {
        return feedbackRepository.search(guestName, rating, sentimentTag, fromDate, toDate);
    }

    public void exportFeedbackCsv(String filePath, List<Feedback> feedbackList) {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("Reservation ID,Guest,Phone,Rating,Date,Sentiment Tag,Issue Tag,Visit Again,Comment\n");
            for (Feedback feedback : feedbackList) {
                writer.write(String.format("%s,%s,%s,%d,%s,%s,%s,%s,%s%n",
                        feedback.getReservation().getReservationId(),
                        escape(feedback.getGuest().getFirstName() + " " + feedback.getGuest().getLastName()),
                        escape(feedback.getGuest().getPhone()),
                        feedback.getRating(),
                        feedback.getSubmittedDate(),
                        escape(feedback.getSentimentTag()),
                        escape(feedback.getIssueTag()),
                        feedback.isVisitAgain() ? "Yes" : "No",
                        escape(feedback.getComment())));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to export feedback summary.", e);
        }
    }

    public Optional<Feedback> findByReservationId(Long reservationId) {
        return feedbackRepository.findByReservationId(reservationId);
    }

    private void validateSubmission(String phone, int rating, String comment) {
        if (phone.isBlank()) {
            throw new IllegalArgumentException("Please enter your phone number.");
        }
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Please select a star rating from 1 to 5.");
        }
        if (comment.length() > MAX_COMMENT_LENGTH) {
            throw new IllegalArgumentException("Comments cannot exceed 500 characters.");
        }
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

}
