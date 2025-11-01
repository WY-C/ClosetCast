package com.admc.closet_cast.entity;

import jakarta.persistence.*;
import lombok.*;

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
    @Setter
    private String password;

    @Setter
    private String preference;

    @Setter
    private List<Tendency> tendencies;

    @Setter
    @ElementCollection(fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    private List<Cloth> clothes;

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
