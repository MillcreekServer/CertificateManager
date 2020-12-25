package io.github.wysohn.certificatemanager.main;

import io.github.wysohn.certificatemanager.manager.CertificateExamManager;
import io.github.wysohn.certificatemanager.manager.QuestionManager;
import io.github.wysohn.certificatemanager.manager.UserManager;
import io.github.wysohn.certificatemanager.mediator.ExamMediator;
import io.github.wysohn.certificatemanager.objects.BukkitArgumentMappers;
import io.github.wysohn.certificatemanager.objects.CertificateExam;
import io.github.wysohn.certificatemanager.objects.User;
import io.github.wysohn.rapidframework3.bukkit.main.AbstractBukkitPlugin;
import io.github.wysohn.rapidframework3.core.command.ArgumentMappers;
import io.github.wysohn.rapidframework3.core.command.SubCommand;
import io.github.wysohn.rapidframework3.core.command.TabCompleters;
import io.github.wysohn.rapidframework3.core.inject.module.*;
import io.github.wysohn.rapidframework3.core.main.PluginMainBuilder;
import io.github.wysohn.rapidframework3.core.message.MessageBuilder;
import io.github.wysohn.rapidframework3.core.paging.ListWrapper;
import io.github.wysohn.rapidframework3.core.paging.Pagination;
import io.github.wysohn.rapidframework3.core.player.AbstractPlayerWrapper;
import io.github.wysohn.rapidframework3.interfaces.ICommandSender;
import io.github.wysohn.rapidframework3.interfaces.command.CommandAction;
import io.github.wysohn.rapidframework3.interfaces.command.ITabCompleter;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;

import java.lang.ref.Reference;
import java.util.*;
import java.util.stream.Collectors;

public class CertificateManager extends AbstractBukkitPlugin {
    public CertificateManager() {
    }

    CertificateManager(Server server) {
        super(server);
    }

    @Override
    protected void init(PluginMainBuilder pluginMainBuilder) {
        pluginMainBuilder.addModule(new LanguagesModule(CertificateManagerLangs.values()));
        pluginMainBuilder.addModule(new ManagerModule(
                CertificateExamManager.class,
                QuestionManager.class,
                UserManager.class
        ));
        pluginMainBuilder.addModule(new MediatorModule(
                ExamMediator.class
        ));
        pluginMainBuilder.addModule(new GsonSerializerModule());
        pluginMainBuilder.addModule(new TypeAsserterModule());
        //TODO and some other modules as your need...
    }

