package alertbus.user_service.service;

import alertbus.user_service.domain.entity.User;
import alertbus.user_service.dto.request.LoginRequestDTO;
import alertbus.user_service.dto.request.UserRequestDTO;
import alertbus.user_service.dto.response.UserResponseDTO;
import alertbus.user_service.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenService tokenService;

    @Transactional
    public UserResponseDTO createUser(UserRequestDTO dto){
        if (userRepository.existsByEmail(dto.email())){
            throw new IllegalArgumentException("E-mail já cadastrado no sistema.");
        }

        User user= User.builder()
                .name(dto.name())
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .build();

        User savedUser= userRepository.save(user);

        return new UserResponseDTO(savedUser.getId(),
                savedUser.getName(), savedUser.getEmail());
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(UUID id){
        User user= userRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Usuário não encontrado"));

        return new UserResponseDTO(user.getId(), user.getName(), user.getEmail());
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers(){
        return userRepository.findAll()
                .stream()
                .map(user -> new UserResponseDTO(user.getId(), user.getName(), user.getEmail()))
                .toList();
    }

    public String Login(LoginRequestDTO dto){
        User user= userRepository.findByEmail(dto.email())
                .orElseThrow(()-> new IllegalArgumentException("Usuário ou senha inválidos."));

        if(!passwordEncoder.matches(dto.email(), user.getPassword())){
            throw  new IllegalArgumentException("Usuário ou senha inválido");
        }
        return tokenService.generateToken(user);
    }


}
