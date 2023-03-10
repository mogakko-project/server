package com.example.mogakko.domain.evaluation.controller;

import com.example.mogakko.domain.evaluation.domain.Evaluation;
import com.example.mogakko.domain.evaluation.dto.AddEvaluationRequestDTO;
import com.example.mogakko.domain.evaluation.dto.ContentDTO;
import com.example.mogakko.domain.evaluation.repository.EvaluationRepository;
import com.example.mogakko.domain.group.domain.Group;
import com.example.mogakko.domain.group.domain.GroupUser;
import com.example.mogakko.domain.group.enums.GroupStatus;
import com.example.mogakko.domain.group.repository.GroupRepository;
import com.example.mogakko.domain.group.repository.GroupUserRepository;
import com.example.mogakko.domain.user.controller.SessionConst;
import com.example.mogakko.domain.user.domain.User;
import com.example.mogakko.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource("/application-test.properties")
@AutoConfigureMockMvc
@SpringBootTest
@Transactional
class EvaluationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    protected MockHttpSession session;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private EvaluationRepository evaluationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupUserRepository groupUserRepository;

    private User user1, user2;  // 1: ???????????? ??????, 2: ???????????? ??????
    private Evaluation evaluation;
    private Group group;

    @BeforeEach
    void setupDatabase() {
        user1 = userRepository.save(createUser("qwer1234", "qwer1234!", "??????", "???????????????.", "01012341234", "qwer.github.com", "img1"));
        user2 = userRepository.save(createUser("asdf1234", "asdf1234!", "??????", "???????????????.", "01012341235", "asdf.github.com", "img2"));
        evaluation = evaluationRepository.save(createEvaluation(user1, user2, "user2??? user1??? ??????"));
        group = groupRepository.save(createGroup(user1, user2));
        groupUserRepository.save(GroupUser.createGroupUser(group, user1));
        groupUserRepository.save(GroupUser.createGroupUser(group, user2));
        session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_USER, user2.getId());
    }

    private User createUser(String username, String password, String nickname, String oneLineIntroduction, String phoneNumber, String githubAddress, String picture) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setNickname(nickname);
        user.setOneLineIntroduction(oneLineIntroduction);
        user.setPhoneNumber(phoneNumber);
        user.setGithubAddress(githubAddress);
        user.setPicture(picture);
        user.setAdmin(false);
        return user;
    }

    private Group createGroup(User user1, User user2) {
        Group group1 = new Group();
        group1.setGroupStatus(GroupStatus.END_GROUP);
        return group1;
    }

    private Evaluation createEvaluation(User evaluatedUser, User evaluatingUser, String content) {
        Evaluation evaluation1 = new Evaluation();
        evaluation1.setEvaluatedUser(evaluatedUser);
        evaluation1.setEvaluatingUser(evaluatingUser);
        evaluation1.setContent(content);
        return evaluation1;
    }


    @Test
    @DisplayName("?????? ??????")
    void addEvaluationHttpRequest() throws Exception {
        final String content = "user1??? user1??? ??????";
        AddEvaluationRequestDTO addEvaluationRequestDTO = new AddEvaluationRequestDTO();
        addEvaluationRequestDTO.setEvaluatingUserId(user2.getId());
        addEvaluationRequestDTO.setContent(content);

        mockMvc.perform(post("/api/groups/{groupId}/users/{userId}/evaluations", group.getId(), user1.getId())
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addEvaluationRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.evaluatedUserId").value(user1.getId()))
                .andExpect(jsonPath("$.evaluatingUserId").value(user2.getId()))
                .andExpect(jsonPath("$.evaluatingUserNickname").value(user2.getNickname()))
                .andExpect(jsonPath("$.content").value(content));
    }

    @Test
    @DisplayName("????????? ?????? ?????? ??????")
    void getEvaluationHttpRequest() throws Exception {
        mockMvc.perform(get("/api/users/{userId}/evaluations", user1.getId())
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].evaluationId").value(evaluation.getId()))
                .andExpect(jsonPath("$[0].evaluatedUserId").value(user1.getId()))
                .andExpect(jsonPath("$[0].evaluatingUserId").value(user2.getId()))
                .andExpect(jsonPath("$[0].evaluatingUserNickname").value(user2.getNickname()))
                .andExpect(jsonPath("$[0].content").value("user2??? user1??? ??????"));
    }

    @Test
    @DisplayName("?????? ??????")
    void deleteEvaluationHttpRequest() throws Exception {
        mockMvc.perform(delete("/api/evaluations/{evaluationId}", evaluation.getId())
                        .session(session))
                .andExpect(status().isOk());

        assertThat(evaluationRepository.findById(evaluation.getId()))
                .isEmpty();
    }

    @Test
    @DisplayName("?????? ??????")
    void updateEvaluationHttpRequest() throws Exception {
        final String content = "????????? ??????";

        ContentDTO contentDTO = new ContentDTO();
        contentDTO.setContent(content);

        mockMvc.perform(patch("/api/evaluations/{evaluationId}", evaluation.getId())
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contentDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.evaluationId").value(evaluation.getId()))
                .andExpect(jsonPath("$.evaluatedUserId").value(user1.getId()))
                .andExpect(jsonPath("$.evaluatingUserId").value(user2.getId()))
                .andExpect(jsonPath("$.evaluatingUserNickname").value(user2.getNickname()))
                .andExpect(jsonPath("$.content").value(content));
    }
}