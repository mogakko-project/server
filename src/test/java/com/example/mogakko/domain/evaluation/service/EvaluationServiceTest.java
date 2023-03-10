package com.example.mogakko.domain.evaluation.service;

import com.example.mogakko.domain.comment.domain.Comment;
import com.example.mogakko.domain.evaluation.domain.Evaluation;
import com.example.mogakko.domain.evaluation.dto.AddEvaluationRequestDTO;
import com.example.mogakko.domain.evaluation.dto.ContentDTO;
import com.example.mogakko.domain.evaluation.dto.EvaluationDTO;
import com.example.mogakko.domain.evaluation.repository.EvaluationRepository;
import com.example.mogakko.domain.group.domain.Group;
import com.example.mogakko.domain.group.domain.GroupUser;
import com.example.mogakko.domain.group.enums.GroupStatus;
import com.example.mogakko.domain.group.repository.GroupRepository;
import com.example.mogakko.domain.group.repository.GroupUserRepository;
import com.example.mogakko.domain.user.domain.User;
import com.example.mogakko.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource("/application-test.properties")
@SpringBootTest
@Transactional
class EvaluationServiceTest {

    @Autowired
    private EvaluationService evaluationService;

    @Autowired
    private EvaluationRepository evaluationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupUserRepository groupUserRepository;

    private User user1, user2;
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
    }

    private Group createGroup(User user1, User user2) {
        Group group1 = new Group();
        group1.setGroupStatus(GroupStatus.END_GROUP);
        return group1;
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

    private Evaluation createEvaluation(User evaluatedUser, User evaluatingUser, String content) {
        Evaluation evaluation1 = new Evaluation();
        evaluation1.setEvaluatedUser(evaluatedUser);
        evaluation1.setEvaluatingUser(evaluatingUser);
        evaluation1.setContent(content);
        return evaluation1;
    }


    @Test
    @DisplayName("?????? ??????")
    void saveEvaluationTest() {
        AddEvaluationRequestDTO addEvaluationRequestDTO = new AddEvaluationRequestDTO();
        addEvaluationRequestDTO.setEvaluatingUserId(user1.getId());
        addEvaluationRequestDTO.setContent("user1??? user2??? ??????");

        EvaluationDTO evaluationDTO = evaluationService.saveEvaluation(group.getId(), user2.getId(), addEvaluationRequestDTO);

        Optional<Evaluation> optionalEvaluation = evaluationRepository.findById(evaluationDTO.getEvaluationId());
        assertThat(optionalEvaluation).as("????????? ????????? ??????????????????.")
                .isNotEmpty();
    }

    @Test
    @DisplayName("????????? ?????? ?????? ??????")
    void findEvaluationsOfUserTEst() {
        List<EvaluationDTO> evaluations = evaluationService.findEvaluationsOfUser(user1.getId());

        assertThat(evaluations).as("user1??? 1?????? ????????? ?????? ????????? ??????.")
                .hasSize(1);
    }

    @Test
    @DisplayName("?????? ??????")
    void deleteByIdTest() {
        evaluationService.deleteById(evaluation.getId());

        Optional<Evaluation> optionalEvaluation = evaluationRepository.findById(evaluation.getId());

        assertThat(optionalEvaluation.isEmpty()).as("????????? ????????? ???????????? null????????? ??????.")
                .isTrue();
    }

    @Test
    @DisplayName("?????? ??????")
    void updateEvaluationTest() {
        final String updateContent = "????????? ??????";
        ContentDTO contentDTO = new ContentDTO();
        contentDTO.setContent(updateContent);
        evaluationService.updateEvaluation(evaluation.getId(), contentDTO);

        Optional<Evaluation> optionalEvaluation = evaluationRepository.findById(evaluation.getId());
        assertThat(optionalEvaluation).as("????????? ???????????? ??????.")
                .isNotEmpty();
        Evaluation updatedEvaluation = optionalEvaluation.get();

        assertThat(updatedEvaluation.getContent()).as("????????? ????????? ???????????? ????????? ??????.")
                .isEqualTo(updateContent);

    }
}