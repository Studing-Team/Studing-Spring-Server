package studing.studing_server.member.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import studing.studing_server.member.dto.CustomMemberDetails;
import studing.studing_server.member.entity.Member;
import studing.studing_server.member.repository.MemberRepository;

@Service
public class CustomMemberDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    public CustomMemberDetailsService(MemberRepository memberRepository) {

        this.memberRepository = memberRepository;
    }



    @Override
    public UserDetails loadUserByUsername(String loginIdentifier) throws UsernameNotFoundException {

        // DB에서 조회하고, 없으면 예외를 던짐
        Member memberData = memberRepository.findByLoginIdentifier(loginIdentifier)
                .orElseThrow(() -> new UsernameNotFoundException("해당 사용자를 찾을 수 없습니다: " + loginIdentifier));

        // UserDetails에 담아서 return하면 AuthenticationManager가 검증함
        return new CustomMemberDetails(memberData);
    }


}
