package antifraud.controller;

import antifraud.entity.AppUser;
import antifraud.repository.AppUserRepository;
import antifraud.entity.TransactionRequest;
import antifraud.entity.TransactionResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Objects;

@RestController
public class AntiFraudController {

    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public AntiFraudController(AppUserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/api/antifraud/transaction")
    public ResponseEntity<TransactionResult> validateTransaction(@RequestBody TransactionRequest request){
        if(request.getAmount() <= 0){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }else if(request.getAmount() <= 200){
            return new ResponseEntity<>(new TransactionResult("ALLOWED"), HttpStatus.OK);
        } else if(request.getAmount() <= 1500){
            return new ResponseEntity<>(new TransactionResult("MANUAL_PROCESSING"), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new TransactionResult("PROHIBITED"), HttpStatus.OK);
        }
    }

    @PostMapping(path = "/api/auth/user")
    public ResponseEntity<AppUser> user(@RequestBody RegistrationRequest request) {
        if(repository.findAppUserByUsername(request.username.toLowerCase()).isPresent()){
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } else {
            var user = new AppUser();
            user.setName(request.name());
            user.setUsername(request.username());
            user.setPassword(passwordEncoder.encode(request.password));
            user.setAuthority("ROLE_USER");
            repository.save(user);
            return new ResponseEntity<>(user,HttpStatus.CREATED);
        }
    }

    @GetMapping("/api/auth/list")
    public ResponseEntity<Iterable<AppUser>> list(){
        return new ResponseEntity<>(repository.findAll(),HttpStatus.OK);
    }

    record RegistrationRequest(String name, String username, String password) { }
}
