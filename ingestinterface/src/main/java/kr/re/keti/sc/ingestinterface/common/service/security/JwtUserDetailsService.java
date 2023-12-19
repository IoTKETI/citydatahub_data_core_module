package kr.re.keti.sc.ingestinterface.common.service.security;

import kr.re.keti.sc.ingestinterface.externalplatformauthentication.service.ExternalPlatformAuthenticationSVC;
import kr.re.keti.sc.ingestinterface.externalplatformauthentication.vo.ExternalPlatformAuthenticationBaseVO;
import kr.re.keti.sc.ingestinterface.externalplatformauthentication.vo.ExternalPlatformAuthenticationClientVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * JWT user Detail Service Class (Implements Srping UserDetailsService)
 */
@Service
public class JwtUserDetailsService implements UserDetailsService {

    @Autowired
    ExternalPlatformAuthenticationSVC externalPlatformAuthenticationSVC;

    @Override
    public UserDetails loadUserByUsername(String clientId) throws UsernameNotFoundException {

        ExternalPlatformAuthenticationBaseVO externalPlatformAuthenticationBaseVO = externalPlatformAuthenticationSVC.getExternalPlatformAuthenticationBaseVOByClientId(clientId);

        ExternalPlatformAuthenticationClientVO externalPlatformAuthenticationClientVO = new ExternalPlatformAuthenticationClientVO();
        if (externalPlatformAuthenticationBaseVO == null) {
            throw new UsernameNotFoundException("client ID not found");
        }

        BeanUtils.copyProperties(externalPlatformAuthenticationBaseVO, externalPlatformAuthenticationClientVO);

        externalPlatformAuthenticationClientVO.setUsername(clientId);
        return externalPlatformAuthenticationClientVO;
    }

}