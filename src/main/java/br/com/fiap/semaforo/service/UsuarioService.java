package br.com.fiap.semaforo.service;

import br.com.fiap.semaforo.dto.*;
import br.com.fiap.semaforo.exception.RecursoNaoEncontradoException;
import br.com.fiap.semaforo.exception.RegraNegocioException;
import br.com.fiap.semaforo.model.Papel;
import br.com.fiap.semaforo.model.Usuario;
import br.com.fiap.semaforo.repository.UsuarioRepository;
import br.com.fiap.semaforo.security.JwtService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwt;

    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder encoder, AuthenticationManager authManager,
                          JwtService jwt) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.authManager = authManager;
        this.jwt = jwt;
    }

    @Transactional
    public UsuarioResponse cadastrar(CadastroRequest req) {
        return UsuarioResponse.de(criar(req.nome(), req.email(), req.senha(), Papel.CIDADAO));
    }

    @Transactional
    public Usuario criar(String nome, String email, String senha, Papel papel) {
        if (usuarios.existsByEmailIgnoreCase(email)) {
            throw new RegraNegocioException("E-mail já cadastrado");
        }
        Usuario u = new Usuario();
        u.setNome(nome);
        u.setEmail(email.trim().toLowerCase());
        u.setSenha(encoder.encode(senha));
        u.setPapel(papel);
        return usuarios.save(u);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest req) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(req.email().trim().toLowerCase(), req.senha()));
        Usuario u = usuarios.findByEmailIgnoreCase(req.email().trim()).orElseThrow();
        return new TokenResponse(jwt.gerar(u), "Bearer", jwt.validadeSegundos(), u.getEmail(), u.getPapel());
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listar(Pageable pageable) {
        return usuarios.findAll(pageable).map(UsuarioResponse::de);
    }

    @Transactional
    public UsuarioResponse alterarPapel(Long id, Papel papel) {
        Usuario u = usuarios.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", id));
        u.setPapel(papel);
        return UsuarioResponse.de(u);
    }
}
