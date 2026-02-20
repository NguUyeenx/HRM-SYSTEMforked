package com.hrm.gui.admin;

import com.hrm.model.Department;
import com.hrm.service.DepartmentService;
import com.hrm.util.SessionContext;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class DepartmentPanel extends JPanel {

    private DepartmentService service = new DepartmentService();
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    private JTextField txtSearch;
    private JComboBox<String> cboFilter;

    // Buttons cần phân quyền
    private JButton btnThem;
    private JButton btnSua;
    private JButton btnNgung;

    public DepartmentPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ── PANEL TRÊN: Tiêu đề + Tìm kiếm + Lọc
        JPanel topPanel = new JPanel(new BorderLayout());

        JLabel title = new JLabel("QUAN LY PHONG BAN");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        topPanel.add(title, BorderLayout.NORTH);

        // Panel chứa search + filter
        JPanel searchFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel lblSearch = new JLabel("Tim kiem:");
        txtSearch = new JTextField(20);
        txtSearch.setToolTipText("Nhap ten phong ban de tim kiem");

        JLabel lblFilter = new JLabel("    Trang thai:");
        cboFilter = new JComboBox<>(new String[] { "Tat ca", "Hoat dong", "Ngung hoat dong" });

        searchFilterPanel.add(lblSearch);
        searchFilterPanel.add(txtSearch);
        searchFilterPanel.add(lblFilter);
        searchFilterPanel.add(cboFilter);

        topPanel.add(searchFilterPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        // ── BẢNG
        tableModel = new DefaultTableModel(
                new Object[] { "Ma PB", "Ten phong ban", "Phong ban cha", "Trang thai" }, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);

        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);

        // Thêm sorter để có thể filter
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // ── THANH NÚT
        btnThem = new JButton("+ Them");
        btnSua = new JButton("Sua");
        btnNgung = new JButton("Ngung hoat dong");
        JButton btnRefresh = new JButton("Lam moi");

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(btnThem);
        btnPanel.add(btnSua);
        btnPanel.add(btnNgung);
        btnPanel.add(btnRefresh);
        add(btnPanel, BorderLayout.SOUTH);

        // ── PHÂN QUYỀN
        setupPermissions();

        // ── SỰ KIỆN
        btnThem.addActionListener(e -> showAddDialog());
        btnSua.addActionListener(e -> showEditDialog());
        btnNgung.addActionListener(e -> confirmDeactivate());
        btnRefresh.addActionListener(e -> refreshTable());

        // Tìm kiếm realtime
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                applyFilter();
            }
        });

        // Lọc theo trạng thái
        cboFilter.addActionListener(e -> applyFilter());

        // Double-click để sửa (nếu có quyền)
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && btnSua.isEnabled()) {
                    showEditDialog();
                }
            }
        });

        refreshTable();
    }

    // ── PHÂN QUYỀN

    private void setupPermissions() {
        SessionContext sc = SessionContext.getInstance();

        // Admin hoặc có quyền DEPARTMENT_CREATE/EDIT
        boolean canEdit = sc.hasRole("ADMIN")
                || sc.hasPermission("DEPARTMENT_CREATE")
                || sc.hasPermission("DEPARTMENT_EDIT");

        // Nếu không có quyền → Ẩn/Disable các nút
        btnThem.setVisible(canEdit);
        btnSua.setVisible(canEdit);
        btnNgung.setVisible(canEdit);

        // Nếu muốn disable thay vì ẩn (để user thấy nhưng không bấm được)
    }

    // ── LỌC DỮ LIỆU

    private void applyFilter() {
        String searchText = txtSearch.getText().toLowerCase().trim();
        String statusFilter = (String) cboFilter.getSelectedItem();

        RowFilter<DefaultTableModel, Object> rf = new RowFilter<DefaultTableModel, Object>() {
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                // Lọc theo tên (cột 1)
                String tenPhongBan = entry.getStringValue(1).toLowerCase();
                boolean matchSearch = searchText.isEmpty() || tenPhongBan.contains(searchText);

                // Lọc theo trạng thái (cột 3)
                String trangThai = entry.getStringValue(3);
                boolean matchStatus = true;

                if ("Hoat dong".equals(statusFilter)) {
                    matchStatus = "hoat dong".equals(trangThai);
                } else if ("Ngung hoat dong".equals(statusFilter)) {
                    matchStatus = "ngung hoat dong".equals(trangThai);
                }

                return matchSearch && matchStatus;
            }
        };

        sorter.setRowFilter(rf);
    }

    // ── LÀM MỚI BẢNG

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Department d : service.getAllDepartments()) {
            String tenCha = "— (goc)";
            if (d.getPhongBanCha() != null) {
                Department cha = service.getById(d.getPhongBanCha());
                if (cha != null) {
                    tenCha = cha.getTenPhongBan();
                }
            }

            tableModel.addRow(new Object[] {
                    d.getMaPhongBan(),
                    d.getTenPhongBan(),
                    tenCha,
                    d.getTrangThai()
            });
        }

        // Reset filter sau khi refresh
        txtSearch.setText("");
        cboFilter.setSelectedIndex(0);
    }

    // ── FORM THÊM

    private void showAddDialog() {
        JTextField txtMa = new JTextField();
        JTextField txtTen = new JTextField();

        List<Department> dsActive = service.getActiveDepartments();
        JComboBox<String> comboCha = buildParentCombo(dsActive, null);

        Object[] fields = {
                "Ma phong ban (*):", txtMa,
                "Ten phong ban (*):", txtTen,
                "Phong ban cha:", comboCha
        };

        int ok = JOptionPane.showConfirmDialog(this, fields, "Them phong ban moi", JOptionPane.OK_CANCEL_OPTION);

        if (ok != JOptionPane.OK_OPTION) {
            return;
        }

        String maCha = getSelectedMa(comboCha, dsActive);

        try {
            service.addDepartment(txtMa.getText().trim(), txtTen.getText().trim(), maCha);
            refreshTable();
            JOptionPane.showMessageDialog(this, "Them phong ban thanh cong!");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── FORM SỬA
    private void showEditDialog() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui long chon mot phong ban de sua.");
            return;
        }

        // Lấy index thật trong model (vì có filter)
        int modelRow = table.convertRowIndexToModel(row);
        String ma = (String) tableModel.getValueAt(modelRow, 0);

        Department dept = service.getById(ma);
        if (dept == null) {
            return;
        }

        JTextField txtMa = new JTextField(dept.getMaPhongBan());
        txtMa.setEnabled(false);
        JTextField txtTen = new JTextField(dept.getTenPhongBan());

        List<Department> dsActive = service.getActiveDepartments();
        dsActive.removeIf(d -> d.getMaPhongBan().equals(ma));
        JComboBox<String> comboCha = buildParentCombo(dsActive, dept.getPhongBanCha());

        Object[] fields = {
                "Ma phong ban:", txtMa,
                "Ten phong ban (*):", txtTen,
                "Phong ban cha:", comboCha
        };

        int ok = JOptionPane.showConfirmDialog(this, fields, "Chinh sua phong ban", JOptionPane.OK_CANCEL_OPTION);

        if (ok != JOptionPane.OK_OPTION) {
            return;
        }

        String maCha = getSelectedMa(comboCha, dsActive);

        try {
            service.updateDepartment(ma, txtTen.getText().trim(), maCha);
            refreshTable();
            JOptionPane.showMessageDialog(this, "Cap nhat phong ban thanh cong!");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── XÁC NHẬN NGƯNG

    private void confirmDeactivate() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui long chon mot phong ban de ngung.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(row);
        String ma = (String) tableModel.getValueAt(modelRow, 0);
        String ten = (String) tableModel.getValueAt(modelRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Ban co chac muon ngung hoat dong phong ban:\n\"" + ten + "\"?\n\n"
                        + "Luu y:\n"
                        + "- Phai ngung phong ban con truoc\n"
                        + "- Du lieu van duoc luu de bao cao/kiem toan",
                "Xac nhan ngung hoat dong",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            service.deactivateDepartment(ma);
            refreshTable();
            JOptionPane.showMessageDialog(this, "Phong ban '" + ten + "' da duoc ngung hoat dong.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Khong the ngung", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JComboBox<String> buildParentCombo(List<Department> dsActive, String maChaHienTai) {
        String[] items = new String[dsActive.size() + 1];
        items[0] = "— Khong co (phong ban goc) —";
        int selectedIndex = 0;

        for (int i = 0; i < dsActive.size(); i++) {
            items[i + 1] = dsActive.get(i).toString();
            if (dsActive.get(i).getMaPhongBan().equals(maChaHienTai)) {
                selectedIndex = i + 1;
            }
        }

        JComboBox<String> combo = new JComboBox<>(items);
        combo.setSelectedIndex(selectedIndex);
        return combo;
    }

    private String getSelectedMa(JComboBox<String> combo, List<Department> dsActive) {
        if (combo.getSelectedIndex() == 0) {
            return null;
        }
        int idx = combo.getSelectedIndex() - 1;
        return dsActive.get(idx).getMaPhongBan();
    }
}