package antifraud.service;

import antifraud.controller.AntiFraudController;
import antifraud.entity.AppUser;
import antifraud.security.AppUserAdapter;
import antifraud.repository.AppUserRepository;
import jakarta.validation.Valid;
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
            if(repository.count() == 0){
                user.setAuthority("ADMINISTRATOR");
                user.setIsLocked(false);
            } else {
                user.setAuthority("MERCHANT");
                user.setIsLocked(true);
            }
            repository.save(user);
            return new ResponseEntity<>(user,HttpStatus.CREATED);
        }
    }

    public ResponseEntity<AppUser> role(AntiFraudController.ChangeRoleRequest request){
        if(repository.findAppUserByUsername(request.username().toLowerCase()).isPresent()) {
            var user = repository.findAppUserByUsername(request.username().toLowerCase()).get();
            if(user.getAuthority().equals(request.role())){
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            } else {
                user.setAuthority(request.role());
            }
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> access(AntiFraudController.ActivationRequest request){
        if(repository.findAppUserByUsername(request.username().toLowerCase()).isPresent()){
            var user = repository.findAppUserByUsername(request.username().toLowerCase()).get();
            if (user.getAuthority().equals("ROLE_ADMINISTRATOR")) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            } else {
                if(request.operation().equals("LOCK")){
                    user.setIsLocked(true);
                } else{
                    user.setIsLocked(false);
                }
                return new ResponseEntity<>(Map.of(
                        "status", "User " + request.username() + " "+ request.operation().toLowerCase() + "ed!"),
                        HttpStatus.OK);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
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
