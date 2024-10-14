package studing.studing_server.member.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import studing.studing_server.member.dto.CustomUserDetails;
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

        //DB에서 조회
        Member memberData = memberRepository.findByLoginIdentifier(loginIdentifier);

        if (memberData != null) {

            //UserDetails에 담아서 return하면 AutneticationManager가 검증 함
            return new CustomUserDetails(memberData);
        }

        return null;
    }
}
