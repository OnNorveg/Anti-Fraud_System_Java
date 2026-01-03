package antifraud.controller;

import antifraud.entity.*;
import antifraud.repository.AppCardRepository;
import antifraud.repository.AppIpRepository;
import antifraud.service.AppUserDetailsServiceImpl;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.LuhnCheck;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
public class AntiFraudController {

    private static final String IP_REGEX = "^((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])\\.){3}(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])$";

    private final AppUserDetailsServiceImpl appUserDetailsService;
    private final AppIpRepository appIPRepository;
    private final AppCardRepository appCardRepository;

    public AntiFraudController(AppUserDetailsServiceImpl appUserDetailsService,
                               PasswordEncoder passwordEncoder,
                               AppIpRepository appIPRepository,
                               AppCardRepository appCardRepository) {
        this.appUserDetailsService = appUserDetailsService;
        this.appIPRepository = appIPRepository;
        this.appCardRepository = appCardRepository;
    }

    @PostMapping("/api/antifraud/transaction")
    public ResponseEntity<?> validateTransaction(@Valid @RequestBody TransactionRequest request){
        if(request.amount() <= 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }else if(request.amount() > 1500){
            return new ResponseEntity<>(new TransactionResult("PROHIBITED", "amount"),HttpStatus.OK);
        } else if(appIPRepository.findAppIpByIp(request.ip()).isPresent()){
            return new ResponseEntity<>(new TransactionResult("PROHIBITED", "ip"),HttpStatus.OK);
        } else if(appCardRepository.findAppCardByCardNumber(request.number()).isPresent()){
            return new ResponseEntity<>(new TransactionResult("PROHIBITED", "card-number"),HttpStatus.OK);
        } else if(request.amount() <= 200){
            return new ResponseEntity<>(new TransactionResult("ALLOWED","none"), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new TransactionResult("MANUAL_PROCESSING","amount"), HttpStatus.OK);
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

   @PostMapping(path = "/api/antifraud/suspicious-ip")
   public ResponseEntity<?> suspiciousIpAdd(@Valid @RequestBody IPRequest  request) {
       if(appIPRepository.findAppIpByIp(request.ip()).isPresent()){
           return new ResponseEntity<>(HttpStatus.CONFLICT);
       } else {
           var ip = new AppIp();
           ip.setIp(request.ip());
           appIPRepository.save(ip);
           return new ResponseEntity<>(ip,HttpStatus.CREATED);
       }
   }

   @PostMapping(path = "/api/antifraud/stolencard")
   public ResponseEntity<?> stolenCardAdd(@Valid @RequestBody CardRequest cardRequest) {
        if(appCardRepository.findAppCardByCardNumber(cardRequest.cardNumber()).isPresent()){
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } else {
            var card = new AppCard();
            card.setCardNumber(cardRequest.cardNumber());
            appCardRepository.save(card);
            return new ResponseEntity<>(card,HttpStatus.CREATED);
        }
   }

    @GetMapping("/api/auth/list")
    public ResponseEntity<Iterable<AppUser>> list(){
        return appUserDetailsService.list();
    }

    @GetMapping("/api/antifraud/suspicious-ip")
    public ResponseEntity<Iterable<AppIp>> suspiciousIpList(){
        return new ResponseEntity<>(appIPRepository.findAll(),HttpStatus.OK);
    }

    @GetMapping("/api/antifraud/stolencard")
    public ResponseEntity<Iterable<AppCard>> stolencardList(){
        return new ResponseEntity<>(appCardRepository.findAll(),HttpStatus.OK);
    }

    @DeleteMapping("/api/auth/user/{username}")
    public ResponseEntity<?> delete(@PathVariable("username") String username){
        return appUserDetailsService.delete(username);
    }

    @DeleteMapping("/api/antifraud/suspicious-ip/{ip}")
    public ResponseEntity<?> suspiciousIpDelete(@PathVariable("ip") @Pattern(regexp = IP_REGEX) String ip){
        try {
            appIPRepository.delete(appIPRepository.findAppIpByIp(ip).orElseThrow());
            return new ResponseEntity<>(Map.of(
                    "status", "IP " + ip + " successfully removed!"),
                    HttpStatus.OK);
        } catch (NoSuchElementException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/api/antifraud/stolencard/{number}")
    public ResponseEntity<?> stolenCardDelete(@PathVariable("number") @LuhnCheck String number){
        try{
            appCardRepository.delete(appCardRepository.findAppCardByCardNumber(number).orElseThrow());
            return new ResponseEntity<>(Map.of("status", "Card " + number + " successfully removed!"),
                    HttpStatus.OK);
        } catch (NoSuchElementException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    public record RegistrationRequest(@NotEmpty String name, @NotEmpty String username, @NotEmpty String password) { }

    public record ChangeRoleRequest(@NotEmpty String username, @Pattern(regexp = "MERCHANT|SUPPORT") String role) { }

    public record ActivationRequest(@NotEmpty String username, @Pattern(regexp = "LOCK|UNLOCK") String operation) { }

    public record IPRequest(@Pattern(regexp = IP_REGEX) String ip) { }

    public record CardRequest(@LuhnCheck String cardNumber) { }

    public record TransactionRequest(Long amount, @NotEmpty @Pattern(regexp = IP_REGEX) String ip, @NotEmpty @LuhnCheck String number) { }

    public record TransactionResult(String result, String info) { }
}
