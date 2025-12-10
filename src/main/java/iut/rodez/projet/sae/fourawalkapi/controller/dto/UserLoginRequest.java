package iut.rodez.projet.sae.fourawalkapi.controller.dto;

/**
 * DTO pour recevoir les informations de connexion (POST /login).
 */
public class UserLoginRequest {
    private String mail;
    private String password;

    public UserLoginRequest() {}

    public String getMail() { return mail; }
    public String getPassword() { return password; }

    public void setMail(String mail) { this.mail = mail; }
    public void setPassword(String password) { this.password = password; }
}
