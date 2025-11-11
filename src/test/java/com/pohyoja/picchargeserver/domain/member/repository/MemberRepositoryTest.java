package com.pohyoja.picchargeserver.domain.member.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.pohyoja.picchargeserver.domain.member.entity.Member;
import com.pohyoja.picchargeserver.domain.member.entity.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 멤버_저장_테스트() {
        // given
        Member member = Member.builder()
                .name("이상현")
                .role(Role.CHILD)
                .uid("uid123")
                .email("dgh06175@gmail.com")
                .build();

        // when
        Member savedMember = memberRepository.save(member);

        // then
        assertThat(savedMember).isNotNull();
        assertThat(savedMember.getEmail()).isEqualTo("dgh06175@gmail.com");
        assertThat(savedMember.getRole()).isEqualTo(Role.CHILD);
        assertThat(savedMember.getUid()).isEqualTo("uid123");
        assertThat(savedMember.getName()).isEqualTo("이상현");
    }
}