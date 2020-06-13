package io.github.wysohn.certificatemanager.objects;

import io.github.wysohn.rapidframework2.bukkit.main.config.ConfigFileSession;
import io.github.wysohn.rapidframework2.bukkit.main.config.I18NConfigSession;
import io.github.wysohn.rapidframework2.core.interfaces.plugin.PluginRuntime;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Questions implements PluginRuntime {
    private static final String QUESTION = "Question";
    private static final String ANSWERS = "Answers";
    //folder containing folders (cert1/q1/, cert1/q2/, ...)
    private final File certQuestionFolder;
    private final String fileName = "setting";
    private final List<I18NConfigSession> configSessions = new ArrayList<>();

    public Questions(File certQuestionFolder) {
        this.certQuestionFolder = certQuestionFolder;
    }

    @Override
    public void enable() throws Exception {

    }

    @Override
    public void load() throws Exception {
        File[] questions = certQuestionFolder.listFiles(File::isDirectory);
        if (questions == null)
            return;

        configSessions.clear();
        for (File question : questions) {
            I18NConfigSession configSession = new I18NConfigSession(question, fileName);
            configSessions.add(configSession);
        }
    }

    @Override
    public void disable() throws Exception {

    }

    public List<Question> getQuestions(String localeCode) {
        List<Question> questions = new ArrayList<>();
        configSessions.stream()
                .map(session -> session.getSession(localeCode))
                .map(this::configToQuestion)
                .filter(Objects::nonNull)
                .forEach(questions::add);
        return questions;
    }

    private Question configToQuestion(ConfigFileSession config) {
        String prompt = config.get(QUESTION)
                .map(String.class::cast)
                .orElseThrow(() -> new RuntimeException("Failed to get " + QUESTION + " from " + config));
        List<String> answers = config.get(ANSWERS)
                .map(List.class::cast)
                .orElseThrow(() -> new RuntimeException("Failed to get " + ANSWERS + " from " + config));

        return new Question(prompt, answers.toArray(new String[0]));
    }
}
