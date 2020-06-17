package io.github.wysohn.certificatemanager.main;

import io.github.wysohn.certificatemanager.manager.CertificateExamManager;
import io.github.wysohn.certificatemanager.manager.QuestionManager;
import io.github.wysohn.certificatemanager.manager.UserManager;
import io.github.wysohn.certificatemanager.mediator.ExamMediator;
import io.github.wysohn.certificatemanager.objects.CertificateExam;
import io.github.wysohn.certificatemanager.objects.User;
import io.github.wysohn.rapidframework2.bukkit.main.AbstractBukkitPlugin;
import io.github.wysohn.rapidframework2.bukkit.main.BukkitPluginBridge;
import io.github.wysohn.rapidframework2.core.interfaces.entity.ICommandSender;
import io.github.wysohn.rapidframework2.core.interfaces.plugin.IPluginManager;
import io.github.wysohn.rapidframework2.core.main.PluginMain;
import io.github.wysohn.rapidframework2.core.manager.command.ArgumentMapper;
import io.github.wysohn.rapidframework2.core.manager.command.SubCommand;
import io.github.wysohn.rapidframework2.core.manager.common.message.MessageBuilder;
import io.github.wysohn.rapidframework2.core.manager.lang.page.ListWrapper;
import io.github.wysohn.rapidframework2.core.manager.lang.page.Pagination;

import java.io.File;
import java.lang.ref.Reference;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CertificateManagerBridge extends BukkitPluginBridge {
    public CertificateManagerBridge(AbstractBukkitPlugin bukkit) {
        super(bukkit);
    }

    public CertificateManagerBridge(String pluginName,
                                    String pluginDescription,
                                    String mainCommand,
                                    String adminPermission,
                                    Logger logger,
                                    File dataFolder,
                                    IPluginManager iPluginManager,
                                    AbstractBukkitPlugin bukkit) {
        super(pluginName, pluginDescription, mainCommand, adminPermission, logger, dataFolder, iPluginManager, bukkit);
    }

    @Override
    protected PluginMain init(PluginMain.Builder builder) {
        return builder
                .withManagers(new CertificateExamManager(PluginMain.Manager.NORM_PRIORITY))
                .withManagers(new QuestionManager(PluginMain.Manager.NORM_PRIORITY))
                .withManagers(new UserManager(PluginMain.Manager.NORM_PRIORITY))
                .withMediators(new ExamMediator())
                .addLangs(CertificateManagerLangs.values())
                .build();
    }

    @Override
    protected void registerCommands(List<SubCommand> list) {
        list.add(new SubCommand.Builder(getMain(), "list", -1)
                .withDescription(CertificateManagerLangs.Command_List_Desc)
                .addUsage(CertificateManagerLangs.Command_List_Usage)
                .addArgumentMapper(0, ArgumentMapper.INTEGER)
                .action((sender, args) -> {
                    int page = args.get(0)
                            .map(Integer.class::cast)
                            .map(p -> p - 1)
                            .orElse(0);

                    getMain().getMediator(ExamMediator.class).ifPresent(examMediator -> {
                        new Pagination<>(getMain(),
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
                })
                .create());

        list.add(new SubCommand.Builder(getMain(), "take", 1)
                .withDescription(CertificateManagerLangs.Command_Take_Desc)
                .addUsage(CertificateManagerLangs.Command_Take_Usage)
                .addArgumentMapper(0, ArgumentMapper.STRING)
                .action((sender, args) -> {
                    String key = args.get(0)
                            .map(String.class::cast)
                            .orElse(null);
                    if(key == null)
                        return false;

                    getMain().getMediator(ExamMediator.class).ifPresent(examMediator -> {
                        getUser(sender).ifPresent(user -> {
                            Set<String> prerequisites = examMediator.missingPrerequisites(user, key);
                            if(prerequisites.size() > 0){
                                getMain().lang().sendMessage(sender, CertificateManagerLangs.Command_Take_Prerequisites, ((sen, langman) ->
                                        langman.addString(commaSeparate(prerequisites))));
                                return;
                            }

                            examMediator.takeExam(user, key, (examResult -> {
                                switch (examResult){
                                    case NOT_EXIST:
                                        break;
                                    case NO_QUESTIONS:
                                        break;
                                    case DUPLICATE:
                                        break;
                                    case RETAKE_DELAY:
                                        break;
                                    case ABANDONED:
                                        break;
                                    case FAIL:
                                        break;
                                    case PASS:
                                        break;
                                }
                            }));
                        });
                    });

                    return true;
                })
                .create());
    }

    private String commaSeparate(Collection<String> stringCollection) {
        return stringCollection.stream()
                .collect(Collectors.joining("&8, &2", "&2", ""));
    }

    private Optional<User> getUser(ICommandSender sender) {
        return getMain().getManager(UserManager.class)
                .flatMap(userManager -> userManager.get(sender.getUuid()))
                .map(Reference::get);
    }
}
