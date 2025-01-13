package org.iesvdm.ventas_sb.dao;

import org.iesvdm.ventas_sb.modelo.Comercial;

import java.util.List;
import java.util.Optional;

public interface ComercialDAO {

    public void create(Comercial comercial);

    public List<Comercial> getAll();
    public Optional<Comercial> find(int id);

    public void update(Comercial comercial);

    public void delete(int id);
}
