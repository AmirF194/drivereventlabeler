package Civil.Sharif.Fathi.drivingeventlabeler.activity;

public class Event {
    private long start, end;
    private String eventLable;

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public String getEventLable() {
        return eventLable;
    }

    public void setEventLable(String eventLable) {
        this.eventLable = eventLable;
    }

    public Event(long start, long end, String eventLable) {
        this.start = start;
        this.end = end;
        this.eventLable = eventLable;
    }
}
