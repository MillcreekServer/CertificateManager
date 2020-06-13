package io.github.wysohn.certificatemanager.objects;

public class Question {
    private final String question;
    private final String[] answers;

    public Question(String question, String[] answers) {
        this.question = question;
        this.answers = answers;
    }

    public String getQuestion() {
        return question;
    }

    public String[] getAnswers() {
        return answers.clone();
    }
}
