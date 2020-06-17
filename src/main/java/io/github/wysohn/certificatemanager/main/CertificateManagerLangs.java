package io.github.wysohn.certificatemanager.main;

import io.github.wysohn.rapidframework2.core.manager.lang.Lang;

public enum CertificateManagerLangs implements Lang {
    CertificateExamManager_Welcome("&dWelcome to the exam of &e${string}&d.",
            "&7${string}&7.",
            "&7Once started, you may exit the exam by typing &dexit&7."),
    CertificateExamManager_Feedback_Question("&7Q.${integer} ${string}"),
    CertificateExamManager_Feedback_Answer("&7A.${integer} ${string}"),
    CertificateExamManager_Result("&7Number of correct answers &a${integer}&8/&4${integer} &8(&a${double}%&8)",
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
    ;

    private final String[] def;

    CertificateManagerLangs(String... def) {
        this.def = def;
    }

    @Override
    public String[] getEngDefault() {
        return def;
    }
}
