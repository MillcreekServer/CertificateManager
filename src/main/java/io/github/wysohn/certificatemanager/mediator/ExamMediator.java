package io.github.wysohn.certificatemanager.mediator;

import io.github.wysohn.certificatemanager.main.CertificateManagerLangs;
import io.github.wysohn.certificatemanager.manager.CertificateExamManager;
import io.github.wysohn.certificatemanager.manager.QuestionManager;
import io.github.wysohn.certificatemanager.objects.CertificateExam;
import io.github.wysohn.certificatemanager.objects.Question;
import io.github.wysohn.certificatemanager.objects.User;
import io.github.wysohn.rapidframework2.bukkit.utils.conversation.ConversationBuilder;
import io.github.wysohn.rapidframework2.core.main.PluginMain;
import io.github.wysohn.rapidframework2.core.manager.common.message.MessageBuilder;
import org.bukkit.conversations.Conversation;
import util.Sampling;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class ExamMediator extends PluginMain.Mediator {
    public static final long SECONDS = 1000L;
    public static final long DAYS = SECONDS * 60L * 60L * 24L;

    public static final String ANSWER_INDEX = "answerIndex";
    public static final String NUM_CORRECT = "numCorrect";
    public static final String FEEDBACKS = "Feedbacks";
    public static final String QUESTION = "question";
    public static final String ANSWERS = "answers";

    private CertificateExamManager certificateExamManager;
    private QuestionManager questionManager;

    @Override
    public void enable() throws Exception {
        certificateExamManager = main().getManager(CertificateExamManager.class).get();
        questionManager = main().getManager(QuestionManager.class).get();
    }

    @Override
    public void load() throws Exception {

    }

    @Override
    public void disable() throws Exception {

    }

    public Set<String> getCertificateNames(){
        return certificateExamManager.getExamNames();
    }

    public List<ExamPair> getCertificateExams(){
        return certificateExamManager.getExams();
    }

    public static class ExamPair{
        public final String key;
        public final CertificateExam exam;

        public ExamPair(String key, CertificateExam exam) {
            this.key = key;
            this.exam = exam;
        }
    }

    /**
     *
     * @param examTaker
     * @param certificateName
     * @return null if the certificate with given name doesn't exist; Set of prerequisites otherwise. Set can be empty
     * if all requirements are met.
     */
    public Set<String> missingPrerequisites(User examTaker, String certificateName){
        CertificateExam certificateExam = certificateExamManager.getExam(certificateName);
        if(certificateExam == null) {
            return null;
        }

        return new HashSet<>(certificateExam.getPreRequisites());
    }

    /**
     * Start taking exam. This does not check if prerequisites are met.
     * Make sure to check first with {@link #missingPrerequisites(User, String)}
     * @param examTaker
     * @param certificateName
     * @param callback
     */
    public void takeExam(User examTaker, String certificateName, Consumer<ExamResult> callback){
        CertificateExam certificateExam = certificateExamManager.getExam(certificateName);
        if(certificateExam == null) {
            callback.accept(ExamResult.NOT_EXIST);
            return;
        }

        if(examTaker.containsCertificate(certificateName)){
            long expire = examTaker.getExpireDate(certificateName);
            if(expire > -1L && System.currentTimeMillis() < expire){
                callback.accept(ExamResult.DUPLICATE);
                return;
            } else {
                examTaker.setExpiration(certificateName, -1L);
            }
        }

        long retakeDue = examTaker.getRetakeDue(certificateName);
        if(retakeDue > -1L && System.currentTimeMillis() < retakeDue){
            callback.accept(ExamResult.RETAKE_DELAY);
            return;
        }

        List<Question> questions = questionManager.getQuestions(certificateName, examTaker.getLocale());
        if(questions.isEmpty()){
            callback.accept(ExamResult.NO_QUESTIONS);
            return;
        }

        ConversationBuilder builder = ConversationBuilder.of(main());

        // show welcome
        builder.doTask(context -> main().lang().sendMessage(examTaker, CertificateManagerLangs.CertificateExamManager_Welcome,
                ((sen, langman) -> langman.addString(certificateExam.getTitle(examTaker.getLocale()))
                        .addString(certificateExam.getDesc(examTaker.getLocale()))),
                true));
        builder.appendConfirm((context) -> {
        });

        // for each question
        int[] indices = Sampling.uniform(questions.size(), questions.size(), false);
        for(int qIndex = 1; qIndex <= questions.size(); qIndex++){
            Question question = questions.get(indices[qIndex - 1]);

            // show prompt
            builder.doTask((context) -> {
                String prompt = question.getQuestion();
                String[] answers = question.getAnswers();
                if(answers.length < 2)
                    throw new RuntimeException("Question "+prompt+" of "+certificateName+" must have at least 2 answers.");

                int[] answerIndices = Sampling.uniform(answers.length, answers.length, false);

                main().lang().sendRawMessage(examTaker, MessageBuilder.forMessage(prompt).build(), true);

                int visibleIndex = 1;
                for (int answerIndex : answerIndices) {
                    if(answerIndex == 0){ // first element is always answer
                        context.setSessionData(ANSWER_INDEX, visibleIndex);
                    }

                    main().lang().sendRawMessage(examTaker, MessageBuilder.forMessage((visibleIndex++) + answers[answerIndex])
                            .build(), true);
                }

                context.setSessionData(QUESTION, prompt);
                context.setSessionData(ANSWERS, IntStream.of(answerIndices)
                        .mapToObj(i -> answers[i])
                        .toArray(String[]::new));
            });

            // show list of answers and accept index
            int finalQIndex = qIndex;
            builder.appendInt((context, i) -> {
                int totalAnswers = question.getAnswers().length;
                if(i < 1 || i > totalAnswers)
                    return false;

                int answerIndex = (int) context.getSessionData(ANSWER_INDEX);
                if (answerIndex == i) {
                    int numCorrect = Optional.ofNullable(context.getSessionData(NUM_CORRECT))
                            .map(Integer.class::cast)
                            .orElse(0);
                    context.setSessionData(NUM_CORRECT, numCorrect + 1);
                }

                if(certificateExam.isShowFeedback()){
                    String prompt = (String) context.getSessionData(QUESTION);
                    String[] answers = (String[]) context.getSessionData(ANSWERS);

                    List<Runnable> feedbackList = Optional.ofNullable(context.getSessionData(FEEDBACKS))
                            .map(List.class::cast)
                            .orElseGet(ArrayList::new);

                    feedbackList.add(() -> {
                        main().lang().sendMessage(examTaker, CertificateManagerLangs.CertificateExamManager_Feedback_Question, (lan, man) ->
                                man.addInteger(finalQIndex).addString(prompt));
                        for (int k = 1; k <= answers.length; k++) {
                            int finalK = k;
                            main().lang().sendMessage(examTaker, CertificateManagerLangs.CertificateExamManager_Feedback_Answer, (lan, man) ->
                                    man.addInteger(finalK).addString(answers[finalK - 1]));
                        }
                    });

                    context.setSessionData(FEEDBACKS, feedbackList);
                }
                return true;
            });
        }

        // show result
        builder.doTask((context) -> {
            // reset retake timer
            if(certificateExam.isRetake() && certificateExam.getRetakeAfterSeconds() > 0){
                examTaker.setRetakeDue(certificateName, certificateExam.getRetakeAfterSeconds() * SECONDS);
            }

            // show feedbacks
            Optional.ofNullable(context.getSessionData(FEEDBACKS))
                    .ifPresent(o -> ((List<Runnable>) o).forEach(Runnable::run));

            // show final result
            int numCorrect = Optional.ofNullable(context.getSessionData(NUM_CORRECT))
                    .map(Integer.class::cast)
                    .orElse(0);
            double correctPct = (double) numCorrect / questions.size();

            if(correctPct >= certificateExam.getPassingGrade()){
                String resultParsed = main().lang().parseFirst(examTaker, CertificateManagerLangs.CertificateExamManager_Pass);

                main().lang().sendMessage(examTaker, CertificateManagerLangs.CertificateExamManager_Result, (sen, man) ->
                        man.addInteger(numCorrect).addInteger(questions.size())
                                .addDouble(certificateExam.getPassingGrade())
                                .addString(resultParsed), true);

                examTaker.addCertificate(certificateName);
                examTaker.setExpiration(certificateName, certificateExam.getExpireAfterDays() * DAYS);

                callback.accept(ExamResult.PASS);
            } else {
                String resultParsed = main().lang().parseFirst(examTaker, CertificateManagerLangs.CertificateExamManager_Fail);

                main().lang().sendMessage(examTaker, CertificateManagerLangs.CertificateExamManager_Result, (sen, man) ->
                        man.addInteger(numCorrect).addInteger(questions.size())
                                .addDouble(certificateExam.getPassingGrade())
                                .addString(resultParsed), true);

                callback.accept(ExamResult.FAIL);
            }
        });

        Conversation conversation = builder.build(examTaker.getSender());
        conversation.addConversationAbandonedListener(event -> callback.accept(ExamResult.ABANDONED));
        conversation.begin();
    }

    public enum ExamResult{
        NOT_EXIST, DUPLICATE, RETAKE_DELAY, NO_QUESTIONS, ABANDONED, PASS, FAIL;
    }
}
