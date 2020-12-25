package io.github.wysohn.certificatemanager.objects;

import io.github.wysohn.rapidframework3.bukkit.config.I18NConfigSession;
import io.github.wysohn.rapidframework3.core.inject.factory.IStorageFactory;
import io.github.wysohn.rapidframework3.interfaces.io.file.IFileWriter;
import io.github.wysohn.rapidframework3.interfaces.store.IKeyValueStorage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Questions {
    private static final String QUESTION = "Question";
    private static final String ANSWERS = "Answers";

    private final List<I18NConfigSession> configSessions = new ArrayList<>();

    public Questions(IFileWriter fileWriter, IStorageFactory storageFactory, File certQuestionFolder) {
        File[] questions = certQuestionFolder.listFiles(f -> f.getName().endsWith(".yml")
                && f.getName().indexOf('_') == -1);
        if (questions == null)
            return;

        configSessions.clear();
        for (File question : questions) {
            int dotIndex = question.getName().indexOf('.');
            if (dotIndex == -1)
                continue;

            I18NConfigSession configSession = new I18NConfigSession(fileWriter,
                    storageFactory,
                    certQuestionFolder,
                    question.getName().substring(0, dotIndex));
            try {
                configSession.load();
                configSessions.add(configSession);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public List<Question> getQuestions(String localeCode) {
        List<Question> questions = new ArrayList<>();
        configSessions.stream()
                .map(session -> session.getSession(localeCode))
                .map(this::configToQuestion)
                .forEach(questions::add);
        return questions;
    }

    private Question configToQuestion(IKeyValueStorage config) {
        String prompt = config.get(QUESTION)
                .map(String.class::cast)
                .orElseThrow(() -> new RuntimeException("Failed to get " + QUESTION + " from " + config));
        List<String> answers = config.get(ANSWERS)
                .map(List.class::cast)
                .orElseThrow(() -> new RuntimeException("Failed to get " + ANSWERS + " from " + config));

        return new Question(prompt, answers.toArray(new String[0]));
    }
}
