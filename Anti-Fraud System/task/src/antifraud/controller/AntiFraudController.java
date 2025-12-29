package antifraud.controller;

import antifraud.entity.AppUser;
import antifraud.repository.AppUserRepository;
import antifraud.entity.TransactionRequest;
import antifraud.entity.TransactionResult;
import antifraud.security.AppUserAdapter;
import antifraud.service.AppUserDetailsServiceImpl;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

@RestController
public class AntiFraudController {

    private final AppUserDetailsServiceImpl appUserDetailsService;

    public AntiFraudController(AppUserDetailsServiceImpl appUserDetailsService, PasswordEncoder passwordEncoder) {
        this.appUserDetailsService = appUserDetailsService;
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
    public ResponseEntity<AppUser> user(@Valid @RequestBody RegistrationRequest request) {
        return appUserDetailsService.user(request);
    }

    @PostMapping(path = "/api/auth/role")
    public ResponseEntity<AppUser> role(@Valid @RequestBody ChangeRoleRequest request) {
        return appUserDetailsService.role(request);
    }

    @PostMapping(path = "/api/auth/access")
    public ResponseEntity<?> access(@Valid @RequestBody ActivationRequest request) {
        return appUserDetailsService.access(request);
    }

    @GetMapping("/api/auth/list")
    public ResponseEntity<Iterable<AppUser>> list(){
        return appUserDetailsService.list();
    }

    @DeleteMapping("/api/auth/user/{username}")
    public ResponseEntity<?> delete(@PathVariable("username") String username){
        return appUserDetailsService.delete(username);
    }

    public record RegistrationRequest(@NotEmpty String name, @NotEmpty String username, @NotEmpty String password) { }

    public record ChangeRoleRequest(@NotEmpty String username, @Pattern(regexp = "MERCHANT|SUPPORT") String role) { }

    public record ActivationRequest(@NotEmpty String username, @Pattern(regexp = "LOCK|UNLOCK") String operation) { }
}
