package com.vidara.tradecenter.support.dto.response;

public class TicketSummaryResponse {

    private long totalOpen;
    private long totalInProgress;
    private long totalResolved;
    private long totalClosed;
    private long urgentCount;


    // CONSTRUCTORS

    public TicketSummaryResponse() {
    }

    public TicketSummaryResponse(long totalOpen, long totalInProgress,
                                  long totalResolved, long totalClosed,
                                  long urgentCount) {
        this.totalOpen = totalOpen;
        this.totalInProgress = totalInProgress;
        this.totalResolved = totalResolved;
        this.totalClosed = totalClosed;
        this.urgentCount = urgentCount;
    }


    // GETTERS AND SETTERS

    public long getTotalOpen() {
        return totalOpen;
    }

    public void setTotalOpen(long totalOpen) {
        this.totalOpen = totalOpen;
    }

    public long getTotalInProgress() {
        return totalInProgress;
    }

    public void setTotalInProgress(long totalInProgress) {
        this.totalInProgress = totalInProgress;
    }

    public long getTotalResolved() {
        return totalResolved;
    }

    public void setTotalResolved(long totalResolved) {
        this.totalResolved = totalResolved;
    }

    public long getTotalClosed() {
        return totalClosed;
    }

    public void setTotalClosed(long totalClosed) {
        this.totalClosed = totalClosed;
    }

    public long getUrgentCount() {
        return urgentCount;
    }

    public void setUrgentCount(long urgentCount) {
        this.urgentCount = urgentCount;
    }
}
