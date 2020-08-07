package io.github.wysohn.certificatemanager.manager;

import io.github.wysohn.certificatemanager.mediator.ExamMediator;
import io.github.wysohn.certificatemanager.objects.CertificateExam;
import io.github.wysohn.rapidframework2.bukkit.utils.Utf8YamlConfiguration;
import io.github.wysohn.rapidframework2.core.main.PluginMain;
import org.bukkit.configuration.file.FileConfiguration;
import util.JarUtil;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class CertificateExamManager extends PluginMain.Manager {
    private File folder;
    private final Map<String, CertificateExam> examMap = new HashMap<>();

    public CertificateExamManager(int loadPriority) {
        super(loadPriority);
    }

    @Override
    public void preload() throws Exception {
        folder = new File(main().getPluginDirectory(), "CertificateExams");
    }

    @Override
    public void enable() throws Exception {

    }

    @Override
    public void load() throws Exception {
        if (!folder.exists())
            JarUtil.copyFromJar(getClass(),
                    "CertificateExams/*",
                    main().getPluginDirectory(),
                    JarUtil.CopyOption.COPY_IF_NOT_EXIST);

        File[] examFiles = folder.listFiles(f -> f.getName().endsWith(".yml"));
        if (examFiles == null)
            return;

        examMap.clear();
        for (File examFile : examFiles) {
            String fileName = examFile.getName();
            FileConfiguration configuration = new Utf8YamlConfiguration();
            try {
                configuration.load(examFile);
                examMap.put(fileName.substring(0, fileName.indexOf('.')), new CertificateExam(configuration));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void disable() throws Exception {

    }

    public CertificateExam getExam(String name){
        return examMap.get(name);
    }

    public Set<String> getExamNames(){
        return Collections.unmodifiableSet(examMap.keySet());
    }

    public List<ExamMediator.ExamPair> getExams() {
        return Collections.unmodifiableList(examMap.entrySet().stream()
                .map(entry -> new ExamMediator.ExamPair(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList()));
    }
}
