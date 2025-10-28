package com.admc.closet_cast.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member")
public class Member extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    private String loginId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    private String preference;

    private List<Tendency> tendencies;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberToCloth> clothes;

    @Builder
    public Member(String name, String loginId, String password, String preference, List<Tendency> tendencies) {
        this.name = name;
        this.loginId = loginId;
        this.password = password;
        this.preference = preference;
        this.tendencies = tendencies;
        this.clothes = new ArrayList<>();
    }
}
