package antifraud;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AntiFraudController {

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
}
