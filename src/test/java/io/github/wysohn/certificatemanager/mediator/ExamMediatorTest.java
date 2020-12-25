package io.github.wysohn.certificatemanager.mediator;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.Provides;
import io.github.wysohn.certificatemanager.manager.CertificateExamManager;
import io.github.wysohn.certificatemanager.manager.QuestionManager;
import io.github.wysohn.certificatemanager.manager.UserManager;
import io.github.wysohn.certificatemanager.objects.CertificateExam;
import io.github.wysohn.certificatemanager.objects.User;
import io.github.wysohn.rapidframework3.core.language.ManagerLanguage;
import io.github.wysohn.rapidframework3.core.main.PluginMain;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class ExamMediatorTest {

    private List<Module> moduleList = new LinkedList<>();

    private CertificateExamManager mockExamManager;
    private QuestionManager mockQuestionManager;
    private UserManager mockUserManager;
    private PluginMain mockMain;
    private ExamMediator examMediator;
    private ManagerLanguage mockLang;

    @Before
    public void init() throws Exception {
        mockMain = mock(PluginMain.class);
        mockLang = mock(ManagerLanguage.class);

        mockExamManager = mock(CertificateExamManager.class);
        mockQuestionManager = mock(QuestionManager.class);
        mockUserManager = mock(UserManager.class);

        when(mockMain.getManager(CertificateExamManager.class)).thenReturn(Optional.of(mockExamManager));
        when(mockMain.getManager(QuestionManager.class)).thenReturn(Optional.of(mockQuestionManager));
        when(mockMain.getManager(UserManager.class)).thenReturn(Optional.of(mockUserManager));

        moduleList.add(new AbstractModule() {
            @Provides
            PluginMain pluginMain(){
                return mockMain;
            }

            @Provides
            ManagerLanguage managerLanguage(){
                return mockLang;
            }

            @Provides
            CertificateExamManager examManager(){
                return mockExamManager;
            }

            @Provides
            QuestionManager questionManager(){
                return mockQuestionManager;
            }

            @Provides
            UserManager userManager(){
                return mockUserManager;
            }
        });

        examMediator = Guice.createInjector(moduleList).getInstance(ExamMediator.class);
        examMediator.enable();
    }

    @Test
    public void getCertificateNames() {

    }

    @Test
    public void getCertificateExams() {

    }

    @Test
    public void missingPrerequisites() {
        User mockUser = mock(User.class);
        Set<String> missing = examMediator.missingPrerequisites(mockUser, "abc");
        assertNull(missing);
    }

    @Test
    public void missingPrerequisites2() {
        User mockUser = mock(User.class);
        CertificateExam mockExam = mock(CertificateExam.class);

        when(mockExamManager.getExam(eq("abc"))).thenReturn(mockExam);
        when(mockUser.containsCertificate(anyString())).thenReturn(false);
        when(mockExam.getPreRequisites()).thenReturn(Arrays.asList("aaa", "bbb", "ccc"));

        Set<String> missing = examMediator.missingPrerequisites(mockUser, "abc");
        assertNotNull(missing);
        assertEquals(3, missing.size());
    }

    @Test
    public void takeExam() {
        User mockUser = mock(User.class);

        ExamMediator.ExamResultHandle mockHandle = mock(ExamMediator.ExamResultHandle.class);
        examMediator.takeExam(mockUser, "abc", mockHandle);

        verify(mockHandle).accept(ExamMediator.ExamResult.NOT_EXIST, "abc");
    }

    @Test
    public void takeExam2() {
        User mockUser = mock(User.class);
        CertificateExam mockExam = mock(CertificateExam.class);

        when(mockExamManager.getExam(eq("abc"))).thenReturn(mockExam);
        when(mockUser.containsCertificate(eq("abc"))).thenReturn(true);
        long expire = System.currentTimeMillis() * 2;
        when(mockUser.getExpireDate(eq("abc"))).thenReturn(expire);

        ExamMediator.ExamResultHandle mockHandle = mock(ExamMediator.ExamResultHandle.class);
        examMediator.takeExam(mockUser, "abc", mockHandle);

        verify(mockHandle).accept(ExamMediator.ExamResult.DUPLICATE, expire);
    }

    @Test
    public void takeExam2_1() {
        User mockUser = mock(User.class);
        CertificateExam mockExam = mock(CertificateExam.class);

        when(mockExamManager.getExam(eq("abc"))).thenReturn(mockExam);
        when(mockUser.containsCertificate(eq("abc"))).thenReturn(true);
        long expire = -1L;
        when(mockUser.getExpireDate(eq("abc"))).thenReturn(expire);

        ExamMediator.ExamResultHandle mockHandle = mock(ExamMediator.ExamResultHandle.class);
        examMediator.takeExam(mockUser, "abc", mockHandle);

        verify(mockHandle).accept(ExamMediator.ExamResult.DUPLICATE, expire);
    }

    @Test
    public void takeExam3() {
        User mockUser = mock(User.class);
        CertificateExam mockExam = mock(CertificateExam.class);

        when(mockExamManager.getExam(eq("abc"))).thenReturn(mockExam);
        when(mockUser.containsCertificate(eq("abc"))).thenReturn(true);
        long dueDate = System.currentTimeMillis() * 2;
        when(mockUser.getRetakeDue(eq("abc"))).thenReturn(dueDate);

        ExamMediator.ExamResultHandle mockHandle = mock(ExamMediator.ExamResultHandle.class);
        examMediator.takeExam(mockUser, "abc", mockHandle);

        verify(mockHandle).accept(ExamMediator.ExamResult.RETAKE_DELAY, dueDate);
    }

    @Test
    public void takeExam4() {
        User mockUser = mock(User.class);
        CertificateExam mockExam = mock(CertificateExam.class);

        when(mockExamManager.getExam(eq("abc"))).thenReturn(mockExam);
        when(mockUser.containsCertificate(eq("abc"))).thenReturn(true);
        long dueDate = -1;
        when(mockUser.getRetakeDue(eq("abc"))).thenReturn(dueDate);

        ExamMediator.ExamResultHandle mockHandle = mock(ExamMediator.ExamResultHandle.class);
        examMediator.takeExam(mockUser, "abc", mockHandle);

        verify(mockHandle).accept(ExamMediator.ExamResult.NO_QUESTIONS);
    }
}