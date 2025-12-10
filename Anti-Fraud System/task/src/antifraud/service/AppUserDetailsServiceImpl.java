package antifraud.service;

import antifraud.controller.AntiFraudController;
import antifraud.entity.AppUser;
import antifraud.security.AppUserAdapter;
import antifraud.repository.AppUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class AppUserDetailsServiceImpl implements UserDetailsService {
    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public AppUserDetailsServiceImpl(AppUserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = repository
                .findAppUserByUsername(username.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("Not found"));

        return new AppUserAdapter(user);
    }

    public ResponseEntity<AppUser> user(AntiFraudController.RegistrationRequest request){
        if(repository.findAppUserByUsername(request.username().toLowerCase()).isPresent()){
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } else {
            var user = new AppUser();
            user.setName(request.name());
            user.setUsername(request.username());
            user.setPassword(passwordEncoder.encode(request.password()));
            user.setAuthority("ROLE_USER");
            repository.save(user);
            return new ResponseEntity<>(user,HttpStatus.CREATED);
        }
    }

    public ResponseEntity<Iterable<AppUser>> list(){
        return new ResponseEntity<>(repository.findAll(),HttpStatus.OK);
    }

    public ResponseEntity<?> delete(String username){
        try{
            repository.delete(repository.findAppUserByUsername(username).orElseThrow());
            return new ResponseEntity<>(Map.of(
                    "username", username,
                    "status", "Deleted successfully!"),
                    HttpStatus.OK);
        } catch (NoSuchElementException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
