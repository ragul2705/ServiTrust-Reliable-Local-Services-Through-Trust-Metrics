package com.servitrust.logic;

public class TrustScoreCalculator {

    public static double calculate(
            double rating,
            long completedJobs,
            long totalRequests,
            long successfulRequests,
            boolean isAvailable,
            long cancellations
    ) {

        double ratingWeight = rating * 20.0;

        double successRate = totalRequests == 0
                ? 0
                : ((double) successfulRequests / totalRequests) * 30.0;

        double completionWeight = completedJobs * 2.0;

        double availabilityWeight = isAvailable ? 10.0 : 0.0;

        double cancellationPenalty = cancellations * 2.0;

        double score = ratingWeight
                + successRate
                + completionWeight
                + availabilityWeight
                - cancellationPenalty;

        if (score < 0) score = 0;
        if (score > 100) score = 100;

        return Math.round(score * 100.0) / 100.0;
    }
}