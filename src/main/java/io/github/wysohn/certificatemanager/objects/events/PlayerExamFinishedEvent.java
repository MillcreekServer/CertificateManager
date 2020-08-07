package io.github.wysohn.certificatemanager.objects.events;

import io.github.wysohn.certificatemanager.objects.CertificateExam;
import io.github.wysohn.certificatemanager.objects.User;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerExamFinishedEvent extends PlayerEvent {
    private final String examName;
    private final CertificateExam exam;
    private final boolean passed;

    public PlayerExamFinishedEvent(User who, String examName, CertificateExam exam, boolean passed) {
        super(who.getSender());
        this.examName = examName;
        this.exam = exam;
        this.passed = passed;
    }

    public String getExamName() {
        return examName;
    }

    public CertificateExam getExam() {
        return exam;
    }

    public boolean isPassed() {
        return passed;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    ///////////////////////////////////////////////////
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
