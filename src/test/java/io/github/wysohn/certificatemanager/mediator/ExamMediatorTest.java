package io.github.wysohn.certificatemanager.mediator;

import io.github.wysohn.certificatemanager.manager.CertificateExamManager;
import io.github.wysohn.certificatemanager.manager.QuestionManager;
import io.github.wysohn.certificatemanager.manager.UserManager;
import io.github.wysohn.certificatemanager.objects.CertificateExam;
import io.github.wysohn.certificatemanager.objects.User;
import io.github.wysohn.rapidframework2.core.main.PluginMain;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class ExamMediatorTest {

    private CertificateExamManager mockExamManager;
    private QuestionManager mockQuestionManager;
    private UserManager mockUserManager;
    private PluginMain mockMain;
    private ExamMediator examMediator;

    @Before
    public void init() throws Exception {
        mockMain = mock(PluginMain.class);

        mockExamManager = mock(CertificateExamManager.class);
        mockQuestionManager = mock(QuestionManager.class);
        mockUserManager = mock(UserManager.class);

        examMediator = new ExamMediator();
        Whitebox.setInternalState(examMediator, "main", mockMain);

        when(mockMain.getManager(CertificateExamManager.class)).thenReturn(Optional.of(mockExamManager));
        when(mockMain.getManager(QuestionManager.class)).thenReturn(Optional.of(mockQuestionManager));
        when(mockMain.getManager(UserManager.class)).thenReturn(Optional.of(mockUserManager));

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