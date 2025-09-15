package pe.edu.upeu.farmafx.modelo;

import pe.edu.upeu.farmafx.enums.RolUsuario;

public class Usuario {
    private int id;
    private String dni;
    private String passwordHash;
    private RolUsuario rol;

    public Usuario() {}

    public Usuario(int id, String dni, String passwordHash, RolUsuario rol) {
        this.id = id;
        this.dni = dni;
        this.passwordHash = passwordHash;
        this.rol = rol;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getDni() {
        return dni;
    }
    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public RolUsuario getRol() {
        return rol;
    }
    public void setRol(RolUsuario rol) {
        this.rol = rol;
    }
}