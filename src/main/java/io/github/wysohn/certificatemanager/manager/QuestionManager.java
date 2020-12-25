package io.github.wysohn.certificatemanager.manager;

import io.github.wysohn.certificatemanager.objects.Question;
import io.github.wysohn.certificatemanager.objects.Questions;
import io.github.wysohn.rapidframework3.core.inject.annotations.PluginDirectory;
import io.github.wysohn.rapidframework3.core.inject.factory.IStorageFactory;
import io.github.wysohn.rapidframework3.core.main.Manager;
import io.github.wysohn.rapidframework3.interfaces.io.file.IFileWriter;
import io.github.wysohn.rapidframework3.utils.JarUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.*;

@Singleton
public class QuestionManager extends Manager {
    private final File pluginDirectory;
    private final IFileWriter fileWriter;
    private final IStorageFactory storageFactory;

    private final Map<String, Questions> questionsMap = new HashMap<>();
    private File folder;

    @Inject
    public QuestionManager(@PluginDirectory File pluginDirectory,
                           IFileWriter fileWriter,
                           IStorageFactory storageFactory) {
        this.pluginDirectory = pluginDirectory;
        this.fileWriter = fileWriter;
        this.storageFactory = storageFactory;
    }

    @Override
    public void preload() throws Exception {
        folder = new File(pluginDirectory, "QuizBank");
    }

    @Override
    public void enable() throws Exception {

    }

    @Override
    public void load() throws Exception {
        if (!folder.exists())
            JarUtil.copyFromJar(getClass(),
                    "QuizBank/*",
                    pluginDirectory,
                    JarUtil.CopyOption.COPY_IF_NOT_EXIST);

        File[] certQuestionFolders = folder.listFiles(File::isDirectory);
        if (certQuestionFolders == null)
            return;

        questionsMap.clear();
        for (File certQuestionFolder : certQuestionFolders) {
            String certName = certQuestionFolder.getName();
            questionsMap.put(certName, new Questions(fileWriter, storageFactory, certQuestionFolder));
        }
    }

    @Override
    public void disable() throws Exception {

    }

    public List<Question> getQuestions(String certName, Locale locale) {
        return getQuestions(certName, locale.getLanguage());
    }

    public List<Question> getQuestions(String certName, String localeCode) {
        return Optional.of(certName)
                .map(questionsMap::get)
                .map(questions -> questions.getQuestions(localeCode))
                .orElseGet(ArrayList::new);
    }
}
