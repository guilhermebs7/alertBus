package alertbus.user_service.controller;

import alertbus.user_service.dto.request.LoginRequestDTO;
import alertbus.user_service.dto.request.UserRequestDTO;
import alertbus.user_service.dto.response.LoginResponseDTO;
import alertbus.user_service.dto.response.UserResponseDTO;
import alertbus.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@Tag(name = "User Controller", description = "Endpoint para cadastrar usuários e efetuar o login")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "Criar usuário")
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody UserRequestDTO dto){
        UserResponseDTO response= userService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Busca usuário por ID")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable UUID id){
        UserResponseDTO response= userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Lista todos os usuários")
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(){
        List<UserResponseDTO> response = userService.getAllUsers();
        return ResponseEntity.ok(response);
    }
    @Operation(summary = "Efetua o login")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO dto){
        System.out.println("Tentando login para o email: " + dto.email());
        String token= userService.Login(dto);
        return ResponseEntity.ok(new LoginResponseDTO(token));
    }
}
