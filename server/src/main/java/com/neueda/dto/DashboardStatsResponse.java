package com.neueda.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class DashboardStatsResponse {

    public record StatusCount(String status, long count) {}

    public record VolumePoint(LocalDate date, long count, BigDecimal totalAmount) {}

    public record FailureReasonCount(String errorCode, long count) {}

    public record StageDuration(String stage, double avgSeconds) {}

    public record SuccessRatePoint(LocalDate date, double successRate) {}

    public record CurrencyBreakdown(String currency, long count, BigDecimal totalAmount) {}

    private LocalDate from;
    private LocalDate to;
    private List<StatusCount> statusDistribution;
    private List<VolumePoint> volumeOverTime;
    private List<FailureReasonCount> failureReasons;
    private List<StageDuration> avgStageDuration;
    private List<SuccessRatePoint> successRateOverTime;
    private List<CurrencyBreakdown> currencyBreakdown;

    public DashboardStatsResponse() {}

    public DashboardStatsResponse(LocalDate from,
                                   LocalDate to,
                                   List<StatusCount> statusDistribution,
                                   List<VolumePoint> volumeOverTime,
                                   List<FailureReasonCount> failureReasons,
                                   List<StageDuration> avgStageDuration,
                                   List<SuccessRatePoint> successRateOverTime,
                                   List<CurrencyBreakdown> currencyBreakdown) {
        this.from = from;
        this.to = to;
        this.statusDistribution = statusDistribution;
        this.volumeOverTime = volumeOverTime;
        this.failureReasons = failureReasons;
        this.avgStageDuration = avgStageDuration;
        this.successRateOverTime = successRateOverTime;
        this.currencyBreakdown = currencyBreakdown;
    }

    public LocalDate getFrom() {
        return from;
    }

    public void setFrom(LocalDate from) {
        this.from = from;
    }

    public LocalDate getTo() {
        return to;
    }

    public void setTo(LocalDate to) {
        this.to = to;
    }

    public List<StatusCount> getStatusDistribution() {
        return statusDistribution;
    }

    public void setStatusDistribution(List<StatusCount> statusDistribution) {
        this.statusDistribution = statusDistribution;
    }

    public List<VolumePoint> getVolumeOverTime() {
        return volumeOverTime;
    }

    public void setVolumeOverTime(List<VolumePoint> volumeOverTime) {
        this.volumeOverTime = volumeOverTime;
    }

    public List<FailureReasonCount> getFailureReasons() {
        return failureReasons;
    }

    public void setFailureReasons(List<FailureReasonCount> failureReasons) {
        this.failureReasons = failureReasons;
    }

    public List<StageDuration> getAvgStageDuration() {
        return avgStageDuration;
    }

    public void setAvgStageDuration(List<StageDuration> avgStageDuration) {
        this.avgStageDuration = avgStageDuration;
    }

    public List<SuccessRatePoint> getSuccessRateOverTime() {
        return successRateOverTime;
    }

    public void setSuccessRateOverTime(List<SuccessRatePoint> successRateOverTime) {
        this.successRateOverTime = successRateOverTime;
    }

    public List<CurrencyBreakdown> getCurrencyBreakdown() {
        return currencyBreakdown;
    }

    public void setCurrencyBreakdown(List<CurrencyBreakdown> currencyBreakdown) {
        this.currencyBreakdown = currencyBreakdown;
    }
}
