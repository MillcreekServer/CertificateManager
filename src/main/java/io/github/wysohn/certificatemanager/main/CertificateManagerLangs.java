package io.github.wysohn.certificatemanager.main;

import io.github.wysohn.rapidframework2.core.manager.lang.Lang;

public enum CertificateManagerLangs implements Lang {
    General_InvalidPlayer("&6${string} &cis not online."),
    General_InvalidOfflinePlayer("&There is no player named 6${string}&c."),

    CertificateExamManager_Welcome("&dWelcome to the exam of &e${string}&d.",
            "&7${string}&7.",
            "&7Once started, you may exit the exam by typing &dexit&7."),
    CertificateExamManager_Feedback_Question("&8[&7Q.${integer}&8] &7${string}"),
    CertificateExamManager_Feedback_Answer("  &8[&7A.${integer}&8] &7${string} ${string}"),
    CertificateExamManager_Result("&7Number of correct answers &a${integer}&8/&4${integer} &8(&e${double}%&8)",
            "&7Passing grade &8: &6${double}%",
            "&7Exam result &8: ${string}"),
    CertificateExamManager_RetakeAllowed("&eYou may retake it after ${string}"),
    CertificateExamManager_RetakeNotAllowed("&cRetake is not allowed for this exam."),
    CertificateExamManager_Pass("&aPassed"),
    CertificateExamManager_Fail("&cFailed"),

    Command_List_Desc("List exams you can take."),
    Command_List_Usage("/cer list"),

    Command_Take_Desc("Take the exam."),
    Command_Take_Usage("/cer take <certificate name>"),
    Command_Take_Prerequisites("&cSome of the requirements are not met.",
            "&7Required &8: ${string}"),

    Command_Reset_Desc("Delete or expire certificate from user."),
    Command_Reset_Usage("/cer reset <user> <certificate name>"),
    Command_Reset_Deleted("&aDeleted."),
    Command_Reset_Failed("&cCould not delete. Does the player really has &6${string}&c?"),

    Command_Pass_Desc("Manually pass the exam. Does not give the rewards."),
    Command_Pass_Usage("/cer pass <user> <certificate name>"),
    Command_Pass_Added("&aAdded."),
    Command_Pass_Failed("&cCould not add. Make sure &6${string} &cis a valid exam?"),

    Command_Take_NotExist("&7Exam &6${string} &7does not exist."),
    Command_Take_NoQuestions("&cThere are no question set for this exam. Contact administrator."),
    Command_Take_Duplicate("&7You already have this certificate. Expire&8: &e${date short GMT-07:00}"),
    Command_Take_Delay("&7You can't take it now. Wait until &e${date short GMT-07:00}"),
    Command_Take_Abandoned("&8Exam ended."),
    Command_Take_Fail("&cYou have failed the exam."),
    Command_Take_Success("&aYou have passed the exam!");


    private final String[] def;

    CertificateManagerLangs(String... def) {
        this.def = def;
    }

    @Override
    public String[] getEngDefault() {
        return def;
    }
}
