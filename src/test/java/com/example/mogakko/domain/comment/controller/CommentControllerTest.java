package com.example.mogakko.domain.comment.controller;

import com.example.mogakko.domain.comment.domain.Comment;
import com.example.mogakko.domain.comment.dto.CommentRequestDTO;
import com.example.mogakko.domain.comment.dto.UpdateCommentDTO;
import com.example.mogakko.domain.comment.repository.CommentRepository;
import com.example.mogakko.domain.post.domain.Post;
import com.example.mogakko.domain.post.domain.Project;
import com.example.mogakko.domain.post.repository.PostRepository;
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
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.internal.bytebuddy.matcher.ElementMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource("/application-test.properties")
@AutoConfigureMockMvc
@SpringBootTest
@Transactional
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    protected MockHttpSession session;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private Post post;
    private Comment comment;

    @BeforeEach
    void setupDatabase() throws Exception {
        user = userRepository.save(createUser());
        post = postRepository.save(createPost());
        comment = commentRepository.save(createComment(post, user, null, "????????????1"));
        session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_USER, user.getId());
    }

    private User createUser() {
        User user = new User();
        user.setUsername("qwer1234");
        user.setPassword("qwer1234!");
        user.setNickname("??????");
        user.setOneLineIntroduction("???????????????.");
        user.setPhoneNumber("01012341234");
        user.setGithubAddress("qwer.github.com");
        user.setPicture("img");
        user.setAdmin(false);
        return user;
    }

    private Post createPost() {
        Project project = new Project();
        project.setUser(user);
        project.setTitle("??????");
        project.setContent("??????");
        return project;
    }

    private Comment createComment(Post post, User user, Comment root, String content) {
        Comment comment = new Comment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setRoot(root);
        comment.setContent(content);
        return comment;
    }

    private CommentRequestDTO createCommentRequestDTO(Long rootCommentId) {
        CommentRequestDTO commentRequestDTO = new CommentRequestDTO();
        commentRequestDTO.setPostId(post.getId());
        commentRequestDTO.setUserId(user.getId());
        commentRequestDTO.setContent("???????????? ????????????.");
        commentRequestDTO.setRootCommentId(rootCommentId);
        return commentRequestDTO;
    }

    @Test
    @DisplayName("???????????? ?????? ??????")
    void addCommentHttpRequest() throws Exception {
        CommentRequestDTO commentRequestDTO = createCommentRequestDTO(null);

        mockMvc.perform(post("/api/comments")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.postId").value(post.getId()))
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andExpect(jsonPath("$.content").value("???????????? ????????????."))
                .andExpect(jsonPath("$.rootCommentId").doesNotExist());
    }

    @Test
    @DisplayName("??????????????? ????????? ??????????????? ???????????? ????????? ????????? ????????????.")
    void addCommentWithANonValidRootCommentHttpRequest() throws Exception {
        CommentRequestDTO commentRequestDTO = createCommentRequestDTO(-1L);

        mockMvc.perform(post("/api/comments")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequestDTO)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.code").value("COMMENT-002"))
                .andExpect(jsonPath("$.message").value("???????????? ?????? ?????????????????????."));
    }

    @Test
    @DisplayName("??????????????? ????????? ??????????????? ?????? ???????????? ????????? ????????? ????????? ????????????.")
    void addCommentWithARootCommentNotBelongingToPostHttpRequest() throws Exception {
        Post anotherPost = postRepository.save(createPost());
        Comment anotherComment = commentRepository.save(createComment(anotherPost, user, null, "?????? ???????????? ????????? ??????"));

        CommentRequestDTO commentRequestDTO = createCommentRequestDTO(anotherComment.getId());

        mockMvc.perform(post("/api/comments")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequestDTO)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.code").value("COMMENT-003"))
                .andExpect(jsonPath("$.message").value("?????? ???????????? ????????? ??????????????? ????????????."));
    }

    @Test
    @DisplayName("??????????????? ????????? ??????????????? ?????? ????????? ??????????????? ????????? ????????? ????????????.")
    void addCommentWithARootCommentHavingAnotherRootCommentHttpRequest() throws Exception {
        Comment hasAnotherRootComment = commentRepository.save(createComment(post, user, comment, "?????? ????????? ??????????????? ?????? ??????"));

        CommentRequestDTO commentRequestDTO = createCommentRequestDTO(hasAnotherRootComment.getId());

        mockMvc.perform(post("/api/comments")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequestDTO)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.code").value("COMMENT-004"))
                .andExpect(jsonPath("$.message").value("??????????????? ?????? ??????????????? ????????????."));
    }

    @Test
    @DisplayName("???????????? ?????? ????????? ????????????.")
    void getCommentsOfPostHttpRequest() throws Exception {
        mockMvc.perform(get("/api/posts/{postId}/comments", post.getId())
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].content").value("????????????1"))
                .andExpect(jsonPath("$[0].rootCommentId").doesNotExist());
    }

    @Test
    @DisplayName("?????? ??????")
    void deleteCommentHttpRequest() throws Exception {
        mockMvc.perform(delete("/api/comments/{commentId}", comment.getId())
                        .session(session))
                .andExpect(status().isOk());

        assertThat(commentRepository.findById(comment.getId()))
                .isEmpty();
    }

    @Test
    @DisplayName("?????? ??????")
    void updateCommentHttpRequest() throws Exception {
        UpdateCommentDTO updateCommentDTO = new UpdateCommentDTO();
        updateCommentDTO.setContent("????????? ??????");

        mockMvc.perform(patch("/api/comments/{commentId}", comment.getId())
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateCommentDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.postId").value(post.getId()))
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andExpect(jsonPath("$.content").value("????????? ??????"))
                .andExpect(jsonPath("$.rootCommentId").doesNotExist());
    }
}