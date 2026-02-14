package com.example.demo.service;

import com.example.demo.model.ConfiguracionSistema;
import java.util.List;

public interface ConfiguracionService {
    String getValor(String clave, String valorPorDefecto);
    int getValorInt(String clave, int valorPorDefecto);
    void updateValor(String clave, String nuevoValor);
    List<ConfiguracionSistema> getAll();
}