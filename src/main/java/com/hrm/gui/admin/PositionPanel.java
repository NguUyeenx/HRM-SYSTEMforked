package com.hrm.gui.admin;

import com.hrm.model.Position;
import com.hrm.model.SalaryHistory;
import com.hrm.service.PositionService;
import com.hrm.util.SessionContext;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PositionPanel extends JPanel {

    private PositionService service = new PositionService();
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private NumberFormat moneyFmt = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    private JTextField txtSearch;
    private JComboBox<String> cboFilter;

    // Buttons cần phân quyền
    private JButton btnThem;
    private JButton btnSua;
    private JButton btnNgung;

    public PositionPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ── PANEL TRÊN: Tiêu đề + Tìm kiếm + Lọc
        JPanel topPanel = new JPanel(new BorderLayout());

        JLabel title = new JLabel("QUAN LY CHUC VU");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        topPanel.add(title, BorderLayout.NORTH);

        // Panel chứa search + filter
        JPanel searchFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel lblSearch = new JLabel("Tim kiem:");
        txtSearch = new JTextField(20);
        txtSearch.setToolTipText("Nhap ten chuc vu de tim kiem");

        JLabel lblFilter = new JLabel("    Trang thai:");
        cboFilter = new JComboBox<>(new String[] { "Tat ca", "Hoat dong", "Ngung" });

        searchFilterPanel.add(lblSearch);
        searchFilterPanel.add(txtSearch);
        searchFilterPanel.add(lblFilter);
        searchFilterPanel.add(cboFilter);

        topPanel.add(searchFilterPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        // ── BẢNG
        tableModel = new DefaultTableModel(
                new Object[] { "Ma CV", "Ten chuc vu", "Cap bac", "He so luong", "Phu cap (VND)", "Trang thai" }, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);

        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(70);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(110);
        table.getColumnModel().getColumn(5).setPreferredWidth(90);

        // Thêm sorter để có thể filter
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // ── THANH NÚT
        btnThem = new JButton("+ Them");
        btnSua = new JButton("Sua");
        btnNgung = new JButton("Ngung hoat dong");
        JButton btnLichSu = new JButton("Xem lich su he so");
        JButton btnRefresh = new JButton("Lam moi");

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(btnThem);
        btnPanel.add(btnSua);
        btnPanel.add(btnNgung);
        btnPanel.add(btnLichSu);
        btnPanel.add(btnRefresh);
        add(btnPanel, BorderLayout.SOUTH);

        // ── PHÂN QUYỀN
        setupPermissions();

        // ── SỰ KIỆN
        btnThem.addActionListener(e -> showAddDialog());
        btnSua.addActionListener(e -> showEditDialog());
        btnNgung.addActionListener(e -> confirmDeactivate());
        btnLichSu.addActionListener(e -> showHistoryDialog());
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

        // Admin hoặc có quyền POSITION_CREATE/EDIT
        boolean canEdit = sc.hasRole("ADMIN")
                || sc.hasPermission("POSITION_CREATE")
                || sc.hasPermission("POSITION_EDIT");

        // Nếu không có quyền → Ẩn các nút
        btnThem.setVisible(canEdit);
        btnSua.setVisible(canEdit);
        btnNgung.setVisible(canEdit);
    }

    // ── LỌC DỮ LIỆU

    private void applyFilter() {
        String searchText = txtSearch.getText().toLowerCase().trim();
        String statusFilter = (String) cboFilter.getSelectedItem();

        RowFilter<DefaultTableModel, Object> rf = new RowFilter<DefaultTableModel, Object>() {
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                // Lọc theo tên (cột 1)
                String tenChucVu = entry.getStringValue(1).toLowerCase();
                boolean matchSearch = searchText.isEmpty() || tenChucVu.contains(searchText);

                // Lọc theo trạng thái (cột 5)
                String trangThai = entry.getStringValue(5);
                boolean matchStatus = true;

                if ("Hoat dong".equals(statusFilter)) {
                    matchStatus = "hoat dong".equals(trangThai);
                } else if ("Ngung".equals(statusFilter)) {
                    matchStatus = "ngung".equals(trangThai);
                }

                return matchSearch && matchStatus;
            }
        };

        sorter.setRowFilter(rf);
    }

    // ── LÀM MỚI BẢNG

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Position p : service.getAllPositions()) {
            tableModel.addRow(new Object[] {
                    p.getMaChucVu(),
                    p.getTenChucVu(),
                    "Cap " + p.getCapBac(),
                    p.getHeSoLuong() + "x",
                    moneyFmt.format(p.getPhuCapChucVu()),
                    p.getTrangThai()
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
        JTextField txtCapBac = new JTextField("1");
        JTextField txtHeSo = new JTextField("1.0");
        JTextField txtPhuCap = new JTextField("0");
        JTextArea txtMoTa = new JTextArea(3, 20);
        txtMoTa.setLineWrap(true);

        Object[] fields = {
                "Ma chuc vu (*):", txtMa,
                "Ten chuc vu (*):", txtTen,
                "Cap bac (1=cao nhat):", txtCapBac,
                "He so luong (*):", txtHeSo,
                "Phu cap (VND):", txtPhuCap,
                "Mo ta:", new JScrollPane(txtMoTa)
        };

        int ok = JOptionPane.showConfirmDialog(this, fields, "Them chuc vu moi", JOptionPane.OK_CANCEL_OPTION);

        if (ok != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            int capBac = Integer.parseInt(txtCapBac.getText().trim());
            double heSo = Double.parseDouble(txtHeSo.getText().trim());
            double phuCap = Double.parseDouble(txtPhuCap.getText().trim());

            service.addPosition(txtMa.getText().trim(), txtTen.getText().trim(), capBac, heSo, phuCap,
                    txtMoTa.getText().trim());
            refreshTable();
            JOptionPane.showMessageDialog(this, "Them chuc vu thanh cong!");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Cap bac, he so, phu cap phai la so hop le.", "Loi nhap lieu",
                    JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── FORM SỬA

    private void showEditDialog() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui long chon mot chuc vu de sua.");
            return;
        }

        // Lấy index thật trong model (vì có filter)
        int modelRow = table.convertRowIndexToModel(row);
        String ma = (String) tableModel.getValueAt(modelRow, 0);

        Position pos = service.getById(ma);
        if (pos == null) {
            return;
        }

        JTextField txtMa = new JTextField(pos.getMaChucVu());
        txtMa.setEnabled(false);

        JTextField txtTen = new JTextField(pos.getTenChucVu());
        JTextField txtCapBac = new JTextField(String.valueOf(pos.getCapBac()));
        JTextField txtHeSo = new JTextField(String.valueOf(pos.getHeSoLuong()));
        JTextField txtPhuCap = new JTextField(String.valueOf(pos.getPhuCapChucVu()));
        JTextArea txtMoTa = new JTextArea(pos.getMoTa(), 3, 20);
        txtMoTa.setLineWrap(true);

        JLabel lblWarning = new JLabel(
                "<html><i style='color:orange'>⚠ Thay doi he so/phu cap se tu ghi vao Lich su he so luong</i></html>");

        Object[] fields = {
                "Ma chuc vu:", txtMa,
                "Ten chuc vu (*):", txtTen,
                "Cap bac:", txtCapBac,
                "He so luong (*):", txtHeSo,
                "Phu cap (VND):", txtPhuCap,
                "Mo ta:", new JScrollPane(txtMoTa),
                lblWarning
        };

        int ok = JOptionPane.showConfirmDialog(this, fields, "Chinh sua chuc vu", JOptionPane.OK_CANCEL_OPTION);

        if (ok != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            int capBac = Integer.parseInt(txtCapBac.getText().trim());
            double heSo = Double.parseDouble(txtHeSo.getText().trim());
            double phuCap = Double.parseDouble(txtPhuCap.getText().trim());

            service.updatePosition(ma, txtTen.getText().trim(), capBac, heSo, phuCap, txtMoTa.getText().trim());
            refreshTable();
            JOptionPane.showMessageDialog(this, "Cap nhat chuc vu thanh cong!");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Cap bac, he so, phu cap phai la so hop le.", "Loi nhap lieu",
                    JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── XÁC NHẬN NGƯNG

    private void confirmDeactivate() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui long chon mot chuc vu de ngung.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(row);
        String ma = (String) tableModel.getValueAt(modelRow, 0);
        String ten = (String) tableModel.getValueAt(modelRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Ban co chac muon ngung hoat dong chuc vu:\n\"" + ten + "\"?\n\n"
                        + "Luu y: Du lieu van duoc luu de bao cao/kiem toan",
                "Xac nhan ngung hoat dong",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            service.deactivatePosition(ma);
            refreshTable();
            JOptionPane.showMessageDialog(this, "Chuc vu '" + ten + "' da duoc ngung hoat dong.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── DIALOG XEM LỊCH SỬ

    private void showHistoryDialog() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui long chon mot chuc vu de xem lich su.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(row);
        String ma = (String) tableModel.getValueAt(modelRow, 0);
        String ten = (String) tableModel.getValueAt(modelRow, 1);

        List<SalaryHistory> danhSach = service.getHistoryByMaChucVu(ma);

        DefaultTableModel histModel = new DefaultTableModel(
                new Object[] { "Ngay thay doi", "He so cu", "He so moi", "Phu cap cu (VND)", "Phu cap moi (VND)",
                        "Nguoi thay doi" },
                0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        for (SalaryHistory h : danhSach) {
            histModel.addRow(new Object[] {
                    h.getNgayThayDoi(),
                    h.getHeSoLuongCu() + "x",
                    h.getHeSoLuongMoi() + "x",
                    moneyFmt.format(h.getPhuCapCu()),
                    moneyFmt.format(h.getPhuCapMoi()),
                    h.getNguoiThayDoi()
            });
        }

        JTable histTable = new JTable(histModel);
        histTable.setRowHeight(24);
        JScrollPane scroll = new JScrollPane(histTable);
        scroll.setPreferredSize(new Dimension(640, 200));

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Lich su he so luong — " + ten + " (" + ma + ")", true);
        dialog.setLayout(new BorderLayout());

        if (danhSach.isEmpty()) {
            dialog.add(new JLabel("  Chua co lich su thay doi nao.", SwingConstants.CENTER), BorderLayout.CENTER);
        } else {
            dialog.add(scroll, BorderLayout.CENTER);
        }

        JButton btnDong = new JButton("Dong");
        btnDong.addActionListener(e -> dialog.dispose());
        JPanel footer = new JPanel();
        footer.add(btnDong);
        dialog.add(footer, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}