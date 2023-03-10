package com.example.mogakko.domain.group.service;

import com.example.mogakko.domain.comment.exception.RootCommentHasAnotherRootCommentException;
import com.example.mogakko.domain.group.domain.Group;
import com.example.mogakko.domain.group.domain.GroupUser;
import com.example.mogakko.domain.group.dto.*;
import com.example.mogakko.domain.group.enums.GroupStatus;
import com.example.mogakko.domain.group.exception.IsNotGroupMasterException;
import com.example.mogakko.domain.group.repository.GroupRepository;
import com.example.mogakko.domain.group.repository.GroupUserRepository;
import com.example.mogakko.domain.post.domain.Post;
import com.example.mogakko.domain.post.enums.Type;
import com.example.mogakko.domain.post.repository.PostRepository;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource("/application-test.properties")
@SpringBootTest
@Transactional
class GroupServiceTest {
    
    @Autowired
    private GroupService groupService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupUserRepository groupUserRepository;
    
    @Autowired
    private PostRepository postRepository;
    
    private Group group;
    private User user1, user2;
    private Post post;
    
    @BeforeEach
    void setupDatabase() {
        user1 = userRepository.save(createUser("qwer1234", "qwer1234!", "??????", "???????????????.", "01012341234", "qwer.github.com", "img1"));
        user2 = userRepository.save(createUser("asdf1234", "asdf1234!", "??????", "???????????????.", "01012341235", "asdf.github.com", "img2"));
        group = groupRepository.save(createGroup());

        GroupUser groupUser1 = GroupUser.createGroupUser(group, user1);
        groupUser1.setIsMaster(true);
        GroupUser saveGroupUser1 = groupUserRepository.save(groupUser1);
        GroupUser saveGroupUser2 = groupUserRepository.save(GroupUser.createGroupUser(group, user2));
        user1.getGroupUsers().add(saveGroupUser1);
        user2.getGroupUsers().add(saveGroupUser2);
        group.getGroupUsers().add(saveGroupUser1);
        group.getGroupUsers().add(saveGroupUser2);

        post = postRepository.save(createPost("PROJECT", user1, group, "??????", "??????"));
        post.setGroup(group);
    }

    private Post createPost(String type, User user, Group group, String title, String content) {
        Post newPost = new Post();
        newPost.setDtype(type);
        newPost.setUser(user);
        newPost.setGroup(group);
        newPost.setTitle(title);
        newPost.setContent(content);
        return newPost;
    }

    private Group createGroup() {
        Group newGroup = new Group();
        newGroup.setGroupStatus(GroupStatus.RECRUIT);
        return newGroup;
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
    
    
    @Test
    @DisplayName("groupId??? ?????? ????????? ?????? User ???????????? ????????????.")
    void findGroupMembersByGroupId() {
        List<GroupMemberDTO> groupMembers = groupService.findGroupMembersByGroupId(group.getId());

        assertThat(groupMembers).as("??? ????????? 2?????? User??? ????????? ??????.")
                .hasSize(2);
    }

    @Test
    @DisplayName("User??? ?????? Group ???????????? ????????????.")
    void getGroupListOfUser() {
        List<MyGroupDTO> groups = groupService.getGroupListOfUser(user1.getId());

        assertThat(groups).as("??? User??? 1?????? Group??? ????????????.")
                .hasSize(1);
    }

    @Test
    @DisplayName("Group master??? group member ??? ?????? ???????????????.")
    void deleteGroupMember() {
        UserIdDTO userIdDTO = new UserIdDTO();
        userIdDTO.setUserId(user1.getId());
        groupService.deleteGroupMember(group.getId(), user2.getId(), userIdDTO);

        List<GroupUser> groupUsers = groupUserRepository.findByGroup(group);
        assertThat(groupUsers).as("member ??? ?????? ????????????, ???????????? 1?????? ????????????.")
                .hasSize(1);
    }

    @Test
    @DisplayName("Group master??? ?????? member??? group member??? ??????????????? ????????? ????????????.")
    void OnlyGroupMasterCanDeleteMemberException() {
        UserIdDTO userIdDTO = new UserIdDTO();
        userIdDTO.setUserId(user2.getId());
        assertThatThrownBy(() -> groupService.deleteGroupMember(group.getId(), user1.getId(), userIdDTO))
                .isInstanceOf(IsNotGroupMasterException.class);
    }

    @Test
    @DisplayName("????????? ????????? ????????????.")
    void getGroupStatus() {
        GroupStatusResponseDTO groupStatus = groupService.getGroupStatus(group.getId());
        assertThat(groupStatus.getGroupStatus()).as("??????????????? group status??? 'RECRUIT'??? ????????? ??????.")
                .isEqualTo(GroupStatus.RECRUIT);
    }

    @Test
    @DisplayName("????????? ????????? ????????????.")
    void setGroupStatus() {
        groupService.setGroupStatus(group.getId(), GroupStatus.END_GROUP);
        GroupStatusResponseDTO groupStatus = groupService.getGroupStatus(group.getId());
        assertThat(groupStatus.getGroupStatus()).as("????????? group status??? 'END_GROUP'??? ????????? ??????.")
                .isEqualTo(GroupStatus.END_GROUP);
    }

    @Test
    @DisplayName("????????? ????????? ?????????????????? post id??? ????????????.")
    void getPostIdByGroupId() {
        PostIdDTO postIdByGroupId = groupService.getPostIdByGroupId(group.getId());
        assertThat(postIdByGroupId.getPostId())
                .isEqualTo(post.getId());
    }
}