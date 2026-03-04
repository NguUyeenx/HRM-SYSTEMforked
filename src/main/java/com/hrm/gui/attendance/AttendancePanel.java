package com.hrm.gui.attendance;

import com.hrm.model.*;
import com.hrm.service.AttendanceService;
import com.hrm.service.AttendanceService.ServiceResult;
import com.hrm.service.MockDataService;
import com.hrm.util.SessionContext;
import com.hrm.util.UIColors;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class AttendancePanel extends JPanel {

    private final AttendanceService svc;
    private final User currentUser;
    private final boolean isAdmin;
    private final NumberFormat moneyFmt;

    private JTabbedPane tabbedPane;

    // Tab 1
    private JTable tableChamCong; private DefaultTableModel modelCC;
    private JComboBox<String> cboThang, cboNam;
    private JPanel statsPanel;
    // Tab 2
    private JTable tableCaLam; private DefaultTableModel modelCaLam;
    // Tab 3
    private JTable tableDonOT; private DefaultTableModel modelOT;
    // Tab 4
    private JTable tableLuong; private DefaultTableModel modelLuong;
    private JComboBox<String> cboThangL, cboNamL;
    private JPanel luongStats;
    private BangLuong bangLuongHienTai;
        // Thêm vào khu vực field Tab 4;
    // Cache ds ChiTietLuong đã load để Export dùng lại (tránh query lại DB)
    private List<ChiTietLuong> dsCachedLuong = new java.util.ArrayList<>();

        // Thêm vào khu vực khai báo field Tab 4
    private JSpinner spinThangL, spinNamL;
    // Map lưu trạng thái ẩn/hiện của từng cột (index → ẩn)
    private final boolean[] colHidden = new boolean[12]; // 12 cột
    // Lưu độ rộng cột gốc để khôi phục
    private final int[] colWidthOrig = new int[12];
    // Tab 5
    private JTable tablePC; private DefaultTableModel modelPC;

    public AttendancePanel() {
        svc = AttendanceService.getInstance();
        currentUser = SessionContext.getInstance().getCurrentUser();
        isAdmin = SessionContext.getInstance().hasRole("ADMIN");
        moneyFmt = NumberFormat.getInstance(new Locale("vi", "VN"));
        setLayout(new BorderLayout()); setBackground(UIColors.LIGHT_GRAY_BG);
        initTabs();
    }

    private void initTabs() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.setBackground(UIColors.WHITE);
        if (isAdmin) {
            tabbedPane.addTab("Tong hop cham cong", tabTongHop());
            tabbedPane.addTab("Quan ly ca lam", tabCaLam());
            tabbedPane.addTab("Duyet don OT", tabDuyetOT());
            tabbedPane.addTab("Phu cap & Khau tru", tabPhuCap());
            tabbedPane.addTab("Bang luong", tabBangLuong());
        } else {
            tabbedPane.addTab("Cham cong", tabCaNhan());
            tabbedPane.addTab("Lich su", tabTongHop());
        }
        add(tabbedPane, BorderLayout.CENTER);
    }

    // ═══════════════════════════════════
    //  TAB 1 — TỔNG HỢP CHẤM CÔNG
    // ═══════════════════════════════════
    private JPanel tabTongHop() {
        JPanel p=panel();
        JPanel tb=new JPanel(new FlowLayout(FlowLayout.LEFT,10,5)); tb.setOpaque(false);
        tb.add(new JLabel("Thang:")); cboThang=combMonth(); tb.add(cboThang);
        tb.add(new JLabel("Nam:")); cboNam=combYear(); tb.add(cboNam);
        JButton bL=btn("Loc du lieu",UIColors.PRIMARY_PURPLE); bL.addActionListener(e->loadCC()); tb.add(bL);
        tb.add(Box.createHorizontalStrut(20));
        JButton bT=btn("+ Them thu cong",UIColors.SUCCESS_GREEN); bT.addActionListener(e->dlgThemCC()); tb.add(bT);
        p.add(tb,BorderLayout.NORTH);
        String[]cols={"Ma NV","Ho ten","Ngay","Ca lam","Gio vao","Gio ra","So gio","OT","Trang thai"};
        modelCC=mdl(cols); tableChamCong=tbl(modelCC);
        tableChamCong.getColumnModel().getColumn(8).setCellRenderer(new StatusR());
        p.add(new JScrollPane(tableChamCong),BorderLayout.CENTER);
        statsPanel=new JPanel(new FlowLayout(FlowLayout.LEFT,30,10));
        statsPanel.setOpaque(false); statsPanel.setBorder(new EmptyBorder(5,0,0,0));
        p.add(statsPanel,BorderLayout.SOUTH); loadCC(); return p;
    }

    private void loadCC() {
        modelCC.setRowCount(0);
        int th = Integer.parseInt((String) cboThang.getSelectedItem());
        int nm = Integer.parseInt((String) cboNam.getSelectedItem());
        List<ChamCong> ds = svc.getChamCongTheoThang(th, nm);
        DateTimeFormatter fN = DateTimeFormatter.ofPattern("dd/MM");
        DateTimeFormatter fG = DateTimeFormatter.ofPattern("HH:mm");
        for (ChamCong cc : ds) {
            // ✅ Lấy maNhanVien (mã hiển thị) từ DB thay vì getMaNV() (số int)
            String maNhanVien = com.hrm.repo.AttendanceRepository
                .getInstance().getMaNhanVienById(cc.getMaNV());
            modelCC.addRow(new Object[]{
                maNhanVien,
                cc.getEmployeeName() != null ? cc.getEmployeeName() : "NV-" + cc.getMaNV(),
                cc.getNgay().format(fN),
                cc.getTenCaLam() != null ? cc.getTenCaLam() : cc.getMaCaLam(),
                cc.getGioVao() != null ? cc.getGioVao().format(fG) : "-",
                cc.getGioRa()  != null ? cc.getGioRa().format(fG)  : "-",
                String.format("%.1f", cc.getSoGioLam()),
                cc.getGioLamThem() > 0 ? String.format("%.1f", cc.getGioLamThem()) : "-",
                cc.getTrangThai() != null ? cc.getTrangThai().getDisplayName() : "N/A"
            });
        }
        if (statsPanel == null) return;
        long dg = ds.stream().filter(c -> c.getTrangThai() == ChamCong.TrangThai.DUNG_GIO).count();
        long dm = ds.stream().filter(c -> c.getTrangThai() == ChamCong.TrangThai.DI_MUON).count();
        long vm = ds.stream().filter(c -> c.getTrangThai() == ChamCong.TrangThai.VANG_MAT).count();
        statsPanel.removeAll();
        statsPanel.add(lbl("Tong:", String.valueOf(ds.size()), UIColors.PRIMARY_PURPLE));
        statsPanel.add(lbl("Dung gio:", String.valueOf(dg), UIColors.SUCCESS_GREEN));
        statsPanel.add(lbl("Di muon:", String.valueOf(dm), UIColors.DANGER_RED));
        statsPanel.add(lbl("Vang:", String.valueOf(vm), Color.GRAY));
        statsPanel.revalidate();
        statsPanel.repaint();
    }    
    private void dlgThemCC() {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Them cham cong thu cong", true);
        d.setSize(500, 560);
        d.setLocationRelativeTo(this);

        JPanel f = new JPanel(new GridBagLayout());
        f.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints g = gbc();

        // ── Row 0: Mã NV + nút Tìm ──
        g.gridx = 0; g.gridy = 0; g.weightx = 0; g.gridwidth = 1;
        f.add(new JLabel("Ma nhan vien:"), g);
        JTextField txtMaNV = new JTextField(12);
        txtMaNV.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtMaNV.setToolTipText("Nhap ma nhan vien (VD: NV001)");
        g.gridx = 1; g.weightx = 1;
        f.add(txtMaNV, g);
        JButton btnTim = btn("Tim", UIColors.PRIMARY_PURPLE);
        btnTim.setPreferredSize(new Dimension(70, 30));
        g.gridx = 2; g.weightx = 0;
        f.add(btnTim, g);

        // ── Row 1: Panel thông tin nhân viên (ẩn mặc định) ──
        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 8, 6));
        infoPanel.setBackground(new Color(245, 247, 255));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIColors.PRIMARY_PURPLE, 1),
            new EmptyBorder(10, 12, 10, 12)));
        infoPanel.setVisible(false);

        JLabel lblHoTenVal     = new JLabel(""); lblHoTenVal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JLabel lblEmailVal     = new JLabel(""); lblEmailVal.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JLabel lblChucVuVal    = new JLabel(""); lblChucVuVal.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JLabel lblPhongBanVal  = new JLabel(""); lblPhongBanVal.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JLabel lblTrangThaiVal = new JLabel(""); lblTrangThaiVal.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        infoPanel.add(boldLabel("Ho ten:"));    infoPanel.add(lblHoTenVal);
        infoPanel.add(boldLabel("Email:"));     infoPanel.add(lblEmailVal);
        infoPanel.add(boldLabel("Chuc vu:"));   infoPanel.add(lblChucVuVal);
        infoPanel.add(boldLabel("Phong ban:")); infoPanel.add(lblPhongBanVal);
        infoPanel.add(boldLabel("Trang thai:")); infoPanel.add(lblTrangThaiVal);

        g.gridx = 0; g.gridy = 1; g.gridwidth = 3;
        g.insets = new Insets(8, 8, 8, 8);
        f.add(infoPanel, g);
        g.gridwidth = 1; g.insets = new Insets(8, 8, 8, 8);

        // Mảng giữ maNV tìm được
        int[] maNVRef = {-1};

        // ── Hành động Tìm ──
        btnTim.addActionListener(e -> {
            String ma = txtMaNV.getText().trim();
            if (ma.isEmpty()) {
                JOptionPane.showMessageDialog(d, "Vui long nhap ma nhan vien.", "Thong bao", JOptionPane.WARNING_MESSAGE);
                return;
            }
            com.hrm.repo.AttendanceRepository.NhanVienInfo info =
                com.hrm.repo.AttendanceRepository.getInstance().findNhanVienByMa(ma);
            if (info == null) {
                infoPanel.setVisible(false);
                maNVRef[0] = -1;
                JOptionPane.showMessageDialog(d, "Khong tim thay nhan vien: " + ma, "Loi", JOptionPane.ERROR_MESSAGE);
            } else {
                maNVRef[0] = info.maNV;
                lblHoTenVal.setText(info.hoTen);
                lblEmailVal.setText(info.email.isEmpty() ? "(Chua co)" : info.email);
                lblChucVuVal.setText(info.tenChucVu.isEmpty() ? "(Chua co)" : info.tenChucVu);
                lblPhongBanVal.setText(info.tenPhongBan.isEmpty() ? "(Chua co)" : info.tenPhongBan);
                lblTrangThaiVal.setText(formatTrangThaiNV(info.trangThai));
                lblTrangThaiVal.setForeground(
                    "dang_lam_viec".equals(info.trangThai) ? UIColors.SUCCESS_GREEN : UIColors.DANGER_RED);
                infoPanel.setVisible(true);
                d.revalidate(); d.repaint();
            }
        });
        txtMaNV.addActionListener(e -> btnTim.doClick()); // Enter = Tìm

        // ── Row 2: Ngày ──
        g.gridx = 0; g.gridy = 2; g.weightx = 0; g.gridwidth = 1;
        f.add(new JLabel("Ngay (dd/MM/yyyy):"), g);
        JTextField txtNgay = new JTextField(
            LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        g.gridx = 1; g.gridwidth = 2; g.weightx = 1;
        f.add(txtNgay, g);

        // ── Row 3: Ca làm ──
        g.gridx = 0; g.gridy = 3; g.gridwidth = 1; g.weightx = 0;
        f.add(new JLabel("Ca lam:"), g);
        JComboBox<String> cCa = new JComboBox<>();
        for (CaLam ca : svc.getDanhSachCaLam()) cCa.addItem(ca.getMaCaLam() + " - " + ca.getTenCaLam());
        g.gridx = 1; g.gridwidth = 2; g.weightx = 1;
        f.add(cCa, g);

        // ── Row 4: Giờ vào ──
        g.gridx = 0; g.gridy = 4; g.gridwidth = 1; g.weightx = 0;
        f.add(new JLabel("Gio vao (HH:mm):"), g);
        JTextField tV = new JTextField("08:00");
        g.gridx = 1; g.gridwidth = 2; g.weightx = 1;
        f.add(tV, g);

        // ── Row 5: Giờ ra ──
        g.gridx = 0; g.gridy = 5; g.gridwidth = 1; g.weightx = 0;
        f.add(new JLabel("Gio ra (HH:mm):"), g);
        JTextField tR = new JTextField("17:00");
        g.gridx = 1; g.gridwidth = 2; g.weightx = 1;
        f.add(tR, g);

        // ── Row 6: Trạng thái ──
        g.gridx = 0; g.gridy = 6; g.gridwidth = 1; g.weightx = 0;
        f.add(new JLabel("Trang thai:"), g);
        JComboBox<String> cTT = new JComboBox<>(
            new String[]{"Dung gio", "Di muon", "Ve som", "Vang mat"});
        g.gridx = 1; g.gridwidth = 2; g.weightx = 1;
        f.add(cTT, g);

        // ── Row 7: Nút Lưu ──
        JButton bLuu = btn("Luu", UIColors.SUCCESS_GREEN);
        bLuu.setFont(new Font("Segoe UI", Font.BOLD, 14));
        bLuu.setPreferredSize(new Dimension(120, 38));
        g.gridx = 0; g.gridy = 7; g.gridwidth = 3;
        g.insets = new Insets(20, 8, 8, 8);
        f.add(bLuu, g);

        ChamCong.TrangThai[] tts = {
            ChamCong.TrangThai.DUNG_GIO, ChamCong.TrangThai.DI_MUON,
            ChamCong.TrangThai.VE_SOM,   ChamCong.TrangThai.VANG_MAT
        };

        bLuu.addActionListener(e -> {
            try {
                if (maNVRef[0] == -1) {
                    JOptionPane.showMessageDialog(d,
                        "Vui long tim kiem nhan vien truoc khi luu.",
                        "Thieu thong tin", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                LocalDate ngay;
                try {
                    ngay = LocalDate.parse(txtNgay.getText().trim(),
                        DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(d,
                        "Dinh dang ngay khong hop le. Dung: dd/MM/yyyy",
                        "Loi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String maCa = ((String) cCa.getSelectedItem()).split(" - ")[0].trim();
                String[] v  = tV.getText().trim().split(":");
                String[] r  = tR.getText().trim().split(":");

                ChamCong cc = new ChamCong();
                cc.setMaNV(maNVRef[0]);
                cc.setNgay(ngay);
                cc.setMaCaLam(maCa);
                CaLam caL = svc.getDanhSachCaLam().stream()
                    .filter(c -> c.getMaCaLam().equals(maCa)).findFirst().orElse(null);
                cc.setTenCaLam(caL != null ? caL.getTenCaLam() : maCa);
                cc.setGioVao(ngay.atTime(Integer.parseInt(v[0]), Integer.parseInt(v[1])));
                cc.setGioRa(ngay.atTime(Integer.parseInt(r[0]), Integer.parseInt(r[1])));
                cc.setSoGioLam(cc.tinhSoGioLam());
                cc.setPhuongThucChamCong(ChamCong.PhuongThuc.THU_CONG);
                cc.setEmployeeName(lblHoTenVal.getText());
                cc.setTrangThai(tts[cTT.getSelectedIndex()]);
                if (caL != null && cc.getSoGioLam() > caL.getSoGioChuan())
                    cc.setGioLamThem(cc.getSoGioLam() - caL.getSoGioChuan());

                com.hrm.repo.AttendanceRepository.getInstance().saveChamCong(cc);
                JOptionPane.showMessageDialog(d,
                    "Da them cham cong cho: " + lblHoTenVal.getText(),
                    "Thanh cong", JOptionPane.INFORMATION_MESSAGE);
                d.dispose();
                loadCC();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(d, "Loi: " + ex.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
            }
        });

        d.setContentPane(f);
        d.setVisible(true);
    }



    /** Chuyển trangThai DB → text hiển thị */
    private String formatTrangThaiNV(String tt) {
        return switch (tt) {
            case "dang_lam_viec" -> "Dang lam viec";
            case "tam_nghi"      -> "Tam nghi";
            case "nghi_viec"     -> "Nghi viec";
            default              -> tt;
        };
    }    
    /** Helper tạo JLabel in đậm */
    private JLabel boldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return l;
    }

    /** Chuyển trangThai DB → text hiển thị */
    // ═══════════════════════════════════
    //  TAB 2 — QUẢN LÝ CA LÀM
    // ═══════════════════════════════════
    private JPanel tabCaLam() {
        JPanel p=panel();JPanel hdr=new JPanel(new BorderLayout());hdr.setOpaque(false);
        JLabel l=new JLabel("Danh sach ca lam viec");l.setFont(new Font("Segoe UI",Font.BOLD,16));l.setForeground(UIColors.TEXT_DARK);
        hdr.add(l,BorderLayout.WEST);
        JPanel bp=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));bp.setOpaque(false);
        JButton bT=btn("+ Them ca moi",UIColors.SUCCESS_GREEN);bT.addActionListener(e->dlgCaLam(null));
        JButton bS=btn("Sua",UIColors.PRIMARY_PURPLE);bS.addActionListener(e->{
            int row=tableCaLam.getSelectedRow();if(row<0){JOptionPane.showMessageDialog(this,"Chon ca lam.");return;}
            CaLam ca=com.hrm.repo.AttendanceRepository.getInstance().findCaLamByMa((String)modelCaLam.getValueAt(row,0));
            if(ca!=null)dlgCaLam(ca);});
        JButton bX=btn("Xoa",UIColors.DANGER_RED);bX.addActionListener(e->xoaCaLam());
        bp.add(bT);bp.add(bS);bp.add(bX);hdr.add(bp,BorderLayout.EAST);p.add(hdr,BorderLayout.NORTH);
        String[]cols={"Ma ca","Ten ca","Gio bat dau","Gio ket thuc","So gio chuan","Cho phep OT","Trang thai"};
        modelCaLam=mdl(cols);tableCaLam=tbl(modelCaLam);
        tableCaLam.getColumnModel().getColumn(6).setCellRenderer(new StatusR());
        loadCaLam();p.add(new JScrollPane(tableCaLam),BorderLayout.CENTER);return p;
    }
    private void loadCaLam(){modelCaLam.setRowCount(0);DateTimeFormatter f=DateTimeFormatter.ofPattern("HH:mm");
        for(CaLam ca:svc.getTatCaCaLam())modelCaLam.addRow(new Object[]{ca.getMaCaLam(),ca.getTenCaLam(),
            ca.getGioBatDau().format(f),ca.getGioKetThuc().format(f),ca.getSoGioChuan(),
            ca.isChoPhepLamThem()?"Co":"Khong",ca.conHoatDong()?"Hoat dong":"Ngung hoat dong"});}
    private void dlgCaLam(CaLam ex){boolean edit=ex!=null;
        JDialog d=new JDialog((Frame)SwingUtilities.getWindowAncestor(this),edit?"Sua ca":"Them ca moi",true);
        d.setSize(450,380);d.setLocationRelativeTo(this);
        JPanel f=new JPanel(new GridBagLayout());f.setBorder(new EmptyBorder(20,20,20,20));
        GridBagConstraints g=gbc();DateTimeFormatter fmt=DateTimeFormatter.ofPattern("HH:mm");
        g.gridx=0;g.gridy=0;f.add(new JLabel("Ma ca:"),g);
        JTextField tMa=new JTextField(edit?ex.getMaCaLam():"",20);tMa.setEditable(!edit);
        if(edit)tMa.setBackground(new Color(240,240,240));g.gridx=1;g.weightx=1;f.add(tMa,g);
        g.gridx=0;g.gridy=1;g.weightx=0;f.add(new JLabel("Ten ca:"),g);
        JTextField tTen=new JTextField(edit?ex.getTenCaLam():"");g.gridx=1;f.add(tTen,g);
        g.gridx=0;g.gridy=2;f.add(new JLabel("Gio bat dau:"),g);
        JTextField tBD=new JTextField(edit?ex.getGioBatDau().format(fmt):"08:00");g.gridx=1;f.add(tBD,g);
        g.gridx=0;g.gridy=3;f.add(new JLabel("Gio ket thuc:"),g);
        JTextField tKT=new JTextField(edit?ex.getGioKetThuc().format(fmt):"17:00");g.gridx=1;f.add(tKT,g);
        g.gridx=0;g.gridy=4;f.add(new JLabel("So gio chuan:"),g);
        JTextField tGio=new JTextField(edit?String.valueOf(ex.getSoGioChuan()):"8.0");g.gridx=1;f.add(tGio,g);
        g.gridx=0;g.gridy=5;f.add(new JLabel("Cho phep OT:"),g);
        JCheckBox chk=new JCheckBox("Co",!edit||ex.isChoPhepLamThem());chk.setOpaque(false);g.gridx=1;f.add(chk,g);
        JButton bL=btn(edit?"Cap nhat":"Them moi",UIColors.PRIMARY_PURPLE);bL.setFont(new Font("Segoe UI",Font.BOLD,14));
        bL.addActionListener(e->{try{String ma=tMa.getText().trim(),ten=tTen.getText().trim();
            if(ma.isEmpty()||ten.isEmpty()){JOptionPane.showMessageDialog(d,"Khong de trong.");return;}
            double sg=Double.parseDouble(tGio.getText().trim());
            ServiceResult<CaLam>res=edit?svc.suaCaLam(ma,ten,tBD.getText().trim(),tKT.getText().trim(),sg,chk.isSelected())
                :svc.themCaLam(ma,ten,tBD.getText().trim(),tKT.getText().trim(),sg,chk.isSelected());
            if(res.isSuccess()){JOptionPane.showMessageDialog(d,res.getMessage());d.dispose();loadCaLam();}
            else JOptionPane.showMessageDialog(d,res.getMessage(),"Loi",JOptionPane.ERROR_MESSAGE);
        }catch(NumberFormatException x){JOptionPane.showMessageDialog(d,"So gio phai la so.");}
         catch(Exception x){JOptionPane.showMessageDialog(d,"Loi: "+x.getMessage());}});
        g.gridx=0;g.gridy=6;g.gridwidth=2;g.insets=new Insets(20,8,8,8);f.add(bL,g);
        d.setContentPane(f);d.setVisible(true);}
    private void xoaCaLam(){int row=tableCaLam.getSelectedRow();
        if(row<0){JOptionPane.showMessageDialog(this,"Chon ca.");return;}
        String ma=(String)modelCaLam.getValueAt(row,0),ten=(String)modelCaLam.getValueAt(row,1);
        if(((String)modelCaLam.getValueAt(row,6)).contains("Ngung")){JOptionPane.showMessageDialog(this,"Da ngung.");return;}
        if(JOptionPane.showConfirmDialog(this,"Ngung '"+ten+"'?","Xac nhan",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){
            svc.xoaCaLam(ma);loadCaLam();}}

    // ═══════════════════════════════════
    //  TAB 3 — DUYỆT ĐƠN OT
    // ═══════════════════════════════════
    private JPanel tabDuyetOT(){JPanel p=panel();JPanel hdr=new JPanel(new BorderLayout());hdr.setOpaque(false);
        JLabel l=new JLabel("Don dang ky lam them gio");l.setFont(new Font("Segoe UI",Font.BOLD,16));l.setForeground(UIColors.TEXT_DARK);
        hdr.add(l,BorderLayout.WEST);
        JPanel bp=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));bp.setOpaque(false);
        JButton bD=btn("Duyet",UIColors.SUCCESS_GREEN);bD.addActionListener(e->duyetOT());
        JButton bTC=btn("Tu choi",UIColors.DANGER_RED);bTC.addActionListener(e->tuChoiOT());
        JButton bHS=btn("Chinh he so",UIColors.PRIMARY_PURPLE);bHS.addActionListener(e->chinhHeSo());
        JButton bR=new JButton("Lam moi");bR.setFocusPainted(false);bR.addActionListener(e->loadOT());
        bp.add(bD);bp.add(bTC);bp.add(bHS);bp.add(bR);hdr.add(bp,BorderLayout.EAST);p.add(hdr,BorderLayout.NORTH);
        String[]cols={"Ma DK","Ma NV","Ngay","So gio","He so OT","Ly do","Trang thai","Nguoi duyet"};
        modelOT=mdl(cols);tableDonOT=tbl(modelOT);
        tableDonOT.getColumnModel().getColumn(6).setCellRenderer(new StatusR());
        tableDonOT.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean s,boolean f,int r,int c){
                super.getTableCellRendererComponent(t,v,s,f,r,c);setHorizontalAlignment(CENTER);
                setFont(new Font("Segoe UI",Font.BOLD,13));String str=v!=null?v.toString():"";
                setForeground(str.contains("2.0")||str.contains("3.0")?UIColors.DANGER_RED:UIColors.PRIMARY_PURPLE);
                return this;}});
        loadOT();p.add(new JScrollPane(tableDonOT),BorderLayout.CENTER);
        JPanel info=new JPanel(new FlowLayout(FlowLayout.LEFT,10,5));info.setOpaque(false);
        info.setBorder(new EmptyBorder(5,0,0,0));
        info.add(lbl("He so:","x1.5 (thuong), x2.0 (cuoi tuan), x3.0 (le)",UIColors.TEXT_DARK));
        p.add(info,BorderLayout.SOUTH);return p;}
    private void loadOT() {
        modelOT.setRowCount(0);
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (DangKyLamThem don : com.hrm.repo.AttendanceRepository.getInstance().findAllDonOT()) {
            // ✅ Hiển thị maNhanVien (VD: NV001) thay vì số int
            String maNhanVien = com.hrm.repo.AttendanceRepository
                .getInstance().getMaNhanVienById(don.getMaNV());
            String tenNV = don.getEmployeeName() != null
                ? don.getEmployeeName() : maNhanVien;
            modelOT.addRow(new Object[]{
                don.getMaDK(),
                maNhanVien + " - " + tenNV,
                don.getNgay() != null ? don.getNgay().format(f) : "-",
                String.format("%.1f", don.getSoGio()),
                "x" + don.getHeSoOT(),
                don.getLyDo(),
                don.getTrangThai().getDisplayName(),
                don.getApproverName() != null ? don.getApproverName() : "-"
            });
        }
    }                
    private void duyetOT(){int row=tableDonOT.getSelectedRow();
        if(row<0){JOptionPane.showMessageDialog(this,"Chon don.");return;}
        int maDK=(int)modelOT.getValueAt(row,0);String tt=(String)modelOT.getValueAt(row,6);
        if(!tt.contains("Cho")){JOptionPane.showMessageDialog(this,"Don da xu ly.");return;}
        String[]opts={"x1.5 (ngay thuong)","x2.0 (cuoi tuan)","x3.0 (ngay le)"};
        String ch=(String)JOptionPane.showInputDialog(this,"Chon he so OT:","He so OT",
            JOptionPane.QUESTION_MESSAGE,null,opts,opts[0]);if(ch==null)return;
        double hs=ch.contains("2.0")?2.0:ch.contains("3.0")?3.0:1.5;
        ServiceResult<?>res=svc.duyetDonLamThem(maDK,currentUser.getId(),hs);
        JOptionPane.showMessageDialog(this,res.getMessage());loadOT();}
    private void tuChoiOT(){int row=tableDonOT.getSelectedRow();
        if(row<0){JOptionPane.showMessageDialog(this,"Chon don.");return;}
        int maDK=(int)modelOT.getValueAt(row,0);String tt=(String)modelOT.getValueAt(row,6);
        if(!tt.contains("Cho")){JOptionPane.showMessageDialog(this,"Don da xu ly.");return;}
        svc.tuChoiDonLamThem(maDK,currentUser.getId());loadOT();}
    private void chinhHeSo(){int row=tableDonOT.getSelectedRow();
        if(row<0){JOptionPane.showMessageDialog(this,"Chon don.");return;}
        int maDK=(int)modelOT.getValueAt(row,0);
        String input=JOptionPane.showInputDialog(this,"Nhap he so moi (vd: 1.5):","Chinh he so",JOptionPane.QUESTION_MESSAGE);
        if(input==null||input.trim().isEmpty())return;
        try{double hs=Double.parseDouble(input.trim());svc.capNhatHeSoOT(maDK,hs);loadOT();
        }catch(NumberFormatException ex){JOptionPane.showMessageDialog(this,"He so phai la so.");}}

    // ═══════════════════════════════════
    //  TAB 4 — BẢNG LƯƠNG
    // ═══════════════════════════════════
    // ═══════════════════════════════════
    //  TAB BẢNG LƯƠNG
    // ═══════════════════════════════════
    private JPanel tabBangLuong() {
        JPanel p = panel();

        // ── Toolbar trên ──
        JPanel tb = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        tb.setOpaque(false);

        // Spinner tháng (1–12)
        tb.add(new JLabel("Thang:"));
        spinThangL = new JSpinner(new SpinnerNumberModel(
            LocalDate.now().getMonthValue(), 1, 12, 1));
        spinThangL.setPreferredSize(new Dimension(60, 30));
        ((JSpinner.DefaultEditor) spinThangL.getEditor()).getTextField()
            .setHorizontalAlignment(JTextField.CENTER);
        tb.add(spinThangL);

        // Spinner năm (2020 → năm hiện tại + 1)
        tb.add(new JLabel("Nam:"));
        spinNamL = new JSpinner(new SpinnerNumberModel(
            LocalDate.now().getYear(), 2020, LocalDate.now().getYear() + 1, 1));
        spinNamL.setPreferredSize(new Dimension(80, 30));
        ((JSpinner.DefaultEditor) spinNamL.getEditor()).getTextField()
            .setHorizontalAlignment(JTextField.CENTER);
        tb.add(spinNamL);

        // Label hiển thị tháng/năm đang xem
        JLabel lblThangNam = new JLabel();
        lblThangNam.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblThangNam.setForeground(UIColors.PRIMARY_PURPLE);
        Runnable refreshLabel = () -> lblThangNam.setText(String.format("  [ Thang %02d / %d ]  ",
            (int) spinThangL.getValue(), (int) spinNamL.getValue()));
        refreshLabel.run();
        spinThangL.addChangeListener(e -> refreshLabel.run());
        spinNamL.addChangeListener(e -> refreshLabel.run());
        tb.add(lblThangNam);

        tb.add(Box.createHorizontalStrut(8));
        JButton bTinh = btn("Tinh / Lam moi", UIColors.PRIMARY_PURPLE);
        tb.add(bTinh);
        JButton bCT = btn("Xem chi tiet", UIColors.INFO_BLUE);
        tb.add(bCT);
        JButton bExport = btn("Xuat Excel", new Color(33, 115, 70)); // Xanh lá đậm
        bExport.setToolTipText("Xuat bang luong ra file .xlsx");
        tb.add(bExport);
        // Nút điều chỉnh cột hiển thị
        JButton bCol = btn("Tuy chinh cot", new Color(120, 100, 180));
        tb.add(bCol);

        p.add(tb, BorderLayout.NORTH);

        // ── Bảng ──
        String[] cols = {"Ma NV", "Ho ten", "Luong chinh", "Ngay cong",
            "Gio lam", "Gio OT", "Phu cap", "Khau tru",
            "Tien OT", "Tong thu nhap", "Thuc nhan", "Trang thai"};
        modelLuong = mdl(cols);
        tableLuong = tbl(modelLuong);

        // Renderer căn phải cho cột tiền
        DefaultTableCellRenderer rightR = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                setHorizontalAlignment(RIGHT); setForeground(Color.BLACK); return this;
            }
        };
        for (int i : new int[]{2, 6, 7, 8, 9})
            tableLuong.getColumnModel().getColumn(i).setCellRenderer(rightR);

        // Cột "Thực nhận" highlight tím đậm
        tableLuong.getColumnModel().getColumn(10).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                setHorizontalAlignment(RIGHT);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setForeground(UIColors.PRIMARY_PURPLE); return this;
            }
        });
        tableLuong.getColumnModel().getColumn(11).setCellRenderer(new StatusR());

        p.add(new JScrollPane(tableLuong), BorderLayout.CENTER);

        luongStats = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 10));
        luongStats.setOpaque(false);
        luongStats.setBorder(new EmptyBorder(5, 0, 0, 0));
        p.add(luongStats, BorderLayout.SOUTH);

        // ── Sự kiện ──
        bTinh.addActionListener(e -> loadLuong());
        bCT.addActionListener(e -> xemChiTiet());
        bCol.addActionListener(e -> showColChooser());
        bExport.addActionListener(e -> xuatExcel());

        // Load tháng hiện tại lúc khởi động
        loadLuong();
        return p;
    }

    private void loadLuong() {
        int th = (int) spinThangL.getValue();
        int nm = (int) spinNamL.getValue();        modelLuong.setRowCount(0);
        dsCachedLuong.clear();

        bangLuongHienTai = svc.tinhBangLuong(th, nm);
        List<ChiTietLuong> ds = svc.getChiTietLuong(bangLuongHienTai.getMaBL());

        // ✅ Chỉ hiển thị NV có giờ công > 0
        ds = ds.stream()
            .filter(ct -> ct.getSoNgayCong() > 0 && ct.getTongGioLam() > 0)
            .collect(java.util.stream.Collectors.toList());

        dsCachedLuong.addAll(ds); // Cache lại cho Export

        if (ds.isEmpty()) {
            if (luongStats != null) {
                luongStats.removeAll();
                luongStats.add(lbl("Thang " + th + "/" + nm + ":",
                    "Khong co du lieu cham cong", Color.GRAY));
                luongStats.revalidate(); luongStats.repaint();
            }
            return;
        }

        double tQ = 0, tO = 0, tK = 0;
        for (ChiTietLuong ct : ds) {
            String maNhanVien = com.hrm.repo.AttendanceRepository
                .getInstance().getMaNhanVienById(ct.getMaNV());
            modelLuong.addRow(new Object[]{
                maNhanVien,
                ct.getTenNV(),
                fmtTien(ct.getLuongCoBan()),
                (int) ct.getSoNgayCong(),
                String.format("%.1f", ct.getTongGioLam()),
                ct.getTongGioOT() > 0 ? String.format("%.1f", ct.getTongGioOT()) : "-",
                fmtTien(ct.getTongLuongChucVu()),
                fmtTien(ct.getTongKhauTru()),
                ct.getTienOT() > 0 ? fmtTien(ct.getTienOT()) : "-",
                fmtTien(ct.getTongLuong()),
                fmtTien(ct.getLuongThucNhan()),
                ct.getTrangThai().getDisplayName()
            });
            tQ += ct.getLuongThucNhan();
            tO += ct.getTienOT();
            tK += ct.getTongKhauTru();
        }

        applyColVisibility();

        if (luongStats == null) return;
        luongStats.removeAll();
        luongStats.add(lbl("Nhan vien:", String.valueOf(ds.size()), UIColors.PRIMARY_PURPLE));
        luongStats.add(lbl("Tong quy luong:", fmtTien(tQ), UIColors.SUCCESS_GREEN));
        luongStats.add(lbl("Tong tien OT:", fmtTien(tO), UIColors.INFO_BLUE));
        luongStats.add(lbl("Tong khau tru:", fmtTien(tK), UIColors.DANGER_RED));
        luongStats.revalidate(); luongStats.repaint();
    }    /** Hộp thoại cho phép ẩn/hiện từng cột trong bảng lương ngoài */
    private void showColChooser() {
        String[] colNames = {"Ma NV", "Ho ten", "Luong chinh", "Ngay cong",
            "Gio lam", "Gio OT", "Phu cap", "Khau tru",
            "Tien OT", "Tong thu nhap", "Thuc nhan", "Trang thai"};
        // 2 cột luôn cố định: "Ho ten" (1) và "Thuc nhan" (10)
        int[] lockedCols = {1, 10};

        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Tuy chinh cot hien thi", true);
        d.setSize(320, 460);
        d.setLocationRelativeTo(this);

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel hint = new JLabel("<html><i>Tick = hien thi | Bo tick = an cot</i></html>");
        hint.setForeground(Color.GRAY);
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        main.add(hint, BorderLayout.NORTH);

        JPanel listP = new JPanel(new GridLayout(0, 1, 0, 6));
        listP.setOpaque(false);
        JCheckBox[] checks = new JCheckBox[colNames.length];
        for (int i = 0; i < colNames.length; i++) {
            checks[i] = new JCheckBox(colNames[i], !colHidden[i]);
            checks[i].setOpaque(false);
            checks[i].setFont(new Font("Segoe UI", Font.PLAIN, 13));
            // Khoá 2 cột bắt buộc
            for (int lk : lockedCols) {
                if (i == lk) {
                    checks[i].setEnabled(false);
                    checks[i].setSelected(true);
                    checks[i].setToolTipText("Cot nay khong the an");
                }
            }
            listP.add(checks[i]);
        }
        main.add(new JScrollPane(listP), BorderLayout.CENTER);

        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton bApply = btn("Ap dung", UIColors.PRIMARY_PURPLE);
        JButton bReset = btn("Hien tat ca", UIColors.SUCCESS_GREEN);
        JButton bClose = btn("Dong", Color.GRAY);
        btnP.add(bApply); btnP.add(bReset); btnP.add(bClose);
        main.add(btnP, BorderLayout.SOUTH);

        bApply.addActionListener(e -> {
            for (int i = 0; i < colNames.length; i++) {
                colHidden[i] = !checks[i].isSelected();
            }
            // Đảm bảo 2 cột cố định luôn hiện
            for (int lk : lockedCols) colHidden[lk] = false;
            applyColVisibility();
            d.dispose();
        });
        bReset.addActionListener(e -> {
            java.util.Arrays.fill(colHidden, false);
            for (JCheckBox cb : checks) cb.setSelected(true);
            applyColVisibility();
            d.dispose();
        });
        bClose.addActionListener(e -> d.dispose());

        d.setContentPane(main);
        d.setVisible(true);
    }

    /** Áp dụng trạng thái ẩn/hiện cột vào tableLuong */
    private void applyColVisibility() {
        if (tableLuong == null) return;
        for (int i = 0; i < colHidden.length; i++) {
            javax.swing.table.TableColumn col = tableLuong.getColumnModel().getColumn(i);
            if (colHidden[i]) {
                // Lưu độ rộng gốc trước khi ẩn
                if (col.getWidth() > 0) colWidthOrig[i] = col.getWidth();
                col.setMinWidth(0); col.setMaxWidth(0); col.setPreferredWidth(0);
            } else {
                // Khôi phục độ rộng gốc (fallback = 100)
                int w = colWidthOrig[i] > 0 ? colWidthOrig[i] : 100;
                col.setMinWidth(15); col.setMaxWidth(500); col.setPreferredWidth(w);
            }
        }
    }

       private void xuatExcel() {
        if (bangLuongHienTai == null || modelLuong.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "Chua co du lieu luong. Bam 'Tinh / Lam moi' truoc.",
                "Thong bao", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int th = (int) spinThangL.getValue();
        int nm = (int) spinNamL.getValue();

        // Lấy lại danh sách ChiTietLuong từ DB (đã lọc giờ công > 0)
        List<ChiTietLuong> ds = svc.getChiTietLuong(bangLuongHienTai.getMaBL())
            .stream()
            .filter(ct -> ct.getSoNgayCong() > 0 && ct.getTongGioLam() > 0)
            .collect(java.util.stream.Collectors.toList());

        if (ds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Khong co du lieu de xuat.", "Thong bao", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ── Hỏi kiểu xuất ──
        String[] options = {
            "Xuat hien tai (giu nguyen cot dang hien)",
            "Xuat day du (tat ca cot + sheet phu cap)"
        };
        int choice = JOptionPane.showOptionDialog(
            this,
            "Chon kieu xuat bieu:",
            "Xuat Excel - Thang " + String.format("%02d/%d", th, nm),
            JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
            null, options, options[0]);

        if (choice < 0) return;

        // ── Chọn nơi lưu ──
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Luu file Excel");
        fc.setSelectedFile(new java.io.File(
            String.format("BangLuong_T%02d_%d%s.xlsx",
                th, nm, choice == 1 ? "_DayDu" : "_HienTai")));
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Excel Files (*.xlsx)", "xlsx"));

        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        String path = fc.getSelectedFile().getAbsolutePath();
        if (!path.endsWith(".xlsx")) path += ".xlsx";
        final String filePath = path;
        final int finalChoice = choice;
        final List<ChiTietLuong> finalDs = ds;

        // ── Xuất trên thread riêng — tránh đơ UI ──
        new Thread(() -> {
            try {
                java.util.function.Function<Integer, String> getMaNV =
                    maNV -> com.hrm.repo.AttendanceRepository
                        .getInstance().getMaNhanVienById(maNV);

                if (finalChoice == 0) {
                    com.hrm.util.ExcelExporter.exportHienTai(
                        filePath, finalDs, colHidden, th, nm, getMaNV);
                } else {
                    com.hrm.util.ExcelExporter.exportDayDu(
                        filePath, finalDs, th, nm, getMaNV);
                }

                SwingUtilities.invokeLater(() -> {
                    int open = JOptionPane.showConfirmDialog(
                        AttendancePanel.this,
                        "Xuat thanh cong!\nFile: " + filePath + "\n\nMo file ngay?",
                        "Thanh cong", JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE);
                    if (open == JOptionPane.YES_OPTION) {
                        try {
                            java.awt.Desktop.getDesktop().open(new java.io.File(filePath));
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(AttendancePanel.this,
                                "Khong the mo file tu dong. Vui long mo thu cong.",
                                "Luu y", JOptionPane.WARNING_MESSAGE);
                        }
                    }
                });

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(AttendancePanel.this,
                        "Loi khi xuat Excel: " + ex.getMessage(),
                        "Loi", JOptionPane.ERROR_MESSAGE));
            }
        }).start();
    }



    private void xemChiTiet() {
        int row = tableLuong.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Chon nhan vien de xem."); return; }
        if (bangLuongHienTai == null) { JOptionPane.showMessageDialog(this, "Chua tinh luong."); return; }

        // Cột 0 = maNhanVien (String), cột 1 = tên
        String maNhanVienHienThi = (String) modelLuong.getValueAt(row, 0);
        String tenNV = (String) modelLuong.getValueAt(row, 1);

        List<ChiTietLuong> dsCT = svc.getChiTietLuong(bangLuongHienTai.getMaBL());
        // Tìm theo maNhanVien
        ChiTietLuong ct = dsCT.stream()
            .filter(c -> com.hrm.repo.AttendanceRepository.getInstance()
                .getMaNhanVienById(c.getMaNV()).equals(maNhanVienHienThi))
            .findFirst().orElse(null);

        if (ct == null) { JOptionPane.showMessageDialog(this, "Khong tim thay chi tiet."); return; }

        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Chi tiet luong: " + tenNV + "  [" + maNhanVienHienThi + "]", true);
        d.setSize(680, 540);
        d.setLocationRelativeTo(this);

        JPanel main = new JPanel(new BorderLayout(0, 10));
        main.setBorder(new EmptyBorder(15, 15, 15, 15));
        main.setBackground(UIColors.WHITE);

        // ── Thông tin tổng hợp (LUÔN hiện đầy đủ, không phụ thuộc colHidden) ──
        JPanel info = new JPanel(new GridLayout(0, 2, 10, 6));
        info.setOpaque(false);
        info.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Tong hop luong " +
                String.format("thang %02d/%d",
                    (int) spinThangL.getValue(), (int) spinNamL.getValue())),
            new EmptyBorder(8, 10, 8, 10)));

        java.util.function.BiConsumer<String, String> addRow = (k, v) -> {
            JLabel lk = new JLabel(k); lk.setFont(new Font("Segoe UI", Font.BOLD, 13));
            JLabel lv = new JLabel(v); lv.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            info.add(lk); info.add(lv);
        };
        addRow.accept("Nhan vien:",    tenNV + "  (" + maNhanVienHienThi + ")");
        addRow.accept("Ngay cong:",    String.valueOf((int) ct.getSoNgayCong()));
        addRow.accept("Tong gio lam:", String.format("%.1f gio", ct.getTongGioLam()));
        addRow.accept("Gio OT:",       ct.getTongGioOT() > 0 ? String.format("%.1f gio", ct.getTongGioOT()) : "-");
        addRow.accept("Luong chinh:",  fmtTien(ct.getLuongCoBan()));
        addRow.accept("Tien OT:",      ct.getTienOT() > 0 ? fmtTien(ct.getTienOT()) : "-");
        addRow.accept("Phu cap:",      fmtTien(ct.getTongLuongChucVu()));
        addRow.accept("Khau tru:",     fmtTien(ct.getTongKhauTru()));
        addRow.accept("Tong thu nhap:", fmtTien(ct.getTongLuong()));

        // Dòng "Thực nhận" nổi bật
        JLabel lkTN = new JLabel("THUC NHAN:");
        lkTN.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JLabel lvTN = new JLabel(fmtTien(ct.getLuongThucNhan()));
        lvTN.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lvTN.setForeground(UIColors.PRIMARY_PURPLE);
        info.add(lkTN); info.add(lvTN);

        main.add(info, BorderLayout.NORTH);

        // ── Bảng thành phần lương (phụ cấp / khấu trừ) ──
        String[] cols = {"Loai", "Ten khoan", "So tien", "Nguon"};
        DefaultTableModel mdlTP = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tblTP = tbl(mdlTP);
        tblTP.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                String str = v != null ? v.toString() : "";
                setForeground(str.contains("cap") ? UIColors.SUCCESS_GREEN : UIColors.DANGER_RED);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setHorizontalAlignment(CENTER); return this;
            }
        });
        tblTP.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                setHorizontalAlignment(RIGHT); setForeground(Color.BLACK); return this;
            }
        });

        for (ThanhPhanLuong tp : ct.getDanhSachThanhPhan()) {
            String px = tp.getLoai() == ThanhPhanLuong.Loai.PHU_CAP ? "+" : "-";
            mdlTP.addRow(new Object[]{
                tp.getLoai().getDisplayName(), tp.getTenKhoan(),
                px + " " + fmtTien(tp.getSoTien()), tp.getNguon()
            });
        }

        JScrollPane scrollTP = new JScrollPane(tblTP);
        scrollTP.setBorder(BorderFactory.createTitledBorder("Thanh phan phu cap / khau tru"));
        main.add(scrollTP, BorderLayout.CENTER);

        JButton btnDong = btn("Dong", UIColors.PRIMARY_PURPLE);
        btnDong.addActionListener(e -> d.dispose());
        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnP.setOpaque(false); btnP.add(btnDong);
        main.add(btnP, BorderLayout.SOUTH);

        d.setContentPane(main);
        d.setVisible(true);
    }
    // ═══════════════════════════════════════════════
    //  TAB 5 — QUẢN LÝ PHỤ CẤP & KHẤU TRỪ
    // ═══════════════════════════════════════════════

    private JPanel tabPhuCap() {
        JPanel p = panel();

        // Header
        JPanel hdr = new JPanel(new BorderLayout()); hdr.setOpaque(false);
        JLabel l = new JLabel("Cau hinh phu cap & khau tru");
        l.setFont(new Font("Segoe UI", Font.BOLD, 16)); l.setForeground(UIColors.TEXT_DARK);
        hdr.add(l, BorderLayout.WEST);

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); bp.setOpaque(false);
        JButton bThem = btn("+ Them khoan moi", UIColors.SUCCESS_GREEN);
        bThem.addActionListener(e -> dlgPhuCap(null));
        JButton bSua = btn("Sua", UIColors.PRIMARY_PURPLE);
        bSua.addActionListener(e -> {
            int row = tablePC.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Chon khoan de sua."); return; }
            int maPC = (int) modelPC.getValueAt(row, 0);
            CauHinhPhuCap pc = com.hrm.repo.AttendanceRepository.getInstance().findCauHinhPCById(maPC);
            if (pc != null) dlgPhuCap(pc);
        });
        JButton bXoa = btn("Xoa", UIColors.DANGER_RED);
        bXoa.addActionListener(e -> xoaPhuCap());
        bp.add(bThem); bp.add(bSua); bp.add(bXoa);
        hdr.add(bp, BorderLayout.EAST);
        p.add(hdr, BorderLayout.NORTH);

        // Bảng
        String[] cols = {"Ma", "Loai", "Ten khoan", "Kieu tinh", "Gia tri", "Nguon", "Trang thai"};
        modelPC = mdl(cols);
        tablePC = tbl(modelPC);

        // Renderer loại
        tablePC.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                String str = v != null ? v.toString() : "";
                setForeground(str.contains("cap") ? UIColors.SUCCESS_GREEN : UIColors.DANGER_RED);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setHorizontalAlignment(CENTER); return this;
            }
        });

        // Renderer giá trị — align phải
        tablePC.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                setHorizontalAlignment(RIGHT); setForeground(Color.BLACK);
                setFont(new Font("Segoe UI", Font.BOLD, 13)); return this;
            }
        });

        // Renderer trạng thái
        tablePC.getColumnModel().getColumn(6).setCellRenderer(new StatusR());

        loadPhuCap();
        p.add(new JScrollPane(tablePC), BorderLayout.CENTER);

        // Info panel
        JPanel info = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5)); info.setOpaque(false);
        info.setBorder(new EmptyBorder(5, 0, 0, 0));
        info.add(lbl("Kieu tinh:", "'Co dinh' = so tien cu the, '% Luong CB' = phan tram luong co ban", UIColors.TEXT_GRAY));
        p.add(info, BorderLayout.SOUTH);

        return p;
    }

    private void loadPhuCap() {
        modelPC.setRowCount(0);
        for (CauHinhPhuCap pc : svc.getAllCauHinhPC()) {
            String giaTri;
            if (pc.getKieuTinh() == CauHinhPhuCap.KieuTinh.PHAN_TRAM) {
                giaTri = pc.hienThiGiaTri();
            } else {
                giaTri = fmtTien(pc.getGiaTri());
            }
            modelPC.addRow(new Object[]{
                pc.getMaPC(),
                pc.getLoai().getDisplayName(),
                pc.getTenKhoan(),
                pc.getKieuTinh().getDisplayName(),
                giaTri,
                pc.getNguon(),
                pc.isHoatDong() ? "Hoat dong" : "Ngung"
            });
        }
    }

    private void dlgPhuCap(CauHinhPhuCap existing) {
        boolean edit = (existing != null);
        String title = edit ? "Sua: " + existing.getTenKhoan() : "Them khoan moi";

        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        d.setSize(500, 400); d.setLocationRelativeTo(this);

        JPanel f = new JPanel(new GridBagLayout());
        f.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints g = gbc();

        // Loại
        g.gridx = 0; g.gridy = 0;
        f.add(new JLabel("Loai:"), g);
        JComboBox<String> cboLoai = new JComboBox<>(new String[]{"Phu cap", "Khau tru"});
        if (edit) cboLoai.setSelectedIndex(existing.getLoai() == ThanhPhanLuong.Loai.PHU_CAP ? 0 : 1);
        g.gridx = 1; g.weightx = 1; f.add(cboLoai, g);

        // Tên khoản
        g.gridx = 0; g.gridy = 1; g.weightx = 0;
        f.add(new JLabel("Ten khoan:"), g);
        JTextField tTen = new JTextField(edit ? existing.getTenKhoan() : "", 25);
        g.gridx = 1; g.weightx = 1; f.add(tTen, g);

        // Kiểu tính
        g.gridx = 0; g.gridy = 2; g.weightx = 0;
        f.add(new JLabel("Kieu tinh:"), g);
        JComboBox<String> cboKieu = new JComboBox<>(new String[]{"Co dinh (so tien)", "% Luong co ban"});
        if (edit) cboKieu.setSelectedIndex(existing.getKieuTinh() == CauHinhPhuCap.KieuTinh.CO_DINH ? 0 : 1);
        g.gridx = 1; f.add(cboKieu, g);

        // Giá trị
        g.gridx = 0; g.gridy = 3; g.weightx = 0;
        f.add(new JLabel("Gia tri:"), g);
        JTextField tGiaTri = new JTextField(edit ? String.valueOf(existing.getGiaTri()) : "");
        g.gridx = 1; f.add(tGiaTri, g);

        // Hint giá trị
        g.gridx = 1; g.gridy = 4;
        JLabel lblHint = new JLabel("VD: 500000 (co dinh) hoac 8 (phan tram)");
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblHint.setForeground(UIColors.TEXT_LIGHT_GRAY);
        f.add(lblHint, g);

        // Cập nhật hint theo kiểu tính
        cboKieu.addActionListener(e -> {
            if (cboKieu.getSelectedIndex() == 0) {
                lblHint.setText("VD: 500000 (dong), 1000000 (dong)");
            } else {
                lblHint.setText("VD: 8 (= 8% luong co ban), 1.5 (= 1.5%)");
            }
        });

        // Nguồn
        g.gridx = 0; g.gridy = 5; g.weightx = 0;
        f.add(new JLabel("Nguon:"), g);
        JComboBox<String> cboNguon = new JComboBox<>(new String[]{"CongTy", "LuatDinh", "ChucVu", "Khac"});
        if (edit) cboNguon.setSelectedItem(existing.getNguon());
        g.gridx = 1; f.add(cboNguon, g);

        // Nút lưu
        JButton bLuu = btn(edit ? "Cap nhat" : "Them moi", UIColors.PRIMARY_PURPLE);
        bLuu.setFont(new Font("Segoe UI", Font.BOLD, 14));
        bLuu.addActionListener(e -> {
            try {
                String ten = tTen.getText().trim();
                if (ten.isEmpty()) {
                    JOptionPane.showMessageDialog(d, "Ten khoan khong duoc de trong.", "Loi", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                double giaTri = Double.parseDouble(tGiaTri.getText().trim());
                if (giaTri <= 0) {
                    JOptionPane.showMessageDialog(d, "Gia tri phai lon hon 0.", "Loi", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                ThanhPhanLuong.Loai loai = cboLoai.getSelectedIndex() == 0
                        ? ThanhPhanLuong.Loai.PHU_CAP : ThanhPhanLuong.Loai.KHAU_TRU;
                CauHinhPhuCap.KieuTinh kieu = cboKieu.getSelectedIndex() == 0
                        ? CauHinhPhuCap.KieuTinh.CO_DINH : CauHinhPhuCap.KieuTinh.PHAN_TRAM;
                String nguon = (String) cboNguon.getSelectedItem();

                ServiceResult<?> res;
                if (edit) {
                    res = svc.suaCauHinhPC(existing.getMaPC(), loai, ten, kieu, giaTri, nguon);
                } else {
                    res = svc.themCauHinhPC(loai, ten, kieu, giaTri, nguon);
                }

                if (res.isSuccess()) {
                    JOptionPane.showMessageDialog(d, res.getMessage(), "Thanh cong", JOptionPane.INFORMATION_MESSAGE);
                    d.dispose(); loadPhuCap();
                } else {
                    JOptionPane.showMessageDialog(d, res.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(d, "Gia tri phai la so.\nVD: 500000 hoac 8", "Loi", JOptionPane.ERROR_MESSAGE);
            }
        });

        g.gridx = 0; g.gridy = 6; g.gridwidth = 2; g.insets = new Insets(20, 8, 8, 8);
        f.add(bLuu, g);

        d.setContentPane(f); d.setVisible(true);
    }

    private void xoaPhuCap() {
        int row = tablePC.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Chon khoan de xoa."); return; }

        int maPC = (int) modelPC.getValueAt(row, 0);
        String ten = (String) modelPC.getValueAt(row, 2);
        String tt = (String) modelPC.getValueAt(row, 6);

        if (tt.contains("Ngung")) {
            JOptionPane.showMessageDialog(this, "Khoan nay da ngung.");
            return;
        }

        int cf = JOptionPane.showConfirmDialog(this,
                "Ngung khoan '" + ten + "'?\n(Se khong ap dung khi tinh luong nua)",
                "Xac nhan", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (cf == JOptionPane.YES_OPTION) {
            ServiceResult<Void> res = svc.xoaCauHinhPC(maPC);
            JOptionPane.showMessageDialog(this, res.getMessage());
            loadPhuCap();
        }
    }

    // ═══════════════════════════════════
    //  TAB — EMPLOYEE (TODO)
    // ═══════════════════════════════════
    private JPanel tabCaNhan() {
        JPanel p = new JPanel(new GridBagLayout()); p.setBackground(UIColors.WHITE);
        JLabel l = new JLabel("Chuc nang cham cong ca nhan — Dang phat trien...");
        l.setFont(new Font("Segoe UI", Font.PLAIN, 16)); l.setForeground(UIColors.TEXT_GRAY);
        p.add(l); return p;
    }

    // ═══════════════════════════════════
    //  HELPER
    // ═══════════════════════════════════
    private String fmtTien(double a) { return moneyFmt.format(Math.round(a)) + " d"; }
    private JPanel panel() {
        JPanel p = new JPanel(new BorderLayout(0, 10)); p.setBackground(UIColors.WHITE);
        p.setBorder(new EmptyBorder(15, 15, 15, 15)); return p;
    }
    private JButton btn(String text, Color bg) {
        JButton b = new JButton(text); b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR)); return b;
    }
    private JTable tbl(DefaultTableModel m) {
        JTable t = new JTable(m); t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setForeground(Color.BLACK); t.setRowHeight(32);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.getTableHeader().setBackground(UIColors.PRIMARY_PURPLE);
        t.getTableHeader().setForeground(Color.BLACK);
        t.setSelectionBackground(UIColors.LIGHT_PURPLE);
        t.setSelectionForeground(Color.BLACK);
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(table, v, s, f, r, c);
                setForeground(Color.BLACK); return this;
            }
        });
        return t;
    }
    private DefaultTableModel mdl(String[] cols) {
        return new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
    }
    private JLabel lbl(String label, String val, Color color) {
        JLabel l = new JLabel(label + " " + val); l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(color); return l;
    }
    private GridBagConstraints gbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8); g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL; return g;
    }
    private JComboBox<String> combMonth() {
        JComboBox<String> c = new JComboBox<>();
        for (int i = 1; i <= 12; i++) c.addItem(String.valueOf(i));
        c.setSelectedIndex(LocalDate.now().getMonthValue() - 1); return c;
    }
    private JComboBox<String> combYear() {
        JComboBox<String> c = new JComboBox<>(); int y = LocalDate.now().getYear();
        for (int yr = y - 2; yr <= y; yr++) c.addItem(String.valueOf(yr));
        c.setSelectedIndex(2); return c;
    }
    private static class StatusR extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, s, f, r, c);
            String str = v != null ? v.toString() : "";
            if (str.contains("muon") || str.contains("som") || str.contains("choi")) setForeground(UIColors.DANGER_RED);
            else if (str.contains("Cho") || str.contains("Chua")) setForeground(new Color(200, 150, 0));
            else if (str.contains("Dung") || str.contains("duyet") || str.contains("Hoat dong") || str.contains("tinh"))
                setForeground(UIColors.SUCCESS_GREEN);
            else if (str.contains("Vang") || str.contains("Ngung")) setForeground(Color.GRAY);
            else setForeground(UIColors.TEXT_DARK);
            setHorizontalAlignment(CENTER); return this;
        }
    }
}