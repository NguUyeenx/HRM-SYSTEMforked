package com.hrm.repo;

import com.hrm.model.Position;
import java.util.ArrayList;
import java.util.List;

public class PositionRepository {

    private List<Position> positions = new ArrayList<>();

    public PositionRepository() {
        // Dữ liệu mẫu
        positions.add(new Position("CV001", "Giam doc", 1, 3.5, 5000000, "Lanh dao tong cong ty", "hoat dong"));
        positions.add(new Position("CV002", "Truong phong", 2, 2.8, 3000000, "Quan ly phong ban", "hoat dong"));
        positions
                .add(new Position("CV003", "Senior Developer", 3, 2.2, 1500000, "Lap trinh vien cap cao", "hoat dong"));
        positions.add(
                new Position("CV004", "Junior Developer", 4, 1.4, 500000, "Lap trinh vien moi vao nghe", "hoat dong"));
    }

    public List<Position> findAll() {
        return positions;
    }

    public Position findById(String maChucVu) {
        for (Position p : positions) {
            if (p.getMaChucVu().equals(maChucVu)) {
                return p;
            }
        }
        return null;
    }

    public boolean existsById(String maChucVu) {
        return findById(maChucVu) != null;
    }

    public void save(Position position) {
        positions.add(position);
    }

    public void update(Position updated) {
        for (int i = 0; i < positions.size(); i++) {
            if (positions.get(i).getMaChucVu().equals(updated.getMaChucVu())) {
                positions.set(i, updated);
                return;
            }
        }
    }
}