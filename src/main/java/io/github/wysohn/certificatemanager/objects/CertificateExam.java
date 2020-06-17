package io.github.wysohn.certificatemanager.objects;

import io.github.wysohn.certificatemanager.objects.reward.CommandReward;
import io.github.wysohn.certificatemanager.objects.reward.Reward;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class CertificateExam {
    public static final String DEFAULT = "default";

    private Map<String, String> title = new HashMap<>();
    private Map<String, String> desc = new HashMap<>();
    private int numQuestions = 10;
    private double passingGrade = 0.7;
    private boolean showFeedback = true;
    private List<String> preRequisites = new ArrayList<>();
    private int expireAfterDays = 0;
    private boolean retake = true;
    private int retakeAfterSeconds = 60;
    private List<Reward> rewards = new ArrayList<>();

    public CertificateExam(ConfigurationSection section) {
        title.put(DEFAULT, "None");
        desc.put(DEFAULT, "None");

        Optional.ofNullable(section.getConfigurationSection("title"))
                .map(sec -> sec.getValues(false))
                .ifPresent(map -> map.forEach((key, val) -> title.put(key, (String) val)));

        Optional.ofNullable(section.getConfigurationSection("desc"))
                .map(sec -> sec.getValues(false))
                .ifPresent(map -> map.forEach((key, val) -> desc.put(key, (String) val)));

        numQuestions = section.getInt("numQuestions", 10);
        passingGrade = section.getDouble("passingGrade", 0.7);
        showFeedback = section.getBoolean("showFeedback", true);
        preRequisites = section.getStringList("preRequisites");
        expireAfterDays = section.getInt("expireAfterDays", 0);
        retake = section.getBoolean("retake", true);
        retakeAfterSeconds = section.getInt("retakeAfterSeconds", 60);

        rewards.add(new CommandReward(section.getStringList("rewards.commands")));
    }

    public String getTitle(Locale locale) {
        return getTitle(locale.getLanguage());
    }

    public String getTitle(String localeCode) {
        return title.getOrDefault(localeCode, title.get(DEFAULT));
    }

    public String getDesc(Locale locale) {
        return getDesc(locale.getLanguage());
    }

    public String getDesc(String localeCode) {
        return desc.getOrDefault(localeCode, title.get(DEFAULT));
    }

    public int getNumQuestions() {
        return numQuestions;
    }

    public double getPassingGrade() {
        return passingGrade;
    }

    public boolean isShowFeedback() {
        return showFeedback;
    }

    public List<String> getPreRequisites() {
        return Collections.unmodifiableList(preRequisites);
    }

    public int getExpireAfterDays() {
        return expireAfterDays;
    }

    public boolean isRetake() {
        return retake;
    }

    public int getRetakeAfterSeconds() {
        return retakeAfterSeconds;
    }

    public List<Reward> getRewards() {
        return Collections.unmodifiableList(rewards);
    }
}