    @Override
    protected void registerCommands(List<SubCommand.Builder> list) {
        list.add(new SubCommand.Builder("list", -1)
                .withDescription(CertificateManagerLangs.Command_List_Desc)
                .addUsage(CertificateManagerLangs.Command_List_Usage)
                .addArgumentMapper(0, ArgumentMappers.INTEGER)
                .addTabCompleter(0, TabCompleters.hint("[page]"))
                .action((sender, args) -> {
                    int page = args.get(0)
                            .map(Integer.class::cast)
                            .map(p -> p - 1)
                            .orElse(0);

                    getMain().getMediator(ExamMediator.class).ifPresent(examMediator -> {
                        new Pagination<>(getMain().lang(),
                                ListWrapper.wrap(examMediator.getCertificateExams()),
                                6,
                                "Exams",
                                "/cer list").show(sender, page, (iCommandSender, pair, i) -> {
                            String key = pair.key;
                            CertificateExam certificateExam = pair.exam;

                            return MessageBuilder.forMessage("&e" + certificateExam.getTitle(sender.getLocale()))
                                    .withHoverShowText("&7" + certificateExam.getDesc(sender.getLocale()))
                                    .append(" ")
                                    .append("&d[ \u25b6 ]")
                                    .withHoverShowText("/cer take " + key)
                                    .withClickRunCommand("/cer take " + key)
                                    .build();
                        });
                    });

                    return true;
                }));
        
        list.add(new SubCommand.Builder("take", 1)
                .withDescription(CertificateManagerLangs.Command_Take_Desc)
                .addUsage(CertificateManagerLangs.Command_Take_Usage)
                .addArgumentMapper(0, ArgumentMappers.STRING)
                .addTabCompleter(0, getExamNameCompleter())
                .action(new CommandAction() {

                    private void handleResult(ICommandSender sender, ExamMediator.ExamResult examResult, Object... args) {
                        switch (examResult) {
                            case NOT_EXIST:
                                getMain().lang().sendMessage(sender, CertificateManagerLangs.Command_Take_NotExist, (sen, langman) ->
                                        langman.addString(String.valueOf(args[0])));
                                break;
                            case NO_QUESTIONS:
                                getMain().lang().sendMessage(sender, CertificateManagerLangs.Command_Take_NoQuestions);
                                break;
                            case DUPLICATE:
                                getMain().lang().sendMessage(sender, CertificateManagerLangs.Command_Take_Duplicate, (sen, langman) ->
                                        langman.addDate(new Date((long) args[0])));
                                break;
                            case RETAKE_DELAY:
                                getMain().lang().sendMessage(sender, CertificateManagerLangs.Command_Take_Delay, (sen, langman) ->
                                        langman.addDate(new Date((long) args[0])));
                                break;
                            case ABANDONED:
                                getMain().lang().sendMessage(sender, CertificateManagerLangs.Command_Take_Abandoned);
                                break;
                            case FAIL:
                                getMain().lang().sendMessage(sender, CertificateManagerLangs.Command_Take_Fail);
                                break;
                            case PASS:
                                getMain().lang().sendMessage(sender, CertificateManagerLangs.Command_Take_Success);
                                break;
                        }
                    }

                    @Override
                    public boolean execute(ICommandSender sender, SubCommand.Arguments args) {
                        String key = args.get(0)
                                .map(String.class::cast)
                                .orElse(null);
                        if (key == null)
                            return false;

                        getMain().getMediator(ExamMediator.class).ifPresent(examMediator -> {
                            getUser(sender).ifPresent(user -> {
                                Set<String> prerequisites = examMediator.missingPrerequisites(user, key);
                                if (prerequisites == null) {
                                    handleResult(sender, ExamMediator.ExamResult.NOT_EXIST, key);
                                    return;
                                }

                                if (prerequisites.size() > 0) {
                                    getMain().lang().sendMessage(sender, CertificateManagerLangs.Command_Take_Prerequisites, ((sen, langman) ->
                                            langman.addString(commaSeparate(prerequisites))));
                                    return;
                                }

                                examMediator.takeExam(user, key, (result, params) -> handleResult(sender, result, params));
                            });
                        });

                        return true;
                    }
                }));
        
        list.add(new SubCommand.Builder("reset", 2)
                .withDescription(CertificateManagerLangs.Command_Reset_Desc)
                .addUsage(CertificateManagerLangs.Command_Reset_Usage)
                .addArgumentMapper(0, BukkitArgumentMappers.OFFLINE_PLAYER)
                .addArgumentMapper(1, ArgumentMappers.STRING)
                .addTabCompleter(0, TabCompleters.PLAYER)
                .addTabCompleter(1, getExamNameCompleter())
                .action((sender, args) -> {
                    OfflinePlayer offp = args.get(0)
                            .map(OfflinePlayer.class::cast)
                            .orElse(null);
                    if (offp == null)
                        return false;

                    String key = args.get(1)
                            .map(String.class::cast)
                            .orElse(null);
                    if (key == null)
                        return false;

                    getMain().getMediator(ExamMediator.class).ifPresent(examMediator -> {
                        getUser(offp.getUniqueId()).ifPresent(user -> {
                            if (examMediator.deleteCertificate(user, key)) {
                                getMain().lang().sendMessage(sender, CertificateManagerLangs.Command_Reset_Deleted);
                            } else {
                                getMain().lang().sendMessage(sender, CertificateManagerLangs.Command_Reset_Failed, (sen, langman) ->
                                        langman.addString(key));
                            }
                        });
                    });

                    return true;
                }));
        
        list.add(new SubCommand.Builder("pass", 2)
                .withDescription(CertificateManagerLangs.Command_Pass_Desc)
                .addUsage(CertificateManagerLangs.Command_Pass_Usage)
                .addArgumentMapper(0, BukkitArgumentMappers.OFFLINE_PLAYER)
                .addArgumentMapper(1, ArgumentMappers.STRING)
                .addTabCompleter(0, TabCompleters.PLAYER)
                .addTabCompleter(1, getExamNameCompleter())
                .action((sender, args) -> {
                    OfflinePlayer offp = args.get(0)
                            .map(OfflinePlayer.class::cast)
                            .orElse(null);
                    if (offp == null)
                        return false;

                    String key = args.get(1)
                            .map(String.class::cast)
                            .orElse(null);
                    if (key == null)
                        return false;

                    getMain().getMediator(ExamMediator.class).ifPresent(examMediator -> {
                        getUser(offp.getUniqueId()).ifPresent(user -> {
                            if (examMediator.addCertificate(user, key)) {
                                getMain().lang().sendMessage(sender, CertificateManagerLangs.Command_Pass_Added);
                            } else {
                                getMain().lang().sendMessage(sender, CertificateManagerLangs.Command_Pass_Failed, (sen, langman) ->
                                        langman.addString(key));
                            }
                        });
                    });

                    return true;
                }));
    }

    private ITabCompleter getExamNameCompleter() {
        return new ITabCompleter() {
            @Override
            public List<String> getCandidates(String part) {
                return getExamNames().stream()
                        .filter(name -> name.startsWith(part))
                        .collect(Collectors.toList());
            }

            @Override
            public List<String> getHint() {
                return new ArrayList<>(getExamNames());
            }
        };
    }

    private Set<String> getExamNames() {
        return getMain().getManager(CertificateExamManager.class)
                .map(CertificateExamManager::getExamNames)
                .orElseGet(HashSet::new);
    }

    private String commaSeparate(Collection<String> stringCollection) {
        return stringCollection.stream()
                .collect(Collectors.joining("&8, &2", "&2", ""));
    }

    private Optional<User> getUser(UUID uniqueId) {
        return getMain().getManager(UserManager.class)
                .flatMap(userManager -> userManager.get(uniqueId))
                .map(Reference::get);
    }

    private Optional<User> getUser(ICommandSender sender) {
        return getUser(sender.getUuid());
    }

    @Override
    protected Optional<? extends AbstractPlayerWrapper> getPlayerWrapper(UUID uuid) {
        return getUser(uuid);
    }
}
