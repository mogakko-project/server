package com.example.mogakko.domain.user.domain;

import com.example.mogakko.domain.evaluation.domain.Evaluation;
import com.example.mogakko.domain.group.domain.GroupAdmission;
import com.example.mogakko.domain.group.domain.GroupUser;
import com.example.mogakko.domain.post.domain.Post;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
public class User {

    @Id @GeneratedValue
    @Column(name = "user_id")
    private Long id;

    @OneToMany(mappedBy = "user")
    private List<UserLanguage> languages = new ArrayList<UserLanguage>();

    @OneToMany(mappedBy = "user")
    private List<UserLocation> locations = new ArrayList<UserLocation>();

    @OneToMany(mappedBy = "user")
    private List<UserOccupation> occupations = new ArrayList<UserOccupation>();

    @OneToMany(mappedBy = "user")
    private List<Post> posts = new ArrayList<Post>();

    @OneToMany(mappedBy = "user")
    private List<GroupUser> groupUsers = new ArrayList<GroupUser>();

    @OneToMany(mappedBy = "user")
    private List<GroupAdmission> groupAdmissions = new ArrayList<GroupAdmission>();

    // 본인이 받은 평가 리스트
    @OneToMany(mappedBy = "evaluatedUser")
    private List<Evaluation> evaluations = new ArrayList<Evaluation>();

    private String username;

    private String password;

    private String nickname;

    private String oneLineIntroduction;

    private String phoneNumber;

    private String githubAddress;

    private String picture;

    private Boolean admin;


}
